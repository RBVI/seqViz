package edu.ucsf.rbvi.seqViz.internal.events;

import java.util.EventObject;

import edu.ucsf.rbvi.seqViz.internal.model.DisplayGraphSettings;

public class DisplayGraphEvent extends EventObject {

	private DisplayGraphSettings settings;
	
	public DisplayGraphEvent(Object source) {
		super(source);
		// TODO Auto-generated constructor stub
	}
	
	public void setDisplayGraphSettings(DisplayGraphSettings s) {
		settings = s;
	}
	
	public DisplayGraphSettings getDisplayGraphSettings() {
		return settings;
	}
}
