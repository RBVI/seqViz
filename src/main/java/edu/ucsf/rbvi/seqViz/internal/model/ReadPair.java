package edu.ucsf.rbvi.seqViz.internal.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReadPair {
	
/*	private HashMap<String, List<ReadMappingInfo>> mate1;
	private HashMap<String, List<ReadMappingInfo>> mate2; */
	private HashMap<String, List<ReadMappingInfo>> readInfo;
	
	public ReadPair() {
	/*	this.mate1 = null;
		this.mate2 = null; */
		readInfo = new HashMap<String, List<ReadMappingInfo>>();
	}
	
	/**
	 * Adds mapped contig to the ReadPair data structure
	 * 
	 * @param contig name of the contig the read is on
	 * @param mappingInfo mapping information for the read
	 */
	public void addReadMappingInfo(String contig, ReadMappingInfo mappingInfo) {
		List<ReadMappingInfo> theseReads;
		if (readInfo.get(contig) != null)
			theseReads = readInfo.get(contig);
		else
			theseReads = new ArrayList<ReadMappingInfo>();
		theseReads.add(mappingInfo);
		readInfo.put(contig, theseReads);
	/*	HashMap<String, List<ReadMappingInfo>> thisMap;
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
		} */
	}
	
	/**
	 * A Set of contigs mate #1 maps to
	 * @return Names of the contigs the read maps to
	 */
	public Set<String> getMate1Contigs() {
		HashSet<String> result = null;
		for (String s: readInfo.keySet())
			for (ReadMappingInfo r: readInfo.get(s)) {
				if (r.read().pair()) {
					if (result == null)
						result = new HashSet<String>();
					result.add(s);
				}
			}
		return result;
	/*	if (mate1 != null)
			return mate1.keySet();
		else return null; */
	}
	
	/**
	 * A Set of contigs mate #2 maps to
	 * @return Names of the contigs the read maps to
	 */
	public Set<String> getMate2Contigs() {
		HashSet<String> result = null;
		for (String s: readInfo.keySet())
			for (ReadMappingInfo r: readInfo.get(s)) {
				if (! r.read().pair()) {
					if (result == null)
						result = new HashSet<String>();
					result.add(s);
				}
			}
		return result;
	/*	if (mate2 != null)
			return mate2.keySet();
		else return null; */
	}
	
	/**
	 * Mapping information for mate #1
	 * @return A Set of positions and other information mate #1 maps to
	 */
	public List<ReadMappingInfo> getReadMappingInfoMate1(String contig) {
		List<ReadMappingInfo> temp = readInfo.get(contig), result = null;
		for (ReadMappingInfo r: temp)
			if (r.read().pair()) {
				if (result == null) result = new ArrayList<ReadMappingInfo>();
				result.add(r);
			}
		return result;
	/*	if (mate1 != null)
			return mate1.get(contig);
		else return null; */
	}
	
	/**
	 * Mapping information for mate #2
	 * @return A Set of positions and other information mate #2 maps to
	 */
	public List<ReadMappingInfo> getReadMappingInfoMate2(String contig) {
		List<ReadMappingInfo> temp = readInfo.get(contig), result = null;
		for (ReadMappingInfo r: temp)
			if (! r.read().pair()) {
				if (result == null) result = new ArrayList<ReadMappingInfo>();
				result.add(r);
			}
		return result;
	/*	if (mate1 != null)
			return mate2.get(contig);
		else return null; */
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
