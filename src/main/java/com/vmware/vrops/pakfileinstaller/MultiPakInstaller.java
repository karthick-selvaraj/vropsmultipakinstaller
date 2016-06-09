/* *************************************************************************
 * Copyright 2013 VMware, Inc.  All rights reserved. VMware Confidential
 * *************************************************************************/

package com.vmware.vrops.pakfileinstaller;

import java.io.Console;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONException;

import com.vmware.vrops.exception.DirectoryOperationsException;
import com.vmware.vrops.exception.HelpException;
import com.vmware.vrops.exception.InstallFailException;
import com.vmware.vrops.exception.InvalidIPException;
import com.vmware.vrops.exception.InvalidParameterException;
import com.vmware.vrops.exception.RestCallFailedException;
import com.vmware.vrops.exception.TrustManagerCertFailedException;
import com.vmware.vrops.exception.UploadFailedException;
import com.vmware.vrops.gui.PakInstallerGUI;
import com.vmware.vrops.pakfile.PakFileStatus;
import com.vmware.vrops.pakfile.PakInstallState;
import com.vmware.vrops.pakfile.ResponseCode;
import com.vmware.vrops.pakfile.RestServiceStatus;
import com.vmware.vrops.properties.PropertiesReader;
import com.vmware.vrops.properties.PropertiesStore;
import com.vmware.vrops.restclient.OpsAPIHelper;


/**
 * This utility provides functionality to install group of vROPs pak files in to
 * particular node. While running this utility users dont have to provide
 * password explicitly. This utility will ask users to enter the password in
 * masked mode, hence password wont be visible to others.
 *
 * Created by kselvaraj on 5/4/15
 */
public class MultiPakInstaller {

   private static Logger log = Logger.getLogger(MultiPakInstaller.class);
   public static final int POLL_INTERVAL_TEN_SECONDS = 10000;
   public static final String cluster_scope = "cluster"; // this can be either slice or cluster
   public static PakInstallerGUI gui_thread=null;
   private static final String FONT_RED_TAG = "<font color=\"red\">";
   private static final String FONT_END_TAG = "</font>";

   private static String ipaddress = null;
   private static String username = null;
   private static String base64Credentials = null;
   private static String password = null;
   private static String zipFilePath = null;
   private static boolean force = false;
   private static String loglevel = null;
   private static int delay = 0;
   private static String propFile = null;


   public boolean ValidateIPAddress(String ipAddress) {
      String[] tokens = ipAddress.split("\\.");
      if (tokens.length != 4) {
         return false;
      }

      for (String str : tokens) {
         int i = Integer.parseInt(str);
         if ((i < 0) || (i > 255)) {
            return false;
         }
      }
      return true;
   }

   /**
    * Install pakfiles on particular node
    *
    * @param pakIdList
    *           This contains the list of pakIds to be installed after
    *           successful upload
    * @param ipaddress
    *           This contains the IPaddress of the vrops node
    * @param base64Credentials
    *           This stores base64 encoded string which is created by username
    *           and password
    *
    */
   public boolean installPakFiles(final List<RestServiceStatus> pakIdList,
         final String ipaddress, final String base64Credentials) {
      int response_status;
      if (pakIdList.isEmpty()) {
         Log(log, Level.ERROR, "pakId list is empty");
         return false;
      }

      for (RestServiceStatus pakId : pakIdList) {
         try {
            response_status =
                  OpsAPIHelper.installPakFile(ipaddress, pakId.getPakId(),
                        base64Credentials);
            if (response_status != ResponseCode.PACKAGE_UPLOAD_SUCCESS) {
               return false;
            }
         } catch (Exception e) {
            Log(log, Level.ERROR, "Installing pack file failed for " + pakId.getPakId());
            return false;
         }
      }
      return true;
   }

