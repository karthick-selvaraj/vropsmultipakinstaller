/* *************************************************************************
 * Copyright 2013 VMware, Inc.  All rights reserved. VMware Confidential
 * *************************************************************************/

package com.vmware.vrops.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;

import javax.swing.text.PlainDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.NumberFormatter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.GroupLayout;
import javax.swing.WindowConstants;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JFormattedTextField;

import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import javax.imageio.ImageIO;

import java.net.URL;

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
import com.vmware.vrops.pakfileinstaller.MultiPakInstaller;
import com.vmware.vrops.properties.PropertiesStore;

/**
 *This class provides GUI for installing multiple vrops management packs
 * @author kselvaraj
 */
public class PakInstallerGUI extends JFrame {
   // Variables declaration - do not modify
   private static Logger log = Logger.getLogger(PakInstallerGUI.class);
   private JButton installButton;
   private JButton attachButton;
   private JButton resetButton;
   private JCheckBox overwriteCheckBox;
   private JComboBox logComboBox;
   private JLabel ipLabel;
   private JLabel delayLabel;
   private JLabel userNameLabel;
   private JLabel passwordLabel;
   private JLabel installBundleLabel;
   private JLabel logLabel;
   private JPasswordField passwordTextField;
   private JScrollPane logScrollPane;
   private JTextPane logPane;
   private JTextField userNameTextField;
   private JTextField delayTextField;
   private JSpinner delaySpinner;
   private JTextField installBundleTextField;
   private ImagePanel imgPanel = new ImagePanel();
   private JLabel installLogLabel;
   private JSeparator separator;
   private JButton showHideButton;
   private GroupLayout layout;

   //set this flag if an operation is in progress, false otherwise
   private boolean operationInProgress = true;
   private File zipFile = null;
   private HTMLEditorKit kit;
   private HTMLDocument htmlDocument;
   private final String DEFAULT_DELAY = "300";
   private String SERVER_NAME = "Server Name";
   private String serverNames = null;
   private PersistUtil persistance = new PersistUtil(PakInstallerGUI.class);
   private JComboBox ipComboBox = new JComboBox();

   //Defining a number of String and int constants
   private static final int HORIZONTAL_SPACING = 25;
   private static final int VERTICAL_SPACING = 10;
   private static final String EMPTY_STRING = "";
   private static final String IP_ADDRESS_STRING = "IP addre<u>s</u>s:";
   private static final String USERNAME_STRING = "<u>U</u>ser name:";
   private static final String PASSWORD_STRING = "<u>P</u>assword:";
   private static final String DELAY_STRING = "<u>D</u>elay (sec):";
   private static final String INSTALLATION_BUNDLE_STRING =
         "Installation <u>B</u>undle:";
   private static final String LOG_LEVEL_STRING = "<u>L</u>og Level:";
   private static final String ATTACH_STRING = "Attach";
   private static final String RESET_ALL_STRING = "Reset All";
   private static final String FORCE_OVERWRITE_STRING =
         "<u>F</u>orce Overwrite";
   private static final String INSTALL_STRING = "Install";
   private static final String INSTALL_LOG_STRING = "Install Log";
   private static final String LOG_INFO_STRING = "info";
   private static final String LOG_DEBUG_STRING = "debug";
   private static final String SHOW_DETAILS_STRING = "<< Show Details >>";
   private static final String HIDE_DETAILS_STRING = "<< Hide Details >>";
   private static final String INVALID_USER_NAME_STRING = "Invalid User Name";
   private static final String INVALID_USER_NAME_TITLE =
         "Error: Please provide user name";
   private static final String INVALID_PASSWORD_STRING = "Invalid Password";
   private static final String INVALID_PASSWORD_TITLE =
         "Error: Please provide password";
   private static final String INVALID_IP_ADDRESS_STRING = "Invalid IP Address";
   private static final String INVALID_IP_ADDRESS_TITLE =
         "Error: Please provide a valid IP Address";
   private static final String WARNING_STRING = "Warning!";
   private static final String OVERWRITE_INSTALLATION_STRING =
         "This will overwrite the existing installation. Do you wish to continue?";
   private static final String INSTALLATION_PROGRESS_STRING =
         "Installation in progress! Do you wish to continue?";
   private static final String DELAY_TOOL_TIP =
         "Please provide delay in seconds ...";
   private static final String BUNDLE_TOOL_TIP =
         "Provide the path to the zip bundle to be installed from ...";
   private static final String OVERWRITE_TOOL_TIP =
         "Enable this option to overwrite existing installation";
   private static final String IP_TOOL_TIP =
         "Enter an ip address / Pick from an existing one / Shift + Del to remove an existing one";
   private static final String WINDOW_TITLE = "MultiPakInstallerGUI";
   private static final String IMAGE_RESOURCE = "VMwareClientLogo.png";
   private static final String FONT_BLUE_TAG = "<font color=\"blue\">";
   private static final String FONT_RED_TAG = "<font color=\"red\">";
   private static final String FONT_END_TAG = "</font>";
   private static final String COPY_TO_CLIPBOARD_STRING = "Copy to Clipboard";
   private static final String CLEAR_STRING = "Clear";
   private static final String HTML_START_TAG = "<html>";
   private static final String HTML_END_TAG = "</html>";
   private static final String IP_ADDRESS_DELIMITER = ";";
   private static PakInstallerGUI pakInstallerGUI = null;

