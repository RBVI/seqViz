package edu.ucsf.rbvi.seqViz.internal.tasks;

import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;

public class BowtieMapReadsTask extends AbstractMapReadsTask {

	public BowtieMapReadsTask(ContigsManager contigs, String mate1, String mate2) {
		super(contigs/*, mate1, mate2*/);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		// TODO Auto-generated method stub
		if (contigs.getSettings().mate1 != null && !contigs.getSettings().mate1.isEmpty());
		if (contigs.getSettings().mate1 != null && !contigs.getSettings().mate2.isEmpty());
		Process index = Runtime.getRuntime().exec(contigs.getSettings().mapper_dir + "bowtie2-build -f " + contigsFile.getAbsolutePath() + " " + contigs.getSettings().temp_dir + contigsFile.getName());
		Process p = Runtime.getRuntime().exec(contigs.getSettings().mapper_dir + "bowtie2 -q --end-to-end --fast -p 2 --phred64 -a -x " + contigs.getSettings().temp_dir + contigsFile.getName() + " -1 " + mate1.getAbsolutePath() + " -2 " + mate2.getAbsolutePath());
		ReadsReader reader = new SAMReader(contigs);
		reader.readReads(p.getInputStream(), arg0, 0);
	}

}
