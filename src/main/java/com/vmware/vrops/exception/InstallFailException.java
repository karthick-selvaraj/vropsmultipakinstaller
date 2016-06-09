/* *************************************************************************
 * Copyright 2013 VMware, Inc.  All rights reserved. VMware Confidential
 * *************************************************************************/

package com.vmware.vrops.exception;

/**
 * This class used to denote a specific type of exception which is used to
 * differentiate the action - Installation Failure status.
 *
 * Created by kselvaraj on 5/4/15.
 */

public class InstallFailException extends Exception {

   private String message = null;

   public InstallFailException() {
      super();
   }

   public InstallFailException(String message) {
      super(message);
      this.message = message;
   }

   public InstallFailException(Throwable cause) {
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
