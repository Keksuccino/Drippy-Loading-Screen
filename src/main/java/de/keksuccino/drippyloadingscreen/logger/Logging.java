package de.keksuccino.drippyloadingscreen.logger;

import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;

public class Logging {
	
	private static final String NAME = "drippyloadingscreen";
	
	public static boolean printWarnings = true;
	public static boolean printErrors = true;
	public static boolean printInfos = true;
	
	public static void init() {
		
		printWarnings = DrippyLoadingScreen.config.getOrDefault("printwarnings", true);
		
	}
	
	public static void warn(String... out) {
		if (printWarnings) {
			
			printMultiline("WARNING", out);
			
		}
	}
	
	public static void error(String... out) {
		if (printErrors) {
			
			printMultiline("ERROR", out);
			
		}
	}
	
	public static void info(String... out) {
		if (printInfos) {
			
			printMultiline("INFO", out);
			
		}
	}
	
	public static void printMultiline(String type, String... out) {
			
		String head = "############# " + type + " : " + NAME + " #############";
		String tail = "";
		int i = 0;
		while (i < head.length()) {
			tail = tail + "#";
			i++;
		}

		System.out.println(head);

		for (String s : out) {
			System.out.println(s);
		}

		System.out.println(tail);
			
	}
	
	public static void print(String out) {
		System.out.println("[" + NAME + "] " + out);
	}

}
