package edu.ucsf.rbvi.seqViz.internal.tasks;

import java.io.File;

import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;

public class BowtieMapReadsTask extends AbstractMapReadsTask {

	@Tunable(description="File containing the contigs",params="input=true;fileCategory=unspecified")
	public File contigsFile;
	@Tunable(description="File containing reads of mate2", params="input=true;fileCategory=unspecified")
	public File mate2;
	@Tunable(description="File containing reads of mate1", params="input=true;fileCategory=unspecified")
	public File mate1;
	
	public BowtieMapReadsTask(ContigsManager contigs/*, String mate1, String mate2 */) {
		super(contigs/*, mate1, mate2*/);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		// TODO Auto-generated method stub
		if (contigs.getSettings().mate1 != null && !contigs.getSettings().mate1.isEmpty());
		if (contigs.getSettings().mate2 != null && !contigs.getSettings().mate2.isEmpty());
		
		ReadFASTAContigsTask contigReader = new ReadFASTAContigsTask(contigs);
		contigReader.contigsFile = contigsFile;
		contigReader.run(arg0);
		
		Process index = Runtime.getRuntime().exec(contigs.getSettings().mapper_dir + "bowtie2-build -f " + contigsFile.getAbsolutePath() + " " + contigs.getSettings().temp_dir + contigsFile.getName());
		index.waitFor();
		Process p = Runtime.getRuntime().exec(contigs.getSettings().mapper_dir + "bowtie2 -q --end-to-end --fast -p " + contigs.getSettings().threads + " --phred64 -a -x " + contigs.getSettings().temp_dir + contigsFile.getName() + " -1 " + mate1.getAbsolutePath() + " -2 " + mate2.getAbsolutePath());
		AbstractMapOutputReader reader = new SAMReader(contigs);
		reader.readReads(p.getInputStream(), arg0, 0);
		contigs.displayBridgingReads();
		contigs.createHist(200);
		contigs.displayNetwork();
	}

}
