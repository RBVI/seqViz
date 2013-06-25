package edu.ucsf.rbvi.seqViz.internal.tasks;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;

public class ReadFASTAContigsTaskFactory extends AbstractTaskFactory {

	private ContigsManager manager;
	
	public ReadFASTAContigsTaskFactory(ContigsManager manager) {
		this.manager = manager;
	}
	
	public TaskIterator createTaskIterator() {
		// TODO Auto-generated method stub
		return new TaskIterator(new ReadFASTAContigsTask(manager));
	}

}
