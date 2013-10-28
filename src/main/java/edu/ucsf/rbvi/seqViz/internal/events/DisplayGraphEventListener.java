package edu.ucsf.rbvi.seqViz.internal.events;

import java.util.EventListener;

public interface DisplayGraphEventListener extends EventListener {
	public void graphSelectionChange(DisplayGraphEvent event);
}
