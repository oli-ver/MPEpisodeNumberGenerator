/**
 * 
 */
package de.mediaportal.episodenumbergenerator.model.series;

import java.util.Vector;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Object representation of thetvdb.com Updates api function
 * 
 * @author Oliver
 *
 */
@XStreamAlias("Items")
public class Updates {
	@XStreamImplicit(itemFieldName = "Series")
	protected Vector<String> seriesId = null;

	/**
	 * @param o
	 * @return
	 * @see java.util.Vector#contains(java.lang.Object)
	 */
	public boolean contains(Object o) {
		return seriesId!=null && seriesId.contains(o);
	}
}
