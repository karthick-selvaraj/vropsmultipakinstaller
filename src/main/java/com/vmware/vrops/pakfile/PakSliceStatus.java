/* *************************************************************************
 * Copyright 2013 VMware, Inc.  All rights reserved. VMware Confidential
 * *************************************************************************/

package com.vmware.vrops.pakfile;

/**
 * PakSliceStatus bean stores the pakfile install status on each slice in the
 * cluster lusterPakInstallStatus()
 *
 * Created by kselvaraj on 5/4/15
 *
 */
public class PakSliceStatus {
   private String sliceIp;
   private PakState pakState;
   private PakInstallState pakInstallState;

   public PakSliceStatus(String sliceIp, String pakState, String pakInstallState) {
      this.sliceIp = sliceIp;
      this.pakState = PakState.valueOf(pakState.toUpperCase());
      this.pakInstallState =
            PakInstallState.valueOf(pakInstallState.toUpperCase());
   }

   public String getSliceIp() {
      return sliceIp;
   }

   public void setSliceIp(String sliceIp) {
      this.sliceIp = sliceIp;
   }

   public String getPakState() {
      return pakState.toString();
   }

   public void setPakState(String pakState) {
      this.pakState = PakState.valueOf(pakState.toUpperCase());
   }

   public String getPakInstallState() {
      return pakInstallState.toString();
   }

   public void setPakInstallState(String pakInstallState) {
      this.pakInstallState = PakInstallState.valueOf(pakInstallState);
   }

   @Override
   public String toString() {
      return "PakFileSliceStatus [ sliceIp=" + sliceIp + ", pakState="
            + pakState + ", pakInstallState=" + pakInstallState + "]";
   }

}
