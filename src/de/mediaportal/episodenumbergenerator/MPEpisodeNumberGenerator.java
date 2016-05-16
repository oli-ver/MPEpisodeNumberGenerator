package de.mediaportal.episodenumbergenerator;

import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.mediaportal.episodenumbergenerator.controller.DatabaseConnection;
import de.mediaportal.episodenumbergenerator.controller.TheTvDbController;
import de.mediaportal.episodenumbergenerator.model.Config;
import de.mediaportal.episodenumbergenerator.model.Substitutions;
import de.mediaportal.episodenumbergenerator.model.series.data.EpisodeInformation;
import de.mediaportal.episodenumbergenerator.model.series.data.SeriesData;

/**
 * MPEpisodeNumberGenerator scans MediaPortal's EPG for series beginning with a pattern
 * <code>epgdescriptionseriesindicator</code> defined in settings.properties. For every Episode the season number and
 * episode number is scanned from thetvdb.com. If it is not possible to find the data at thetvdb the tool scans the EPG
 * description text for the pattern <code>epgdescriptionpattern</code> also defined in settings.properties.
 * 
 * @author Oliver
 *
 */
public class MPEpisodeNumberGenerator {

   /**
    * Logger of the class {@link MPEpisodeNumberGenerator}
    */
   private static Logger logger = null;

   /**
    * Regular Expression for a number
    */
   private final static String REGEX_NUMBER = "\\d+";

   /**
    * Holds configuration set in settings.properties
    */
   private static Config config = null;

   /**
    * Table of episode names in the epg with their substitution name to be used for search on thetvdb.com
    */
   private static Hashtable<String, String> episodeNameSubstitutions = null;

   /**
    * Table of series names in the epg with their substitution name to be used for search on thetvdb.com
    */
   private static Hashtable<String, String> seriesNameSubstitutions = null;

   /**
    * SystemTray object to show notifications
    */
   private static SystemTray systemTray = null;

   /**
    * SystemTray tray icon
    */
   private static TrayIcon trayIcon = null;

