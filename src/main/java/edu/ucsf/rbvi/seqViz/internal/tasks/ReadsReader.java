package edu.ucsf.rbvi.seqViz.internal.tasks;

import java.io.InputStream;

import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;

/**
 * ReadersReader is an interface that takes an results of mapping reads to contigs and stores
 * them in a ContigManager.
 * 
 * @author aywu
 *
 */
public abstract class ReadsReader {
	
	protected ContigsManager contigs;
	
	public ReadsReader(ContigsManager contigs) {
		this.contigs = contigs;
	}
	/**
	 * Abstract method for parsing the output of a mapper and stores the mapping results in
	 * a ContigManager.
	 * 
	 * @param stream Output of the mapper, in the form of an InputStream 
	 * @param contigs A ContigsManager to store the data obtained from InputStream stream
	 * @param arg0 helps report to the user the progress of mapping
	 * @param reads expected number of reads to be mapped
	 * @throws Exception Throws exception if the contigs do not exist in ContigsManager, or
	 * 							an error reading stream.
	 */
	public abstract void readReads(InputStream stream, TaskMonitor arg0, int reads) throws Exception;
}
