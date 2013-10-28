package edu.ucsf.rbvi.seqViz.internal.tasks;

import java.util.Collection;
import java.util.HashMap;

import org.cytoscape.task.AbstractNetworkViewCollectionTaskFactory;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;
import edu.ucsf.rbvi.seqViz.internal.model.DisplayGraphSettings;

public class ChangeStyleTaskFactory extends AbstractNetworkViewTaskFactory {

	private HashMap<String,VisualStyle> vs;
	private DisplayGraphSettings settings;
	
	public ChangeStyleTaskFactory(DisplayGraphSettings graphSettings, HashMap<String, VisualStyle> vs) {
		this.vs = vs;
		settings = graphSettings;
	}

	public TaskIterator createTaskIterator(CyNetworkView arg0) {
		// TODO Auto-generated method stub
		return new TaskIterator(new ChangeStyleTask(arg0, settings, vs));
	}

}
