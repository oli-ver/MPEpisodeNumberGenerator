package de.mediaportal.episodenumbergenerator;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.mediaportal.episodenumbergenerator.controller.DatabaseConnection;
import de.mediaportal.episodenumbergenerator.controller.TheTvDbController;
import de.mediaportal.episodenumbergenerator.model.Config;
import de.mediaportal.episodenumbergenerator.model.Substitutions;
import de.mediaportal.episodenumbergenerator.model.series.data.EpisodeInformation;
import de.mediaportal.episodenumbergenerator.model.series.data.SeriesData;

/**
 * MPEpisodeNumberGenerator scans MediaPortal's EPG for series beginning with a
 * pattern <code>epgdescriptionseriesindicator</code> defined in
 * settings.properties. For every Episode the season number and episode number
 * is scanned from thetvdb.com. If it is not possible to find the data at
 * thetvdb the tool scans the EPG description text for the pattern
 * <code>epgdescriptionpattern</code> also defined in settings.properties.
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
	 * Table of episode names in the epg with their substitution name to be used
	 * for search on thetvdb.com
	 */
	private static Hashtable<String, String> episodeNameSubstitutions = null;

	/**
	 * Table of series names in the epg with their substitution name to be used
	 * for search on thetvdb.com
	 */
	private static Hashtable<String, String> seriesNameSubstitutions = null;

	/**
	 * Application main flow
	 * 
	 * @param args
	 *            no arguments are used
	 */
	public static void main(String[] args) {
		// Initialize Logger
		System.setProperty("log4j.configurationFile", "config/log4j2.xml");
		logger = LogManager.getLogger(MPEpisodeNumberGenerator.class);
		logger.warn("****************************************");
		logger.warn("*** MPEpisodeNumberGenerator started ***");
		logger.warn("****************************************");
		try {
			// Create config instance
			config = new Config();

			// Create substitutions instance
			fetchSubstitutions();

			// Establish database connection to MediaPortal
			DatabaseConnection dbConnection = DatabaseConnection.getInstance();

			// Dump database
			try {
				int returnCode = dbConnection.dumpDatabase();
				if (returnCode == 0) {
					logger.info("MediaPortal database backup taken successfully");
				} else {
					logger.error("Could not take MediaPortal database backup (RC=" + returnCode + ")");
				}
			} catch (InterruptedException e) {
				logger.error("Could not dump database (" + e.getMessage() + ")", e);
			}

			// Scan EPG for series
			String epgSeriesIndicator = config.getEpgSeriesIndicator();

			// Get count of episodes to be scanned
			PreparedStatement countStmt = dbConnection.getSelectEpgTableCountStatement(epgSeriesIndicator);
			ResultSet countRs = countStmt.executeQuery();
			countRs.next();
			double epgEpisodesSize = countRs.getInt(1);
			countRs.close();
			int lineCounter = 0;

			// Begin Scan
			logger.info("Beginning scan of " + epgEpisodesSize + " epg lines in the database");
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

				// Create TheTvDbController object per series title and try to
				// find season and episode number
				String newTitle = rs.getString("title");

				String episodeName = rs.getString("episodeName");

				// Change query if substiution exists
				if (episodeNameSubstitutions != null) {
					String substitute = episodeNameSubstitutions.get(episodeName);
					if (substitute != null) {
						logger.debug("Substitution '" + substitute + "' found for episode name '" + episodeName + "'");
						episodeName = substitute;
					} else {
						// Episode name of epg is used
					}
				}

				if (newTitle.equalsIgnoreCase(lastTitle)) {
					epgCounter++;
				} else {
					if (!lastTitle.equals("")) {
						logger.info("Completed series with title '" + lastTitle + "' (mapped " + mappedCounter + " of " + epgCounter
								+ " episodes in epg).");
						epgCounter = 0;
						mappedCounter = 0;
					}
					logger.info("Processing new series " + newTitle);
					tvdb = new TheTvDbController(rs.getString("title"), rs.getString("originalAirDate").substring(0, 4));
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
									logger.info("Mapped episode number successfully: " + episodeInfo.getSeasonNumber() + "x"
											+ episodeInfo.getEpisodeNumber() + " - " + episodeInfo.getEpisodeName() + " - ProgramId='"
											+ rs.getString("idProgram") + "'");
									PreparedStatement updateStmt = dbConnection.updateEpgEpisodeAndSeriesNumber(rs.getInt("idProgram"),
											episodeInfo.getSeasonNumber(), episodeInfo.getEpisodeNumber());
									updateStmt.executeUpdate();
									foundEpisode = true;
									break;
								}
							}
						}
					}
				}

				// If no episode could be found try to get the episode
				// numbers from the epg description field
				if (!foundEpisode) {
					logger.debug("Trying to parse epg description text to find the series and episode number");
					String epgPattern = config.getEpgPattern();
					String epgText = rs.getString("description");

					Pattern numberPattern = Pattern.compile(REGEX_NUMBER);

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
							logger.info("Found series and episode number in description text: " + seasonNumber + "x" + episodeNumber);
							PreparedStatement updateStmt = dbConnection.updateEpgEpisodeAndSeriesNumber(rs.getInt("idProgram"),
									seasonNumber, episodeNumber);
							updateStmt.executeUpdate();
						} else {
							logger.debug("Found no season and episode number for series '" + rs.getString("title") + "' and episode '"
									+ rs.getString("episodeName") + "'");
						}

					} else {
						logger.debug("Found no season and episode number for series '" + rs.getString("title") + "' and episode '"
								+ rs.getString("episodeName") + "'");
					}
				}

				// Print statistics
				if (lineCounter % 100 == 0) {
					logger.info((lineCounter / epgEpisodesSize) * 100 + " % processed...");
					Date tmpDate = new Date();
					long tmpLong = tmpDate.getTime();
					double currentRunTime = tmpLong - beginningDateLong;
					double timeEstimation = (currentRunTime / lineCounter) * epgEpisodesSize - currentRunTime;
					double timeEstimationMinutes = timeEstimation / 1000 / 60;
					String timeStats = "Scan runs since " + sdf.format(beginningDate) + " and will be ready in " + timeEstimationMinutes
							+ " minutes";
					logger.warn(timeStats);
				} else if (lineCounter == epgEpisodesSize) {
					logger.info((lineCounter / epgEpisodesSize) * 100 + " % processed...");
					Date endDate = new Date();
					double diffTime = endDate.getTime() - beginningDateLong;
					double runTime = diffTime / 1000 / 60;
					logger.warn("Scan started " + sdf.format(beginningDate) + ", ended " + sdf.format(endDate) + " and took " + runTime
							+ " minutes");
				}
			}
			rs.close();
		} catch (Exception e) {
			logger.error("When scanning epg an Exception has been thrown (" + e.getMessage() + ")", e);
		}

	}

	/**
	 * @return Config object of the application
	 */
	public static Config getConfig() {
		return config;
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
			episodeNameSubstitutions = substitutions.getEpisodeNameSubstitutions();
			seriesNameSubstitutions = substitutions.getSeriesNameSubstitutions();
		} catch (IOException e) {
			logger.error("Could not parse substitutions file. Please check it (" + e.getMessage() + ").", e);
			System.exit(0);
		}
	}

	/**
	 * @return Table of episode names in the epg with their substitution name to
	 *         be used for search on thetvdb.com
	 */
	public static Hashtable<String, String> getEpisodeNameSubstitutions() {
		return episodeNameSubstitutions;
	}

	/**
	 * @return Table of series names in the epg with their substitution name to
	 *         be used for search on thetvdb.coms
	 */
	public static Hashtable<String, String> getSeriesNameSubstitutions() {
		return seriesNameSubstitutions;
	}
}