   /**
    * Upload pak files in to a particular node
    *
    * @param fileList
    *           This contains list of pak file paths
    * @param pakIdList
    *           This will contain list of pak ids after successful upload
    * @param ipaddress
    *           This contains the IPaddress of vrops node
    * @param base64Credentials
    *           This contains the base64 encoded credential
    */
   public void uploadPakFiles(final List<String> fileList,
         List<RestServiceStatus> pakIdList, final String ipaddress,
         final String base64Credentials, boolean force)
         throws UploadFailedException, TrustManagerCertFailedException,
         RestCallFailedException {

      RestServiceStatus status;
      for (String file : fileList) {
         try {
            status =
                  OpsAPIHelper.uploadPakFiles(ipaddress, file,
                        base64Credentials, force);
            if (status.getResponsecode() == ResponseCode.PACKAGE_UPLOAD_SUCCESS) {
               Log(log, Level.INFO, "Pak file " + status.getPakId()
                     + " uploaded successfully");
               pakIdList.add(status);
            } else if (status.getResponsecode() == ResponseCode.PACKAGE_UPLOAD_FAILURE) {
               throw new UploadFailedException("Pak file " + status.getPakId()
                     + " not compatible with target system OS");
            } else {
               Log(log, Level.INFO, "Pak file " + status.getPakId()
                     + " didn't uploaded successfully");
               if (status.getResponseKey().equalsIgnoreCase(
                     "upgrade.pak.file_name_has_bad_syntax")) {
                  throw new UploadFailedException("Uploaded pak file name:"
                        + status.getPakId() + " contains bad syntax");
               } else if (status.getResponseKey().equalsIgnoreCase(
                     "upgrade.pak.history_present")) {
                  throw new UploadFailedException(
                        "Uploaded pak file : "
                              + status.getPakId()
                              + " already found in the history, please use -force option/mark force checkbox to install");
               } else {
                  throw new UploadFailedException(
                        "Uploaded pak : "
                              + status.getPakId()
                              + " is already active or queued for installation already");
               }
            }
            //pakIdList.add(status);
         } catch (UploadFailedException | TrustManagerCertFailedException
               | RestCallFailedException e) {
            throw e;
         }
         //this catch block will catch JSONException and other exceptions and throw UploadFailedException
         catch (Exception e) {
            throw new UploadFailedException(
                  "Upload failed due to following reasons: \n"
                        + "Incorrect password \n" + "Network Issue \n");
         }

      }

   }

   /**
    * Check pak installation status
    *
    * @param pakIdList
    *           This contains the list of pakId's
    * @param ipaddress
    *           This contains the IPaddress of the vrops system
    * @param base64Credentials
    *           This contains the base64 encoded credential string
    * @throws Exception
    */
   public void checkPakInstallStatus(List<RestServiceStatus> pakIdList,
         String ipaddress, String base64Credentials, int delay)
         throws Exception {
      int flag = 0;
      List<PakFileStatus> pakStatusList = new ArrayList<PakFileStatus>();
      int state_checker;
      int count = 0;

      //we have to decide the number of cycles to run the loop to get the install status
      int cycle = delay / 10;

      while (flag == 0) {
         Log(log, Level.INFO, "\nWaiting for 10 secs to get install status");
         Thread.sleep(POLL_INTERVAL_TEN_SECONDS);
         if (!pakStatusList.isEmpty())
            pakStatusList.clear();

         //get the list of pakFileStatus for each pakId installed
         for (RestServiceStatus pakId : pakIdList) {
            PakFileStatus pakFileStatus =
                  OpsAPIHelper.getPakFileStatus(ipaddress, pakId.getPakId(),
                        cluster_scope, base64Credentials);
            pakStatusList.add(pakFileStatus);
         }
         //checks the install status of every pakId
         state_checker = 1;
         for (PakFileStatus pakStatus : pakStatusList) {
            if (pakStatus.getClusterPakInstallStatus() == PakInstallState.COMPLETED
                  || pakStatus.getClusterPakInstallStatus() == PakInstallState.FAILED)
               state_checker = state_checker & 1;
            else
               state_checker = state_checker & 0;

            Log(log, Level.INFO, "Pak file " + pakStatus.getPakId() + " install status is "
                  + pakStatus.getClusterPakInstallStatus().toString());
         }

         if (state_checker == 1) {
            //flag = 1;
            break;
         }
         count++;
         if (cycle == 0 || count == cycle) {
            Log(log, Level.INFO, "Installation may take longer, Please check pak install "
                  + "look into pakManager folder for further information");
            flag = 1;
         }
      }
   }

