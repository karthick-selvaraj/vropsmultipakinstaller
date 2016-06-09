/* *************************************************************************
 * Copyright 2013 VMware, Inc.  All rights reserved. VMware Confidential
 * *************************************************************************/

package com.vmware.vrops.restclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.vmware.vrops.exception.RestCallFailedException;
import com.vmware.vrops.exception.TrustManagerCertFailedException;
import com.vmware.vrops.pakfile.PakFileStatus;
import com.vmware.vrops.pakfile.PakSliceStatus;
import com.vmware.vrops.pakfile.RestServiceStatus;

/**
 * This class provides rest functionality to upload and install pak files
 *
 * Created by kselvaraj on 5/4/15
 *
 */
public class OpsAPIHelper {
   private static Logger log = Logger.getLogger(OpsAPIHelper.class);
   private static final String OPS_URL = "https://OpsIP:443/casa";
   public static int lastCallResponseCode;

   /**
    * This function reads the inputstream from rest call response.
    *
    * @param inputStream
    *           This containst the response from rest call.
    * @return
    * @throws IOException
    */
   public static String readInputStream(InputStream inputStream)
         throws IOException {
      if (inputStream == null) {
         throw new IOException("Inputstream is null");
      }
      BufferedReader reader =
            new BufferedReader(new InputStreamReader(inputStream));
      String tmp;
      StringBuilder sb = new StringBuilder();
      while ((tmp = reader.readLine()) != null) {
         sb.append(tmp).append("\n");
      }
      if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
         sb.setLength(sb.length() - 1);
      }
      reader.close();
      return sb.toString();
   }

   /**
    * This function returns base64 encoding credentials
    *
    * @param input
    *           this contains the username:password string
    * @return
    */
   public static String getBase64EncodedString(String input) {
      if (input == null || input.isEmpty()) {
         throw new IllegalArgumentException(
               "Cannot Base64 encode a null or empty string");
      }
      byte[] encoded = Base64.encodeBase64(input.getBytes());
      return new String(encoded);
   }

   /**
    * Upload PakFiles on a particular node
    *
    * @param nodeIp
    *           this contains the particular node ip address
    * @param pakFile
    *           this contains the zip file path
    * @param base64Credentials
    *           this contains the base64 encoded credential
    * @return returns RestServiceStatus bean
    * @throws Exception
    */
   public static RestServiceStatus uploadPakFiles(String nodeIp,
         String pakFile, String base64Credentials, boolean force)
         throws JSONException, RestCallFailedException,
         TrustManagerCertFailedException {
      String responseString;
      String restUrl =
            OPS_URL.replace("OpsIP", nodeIp)
                  + "/upgrade/cluster/pak/reserved/operation/upload";
      NameValuePair filePair = new BasicNameValuePair("contents", pakFile);
      log.info(" Uploading of " + pakFile + " starts");
      InputStream responseStream =
            RestAPIHelper.postCallMultipart(restUrl, null, filePair,
                  base64Credentials, force);
      lastCallResponseCode = RestAPIHelper.getLastCallResponseCode();
      log.debug("response for " + pakFile + " is " + lastCallResponseCode);
      try {
         responseString = readInputStream(responseStream);
         responseStream.close();
         responseStream = null;
      } catch (IOException e) {
         throw new RestCallFailedException(
               "Uploading pak files rest call failed, please check the network");
      }

      finally {
         if (responseStream != null) {
            try {
               responseStream.close();
            } catch (IOException e) {
               throw new RestCallFailedException(
                     "Uploading pak files rest call failed");
            }
         }
      }
      return new RestServiceStatus(lastCallResponseCode, responseString);
   }

   /**
    * Install a Pak File on a particular node.
    *
    * @param nodeIp
    *           this contains the node ip address
    * @param pakFileId
    *           this contains the uploaded pakfile id
    * @throws Exception
    */
   public static int installPakFile(String nodeIp, String pakFileId,
         String base64Credentials) throws TrustManagerCertFailedException,
         RestCallFailedException {
      String restUrl =
            OPS_URL.replace("OpsIP", nodeIp) + "/upgrade/cluster/pak/"
                  + pakFileId + "/operation/install";
      RestAPIHelper.postCall(restUrl, "", base64Credentials);
      lastCallResponseCode = RestAPIHelper.getLastCallResponseCode();
      return lastCallResponseCode;
   }

   /**
    * Get Pak File Status on a particular node for a given scope
    * (slice/cluster).
    *
    * @param nodeIp
    *           this contains the node ip address
    * @param pakFileId
    *           this contains the pakfile id
    * @param scope
    *           this scope decides the whether the status info from slice of
    *           cluster
    * @return PakFileStatus this contains the pakfile install status of a
    *         particular pakfile id
    * @throws Exception
    */
   public static PakFileStatus getPakFileStatus(String nodeIp,
         String pakFileId, String scope, String base64Credentials)
         throws TrustManagerCertFailedException, JSONException,
         RestCallFailedException {
      PakFileStatus pakStatus = new PakFileStatus(pakFileId);
      String restUrl =
            OPS_URL.replace("OpsIP", nodeIp) + "/upgrade/" + scope + "/pak/"
                  + pakFileId + "/status";
      InputStream responseStream = null;
      List<PakSliceStatus> perSliceStatusList;
      perSliceStatusList = new ArrayList<PakSliceStatus>();
      try {
         responseStream = RestAPIHelper.restCall(restUrl, base64Credentials);
         String responseString = readInputStream(responseStream);
         responseStream.close();
         responseStream = null;
         JSONObject status = new JSONObject(responseString);
         lastCallResponseCode = RestAPIHelper.getLastCallResponseCode();
         if (scope.equals("slice")) {
            pakStatus.addSliceStatus(new PakSliceStatus(nodeIp, status
                  .getString("pak_state"), status
                  .getString("pak_install_status")));
         } else {
            pakStatus.setClusterPakInstallStatus(status
                  .getString("cluster_pak_install_status"));
            JSONArray array = status.getJSONArray("slices");
            for (int i = 0; i < array.length(); i++) {
               perSliceStatusList.add(new PakSliceStatus(array.getJSONObject(i)
                     .getString("slice_address"), array.getJSONObject(i)
                     .getJSONObject("document").getString("pak_state"), array
                     .getJSONObject(i).getJSONObject("document")
                     .getString("pak_install_status")));
               pakStatus.setSliceStatus(perSliceStatusList);
            }
         }
      } catch (IOException e) {
         throw new RestCallFailedException("Unable to get pak file status");
      } finally {
         if (responseStream != null)
            try {
               responseStream.close();
            } catch (IOException e) {
               throw new RestCallFailedException(
                     "Unable to get pak file status");
            }
      }

      return pakStatus;
   }
}
