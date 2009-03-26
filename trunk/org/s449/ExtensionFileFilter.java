package org.s449;
/*
 * ExtensionFileFilter.java modified 2/4/08 (rev 2)
 */

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.filechooser.FileFilter;

public class ExtensionFileFilter extends FileFilter {
	private Hashtable<String, ExtensionFileFilter> filters = null;
	private String description = null;
	private String fullDescription = null;
	private boolean useExtensionsInDescription = true;
	public ExtensionFileFilter() {
		this.filters = new Hashtable<String, ExtensionFileFilter>(5);
	}
	public ExtensionFileFilter(String extension, String description) {
		this();
		if(extension!=null) addExtension(extension);
		if(description!=null) setDescription(description);
	}
	public ExtensionFileFilter(String[] filters, String description) {
		this();
		for (int i = 0; i < filters.length; i++) {
			// add filters one by one
			addExtension(filters[i]);
		}
		if(description!=null) setDescription(description);
	}
	public boolean accept(File f) {
		if(f != null) {
			if(f.isDirectory()) {
				return true;
			}
			String extension = getExtension(f);
			if(extension != null && filters.get(extension) != null) {
				return true;
			};
		}
		return false;
	}
	public String getExtension(File f) {
		if(f != null) {
			String filename = f.getName();
			int i = filename.lastIndexOf('.');
			if(i>0 && i<filename.length()-1) {
				return filename.substring(i+1).toLowerCase();
			};
		}
		return null;
	}
	public void addExtension(String extension) {
		if(filters == null) {
			filters = new Hashtable<String, ExtensionFileFilter>(5);
		}
		filters.put(extension.toLowerCase(), this);
		fullDescription = null;
	}
	public String getDescription() {
		if(fullDescription == null) {
			if(description == null || isExtensionListInDescription()) {
				fullDescription = description==null ? "(" : description + " (";
				// build the description from the extension list
				Enumeration<String> extensions = filters.keys();
				if(extensions != null) {
					fullDescription += "." + (String) extensions.nextElement();
					while (extensions.hasMoreElements()) {
						fullDescription += ", ." + (String) extensions.nextElement();
					}
				}
				fullDescription += ")";
			} else {
				fullDescription = description;
			}
		}
		return fullDescription;
	}
	public void setDescription(String description) {
		this.description = description;
		fullDescription = null;
	}
	public boolean isExtensionListInDescription() {
		return useExtensionsInDescription;
	}
}