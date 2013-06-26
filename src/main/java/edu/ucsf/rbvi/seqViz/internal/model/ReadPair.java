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
	
	public Set<String> getMate1Contigs() {
		if (mate1 != null)
			return mate1.keySet();
		else return null;
	}
	
	public Set<String> getMate2Contigs() {
		if (mate2 != null)
			return mate2.keySet();
		else return null;
	}
	
	public List<ReadMappingInfo> getReadMappingInfoMate1(String contig) {
		if (mate1 != null)
			return mate1.get(contig);
		else return null;
	}
	
	public List<ReadMappingInfo> getReadMappingInfoMate2(String contig) {
		if (mate1 != null)
			return mate2.get(contig);
		else return null;
	}
}
