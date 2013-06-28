package edu.ucsf.rbvi.seqViz.internal.tasks;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;

public class MapReadsTaskFactory extends AbstractTaskFactory {
	
	private ContigsManager manager;
	
	public MapReadsTaskFactory(ContigsManager manager) {
		this.manager = manager;
	}
	
	public boolean isReady() { return manager.isInitialized() && manager.mapperSettingsIntiialized(); }
	
	public TaskIterator createTaskIterator() {
		// TODO Auto-generated method stub
		return new TaskIterator(manager.getSettings().mapReads);
	}

}
