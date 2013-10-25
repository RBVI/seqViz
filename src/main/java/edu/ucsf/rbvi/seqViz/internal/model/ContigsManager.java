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
import org.cytoscape.service.util.CyServiceRegistrar;
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
	
	private HashMap<Long, ReadPair> readPairs;
	private List<ReadPair> readPairList;
	private HashMap<String,Contig> contigs;
	private SeqVizSettings settings;
	private CyNetwork network;
	private CyServiceRegistrar bundleContext;
	private VisualStyle vs;
	private CyNetworkView networkView = null;
	private int bin, binSize;
	private HashMap<String, ComplementaryGraphs> complementaryGraphs;
	private HashMap<String, CyEdge>	edgeNamesPlusPlus = new HashMap<String, CyEdge>(),
			edgeNamesPlusMinus = new HashMap<String, CyEdge>(),
			edgeNamesMinusPlus = new HashMap<String, CyEdge>(),
			edgeNamesMinusMinus = new HashMap<String, CyEdge>();
	private HashMap<String, Double>	edgeWeightPlusPlus = new HashMap<String, Double>(),
			edgeWeightPlusMinus = new HashMap<String, Double>(),
			edgeWeightMinusPlus = new HashMap<String, Double>(),
			edgeWeightMinusMinus = new HashMap<String, Double>();
	private HashMap<String, Long>	edgeCountPlusPlus = new HashMap<String, Long>(),
			edgeCountPlusMinus = new HashMap<String, Long>(),
			edgeCountMinusPlus = new HashMap<String, Long>(),
			edgeCountMinusMinus = new HashMap<String, Long>();
	private HashMap<String, double []> paired_end_hist, paired_end_hist_rev, read_cov_hist, read_cov_hist_pos, read_cov_hist_rev;
	private double paired_end_min = 0, paired_end_max = 0, read_cov_max = 0, temp, read_cov_pos_max = 0, read_cov_rev_max = 0;
	private HashMap<String, ComplementaryGraphs> bpGraphsAll, bpGraphsUnique, bpGraphsBest, bpGraphsBestUnique;
	private EdgeStat bridgingReadsAll, bridgingReadsUnique, bridgingReadsBest, bridgingReadsBestUnique;
	private Histograms histAll, histUnique, histBest, histBestUnique;

	public ContigsManager(CyServiceRegistrar bc, VisualStyle vs) {
		contigs = new HashMap<String, Contig>();
		readPairs = new HashMap<Long, ReadPair>();
		readPairList = new ArrayList<ReadPair>();
		bundleContext = bc;
		this.vs = vs;
		CyNetworkFactory networkFactory = bundleContext.getService(CyNetworkFactory.class);
		this.network = networkFactory.createNetwork();
		complementaryGraphs = new HashMap<String, ComplementaryGraphs>();
		paired_end_hist = new HashMap<String, double[]>();
		paired_end_hist_rev = new HashMap<String, double[]>();
		read_cov_hist = new HashMap<String, double[]>();
		read_cov_hist_pos = new HashMap<String, double[]>();
		read_cov_hist_rev = new HashMap<String, double[]>();
		
		bpGraphsAll = new HashMap<String, ComplementaryGraphs>();
		bpGraphsUnique = new HashMap<String, ComplementaryGraphs>();
		bpGraphsBest = new HashMap<String, ComplementaryGraphs>();
		bpGraphsBestUnique = new HashMap<String, ComplementaryGraphs>();
		
		bridgingReadsAll = new EdgeStat();
		bridgingReadsUnique = new EdgeStat();
		bridgingReadsBest = new EdgeStat();
		bridgingReadsBestUnique = new EdgeStat();
		
		histAll = new Histograms();
		histUnique = new Histograms();
		histBest = new Histograms();
		histBestUnique = new Histograms();
		
		bin = 50;
		binSize = 200;
		settings = new SeqVizSettings();
	}

