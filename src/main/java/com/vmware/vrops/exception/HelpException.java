/* *************************************************************************
 * Copyright 2013 VMware, Inc.  All rights reserved. VMware Confidential
 * *************************************************************************/

package com.vmware.vrops.exception;

/**
 * This class used to denote the type of exception requires to show utility
 * help/usage documentation.
 *
 * Created by kselvaraj on 5/4/15.
 */
public class HelpException extends Exception {

   private String message = null;

   public HelpException() {
      super();
   }

   public HelpException(String message) {
      super(message);
      this.message = message;
   }

   public HelpException(Throwable cause) {
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
