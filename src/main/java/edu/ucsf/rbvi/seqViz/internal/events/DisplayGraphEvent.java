package edu.ucsf.rbvi.seqViz.internal.events;

import java.util.EventObject;

import edu.ucsf.rbvi.seqViz.internal.model.DisplayGraphSettings;

public class DisplayGraphEvent extends EventObject {

	private DisplayGraphSettings settings;
	
	public DisplayGraphEvent(Object source) {
		super(source);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Set the DisplayGraphicSettings object.
	 * @param s DisplayGraphSettings object.
	 */
	public void setDisplayGraphSettings(DisplayGraphSettings s) {
		settings = s;
	}
	
	/**
	 * Get the DisplayGraphSettings object.
	 * @return DisplayGraphSettings object for this event.
	 */
	public DisplayGraphSettings getDisplayGraphSettings() {
		return settings;
	}
}
