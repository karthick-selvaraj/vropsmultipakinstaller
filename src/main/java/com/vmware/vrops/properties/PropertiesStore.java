/* *************************************************************************
 * Copyright 2013 VMware, Inc.  All rights reserved. VMware Confidential
 * *************************************************************************/

package com.vmware.vrops.properties;

/**
 * This is a bean which holds several attributes to support vrops pack installation
 * @author kselvaraj
 *
 */
public class PropertiesStore {

   private String ipaddress;
   private String username;
   private String passwd;
   private String zipfile;
   private int delay;
   private String loglevel;
   private boolean force;

   private static PropertiesStore propStore = new PropertiesStore();

   /**
    * @return the force
    */
   public boolean isForce() {
      return force;
   }

   /**
    * @param force the force to set
    */
   public void setForce(boolean force) {
      this.force = force;
   }

   /**
    * @return the ipaddress
    */
   public String getIpaddress() {
      return ipaddress;
   }

   /**
    * @param ipaddress the ipaddress to set
    */
   public void setIpaddress(String ipaddress) {
      this.ipaddress = ipaddress;
   }

   /**
    * @return the username
    */
   public String getUsername() {
      return username;
   }

   /**
    * @param username the username to set
    */
   public void setUsername(String username) {
      this.username = username;
   }

   /**
    * @return the passwd
    */
   public String getPasswd() {
      return passwd;
   }

   /**
    * @param passwd the passwd to set
    */
   public void setPasswd(String passwd) {
      this.passwd = passwd;
   }

   /**
    * @return the zipfile
    */
   public String getZipfile() {
      return zipfile;
   }

   /**
    * @param zipfile the zipfile to set
    */
   public void setZipfile(String zipfile) {
      this.zipfile = zipfile;
   }

   /**
    * @return the delay
    */
   public int getDelay() {
      return delay;
   }

   /**
    * @param delay the delay to set
    */
   public void setDelay(int delay) {
      this.delay = delay;
   }

   /**
    * @return the loglevel
    */
   public String getLoglevel() {
      return loglevel;
   }

   /**
    * @param loglevel the loglevel to set
    */
   public void setLoglevel(String loglevel) {
      this.loglevel = loglevel;
   }

   public static synchronized PropertiesStore getInstance()
   {
     /* if(propStore == null)
      {
         propStore = new PropertiesStore();
      }*/
      return propStore;
   }

   private PropertiesStore()
   {
      this.ipaddress=null;
      this.delay=0;
      this.loglevel=null;
      this.passwd=null;
      this.zipfile=null;
      this.username=null;
      this.force=false;
   }

}
