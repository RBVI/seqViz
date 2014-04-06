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
		resetOSSettings();
	}
	
	@Override
	public void run(TaskMonitor arg0) throws Exception {
		// TODO Auto-generated method stub
		if (mapper.getSelectedValue().equals(mapperChoice[0])) {
			int cores = Runtime.getRuntime().availableProcessors();
			cores = Math.max(cores-2, 1);
			if (!manager.isInitialized())
				manager.initializeSettings(new SeqVizSettings(new BowtieMapReadsTask(manager), cores, mapDir, tempDir, loadBridgingReads));
			else {
				manager.getSettings().mapReads = new BowtieMapReadsTask(manager);
				manager.getSettings().threads = cores;
				manager.getSettings().mapper_dir = mapDir;
				manager.getSettings().temp_dir = tempDir;
				manager.getSettings().loadBridingReads = loadBridgingReads;
			}
		}
	}

	public void resetOSSettings() {
		// Get OS information and set seqManager
		// Set default folder to look for bowtie and store bowtie index
		String OS = System.getProperty("os.name").toLowerCase();
		if (OS.indexOf("win") >= 0) {
			loadBridgingReads = false;
			mapDir = "";
			tempDir = "%TEMP%\\";
			mapper.setSelectedValue("bowtie");
			try {
				run(null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 || OS.indexOf("sunos") >= 0 || OS.indexOf("mac") >= 0) {
			loadBridgingReads = false;
			mapDir = "";
			tempDir = "/tmp/";
			mapper.setSelectedValue("bowtie");
			try {
				run(null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
