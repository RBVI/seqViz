package edu.ucsf.rbvi.seqViz.internal.model;
	
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;

import edu.ucsf.rbvi.seqViz.internal.utils.ModelUtils;

/**
 * Class holding statistics on contig coverage.
 * @author Allan Wu
 *
 */
public class Histograms {
	public static String PAIRED_END = "paired_end_hist";
	public static String PAIRED_END_REV = "paired_end_hist_rev";
	public static String PAIRED_END_LOG = "paired_end_hist_log";
	public static String PAIRED_END_REV_LOG = "paired_end_hist_rev_log";
	public static String READ_COV = "read_cov_hist";
	public static String READ_COV_POS = "read_cov_hist_pos";
	public static String READ_COV_REV = "read_cov_hist_rev";
	public static String READ_COV_LOG = "read_cov_hist_log";
	public static String READ_COV_POS_LOG = "read_cov_hist_pos_log";
	public static String READ_COV_REV_LOG = "read_cov_hist_rev_log";
	public static String BARCHART_PAIRED_END = "barchart_paired_end_hist";
	public static String BARCHART_PAIRED_END_REV = "barchart_paired_end_rev_hist";
	public static String BARCHART_READ_COV = "barchart_read_cov_hist";
	public static String BARCHART_READ_COV_REV = "barchart_read_cov_rev_hist";

	private Map<String, double []> paired_end_hist, paired_end_hist_rev, read_cov_hist, 
	                              read_cov_hist_pos, read_cov_hist_rev;

	private double paired_end_min = Double.MAX_VALUE;
	private double paired_end_max = 0;
	private double read_cov_max = 0;
	private double read_cov_pos_max = 0;
	private double read_cov_rev_max = 0;
	private int binSize;

	public Histograms(int binSize) {
		paired_end_hist = new HashMap<String, double[]>();
		paired_end_hist_rev = new HashMap<String, double[]>();
		read_cov_hist = new HashMap<String, double[]>();
		read_cov_hist_pos = new HashMap<String, double[]>();
		read_cov_hist_rev = new HashMap<String, double[]>();
		this.binSize = binSize;
	}

	public void init(Contig contig) {
		String name = contig.getName();
		int size = (contig.sequence().length()/binSize)+1;
		paired_end_hist.put(name, new double[size]);
		paired_end_hist_rev.put(name, new double[size]);
		read_cov_hist.put(name, new double[size]);
		read_cov_hist_pos.put(name, new double[size]);
		read_cov_hist_rev.put(name, new double[size]);
	}

	public void createHist(ReadPair pair) {
		for (String s: pair.getAllContigs()) {
			double[] pe_hist = paired_end_hist.get(s);
			double[] pe_hist_rev = paired_end_hist_rev.get(s);
			double[] rc_hist = read_cov_hist.get(s);
			double[] rc_hist_pos = read_cov_hist_pos.get(s);
			double[] rc_hist_rev = read_cov_hist_rev.get(s);

			for (ReadMappingInfo readInfo: pair.getReadMappingInfo(s)) {
			double length = (double) readInfo.read().length();
				rc_hist[readInfo.locus() / binSize] += length / (double) binSize;
				if (readInfo.strand())
					rc_hist_pos[readInfo.locus() / binSize] += length / (double) binSize;
				else
					rc_hist_rev[readInfo.locus() / binSize] -= length / (double) binSize;

				if (!readInfo.sameContig())
					if (readInfo.strand())
						pe_hist[readInfo.locus() / binSize] += length / (double) binSize;
					else
						pe_hist_rev[readInfo.locus() / binSize] -= length / (double) binSize;
			}
		}
	}