   /**
    * Application main flow
    * 
    * @param args
    *           no arguments are used
    */
   public static void main(String[] args) {
      // Initialize config

      File configDir = new File("config/");
      File configFile = new File("config/settings.properties");

      // Check if user wants to (re)create config and rename current config if
      // exists
      if (args != null && args.length > 0) {
         for (String arg : args) {
            if (arg != null && arg.equalsIgnoreCase("--createconfig")) {
               if (configDir.exists() && configFile.exists()) {
                  File bakFile = new File("config/settings.bak");
                  if (bakFile.exists()) {
                     bakFile.delete();
                  }
                  configFile.renameTo(bakFile);
               }
            }
         }
      }

      // Add system tray symbol
      try {
         MPEpisodeNumberGenerator.systemTray = SystemTray.getSystemTray();
         MPEpisodeNumberGenerator.trayIcon = new TrayIcon(Toolkit.getDefaultToolkit()
                                                                 .getImage(MPEpisodeNumberGenerator.class.getResource(
                                                                       "/de/mediaportal/episodenumbergenerator/view/tray.png")),
               "MPEpisodeNumberGenerator");
         MPEpisodeNumberGenerator.systemTray.add(MPEpisodeNumberGenerator.trayIcon);
         MPEpisodeNumberGenerator.trayIcon.displayMessage("Hinweis", "MPEpisodeNumberGenerator wurde gestartet.",
               MessageType.INFO);
      } catch (Exception e) {
         System.err.println("Could not add the system tray icon (" + e.getMessage()
               + "). This message can be ignored. The tray icon is not needed to run this application successfully.");
         e.printStackTrace();
      }
      try {
         // Check config directory and file and create directory and config
         // file if necessary
         if (!configDir.exists() || !configFile.exists()) {
            configDir.mkdirs();
            JOptionPane.showMessageDialog(null, "A configuration will be created now.");
            JTextArea textArea = new JTextArea(20, 20);
            // Read template settings
            BufferedReader br =
                  new BufferedReader(new InputStreamReader(MPEpisodeNumberGenerator.class.getResourceAsStream(
                        "/de/mediaportal/episodenumbergenerator/model/settings.properties_template")));
            String line = null;
            while ((line = br.readLine()) != null) {
               textArea.append(line + "\n");
            }
            br.close();

            int returnType = JOptionPane.showConfirmDialog(null, textArea, "", JOptionPane.OK_CANCEL_OPTION);
            if (returnType == JOptionPane.CANCEL_OPTION) {
               System.exit(0);
            } else {
               configFile.createNewFile();
               BufferedWriter bw = new BufferedWriter(new FileWriter(configFile));
               bw.write(textArea.getText());
               bw.close();
            }
         }

         // Check log4j2.xml
         MPEpisodeNumberGenerator.checkAndCreate("config/log4j2.xml",
               "/de/mediaportal/episodenumbergenerator/model/log4j2.xml_template");

         // Check substitutions file
         MPEpisodeNumberGenerator.checkAndCreate("config/substitutions.properties",
               "/de/mediaportal/episodenumbergenerator/model/substitutions.properties_template");

         // Check Cache path
         File cachePath = new File(TheTvDbController.PATH_CACHE);
         if (!cachePath.exists()) {
            cachePath.mkdirs();
         }
      } catch (IOException e) {
         System.err.println("When initializing application's config an IOException has been thrown. ");
         e.printStackTrace();
      }
      // Initialize Logger
      System.setProperty("log4j.configurationFile", "config/log4j2.xml");
      MPEpisodeNumberGenerator.logger = LogManager.getLogger(MPEpisodeNumberGenerator.class);
      MPEpisodeNumberGenerator.logger.warn("****************************************");
      MPEpisodeNumberGenerator.logger.warn("*** MPEpisodeNumberGenerator started ***");
      MPEpisodeNumberGenerator.logger.warn("****************************************");
      try {
         // Create config instance
         MPEpisodeNumberGenerator.config = new Config();

         // Create substitutions instance
         MPEpisodeNumberGenerator.fetchSubstitutions();

         // Establish database connection to MediaPortal
         DatabaseConnection dbConnection = DatabaseConnection.getInstance();

         // Dump database
         try {
            int returnCode = dbConnection.dumpDatabase();
            if (returnCode == 0) {
               MPEpisodeNumberGenerator.logger.info("MediaPortal database backup taken successfully");
            } else {
               MPEpisodeNumberGenerator.logger.error(
                     "Could not take MediaPortal database backup (RC=" + returnCode + ")");
            }
         } catch (InterruptedException e) {
            MPEpisodeNumberGenerator.logger.error("Could not dump database (" + e.getMessage() + ")", e);
         }

         // Scan EPG for series
         String epgSeriesIndicator = MPEpisodeNumberGenerator.config.getEpgSeriesIndicator();

         // Get count of episodes to be scanned
         PreparedStatement countStmt = dbConnection.getSelectEpgTableCountStatement(epgSeriesIndicator);
         ResultSet countRs = countStmt.executeQuery();
         countRs.next();
         double epgEpisodesSize = countRs.getInt(1);
         countRs.close();
         int lineCounter = 0;

         // Begin Scan
         MPEpisodeNumberGenerator.logger.info("Beginning scan of " + epgEpisodesSize + " epg lines in the database");
         Date beginningDate = new Date();
         long beginningDateLong = beginningDate.getTime();
         SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
         PreparedStatement stmt = dbConnection.getSelectEpgTableStatement(epgSeriesIndicator);
         ResultSet rs = stmt.executeQuery();
         String lastTitle = "";
         TheTvDbController tvdb = null;
         int epgCounter = 0;
         int mappedCounter = 0;

         // Iterate over episode database lines
         while (rs.next()) {
            boolean foundEpisode = false;
            lineCounter++;

            // Get the series and episode title
            String title = rs.getString("title");
            String episodeName = rs.getString("episodeName");

            MPEpisodeNumberGenerator.logger.info("Beginning search for " + title + " - " + episodeName);

            if (!MPEpisodeNumberGenerator.config.isOnlineOnlySeries(title)) {
               // try to get the episode
               // numbers from the epg description field
               MPEpisodeNumberGenerator.logger.debug(
                     "Offline-Search: Trying to parse epg description text to find the series and episode number");
               String epgPattern = MPEpisodeNumberGenerator.config.getEpgPattern();
               String epgText = rs.getString("description");

               Pattern numberPattern = Pattern.compile(MPEpisodeNumberGenerator.REGEX_NUMBER);

               Pattern pattern = Pattern.compile(epgPattern);
               Matcher matcher = pattern.matcher(epgText);
               if (matcher.find()) {

                  int beginIndex = matcher.start();
                  epgText = epgText.substring(beginIndex);
                  String[] episodeSeriesArray = epgText.split("\\.");
                  String episodeNumber = null;
                  String seasonNumber = null;
                  String episodeNumberTmp = episodeSeriesArray[0];
                  String seriesNumberTmp = episodeSeriesArray[1];

                  Matcher episodeMatcher = numberPattern.matcher(episodeNumberTmp);
                  if (episodeMatcher.find()) {
                     int beginEpisodeNumber = episodeMatcher.start();
                     episodeNumber = episodeNumberTmp.substring(beginEpisodeNumber);
                  }

                  Matcher seriesMatcher = numberPattern.matcher(seriesNumberTmp);
                  if (seriesMatcher.find()) {
                     int beginSeriesNumber = seriesMatcher.start();
                     seasonNumber = seriesNumberTmp.substring(beginSeriesNumber);
                  }
                  if (episodeNumber != null && seasonNumber != null) {
                     MPEpisodeNumberGenerator.logger.info(
                           "Offline-Search: Found series and episode number in description text: " + seasonNumber + "x"
                                 + episodeNumber);
                     PreparedStatement updateStmt = dbConnection.updateEpgEpisodeAndSeriesNumber(rs.getInt("idProgram"),
                           seasonNumber, episodeNumber);
                     updateStmt.executeUpdate();
                     foundEpisode = true;
                  } else {
                     // not found message will be logged later
                  }

               } else {
                  MPEpisodeNumberGenerator.logger.debug(
                        "Offline-Search: Found no season and episode number for series '" + title + "' and episode '"
                              + episodeName + "'");
               }
            } else {
               MPEpisodeNumberGenerator.logger.warn(
                     "Skipped Offline-Search for '" + title + "' because it is marked as online only in the config");
            }
            if (!foundEpisode && !MPEpisodeNumberGenerator.config.isOffline()) {
               if (!MPEpisodeNumberGenerator.config.isOfflineOnlySeries(title)) {
                  // Create TheTvDbController object per series title and
                  // try to find season and episode number
                  String newTitle = rs.getString("title");

                  // Change query if substitutions exists
                  if (MPEpisodeNumberGenerator.episodeNameSubstitutions != null) {
                     String substitute = MPEpisodeNumberGenerator.episodeNameSubstitutions.get(episodeName);
                     if (substitute != null) {
                        MPEpisodeNumberGenerator.logger.debug("Online-Search: Substitution '" + substitute
                              + "' found for episode name '" + episodeName + "'");
                        episodeName = substitute;
                     } else {
                        // Episode name of epg is used
                     }
                  }

                  if (newTitle.equalsIgnoreCase(lastTitle)) {
                     epgCounter++;
                  } else {
                     if (!"".equals(lastTitle)) {
                        MPEpisodeNumberGenerator.logger.info("Online-Search: Completed series with title '" + lastTitle
                              + "' (mapped " + mappedCounter + " of " + epgCounter + " episodes in epg).");
                        epgCounter = 0;
                        mappedCounter = 0;
                     }
                     MPEpisodeNumberGenerator.logger.info("Online-Search: Processing new series " + newTitle);
                     tvdb = new TheTvDbController(newTitle, rs.getString("originalAirDate").substring(0, 4));
                  }
                  lastTitle = newTitle;

                  SeriesData seriesData = tvdb.getSeriesData();
                  if (seriesData != null) {
                     Vector<EpisodeInformation> episodeList = seriesData.getEpisodeList();
                     if (episodeList != null && episodeList.size() > 0) {
                        for (EpisodeInformation episodeInfo : episodeList) {
                           if (episodeInfo != null) {
                              if (episodeInfo.getEpisodeName().equalsIgnoreCase(episodeName)) {
                                 mappedCounter++;
                                 MPEpisodeNumberGenerator.logger.info(
                                       "Online-Search: Mapped episode number successfully: "
                                             + episodeInfo.getSeasonNumber() + "x" + episodeInfo.getEpisodeNumber()
                                             + " - " + episodeInfo.getEpisodeName() + " - ProgramId='"
                                             + rs.getString("idProgram") + "'");
                                 PreparedStatement updateStmt =
                                       dbConnection.updateEpgEpisodeAndSeriesNumber(rs.getInt("idProgram"),
                                             episodeInfo.getSeasonNumber(), episodeInfo.getEpisodeNumber());
                                 updateStmt.executeUpdate();
                                 foundEpisode = true;
                                 break;
                              }
                           }
                        }
                     }
                  } else {
                     MPEpisodeNumberGenerator.logger.warn("Online-Search: Could not fetch series data for: " + tvdb);
                  }
               } else {
                  MPEpisodeNumberGenerator.logger.warn(
                        "Skipped Online-Search for '" + title + "' because it is marked as offline only in the config");
               }
            }

            // Print statistics
            if (lineCounter % 100 == 0) {
               MPEpisodeNumberGenerator.logger.info((lineCounter / epgEpisodesSize) * 100 + " % processed...");
               Date tmpDate = new Date();
               long tmpLong = tmpDate.getTime();
               double currentRunTime = tmpLong - beginningDateLong;
               double timeEstimation = (currentRunTime / lineCounter) * epgEpisodesSize - currentRunTime;
               double timeEstimationMinutes = timeEstimation / 1000 / 60;
               String timeStats = "Scan runs since " + sdf.format(beginningDate) + " and will be ready in "
                     + timeEstimationMinutes + " minutes";
               MPEpisodeNumberGenerator.logger.warn(timeStats);
            } else if (lineCounter == epgEpisodesSize) {
               MPEpisodeNumberGenerator.logger.info((lineCounter / epgEpisodesSize) * 100 + " % processed...");
               Date endDate = new Date();
               double diffTime = endDate.getTime() - beginningDateLong;
               double runTime = diffTime / 1000 / 60;
               MPEpisodeNumberGenerator.logger.warn("Scan started " + sdf.format(beginningDate) + ", ended "
                     + sdf.format(endDate) + " and took " + runTime + " minutes");
            }
         }
         rs.close();
         MPEpisodeNumberGenerator.trayIcon.displayMessage("Info",
               "MPEpisodeNumberGenerator ended successfully. All of your episodes should now have "
                     + "series and episode numbers in the EPG.",
               MessageType.INFO);
      } catch (Exception e) {
         MPEpisodeNumberGenerator.logger.error(
               "When scanning epg an Exception has been thrown (" + e.getMessage() + ")", e);
         MPEpisodeNumberGenerator.trayIcon.displayMessage("Error",
               "MPEpisodeNumberGenerator terminated abnormally. Please check the log file. ", MessageType.ERROR);
      }
      Timer timer = new Timer();
      timer.schedule(new TimerTask() {

         @Override
         public void run() {
            MPEpisodeNumberGenerator.systemTray.remove(MPEpisodeNumberGenerator.trayIcon);
            System.exit(0);
         }
      }, 10000);
   }

