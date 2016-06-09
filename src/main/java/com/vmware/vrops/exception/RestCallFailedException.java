/* *************************************************************************
 * Copyright 2013 VMware, Inc.  All rights reserved. VMware Confidential
 * *************************************************************************/

package com.vmware.vrops.exception;


/**
 * This class used to denote a specific type of exception which is used to
 * differentiate the action - Restcall failures will show different rest apis
 * failures and their respective messages
 *
 * Created by kselvaraj on 5/4/15.
 */
public class RestCallFailedException extends Exception {

   private String message = null;

   public RestCallFailedException() {
      super();
   }

   public RestCallFailedException(String message) {
      super(message);
      this.message = message;
   }

   public RestCallFailedException(Throwable cause) {
      super(cause);
   }

   @Override
   public String toString() {
      return message;
   }

   @Override
   public String getMessage() {
      return message;
   }
}
