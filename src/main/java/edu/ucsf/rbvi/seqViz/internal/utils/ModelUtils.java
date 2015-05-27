package edu.ucsf.rbvi.seqViz.internal.utils;

import org.cytoscape.model.CyTable;

public class ModelUtils {
	
	public static void createColumn(CyTable table, String columnName, Class<?> clazz) {
		if (table.getColumn(columnName) == null)
			table.createColumn(columnName, clazz, false);
	}

	public static String createColumn(CyTable table, String prefix, Class<?> clazz, String title) {
		String	columnName = prefix + ((title == null||title == "") ? "" : ":" + title);
		if (table.getColumn(columnName) == null)
			table.createColumn(columnName, clazz, false);
		return columnName;
	}

	public static void createListColumn(CyTable table, String columnName, Class<?> listElementType) {
		if (table.getColumn(columnName) == null)
			table.createListColumn(columnName, listElementType, false);
	}

	public static String createListColumn(CyTable table, String prefix, Class<?> listElementType, String title) {
		String	columnName = prefix + ((title == null||title == "") ? "" : ":" + title);
		if (table.getColumn(columnName) == null)
			table.createListColumn(columnName, listElementType, false);
		return columnName;
	}

}
