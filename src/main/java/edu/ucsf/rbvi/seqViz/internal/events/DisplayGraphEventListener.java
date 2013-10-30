package edu.ucsf.rbvi.seqViz.internal.events;

import java.util.EventListener;

/**
 * Listener for DisplayGraphEvent. When a DisplayGraphSettings object has been changed,
 * the methods in this interface will be called to update the program to the changes.
 * @author Allan Wu
 *
 */
public interface DisplayGraphEventListener extends EventListener {
	
	/**
	 * This method is called when a new graph is selected.
	 * @param event The DisplayGraphEvent object that has been changed.
	 */
	public void graphSelectionChange(DisplayGraphEvent event);
}
