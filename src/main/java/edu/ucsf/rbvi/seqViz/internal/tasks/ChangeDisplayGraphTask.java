package edu.ucsf.rbvi.seqViz.internal.tasks;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.seqViz.internal.events.FireDisplayGraphEvent;
import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager.ReadType;
import edu.ucsf.rbvi.seqViz.internal.model.DisplayGraphSettings;

public class ChangeDisplayGraphTask extends AbstractTask implements
		ObservableTask {

	private FireDisplayGraphEvent settings;
	private ReadType rType;
	
	public ChangeDisplayGraphTask(FireDisplayGraphEvent graphEvent, ReadType rType) {
		settings = graphEvent;
		this.rType = rType;
	}
	
	public <R> R getResults(Class<? extends R> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		arg0.setTitle(getTitle());
		settings.getDisplayGraphEvent().getDisplayGraphSettings().setReadType(rType);
		settings.fireGraphSelectionChange();
	}

	@ProvidesTitle
	public String getTitle() {
		return "Changing to "+rType.toString()+" reads";
	}

}
