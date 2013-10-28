package edu.ucsf.rbvi.seqViz.internal.tasks;

import java.util.Collection;
import java.util.HashMap;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;
import edu.ucsf.rbvi.seqViz.internal.model.DisplayGraphSettings;

/**
 * This task changes the visual style of a network. See src/main/java/resources for styles
 * available.
 * @author aywu
 *
 */
public class ChangeStyleTask extends AbstractTask {
	
	private CyNetworkView networkView;
	private HashMap<String, VisualStyle> vs;
	private DisplayGraphSettings settings;
	
	public ChangeStyleTask(CyNetworkView networkView, DisplayGraphSettings graphSettings, HashMap<String, VisualStyle> vs) {
		this.networkView = networkView;
		this.settings = graphSettings;
		this.vs = vs;
	}
	
	@Override
	public void run(TaskMonitor arg0) throws Exception {
		// TODO Auto-generated method stub
		settings.networkViewSetting = vs;
		if (vs.containsKey(settings.graphSelection))
			vs.get(settings.graphSelection).apply(networkView);
		else
			vs.get(null).apply(networkView);
		networkView.updateView();
	}

}
