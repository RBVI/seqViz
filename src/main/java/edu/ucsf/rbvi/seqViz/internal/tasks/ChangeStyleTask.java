package edu.ucsf.rbvi.seqViz.internal.tasks;

import java.util.Collection;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;

public class ChangeStyleTask extends AbstractTask {
	
	private CyNetworkView networkView;
	private VisualStyle vs;
	
	public ChangeStyleTask(CyNetworkView networkView, VisualStyle vs) {
		this.networkView = networkView;
		this.vs = vs;
	}
	
	@Override
	public void run(TaskMonitor arg0) throws Exception {
		// TODO Auto-generated method stub
		vs.apply(networkView);
	}

}
