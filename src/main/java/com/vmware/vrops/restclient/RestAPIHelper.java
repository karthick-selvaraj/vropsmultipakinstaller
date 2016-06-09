/* *************************************************************************
 * Copyright 2013 VMware, Inc.  All rights reserved. VMware Confidential
 * *************************************************************************/

package com.vmware.vrops.restclient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.NameValuePair;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import com.vmware.vrops.exception.RestCallFailedException;
import com.vmware.vrops.exception.TrustManagerCertFailedException;

/**
 * This class provides basic rest functionality to access any rest service
 *
 * Created by kselvaraj on 5/4/15
 */
public class RestAPIHelper {

   private static int lastCallResponseCode;
   private static final String HTTP_BASIC_AUTH = "Basic";
   private static final String SINGLE_SPACE = " ";
   private static String contentType_json = "application/json";
   private static boolean trust_cert = false;

   static {
      trustCerts();
      trust_cert = true;
   }

   /**
    * This function to trust the SSL certs
    */
   private static void trustCerts() {
      // Creating a trust manager that does not validate certificate chains
      TrustManager[] trustAllCerts =
            new TrustManager[] { new X509TrustManager() {

               @Override
               public X509Certificate[] getAcceptedIssuers() {
                  return null;
               }

               @Override
               public void checkClientTrusted(X509Certificate[] certs,
                     String authType) {
               }

               @Override
               public void checkServerTrusted(X509Certificate[] certs,
                     String authType) {
               }
            } };

      // Installing the all-trusting trust manager
      try {
         SSLContext sc = SSLContext.getInstance("TLS");
         sc.init(null, trustAllCerts, new SecureRandom());
         HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
      } catch (Exception e) {
         trust_cert = false;
         e.printStackTrace();
      }
      // Promiscuous Hostname Verifier
      HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
         @Override
         public boolean verify(String hostname, SSLSession session) {
            return true;
         }
      });
   }

   /**
    * This method returns last call response code.
    *
    * @return returns last call response code.
    */
   public static int getLastCallResponseCode() {
      return lastCallResponseCode;
   }

   /**
    * This function post the requestBody in to the rest service pointed by
    * restUrl
    *
    * @param restUrl
    *           This contains the restUrl
    * @param requestBody
    *           This contains the requestBody to be posted in to rest service
    * @param base64UserCredential
    *           This contains the base64 encoded credential to access the rest
    *           service
    * @return returns Inputstream of the response
    */
   public static void postCall(String restUrl, String requestBody,
         String base64UserCredential) throws TrustManagerCertFailedException,
         RestCallFailedException {
      URL url = null;
      HttpURLConnection httpCon = null;
      OutputStream outputsream = null;
      String charset = "UTF-8";
      lastCallResponseCode = -1;
      try {

         if (!trust_cert) {
            throw new TrustManagerCertFailedException(
                  "Unable to verify the SSL certificate");
         }

         url = new URL(restUrl);
         httpCon = (HttpURLConnection) url.openConnection();
         httpCon.setUseCaches(false);
         httpCon.setDoOutput(true); // Triggers POST.
         httpCon.setRequestMethod("POST");
         httpCon.setRequestProperty("Accept-Charset", charset);
         httpCon.setRequestProperty("Content-Type", "application/json");
         httpCon.setRequestProperty("Authorization", HTTP_BASIC_AUTH
               + SINGLE_SPACE + base64UserCredential);

         if (null != requestBody) {
            outputsream = httpCon.getOutputStream();
            outputsream.write(requestBody.getBytes(charset));
         }
         lastCallResponseCode = httpCon.getResponseCode();
         httpCon.disconnect();
         outputsream.close();
         httpCon = null;
         outputsream = null;
      } catch (IOException e) {
         throw new RestCallFailedException(
               "post call to initiate pak install failed");
      } finally {
         if (httpCon != null)
            httpCon.disconnect();
         if (outputsream != null)
            try {
               outputsream.close();
            } catch (IOException e) {
               throw new RestCallFailedException(
                     "post call to initiate pak install failed");
            }
      }
   }

   /**
    * This function uploaded the pak files in multipart form data.
    *
    * @param restUrl
    *           This contains the rest service url
    * @param requestBody
    *           This contains the requestBody of the data to be posted
    * @param uploadFile
    *           This contains the pak file path
    * @param base64UserCredential
    *           This contains the base64 encoded credential
    * @param force
    *           This forces the pak file upload, this will overwrite the
    *           existing pak
    * @return
    */
   public static InputStream postCallMultipart(String restUrl,
         String requestBody, NameValuePair uploadFile,
         String base64UserCredential, boolean force)
         throws TrustManagerCertFailedException {
      URL url = null;
      InputStream inputstream_response = null;
      HttpURLConnection httpCon = null;
      OutputStream outputstream = null;
      String charset = "UTF-8";
      MultipartEntity reqEntity =
            new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

      if (!trust_cert) {
         throw new TrustManagerCertFailedException(
               "Unable to verify the SSL certificate");
      }
      lastCallResponseCode = -1;
      try {

         String fileName = uploadFile.getName();
         File fileHandler = new File(uploadFile.getValue());
         FileBody fileBody = new FileBody(fileHandler);
         reqEntity.addPart(fileName, fileBody);

         if (null != requestBody && !requestBody.isEmpty()) {
            reqEntity.addPart("rawData", new StringBody(requestBody));
         }

         if (force) {
            reqEntity.addPart("pak_handling_advice", new StringBody("CLOBBER"));
            reqEntity.addPart("force_content_update", new StringBody("true"));
         }

         url = new URL(restUrl);
         httpCon = (HttpURLConnection) url.openConnection();
         httpCon.setUseCaches(false);
         httpCon.setDoInput(true);
         httpCon.setDoOutput(true);
         httpCon.setRequestMethod("POST");
         httpCon.setRequestProperty("Accept", "application/json");
         httpCon.setRequestProperty("Accept-Charset", charset);
         httpCon.addRequestProperty("Content-length",
               reqEntity.getContentLength() + "");
         httpCon.addRequestProperty(reqEntity.getContentType().getName(),
               reqEntity.getContentType().getValue());
         httpCon.setRequestProperty("Authorization", HTTP_BASIC_AUTH
               + SINGLE_SPACE + base64UserCredential);
         outputstream = httpCon.getOutputStream();
         reqEntity.writeTo(outputstream);
         outputstream.close();
         httpCon.connect();

         lastCallResponseCode = httpCon.getResponseCode();
         inputstream_response = httpCon.getInputStream();
      } catch (IOException e) {
         inputstream_response = httpCon.getErrorStream();
      }
      return inputstream_response;
   }

   /**
    * This function is GET information from rest sercice
    *
    * @param api
    *           This contains the rest URL
    * @param base64UserCredential
    *           This contains the base64 credential
    * @return
    */
   public static InputStream restCall(String api, String base64UserCredential)
         throws TrustManagerCertFailedException {
      InputStream inputstream_response;
      URLConnection conn;
      HttpURLConnection httpConn = null;
      lastCallResponseCode = -1;
      if (!trust_cert) {
         throw new TrustManagerCertFailedException(
               "Unable to verify the SSL certificate");
      }
      try {
         URL url = new URL(api);
         conn = url.openConnection();
         httpConn = (HttpURLConnection) conn;
         StringBuilder authHeader = new StringBuilder();
         authHeader.append(HTTP_BASIC_AUTH);
         authHeader.append(SINGLE_SPACE);
         authHeader.append(base64UserCredential);
         conn.setRequestProperty("Authorization", authHeader.toString());
         conn.setRequestProperty("Accept", contentType_json);
         lastCallResponseCode = httpConn.getResponseCode();
         inputstream_response = httpConn.getInputStream();

      } catch (IOException e) {
         inputstream_response = httpConn.getErrorStream();
      }
      return inputstream_response;
   }
}
