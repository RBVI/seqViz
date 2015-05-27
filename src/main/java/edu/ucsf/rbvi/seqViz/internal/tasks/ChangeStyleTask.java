package edu.ucsf.rbvi.seqViz.internal.tasks;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;
import edu.ucsf.rbvi.seqViz.internal.model.DisplayGraphSettings;
import edu.ucsf.rbvi.seqViz.internal.utils.StyleMaker.HistogramType;

/**
 * This task changes the visual style of a network. See src/main/java/resources for styles
 * available.
 * @author aywu
 *
 */
public class ChangeStyleTask extends AbstractTask {
	
	private CyNetworkView networkView;
	private HistogramType hType;
	private DisplayGraphSettings settings;
	
	public ChangeStyleTask(CyNetworkView networkView, DisplayGraphSettings graphSettings, 
	                       HistogramType hType) {
		this.networkView = networkView;
		this.settings = graphSettings;
		this.hType = hType;
	}
	
	@Override
	public void run(TaskMonitor arg0) throws Exception {
		arg0.setTitle(getTitle());
		if (settings.getHistogramType().equals(hType))
			return;

		settings.setHistogramType(hType);
		VisualStyle vs = settings.getVisualStyle();
		vs.apply(networkView);
		networkView.updateView();
	}

	@ProvidesTitle
	public String getTitle() {
		return "Changing to use "+hType+" histograms";
	}

}
