package edu.ucsf.rbvi.seqViz.internal.tasks;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
	private Map<String, VisualStyle> vs;
	private DisplayGraphSettings settings;
	private ContigsManager manager;
	
	public ChangeStyleTask(CyNetworkView networkView, DisplayGraphSettings graphSettings, 
	                       Map<String, VisualStyle> vs) {
		this.networkView = networkView;
		this.settings = graphSettings;
		this.vs = vs;
	}
	
	@Override
	public void run(TaskMonitor arg0) throws Exception {
		settings.networkViewSetting = vs;
		if (vs.containsKey(settings.graphSelection))
			vs.get(settings.graphSelection).apply(networkView);
		else
			vs.get(null).apply(networkView);
		networkView.updateView();
	}

}
