package de.mediaportal.episodenumbergenerator.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
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
public class Substitutions extends Properties implements SettingsFields {

	/**
	 * default serial version UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Logger of the class {@link Substitutions}
	 */
	private static Logger logger = null;

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
	public Substitutions() throws FileNotFoundException, IOException {
		super();
		load(new FileInputStream(new File(FILE_SUBSTITUTIONS_PROPERTIES)));
		logger = LogManager.getLogger(Substitutions.class);
	}

	/**
	 * @return Table of series names in the epg with their substitution name to
	 *         be used for search on thetvdb.com
	 */
	public Hashtable<String, String> getSeriesNameSubstitutions() {
		Hashtable<String, String> seriesNameSubstitutions = null;
		String substitutions = this.getProperty(FIELD_SUBSTITUTIONS_SERIES);
		if (substitutions != null && !substitutions.equalsIgnoreCase("")) {
			String[] substitutionStrings = substitutions.split(";");
			int substitutionsLength = -1;
			if (substitutionStrings != null && (substitutionsLength = substitutionStrings.length) > 0) {
				if (substitutionsLength % 2 == 0) {
					seriesNameSubstitutions = new Hashtable<>();
					for (int i = 0; i < substitutionsLength; i += 2) {
						String epgSeriesName = substitutionStrings[i];
						String substitutionName = substitutionStrings[i + 1];
						seriesNameSubstitutions.put(epgSeriesName, substitutionName);
					}
				} else {
					logger.error("Your series name substitutions do not consist of pairs of name and substitution pattern. Please check.");
					System.exit(0);
				}
			}
		}
		return seriesNameSubstitutions;
	}

	/**
	 * @return Table of episode names in the epg with their substitution name to
	 *         be used for search on thetvdb.com
	 */
	public Hashtable<String, String> getEpisodeNameSubstitutions() {
		Hashtable<String, String> seriesNameSubstitutions = null;
		String substitutions = this.getProperty(FIELD_SUBSTITUTIONS_EPISODES);
		if (substitutions != null && !substitutions.equalsIgnoreCase("")) {
			String[] substitutionStrings = substitutions.split(";");
			int substitutionsLength = -1;
			if (substitutionStrings != null && (substitutionsLength = substitutionStrings.length) > 0) {
				if (substitutionsLength % 2 == 0) {
					seriesNameSubstitutions = new Hashtable<>();
					for (int i = 0; i < substitutionsLength; i += 2) {
						String epgEpisodeName = substitutionStrings[i];
						String substitutionName = substitutionStrings[i + 1];
						seriesNameSubstitutions.put(epgEpisodeName, substitutionName);
					}
				} else {
					logger.error("Your episode name substitutions do not consist of pairs of name and substitution pattern. Please check.");
					System.exit(0);
				}
			}
		}
		return seriesNameSubstitutions;
	}

	/**
	 * Main class for testing purposes
	 * 
	 * @param args
	 *            no arguments have to be passed
	 */
	public static void main(String[] args) {
		System.setProperty("log4j.configurationFile", "config/log4j2.xml");
		Substitutions s;
		try {
			s = new Substitutions();
			s.getSeriesNameSubstitutions();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
