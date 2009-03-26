package org.s449.extra;

import java.io.*;

public class LineCount {
	public static void main(String[] args) {
		File s449 = new File("org/s449");
		System.out.println(count(s449));
	}
	private static int count(File what) {
		try {
			int c = 0;
			if (what.isDirectory()) {
				for (File file : what.listFiles(new JAVAfilter()))
					c += count(file);
			} else {
				BufferedReader br = new BufferedReader(new FileReader(what));
				while (br.readLine() != null)
					c++;
				br.close();
			}
			return c;
		} catch (Exception e) {
			return 0;
		}
	}

	private static class JAVAfilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return name.endsWith(".java");
		}
	}
}