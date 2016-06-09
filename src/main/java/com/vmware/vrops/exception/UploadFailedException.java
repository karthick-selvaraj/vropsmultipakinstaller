/* *************************************************************************
 * Copyright 2013 VMware, Inc.  All rights reserved. VMware Confidential
 * *************************************************************************/

package com.vmware.vrops.exception;

/**
 * This exception class used to denote an upload pak file status
 *
 * Created by kselvaraj on 5/4/15.
 */

public class UploadFailedException extends Exception {

   private String message = null;

   public UploadFailedException() {
      super();
   }

   public UploadFailedException(String message) {
      super(message);
      this.message = message;
   }

   public UploadFailedException(Throwable cause) {
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
