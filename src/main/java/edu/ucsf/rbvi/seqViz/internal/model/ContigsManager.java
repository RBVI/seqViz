package edu.ucsf.rbvi.seqViz.internal.model;

import java.util.HashMap;

import org.cytoscape.work.TaskMonitor;

/**
 * ContigsManager is a container for Contig(s), and some other information that allows construction
 * of a scaffold. ContigsManager associates each contig with a name. It is the main interface by
 * which Cytoscape task will store and manipulate contig and scaffolding information.
 * 
 * @author aywu
 *
 */
public class ContigsManager {
	
	private HashMap<String,Contig> contigs;
	private SeqVizSettings settings;
	
	public ContigsManager() {
		contigs = new HashMap<String, Contig>();
		settings = null;
	}
	
	public void addSettings(SeqVizSettings settings) {
		this.settings = settings;
	}
	
	public boolean isInitialized() {
		if (settings != null) return true;
		else return false;
	}
	
	public SeqVizSettings getSettings() {return settings;}
	/**
	 * Adds a Contig to the ContigManager.
	 * 
	 * @param name Name of the new contig
	 * @param contig A Contig class containing information about the contig
	 * @throws Exception throws error if the contig already exists.
	 */
	public void addContig(String name, Contig contig) throws Exception {
		if (contigs.containsKey(name)) throw new Exception("Cannot add contig, contig with same name already exists.");
		contigs.put(name, contig);
	}
	
	/**
	 * Add a ReadMappingInfo to the Contig
	 * 
	 * @param contigName name of new contig
	 * @param read Read to be added
	 * @param score alignment score of the read to the contig
	 * @param locus left-most position the read maps with respect to the contig
	 * @param strand orientation the read maps to the contig ('true' for +, 'false' for -)
	 * @throws Exception throws an exception if contigName matches a name of an existing contig
	 * 					already in ContigsManager
	 */
	public void addRead(String contigName, Read read, int score, int locus, boolean strand) throws Exception {
		Contig contig = contigs.get(contigName);
		if (contig == null) throw new Exception("Cannot add new read, contig " + contigName + " does not exist.");
		contig.addRead(read, score, locus, strand);
	}
	/**
	 * Add a ReadMappingInfo to the Contig.
	 * 
	 * @param contigName name of new contig
	 * @param readsInfo ReadMappingInfo to be added to the contig.
	 * @throws Exception throws an exception if contigName matches a name of an existing contig
	 * 					already in ContigsManager
	 */
	public void addRead(String contigName, ReadMappingInfo readInfo) throws Exception {
		Contig contig = contigs.get(contigName);
		if (contig == null) throw new Exception("Cannot add new read, contig " + contigName + " does not exist.");
		contig.addReadMappingInfo(readInfo);
	}
}
