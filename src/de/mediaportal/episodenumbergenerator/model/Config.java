package de.mediaportal.episodenumbergenerator.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class representation of the local settings.properties file. Implements the
 * {@link SettingsFields} interface for convenience
 * 
 * @author Oliver
 * 
 * @see java.util.Properties
 *
 */
public class Config extends Properties implements SettingsFields {

	/**
	 * default serial version UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The top level domain from {@link Config}
	 */
	private String tld = null;

	/**
	 * The language from {@link Config}
	 */
	private String language = null;

	/**
	 * The URL of the thetvdb.com proxy
	 */
	private String proxyUrl = null;

	/**
	 * The EPG string to find only series episodes an no movies in the database
	 * table
	 */
	private String epgSeriesIndicator = null;

	/**
	 * description pattern in the EPG that can be used to resolve series and
	 * episode number if there could not be found anything for the series on
	 * thetvdb.com
	 */
	private String epgPattern = null;

	/**
	 * Flag to only scan EPG for series and episode numbers. If set the program
	 * will not try to resolve informations using thetvdb.com
	 */
	private boolean offline = false;

	/**
	 * maximal amoung of backups to be stored before deleting the oldest one
	 */
	private int backupCount = 10;

	/**
	 * List of titles of series that should never be resolved using the online
	 * thetvdb search
	 */
	private String[] seriesTitlesOfflineOnly = null;

	/**
	 * List of titles of series that should never be resolved using the offline
	 * epg text search
	 */
	private String[] seriesTitlesOnlineOnly = null;

	/**
	 * Logger of the {@link Config} class
	 */
	private Logger logger = null;

	/**
	 * Creates an instance of the Config class by reading from
	 * settings.properties file
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception of some sort has occurred. This
	 *             class is the general class of exceptions produced by failed
	 *             or interrupted I/O operations.
	 * @throws FileNotFoundException
	 *             Signals that an attempt to open the file denoted by a
	 *             specified pathname has failed.
	 */
	public Config() throws FileNotFoundException, IOException {
		super();
		logger = LogManager.getLogger(this.getClass());
		load(new FileInputStream(new File(FILE_SETTINGS_PROPERTIES)));
		this.tld = getProperty(FIELD_TOP_LEVEL_DOMAIN);
		this.language = getProperty(FIELD_LANGUAGE);
		this.proxyUrl = getProperty(FIELD_PROXY_NAME);
		this.epgSeriesIndicator = getProperty(FIELD_EPG_DESCRIPTION_SERIESINDICATOR);
		this.epgPattern = getProperty(FIELD_EPG_DESCRIPTION_PATTERN);
		this.offline = "true".equalsIgnoreCase(getProperty(FIELD_OFFLINE));
		String backupCountStr = getProperty(FIELD_BACKUP_COUNT);
		if (backupCountStr != null) {
			this.backupCount = Integer.parseInt(backupCountStr);
		}
		String offlineOnlyStr = getProperty(FIELD_SERIES_OFFLINE_ONLY);
		if (offlineOnlyStr != null && !"".equalsIgnoreCase(offlineOnlyStr)) {
			seriesTitlesOfflineOnly = offlineOnlyStr.split(";");
		}
		String onlineOnlyStr = getProperty(FIELD_SERIES_ONLINE_ONLY);
		if (onlineOnlyStr != null && !"".equalsIgnoreCase(onlineOnlyStr)) {
			seriesTitlesOnlineOnly = onlineOnlyStr.split(";");
		}
	}

	/**
	 * @return The top level domain from {@link Config}
	 */
	public String getTld() {
		return this.tld;
	}

	/**
	 * @return The language from {@link Config}
	 */
	public String getLanguage() {
		return this.language;
	}

	/**
	 * @return The URL of the thetvdb.com proxy
	 */
	public String getProxyUrl() {
		return this.proxyUrl;
	}

	/**
	 * @return The EPG string to find only series episodes an no movies in the
	 *         database table
	 */
	public String getEpgSeriesIndicator() {
		return this.epgSeriesIndicator;
	}

	/**
	 * @return description pattern in the EPG that can be used to resolve series
	 *         and episode number if there could not be found anything for the
	 *         series on thetvdb.com
	 */
	public String getEpgPattern() {
		return this.epgPattern;
	}

	/**
	 * @return true, if offline mode is enabled, false, if not
	 */
	public boolean isOffline() {
		return offline;
	}

	/**
	 * @return maximal amoung of backups to be stored before deleting the oldest
	 *         one
	 */
	public int getBackupCount() {
		return backupCount;
	}

	/**
	 * @param seriesTitle
	 *            title of the series
	 * @return true, if title is in the list of offline only series
	 */
	public boolean isOfflineOnlySeries(String seriesTitle) {
		seriesTitle = ansiToUTF8(seriesTitle);
		if (seriesTitle != null && this.seriesTitlesOfflineOnly != null && this.seriesTitlesOfflineOnly.length > 0) {
			for (String offlineSeries : seriesTitlesOfflineOnly) {
				if (seriesTitle.equalsIgnoreCase(offlineSeries)) {
					logger.warn("Found offline-only-series: " + seriesTitle + " equals " + offlineSeries);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Converts a given Ansi String to UTF-8
	 * 
	 * @param source
	 *            ansi string
	 * @return UTF-8 encoded String
	 * @throws UnsupportedEncodingException
	 */
	private static String ansiToUTF8(String source) {
		try {
			byte[] ansiBytes = source.getBytes();
			byte[] utf8 = new String(ansiBytes, "ISO-8859-1").getBytes("UTF-8");
			String converted = new String(utf8);
			return converted;
		} catch (Exception e) {
			return source;
		}
	}

	/**
	 * @param seriesTitle
	 *            title of the series
	 * @return true, if title is in the list of online only series
	 */
	public boolean isOnlineOnlySeries(String seriesTitle) {
		seriesTitle = ansiToUTF8(seriesTitle);
		if (seriesTitle != null && this.seriesTitlesOnlineOnly != null && this.seriesTitlesOnlineOnly.length > 0) {
			for (String onlineeries : seriesTitlesOnlineOnly) {
				if (seriesTitle.equalsIgnoreCase(onlineeries)) {
					logger.warn("Found online-only-series: " + seriesTitle + " equals " + onlineeries);
					return true;
				}
			}
		}
		return false;
	}
}
