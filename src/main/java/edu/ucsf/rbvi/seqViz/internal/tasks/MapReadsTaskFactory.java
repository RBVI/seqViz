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
		// TODO Auto-generated method stub
		return new TaskIterator(new ExecuteMapping());
	}

	private class ExecuteMapping extends AbstractTask {
		
		@Override
		public void run(TaskMonitor arg0) throws Exception {
			taskManager.execute(new TaskIterator(manager.getSettings().mapReads), new TaskObserver() {
				
				public void taskFinished(ObservableTask arg0) {
				}
				
				public void allFinished(FinishStatus arg0) {
					if (arg0.getType() == FinishStatus.Type.FAILED)
						manager.reset();
				}
			});
		}
		
	}
}
