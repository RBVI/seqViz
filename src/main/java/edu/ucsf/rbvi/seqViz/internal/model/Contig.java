package edu.ucsf.rbvi.seqViz.internal.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The Contig class contains information about each contig such as sequence and potentially other
 * information, along with the reads that map to it.
 * 
 * @author aywu
 *
 */
public class Contig {
	private String sequence;
	private List<ReadMappingInfo> reads;
	
	/**
	 * Contig contains the sequence of the contig, along with the
	 * reads that map onto this contig
	 * 
	 * @param sequence nucleotide sequence of the contig
	 */
	public Contig(String sequence) {
		this.sequence = sequence;
		this.reads = new ArrayList<ReadMappingInfo>();
	}
	
	/**
	 * Add a ReadMappingInfo to the Contig.
	 * 
	 * @param read Read to be added
	 * @param score alignment score of the read to the contig
	 * @param locus left-most position the read maps with respect to the contig
	 * @param strand orientation the read maps to the contig ('true' for +, 'false' for -)
	 */
	public void addRead(Read read, int score, int locus, boolean strand) {
		reads.add(new ReadMappingInfo(read, score, locus, strand));
	}
	
	/**
	 * Add a ReadMappingInfo to the Contig.
	 * 
	 * @param readsInfo ReadMappingInfo to be added to the contig.
	 */
	public void addReadMappingInfo(ReadMappingInfo readsInfo) {
		reads.add(readsInfo);
	}
}
