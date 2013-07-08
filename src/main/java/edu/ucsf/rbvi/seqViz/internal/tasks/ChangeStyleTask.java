package edu.ucsf.rbvi.seqViz.internal.tasks;

import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;

public class ChangeStyleTask extends AbstractTask {
	
	private ContigsManager contigs;
	private VisualStyle vs;
	
	public ChangeStyleTask(ContigsManager contigs, VisualStyle vs) {
		this.contigs = contigs;
		this.vs = vs;
	}
	
	@Override
	public void run(TaskMonitor arg0) throws Exception {
		// TODO Auto-generated method stub
		contigs.applyStyle(vs);
	}

}
