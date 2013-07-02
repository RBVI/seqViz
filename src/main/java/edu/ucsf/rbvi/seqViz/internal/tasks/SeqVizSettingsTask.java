package edu.ucsf.rbvi.seqViz.internal.tasks;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;
import edu.ucsf.rbvi.seqViz.internal.model.SeqVizSettings;

public class SeqVizSettingsTask extends AbstractTask {
	
	private static final String[] mapperChoice = {"bowtie"};
	private ContigsManager manager;
	@Tunable(description="Load only reads that bridge contigs")
	public boolean loadBridgingReads;
	@Tunable(description="Directory to put temporary results")
	public String tempDir;
	@Tunable(description="Directory containing mapper binaries")
	public String mapDir;
	@Tunable(description="Choose mapper")
	public ListSingleSelection<String> mapper;
	
	public SeqVizSettingsTask(ContigsManager manager) {
		mapper = new ListSingleSelection<String>(mapperChoice);
		this.manager = manager;
		loadBridgingReads = false;
	}
	
	@Override
	public void run(TaskMonitor arg0) throws Exception {
		// TODO Auto-generated method stub
		if (mapper.getSelectedValue().equals(mapperChoice[0])) {
			if (!manager.isInitialized())
				manager.initializeSettings(new SeqVizSettings(new BowtieMapReadsTask(manager), 2, mapDir, tempDir, loadBridgingReads));
			else {
				manager.getSettings().mapReads = new BowtieMapReadsTask(manager);
				manager.getSettings().threads = 2;
				manager.getSettings().mapper_dir = mapDir;
				manager.getSettings().temp_dir = tempDir;
				manager.getSettings().loadBridingReads = loadBridgingReads;
			}
		}
	}

}
