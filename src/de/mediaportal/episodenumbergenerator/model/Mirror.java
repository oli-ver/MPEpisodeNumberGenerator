package de.mediaportal.episodenumbergenerator.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * XStream annotated class with the needed fields of the mirror xml file. The
 * function is actually not in need anymore. thetvdb.com use a round robin DNS
 * technique to control this on their sid
 * 
 * @author Oliver
 *
 */
@XStreamAlias("Mirror")
public class Mirror {
	/**
	 * Unique id of the mirror
	 */
	@XStreamAlias("id")
	protected String id = null;

	/**
	 * Mirror's url
	 */
	@XStreamAlias("mirrorpath")
	protected String url = null;

	/**
	 * Mirror's typemask
	 */
	@XStreamAlias("typemask")
	protected String typemask = null;

	public Mirror(String proxyUrl) {
		this.url = proxyUrl;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @return the typemask
	 */
	public String getTypemask() {
		return typemask;
	}

	/**
	 * @return tld parsed from url
	 */
	public String getTld() {
		if (url != null && url.lastIndexOf('.') != -1) {
			return url.substring(url.lastIndexOf('.'));
		} else {
			return null;
		}
	}

}
