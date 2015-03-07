package de.mediaportal.episodenumbergenerator.model.series.list;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Series entries from thetvdb.com GetSeries api function<br>
 * <br>
 * 
 * @see <a
 *      href="http://thetvdb.com/wiki/index.php?title=API:GetSeries">http://thetvdb.com/wiki/index.php?title=API:GetSeries</a>
 * 
 * @author Oliver
 *
 */
@XStreamAlias("Series")
public class SeriesListEntry {
	/**
	 * Unique ID of the series
	 */
	@XStreamAlias("seriesid")
	protected String seriesid = null;

	/**
	 * Series name
	 */
	@XStreamAlias("SeriesName")
	protected String seriesName = null;

	/**
	 * Date when the Series was aired first
	 */
	@XStreamAlias("FirstAired")
	protected String firstAired = null;

	/**
	 * Language
	 */
	@XStreamAlias("language")
	protected String language = null;

	/**
	 * @return Unique ID of the series
	 */
	public String getSeriesId() {
		return seriesid;
	}

	/**
	 * @return Series name
	 */
	public String getSeriesName() {
		return seriesName;
	}

	/**
	 * @return Date when the Series was aired first
	 */
	public String getFirstAired() {
		return firstAired;
	}

	/**
	 * @return Language
	 */
	public String getLanguage() {
		return language;
	}
}
