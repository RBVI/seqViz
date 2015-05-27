package edu.ucsf.rbvi.seqViz.internal.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.seqViz.internal.model.Contig;
import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;

public class ReadFASTAContigsTask extends AbstractReadContigsTask {
	
	@Tunable(description="File containing the contigs", params="input=true;fileCategory=unspecified")
	public File contigsFile;
	
	public ReadFASTAContigsTask(ContigsManager manager) {
		super(manager);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		// TODO Auto-generated method stub
		arg0.setTitle("Loading contigs");
		arg0.setStatusMessage("Loading contigs...");
		BufferedReader reader = new BufferedReader(new FileReader(contigsFile));
		manager.getSettings().contigs = contigsFile;
		String line, header = null;
		StringBuilder seq = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.charAt(0) == '>') {
			//	System.out.println(header);
			//	System.out.println(seq.toString());
				if (header != null)
					manager.addContig(header, new Contig(seq.toString()));
				seq = new StringBuilder();
				if (line.length() > 1) {
					header = line.substring(1).split(" ")[0];
				}
			}
			else seq = seq.append(line);
		}
	//	System.out.println(header);
	//	System.out.println(seq.toString());
		manager.addContig(header, new Contig(seq.toString()));
		manager.displayContigs();
	}

	@ProvidesTitle
	public String getTitle() {
		return "Reading contig file";
	}

}
