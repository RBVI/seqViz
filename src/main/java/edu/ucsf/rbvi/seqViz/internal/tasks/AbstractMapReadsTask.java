package edu.ucsf.rbvi.seqViz.internal.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.util.HashMap;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;

public abstract class AbstractMapReadsTask extends AbstractTask implements ObservableTask {

	protected ContigsManager contigManager;
	
	/**
	 * AbstractMaperReaders is an abstract class that serves as an interface to map reads to
	 * contigs using any mapper. The constructor takes in parameters required for mapping
	 * the reads to the contigs, including the file which contains the contigs, the reads,
	 * temporary folders to store the index, and other parameters.
	 * 
	 * @param contigs ContigManager that stores the mapping results.
	 */
	public AbstractMapReadsTask(ContigsManager contigs) {
		this.contigManager = contigs;
	}
}
