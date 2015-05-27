package edu.ucsf.rbvi.seqViz.internal.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;

import edu.ucsf.rbvi.seqViz.internal.utils.ModelUtils;
	
/**
 * Class holding statistics calculated on edges.
 * @author Allan Wu
 *
 */
public class EdgeStat {

	// Column names
	public static String WEIGHT = "weight";
	public static String ORIENTATION = "orientation";
	public static String RELIABILITY = "reliability";
	public static String WEIGHT_LOG = "weight_log";

	// Read orientations
	public static String PLUSPLUS = "plusplus";
	public static String PLUSMINUS = "plusminus";
	public static String MINUSPLUS = "minusplus";
	public static String MINUSMINUS = "minusminus";


	private Map<String, Double>	edgeWeightPlusPlus, edgeWeightPlusMinus, edgeWeightMinusPlus, edgeWeightMinusMinus;
	private Map<String, Long>	edgeCountPlusPlus, edgeCountPlusMinus, edgeCountMinusPlus, edgeCountMinusMinus;
	private Map<String, CyEdge>	edgeNamesPlusPlus, edgeNamesPlusMinus, edgeNamesMinusPlus, edgeNamesMinusMinus;

	private final ContigsManager contigManager;
	
	public EdgeStat(final ContigsManager contigManager) {
		edgeWeightPlusPlus = new HashMap<String, Double>();
		edgeWeightPlusMinus = new HashMap<String, Double>();
		edgeWeightMinusPlus = new HashMap<String, Double>();
		edgeWeightMinusMinus = new HashMap<String, Double>();
		edgeCountPlusPlus = new HashMap<String, Long>();
		edgeCountPlusMinus = new HashMap<String, Long>();
		edgeCountMinusPlus = new HashMap<String, Long>();
		edgeCountMinusMinus = new HashMap<String, Long>();
		edgeNamesPlusPlus = new HashMap<String, CyEdge>();
		edgeNamesPlusMinus = new HashMap<String, CyEdge>();
		edgeNamesMinusPlus = new HashMap<String, CyEdge>();
		edgeNamesMinusMinus = new HashMap<String, CyEdge>();
		this.contigManager = contigManager;
	}

	public void createBridgingReads(CyNetwork network, ReadPair p) {
		if (p.getMate1Contigs() != null && p.getMate2Contigs() != null) {
		double weight = p.weight();

		for (String contig1: p.getMate1Contigs())
			for (String contig2: p.getMate2Contigs()) {
				if (! contig1.equals(contig2)) {
					String thisContig1, thisContig2;
					CyNode n1 = contigManager.getContig(contig1).node,
							n2 = contigManager.getContig(contig2).node,
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

	public void saveBridgingReads(CyNetwork network, ContigsManager.ReadType readType) {
		String title = readType.toString();
		CyTable table = network.getDefaultEdgeTable();
		String weight = ModelUtils.createColumn(table, WEIGHT, Double.class, title);
		String orientation = ModelUtils.createColumn(table, ORIENTATION, String.class, title);
		String reliability = ModelUtils.createColumn(table, RELIABILITY, Double.class, title);
		for (String s: edgeNamesPlusPlus.keySet()) {
			if (edgeWeightPlusPlus.get(s) != null && edgeCountPlusPlus.get(s) != null) {
				long suid = edgeNamesPlusPlus.get(s).getSUID();
				table.getRow(suid).set(CyNetwork.NAME, s);
				table.getRow(suid).set(weight, edgeWeightPlusPlus.get(s));
				table.getRow(suid).set(orientation, PLUSPLUS);
				table.getRow(suid).set(reliability, edgeWeightPlusPlus.get(s) / edgeCountPlusPlus.get(s));
			}
		}
		for (String s: edgeNamesPlusMinus.keySet()) {
			if (edgeWeightPlusMinus.get(s) != null && edgeCountPlusMinus.get(s) != null) {
				long suid = edgeNamesPlusMinus.get(s).getSUID();
				table.getRow(suid).set(CyNetwork.NAME, s);
				table.getRow(suid).set(weight, edgeWeightPlusMinus.get(s));
				table.getRow(suid).set(orientation, PLUSMINUS);
				table.getRow(suid).set(reliability, edgeWeightPlusMinus.get(s) / edgeCountPlusMinus.get(s));
			}
		}
		for (String s: edgeNamesMinusPlus.keySet()) {
			if (edgeWeightMinusPlus.get(s) != null && edgeCountMinusPlus.get(s) != null) {
				long suid = edgeNamesMinusPlus.get(s).getSUID();
				table.getRow(suid).set(CyNetwork.NAME, s);
				table.getRow(suid).set(weight, edgeWeightMinusPlus.get(s));
				table.getRow(suid).set(orientation, MINUSPLUS);
				table.getRow(suid).set(reliability, edgeWeightMinusPlus.get(s) / edgeCountMinusPlus.get(s));
			}
		}
		for (String s: edgeNamesMinusMinus.keySet()) {
			if (edgeWeightMinusMinus.get(s) != null && edgeCountMinusMinus.get(s) != null) {
				long suid = edgeNamesMinusMinus.get(s).getSUID();
				table.getRow(suid).set(CyNetwork.NAME, s);
				table.getRow(suid).set(weight, edgeWeightMinusMinus.get(s));
				table.getRow(suid).set(orientation, MINUSMINUS);
				table.getRow(suid).set(reliability, edgeWeightMinusMinus.get(s) / edgeCountMinusMinus.get(s));
			}
		}
		// Calculate log of weight
		String weight_log = ModelUtils.createColumn(table, WEIGHT_LOG, Double.class, title);
		for (Long cyId: table.getPrimaryKey().getValues(Long.class)) {
			Double getWeight = table.getRow(cyId).get(weight, Double.class);
			if (getWeight != null)
				table.getRow(cyId).set(weight_log, Math.log(getWeight));
		}
	}
}