   /**
    * This function checks the uploaded pak file status by querying the response
    * from RestServiceStatus bean
    *
    * @param pakIdList
    *           This contains the list of responses received as part of
    *           uploading the pak files.
    * @return returns boolean value to indicate whether all files uploaded
    *         successfully or not.
    */
   public boolean CheckPakUploadStatus(List<RestServiceStatus> pakIdList) {
      if (pakIdList.isEmpty()) {
         return false;
      }
      for (RestServiceStatus status : pakIdList) {
         if (status.getResponsecode() != ResponseCode.PACKAGE_UPLOAD_SUCCESS) {
            log.debug("uploading of pakid : " + status.getPakId()
                  + "is not successful");
            return false;
         }
      }
      return true;
   }

   /**
    * This function reads password and generates base64 encrypted string
    *
    * @param username
    *           This contains username
    * @throws Exception
    */
   public void passwordReader(String username) throws Exception {
      Console console = System.console();
      if (console == null) {
         log.debug("Couldn't get Console instance");
         throw new Exception(
               "Unable to get console instance for getting password");
      }

      char passwordArray[] =
            console.readPassword("Enter " + username + "'s password: ");
      base64Credentials =
            OpsAPIHelper.getBase64EncodedString(username + ":"
                  + new String(passwordArray));
      if (base64Credentials == null) {
         throw new Exception("base64 encoding on username /password failed");
      }
   }

   public void DisplayGUI()
   {
      java.awt.EventQueue.invokeLater(new Runnable() {
         public void run() {
            gui_thread = PakInstallerGUI.getInstance();
            gui_thread.setVisible(true);
         }
     });
   }

   /**
    * This function reads the commandline arguments and parse
    *
    * @param args
    *           command line arguments
    * @throws Exception
    */
   public void readArguments(String args[]) throws Exception {
      HashMap<String, String> argMap = new HashMap<String, String>();
      PropertiesStore prop = PropertiesStore.getInstance();
      for (int i = 0; i < args.length; i++) {
         if(args[i].contains("="))
         {
            try {
               String[] keyvalue = args[i].split("=");
               argMap.put(keyvalue[0].replace('-', ' ').trim(), keyvalue[1]);
            } catch (ArrayIndexOutOfBoundsException e) {
               help();
               throw new HelpException("Invalid Arguments");
            }
         }
         else
         {
            try {
               String keyvalue = args[i];
               if(keyvalue.equalsIgnoreCase("-f"))
               {
                  argMap.put(keyvalue.replace('-', ' ').trim(), "true");
               }
               else
               {
                  argMap.put(keyvalue.replace('-', ' ').trim(), args[++i]);
               }
            } catch (ArrayIndexOutOfBoundsException e) {
               help();
               throw new HelpException("Invalid Arguments");
            }
         }
      }

      if (argMap.size() == 0) {
           DisplayGUI();
           return;
      }
      else if (argMap.get("configfile") != null || argMap.get("c") != null ) {
         if(argMap.get("configfile") != null)
         {
            propFile = argMap.get("configfile");
         }
         else
         {
            propFile = argMap.get("c");
         }
         PropertiesReader reader = new PropertiesReader(propFile);
         reader.setProps();
      }
      else {
         for (String key : argMap.keySet()) {

            switch (key.toLowerCase()) {
            case "username":
               prop.setUsername(argMap.get(key));
               break;
            case "u":
               prop.setUsername(argMap.get(key));
               break;
            case "password":
               prop.setPasswd(argMap.get(key));
               break;
            case "p":
               prop.setPasswd(argMap.get(key));
               break;
            case "ipaddress":
               prop.setIpaddress(argMap.get(key));
               break;
            case "i":
               prop.setIpaddress(argMap.get(key));
               break;
            case "zipfile":
               prop.setZipfile(argMap.get(key));
               break;
            case "z":
               prop.setZipfile(argMap.get(key));
               break;
            case "loglevel":
               prop.setLoglevel(argMap.get(key));
               break;
            case "l":
               prop.setLoglevel(argMap.get(key));
               break;
            case "force":
               prop.setForce(argMap.get(key).equalsIgnoreCase("true"));
               break;
            case "f":
               prop.setForce(argMap.get(key).equalsIgnoreCase("true"));
               break;
            case "delay":
               prop.setDelay(Integer.parseInt(argMap.get(key)));
               break;
            case "d":
               prop.setDelay(Integer.parseInt(argMap.get(key)));
               break;
            default:
               help();
               Log(log, Level.INFO, "Invalid parameter passed");
               throw new InvalidParameterException(
                     "Unknown parameters passed as command line arg");
            }
         }

         //set default delay in seconds
         if (prop.getDelay() == 0)
            prop.setDelay(300);
      }
   }

