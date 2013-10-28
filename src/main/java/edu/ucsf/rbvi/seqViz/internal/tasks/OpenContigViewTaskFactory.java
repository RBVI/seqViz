package edu.ucsf.rbvi.seqViz.internal.tasks;

import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.seqViz.internal.events.FireDisplayGraphEvent;
import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;

public class OpenContigViewTaskFactory extends AbstractNodeViewTaskFactory {
	
	private ContigsManager manager;
	private FireDisplayGraphEvent event;
	
	public OpenContigViewTaskFactory(ContigsManager manager, FireDisplayGraphEvent graphEvent) {
		this.manager = manager;
		this.event = graphEvent;
	}
	
	public TaskIterator createTaskIterator(View<CyNode> arg0, CyNetworkView arg1) {
		return new TaskIterator(new OpenContigViewTask(arg0, arg1, manager, event));
	}

}
