package edu.ucsf.rbvi.seqViz.internal.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ReadPair {
	
	private HashMap<String, List<ReadMappingInfo>> mate1;
	private HashMap<String, List<ReadMappingInfo>> mate2;
	
	public ReadPair() {
		this.mate1 = null;
		this.mate2 = null;
	}
	
	/**
	 * Adds mapped contig to the ReadPair data structure
	 * 
	 * @param contig name of the contig the read is on
	 * @param mappingInfo mapping information for the read
	 */
	public void addReadMappingInfo(String contig, ReadMappingInfo mappingInfo) {
		HashMap<String, List<ReadMappingInfo>> thisMap;
		if (mappingInfo.read().pair()) {
			if (mate1 != null)
				thisMap = mate1;
			else {
				thisMap = new HashMap<String, List<ReadMappingInfo>>();
				mate1 = thisMap;
			}
		}
		else {
			if (mate2 != null)
				thisMap = mate2;
			else {
				thisMap = new HashMap<String, List<ReadMappingInfo>>();
				mate2 = thisMap;
			}
		}
		List<ReadMappingInfo> theseReads = thisMap.get(contig);
		if (theseReads != null)
			theseReads.add(mappingInfo);
		else {
			List<ReadMappingInfo> thisList = new ArrayList<ReadMappingInfo>();
			thisList.add(mappingInfo);
			thisMap.put(contig, thisList);
		}
	}
	
	/**
	 * A Set of contigs mate #1 maps to
	 * @return Names of the contigs the read maps to
	 */
	public Set<String> getMate1Contigs() {
		if (mate1 != null)
			return mate1.keySet();
		else return null;
	}
	
	/**
	 * A Set of contigs mate #2 maps to
	 * @return Names of the contigs the read maps to
	 */
	public Set<String> getMate2Contigs() {
		if (mate2 != null)
			return mate2.keySet();
		else return null;
	}
	
	/**
	 * Mapping information for mate #1
	 * @return A Set of positions and other information mate #1 maps to
	 */
	public List<ReadMappingInfo> getReadMappingInfoMate1(String contig) {
		if (mate1 != null)
			return mate1.get(contig);
		else return null;
	}
	
	/**
	 * Mapping information for mate #2
	 * @return A Set of positions and other information mate #2 maps to
	 */
	public List<ReadMappingInfo> getReadMappingInfoMate2(String contig) {
		if (mate1 != null)
			return mate2.get(contig);
		else return null;
	}
	
	/**
	 * Weight of each edge between contigs. For use by displayBridgingReads() to calculate the
	 * weight of each edge.
	 * @return
	 */
	public double weight() {
		if (this.getMate1Contigs() != null && this.getMate2Contigs() != null) {
			int links = this.getMate1Contigs().size() > this.getMate2Contigs().size() ?
					this.getMate1Contigs().size(): this.getMate2Contigs().size();
			return 1.0 / (double) links;
		}
		else return 0.0;
	}
}
