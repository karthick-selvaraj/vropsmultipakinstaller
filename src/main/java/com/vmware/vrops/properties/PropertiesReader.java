/* *************************************************************************
 * Copyright 2013 VMware, Inc.  All rights reserved. VMware Confidential
 * *************************************************************************/

package com.vmware.vrops.properties;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;


/**
 * This class provides singleton implementation to hold the several attributes
 * to support the vrops multiple pack installation
 * @author kselvaraj
 *
 */
public class PropertiesReader {

   private static Logger log = Logger.getLogger(PropertiesReader.class);
   private String configFile;
   private Properties prop;
   private InputStream input;

   /**
    * Constructor call
    * @throws IOException
    */
   public PropertiesReader(String inputfile) throws IOException {
      this.configFile = inputfile;
      this.prop = new Properties();
      try
      {
         this.input = new FileInputStream(this.configFile);
         this.prop.load(this.input);
      }
      catch(FileNotFoundException e)
      {
         log.error(" Configuration properties file not found ");
         throw new FileNotFoundException("Configuration properties file not found");
      }
   }

   public void setProps()
   {
      PropertiesStore propStore = PropertiesStore.getInstance();
      propStore.setUsername(prop.getProperty("username"));
      propStore.setIpaddress(prop.getProperty("ipaddress"));
      propStore.setPasswd(prop.getProperty("password"));
      propStore.setZipfile(prop.getProperty("zipfile"));
      propStore.setDelay(Integer.parseInt(prop.getProperty("delay")));
      propStore.setForce(prop.getProperty("force").equalsIgnoreCase("true"));
      propStore.setLoglevel(prop.getProperty("loglevel"));
   }
}
