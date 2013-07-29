package edu.ucsf.rbvi.seqViz.internal.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

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
		
		BufferedReader mate1Reads = new BufferedReader(new FileReader(mate1));
		String line;
		long sample = 10000, sampleSize = 0;
		for (int i = 0; i < sample; i++) {
			line = mate1Reads.readLine();
			sampleSize += line.length();
		}
		mate1Reads.close();
		long readEstimate = mate1.length() * (sample / 4) / sampleSize;
		
		Process index = Runtime.getRuntime().exec(contigs.getSettings().mapper_dir + "bowtie2-build -f " + contigsFile.getAbsolutePath() + " " + contigs.getSettings().temp_dir + contigsFile.getName());
		index.waitFor();
		if (index.exitValue() != 0)
			throw new Exception("bowtie2-build exited with error " + index.exitValue());
		Process p = Runtime.getRuntime().exec(contigs.getSettings().mapper_dir + "bowtie2 -q --end-to-end --fast -p " + contigs.getSettings().threads + " --phred64 -a -x " + contigs.getSettings().temp_dir + contigsFile.getName() + " -1 " + mate1.getAbsolutePath() + " -2 " + mate2.getAbsolutePath());
		AbstractMapOutputReader reader = new SAMReader(contigs);
		reader.readReads(p.getInputStream(), arg0, readEstimate);
		p.waitFor();
		if (p.exitValue() != 0)
			throw new Exception("bowtie2-align exited with error " + p.exitValue());
		File indexFile;
		String[] suffixes = {".1.bt2", ".2.bt2", ".3.bt2", ".4.bt2", ".rev.1.bt2", ".rev.2.bt2"};
		for (String suffix : suffixes) {
			indexFile = new File(contigs.getSettings().temp_dir + contigsFile.getName() + suffix);
			indexFile.delete();
		}
		
	//	contigs.displayBridgingReads();
	//	contigs.createHist(200);
	//	contigs.loadBpGraphs(50);
		contigs.saveBpGraphs();
		contigs.saveBridgingReads();
		contigs.saveHist();
		contigs.displayNetwork();
		System.gc();
	}

}
