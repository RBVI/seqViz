package edu.ucsf.rbvi.seqViz.internal.events;

import java.util.HashSet;

public class FireDisplayGraphEvent {
	private HashSet<DisplayGraphEventListener> eventListener;
	private DisplayGraphEvent event;
	
	public FireDisplayGraphEvent(DisplayGraphEvent e) {
		eventListener = new HashSet<DisplayGraphEventListener>();
		event = e;
	}
	
	public DisplayGraphEvent getDisplayGraphEvent() {return event;}
	
	public void addDisplayGraphEventListener(DisplayGraphEventListener listener) {
		if (!eventListener.contains(listener)) eventListener.add(listener);
	}
	
	public void removeDisplayGraphEventListener(DisplayGraphEventListener listener) {
		if (eventListener.contains(listener)) eventListener.remove(listener);
	}
	
	public void fireGraphSelectionChange() {
		for (DisplayGraphEventListener listener: eventListener)
			listener.graphSelectionChange(event);
	}
}