/*	public ContigsManager(BundleContext bc, CyNetwork curNetwork, VisualStyle vs) {
		contigs = new HashMap<String, Contig>();
		readPairs = new HashMap<Long, ReadPair>();
		readPairList = new ArrayList<ReadPair>();
		bundleContext = bc;
		this.vs = vs;
		this.network = curNetwork;
		settings = new SeqVizSettings();
	} */

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
		paired_end_hist.put(name, new double[(contig.sequence().length() / binSize) + 1]);
		paired_end_hist_rev.put(name, new double[(contig.sequence().length() / binSize) + 1]);
		read_cov_hist.put(name, new double[(contig.sequence().length() / binSize) + 1]);
		read_cov_hist_pos.put(name, new double[(contig.sequence().length() / binSize) + 1]);
		read_cov_hist_rev.put(name, new double[(contig.sequence().length() / binSize) + 1]);
		Histograms [] histograms = {histAll, histUnique, histBest, histBestUnique};
		for (Histograms h: histograms) {
			h.paired_end_hist.put(name, new double[(contig.sequence().length() / binSize) + 1]);
			h.paired_end_hist_rev.put(name, new double[(contig.sequence().length() / binSize) + 1]);
			h.read_cov_hist.put(name, new double[(contig.sequence().length() / binSize) + 1]);
			h.read_cov_hist_pos.put(name, new double[(contig.sequence().length() / binSize) + 1]);
			h.read_cov_hist_rev.put(name, new double[(contig.sequence().length() / binSize) + 1]);
		}
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
	
	private ReadPair bundle2Pair(HashMap<String, List<ReadMappingInfo>> readBundle) throws Exception {
		ReadPair thisPair = new ReadPair();
		for (String contigName: readBundle.keySet()) {
			Contig contig = contigs.get(contigName);
			if (contig == null) throw new Exception("Cannot add new read, contig " + contigName + " does not exist.");
			for (ReadMappingInfo readInfo: readBundle.get(contigName)) {
				contig.addReadMappingInfo(readInfo);
				thisPair.addReadMappingInfo(contigName, readInfo);
			}
		}
		return thisPair;
	}
	/**
	 * Adds reads in the form of a read bundle to ContigsManager. A read bundle is a special
	 * HashMap which stores all the ReadMappingInfo for a particular mate-pair.
	 * 
	 * @param readBundle A special object that stores the read mapping information. All
	 * ReadMappingInfo in readBundle are from the same mate-pair (either mate). For a HashMap of
	 * type HashMap<String, List<ReadMappingInfo>>, all ReadMappingInfo in a List<ReadMappingInfo>
	 * is from the same contig of label String.
	 * @throws Exception Throws exception if contig is not already loaded into ContigsManager.
	 */
	public void addRead(HashMap<String, List<ReadMappingInfo>> readBundle) throws Exception {
		ReadPair thisPair = bundle2Pair(readBundle);
	//	readPairList.add(thisPair);
		HashMap<String, List<ReadMappingInfo>>	bestPair = new HashMap<String, List<ReadMappingInfo>>(),
												uniquePair = new HashMap<String, List<ReadMappingInfo>>(),
												bestUniquePair = new HashMap<String, List<ReadMappingInfo>>();
		List<String>	bestPosContig = new ArrayList<String>(),
						bestRevContig = new ArrayList<String>();
		List<ReadMappingInfo>	bestPos = new ArrayList<ReadMappingInfo>(),
								bestRev = new ArrayList<ReadMappingInfo>();
		int pos = 0, rev = 0, posBest = 0, revBest = 0;
		for (String thisContig: readBundle.keySet()) {
			for (ReadMappingInfo readMappingInfo: readBundle.get(thisContig)) {
				if (readMappingInfo.read().pair()) {
					pos++;
					if (bestPos.size() == 0) {
						bestPosContig.add(thisContig);
						bestPos.add(readMappingInfo);
						posBest = 1;
					}
					else {
						if (bestPos.get(0).score() < readMappingInfo.score()) {
							bestPosContig = new ArrayList<String>();
							bestPosContig.add(thisContig);
							bestPos = new ArrayList<ReadMappingInfo>();
							bestPos.add(readMappingInfo);
							posBest = 1;
						}
						else if (bestPos.get(0).score() == readMappingInfo.score()) {
							bestPosContig.add(thisContig);
							bestPos.add(readMappingInfo);
							posBest++;
						}
					}
				}
				else {
					rev++;
					if (bestRev.size() == 0) {
						bestRevContig.add(thisContig);
						bestRev.add(readMappingInfo);
						revBest = 1;
					}
					else {
						if (bestRev.get(0).score() < readMappingInfo.score()) {
							bestRevContig = new ArrayList<String>();
							bestRevContig.add(thisContig);
							bestRev = new ArrayList<ReadMappingInfo>();
							bestRev.add(readMappingInfo);
							revBest = 1;
						}
						else if (bestRev.get(0).score() == readMappingInfo.score()) {
							bestRevContig.add(thisContig);
							bestRev.add(readMappingInfo);
							revBest++;
						}
					}
				}
			}
		}
		for (int i = 0; i < bestPosContig.size(); i++) {
			List<ReadMappingInfo> bestPairPos;
			if (bestPair.containsKey(bestPosContig.get(i)))
				bestPairPos = bestPair.get(bestPosContig.get(i));
			else {
				bestPairPos = new ArrayList<ReadMappingInfo>();
				bestPair.put(bestPosContig.get(i), bestPairPos);
			}
			bestPairPos.add(bestPos.get(i));
		}
		for (int i = 0; i < bestRevContig.size(); i++) {
			List<ReadMappingInfo> bestPairRev;
			if (bestPair.containsKey(bestRevContig.get(i)))
				bestPairRev = bestPair.get(bestRevContig.get(i));
			else {
				bestPairRev = new ArrayList<ReadMappingInfo>();
				bestPair.put(bestRevContig.get(i), bestPairRev);
			}
			bestPairRev.add(bestRev.get(i));
		}
		
		iterativeBpGraph(bpGraphsAll, thisPair);
		iterativeBridgingReads(bridgingReadsAll, thisPair);
		iterativeCreateHist(histAll, thisPair);
		
		iterativeBpGraph(bpGraphsBest, bundle2Pair(bestPair));
		iterativeBridgingReads(bridgingReadsBest, bundle2Pair(bestPair));
		iterativeCreateHist(histBest, bundle2Pair(bestPair));

		if (pos == 1 && rev == 1) {
			iterativeBpGraph(bpGraphsUnique, thisPair);
			iterativeBridgingReads(bridgingReadsUnique, thisPair);
			iterativeCreateHist(histUnique, thisPair);
		}
		
		if (bestPos.size() == 1 && bestRev.size() == 1) {
			iterativeBpGraph(bpGraphsBestUnique, bundle2Pair(bestPair));
			iterativeBridgingReads(bridgingReadsBestUnique, bundle2Pair(bestPair));
			iterativeCreateHist(histBestUnique, bundle2Pair(bestPair));
		}
	}
	
	/**
	 * Returns contig with name "name"
	 * @param name Name of the contig.
	 * @return The contig.
	 */
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
		if (table.getColumn("sequence") == null)
			table.createColumn("sequence", String.class, false);
		for (String s: contigs.keySet()) {
			if (contigs.get(s).node == null) {
				CyNode node = network.addNode();
				contigs.get(s).node = node;
				network.getRow(node).set(CyNetwork.NAME, s);
				network.getRow(node).set("length", (long) contigs.get(s).sequence().length());
				network.getRow(node).set("sequence", contigs.get(s).sequence());
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
	
	private void iterativeBridgingReads(EdgeStat stat, ReadPair p) {
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
									if (stat.edgeWeightPlusPlus.containsKey(edgeName))
										stat.edgeWeightPlusPlus.put(edgeName, stat.edgeWeightPlusPlus.get(edgeName) + weight);
									else stat.edgeWeightPlusPlus.put(edgeName, weight);
									if (stat.edgeCountPlusPlus.containsKey(edgeName))
										stat.edgeCountPlusPlus.put(edgeName, stat.edgeCountPlusPlus.get(edgeName) + 1);
									else stat.edgeCountPlusPlus.put(edgeName, (long) 1);
								}
								else if (read1Orientation && ! read2Orientation) {
									if (edgeNamesPlusMinus.containsKey(edgeName))
										thisEdge = edgeNamesPlusMinus.get(edgeName);
									else {
										thisEdge = network.addEdge(node1, node2, true);
										edgeNamesPlusMinus.put(edgeName, thisEdge);
									}
									if (stat.edgeWeightPlusMinus.containsKey(edgeName))
										stat.edgeWeightPlusMinus.put(edgeName, stat.edgeWeightPlusMinus.get(edgeName) + weight);
									else stat.edgeWeightPlusMinus.put(edgeName, weight);
									if (stat.edgeCountPlusMinus.containsKey(edgeName))
										stat.edgeCountPlusMinus.put(edgeName, stat.edgeCountPlusMinus.get(edgeName) + 1);
									else stat.edgeCountPlusMinus.put(edgeName, (long) 1);
								}
								else if (! read1Orientation && read2Orientation) {
									if (edgeNamesMinusPlus.containsKey(edgeName))
										thisEdge = edgeNamesMinusPlus.get(edgeName);
									else {
										thisEdge = network.addEdge(node1, node2, true);
										edgeNamesMinusPlus.put(edgeName, thisEdge);
									}
									if (stat.edgeWeightMinusPlus.containsKey(edgeName))
										stat.edgeWeightMinusPlus.put(edgeName, stat.edgeWeightMinusPlus.get(edgeName) + weight);
									else stat.edgeWeightMinusPlus.put(edgeName, weight);
									if (stat.edgeCountMinusPlus.containsKey(edgeName))
										stat.edgeCountMinusPlus.put(edgeName, stat.edgeCountMinusPlus.get(edgeName) + 1);
									else stat.edgeCountMinusPlus.put(edgeName, (long) 1);
								}
								else if (! read1Orientation && ! read2Orientation) {
									if (edgeNamesMinusMinus.containsKey(edgeName))
										thisEdge = edgeNamesMinusMinus.get(edgeName);
									else {
										thisEdge = network.addEdge(node1, node2, true);
										edgeNamesMinusMinus.put(edgeName, thisEdge);
									}
									if (stat.edgeWeightMinusMinus.containsKey(edgeName))
										stat.edgeWeightMinusMinus.put(edgeName, stat.edgeWeightMinusMinus.get(edgeName) + weight);
									else stat.edgeWeightMinusMinus.put(edgeName, weight);
									if (stat.edgeCountMinusMinus.containsKey(edgeName))
										stat.edgeCountMinusMinus.put(edgeName, stat.edgeCountMinusMinus.get(edgeName) + 1);
									else stat.edgeCountMinusMinus.put(edgeName, (long) 1);
								}
							}
						}
				}
			}
		}
	}

	private void iterativeBridgingReads(ReadPair p) {
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
	
	private void saveBridgingReads(EdgeStat stat, String title) {
		CyTable table = network.getDefaultEdgeTable();
		String	weight = "weight" + (title == null ? "" : ":" + title),
				orientation = "orientation" + (title == null ? "" : ":" + title),
				reliability = "reliability" + (title == null ? "" : ":" + title),
				weight_log = "weight_log" + (title == null ? "" : ":" + title);
		if (table.getColumn(weight) == null)
			table.createColumn(weight, Double.class, false);
		if (table.getColumn(orientation) == null)
			table.createColumn(orientation, String.class, false);
		if (table.getColumn(reliability) == null)
			table.createColumn(reliability, Double.class, false);
		for (String s: edgeNamesPlusPlus.keySet()) {
			if (stat.edgeWeightPlusPlus.get(s) != null && stat.edgeCountPlusPlus.get(s) != null) {
				table.getRow(edgeNamesPlusPlus.get(s).getSUID()).set(CyNetwork.NAME, s);
				table.getRow(edgeNamesPlusPlus.get(s).getSUID()).set(weight, stat.edgeWeightPlusPlus.get(s));
				table.getRow(edgeNamesPlusPlus.get(s).getSUID()).set(orientation, "plusplus");
				table.getRow(edgeNamesPlusPlus.get(s).getSUID()).set(reliability, stat.edgeWeightPlusPlus.get(s) / stat.edgeCountPlusPlus.get(s));
			}
		}
		for (String s: edgeNamesPlusMinus.keySet()) {
			if (stat.edgeWeightPlusMinus.get(s) != null && stat.edgeCountPlusMinus.get(s) != null) {
				table.getRow(edgeNamesPlusMinus.get(s).getSUID()).set(CyNetwork.NAME, s);
				table.getRow(edgeNamesPlusMinus.get(s).getSUID()).set(weight, stat.edgeWeightPlusMinus.get(s));
				table.getRow(edgeNamesPlusMinus.get(s).getSUID()).set(orientation, "plusminus");
				table.getRow(edgeNamesPlusMinus.get(s).getSUID()).set(reliability, stat.edgeWeightPlusMinus.get(s) / stat.edgeCountPlusMinus.get(s));
			}
		}
		for (String s: edgeNamesMinusPlus.keySet()) {
			if (stat.edgeWeightMinusPlus.get(s) != null && stat.edgeCountMinusPlus.get(s) != null) {
				table.getRow(edgeNamesMinusPlus.get(s).getSUID()).set(CyNetwork.NAME, s);
				table.getRow(edgeNamesMinusPlus.get(s).getSUID()).set(weight, stat.edgeWeightMinusPlus.get(s));
				table.getRow(edgeNamesMinusPlus.get(s).getSUID()).set(orientation, "minusplus");
				table.getRow(edgeNamesMinusPlus.get(s).getSUID()).set(reliability, stat.edgeWeightMinusPlus.get(s) / stat.edgeCountMinusPlus.get(s));
			}
		}
		for (String s: edgeNamesMinusMinus.keySet()) {
			if (stat.edgeWeightMinusMinus.get(s) != null && stat.edgeCountMinusMinus.get(s) != null) {
				table.getRow(edgeNamesMinusMinus.get(s).getSUID()).set(CyNetwork.NAME, s);
				table.getRow(edgeNamesMinusMinus.get(s).getSUID()).set(weight, stat.edgeWeightMinusMinus.get(s));
				table.getRow(edgeNamesMinusMinus.get(s).getSUID()).set(orientation, "minusminus");
				table.getRow(edgeNamesMinusMinus.get(s).getSUID()).set(reliability, stat.edgeWeightMinusMinus.get(s) / stat.edgeCountMinusMinus.get(s));
			}
		}
		// Calculate log of weight
		if (table.getColumn(weight_log) == null)
			table.createColumn(weight_log, Double.class, false);
		for (Long cyId: table.getPrimaryKey().getValues(Long.class))
			table.getRow(cyId).set(weight_log, Math.log(table.getRow(cyId).get("weight", Double.class)));
	}

	/**
	 * Save the edges between the contigs that was built in the process of reading in the output
	 * of the mapper to this session.
	 */
	public void saveBridgingReads() {
		saveBridgingReads(bridgingReadsAll, null);
		saveBridgingReads(bridgingReadsBest, "best");
		saveBridgingReads(bridgingReadsUnique, "unique");
		saveBridgingReads(bridgingReadsBestUnique, "best&unique");
	/*	CyTable table = network.getDefaultEdgeTable();
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
			table.getRow(cyId).set("weight_log", Math.log(table.getRow(cyId).get("weight", Double.class))); */
	}
	
	private void iterativeCreateHist(Histograms hist, ReadPair pair) {
		for (String s: pair.getAllContigs()) {
			double []	paired_end_hist = hist.paired_end_hist.get(s),
						paired_end_hist_rev = hist.paired_end_hist_rev.get(s),
						read_cov_hist = hist.read_cov_hist.get(s),
						read_cov_hist_pos = hist.read_cov_hist_pos.get(s),
						read_cov_hist_rev = hist.read_cov_hist_rev.get(s);
			for (ReadMappingInfo readInfo: pair.getReadMappingInfo(s)) {
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
		}
	}

	public void iterativeCreateHist(ReadPair pair) {
		for (String s: pair.getAllContigs()) {
			double [] paired_end_hist = this.paired_end_hist.get(s),
					paired_end_hist_rev = this.paired_end_hist_rev.get(s),
					read_cov_hist = this.read_cov_hist.get(s),
					read_cov_hist_pos = this.read_cov_hist_pos.get(s),
					read_cov_hist_rev = this.read_cov_hist_rev.get(s);
			for (ReadMappingInfo readInfo: pair.getReadMappingInfo(s)) {
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
		}
	}
	
	private void saveHist(Histograms hist, String title) {
		CyTable table = network.getDefaultNodeTable();
		String	paired_end_hist_string = "paired_end_hist" + (title == null ? "" : ":" + title),
				paired_end_hist_rev_string = "paired_end_hist_rev" + (title == null ? "" : ":" + title),
				read_cov_hist_string = "read_cov_hist" + (title == null ? "" : ":" + title),
				read_cov_hist_pos_string = "read_cov_hist_pos" + (title == null ? "" : ":" + title),
				read_cov_hist_rev_string = "read_cov_hist_rev" + (title == null ? "" : ":" + title),
				paired_end_hist_log_string = "paired_end_hist_log" + (title == null ? "" : ":" + title),
				paired_end_hist_rev_log_string = "paired_end_hist_rev_log" + (title == null ? "" : ":" + title),
				read_cov_hist_log_string = "read_cov_hist_log" + (title == null ? "" : ":" + title),
				read_cov_hist_pos_log_string = "read_cov_hist_pos_log" + (title == null ? "" : ":" + title),
				read_cov_hist_rev_log_string = "read_cov_hist_rev_log" + (title == null ? "" : ":" + title),
				barchart_paired_end_hist_string = "barchart_paired_end_hist" + (title == null ? "" : ":" + title),
				barchart_paired_end_rev_hist_string = "barchart_paired_end_rev_hist" + (title == null ? "" : ":" + title),
				bartchart_read_cov_hist_string = "bartchart_read_cov_hist" + (title == null ? "" : ":" + title),
				bartchart_read_cov_rev_hist_string = "bartchart_read_cov_rev_hist" + (title == null ? "" : ":" + title);
		if (table.getColumn(paired_end_hist_string) == null)
			table.createListColumn(paired_end_hist_string, Double.class, false);
		if (table.getColumn(paired_end_hist_rev_string) == null)
			table.createListColumn(paired_end_hist_rev_string, Double.class, false);
		if (table.getColumn(read_cov_hist_string) == null)
			table.createListColumn(read_cov_hist_string, Double.class, false);
		if (table.getColumn(read_cov_hist_pos_string) == null)
			table.createListColumn(read_cov_hist_pos_string, Double.class, false);
		if (table.getColumn(read_cov_hist_rev_string) == null)
			table.createListColumn(read_cov_hist_rev_string, Double.class, false);

		if (table.getColumn(paired_end_hist_log_string) == null)
			table.createListColumn(paired_end_hist_log_string, Double.class, false);
		if (table.getColumn(paired_end_hist_rev_log_string) == null)
			table.createListColumn(paired_end_hist_rev_log_string, Double.class, false);
		if (table.getColumn(read_cov_hist_log_string) == null)
			table.createListColumn(read_cov_hist_log_string, Double.class, false);
		if (table.getColumn(read_cov_hist_pos_log_string) == null)
			table.createListColumn(read_cov_hist_pos_log_string, Double.class, false);
		if (table.getColumn(read_cov_hist_rev_log_string) == null)
			table.createListColumn(read_cov_hist_rev_log_string, Double.class, false);
		
		if (table.getColumn(barchart_paired_end_hist_string) == null)
			table.createColumn(barchart_paired_end_hist_string, String.class, false);
		if (table.getColumn(barchart_paired_end_rev_hist_string) == null)
			table.createColumn(barchart_paired_end_rev_hist_string, String.class, false);
		if (table.getColumn(bartchart_read_cov_hist_string) == null)
			table.createColumn(bartchart_read_cov_hist_string, String.class, false);
		if (table.getColumn(bartchart_read_cov_rev_hist_string) == null)
			table.createColumn(bartchart_read_cov_rev_hist_string, String.class, false);
		for (String s: contigs.keySet()) {
			double [] paired_end_hist = hist.paired_end_hist.get(s),
					paired_end_hist_rev = hist.paired_end_hist_rev.get(s),
					read_cov_hist = hist.read_cov_hist.get(s),
					read_cov_hist_pos = hist.read_cov_hist_pos.get(s),
					read_cov_hist_rev = hist.read_cov_hist_rev.get(s);
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
				d.add(temp = Math.log(paired_end_hist[i] + 1));
				if (temp > paired_end_max)
					paired_end_max = temp;
			}
			table.getRow(contigs.get(s).node.getSUID()).set(paired_end_hist_string, a);
			table.getRow(contigs.get(s).node.getSUID()).set(paired_end_hist_log_string, d);
			for (int i = 0; i < paired_end_hist_rev.length; i++) {
				b.add(paired_end_hist_rev[i]);
				e.add(temp = - Math.log(- paired_end_hist_rev[i] + 1));
				if (temp < paired_end_min)
					paired_end_min = temp;
			}
			table.getRow(contigs.get(s).node.getSUID()).set(paired_end_hist_rev_string, b);
			table.getRow(contigs.get(s).node.getSUID()).set(paired_end_hist_rev_log_string, e);
			for (int i = 0; i < read_cov_hist.length; i++) {
				c.add(read_cov_hist[i]);
				f.add(temp = Math.log(read_cov_hist[i] + 1));
				if (temp > read_cov_max)
					read_cov_max = temp;
			}
			table.getRow(contigs.get(s).node.getSUID()).set(read_cov_hist_string, c);
			table.getRow(contigs.get(s).node.getSUID()).set(read_cov_hist_log_string, f);
			for (int i = 0; i < read_cov_hist_pos.length; i++) {
				g.add(read_cov_hist_pos[i]);
				h.add(temp = Math.log(read_cov_hist_pos[i] + 1));
				if (temp > read_cov_pos_max)
					read_cov_pos_max = temp;
			}
			table.getRow(contigs.get(s).node.getSUID()).set(read_cov_hist_pos_string, g);
			table.getRow(contigs.get(s).node.getSUID()).set(read_cov_hist_pos_log_string, h);
			for (int i = 0; i < read_cov_hist_rev.length; i++) {
				j.add(read_cov_hist_rev[i]);
				k.add(temp = - Math.log(- read_cov_hist_rev[i] + 1));
				if (temp < read_cov_rev_max)
					read_cov_rev_max = temp;
			}
			table.getRow(contigs.get(s).node.getSUID()).set(read_cov_hist_rev_string, j);
			table.getRow(contigs.get(s).node.getSUID()).set(read_cov_hist_rev_log_string, k);
		}
		for (String s: contigs.keySet()) {
			table.getRow(contigs.get(s).node.getSUID()).set(barchart_paired_end_hist_string, "barchart: attributelist=\"" + paired_end_hist_log_string + "\" showlabels=\"false\" colorlist=\"up:blue,down:yellow,zero:black\" range=\"" + paired_end_min + "," + paired_end_max + "\"");
			table.getRow(contigs.get(s).node.getSUID()).set(barchart_paired_end_rev_hist_string, "barchart: attributelist=\"" + paired_end_hist_rev_log_string + "\" showlabels=\"false\" colorlist=\"up:blue,down:yellow,zero:black\" range=\"" + paired_end_min + "," + paired_end_max + "\"");
			table.getRow(contigs.get(s).node.getSUID()).set(bartchart_read_cov_hist_string, "barchart: attributelist=\"" + read_cov_hist_pos_log_string + "\" showlabels=\"false\" colorlist=\"up:blue,down:yellow,zero:black\" range=\"" + read_cov_rev_max + "," + read_cov_pos_max + "\"");
			table.getRow(contigs.get(s).node.getSUID()).set(bartchart_read_cov_rev_hist_string, "barchart: attributelist=\"" + read_cov_hist_rev_log_string + "\" showlabels=\"false\" colorlist=\"up:blue,down:yellow,zero:black\" range=\"" + read_cov_rev_max + "," + read_cov_pos_max + "\"");
		}
	}

	/**
	 * Save histograms for display on node in this session.
	 */
	public void saveHist() {
		saveHist(histAll, null);
		saveHist(histBest, "best");
		saveHist(histUnique, "unique");
		saveHist(histBestUnique, "best&unique");
	/*	CyTable table = network.getDefaultNodeTable();
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
		for (String s: contigs.keySet()) {
			double [] paired_end_hist = this.paired_end_hist.get(s),
					paired_end_hist_rev = this.paired_end_hist_rev.get(s),
					read_cov_hist = this.read_cov_hist.get(s),
					read_cov_hist_pos = this.read_cov_hist_pos.get(s),
					read_cov_hist_rev = this.read_cov_hist_rev.get(s);
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
				d.add(temp = Math.log(paired_end_hist[i] + 1));
				if (temp > paired_end_max)
					paired_end_max = temp;
			}
			table.getRow(contigs.get(s).node.getSUID()).set("paired_end_hist", a);
			table.getRow(contigs.get(s).node.getSUID()).set("paired_end_hist_log", d);
			for (int i = 0; i < paired_end_hist_rev.length; i++) {
				b.add(paired_end_hist_rev[i]);
				e.add(temp = - Math.log(- paired_end_hist_rev[i] + 1));
				if (temp < paired_end_min)
					paired_end_min = temp;
			}
			table.getRow(contigs.get(s).node.getSUID()).set("paired_end_hist_rev", b);
			table.getRow(contigs.get(s).node.getSUID()).set("paired_end_hist_rev_log", e);
			for (int i = 0; i < read_cov_hist.length; i++) {
				c.add(read_cov_hist[i]);
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
		} */
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
	
	private void iterativeBpGraph(HashMap<String, ComplementaryGraphs> complementaryGraphs, ReadPair pair) {
		for (String contigName: pair.getAllContigs()) {
			ComplementaryGraphs y = complementaryGraphs.get(contigName);
			if (y == null) {
				y = new ComplementaryGraphs();
				complementaryGraphs.put(contigName, y);
			}
			for (ReadMappingInfo read: pair.getReadMappingInfo(contigName)) {
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
						if (contigs.get(contigName).sequence().length() % bin != 0)
							covGraph = new long[(contigs.get(contigName).sequence().length() / bin) + 1];
						else
							covGraph = new long[(contigs.get(contigName).sequence().length() / bin)];
						covGraphs.put(c, covGraph);
					}
					int thisBin = (read.locus() - 1) / bin;
					int readLength = read.read().length(), offset = (read.locus() - 1) % bin;
					while (readLength > 0 && thisBin < covGraph.length) {
						if (bin - offset > readLength)
							covGraph[thisBin] += bin - offset;
						else
							covGraph[thisBin] += readLength;
						readLength -= bin - offset;
						offset = 0;
						thisBin++;
					}
				}
			}
		}
	}
	
	private void iterativeBpGraph(ReadPair pair) {
		for (String contigName: pair.getAllContigs()) {
			ComplementaryGraphs y = complementaryGraphs.get(contigName);
			if (y == null) {
				y = new ComplementaryGraphs();
				complementaryGraphs.put(contigName, y);
			}
			for (ReadMappingInfo read: pair.getReadMappingInfo(contigName)) {
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
						if (contigs.get(contigName).sequence().length() % bin != 0)
							covGraph = new long[(contigs.get(contigName).sequence().length() / bin) + 1];
						else
							covGraph = new long[(contigs.get(contigName).sequence().length() / bin)];
						covGraphs.put(c, covGraph);
					}
					int thisBin = (read.locus() - 1) / bin;
					int readLength = read.read().length(), offset = (read.locus() - 1) % bin;
					while (readLength > 0 && thisBin < covGraph.length) {
						if (bin - offset > readLength)
							covGraph[thisBin] += bin - offset;
						else
							covGraph[thisBin] += readLength;
						readLength -= bin - offset;
						offset = 0;
						thisBin++;
					}
				}
			}
		}
	}

	private void saveBpGraphs(HashMap<String, ComplementaryGraphs> complementaryGraphs, String title) {
		CyTable table = network.getDefaultNetworkTable();
		for (String s: contigs.keySet()) {
			ArrayList<String> colNames = new ArrayList<String>();
			ComplementaryGraphs graphs = complementaryGraphs.get(s);
			String colName;
			int lastBinSize = contigs.get(s).sequence().length() % bin;
			if (lastBinSize == 0)
				lastBinSize = bin;
			if (graphs != null) {
				for (String contigName: graphs.pos.keySet()) {
					colName = s + ":" + (contigName != null ? contigName : "unpaired") + ":" + "+" + (title == null ? "" : ":" + title);
					if (table.getColumn(colName) == null)
						table.createListColumn(colName, Long.class, false);
					List<Long> newList = new ArrayList<Long>();
					long[] temp = graphs.pos.get(contigName);
					for (int i = 0; i < temp.length; i++) {
						if (i < temp.length - 1)
							newList.add(temp[i] / bin);
						else
							newList.add(temp[i] / lastBinSize);
					}
					table.getRow(network.getSUID()).set(colName, newList);
					colNames.add(colName);
				}
				for (String contigName: graphs.rev.keySet()) {
					colName = s + ":" + (contigName != null ? contigName : "unpaired") + ":" + "-" + (title == null ? "" : ":" + title);
					if (table.getColumn(colName) == null)
						table.createListColumn(colName, Long.class, false);
					List<Long> newList = new ArrayList<Long>();
					long[] temp = graphs.rev.get(contigName);
					for (int i = 0; i < temp.length; i++) {
						if (i < temp.length - 1)
							newList.add(-temp[i] / bin);
						else
							newList.add(-temp[i] / lastBinSize);
					}
					table.getRow(network.getSUID()).set(colName, newList);
					colNames.add(colName);
				}
				if (table.getColumn(s + ":graphColumns" + (title == null ? "" : ":" + title)) == null)
					table.createListColumn(s + ":graphColumns" + (title == null ? "" : ":" + title), String.class, false);
				table.getRow(network.getSUID()).set(s + ":graphColumns" + (title == null ? "" : ":" + title), colNames);
			}
		}
		if (table.getColumn("graphBinSize") == null)
			table.createColumn("graphBinSize", Long.class, false);
		table.getRow(network.getSUID()).set("graphBinSize", (long) bin);
	}

	/**
	 * Save graphs generated for ContigView and SequenceView to this session.
	 */
	public void saveBpGraphs() {
		saveBpGraphs(bpGraphsAll, null);
		saveBpGraphs(bpGraphsBest, "best");
		saveBpGraphs(bpGraphsUnique, "unique");
		saveBpGraphs(bpGraphsBestUnique, "best&unique");
	/*	CyTable table = network.getDefaultNetworkTable();
		for (String s: contigs.keySet()) {
			ArrayList<String> colNames = new ArrayList<String>();
			ComplementaryGraphs graphs = complementaryGraphs.get(s);
			String colName;
			int lastBinSize = contigs.get(s).sequence().length() % bin;
			if (lastBinSize == 0)
				lastBinSize = bin;
			if (graphs != null) {
				for (String contigName: graphs.pos.keySet()) {
					colName = s + ":" + (contigName != null ? contigName : "unpaired") + ":" + "+";
					if (table.getColumn(colName) == null)
						table.createListColumn(colName, Long.class, false);
					List<Long> newList = new ArrayList<Long>();
					long[] temp = graphs.pos.get(contigName);
					for (int i = 0; i < temp.length; i++) {
						if (i < temp.length - 1)
							newList.add(temp[i] / bin);
						else
							newList.add(temp[i] / lastBinSize);
					}
					table.getRow(network.getSUID()).set(colName, newList);
					colNames.add(colName);
				}
				for (String contigName: graphs.rev.keySet()) {
					colName = s + ":" + (contigName != null ? contigName : "unpaired") + ":" + "-";
					if (table.getColumn(colName) == null)
						table.createListColumn(colName, Long.class, false);
					List<Long> newList = new ArrayList<Long>();
					long[] temp = graphs.rev.get(contigName);
					for (int i = 0; i < temp.length; i++) {
						if (i < temp.length - 1)
							newList.add(-temp[i] / bin);
						else
							newList.add(-temp[i] / lastBinSize);
					}
					table.getRow(network.getSUID()).set(colName, newList);
					colNames.add(colName);
				}
				if (table.getColumn(s + ":graphColumns") == null)
					table.createListColumn(s + ":graphColumns", String.class, false);
				table.getRow(network.getSUID()).set(s + ":graphColumns", colNames);
			}
		}
		if (table.getColumn("graphBinSize") == null)
			table.createColumn("graphBinSize", Long.class, false);
		table.getRow(network.getSUID()).set("graphBinSize", (long) bin); */
	}

	private ComplementaryGraphs createBpGraph(String contigName) {
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
					if (contigs.get(contigName).sequence().length() % binSize != 0)
						covGraph = new long[(contigs.get(contigName).sequence().length() / binSize) + 1];
					else
						covGraph = new long[(contigs.get(contigName).sequence().length() / binSize)];
					covGraphs.put(c, covGraph);
				}
				int thisBin = (read.locus() - 1) / binSize;
				int readLength = read.read().length(), offset = (read.locus() - 1) % binSize;
				while (readLength > 0 && thisBin < covGraph.length) {
					if (binSize - offset > readLength)
						covGraph[thisBin] += binSize - offset;
					else
						covGraph[thisBin] += readLength;
					readLength -= binSize - offset;
					offset = 0;
					thisBin++;
				}
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
			int lastBinSize = contigs.get(s).sequence().length() % binSize;
			if (lastBinSize == 0)
				lastBinSize = binSize;
			for (String contigName: graphs.pos.keySet()) {
				colName = s + ":" + (contigName != null ? contigName : "unpaired") + ":" + "+";
				if (table.getColumn(colName) == null)
					table.createListColumn(colName, Long.class, false);
				List<Long> newList = new ArrayList<Long>();
				long[] temp = graphs.pos.get(contigName);
				for (int i = 0; i < temp.length; i++) {
					if (i < temp.length - 1)
						newList.add(temp[i] / binSize);
					else
						newList.add(temp[i] / lastBinSize);
				}
				table.getRow(network.getSUID()).set(colName, newList);
				colNames.add(colName);
			}
			for (String contigName: graphs.rev.keySet()) {
				colName = s + ":" + (contigName != null ? contigName : "unpaired") + ":" + "-";
				if (table.getColumn(colName) == null)
					table.createListColumn(colName, Long.class, false);
				List<Long> newList = new ArrayList<Long>();
				long[] temp = graphs.rev.get(contigName);
				for (int i = 0; i < temp.length; i++) {
					if (i < temp.length - 1)
						newList.add(-temp[i] / binSize);
					else
						newList.add(-temp[i] / lastBinSize);
				}
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

	/**
	 * Display network
	 */
	public void displayNetwork() {
		CyNetworkManager networkManager = bundleContext.getService(CyNetworkManager.class);
		networkManager.addNetwork(network);
		
		CyNetworkViewFactory networkViewFactory = bundleContext.getService(CyNetworkViewFactory.class);
		CyNetworkView myView = networkViewFactory.createNetworkView(network);
		CyNetworkViewManager networkViewManager = bundleContext.getService(CyNetworkViewManager.class);
		networkViewManager.addNetworkView(myView);
		
		networkView = myView;
		if (vs != null)
			vs.apply(myView);
		myView.updateView();
	}
	
	/**
	 * Apply a visual style to the network.
	 * @param vs
	 */
	public void applyStyle(VisualStyle vs) {
		if (networkView != null) {
			if (vs != null)
				vs.apply(networkView);
			networkView.updateView();
		}
	}
	
	private class EdgeStat {
		public HashMap<String, Double>	edgeWeightPlusPlus, edgeWeightPlusMinus, edgeWeightMinusPlus, edgeWeightMinusMinus;
		public HashMap<String, Long>	edgeCountPlusPlus, edgeCountPlusMinus, edgeCountMinusPlus, edgeCountMinusMinus;
	
		public EdgeStat() {
			edgeWeightPlusPlus = new HashMap<String, Double>();
			edgeWeightPlusMinus = new HashMap<String, Double>();
			edgeWeightMinusPlus = new HashMap<String, Double>();
			edgeWeightMinusMinus = new HashMap<String, Double>();
			edgeCountPlusPlus = new HashMap<String, Long>();
			edgeCountPlusMinus = new HashMap<String, Long>();
			edgeCountMinusPlus = new HashMap<String, Long>();
			edgeCountMinusMinus = new HashMap<String, Long>();
		}
	}
	
	private class Histograms {
		public HashMap<String, double []> paired_end_hist, paired_end_hist_rev, read_cov_hist, read_cov_hist_pos, read_cov_hist_rev;
		
		public Histograms() {
			paired_end_hist = new HashMap<String, double[]>();
			paired_end_hist_rev = new HashMap<String, double[]>();
			read_cov_hist = new HashMap<String, double[]>();
			read_cov_hist_pos = new HashMap<String, double[]>();
			read_cov_hist_rev = new HashMap<String, double[]>();
		}
	}
}
