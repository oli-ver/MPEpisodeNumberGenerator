package de.mediaportal.episodenumbergenerator.model.series.data;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link XStream} annotated class with needed fields of the series information
 * returned by thetvdb.com API
 * 
 * @author Oliver
 *
 */
@XStreamAlias("Series")
public class SeriesInformation {
	/**
	 * Unique ID of the series
	 */
	@XStreamAlias("id")
	protected String id = null;

	/**
	 * Name of the series
	 */
	@XStreamAlias("SeriesName")
	protected String seriesName = null;

	/**
	 * Date when the season was first aired
	 */
	@XStreamAlias("FirstAired")
	protected String firstAired = null;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the seriesName
	 */
	public String getSeriesName() {
		return seriesName;
	}

	/**
	 * @return the firstAired
	 */
	public String getFirstAired() {
		return firstAired;
	}

}
