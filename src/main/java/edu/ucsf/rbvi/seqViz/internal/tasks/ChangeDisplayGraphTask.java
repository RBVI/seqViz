package edu.ucsf.rbvi.seqViz.internal.tasks;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.seqViz.internal.events.FireDisplayGraphEvent;
import edu.ucsf.rbvi.seqViz.internal.model.DisplayGraphSettings;

public class ChangeDisplayGraphTask extends AbstractTask implements
		ObservableTask {

	private FireDisplayGraphEvent settings;
	private String graph;
	
	public ChangeDisplayGraphTask(FireDisplayGraphEvent graphEvent, String graph) {
		settings = graphEvent;
		this.graph = graph;
	}
	
	public <R> R getResults(Class<? extends R> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		settings.getDisplayGraphEvent().getDisplayGraphSettings().graphSelection = graph;
		settings.fireGraphSelectionChange();
	}

}
