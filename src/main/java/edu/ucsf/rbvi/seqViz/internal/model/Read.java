package edu.ucsf.rbvi.seqViz.internal.model;

public class Read {
	
	private boolean pair; // mate-pair indicator
	private int length; // length of read
	private long name; // name of read
//	private String sequence; // nucleotide sequence of read

	/**
 	 * This constructor the read object, which stores information about the read.
 	 *
 	 * @param name name of the read
 	 * @param pair mate-pair number of the read (mate #1 or #2)
 	 * @param length length of the read
 	 * @param sequence nucleotide sequence of the read
 	 */
	public Read(long name, boolean pair, int length, String sequence) {
		this.name = name;
		this.pair = pair;
		this.length = length;
	//	this.sequence = sequence;
	}
	
	/**
	 * Return the name of the read
	 * 
	 * @return name of the read
	 */
	public long name() {return name;}
	
	/**
	 * Return the mate-pair information of read
	 * 
	 * @return false if mate-pair #1, true if mate-pair #2
	 */
	public boolean pair() {return pair;}
	
	/**
	 * Returns the length of the read
	 * 
	 * @return length of the read
	 */
	public int length() {return length;}
	
	/**
	 * Returns the nucleotide sequence of the read
	 * 
	 * @return nucleotide sequence of the read
	 */
//	public String sequence() {return sequence;}
}
