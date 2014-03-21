package edu.ucsf.rbvi.seqViz.internal.tasks;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;

import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;

public class MapReadsTaskFactory extends AbstractTaskFactory {
	
	private ContigsManager manager;
	private TaskManager<?,?> taskManager;
	
	public MapReadsTaskFactory(TaskManager<?,?> taskManager, ContigsManager manager) {
		this.manager = manager;
		this.taskManager = taskManager;
	}
	
	public boolean isReady() { return manager.isInitialized() && manager.mapperSettingsIntiialized(); }
	
	public TaskIterator createTaskIterator() {
		return new TaskIterator(manager.getSettings().mapReads);
	}
}
