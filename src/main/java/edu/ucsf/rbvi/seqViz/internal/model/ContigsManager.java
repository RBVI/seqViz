package edu.ucsf.rbvi.seqViz.internal.model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskMonitor;
import org.osgi.framework.BundleContext;

import edu.ucsf.rbvi.seqViz.internal.utils.ModelUtils;
import edu.ucsf.rbvi.seqViz.internal.utils.StyleMaker;

/**
 * ContigsManager is a container for Contig(s), and some other information that allows construction
 * of a scaffold. ContigsManager associates each contig with a name. It is the main interface by
 * which Cytoscape task will store and manipulate contig and scaffolding information.
 * It is important that the contigs be added to ContigsManager first using addContig(), otherwise
 * an error will be thrown that the contig does not exist.
 * 
 * @author aywu
 *
 */
public class ContigsManager implements SessionLoadedListener {
	
	// Contains the default size for binning reads to generate graph.
	// defaultBin controls binning of reads in Contigs View and
	// defaultBinSize controls binning for reads in custom graphics.
	public static final int defaultBin = 50, defaultBinSize = 200;

	// Column names
	public static String SEQUENCE = "sequence";
	public static String LENGTH = "length";
	public static String GRAPHBINSIZE = "graphBinSize";

	public enum ReadType {
		BEST("best"),
		UNIQUE("unique"),
		BESTANDUNIQUE("best&unique"),
		NONE("");

		private final String title;
		ReadType(final String title) {
			this.title = title;
		}
		public String toString() {return title;}
	}
	
	// Contains a hash table of all contigs.
	private Map<String,Contig> contigs;
	// Settings for how reads are mapped.
	private SeqVizSettings settings;

	// CyNetwork to be displayed.
	private CyNetwork network;
	private CyServiceRegistrar serviceRegistrar;

	// CyNetworkView for this assembly graph
	private CyNetworkView networkView = null;
	// bin size for custom graphics graph and Contigs View, respectively
	private int bin;

	private long minLength = Long.MAX_VALUE;
	private long maxLength = 0;

	// Graphs displayed on Contigs View
	private Map<String, ComplementaryGraphs> bpGraphsAll, bpGraphsUnique, bpGraphsBest, bpGraphsBestUnique;
	// Edge width and other information
	private EdgeStat bridgingReadsAll, bridgingReadsUnique, bridgingReadsBest, bridgingReadsBestUnique;
	// Graphs displayed on custom graphics
	private Histograms histAll, histUnique, histBest, histBestUnique;
	private Map<String, Map<String,VisualStyle>> styles = 
	                                  new HashMap<String, Map<String,VisualStyle>>();

	private StyleMaker styleMaker;

	public ContigsManager(CyServiceRegistrar bc) {
		contigs = new HashMap<String, Contig>();
		serviceRegistrar = bc;
		CyNetworkFactory networkFactory = serviceRegistrar.getService(CyNetworkFactory.class);
		this.network = networkFactory.createNetwork();
		
		bpGraphsAll = new HashMap<String, ComplementaryGraphs>();
		bpGraphsUnique = new HashMap<String, ComplementaryGraphs>();
		bpGraphsBest = new HashMap<String, ComplementaryGraphs>();
		bpGraphsBestUnique = new HashMap<String, ComplementaryGraphs>();
		
		bridgingReadsAll = new EdgeStat(this);
		bridgingReadsUnique = new EdgeStat(this);
		bridgingReadsBest = new EdgeStat(this);
		bridgingReadsBestUnique = new EdgeStat(this);
		
		histAll = new Histograms(defaultBinSize);
		histUnique = new Histograms(defaultBinSize);
		histBest = new Histograms(defaultBinSize);
		histBestUnique = new Histograms(defaultBinSize);
		
		bin = defaultBin;
		settings = new SeqVizSettings();
		styleMaker = new StyleMaker(bc, this);

		// Register ourselves as a session loaded listener
		serviceRegistrar.registerService(this, SessionLoadedListener.class, new Properties());
	}

