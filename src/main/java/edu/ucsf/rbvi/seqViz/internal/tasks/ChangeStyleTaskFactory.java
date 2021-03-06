package edu.ucsf.rbvi.seqViz.internal.tasks;

import java.util.Collection;
import java.util.Map;

import org.cytoscape.task.AbstractNetworkViewCollectionTaskFactory;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;
import edu.ucsf.rbvi.seqViz.internal.model.DisplayGraphSettings;
import edu.ucsf.rbvi.seqViz.internal.utils.StyleMaker.HistogramType;

public class ChangeStyleTaskFactory extends AbstractNetworkViewTaskFactory {

	private HistogramType hType;
	private DisplayGraphSettings settings;
	
	public ChangeStyleTaskFactory(DisplayGraphSettings graphSettings, HistogramType hType) {
		this.hType = hType;
		settings = graphSettings;
	}

	public TaskIterator createTaskIterator(CyNetworkView arg0) {
		return new TaskIterator(new ChangeStyleTask(arg0, settings, hType));
	}

}
