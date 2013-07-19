package edu.ucsf.rbvi.seqViz.internal.model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskMonitor;
import org.osgi.framework.BundleContext;

import edu.ucsf.rbvi.seqViz.internal.CyActivator;

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
	private BundleContext bundleContext;
	private VisualStyle vs;
	private CyNetworkView networkView = null;
	
	public ContigsManager(BundleContext bc, VisualStyle vs) {
		contigs = new HashMap<String, Contig>();
		readPairs = new HashMap<String, ReadPair>();
		bundleContext = bc;
		this.vs = vs;
		CyNetworkFactory networkFactory = (CyNetworkFactory) getService(CyNetworkFactory.class);
		this.network = networkFactory.createNetwork();
		settings = new SeqVizSettings();
	}

	public ContigsManager(BundleContext bc, CyNetwork curNetwork, VisualStyle vs) {
		contigs = new HashMap<String, Contig>();
		readPairs = new HashMap<String, ReadPair>();
		bundleContext = bc;
		this.vs = vs;
		this.network = curNetwork;
		settings = new SeqVizSettings();
	}

	/**
	 * Initialize the settings for ContigsManager
	 * 
	 * @param settings A SeqVizSettings class containing the parameters necessary to run
	 * 					ContigsManager
	 */
	public void initializeSettings(SeqVizSettings settings) {
		this.settings = settings;
	}
	
	/**
	 * Check if SeqVizSettings is initialized (settings != null)
	 * 
	 * @return
	 */
	public boolean isInitialized() {
		if (settings != null) return true;
		else return false;
	}
	
	/**
	 * Check if SeqVizSettings contains initialized parameters for running mapper
	 * 
	 * @return
	 */
	public boolean mapperSettingsIntiialized() {
		return settings.mapReads != null && settings.mapper_dir != null && settings.temp_dir != null && settings.threads != 0;
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
	 * @param sameContig boolean stating whether the mate-pair maps to the same contig
	 * @throws Exception throws an exception if contigName matches a name of an existing contig
	 * 					already in ContigsManager
	 */
	public void addRead(String contigName, Read read, int score, int locus, boolean strand, boolean sameContig) throws Exception {
		Contig contig = contigs.get(contigName);
		if (contig == null) throw new Exception("Cannot add new read, contig " + contigName + " does not exist.");
		contig.addRead(read, score, locus, strand, sameContig);
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
	
	public Contig getContig(String name) {
		return contigs.get(name);
	}
	/**
	 * Function for loading contigs into the network.
	 */
	public void displayContigs() {
		network.getRow(network).set(CyNetwork.NAME, settings.contigs.getName());
		CyTable table = network.getDefaultNodeTable();
		if (table.getColumn("length") == null)
			table.createColumn("length", Long.class, false);
		for (String s: contigs.keySet()) {
			if (contigs.get(s).node == null) {
				CyNode node = network.addNode();
				contigs.get(s).node = node;
				network.getRow(node).set(CyNetwork.NAME, s);
				network.getRow(node).set("length", (long) contigs.get(s).sequence().length());
			}
		}
	}
	
	/**
	 * Function for loading edges (mate-paired reads which connects contigs) into the network
	 */
	public void displayBridgingReads() {
		HashMap<String, CyEdge>	edgeNamesPlusPlus = new HashMap<String, CyEdge>(),
								edgeNamesPlusMinus = new HashMap<String, CyEdge>(),
								edgeNamesMinusPlus = new HashMap<String, CyEdge>(),
								edgeNamesMinusMinus = new HashMap<String, CyEdge>();
		HashMap<String, Double>	edgeWeightPlusPlus = new HashMap<String, Double>(),
								edgeWeightPlusMinus = new HashMap<String, Double>(),
								edgeWeightMinusPlus = new HashMap<String, Double>(),
								edgeWeightMinusMinus = new HashMap<String, Double>();
		HashMap<String, Long>	edgeCountPlusPlus = new HashMap<String, Long>(),
								edgeCountPlusMinus = new HashMap<String, Long>(),
								edgeCountMinusPlus = new HashMap<String, Long>(),
								edgeCountMinusMinus = new HashMap<String, Long>();
		for (ReadPair p: readPairs.values()) {
			if (p.getMate1Contigs() != null && p.getMate2Contigs() != null) {
				double weight = p.weight();
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
								if (! readInfo1.sameContig() && ! readInfo2.sameContig()) {
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
											edgeWeightPlusPlus.put(edgeName, edgeWeightPlusPlus.get(edgeName) + weight);
										else edgeWeightPlusPlus.put(edgeName, weight);
										if (edgeCountPlusPlus.containsKey(edgeName))
											edgeCountPlusPlus.put(edgeName, edgeCountPlusPlus.get(edgeName) + 1);
										else edgeCountPlusPlus.put(edgeName, (long) 1);
									}
									else if (read1Orientation && ! read2Orientation) {
										if (edgeNamesPlusMinus.containsKey(edgeName))
											thisEdge = edgeNamesPlusMinus.get(edgeName);
										else {
											thisEdge = network.addEdge(node1, node2, true);
											edgeNamesPlusMinus.put(edgeName, thisEdge);
										}
										if (edgeWeightPlusMinus.containsKey(edgeName))
											edgeWeightPlusMinus.put(edgeName, edgeWeightPlusMinus.get(edgeName) + weight);
										else edgeWeightPlusMinus.put(edgeName, weight);
										if (edgeCountPlusMinus.containsKey(edgeName))
											edgeCountPlusMinus.put(edgeName, edgeCountPlusMinus.get(edgeName) + 1);
										else edgeCountPlusMinus.put(edgeName, (long) 1);
									}
									else if (! read1Orientation && read2Orientation) {
										if (edgeNamesMinusPlus.containsKey(edgeName))
											thisEdge = edgeNamesMinusPlus.get(edgeName);
										else {
											thisEdge = network.addEdge(node1, node2, true);
											edgeNamesMinusPlus.put(edgeName, thisEdge);
										}
										if (edgeWeightMinusPlus.containsKey(edgeName))
											edgeWeightMinusPlus.put(edgeName, edgeWeightMinusPlus.get(edgeName) + weight);
										else edgeWeightMinusPlus.put(edgeName, weight);
										if (edgeCountMinusPlus.containsKey(edgeName))
											edgeCountMinusPlus.put(edgeName, edgeCountMinusPlus.get(edgeName) + 1);
										else edgeCountMinusPlus.put(edgeName, (long) 1);
									}
									else if (! read1Orientation && ! read2Orientation) {
										if (edgeNamesMinusMinus.containsKey(edgeName))
											thisEdge = edgeNamesMinusMinus.get(edgeName);
										else {
											thisEdge = network.addEdge(node1, node2, true);
											edgeNamesMinusMinus.put(edgeName, thisEdge);
										}
										if (edgeWeightMinusMinus.containsKey(edgeName))
											edgeWeightMinusMinus.put(edgeName, edgeWeightMinusMinus.get(edgeName) + weight);
										else edgeWeightMinusMinus.put(edgeName, weight);
										if (edgeCountMinusMinus.containsKey(edgeName))
											edgeCountMinusMinus.put(edgeName, edgeCountMinusMinus.get(edgeName) + 1);
										else edgeCountMinusMinus.put(edgeName, (long) 1);
									}
								}
							}
					}
				}
			}
		}
		CyTable table = network.getDefaultEdgeTable();
		if (table.getColumn("weight") == null)
			table.createColumn("weight", Double.class, false);
		if (table.getColumn("orientation") == null)
			table.createColumn("orientation", String.class, false);
		if (table.getColumn("reliability") == null)
			table.createColumn("reliability", Double.class, false);
		for (String s: edgeNamesPlusPlus.keySet()) {
			table.getRow(edgeNamesPlusPlus.get(s).getSUID()).set(CyNetwork.NAME, s);
			table.getRow(edgeNamesPlusPlus.get(s).getSUID()).set("weight", edgeWeightPlusPlus.get(s));
			table.getRow(edgeNamesPlusPlus.get(s).getSUID()).set("orientation", "plusplus");
			table.getRow(edgeNamesPlusPlus.get(s).getSUID()).set("reliability", edgeWeightPlusPlus.get(s) / edgeCountPlusPlus.get(s));
		}
		for (String s: edgeNamesPlusMinus.keySet()) {
			table.getRow(edgeNamesPlusMinus.get(s).getSUID()).set(CyNetwork.NAME, s);
			table.getRow(edgeNamesPlusMinus.get(s).getSUID()).set("weight", edgeWeightPlusMinus.get(s));
			table.getRow(edgeNamesPlusMinus.get(s).getSUID()).set("orientation", "plusminus");
			table.getRow(edgeNamesPlusMinus.get(s).getSUID()).set("reliability", edgeWeightPlusMinus.get(s) / edgeCountPlusMinus.get(s));
		}
		for (String s: edgeNamesMinusPlus.keySet()) {
			table.getRow(edgeNamesMinusPlus.get(s).getSUID()).set(CyNetwork.NAME, s);
			table.getRow(edgeNamesMinusPlus.get(s).getSUID()).set("weight", edgeWeightMinusPlus.get(s));
			table.getRow(edgeNamesMinusPlus.get(s).getSUID()).set("orientation", "minusplus");
			table.getRow(edgeNamesMinusPlus.get(s).getSUID()).set("reliability", edgeWeightMinusPlus.get(s) / edgeCountMinusPlus.get(s));
		}
		for (String s: edgeNamesMinusMinus.keySet()) {
			table.getRow(edgeNamesMinusMinus.get(s).getSUID()).set(CyNetwork.NAME, s);
			table.getRow(edgeNamesMinusMinus.get(s).getSUID()).set("weight", edgeWeightMinusMinus.get(s));
			table.getRow(edgeNamesMinusMinus.get(s).getSUID()).set("orientation", "minusminus");
			table.getRow(edgeNamesMinusMinus.get(s).getSUID()).set("reliability", edgeWeightMinusMinus.get(s) / edgeCountMinusMinus.get(s));
		}
		// Calculate log of weight
		if (table.getColumn("weight_log") == null)
			table.createColumn("weight_log", Double.class, false);
		for (Long cyId: table.getPrimaryKey().getValues(Long.class))
			table.getRow(cyId).set("weight_log", Math.log(table.getRow(cyId).get("weight", Double.class)));
	}
	
	/**
	 * Function for creating histogram from mapped reads
	 * @param binSize size of the bins for the histogram
	 */
	public void createHist(int binSize) {
		CyTable table = network.getDefaultNodeTable();
		if (table.getColumn("paired_end_hist") == null)
			table.createListColumn("paired_end_hist", Double.class, false);
		if (table.getColumn("paired_end_hist_rev") == null)
			table.createListColumn("paired_end_hist_rev", Double.class, false);
		if (table.getColumn("read_cov_hist") == null)
			table.createListColumn("read_cov_hist", Double.class, false);
		if (table.getColumn("read_cov_hist_pos") == null)
			table.createListColumn("read_cov_hist_pos", Double.class, false);
		if (table.getColumn("read_cov_hist_rev") == null)
			table.createListColumn("read_cov_hist_rev", Double.class, false);

		if (table.getColumn("paired_end_hist_log") == null)
			table.createListColumn("paired_end_hist_log", Double.class, false);
		if (table.getColumn("paired_end_hist_rev_log") == null)
			table.createListColumn("paired_end_hist_rev_log", Double.class, false);
		if (table.getColumn("read_cov_hist_log") == null)
			table.createListColumn("read_cov_hist_log", Double.class, false);
		if (table.getColumn("read_cov_hist_pos_log") == null)
			table.createListColumn("read_cov_hist_pos_log", Double.class, false);
		if (table.getColumn("read_cov_hist_rev_log") == null)
			table.createListColumn("read_cov_hist_rev_log", Double.class, false);
		
		if (table.getColumn("barchart_paired_end_hist") == null)
			table.createColumn("barchart_paired_end_hist", String.class, false);
		if (table.getColumn("barchart_paired_end_rev_hist") == null)
			table.createColumn("barchart_paired_end_rev_hist", String.class, false);
		if (table.getColumn("bartchart_read_cov_hist") == null)
			table.createColumn("bartchart_read_cov_hist", String.class, false);
		if (table.getColumn("bartchart_read_cov_rev_hist") == null)
			table.createColumn("bartchart_read_cov_rev_hist", String.class, false);

		double paired_end_min = 0, paired_end_max = 0, read_cov_max = 0, temp, read_cov_pos_max = 0, read_cov_rev_max = 0;
		for (String s: contigs.keySet()) {
			double [] paired_end_hist = new double[(contigs.get(s).sequence().length() / binSize) + 1],
					paired_end_hist_rev = new double[(contigs.get(s).sequence().length() / binSize) + 1],
					read_cov_hist = new double[(contigs.get(s).sequence().length() / binSize) + 1],
					read_cov_hist_pos = new double[(contigs.get(s).sequence().length() / binSize) + 1],
					read_cov_hist_rev = new double[(contigs.get(s).sequence().length() / binSize) + 1];
			for (ReadMappingInfo readInfo: contigs.get(s).allReads()) {
				read_cov_hist[readInfo.locus() / binSize] += (double) readInfo.read().length() / (double) binSize;
				if (readInfo.strand())
					read_cov_hist_pos[readInfo.locus() / binSize] += (double) readInfo.read().length() / (double) binSize;
				else
					read_cov_hist_rev[readInfo.locus() / binSize] -= (double) readInfo.read().length() / (double) binSize;
				if (! readInfo.sameContig()) {
					if (readInfo.strand())
						paired_end_hist[readInfo.locus() / binSize] += (double) readInfo.read().length() / (double) binSize;
					else
						paired_end_hist_rev[readInfo.locus() / binSize] -= (double) readInfo.read().length() / (double) binSize;
				}
			}
			ArrayList<Double>	a = new ArrayList<Double>(),
								b = new ArrayList<Double>(),
								c = new ArrayList<Double>(),
								d = new ArrayList<Double>(),
								e = new ArrayList<Double>(),
								f = new ArrayList<Double>(),
								g = new ArrayList<Double>(),
								h = new ArrayList<Double>(),
								j = new ArrayList<Double>(),
								k = new ArrayList<Double>();
			for (int i = 0; i < paired_end_hist.length; i++) {
				a.add(paired_end_hist[i]);
			/*	if (paired_end_hist[i] == 0)
					d.add(temp = 0.0);
				else
					d.add(temp = Math.log(paired_end_hist[i]) + 1); */
				d.add(temp = Math.log(paired_end_hist[i] + 1));
				if (temp > paired_end_max)
					paired_end_max = temp;
			}
			table.getRow(contigs.get(s).node.getSUID()).set("paired_end_hist", a);
			table.getRow(contigs.get(s).node.getSUID()).set("paired_end_hist_log", d);
			for (int i = 0; i < paired_end_hist_rev.length; i++) {
				b.add(paired_end_hist_rev[i]);
			/*	if (paired_end_hist_rev[i] == 0)
					e.add(temp = 0.0);
				else
					e.add(temp = - Math.log(- paired_end_hist_rev[i]) - 1); */
				e.add(temp = - Math.log(- paired_end_hist_rev[i] + 1));
				if (temp < paired_end_min)
					paired_end_min = temp;
			}
			table.getRow(contigs.get(s).node.getSUID()).set("paired_end_hist_rev", b);
			table.getRow(contigs.get(s).node.getSUID()).set("paired_end_hist_rev_log", e);
			for (int i = 0; i < read_cov_hist.length; i++) {
				c.add(read_cov_hist[i]);
			/*	if (read_cov_hist[i] == 0)
					f.add(temp = 0.0);
				else
					f.add(temp = Math.log(read_cov_hist[i]) + 1); */
				f.add(temp = Math.log(read_cov_hist[i] + 1));
				if (temp > read_cov_max)
					read_cov_max = temp;
			}
			table.getRow(contigs.get(s).node.getSUID()).set("read_cov_hist", c);
			table.getRow(contigs.get(s).node.getSUID()).set("read_cov_hist_log", f);
			for (int i = 0; i < read_cov_hist_pos.length; i++) {
				g.add(read_cov_hist_pos[i]);
				h.add(temp = Math.log(read_cov_hist_pos[i] + 1));
				if (temp > read_cov_pos_max)
					read_cov_pos_max = temp;
			}
			table.getRow(contigs.get(s).node.getSUID()).set("read_cov_hist_pos", g);
			table.getRow(contigs.get(s).node.getSUID()).set("read_cov_hist_pos_log", h);
			for (int i = 0; i < read_cov_hist_rev.length; i++) {
				j.add(read_cov_hist_rev[i]);
				k.add(temp = - Math.log(- read_cov_hist_rev[i] + 1));
				if (temp < read_cov_rev_max)
					read_cov_rev_max = temp;
			}
			table.getRow(contigs.get(s).node.getSUID()).set("read_cov_hist_rev", j);
			table.getRow(contigs.get(s).node.getSUID()).set("read_cov_hist_rev_log", k);
		}
		for (String s: contigs.keySet()) {
			table.getRow(contigs.get(s).node.getSUID()).set("barchart_paired_end_hist", "barchart: attributelist=\"paired_end_hist_log\" showlabels=\"false\" colorlist=\"up:blue,down:yellow,zero:black\" range=\"" + paired_end_min + "," + paired_end_max + "\"");
			table.getRow(contigs.get(s).node.getSUID()).set("barchart_paired_end_rev_hist", "barchart: attributelist=\"paired_end_hist_rev_log\" showlabels=\"false\" colorlist=\"up:blue,down:yellow,zero:black\" range=\"" + paired_end_min + "," + paired_end_max + "\"");
			table.getRow(contigs.get(s).node.getSUID()).set("bartchart_read_cov_hist", "barchart: attributelist=\"read_cov_hist_pos_log\" showlabels=\"false\" colorlist=\"up:blue,down:yellow,zero:black\" range=\"" + read_cov_rev_max + "," + read_cov_pos_max + "\"");
			table.getRow(contigs.get(s).node.getSUID()).set("bartchart_read_cov_rev_hist", "barchart: attributelist=\"read_cov_hist_rev_log\" showlabels=\"false\" colorlist=\"up:blue,down:yellow,zero:black\" range=\"" + read_cov_rev_max + "," + read_cov_pos_max + "\"");
		}
	}
	
	public ComplementaryGraphs createBpGraph(String contigName) {
		ComplementaryGraphs y = new ComplementaryGraphs();
		for (ReadMappingInfo read: contigs.get(contigName).allReads()) {
			ReadPair pair = readPairs.get(read.read().name());
			Set<String> pairContigs;
			if (read.read().pair())
				pairContigs = pair.getMate2Contigs();
			else
				pairContigs = pair.getMate1Contigs();
			if (pairContigs == null) {
				pairContigs = new HashSet<String>();
				pairContigs.add(null);
			}
			for (String c: pairContigs) {
				HashMap<String, long[]> covGraphs;
				if (read.strand()) covGraphs = y.pos;
				else covGraphs = y.rev;
				long[] covGraph;
				if (covGraphs.containsKey(c))
					covGraph = covGraphs.get(c);
				else {
					covGraph = new long[contigs.get(contigName).sequence().length()];
					covGraphs.put(c, covGraph);
				}
				for (int i = 0; i < read.read().length(); i++)
					if (i + read.locus() -1 < covGraph.length)
						covGraph[i + read.locus() -1] += 1;
			}
		}
		return y;
	}

	public ComplementaryGraphs createBpGraph(String contigName, int binSize) {
		ComplementaryGraphs y = new ComplementaryGraphs();
		for (ReadMappingInfo read: contigs.get(contigName).allReads()) {
			ReadPair pair = readPairs.get(read.read().name());
			Set<String> pairContigs;
			if (read.read().pair())
				pairContigs = pair.getMate2Contigs();
			else
				pairContigs = pair.getMate1Contigs();
			if (pairContigs == null) {
				pairContigs = new HashSet<String>();
				pairContigs.add(null);
			}
			for (String c: pairContigs) {
				HashMap<String, long[]> covGraphs;
				if (read.strand()) covGraphs = y.pos;
				else covGraphs = y.rev;
				long[] covGraph;
				if (covGraphs.containsKey(c))
					covGraph = covGraphs.get(c);
				else {
					covGraph = new long[(contigs.get(contigName).sequence().length() / binSize) + 1];
					covGraphs.put(c, covGraph);
				}
				covGraph[(read.locus() - 1) / binSize] += read.read().length();
			}
		}
		return y;
	}

	public void loadBpGraphs() {
		CyTable table = network.getDefaultNetworkTable();
		for (String s: contigs.keySet()) {
			ArrayList<String> colNames = new ArrayList<String>();
			ComplementaryGraphs graphs = createBpGraph(s);
			String colName;
			for (String contigName: graphs.pos.keySet()) {
				colName = s + ":" + (contigName != null ? contigName : "unpaired") + ":" + "pos";
				if (table.getColumn(colName) == null)
					table.createListColumn(colName, Long.class, false);
				List<Long> newList = new ArrayList<Long>();
				long[] temp = graphs.pos.get(contigName);
				for (int i = 0; i < temp.length; i++)
					newList.add(temp[i]);
				table.getRow(network.getSUID()).set(colName, newList);
				colNames.add(colName);
			}
			for (String contigName: graphs.rev.keySet()) {
				colName = s + ":" + (contigName != null ? contigName : "unpaired") + ":" + "rev";
				if (table.getColumn(colName) == null)
					table.createListColumn(colName, Long.class, false);
				List<Long> newList = new ArrayList<Long>();
				long[] temp = graphs.rev.get(contigName);
				for (int i = 0; i < temp.length; i++)
					newList.add(-temp[i]);
				table.getRow(network.getSUID()).set(colName, newList);
				colNames.add(colName);
			}
			if (table.getColumn(s + ":graphColumns") == null)
				table.createListColumn(s + ":graphColumns", String.class, false);
			table.getRow(network.getSUID()).set(s + ":graphColumns", colNames);
		}
	}
	
	public void loadBpGraphs(int binSize) {
		CyTable table = network.getDefaultNetworkTable();
		for (String s: contigs.keySet()) {
			ArrayList<String> colNames = new ArrayList<String>();
			ComplementaryGraphs graphs = createBpGraph(s, binSize);
			String colName;
			for (String contigName: graphs.pos.keySet()) {
				colName = s + ":" + (contigName != null ? contigName : "unpaired") + ":" + "pos";
				if (table.getColumn(colName) == null)
					table.createListColumn(colName, Long.class, false);
				List<Long> newList = new ArrayList<Long>();
				long[] temp = graphs.pos.get(contigName);
				for (int i = 0; i < temp.length; i++)
					newList.add(temp[i] / binSize);
				table.getRow(network.getSUID()).set(colName, newList);
				colNames.add(colName);
			}
			for (String contigName: graphs.rev.keySet()) {
				colName = s + ":" + (contigName != null ? contigName : "unpaired") + ":" + "rev";
				if (table.getColumn(colName) == null)
					table.createListColumn(colName, Long.class, false);
				List<Long> newList = new ArrayList<Long>();
				long[] temp = graphs.rev.get(contigName);
				for (int i = 0; i < temp.length; i++)
					newList.add(-temp[i] / binSize);
				table.getRow(network.getSUID()).set(colName, newList);
				colNames.add(colName);
			}
			if (table.getColumn(s + ":graphColumns") == null)
				table.createListColumn(s + ":graphColumns", String.class, false);
			table.getRow(network.getSUID()).set(s + ":graphColumns", colNames);
		}
		if (table.getColumn("graphBinSize") == null)
			table.createColumn("graphBinSize", Long.class, false);
		table.getRow(network.getSUID()).set("graphBinSize", (long) binSize);
	}

	public void displayNetwork() {
		CyNetworkManager networkManager = (CyNetworkManager) getService(CyNetworkManager.class);
		networkManager.addNetwork(network);
		
		CyNetworkViewFactory networkViewFactory = (CyNetworkViewFactory) getService(CyNetworkViewFactory.class);
		CyNetworkView myView = networkViewFactory.createNetworkView(network);
		CyNetworkViewManager networkViewManager = (CyNetworkViewManager) getService(CyNetworkViewManager.class);
		networkViewManager.addNetworkView(myView);
		
		networkView = myView;
		if (vs != null)
			vs.apply(myView);
		myView.updateView();
	}
	
	public void applyStyle(VisualStyle vs) {
		if (networkView != null) {
			if (vs != null)
				vs.apply(networkView);
			networkView.updateView();
		}
	}
	
	private Object getService(Class<?> serviceClass) {
		return bundleContext.getService(bundleContext.getServiceReference(serviceClass.getName()));
	}
}
