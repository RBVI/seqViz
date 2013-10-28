package edu.ucsf.rbvi.seqViz.internal.tasks;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.seqViz.internal.model.DisplayGraphSettings;

public class ChangeDisplayGraphTaskFactory extends AbstractTaskFactory {

	private DisplayGraphSettings settings;
	private String graph;
	
	public ChangeDisplayGraphTaskFactory(DisplayGraphSettings graphSettings, String graph) {
		settings = graphSettings;
		this.graph = graph;
	}
	
	public TaskIterator createTaskIterator() {
		// TODO Auto-generated method stub
		return new TaskIterator(new ChangeDisplayGraphTask(settings, graph));
	}

}