	/**
	 * Reset this instance of ContigsManager so it can be filled with another network.
	 */
	public void reset() {
		contigs = new HashMap<String, Contig>();
		minLength = Long.MAX_VALUE;
		maxLength = 0;
		
		network.dispose();
		CyNetworkFactory networkFactory = serviceRegistrar.getService(CyNetworkFactory.class);
		this.network = networkFactory.createNetwork();
		
		bpGraphsAll = new HashMap<String, ComplementaryGraphs>();
		bpGraphsUnique = new HashMap<String, ComplementaryGraphs>();
		bpGraphsBest = new HashMap<String, ComplementaryGraphs>();
		bpGraphsBestUnique = new HashMap<String, ComplementaryGraphs>();
		
		bridgingReadsAll = new EdgeStat(this);
		bridgingReadsUnique = new EdgeStat(this);
		bridgingReadsBest = new EdgeStat(this);
		bridgingReadsBestUnique = new EdgeStat(this);
		
		histAll = new Histograms(defaultBinSize);
		histUnique = new Histograms(defaultBinSize);
		histBest = new Histograms(defaultBinSize);
		histBestUnique = new Histograms(defaultBinSize);

		styleMaker = new StyleMaker(serviceRegistrar, this);
	}

	public long minLength() { return minLength; }
	public long maxLength() { return maxLength; }
	
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
		if (contigs.containsKey(name)) 
			throw new Exception("Cannot add contig, contig with same name already exists.");
		contigs.put(name, contig);
		contig.setName(name);
		long length = contig.length();
		if (length < minLength) minLength = length;
		if (length > maxLength) maxLength = length;
		Histograms [] histograms = {histAll, histUnique, histBest, histBestUnique};
		for (Histograms h: histograms) {
			h.init(contig);
		}
	}
	
	private ReadPair bundle2Pair(Map<String, List<ReadMappingInfo>> readBundle) throws Exception {
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
	 * Generates four sets of statistics:
	 * default (null) -- statistics generated from all reads
	 * best -- statistics generated from reads with the best score
	 * (if more than one read have the top scores, all reads with top score
	 * are retained)
	 * best&unique -- statistics generated from reads with best score and
	 * have unique mappings for the pair
	 * unique -- statistics generated from read pairs with unique mapping
	 * 
	 * @param readBundle A special object that stores the read mapping information. All
	 * ReadMappingInfo in readBundle are from the same mate-pair (either mate). For a HashMap of
	 * type HashMap<String, List<ReadMappingInfo>>, all ReadMappingInfo in a List<ReadMappingInfo>
	 * is from the same contig of label String.
	 * @throws Exception Throws exception if contig is not already loaded into ContigsManager.
	 */
	public void addRead(Map<String, List<ReadMappingInfo>> readBundle) throws Exception {
		ReadPair thisPair = bundle2Pair(readBundle);

		Map<String, List<ReadMappingInfo>>	bestPair = new HashMap<String, List<ReadMappingInfo>>(),
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
		bridgingReadsAll.createBridgingReads(network, thisPair);
		histAll.createHist(thisPair);
		
		iterativeBpGraph(bpGraphsBest, bundle2Pair(bestPair));
		bridgingReadsBest.createBridgingReads(network, bundle2Pair(bestPair));
		histBest.createHist(thisPair);

		if (pos <= 1 && rev <= 1) {
			iterativeBpGraph(bpGraphsUnique, thisPair);
			bridgingReadsUnique.createBridgingReads(network, thisPair);
			histUnique.createHist(thisPair);
		}
		
		if (bestPos.size() <= 1 && bestRev.size() <= 1) {
			iterativeBpGraph(bpGraphsBestUnique, bundle2Pair(bestPair));
			bridgingReadsBestUnique.createBridgingReads(network, bundle2Pair(bestPair));
			histBestUnique.createHist(thisPair);
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
		ModelUtils.createColumn(table, LENGTH, Long.class);
		ModelUtils.createColumn(table, SEQUENCE, String.class);
		for (String s: contigs.keySet()) {
			if (contigs.get(s).node == null) {
				CyNode node = network.addNode();
				contigs.get(s).node = node;
				network.getRow(node).set(CyNetwork.NAME, s);
				network.getRow(node).set(LENGTH, (long) contigs.get(s).length());
				network.getRow(node).set(SEQUENCE, contigs.get(s).sequence());
			}
		}
	}
	
	/**
	 * Save the edges between the contigs that was built in the process of reading in the output
	 * of the mapper to this session.
	 */
	public void saveBridgingReads() {
		bridgingReadsAll.saveBridgingReads(network, ReadType.NONE);
		bridgingReadsBest.saveBridgingReads(network, ReadType.BEST);
		bridgingReadsUnique.saveBridgingReads(network, ReadType.UNIQUE);
		bridgingReadsBestUnique.saveBridgingReads(network, ReadType.BESTANDUNIQUE);
	}
	

	/**
	 * Save histograms for display on node in this session.
	 */
	public void saveHist() {
		histAll.saveHist(network, ReadType.NONE, contigs);
		histBest.saveHist(network, ReadType.BEST, contigs);
		histUnique.saveHist(network, ReadType.UNIQUE, contigs);
		histBestUnique.saveHist(network, ReadType.BESTANDUNIQUE, contigs);
	}
	
	private void iterativeBpGraph(Map<String, ComplementaryGraphs> complementaryGraphs, ReadPair pair) {
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
					Map<String, long[]> covGraphs;
					if (read.strand()) covGraphs = y.pos;
					else covGraphs = y.rev;
					long[] covGraph;
					if (covGraphs.containsKey(c))
						covGraph = covGraphs.get(c);
					else {
						if (contigs.get(contigName).length() % bin != 0)
							covGraph = new long[(contigs.get(contigName).length() / bin) + 1];
						else
							covGraph = new long[(contigs.get(contigName).length() / bin)];
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

	private void saveBpGraphs(Map<String, ComplementaryGraphs> complementaryGraphs, ReadType type) {
		String title = type.toString();
		CyTable table = network.getDefaultNetworkTable();
		for (String s: contigs.keySet()) {
			ArrayList<String> colNames = new ArrayList<String>();
			ComplementaryGraphs graphs = complementaryGraphs.get(s);
			String colName;
			int lastBinSize = contigs.get(s).length() % bin;
			if (lastBinSize == 0)
				lastBinSize = bin;
			if (graphs != null) {
				for (String contigName: graphs.pos.keySet()) {
					String colPrefix = s + ":" + (contigName != null ? contigName : "unpaired") + ":" + "+";
					colName = ModelUtils.createListColumn(table, colPrefix, Long.class, title);
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
					String colPrefix = s + ":" + (contigName != null ? contigName : "unpaired") + ":" + "-";
					colName = ModelUtils.createListColumn(table, colPrefix, Long.class, title);
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
				String gColumn = ModelUtils.createListColumn(table, s+":graphColumns", String.class, title);
				table.getRow(network.getSUID()).set(gColumn, colNames);
			}
		}
		ModelUtils.createColumn(table, GRAPHBINSIZE, Long.class);
		table.getRow(network.getSUID()).set(GRAPHBINSIZE, (long) bin);
	}

	/**
	 * Save graphs generated for ContigView and SequenceView to this session.
	 */
	public void saveBpGraphs() {
		saveBpGraphs(bpGraphsAll, ReadType.NONE);
		saveBpGraphs(bpGraphsBest, ReadType.BEST);
		saveBpGraphs(bpGraphsUnique, ReadType.UNIQUE);
		saveBpGraphs(bpGraphsBestUnique, ReadType.BESTANDUNIQUE);
	}

	/**
	 * Display network
	 */
	public void displayNetwork() {
		CyNetworkManager networkManager = serviceRegistrar.getService(CyNetworkManager.class);
		networkManager.addNetwork(network);
		
		CyNetworkViewFactory networkViewFactory = serviceRegistrar.getService(CyNetworkViewFactory.class);
		CyNetworkView myView = networkViewFactory.createNetworkView(network);
		CyNetworkViewManager networkViewManager = serviceRegistrar.getService(CyNetworkViewManager.class);
		networkViewManager.addNetworkView(myView);
		
		networkView = myView;
		VisualStyle vs = styleMaker.getStyle(StyleMaker.HistogramType.NONE,ReadType.NONE);
		styleMaker.setVisualStyle(vs, myView);
		myView.updateView();
	}

	public VisualStyle getVisualStyle(StyleMaker.HistogramType hType, ReadType rType) {
		return styleMaker.getStyle(hType, rType);
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

	public void handleEvent(SessionLoadedEvent e) {
		// reloadVisualStyles();
		reset();
	}
}
