/* *************************************************************************
 * Copyright 2013 VMware, Inc.  All rights reserved. VMware Confidential
 * *************************************************************************/

package com.vmware.vrops.exception;

/**
 * This class used to denote a specific type of exception which is used to
 * differentiate the action - Validate ipaddress.
 *
 * Created by kselvaraj on 5/4/15.
 */

public class InvalidIPException extends Exception {

   private String message = null;

   public InvalidIPException() {
      super();
   }

   public InvalidIPException(String message) {
      super(message);
      this.message = message;
   }

   public InvalidIPException(Throwable cause) {
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
