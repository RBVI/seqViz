package edu.ucsf.rbvi.seqViz.internal.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.ucsf.rbvi.seqViz.internal.tasks.AbstractMapReadsTask;
import edu.ucsf.rbvi.seqViz.internal.tasks.AbstractMapOutputReader;

/**
 * Container class holding settings for how a set of reads is mapped to a set of contigs.
 * @author Allan Wu
 *
 */
public class SeqVizSettings {
	public AbstractMapReadsTask mapReads;
	public int threads;
	public String mapper_dir;
	public String options;
	public String temp_dir;
	public List<File> mate1;
	public List<File> mate2;
	public File contigs;
	public File mapContigs;
	public boolean loadBridingReads;
	
	/**
	 * Creates settings for SeqViz. Settings initialize the program. 
	 * 
	 * @param threads Number of threads for mapper
	 * @param mapper_dir Folder containing mapper executables.
	 * @param options Options for mapper
	 * @param temp_dir Temporary folder to store contig index.
	 * @param contigs File containing the contigs for ContigsManager to read.
	 * @param mapContigs File containing the contigs the mapper will map to.
	 * @param contigReader A class that parses a contig file and stores it in ContigsManager.
	 */
	public SeqVizSettings(AbstractMapReadsTask mapReads, int threads, String mapper_dir, /*String options,*/ String temp_dir, /*, File contigs, File mapContigs*/ boolean loadBridgingReads) {
		this.mapReads = mapReads;
		this.threads = threads;
	/*	this.options = options; */
		this.mapper_dir = mapper_dir;
		this.temp_dir = temp_dir;
		this.loadBridingReads = loadBridgingReads;
	/*	this.contigs = contigs;
		this.mapContigs = mapContigs; */
	}
	
	public SeqVizSettings() {
		int cores = Runtime.getRuntime().availableProcessors();
		cores = Math.max(cores-2, 1);
		threads = cores;
		loadBridingReads = false;
	}
	
}
