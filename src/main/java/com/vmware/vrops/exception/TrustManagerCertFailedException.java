/* *************************************************************************
 * Copyright 2013 VMware, Inc.  All rights reserved. VMware Confidential
 * *************************************************************************/

package com.vmware.vrops.exception;

/**
 * This exception class used to denote a specific certificate trust failure
 *
 * Created by kselvaraj on 5/4/15.
 */
public class TrustManagerCertFailedException extends Exception {

   private String message = null;

   public TrustManagerCertFailedException() {
      super();
   }

   public TrustManagerCertFailedException(String message) {
      super(message);
      this.message = message;
   }

   public TrustManagerCertFailedException(Throwable cause) {
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