   /**
    * Validates password and prompt for password
    *
    * @throws Exception
    */
   public void checkPassword() throws Exception {
      PropertiesStore prop = PropertiesStore.getInstance();
      if (prop.getPasswd() == null || prop.getPasswd().isEmpty()) {
         passwordReader(prop.getUsername());
      } else {
         base64Credentials =
               OpsAPIHelper.getBase64EncodedString(prop.getUsername() + ":"
                     + prop.getPasswd());
      }
   }

   /**
    * validates the commandline args. Checks for username, ipaddress, valid zip
    * file path
    *
    * @return returns true/false
    * @throws Exception
    */
   public boolean validateInput(PropertiesStore prop) throws InvalidIPException {
      if (prop.getUsername() != null && prop.getZipfile() != null && prop.getIpaddress() != null) {
         if (ValidateIPAddress(prop.getIpaddress())) {
            return true;
         } else {
            throw new InvalidIPException("Invalid IPAddress");
         }
      }
      return false;
   }

   /**
    * helper function to show the usage
    */
   public static void help() {
      System.out.println("MultiPakInstaller Usage: ");
      System.out
            .println("PakFileInstaller [--ipaddress=<ip addr> | -i <ip addr>] "
                  + "[--username=<username> | -u <username>] "
                  + "[--zipfile=<path to Adapter zip file> | -z <path to Adapter zip file>]"
                  + "[-force=true|false  -f] [-loglevel=info|debug  -l <info|debug>] "
                  + "[-delay=<seconds>|default 300 seconds -d <seconds>]\n");
     System.out
	   .println("PakFileInstaller [--configfile=<path to config file>] [-c <path to configfile>]\n");
     System.out
	   .println("PakFileInstaller with out arguments lauches GUI\n");
   }

   /**
    * This function invokes the installer workflow
    * @throws Exception
    */
   public void startInstallation() throws Exception {
      PropertiesStore propStore = PropertiesStore.getInstance();
      List<String> fileList = new ArrayList<String>();
      List<RestServiceStatus> pakIdList = new ArrayList<RestServiceStatus>();
      File tempdirectory = null;
      try {
         if (propStore.getLoglevel() != null
               && !propStore.getLoglevel().equalsIgnoreCase("info")) {
            Logger.getRootLogger().setLevel(Level.DEBUG);
         }

         if(base64Credentials == null)
         {
            base64Credentials =
                  OpsAPIHelper.getBase64EncodedString(propStore.getUsername() + ":"
                        + propStore.getPasswd());
         }

         // create temporary directory to store the unzipped adapter pak files
         tempdirectory = UnZipAdapters.createTempDir();

         // unzip the adapter zip in to temporary folder
         UnZipAdapters.unZipIt(propStore.getZipfile(), tempdirectory.toPath()
               .toString(), fileList);
         Log(log, Level.INFO, "Unzip Successful");

         // create base64encoded credentials
         //base64Credentials = OpsAPIHelper.getBase64EncodedString(username + ":K@rthick01");
         Log(log, Level.INFO, "Uploading starts");
         // upload the extracted pak files in to vrops system
         uploadPakFiles(fileList, pakIdList, propStore.getIpaddress(),
               base64Credentials, propStore.isForce());

         // remove the temporary directory
         UnZipAdapters.RemoveTemp(tempdirectory);
         tempdirectory = null;
         if (CheckPakUploadStatus(pakIdList)) {
            // install the uploaded pak files
            if (installPakFiles(pakIdList, propStore.getIpaddress(),
                  base64Credentials)) {
               Log(log, Level.INFO, "Pakfile installation started please wait for "
                     + propStore.getDelay() + " seconds");
               // 120 seconds time to install the uploaded pak files
               int display_counter = propStore.getDelay();
               while (display_counter > 0) {
                  Thread.sleep(POLL_INTERVAL_TEN_SECONDS);
                  display_counter =
                        (display_counter - 10) > 0 ? (display_counter - 10) : 0;
                  Log(log, Level.INFO, "Wait for " + display_counter + " seconds more ");
               }
               // periodically poll the pak files installation status
               checkPakInstallStatus(pakIdList, propStore.getIpaddress(),
                     base64Credentials, propStore.getDelay());
            } else {
               throw new InstallFailException(
                     "Installation failed due to some problem, please check vrops pakmanager logs for further details");
            }
         } else {
            //log.info("Uploaded pak files has some problem, please check the log or use -f to force while installing again");
            throw new UploadFailedException(
                  "Upload failed due to following reasons: \n"
                        + "Incorrect password \n"
                        + "Network Issue \n"
                        + "Please check the pakManager log files in vROPs system");
         }
      } finally {
         if (tempdirectory != null)
            UnZipAdapters.RemoveTemp(tempdirectory);
      }
   }