   // End of variables declaration


   /**
    * Creates new form MainUI
    */
   private PakInstallerGUI() {

      try {
         serverNames =
               (String) persistance.getPersistedData(SERVER_NAME, EMPTY_STRING);
         for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
               UIManager.setLookAndFeel(info.getClassName());
               break;
            }
         }
      } catch (Exception e) {
         try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
         } catch (InstantiationException e1) {
            e1.printStackTrace();
         } catch (IllegalAccessException e1) {
            e1.printStackTrace();
         } catch (UnsupportedLookAndFeelException e1) {
            e1.printStackTrace();
         }
         // If Nimbus is not available, you can set the GUI to another look and feel.
      }
      initComponents();
      setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
   }

   /**
    * This is singleton implementation of pakinstallergui This function always
    * returns only one instance of the pakinstallergui object
    *
    * @return this returns the PakInstallerGUI object
    */
   public static synchronized PakInstallerGUI getInstance() {
      if (pakInstallerGUI == null) {
         pakInstallerGUI = new PakInstallerGUI();
      }
      return pakInstallerGUI;
   }

   /**
    * This method is called from within the constructor to initialize the form.
    */
   @SuppressWarnings("unchecked")
   private void initComponents() {

      ipLabel = new JLabel();
      userNameLabel = new JLabel();
      passwordLabel = new JLabel();
      delayLabel = new JLabel();
      overwriteCheckBox = new JCheckBox();
      installBundleLabel = new JLabel();
      installButton = new JButton();
      userNameTextField = new JTextField();
      delayTextField = new JTextField();
      delaySpinner = new JSpinner();
      passwordTextField = new JPasswordField();
      logComboBox = new JComboBox();
      logLabel = new JLabel();
      installLogLabel = new JLabel();
      installBundleTextField = new JTextField();
      installBundleTextField.setToolTipText(BUNDLE_TOOL_TIP);
      attachButton = new JButton();
      resetButton = new JButton();
      showHideButton = new JButton();
      logScrollPane = new JScrollPane();
      htmlDocument = new HTMLDocument();
      logPane = new JTextPane(htmlDocument);
      logPane.setContentType("text/html");
      kit = new HTMLEditorKit();
      logPane.setEditorKit(kit);
      logPane.setStyledDocument(htmlDocument);
      Dimension logPaneDim =
            new Dimension((int) imgPanel.getPreferredSize().getWidth(),
                  (int) (imgPanel.getPreferredSize().getHeight() * 1.5));
      logPane.setPreferredSize(logPaneDim);
      logScrollPane.setPreferredSize(logPaneDim);
      separator = new JSeparator(JSeparator.HORIZONTAL);


      setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      setTitle(WINDOW_TITLE);
      setResizable(false);

      ipComboBox.setEditable(true);
      ipComboBox.setToolTipText(IP_TOOL_TIP);
      java.awt.Component editComponent =
            ipComboBox.getEditor().getEditorComponent();
      //Code to delete an existing ip address using Shift + Del key combination 
      editComponent.addKeyListener(new KeyAdapter() {
         @Override
         public void keyReleased(KeyEvent ke) {
            if (ke.isShiftDown() && ke.getKeyCode() == KeyEvent.VK_DELETE) {
               int index = ipComboBox.getSelectedIndex();
               String selItem = (String) ipComboBox.getSelectedItem();
               if (selItem != null) {
                  selItem = selItem + IP_ADDRESS_DELIMITER;
               }
               if (index != -1) {
                  ipComboBox.removeItemAt(index);
                  int findIndex = serverNames.indexOf(selItem);
                  if (findIndex != -1) {
                     String tempServerNames =
                           serverNames.substring(0, findIndex);
                     tempServerNames =
                           tempServerNames
                                 + serverNames.substring(
                                       findIndex + selItem.length(),
                                       serverNames.length());
                     serverNames = tempServerNames;
                     persistance.persistData(SERVER_NAME, serverNames);
                  }
               }
            }
         }
      });
      if (serverNames != null) {
         StringTokenizer tokens =
               new StringTokenizer(serverNames, IP_ADDRESS_DELIMITER);
         int nTokens = tokens.countTokens();
         for (int i = 0; i < nTokens; i++) {
            ipComboBox.addItem(tokens.nextElement());
         }
      }
      ipComboBox.setSelectedIndex(-1);

      ipLabel.setText(HTML_START_TAG + FONT_BLUE_TAG + IP_ADDRESS_STRING
            + FONT_END_TAG + HTML_END_TAG);
      ipLabel.setToolTipText(IP_TOOL_TIP);
      ipLabel.setDisplayedMnemonic('s');
      ipLabel.setLabelFor(ipComboBox);

      userNameLabel.setText(HTML_START_TAG + FONT_BLUE_TAG + USERNAME_STRING
            + FONT_END_TAG + HTML_END_TAG);
      userNameLabel.setDisplayedMnemonic('U');
      userNameLabel.setLabelFor(userNameTextField);

      passwordLabel.setText(HTML_START_TAG + FONT_BLUE_TAG + PASSWORD_STRING
            + FONT_END_TAG + HTML_END_TAG);
      passwordLabel.setDisplayedMnemonic('P');
      passwordLabel.setLabelFor(passwordTextField);

      delayLabel.setText(HTML_START_TAG + FONT_BLUE_TAG + DELAY_STRING
            + FONT_END_TAG + HTML_END_TAG);
      delayLabel.setToolTipText(DELAY_TOOL_TIP);
      delayLabel.setDisplayedMnemonic('D');
      delayLabel.setLabelFor(delaySpinner);

      overwriteCheckBox.setText(HTML_START_TAG + FONT_BLUE_TAG
            + FORCE_OVERWRITE_STRING + FONT_END_TAG + HTML_END_TAG);
      overwriteCheckBox.setMnemonic('F');
      overwriteCheckBox.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            overwriteCheckBoxActionPerformed(evt);
         }
      });
      overwriteCheckBox.setToolTipText(OVERWRITE_TOOL_TIP);

      installBundleLabel.setText(HTML_START_TAG + FONT_BLUE_TAG
            + INSTALLATION_BUNDLE_STRING + FONT_END_TAG + HTML_END_TAG);
      installBundleLabel.setToolTipText(BUNDLE_TOOL_TIP);
      installBundleLabel.setDisplayedMnemonic('B');
      installBundleLabel.setLabelFor(installBundleTextField);

      installButton.setText(INSTALL_STRING);
      installButton.setMnemonic('I');
      installButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            installAction(evt);
         }
      });

      resetButton.setText(RESET_ALL_STRING);
      resetButton.setMnemonic('R');
      resetButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            resetAction(evt);
         }
      });
      showHideButton.setText(HIDE_DETAILS_STRING);
      showHideButton.setMnemonic('H');
      showHideButton.addActionListener(new java.awt.event.ActionListener() {
         JLabel dummyLabel1 = new JLabel();
         JLabel dummyLabel2 = new JLabel();
         JLabel dummyLabel3 = new JLabel();
         JSeparator dummySeparator = new JSeparator();
         {
            dummySeparator.setVisible(false);
         }

         public void actionPerformed(java.awt.event.ActionEvent evt) {
            String title = showHideButton.getText();
            if (SHOW_DETAILS_STRING.equals(title)) {
               showHideButton.setText(HIDE_DETAILS_STRING);
               showHideButton.setMnemonic('H');
               installLogLabel.setVisible(true);
               logScrollPane.setVisible(true);
               separator.setVisible(true);
               layout.replace(dummyLabel1, installLogLabel);
               layout.replace(dummyLabel2, logScrollPane);
               layout.replace(dummySeparator, separator);
            } else {
               showHideButton.setText(SHOW_DETAILS_STRING);
               showHideButton.setMnemonic('S');
               installLogLabel.setVisible(false);
               logScrollPane.setVisible(false);
               separator.setVisible(false);
               layout.replace(installLogLabel, dummyLabel1);
               layout.replace(logScrollPane, dummyLabel2);
               layout.replace(separator, dummySeparator);
            }
            pack();

            validate();
            repaint();
         }
      });
      installLogLabel.setText(HTML_START_TAG + FONT_BLUE_TAG
            + INSTALL_LOG_STRING + FONT_END_TAG + HTML_END_TAG);

      PlainDocument ipDoc = new PlainDocument() { //ipaddress basic validation
               @Override
               public void insertString(int offset, String string,
                     AttributeSet attr) throws BadLocationException {
                  try {
                     char c = string.charAt(string.length() - 1);
                     if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
                        return;
                     }
                  } catch (NumberFormatException ne) {
                     //ip address cannot contain alphabets
                     return;
                  }
                  super.insertString(offset, string, attr);

               }
            };

      //Remove this (PlainDocument) code, if we decide to go with JSpinner instead of JTextField (for the delay field)
      PlainDocument delayDoc = new PlainDocument() {
         @Override
         public void insertString(int offset, String string, AttributeSet attr)
               throws BadLocationException {
            try {
               Integer.parseInt(string);
            } catch (NumberFormatException ne) {
               //string is not a number
               return;
            }
            super.insertString(offset, string, attr);

         }
      };

      //Prepare JSpinner to take only numeric values
      delaySpinner.setModel(new SpinnerNumberModel(300, 0, 10000, 5));
      delaySpinner.setToolTipText(DELAY_TOOL_TIP);
      JFormattedTextField dTextField =
            ((JSpinner.NumberEditor) delaySpinner.getEditor()).getTextField();
      ((NumberFormatter) dTextField.getFormatter()).setAllowsInvalid(false);


      delayTextField.setDocument(delayDoc);
      delayTextField.setText(DEFAULT_DELAY);
      delayTextField.setToolTipText(DELAY_TOOL_TIP);


      logComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
            LOG_INFO_STRING, LOG_DEBUG_STRING }));

      logLabel.setText(HTML_START_TAG + FONT_BLUE_TAG + LOG_LEVEL_STRING
            + FONT_END_TAG + HTML_END_TAG);
      logLabel.setDisplayedMnemonic('L');
      logLabel.setLabelFor(logComboBox);

      attachButton.setText(ATTACH_STRING);
      attachButton.setToolTipText(BUNDLE_TOOL_TIP);
      attachButton.setMnemonic('A');
      attachButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            filechooserAction(evt);
         }
      });

      logPane.setEditable(false);
      logPane.setComponentPopupMenu(createPopupMenu());

      logScrollPane.setViewportView(logPane);

      createLayout();
      populateUI(null); //Fill in all UI field parameters from a properties file

      getAccessibleContext().setAccessibleName("pakinstallergui");

      pack();
      Dimension constFieldDim =
            new Dimension(ipComboBox.getWidth(), ipComboBox.getHeight());
      userNameTextField.setPreferredSize(constFieldDim);
      installBundleTextField.setPreferredSize(constFieldDim);
      ipComboBox.setPreferredSize(constFieldDim);
      logComboBox.setPreferredSize(constFieldDim);
      passwordTextField.setPreferredSize(constFieldDim);
      delaySpinner.setPreferredSize(constFieldDim);

      Dimension constButtonDim =
            new Dimension(attachButton.getWidth(), attachButton.getHeight());
      attachButton.setPreferredSize(constButtonDim);
      installButton.setPreferredSize(constButtonDim);
      resetButton.setPreferredSize(constButtonDim);
      showHideButton.setPreferredSize(constButtonDim);

      //Code to warn user of the installation operation in progress when the user tries to close the main window (either through close button or through ALT-F4)
      addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent e) {
            if (operationInProgress) {
               int choice =
                     JOptionPane.showConfirmDialog(PakInstallerGUI.this,
                           INSTALLATION_PROGRESS_STRING, WARNING_STRING,
                           JOptionPane.YES_NO_OPTION);
               if (choice == JOptionPane.YES_OPTION) {
                  return;
               }
            }
            dispose();
         }
      });

      //code for centering the Frame on the screen before it is launched 
      Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
      int guiX = (screenDim.width - this.getWidth()) / 2;
      int guiY = (screenDim.height - this.getHeight()) / 2;
      setLocation(guiX, guiY);
   }

   /**
    * This function create components using GroupLayout
    */
   public void createLayout() {
      layout = new GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(layout
            .createParallelGroup(CENTER)
            .addGap(HORIZONTAL_SPACING, HORIZONTAL_SPACING, HORIZONTAL_SPACING)
            .addComponent(imgPanel, PREFERRED_SIZE, PREFERRED_SIZE,
                  PREFERRED_SIZE)
            //draw the installer logo at the top
            .addGroup(
                  layout.createSequentialGroup()
                        .addGap(HORIZONTAL_SPACING, HORIZONTAL_SPACING,
                              HORIZONTAL_SPACING)
                        .addGroup(
                              layout.createParallelGroup(LEADING)
                                    .addComponent(ipLabel)
                                    .addComponent(userNameLabel)
                                    .addComponent(passwordLabel)
                                    .addComponent(delayLabel)
                                    .addComponent(installBundleLabel)
                                    .addComponent(logLabel)
                                    .addComponent(overwriteCheckBox)
                                    .addComponent(installButton))
                        .addGroup(
                              layout.createParallelGroup(LEADING)
                                    .addComponent(ipComboBox)
                                    .addComponent(userNameTextField)
                                    .addComponent(passwordTextField)
                                    //.addComponent(delayTextField)
                                    .addComponent(delaySpinner)
                                    .addComponent(installBundleTextField)
                                    .addComponent(logComboBox)
                                    .addComponent(resetButton))
                        //.addGap(HORIZONTAL_SPACING/2, HORIZONTAL_SPACING/2, HORIZONTAL_SPACING/2)
                        .addGroup(
                              layout.createParallelGroup(LEADING)
                                    .addComponent(attachButton)
                                    .addComponent(showHideButton)))
            .addGap(HORIZONTAL_SPACING, HORIZONTAL_SPACING, HORIZONTAL_SPACING)
            .addComponent(separator)
            .addGap(HORIZONTAL_SPACING, HORIZONTAL_SPACING, HORIZONTAL_SPACING)
            .addComponent(installLogLabel)
            .addGap(HORIZONTAL_SPACING, HORIZONTAL_SPACING, HORIZONTAL_SPACING)
            .addComponent(logScrollPane, PREFERRED_SIZE, PREFERRED_SIZE,
                  PREFERRED_SIZE)
            .addGap(HORIZONTAL_SPACING, HORIZONTAL_SPACING, HORIZONTAL_SPACING));

      layout.linkSize(installButton, resetButton, attachButton, showHideButton);
      layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, imgPanel,
            logScrollPane);

      layout.setVerticalGroup(layout
            .createSequentialGroup()
            .addGap(VERTICAL_SPACING / 3, VERTICAL_SPACING / 3,
                  VERTICAL_SPACING / 3)
            .addGroup(
                  layout.createParallelGroup(BASELINE).addComponent(imgPanel))
            .addGap(VERTICAL_SPACING, VERTICAL_SPACING, VERTICAL_SPACING)
            .addGroup(
                  layout.createParallelGroup(LEADING).addComponent(ipLabel)
                        .addComponent(ipComboBox))
            .addGap(VERTICAL_SPACING, VERTICAL_SPACING, VERTICAL_SPACING)
            .addGroup(
                  layout.createParallelGroup(LEADING)
                        .addComponent(userNameLabel)
                        .addComponent(userNameTextField))
            .addGap(VERTICAL_SPACING, VERTICAL_SPACING, VERTICAL_SPACING)
            .addGroup(
                  layout.createParallelGroup(LEADING)
                        .addComponent(passwordLabel)
                        .addComponent(passwordTextField))
            .addGap(VERTICAL_SPACING, VERTICAL_SPACING, VERTICAL_SPACING)
            .addGroup(
                  layout.createParallelGroup(LEADING).addComponent(delayLabel)
                  //.addComponent(delayTextField))
                        .addComponent(delaySpinner))
            .addGap(VERTICAL_SPACING, VERTICAL_SPACING, VERTICAL_SPACING)
            .addGroup(
                  layout.createParallelGroup(LEADING)
                        .addComponent(installBundleLabel)
                        .addComponent(installBundleTextField)
                        .addComponent(attachButton))
            .addGap(VERTICAL_SPACING, VERTICAL_SPACING, VERTICAL_SPACING)
            .addGroup(
                  layout.createParallelGroup(LEADING).addComponent(logLabel)
                        .addComponent(logComboBox))
            .addGap(VERTICAL_SPACING, VERTICAL_SPACING, VERTICAL_SPACING)
            .addComponent(overwriteCheckBox)
            .addGap(VERTICAL_SPACING, VERTICAL_SPACING, VERTICAL_SPACING)
            .addGroup(
                  layout.createParallelGroup(LEADING)
                        .addComponent(installButton).addComponent(resetButton)
                        .addComponent(showHideButton))
            .addGap(VERTICAL_SPACING, VERTICAL_SPACING, VERTICAL_SPACING)
            .addComponent(separator)
            .addGap(VERTICAL_SPACING, VERTICAL_SPACING, VERTICAL_SPACING)
            .addComponent(installLogLabel)
            .addGap(VERTICAL_SPACING, VERTICAL_SPACING, VERTICAL_SPACING)
            .addComponent(logScrollPane)
            .addGap(VERTICAL_SPACING, VERTICAL_SPACING, VERTICAL_SPACING));
   }

   /**
    * Code to populate UI from the parameters given in the properties file
    *
    * @param propFile
    */
   public void populateUI(File propFile) {

      try {
         if (propFile != null && propFile.exists()) {
            //write code here to read parameters from the properties File
            populateUI(null, null, null, DEFAULT_DELAY, null, null, false);
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   /**
    * This function populates the GUI with properties file contents
    *
    * @param ipAddresses
    * @param userName
    * @param password
    * @param delay
    * @param pathToBundle
    * @param logLevel
    * @param bForceOverwrite
    */
   public void populateUI(String ipAddresses, String userName, String password,
         String delay, String pathToBundle, String logLevel,
         boolean bForceOverwrite) {
      if (ipAddresses != null) {
         StringTokenizer tokens =
               new StringTokenizer(ipAddresses, IP_ADDRESS_DELIMITER);
         int nTokens = tokens.countTokens();
         ipComboBox.removeAllItems();
         for (int i = 0; i < nTokens; i++) {
            ipComboBox.addItem(tokens.nextElement());
         }
      }
      if (userName != null) {
         userNameTextField.setText(userName);
      }
      if (password != null) {
         passwordTextField.setText(password);
      }
      if (delay != null) {
         //delayTextField.setText(delay);
         delaySpinner.setValue(Integer.valueOf(delay));
      }
      if (pathToBundle != null) {
         installBundleTextField.setText(pathToBundle);
      }
      if (logLevel != null) {
         logComboBox.setSelectedItem(logLevel);
      }
      overwriteCheckBox.setSelected(bForceOverwrite);
   }

   //Code to confirm with the user on overwriting the existing installation
   private void overwriteCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
      if (overwriteCheckBox.isSelected()) {
         int choice =
               JOptionPane.showConfirmDialog(this,
                     OVERWRITE_INSTALLATION_STRING, WARNING_STRING,
                     JOptionPane.YES_NO_OPTION);
         if (choice == JOptionPane.NO_OPTION) {
            overwriteCheckBox.setSelected(false);
         }
      }
   }

   //code to reset all components
   private void resetAction(java.awt.event.ActionEvent evt) {
      resetAllComponents();
   }

   private void filechooserAction(java.awt.event.ActionEvent evt) {
      JFileChooser chooser = new JFileChooser();
      //Remember the last chosen path (directory) where the bundle was picked up from
      if (zipFile != null) {
         chooser.setSelectedFile(zipFile);
      }
      FileNameExtensionFilter filter =
            new FileNameExtensionFilter("Zip files", "zip");
      chooser.setFileFilter(filter);
      int choice = chooser.showOpenDialog(this);
      if (choice == JFileChooser.APPROVE_OPTION) {
         zipFile = chooser.getSelectedFile();
         if (zipFile != null) {
            String filepath = zipFile.getAbsolutePath();
            installBundleTextField.setText(filepath);
         }
      }
   }

   //Code for validating all the mandatory fields in the UI before proceeding with the installation
   private void installAction(java.awt.event.ActionEvent evt) {
      PropertiesStore propStore = PropertiesStore.getInstance();
      String ipAddress = (String) ipComboBox.getSelectedItem();
      if (ipAddress == null || !ValidateIPAddress(ipAddress = ipAddress.trim())) {
         JOptionPane.showMessageDialog(null, INVALID_IP_ADDRESS_STRING,
               INVALID_IP_ADDRESS_TITLE, 0);

         ipComboBox.requestFocus();
         return;
      }
      if (ipComboBox.getSelectedIndex() == -1) { //add the ipaddress only if it's a new one not in the list.
         int size = ipComboBox.getItemCount();
         int i=0;
         for ( i=0; i<size; i++) {
            String temp = (String)ipComboBox.getItemAt(i);
            if (ipAddress.trim().equals(temp)) {
                break;
            }
         }
         if (i == size) {
            serverNames = serverNames.concat(ipAddress + IP_ADDRESS_DELIMITER);
            ipComboBox.addItem(ipAddress);
         }
      }
      propStore.setIpaddress(ipAddress);
      if (userNameTextField.getText().trim().length() == 0) {
         JOptionPane.showMessageDialog(null, INVALID_USER_NAME_STRING,
               INVALID_USER_NAME_TITLE, 0);

         userNameTextField.requestFocus();
         return;
      }
      propStore.setUsername(userNameTextField.getText());
      if (passwordTextField.getPassword().toString().trim().length() == 0) {
         JOptionPane.showMessageDialog(null, INVALID_PASSWORD_STRING,
               INVALID_PASSWORD_TITLE, 0);

         passwordTextField.requestFocus();
         return;
      }
      propStore.setPasswd(new String(passwordTextField.getPassword()));
      //propStore.setDelay(Integer.parseInt(delayTextField.getText()));
      propStore.setDelay((int) delaySpinner.getValue());

      File zipFile = new File(installBundleTextField.getText());
      if (!zipFile.exists()) {
         JOptionPane.showMessageDialog(null, "zipFile: " + zipFile
               + " does not exist!", "Error: Please provide valid zip file", 0);
         installBundleTextField.requestFocus();
         return;
      }
      propStore.setZipfile(installBundleTextField.getText());
      propStore.setLoglevel(logComboBox.getSelectedItem().toString());
      propStore.setForce(overwriteCheckBox.isSelected());

      Runnable initiateInstallation = new Runnable() {
         @Override
         public void run() {
            StartInstallation();
         }
      };

      //Disabling all the editable components before beginning with the installation 
      enableAllComponents(false);
      Thread installThread = new Thread(initiateInstallation);
      installThread.start();
   }

   /**
    * This function update the log Pane with the information retrieved from the installation process
    * @param msg
    */
   public void updateUI(String msg) {
      StyledDocument doc = logPane.getStyledDocument();
      try {
         kit.insertHTML((HTMLDocument) doc, doc.getLength(), msg, 0, 0, null);
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   /**
    * This function intitate the installation of pack files.
    */
   private void StartInstallation() {
      setTitle(WINDOW_TITLE + " - Installing ...");
      persistance.persistData(SERVER_NAME, serverNames);
      MultiPakInstaller installer = new MultiPakInstaller();
      try {
         operationInProgress = true;
         installer.startInstallation();
      }

      catch (HelpException e) {
         //nothing to print here
         SwingUtilities.invokeLater(new UpdateJob(e.getMessage()));
      } catch (JSONException e) {
         SwingUtilities.invokeLater(new UpdateJob(e.getMessage()));
         SwingUtilities.invokeLater(new UpdateJob(
               "Unable to parse rest call response"));
      } catch (UploadFailedException e) {
         //SwingUtilities.invokeLater(new UpdateJob(e.getMessage() + "\n"));
         installer.Log(log, Level.ERROR, e.getMessage());
         SwingUtilities
               .invokeLater(new UpdateJob(
                     "Please check above mentioned issue(s) or check pakmanager log for more details"));
      } catch (DirectoryOperationsException | InvalidIPException
            | InvalidParameterException | RestCallFailedException
            | TrustManagerCertFailedException | InstallFailException e) {
         updateUI(e.getMessage() + "\n");
      } catch (Exception e) {
         log.debug(e.getMessage());
         SwingUtilities
               .invokeLater(new UpdateJob(
                     "Uploaded pak files has some problem, please check the pakManager log at vROPS system"));
      } finally {
         operationInProgress = false;
         setTitle(WINDOW_TITLE);
         enableAllComponents(true);
      }
   }


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

   //Code to enable or disable all UI components. Used when installation operation is in progress
   public void enableAllComponents(boolean bEnable) {
      ipComboBox.setEnabled(bEnable);
      userNameTextField.setEnabled(bEnable);
      passwordTextField.setEnabled(bEnable);
      //delayTextField.setEnabled(bEnable);
      delaySpinner.setEnabled(bEnable);
      installBundleTextField.setEnabled(bEnable);
      logComboBox.setEnabled(bEnable);
      overwriteCheckBox.setEnabled(bEnable);
      installButton.setEnabled(bEnable);
      attachButton.setEnabled(bEnable);
      resetButton.setEnabled(bEnable);
   }

   //Code to reset all the UI components
   public void resetAllComponents() {
      ipComboBox.setSelectedIndex(-1);
      userNameTextField.setText(EMPTY_STRING);
      passwordTextField.setText(EMPTY_STRING);
      //delayTextField.setText(DEFAULT_DELAY);
      delaySpinner.setValue(Integer.valueOf(DEFAULT_DELAY));
      installBundleTextField.setText(EMPTY_STRING);
      logComboBox.setSelectedItem(LOG_INFO_STRING);
      overwriteCheckBox.setSelected(false);
   }


   /**
    * This class provides a way to update GUI for progress
    * @author kselvaraj
    *
    */
   class UpdateJob implements Runnable {
      private final String progress;

      UpdateJob(String progress) {
         this.progress = progress;
      }

      public void run() {
         updateUI(progress);
      }
   }

   //Code for creating a popup menu for JTextPane containing clear and clipboard copy menu items
   public JPopupMenu createPopupMenu() {
      JPopupMenu menu = new JPopupMenu();
      JMenuItem clearMenu = new JMenuItem(CLEAR_STRING);
      clearMenu.setMnemonic('C');
      clearMenu.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            logPane.setText(EMPTY_STRING);
         }
      });
      menu.add(clearMenu);
      JMenuItem copyMenu = new JMenuItem(COPY_TO_CLIPBOARD_STRING);
      copyMenu.addActionListener(new ActionListener() {
         Clipboard clipbrd = null;

         @Override
         public void actionPerformed(ActionEvent e) {
            try {
               if (clipbrd == null) {
                  clipbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
               }
               clipbrd.setContents(new StringSelection(logPane.getText()), null);
            } catch (Exception ex) {
               ex.printStackTrace();
            }
         }
      });
      menu.add(copyMenu);
      return menu;
   }

   //Class code to persist user data and load it as required
   private class PersistUtil {
      private Preferences persistRoot = null;

      // making default constructor as private to prevent using it
      private PersistUtil() {
      }

      public PersistUtil(Class className) {
         persistRoot = Preferences.userNodeForPackage(className);
      }

      public void persistIntData(String key, int value) {
         try {
            persistRoot.putInt(key, value);
         } catch (Exception be) {
            be.printStackTrace();
         }
      }

      public int getIntPersistedData(String perfKey) {
         try {
            return persistRoot.getInt(perfKey, 0);
         } catch (Exception be) {
            be.printStackTrace();
         }
         return 0;
      }

      public int getIntPersistedData(String perfKey, int def) {
         try {
            return persistRoot.getInt(perfKey, def);
         } catch (Exception be) {
            be.printStackTrace();
         }
         return def;
      }

      public Object getPersistedData(String perfKey) {
         try {
            return persistRoot.get(perfKey, null);
         } catch (Exception be) {
            be.printStackTrace();
         }
         return null;
      }

      public Object getPersistedData(String perfKey, String def) {
         try {
            return persistRoot.get(perfKey, def);
         } catch (Exception be) {
            be.printStackTrace();
         }
         return def;
      }

      public void persistData(String key, Object value) {
         try {
            persistRoot.put(key, value.toString());
         } catch (Exception be) {
            be.printStackTrace();
         }
      }
   }

   //class to draw vROPS Installer (logo) bitmap
   private class ImagePanel extends JPanel {
      BufferedImage image = null;

      public ImagePanel() {
         try {
            ClassLoader loader = ImagePanel.class.getClassLoader();
            URL imageURL = loader.getResource(IMAGE_RESOURCE);
            if (imageURL == null) {
            }
            //load the image
            image = ImageIO.read(imageURL);
            this.setSize((int) image.getWidth(), (int) image.getHeight());

         } catch (Exception e) {
            e.printStackTrace();
         }
      }

      //Return the preferred size same as the dimension of the image
      @Override
      public Dimension getPreferredSize() {
         if (image == null) {
            return new Dimension(100, 100);
         }
         Dimension dim = new Dimension(image.getWidth(), image.getHeight());
         return dim;
      }

      //draw the bitmap image loaded
      @Override
      protected void paintComponent(Graphics g) {
         super.paintComponent(g);
         if (image != null) {
            ((Graphics2D) g).drawImage(image, 0, 0, this);
         }
      }
   }
}
