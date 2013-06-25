package edu.ucsf.rbvi.seqViz.internal.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.util.HashMap;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;

public abstract class AbstractMapReadsTask extends AbstractTask {

	protected ContigsManager contigs;
	
	/**
	 * AbstractMaperReaders is an abstract class that serves as an interface to map reads to
	 * contigs using any mapper. The constructor takes in parameters required for mapping
	 * the reads to the contigs, including the file which contains the contigs, the reads,
	 * temporary folders to store the index, and other parameters.
	 * 
	 * @param contigs ContigManager that stores the mapping results.
	 * @param mate1 Files containing paired-end or mate-pair 1.
	 * @param mate2 Files containing paired-end or mate-pair 2.
	 */
	public AbstractMapReadsTask(ContigsManager contigs/*, String mate1, String mate2 */) {
		this.contigs = contigs;
	/*	if (contigs.getSettings().mate1 != null && ! contigs.getSettings().mate1.isEmpty())
			this.mate1 = contigs.getSettings().mate1.get(0);
		if (contigs.getSettings().mate2 != null && ! contigs.getSettings().mate2.isEmpty())
			this.mate2 = contigs.getSettings().mate2.get(0); */
	}
}
