package de.mediaportal.episodenumbergenerator.controller;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thoughtworks.xstream.XStream;

import de.mediaportal.episodenumbergenerator.MPEpisodeNumberGenerator;
import de.mediaportal.episodenumbergenerator.model.Config;
import de.mediaportal.episodenumbergenerator.model.series.Updates;
import de.mediaportal.episodenumbergenerator.model.series.data.EpisodeInformation;
import de.mediaportal.episodenumbergenerator.model.series.data.SeriesData;
import de.mediaportal.episodenumbergenerator.model.series.data.SeriesInformation;
import de.mediaportal.episodenumbergenerator.model.series.list.SeriesList;
import de.mediaportal.episodenumbergenerator.model.series.list.SeriesListEntry;

/**
 * Handles connections to thetvdb.com to find season numbers and episode
 * numbers. XML data that is returned from the api is persisted in the local
 * file cache and only updated, if the file cache is cleared manually. <br>
 * <br>
 * 
 * @author Oliver
 * 
 */
public class TheTvDbController {
	/**
	 * Path to the xml file cache ({@value #PATH_CACHE})
	 */
	protected final static String PATH_CACHE = "cache/";

	/**
	 * Filename of the mirror list ({@value #FILENAME_MIRROR_LIST})
	 */
	protected final static String FILENAME_MIRROR_LIST = "mirrors.xml";

	/**
	 * Filename of the persisted query files ({@value #FILENAME_QUERY_FILE}).
	 * The $ is replaced by the query pattern
	 */
	protected final static String FILENAME_QUERY_FILE = "query_$.xml";

	/**
	 * Prefix of the persisted seriesdata files (
	 * {@value #FILENAME_QUERY_SERIESDATA_PREFIX} ). The $ is replaced by the
	 * query pattern
	 */
	protected final static String FILENAME_QUERY_SERIESDATA_PREFIX = "seriesdata";

	/**
	 * Filename of the persisted seriesdata files (
	 * {@value #FILENAME_QUERY_SERIESDATA} ). The $ is replaced by the query
	 * pattern
	 */
	protected final static String FILENAME_QUERY_SERIESDATA = FILENAME_QUERY_SERIESDATA_PREFIX + "_$.xml";

	/**
	 * Config of {@link MPEpisodeNumberGenerator}
	 */
	protected static Config config = MPEpisodeNumberGenerator.getConfig();

	/**
	 * All series and episodes updated since the last time when the application
	 * cached data
	 */
	protected static Updates updatesSinceLastCache = null;

	/**
	 * Name of the series which data is loaded from thetvdb.com
	 */
	protected String querySeriesName = null;

	/**
	 * Air year of the episode which data is loaded from thetvdb.com. The air
	 * year can help to identify the correct series if more than one is found
	 */
	protected String queryAirYear = null;

	/**
	 * ID of a series, if the search was successful
	 */
	protected String seriesId = null;

	/**
	 * Parsed SeriesData
	 */
	protected SeriesData seriesData = null;

	/**
	 * Timestamp of the date 30 days before (time since Epoch in ms)
	 */
	private static long timestamp30DaysBefore = -1;

	/**
	 * Logger of the class {@link TheTvDbController}
	 */
	protected static Logger logger = LogManager.getLogger(TheTvDbController.class);

	/**
	 * Constructs a new instance of the controller that communicates with the
	 * thetvdb API or uses the local file cache to find season and episode
	 * numbers
	 * 
	 * @param querySeriesName
	 *            series name from MediaPortal to find the series
	 * @param queryAirYear
	 *            original air year of the episode to resolve the series by date
	 *            if there are too much series with that name found
	 * @throws IOException
	 *             Is thrown, if it is not possible to persist the lastrun
	 *             timestamp to the {@link Config}
	 */
	public TheTvDbController(String querySeriesName, String queryAirYear) throws IOException {
		this.queryAirYear = queryAirYear;
		if (timestamp30DaysBefore == -1) {
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			c.add(Calendar.DAY_OF_MONTH, -30);
			timestamp30DaysBefore = c.getTimeInMillis();
		}

		logger.info("Resolving series '" + querySeriesName + "' with air year '" + queryAirYear + "' using thetvdb...");

		// Change query if substitution exists
		if (MPEpisodeNumberGenerator.getSeriesNameSubstitutions() != null) {
			String substitute = MPEpisodeNumberGenerator.getSeriesNameSubstitutions().get(querySeriesName);
			if (substitute != null) {
				logger.debug("Substitution '" + substitute + "' found for query '" + querySeriesName + "'");
				this.querySeriesName = substitute;
			} else {
				this.querySeriesName = querySeriesName;
			}
		}

		if (querySeriesName != null) {
			this.querySeriesName = this.querySeriesName.replaceAll("[\\/:*\"<>|?]", "");
		}

		// Read Updates since last caching
		readUpdates();

		// Try to find series by name
		findSeriesByQueryString();

		// Get series episode data
		getSeriesEpisodeData();

	}