	public void saveHist(CyNetwork network, ContigsManager.ReadType readType, 
	                     Map<String, Contig> contigs) {
		String title = readType.toString();
		CyTable table = network.getDefaultNodeTable();
		String paired_end_hist_string = 
		    ModelUtils.createListColumn(table, PAIRED_END, Double.class, title);
		String paired_end_hist_rev_string = 
		    ModelUtils.createListColumn(table, PAIRED_END_REV, Double.class, title);
		String paired_end_hist_log_string = 
		    ModelUtils.createListColumn(table, PAIRED_END_LOG, Double.class, title);
		String paired_end_hist_rev_log_string = 
		    ModelUtils.createListColumn(table, PAIRED_END_REV_LOG, Double.class, title);

		String read_cov_hist_string = 
		    ModelUtils.createListColumn(table, READ_COV, Double.class, title);
		String read_cov_hist_pos_string = 
		    ModelUtils.createListColumn(table, READ_COV_POS, Double.class, title);
		String read_cov_hist_rev_string = 
		    ModelUtils.createListColumn(table, READ_COV_REV, Double.class, title);
		String read_cov_hist_log_string = 
		    ModelUtils.createListColumn(table, READ_COV_LOG, Double.class, title);
		String read_cov_hist_pos_log_string = 
		    ModelUtils.createListColumn(table, READ_COV_POS_LOG, Double.class, title);
		String read_cov_hist_rev_log_string = 
		    ModelUtils.createListColumn(table, READ_COV_REV_LOG, Double.class, title);

		String barchart_paired_end_hist_string = 
		    ModelUtils.createColumn(table, BARCHART_PAIRED_END, String.class, title);
		String barchart_paired_end_rev_hist_string = 
		    ModelUtils.createColumn(table, BARCHART_PAIRED_END_REV, String.class, title);
		String bartchart_read_cov_hist_string = 
		    ModelUtils.createColumn(table, BARCHART_READ_COV, String.class, title);
		String bartchart_read_cov_rev_hist_string = 
		    ModelUtils.createColumn(table, BARCHART_READ_COV_REV, String.class, title);

		double temp = 0;

		for (String s: contigs.keySet()) {
			double [] pe_hist = paired_end_hist.get(s),
					pe_hist_rev = paired_end_hist_rev.get(s),
					rc_hist = read_cov_hist.get(s),
					rc_hist_pos = read_cov_hist_pos.get(s),
					rc_hist_rev = read_cov_hist_rev.get(s);

			long suid = contigs.get(s).node.getSUID();

			List<Double>	pairedEndArray = new ArrayList<Double>(), /* paired end histogram */
								pairedEndRevArray = new ArrayList<Double>(),
								readCovArray = new ArrayList<Double>(),
								pairedEndLogArray = new ArrayList<Double>(), /* log of paired end histogram */
								pairedEndRevLogArray = new ArrayList<Double>(),
								readCovLogArray = new ArrayList<Double>(),
								readCovPosArray = new ArrayList<Double>(),
								readCovPosLogArray = new ArrayList<Double>(),
								readCovRevArray = new ArrayList<Double>(),
								readCovRevLogArray = new ArrayList<Double>();

			// Handle paired end histogram
			for (int i = 0; i < pe_hist.length; i++) {
				pairedEndArray.add(pe_hist[i]);
				pairedEndLogArray.add(temp = Math.log(pe_hist[i] + 1));
				if (temp > paired_end_max)
					paired_end_max = temp;
			}
			table.getRow(suid).set(paired_end_hist_string, pairedEndArray);
			table.getRow(suid).set(paired_end_hist_log_string, pairedEndLogArray);
	
			// Handle paired end rev histogram
			for (int i = 0; i < pe_hist_rev.length; i++) {
				pairedEndRevArray.add(pe_hist_rev[i]);
				pairedEndRevLogArray.add(temp = - Math.log(- pe_hist_rev[i] + 1));
				if (temp < paired_end_min)
					paired_end_min = temp;
			}
			table.getRow(suid).set(paired_end_hist_rev_string, pairedEndRevArray);
			table.getRow(suid).set(paired_end_hist_rev_log_string, pairedEndRevLogArray);

			// Handle read coverage histogram
			for (int i = 0; i < rc_hist.length; i++) {
				readCovArray.add(rc_hist[i]);
				readCovLogArray.add(temp = Math.log(rc_hist[i] + 1));
				if (temp > read_cov_max)
					read_cov_max = temp;
			}
			table.getRow(suid).set(read_cov_hist_string, readCovArray);
			table.getRow(suid).set(read_cov_hist_log_string, readCovLogArray);

			// Handle read coverage position histogram
			for (int i = 0; i < rc_hist_pos.length; i++) {
				readCovPosArray.add(rc_hist_pos[i]);
				readCovPosLogArray.add(temp = Math.log(rc_hist_pos[i] + 1));
				if (temp > read_cov_pos_max)
					read_cov_pos_max = temp;
			}
			table.getRow(suid).set(read_cov_hist_pos_string, readCovPosArray);
			table.getRow(suid).set(read_cov_hist_pos_log_string, readCovPosLogArray);

			// Finally, handl read coverate reverse
			for (int i = 0; i < rc_hist_rev.length; i++) {
				readCovRevArray.add(rc_hist_rev[i]);
				readCovRevLogArray.add(temp = - Math.log(- rc_hist_rev[i] + 1));
				if (temp < read_cov_rev_max)
					read_cov_rev_max = temp;
			}
			table.getRow(suid).set(read_cov_hist_rev_string, readCovRevArray);
			table.getRow(suid).set(read_cov_hist_rev_log_string, readCovRevLogArray);
		}

		// Create all of our barcharts
		for (String s: contigs.keySet()) {
			long suid = contigs.get(s).node.getSUID();

			table.getRow(suid).set(barchart_paired_end_hist_string, 
				"barchart: attributelist=\"" + paired_end_hist_log_string + 
				"\" showlabels=\"false\" colorlist=\"up:blue,down:yellow,zero:black\" range=\"" + 
				paired_end_min + "," + paired_end_max + "\"");

			table.getRow(suid).set(barchart_paired_end_rev_hist_string, 
				"barchart: attributelist=\"" + paired_end_hist_rev_log_string + 
				"\" showlabels=\"false\" colorlist=\"up:blue,down:yellow,zero:black\" range=\"" + 
				paired_end_min + "," + paired_end_max + "\"");

			table.getRow(suid).set(bartchart_read_cov_hist_string, 
			   "barchart: attributelist=\"" + read_cov_hist_pos_log_string + 
				 "\" showlabels=\"false\" colorlist=\"up:blue,down:yellow,zero:black\" range=\"" + 
				 read_cov_rev_max + "," + read_cov_pos_max + "\"");

			table.getRow(suid).set(bartchart_read_cov_rev_hist_string, 
			    "barchart: attributelist=\"" + read_cov_hist_rev_log_string + 
					"\" showlabels=\"false\" colorlist=\"up:blue,down:yellow,zero:black\" range=\"" + 
					read_cov_rev_max + "," + read_cov_pos_max + "\"");
		}
	}
}