   /**
    * This function logs messages in to log file as well as if there is GUI
    * thread active then the same message will be pushed to GUI as well.
    *
    * @param logger
    *           this is logger object reference
    * @param level
    *           this contains the log level like INFO, DEBUG, ERROR
    * @param log_message
    *           this contains the log content
    */
   public void Log(Logger logger, Level level, String log_message) {
      if (level == Level.INFO) {
         logger.info(log_message);
      }

      if (level == Level.DEBUG) {
         logger.debug(log_message);
      }

      if (level == Level.ERROR) {
         logger.error(log_message);
      }
      if (gui_thread != null) {
         if (level == Level.ERROR) {

            SwingUtilities.invokeLater(new UpdateJob(FONT_RED_TAG + log_message
                  + FONT_END_TAG, gui_thread));
         } else {
            SwingUtilities.invokeLater(new UpdateJob(log_message, gui_thread));
         }
      }
   }

   /**
    * This class implements runnable interface and submits pack installation
    * progress to java swing EDT thread.
    *
    * @author kselvaraj
    *
    */
   class UpdateJob implements Runnable {
      private final String progress;
      private PakInstallerGUI pakgui;

      UpdateJob(String progress, PakInstallerGUI pakgui) {
         this.progress = progress;
         this.pakgui = pakgui;
      }

      public void run() {
         this.pakgui.updateUI(progress);
      }
   }


   /**
    * Main function
    *
    * @param args
    *           Commandline arguments
    * @throws Exception
    */
   public static void main(String args[])
   {
      Logger.getRootLogger().setLevel(Level.INFO);
      MultiPakInstaller installer = new MultiPakInstaller();
      PropertiesStore propStore = PropertiesStore.getInstance();


      if(args.length == 0)
      {
         installer.DisplayGUI();
         return;
      }
      try {

         installer.readArguments(args);

         if (!installer.validateInput(propStore)) {
            help();
            throw new InvalidParameterException("Invalid Arguments : "
                  + "Please use help to find the options");
         }

         installer.checkPassword();
         installer.startInstallation();
      }

      catch (HelpException e) {
         //nothing to print here
      } catch (JSONException e) {
         installer.Log(log, Level.ERROR, e.getMessage());
         installer.Log(log, Level.ERROR, "Unable to parse rest call response");
      } catch (UploadFailedException e) {
         installer.Log(log, Level.ERROR, e.getMessage());
         installer.Log(log, Level.ERROR, "Please check above mentioned issue(s) or check pakmanager log");
      } catch (DirectoryOperationsException | InvalidIPException
            | InvalidParameterException | RestCallFailedException
            | TrustManagerCertFailedException | InstallFailException e) {
         log.info(e.getMessage());
      } catch (Exception e) {
         installer.Log(log, Level.DEBUG, e.getMessage());
         installer.Log(log, Level.ERROR, "Uploaded pak files has some problem, please check the pakManager log at vROPS system");
      }
   }
}



