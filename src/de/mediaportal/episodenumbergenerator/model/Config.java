package de.mediaportal.episodenumbergenerator.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

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
		load(new FileInputStream(new File(FILE_SETTINGS_PROPERTIES)));
		// Fetch top level domain and language options
		this.tld = getProperty(FIELD_TOP_LEVEL_DOMAIN);
		this.language = getProperty(FIELD_LANGUAGE);
		this.proxyUrl = getProperty(FIELD_PROXY_NAME);
		this.epgSeriesIndicator = getProperty(FIELD_EPG_DESCRIPTION_SERIESINDICATOR);
		this.epgPattern = getProperty(FIELD_EPG_DESCRIPTION_PATTERN);
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
}
