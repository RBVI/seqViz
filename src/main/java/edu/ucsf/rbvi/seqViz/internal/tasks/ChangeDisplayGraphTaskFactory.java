package edu.ucsf.rbvi.seqViz.internal.tasks;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.seqViz.internal.events.FireDisplayGraphEvent;
import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager.ReadType;
import edu.ucsf.rbvi.seqViz.internal.model.DisplayGraphSettings;

public class ChangeDisplayGraphTaskFactory extends AbstractTaskFactory {

	private FireDisplayGraphEvent settings;
	private ReadType rType;
	
	public ChangeDisplayGraphTaskFactory(FireDisplayGraphEvent graphSettings, ReadType rType) {
		settings = graphSettings;
		this.rType = rType;
	}
	
	public TaskIterator createTaskIterator() {
		// TODO Auto-generated method stub
		return new TaskIterator(new ChangeDisplayGraphTask(settings, rType));
	}

}
