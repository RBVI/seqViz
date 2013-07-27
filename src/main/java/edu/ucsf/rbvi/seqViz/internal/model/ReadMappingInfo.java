package edu.ucsf.rbvi.seqViz.internal.model;

public class ReadMappingInfo {
	
	private Read read;
//	private int score;
	private int locus;
	private boolean strand;
	private boolean sameContig;
	
	/**
	 * ReadMappingInfo stores information gained when mapping reads to the reference sequence
	 * 
	 * @param read The read that was mapped to this reference sequence
	 * @param score The alignment score
	 * @param locus The position the read maps
	 * @param strand The orientation the read maps ('true' for +, 'false' for -)
	 * @param sameContig An argument indicating whether the mate-pair of the read maps to the same
	 * 			contig.
	 */
	public ReadMappingInfo(Read read, int score, int locus, boolean strand, boolean sameContig) {
		this.read = read;
//		this.score = score;
		this.locus = locus;
		this.strand = strand;
		this.sameContig = sameContig;
	}
	
	/**
	 * Returns the read and all the information associated with it
	 * 
	 * @return read as Read
	 */
	public Read read() {return read;}
	
	/**
	 * Returns the score of the alignment with the reference (in this case the contig).
	 * 
	 * @return alignment score of the read with the reference sequence
	 */
//	public int score() {return score;}
	
	/**
	 * Position of the mapped read on the reference sequence
	 * @return Left-most position of the reference sequence the read maps to
	 */
	public int locus() {return locus;}
	
	/**
	 * The strand the read maps to with respect to the reference sequence.
	 * @return 'true' if read maps to + strand, - if read maps to - strand.
	 */
	public boolean strand() {return strand;}
	
	/**
	 * Whether the mate-pair of the read maps to the same contig
	 * @return 'true' if on the same contig, 'false' if not
	 */
	public boolean sameContig() {return sameContig;}
}
