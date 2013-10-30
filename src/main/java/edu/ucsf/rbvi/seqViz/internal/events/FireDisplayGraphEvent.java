package edu.ucsf.rbvi.seqViz.internal.events;

import java.util.HashSet;

/**
 * Fires DisplayGraphEventListener when a DisplayGraphSettings object has been changed.
 * @author Allan Wu
 *
 */
public class FireDisplayGraphEvent {
	private HashSet<DisplayGraphEventListener> eventListener;
	private DisplayGraphEvent event;
	
	/**
	 * Constructor for this class
	 * @param e DisplayGraphEvent containing the DisplayGraphSettings object.
	 */
	public FireDisplayGraphEvent(DisplayGraphEvent e) {
		eventListener = new HashSet<DisplayGraphEventListener>();
		event = e;
	}
	
	/**
	 * Accessor method to get the DisplayGraphEvent.
	 * @return The DisplayGraphEvent for this object.
	 */
	public DisplayGraphEvent getDisplayGraphEvent() {return event;}
	
	/**
	 * Add a new DisplayGraphEventListener.
	 * @param listener listener to be added.
	 */
	public void addDisplayGraphEventListener(DisplayGraphEventListener listener) {
		if (!eventListener.contains(listener)) eventListener.add(listener);
	}
	
	/**
	 * Remove a DisplayGraphListener.
	 * @param listener listener to be removed.
	 */
	public void removeDisplayGraphEventListener(DisplayGraphEventListener listener) {
		if (eventListener.contains(listener)) eventListener.remove(listener);
	}
	
	/**
	 * Fire event for all listeners that have been added to this object.
	 */
	public void fireGraphSelectionChange() {
		for (DisplayGraphEventListener listener: eventListener)
			listener.graphSelectionChange(event);
	}
}
