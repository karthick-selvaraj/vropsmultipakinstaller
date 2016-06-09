/* *************************************************************************
 * Copyright 2013 VMware, Inc.  All rights reserved. VMware Confidential
 * *************************************************************************/

package com.vmware.vrops.pakfileinstaller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

import com.vmware.vrops.exception.DirectoryOperationsException;

/**
 * This class helps to unzip the pak files from a zip file in to a temporary
 * directory. This class provides methods to create/extract/delete directory.
 *
 * Created by kselvaraj on 5/4/15
 *
 */
public class UnZipAdapters {

   private static Logger log = Logger.getLogger(UnZipAdapters.class);

   /**
    * This function creates temporary directory to store the extracted adapter
    * zip file contents.
    *
    * @return This returns File handler for the temporary directory.
    */
   public static File createTempDir() throws DirectoryOperationsException {
      String curr_dir = System.getProperty("user.dir");
      File directory = new File(curr_dir, "tmp");
      try {
         if (!directory.exists()) {
            directory.mkdir();
            log.debug("temporary directory created :" + directory.toString());
         }
      } catch (Exception e) {
         log.info("Unable to create temporary directory");
         throw new DirectoryOperationsException(e.getCause());
      }
      return directory;
   }

   /**
    * Unzip a zip file in to temporary output folder
    *
    * @param zipFile
    *           This contains the zip file path.
    * @param outputFolder
    *           This contains the temporary output folder.
    * @param filelist
    *           This will be updated with list of extracted files.
    */
   public static void unZipIt(String zipFile, String outputFolder,
         List<String> filelist) throws IOException,
         DirectoryOperationsException {

      byte[] buffer = new byte[1024];
      ZipInputStream zis = null;
      FileOutputStream fos = null;
      try {

         // create output directory is not exists
         File folder = new File(outputFolder);
         if (!folder.exists()) {
            folder.mkdir();
         }

         // get the zip file content
         zis = new ZipInputStream(new FileInputStream(zipFile));
         // get the zipped file list entry
         ZipEntry ze = zis.getNextEntry();

         while (ze != null) {

            String fileName = ze.getName();
            File newFile = new File(outputFolder + File.separator + fileName);
            filelist.add(newFile.getAbsolutePath());

            // create all non exists folders
            // else you will hit FileNotFoundException for compressed folder
            new File(newFile.getParent()).mkdirs();

            fos = new FileOutputStream(newFile);

            int len;
            while ((len = zis.read(buffer)) > 0) {
               fos.write(buffer, 0, len);
            }

            fos.close();
            ze = zis.getNextEntry();
         }

         zis.closeEntry();
         zis.close();
         zis = null;
         fos = null;
      }

      catch (IOException ex) {
         throw new DirectoryOperationsException("Unzipping pak files failed");
      } finally {
         if (zis != null) {
            zis.close();
         }
         if (fos != null) {
            fos.close();
         }
      }
   }

   /**
    * Remove the temporary directory
    *
    * @param directory
    *           This contains the directory to be removed
    */
   public static void RemoveTemp(File directory) {
      try {
         if (directory.exists()) {
            String[] entries = directory.list();
            for (String s : entries) {
               File currentFile = new File(directory.getPath(), s);
               currentFile.delete();
            }
            directory.delete();
         }
      } catch (Exception e) {
         log.info("Removing temporary directory failed");
      }
   }
}
