package de.mediaportal.episodenumbergenerator.model.series.data;

import java.util.Vector;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * {@link XStream} annotated class of the series information returned by
 * thetvdb.com api
 * 
 * @author Oliver
 *
 */
@XStreamAlias("Data")
public class SeriesData {
	/**
	 * SeriesInformation
	 */
	@XStreamAlias("Series")
	protected SeriesInformation seriesInformation = null;

	/**
	 * List with all episodes known by thetvdb.com
	 */
	@XStreamImplicit(itemFieldName = "Episode")
	protected Vector<EpisodeInformation> episodeList = null;

	/**
	 * @return the seriesInformation
	 */
	public SeriesInformation getSeriesInformation() {
		return seriesInformation;
	}

	/**
	 * @return the episodeList
	 */
	public Vector<EpisodeInformation> getEpisodeList() {
		return episodeList;
	}
}
