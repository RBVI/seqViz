package edu.ucsf.rbvi.seqViz.internal.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.seqViz.internal.model.ContigsManager;
import edu.ucsf.rbvi.seqViz.internal.model.Read;

public class SAMReader extends ReadsReader {

	public SAMReader(ContigsManager contigs) {
		super(contigs);
		// TODO Auto-generated constructor stub
	}

	public void readReads(InputStream stream, TaskMonitor monitor, int reads) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		if (contigs == null) throw new Exception("The scaffold has not been created.");
		String line, readName, prevReadName = null;
		while ((line = reader.readLine()) != null) {
			String[] fields = line.split("\t");
			Read read1 = null, read2 = null;
			if (fields.length >= 11) {
				readName = fields[0];
				int flags = Integer.parseInt(fields[1]);
				boolean matePair = (flags & 1) == 0 ? false: true,
						aligned = (flags & 4) == 0 ? true: false,
						reverse = (flags & 16) == 0 ? false: true,
						mateReverse = (flags & 32) == 0 ? false: true,
						mate1 = (flags & 64) == 0 ? false: true,
						mate2 = (flags & 128) == 0 ? false: true;
				String contig = fields[2];
				int locus = Integer.parseInt(fields[3]);
				String seq = fields[9];
				int score = 0;
				if (fields.length >= 12)
					score = Integer.parseInt(fields[11].split(":")[2]);
				if (prevReadName == null || ! readName.equals(prevReadName)) {
					read1 = null;
					read2 = null;
				}
				if (contigs == null) throw new Exception("ContigManager not initialized.");
				if (mate1) {
					if (read1 == null)
						read1 = new Read(readName, true, seq.length(), "");
					contigs.addRead(contig, read1, score, locus, !reverse);
				}
				if (mate2) {
					if (read2 == null)
						read2 = new Read(readName, false, seq.length(), "");
					contigs.addRead(contig, read2, score, locus, !reverse);
				}
				prevReadName = readName;
			}
		}
	}

}
