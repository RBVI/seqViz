package edu.ucsf.rbvi.seqViz.internal.tasks;

import java.util.Collection;

import org.cytoscape.task.AbstractNetworkViewCollectionTaskFactory;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;

public class ChangeStyleTaskFactory extends AbstractNetworkViewTaskFactory {

	private VisualStyle vs;
	
	public ChangeStyleTaskFactory(/* ContigsManager contigs, */ VisualStyle vs) {
		this.vs = vs;
	}

	public TaskIterator createTaskIterator(CyNetworkView arg0) {
		// TODO Auto-generated method stub
		return new TaskIterator(new ChangeStyleTask(arg0, vs));
	}

}
