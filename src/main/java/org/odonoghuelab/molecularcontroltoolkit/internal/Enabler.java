package org.odonoghuelab.molecularcontroltoolkit.internal;

/**
 * Interface to allow an object to be enabled or disabled.
 * @author KennySabir
 *
 */
public interface Enabler {

	/**
	 * Set enabled to the given parameter
	 * @param enabled 
	 */
	void setEnable(boolean enabled);
	
	/**
	 * @return if enabled
	 */
	boolean isEnabled();
}
