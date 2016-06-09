/* *************************************************************************
 * Copyright 2013 VMware, Inc.  All rights reserved. VMware Confidential
 * *************************************************************************/

package com.vmware.vrops.pakfile;

/**
 * PakInstallState enum will be used to store the package level details during
 * installation
 *
 * Created by kselvaraj on 7/10/15
 *
 */
public enum PakInstallState {

   INITIAL("Initial status for an install"),
   CANDIDATE("This install is a candidate to reach the COMPLETED status"),
   FAILED("There was a failure in the install process"),
   COMPLETED("The PAK installation was a success");

   private String desc;

   /**
    * This function is constructor definition of PakInstallState enum
    *
    * @param value
    */
   private PakInstallState(String value) {
      this.desc = value;
   }

   /**
    * This function returns the description of the PakInstallState
    *
    * @return
    */
   public String getDesc() {
      return desc;
   }
}
