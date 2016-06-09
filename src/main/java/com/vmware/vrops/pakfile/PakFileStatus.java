/* *************************************************************************
 * Copyright 2013 VMware, Inc.  All rights reserved. VMware Confidential
 * *************************************************************************/

package com.vmware.vrops.pakfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * PakFileStatus bean will be used to store the PakFileStatus during
 * installation
 *
 * Created by kselvaraj on 5/4/15
 *
 */
public class PakFileStatus {
   private String pakId;
   private PakInstallState clusterstate;
   private List<PakSliceStatus> sliceStatus;
   private Boolean clusterActionFailed = false;

   public PakFileStatus(String pakId) {
      this.pakId = pakId;
   }

   /**
    * This function returns pakID
    *
    * @return
    */
   public String getPakId() {
      return pakId;
   }

   /**
    * This function sets pakId
    *
    * @param pakId
    */
   public void setPakId(String pakId) {
      this.pakId = pakId;
   }

   /**
    * This function returns the pakfile install status from cluster
    *
    * @return
    */
   public PakInstallState getClusterPakInstallStatus() {
      return clusterstate;
   }

   /**
    * This function sets the pak file installation status in a cluster
    *
    * @param clusterPakInstallStatus
    */
   public void setClusterPakInstallStatus(String clusterPakInstallStatus) {
      this.clusterstate =
            PakInstallState.valueOf(clusterPakInstallStatus.toUpperCase());
   }

   /**
    * This function returns pak install status of all the slices from the
    * cluster
    *
    * @return
    */
   public List<PakSliceStatus> getSliceStatus() {
      return sliceStatus;
   }

   /**
    * add slice status in to master list. This collects pak install status from
    * every slice and add to the master list.
    *
    * @param newSliceStatus
    *           This contains the pak file status of a slice
    */
   public void addSliceStatus(PakSliceStatus newSliceStatus) {
      if (sliceStatus == null) {
         sliceStatus = new ArrayList<PakSliceStatus>();
      }
      sliceStatus.add(newSliceStatus);
   }

   /**
    * This function returns install action failed status
    *
    * @return returns true/false
    */
   public Boolean getClusterActionFailed() {
      return clusterActionFailed;
   }

   /**
    * This function sets the install action failed status
    *
    * @param clusterActionFailed
    *           This contains the actual install status
    */
   public void setClusterActionFailed(Boolean clusterActionFailed) {
      this.clusterActionFailed = clusterActionFailed;
   }

   /**
    * This function sets the slice status
    *
    * @param sliceStatus
    *           This contains the pak file status of slice
    */
   public void setSliceStatus(List<PakSliceStatus> sliceStatus) {
      this.sliceStatus = sliceStatus;
   }

}
