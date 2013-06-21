package edu.ucsf.rbvi.seqViz.internal.tasks;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.seqViz.internal.model.SeqVizManager;

public class SeqVizSettingsTaskFactory extends AbstractTaskFactory {
	SeqVizManager manager;

	public SeqVizSettingsTaskFactory(SeqVizManager manager) {
		this.manager = manager;
	}

	public boolean isReady() { return true; }
	
	// TODO
	public TaskIterator createTaskIterator() { return null; }
}