	/**
	 * Analyzes the file cache of the application when the class is initialized
	 * the first time and requests updated series' IDs from thetvdb.com. If
	 * there is no cache, {@link #updatesSinceLastCache} will be null. If cache
	 * files exist, it will store the IDs of the updated series since the last
	 * cache timestamp
	 * 
	 * @throws MalformedURLException
	 */
	private void readUpdates() throws MalformedURLException {
		if (updatesSinceLastCache == null) {
			XStream xstream = new XStream();
			xstream.ignoreUnknownElements();
			xstream.processAnnotations(Updates.class);
			long oldestCacheTimestamp = clearCacheAndGetOldestCacheTimestamp();
			if (oldestCacheTimestamp != -1) {
				String updatesUrl = "http://thetvdb.com/api/Updates.php?type=all&time=" + oldestCacheTimestamp;
				logger.info("Fetching updates since last cache from URL " + updatesUrl);
				updatesSinceLastCache = (Updates) xstream.fromXML(new URL(updatesUrl));
			} else {
				updatesSinceLastCache = new Updates();
			}
		}

	}

	/**
	 * Analyzes the file cache of the application to determine the oldest file
	 * modified time stamp of a seriesdata file
	 * 
	 * @return oldest unix time stamp of the files in cache or null, if there is
	 *         no file cache yet
	 */
	private static long clearCacheAndGetOldestCacheTimestamp() {
		File cacheDirectory = new File(PATH_CACHE);

		File[] seriesDataFilesToClear = cacheDirectory.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				if (isOlder30Days(pathname)) {
					return true;
				} else {
					return false;
				}
			}

