package edu.ucsf.rbvi.seqViz.internal.tasks;

import java.io.File;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;

public abstract class AbstractReadContigsTask extends AbstractTask {
	
	protected ContigsManager manager;
	
	public AbstractReadContigsTask(ContigsManager manager) {
		this.manager = manager;
	/*	if (this.manager.getSettings().mapContigs != null)
			contigsFile = this.manager.getSettings().mapContigs; */
	}

}
