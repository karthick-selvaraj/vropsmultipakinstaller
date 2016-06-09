/* *************************************************************************
 * Copyright 2013 VMware, Inc.  All rights reserved. VMware Confidential
 * *************************************************************************/

package com.vmware.vrops.pakfile;

/**
 * PakState enum stores the pakfile install status on each slice in the cluster
 * lusterPakInstallStatus()
 *
 * Created by kselvaraj on 5/4/15
 */

public enum PakState {
   NOT_DISTRIBUTED("The PAK file does not exist on a slice"),
   DISTRIBUTED("The PAK file resides on the file system"),
   STAGED("The PAK file is staged"),
   PREAPPLY_VALIDATED("The PAK validation script passed"),
   APPLIED_SYSTEM_UPDATE("The system update of the PAK was successfully applied"),
   APPLIED_ADAPTER_PRE_SCRIPT("The adapter pre-installation script was successful"),
   APPLIED_ADAPTER("The adapter was successfully applied"),
   APPLIED_ADAPTER_POST_SCRIPT("The adapter post-installation script was successful"),
   APPLIED_AND_CLEANED("The system update and/or adapter was successfully installed,"
   + " and the working area was cleaned up");

   private String desc;

   /**
    * This function sets a description for enum type
    *
    * @param value
    */
   private PakState(String value) {
      this.desc = value;
   }

   /**
    * This function returns description of enum type
    *
    * @return desc
    */
   public String getDesc() {
      return desc;
   }
}
