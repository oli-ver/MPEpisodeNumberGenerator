/**
 * 
 */
package de.mediaportal.episodenumbergenerator.model.series;

import java.io.File;

/**
 * Overrides the compareTo method of {@link File} to sort files by the
 * {@link #lastModified()} date
 * 
 * @author Oliver
 *
 */
public class CacheFile extends File {

	/**
	 * Default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	public CacheFile(String pathname) {
		super(pathname);
	}

	/**
	 * Sorts file objects by their {@link #lastModified()} timestamp
	 */
	@Override
	public int compareTo(File pathname) {
		if (pathname != null) {
			return (int) (this.lastModified() - pathname.lastModified());
		} else {
			return 1;
		}
	}
}