			public boolean isOlder30Days(File f) {
				if (f != null && f.exists() && f.lastModified() < timestamp30DaysBefore) {
					return true;
				} else {
					return false;
				}
			}

		});
		if (seriesDataFilesToClear != null && seriesDataFilesToClear.length > 0) {
			logger.info("Deleting " + seriesDataFilesToClear.length + " found files in cache older 30 days.");
			int errors = 0;
			for (File f : seriesDataFilesToClear) {
				try {
					f.delete();
				} catch (Exception e) {
					errors++;
				}
			}
			if (errors != 0) {
				logger.warn("Could not delete " + errors + " files in your file cache. Check file permissions");
			}
		} else {
			logger.info("No files in cache older 30 days found. Continuing.");
		}

		File[] seriesDataFiles = cacheDirectory.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(FILENAME_QUERY_SERIESDATA_PREFIX);
			}
		});
		if (seriesDataFiles != null && seriesDataFiles.length > 0) {
			Arrays.sort(seriesDataFiles);
			File oldestFile = seriesDataFiles[0];
			logger.info("Oldest file in cache " + oldestFile.getName() + " from " + new Date(oldestFile.lastModified()));
			return oldestFile.lastModified() / 1000;
		} else {
			return -1;
		}
	}

	/**
	 * Tries to find a series by the given query string using the GetSeries
	 * function of the thetvdb.com API
	 * 
	 * @throws IOException
	 *             Is thrown, if there are problems with the local file cache
	 */
	private void findSeriesByQueryString() throws IOException {
		File queryBySeriesNameXml = new File(PATH_CACHE + FILENAME_QUERY_FILE.replace("$", querySeriesName));
		String getSeriesUrl = "http://thetvdb.com/api/GetSeries.php?seriesname=" + URLEncoder.encode(querySeriesName, "UTF-8")
				+ "&language=all";
		logger.debug("Trying to resolve series with url " + getSeriesUrl);

		XStream xstream = new XStream();
		xstream.ignoreUnknownElements();
		xstream.processAnnotations(SeriesList.class);
		xstream.processAnnotations(SeriesListEntry.class);

		SeriesList seriesListObject = (SeriesList) parseFromCacheOrUrl(queryBySeriesNameXml, getSeriesUrl, xstream);
		Vector<SeriesListEntry> seriesList = seriesListObject.getSeriesListEntries();
		if (seriesList != null) {
			int foundSeries = seriesList.size();
			if (foundSeries > 0) {
				if (foundSeries == 1) {
					// Found series by name without problems
					// logger.info("Found the series with the specified name.");
					SeriesListEntry seriesEntry = seriesList.get(0);
					seriesId = seriesEntry.getSeriesId();
				} else {
					// More than one series found, trying to find language
					logger.debug("Found " + foundSeries + " series for the specified name. Resolving by language.");
					Vector<SeriesListEntry> seriesByLanguage = new Vector<>();
					for (SeriesListEntry seriesEntry : seriesList) {
						String seriesLanguage = seriesEntry.getLanguage();
						if (seriesLanguage != null && seriesLanguage.equalsIgnoreCase(config.getLanguage())) {
							seriesByLanguage.add(seriesEntry);
						}
					}

					// More than one series with specified language found,
					// trying to find by air date
					if (seriesByLanguage.size() == 0) {
						logger.debug("Found no series with given language.");
					} else if (seriesByLanguage.size() == 1) {
						logger.debug("Found 1 series with given language. Found series successfully.");
						seriesId = seriesByLanguage.get(0).getSeriesId();
					} else if (seriesByLanguage.size() > 1) {
						logger.debug("Found " + foundSeries + " series for the specified name and language. Resolving by air date.");
						for (SeriesListEntry seriesEntry : seriesByLanguage) {
							String airDateTmp = seriesEntry.getFirstAired();
							String seriesIdTmp = seriesEntry.getSeriesId();
							if (queryAirYear != null && airDateTmp != null && airDateTmp.length() > 4
									&& queryAirYear.equalsIgnoreCase(airDateTmp.substring(0, 4))) {
								seriesId = seriesIdTmp;
								break;
							}
						}
					}
				}
			}
		}

	}

	/**
	 * If it was possible to find a episode its data is parsed from the api or
	 * from the local file cache
	 * 
	 * @throws IOException
	 *             Is thrown, if there are problems with the local file cache
	 */
	private void getSeriesEpisodeData() throws IOException {
		if (seriesId != null) {
			File seriesDataFile = new File(PATH_CACHE + FILENAME_QUERY_SERIESDATA.replace("$", seriesId + "_" + querySeriesName));
			logger.debug("Chosen seriesId is '" + seriesId + "'");

			// Delete local file if new data is found at thetvdb
			if (updatesSinceLastCache != null && updatesSinceLastCache.contains(seriesId)) {

				if (seriesDataFile.exists()) {
					logger.info("Deleting file " + seriesDataFile.getName()
							+ " because new data on thetvdb has been found since last caching");
					seriesDataFile.delete();
				}
			}

			String getSeriesDataUrl = config.getProxyUrl() + "series/?seriesid=" + seriesId + "&language=" + config.getLanguage();
			XStream xstreamSeriesData = new XStream();
			xstreamSeriesData.ignoreUnknownElements();
			xstreamSeriesData.processAnnotations(SeriesData.class);
			xstreamSeriesData.processAnnotations(SeriesInformation.class);
			xstreamSeriesData.processAnnotations(EpisodeInformation.class);
			seriesData = (SeriesData) parseFromCacheOrUrl(seriesDataFile, getSeriesDataUrl, xstreamSeriesData);
		}
	}

	/**
	 * Checks if XML file is saved in local data cache. If not, loads XML from
	 * URL and saves it in the cache. After that the XML is parsed using the
	 * given XStream-Parser and returned.
	 * 
	 * @param xmlLocalFile
	 *            local file cache
	 * @param xmlUrl
	 *            remote XML url
	 * @param xstream
	 *            preconfigured XStream parser
	 * @return parsed Object
	 * @throws IOException
	 *             Is thrown, if there are problems with the local file cache
	 */
	private static Object parseFromCacheOrUrl(File xmlLocalFile, String xmlUrl, XStream xstream) throws IOException {
		Object parsedXml = null;
		if (!xmlLocalFile.exists()) {
			logger.debug("Persisting XML from URL '" + xmlUrl + "'");
			URL xmlUrlObject = new URL(xmlUrl);
			parsedXml = xstream.fromXML(xmlUrlObject);
			wget(xmlUrlObject, xmlLocalFile);
		} else {
			parsedXml = xstream.fromXML(xmlLocalFile);
		}
		return parsedXml;

	}

	/**
	 * Persists a remote file to a local file object
	 * 
	 * @param url
	 *            remote URL
	 * @param localFile
	 *            local {@link File} Object
	 * @throws IOException
	 *             Is thrown, if there are problems saving to the local file
	 */
	private static void wget(URL url, File localFile) throws IOException {
		ReadableByteChannel rbc = Channels.newChannel(url.openStream());
		FileOutputStream fos = new FileOutputStream(localFile);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
	}

	/**
	 * @return seriesData after the search is complete
	 */
	public SeriesData getSeriesData() {
		return seriesData;
	}
}