   /**
    * @param fileToCheck
    *           File to be checked and created from template if not exists
    * @param templateResource
    *           template resource to be used
    * @throws IOException
    */
   private static void checkAndCreate(String fileToCheck, String templateResource) throws IOException {
      File fileToCheckAndCreate = new File(fileToCheck);
      if (!fileToCheckAndCreate.exists()) {
         fileToCheckAndCreate.createNewFile();
         BufferedWriter bw = new BufferedWriter(new FileWriter(fileToCheckAndCreate));
         BufferedReader br = new BufferedReader(
               new InputStreamReader(MPEpisodeNumberGenerator.class.getResourceAsStream(templateResource)));
         String line = null;
         while ((line = br.readLine()) != null) {
            bw.write(line + "\n");
         }
         br.close();
         bw.close();
      }

   }

   /**
    * @return Config object of the application
    */
   public static Config getConfig() {
      return MPEpisodeNumberGenerator.config;
   }

   /**
    * Fetches substitutions from {@link Substitutions}
    * 
    */
   private static void fetchSubstitutions() {
      // Read properties file, if class is initialized for the first time
      Substitutions substitutions = null;
      try {
         substitutions = new Substitutions();
         MPEpisodeNumberGenerator.episodeNameSubstitutions = substitutions.getEpisodeNameSubstitutions();
         MPEpisodeNumberGenerator.seriesNameSubstitutions = substitutions.getSeriesNameSubstitutions();
      } catch (IOException e) {
         MPEpisodeNumberGenerator.logger.error(
               "Could not parse substitutions file. Please check it (" + e.getMessage() + ").", e);
         System.exit(0);
      }
   }

   /**
    * @return Table of episode names in the epg with their substitution name to be used for search on thetvdb.com
    */
   public static Hashtable<String, String> getEpisodeNameSubstitutions() {
      return MPEpisodeNumberGenerator.episodeNameSubstitutions;
   }

   /**
    * @return Table of series names in the epg with their substitution name to be used for search on thetvdb.coms
    */
   public static Hashtable<String, String> getSeriesNameSubstitutions() {
      return MPEpisodeNumberGenerator.seriesNameSubstitutions;
   }
}
