package edu.ucsf.rbvi.seqViz.internal.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
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
	
	private HashMap<String, ReadPair> readPairs;
	private HashMap<String,Contig> contigs;
	private SeqVizSettings settings;
	private CyNetwork network;
	
	public ContigsManager(CyNetwork network) {
		contigs = new HashMap<String, Contig>();
		readPairs = new HashMap<String, ReadPair>();
		this.network = network;
		settings = null;
	}
	
	public void initializeSettings(SeqVizSettings settings) {
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
		ReadPair thisPair;
		if (readPairs.containsKey(readInfo.read().name()))
			thisPair = readPairs.get(readInfo.read().name());
		else {
			thisPair = new ReadPair();
			readPairs.put(readInfo.read().name(), thisPair);
		}
		thisPair.addReadMappingInfo(contigName, readInfo);
	}
	
	public void displayContigs() {
		network.getRow(network).set(CyNetwork.NAME, settings.contigs.getName());
		for (String s: contigs.keySet()) {
			if (contigs.get(s).node == null) {
				CyNode node = network.addNode();
				contigs.get(s).node = node;
				network.getRow(node).set(CyNetwork.NAME, s);
			}
		}
	}
	
	public void displayBridgingReads() {
		CyTable edges = network.getDefaultEdgeTable();
		HashMap<String, CyEdge>	edgeNamesPlusPlus = new HashMap<String, CyEdge>(),
								edgeNamesPlusMinus = new HashMap<String, CyEdge>(),
								edgeNamesMinusPlus = new HashMap<String, CyEdge>(),
								edgeNamesMinusMinus = new HashMap<String, CyEdge>();
		HashMap<String, Float>	edgeWeightPlusPlus = new HashMap<String, Float>(),
								edgeWeightPlusMinus = new HashMap<String, Float>(),
								edgeWeightMinusPlus = new HashMap<String, Float>(),
								edgeWeightMinusMinus = new HashMap<String, Float>();
		if (edges.getColumn("weight") != null)
			edges.createColumn("weight", Float.class, false);
		if (edges.getColumn("orientation") != null)
			edges.createColumn("orientation", String.class, false);
		for (ReadPair p: readPairs.values()) {
			for (String contig1: p.getMate1Contigs())
				for (String contig2: p.getMate2Contigs()) {
					if (! contig1.equals(contig2)) {
						String thisContig1, thisContig2;
						CyNode n1 = contigs.get(contig1).node,
								n2 = contigs.get(contig2).node,
								node1, node2;
						List<ReadMappingInfo> contig1Reads, contig2Reads;
						if (n1.getSUID() < n2.getSUID()) {
							thisContig1 = contig1;
							thisContig2 = contig2;
							node1 = n1;
							node2 = n2;
							contig1Reads = p.getReadMappingInfoMate1(contig1);
							contig2Reads = p.getReadMappingInfoMate2(contig2);
						}
						else {
							thisContig1 = contig2;
							thisContig2 = contig1;
							node1 = n2;
							node2 = n1;
							contig2Reads = p.getReadMappingInfoMate1(contig1);
							contig1Reads = p.getReadMappingInfoMate2(contig2);
						}
						for (ReadMappingInfo readInfo1: contig1Reads)
							for (ReadMappingInfo readInfo2: contig2Reads) {
								CyEdge thisEdge;
								String edgeName = thisContig1 + thisContig2;
								boolean read1Orientation = readInfo1.strand(),
										read2Orientation = readInfo2.strand();
								if (read1Orientation && read2Orientation) {
									if (edgeNamesPlusPlus.containsKey(edgeName))
										thisEdge = edgeNamesPlusPlus.get(edgeName);
									else {
										thisEdge = network.addEdge(node1, node2, true);
										edgeNamesPlusPlus.put(edgeName, thisEdge);
									}
									if (edgeWeightPlusPlus.containsKey(edgeName))
										edgeWeightPlusPlus.put(edgeName, edgeWeightPlusPlus.get(edgeName) + 1);
									else edgeWeightPlusPlus.put(edgeName, new Float(1.0));
								}
								else if (read1Orientation && ! read2Orientation) {
									if (edgeNamesPlusMinus.containsKey(edgeName))
										thisEdge = edgeNamesPlusMinus.get(edgeName);
									else {
										thisEdge = network.addEdge(node1, node2, true);
										edgeNamesPlusMinus.put(edgeName, thisEdge);
									}
									if (edgeWeightPlusMinus.containsKey(edgeName))
										edgeWeightPlusMinus.put(edgeName, edgeWeightPlusMinus.get(edgeName) + 1);
									else edgeWeightPlusMinus.put(edgeName, new Float(1.0));
								}
								else if (! read1Orientation && read2Orientation) {
									if (edgeNamesMinusPlus.containsKey(edgeName))
										thisEdge = edgeNamesMinusPlus.get(edgeName);
									else {
										thisEdge = network.addEdge(node1, node2, true);
										edgeNamesMinusPlus.put(edgeName, thisEdge);
									}
									if (edgeWeightMinusPlus.containsKey(edgeName))
										edgeWeightMinusPlus.put(edgeName, edgeWeightMinusPlus.get(edgeName) + 1);
									else edgeWeightMinusPlus.put(edgeName, new Float(1.0));
								}
								else if (! read1Orientation && ! read2Orientation) {
									if (edgeNamesMinusMinus.containsKey(edgeName))
										thisEdge = edgeNamesMinusMinus.get(edgeName);
									else {
										thisEdge = network.addEdge(node1, node2, true);
										edgeNamesMinusMinus.put(edgeName, thisEdge);
									}
									if (edgeWeightMinusMinus.containsKey(edgeName))
										edgeWeightMinusMinus.put(edgeName, edgeWeightMinusMinus.get(edgeName) + 1);
									else edgeWeightMinusMinus.put(edgeName, new Float(1.0));
								}
							}
					}
				}
		}
		CyTable table = network.getDefaultEdgeTable();
		for (String s: edgeNamesPlusPlus.keySet()) {
			table.getRow(edgeNamesPlusPlus.get(s)).set("weight", edgeWeightPlusPlus.get(s));
			table.getRow(edgeNamesPlusPlus.get(s)).set("orientation", "plusplus");
		}
		for (String s: edgeNamesPlusMinus.keySet()) {
			table.getRow(edgeNamesPlusMinus.get(s)).set("weight", edgeWeightPlusPlus.get(s));
			table.getRow(edgeNamesPlusMinus.get(s)).set("orientation", "plusminus");			
		}
		for (String s: edgeNamesMinusPlus.keySet()) {
			table.getRow(edgeNamesMinusPlus.get(s)).set("weight", edgeWeightPlusPlus.get(s));
			table.getRow(edgeNamesMinusPlus.get(s)).set("orientation", "minusplus");
		}
		for (String s: edgeNamesMinusMinus.keySet()) {
			table.getRow(edgeNamesMinusMinus.get(s)).set("weight", edgeWeightPlusPlus.get(s));
			table.getRow(edgeNamesMinusMinus.get(s)).set("orientation", "minusminus");
		}
	}
}
