/* *************************************************************************
 * Copyright 2013 VMware, Inc.  All rights reserved. VMware Confidential
 * *************************************************************************/

package com.vmware.vrops.pakfile;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * RestServiceStatus bean stores the rest call responses
 *
 * Created by kselvaraj on 5/4/15
 *
 */
public class RestServiceStatus {

   static Logger log = Logger.getLogger(RestServiceStatus.class);

   private int responsecode;
   private String responseString;
   private String responseKey;
   private String pakId;
   private final int PACKAGE_UPLOAD_SUCCESS = 202;
   private final int PACKAGE_UPLOAD_FAILURE = 200;

   public RestServiceStatus(int responseCode, String responseString)
         throws JSONException {
      this.responsecode = responseCode;
      this.responseString = responseString;
      try {
         setResponseKey();
      } catch (JSONException e) {
         log.debug("Unable to parse the rest call response from vR Ops");
         throw e;
      }
   }

   public int getResponsecode() {
      return responsecode;
   }

   public void setResponsecode(int responsecode) {
      this.responsecode = responsecode;
   }

   public String getResponseString() {
      return responseString;
   }

   public void setResponseString(String responseString) {
      this.responseString = responseString;
   }

   /**
    * This function parses the response from the rest call and updates the
    * various attributes of bean
    *
    * @throws JSONException
    */
   private void setResponseKey() throws JSONException {
      JSONObject response = new JSONObject(this.responseString);
      if (this.responsecode == PACKAGE_UPLOAD_SUCCESS) {
         this.responseKey = "Package Uploaded";
         this.pakId = response.getString("pak_id").trim();
      } else if (this.responsecode == PACKAGE_UPLOAD_FAILURE) {
         this.responseKey = response.getString("error_message_key");
         this.pakId = response.getString("pak_id").trim();
      } else {
         String[] temp_error = null;
         temp_error = response.getString("error_arguments").split("\"");
         if (temp_error != null && temp_error.length > 1
               && temp_error[1] != null) {
            this.pakId = temp_error[1];
         }
         this.responseKey = response.getString("error_message_key");
      }
   }

   public String getResponseKey() {
      return this.responseKey;
   }

   public String getPakId() {
      return this.pakId;
   }

   @Override
   public String toString() {
      return "RestServiceStatus : ResponseCode : " + responsecode
            + ", ResponseKey : " + responseKey + " PakId : " + pakId
            + "  ResponseString : " + responseString;
   }
}
