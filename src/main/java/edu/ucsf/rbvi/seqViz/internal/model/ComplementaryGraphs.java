package edu.ucsf.rbvi.seqViz.internal.model;

import java.util.HashMap;

/**
 * Helper class to store coverage graphs.
 * 
 * @author aywu
 *
 */
public class ComplementaryGraphs {
	// Graphs of reads mapping in the positive orientation.
	public HashMap<String, long[]> pos;
	// Graphs of reads mapping in the reverse orientation.
	public HashMap<String, long[]> rev;
	public ComplementaryGraphs() {
		pos = new HashMap<String, long[]>();
		rev = new HashMap<String, long[]>();
	}
}
