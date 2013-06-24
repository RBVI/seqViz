package edu.ucsf.rbvi.seqViz.internal.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.seqViz.internal.model.Contig;
import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;

public class ReadFASTAContigsTask extends AbstractReadContigsTask {
	
	public ReadFASTAContigsTask(ContigsManager manager) {
		super(manager);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		// TODO Auto-generated method stub
		BufferedReader reader = new BufferedReader(new FileReader(contigsFile));
		String line, header = " ";
		StringBuilder seq = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.charAt(0) == '>') {
				if (line.length() > 1)
					header = line.substring(1);
				else header = "";
				manager.addContig(header, new Contig(seq.toString()));
				seq = new StringBuilder();
			}
			else seq = seq.append(line);
		}
		manager.addContig(header, new Contig(seq.toString()));
	}

}
