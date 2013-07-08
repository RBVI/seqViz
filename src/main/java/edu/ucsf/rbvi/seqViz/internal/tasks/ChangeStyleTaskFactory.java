package edu.ucsf.rbvi.seqViz.internal.tasks;

import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;

public class ChangeStyleTaskFactory extends AbstractTaskFactory {

	private ContigsManager contigs;
	private VisualStyle vs;
	
	public ChangeStyleTaskFactory(ContigsManager contigs, VisualStyle vs) {
		this.contigs = contigs;
		this.vs = vs;
	}
	
	public TaskIterator createTaskIterator() {
		// TODO Auto-generated method stub
		return new TaskIterator(new ChangeStyleTask(contigs, vs));
	}

}
