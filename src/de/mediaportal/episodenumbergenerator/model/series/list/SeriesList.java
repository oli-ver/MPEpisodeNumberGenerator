package de.mediaportal.episodenumbergenerator.model.series.list;

import java.util.Vector;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * {@link XStream} annotated Wrapper class with a list of
 * {@link SeriesListEntry} objects. This class is used to parse the information
 * returned by the api method GetSeries of thetvdb.com
 * 
 * @see <a
 *      href="http://thetvdb.com/wiki/index.php?title=API:GetSeries">http://thetvdb.com/wiki/index.php?title=API:GetSeries</a>
 * 
 * @author Oliver
 *
 */
@XStreamAlias("Data")
public class SeriesList {
	/**
	 * List with {@link SeriesListEntry} objects
	 */
	@XStreamImplicit(itemFieldName = "Series")
	protected Vector<SeriesListEntry> seriesListEntries = null;

	/**
	 * @return the seriesListEntries
	 */
	public Vector<SeriesListEntry> getSeriesListEntries() {
		return seriesListEntries;
	}
}
