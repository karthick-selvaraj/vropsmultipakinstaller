/* *************************************************************************
 * Copyright 2013 VMware, Inc.  All rights reserved. VMware Confidential
 * *************************************************************************/

package com.vmware.vrops.exception;

/**
 * This class used to denote directory operations related exception which is
 * used to show create/open/read /remove failures of a directory.
 *
 * Created by kselvaraj on 5/4/15.
 */
public class DirectoryOperationsException extends Exception {

   private String message = null;

   public DirectoryOperationsException() {
      super();
   }

   public DirectoryOperationsException(String message) {
      super(message);
      this.message = message;
   }

   public DirectoryOperationsException(Throwable cause) {
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
