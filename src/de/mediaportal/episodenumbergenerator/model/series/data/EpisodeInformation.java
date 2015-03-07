package de.mediaportal.episodenumbergenerator.model.series.data;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link XStream} annotated class with the needed fields of the episode
 * informations returned by the api function
 * 
 * @author Oliver
 *
 */
@XStreamAlias("Episode")
public class EpisodeInformation {
	/**
	 * Unique ID of the episode
	 */
	@XStreamAlias("id")
	protected String id = null;

	/**
	 * Episode number
	 */
	@XStreamAlias("EpisodeNumber")
	protected String episodeNumber = null;

	/**
	 * Season number
	 */
	@XStreamAlias("SeasonNumber")
	protected String seasonNumber = null;

	/**
	 * Date when the episode was first aired
	 */
	@XStreamAlias("FirstAired")
	protected String firstAired = null;

	/**
	 * Name of the episode
	 */
	@XStreamAlias("EpisodeName")
	protected String episodeName = null;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the episodeNumber
	 */
	public String getEpisodeNumber() {
		return episodeNumber;
	}

	/**
	 * @return the seasonNumber
	 */
	public String getSeasonNumber() {
		return seasonNumber;
	}

	/**
	 * @return the firstAired
	 */
	public String getFirstAired() {
		return firstAired;
	}

	/**
	 * @return the episodeName
	 */
	public String getEpisodeName() {
		return episodeName;
	}

}
