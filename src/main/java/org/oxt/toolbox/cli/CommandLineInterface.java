package org.oxt.toolbox.cli;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.fop.apps.FOPException;
import org.apache.logging.log4j.Logger;
import org.oxt.toolbox.helpers.AppProperties;
import org.oxt.toolbox.helpers.LogConfigurator;
import org.oxt.toolbox.visualization.VisualizerImpl;

public class CommandLineInterface {
	public static final String FLAG_INPUT_FILE = "-file";
	public static final String FLAG_CREATE_PDF = "-pdf";
	
	public static void main(String[] args) {
		VisualizerImpl viz = new VisualizerImpl();
		try {
			String config = "resources/app.config";
			AppProperties.initializeProperties(config);
			
			Logger logger = LogConfigurator.LogConfig(VisualizerImpl.class);
			logger.info("Start");
			
			Map<String, String> mappedArgs = parseArgs(args);
			
			if(!mappedArgs.containsKey(FLAG_INPUT_FILE)) {
				throw new Exception("no path provided!");
			}
			
			File file = new File(mappedArgs.get(FLAG_INPUT_FILE));
			if(!file.exists()) {
				throw new Exception("file doesnt exist!");
			}

			StringWriter sw = viz.runVisualization(file.getName(), file.getAbsolutePath(),
					AppProperties.prop.getProperty("viz.intermediate.ubl.xsl"),
					AppProperties.prop.getProperty("viz.intermediate.ublcn.xsl"),
					AppProperties.prop.getProperty("viz.intermediate.cii.xsl"),
					AppProperties.prop.getProperty("viz.html.xsl"));
			
			if(mappedArgs.containsKey(FLAG_CREATE_PDF)) {
				writePdf(file, viz);
			} else {
				writeHtml(file, sw);
			}
			
			logger.info("done");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void writeHtml(File input, StringWriter sw) throws IOException {
		File htmlFile = new File(input.getParentFile(), input.getName() + ".html");
		
		try(FileWriter fw = new FileWriter(htmlFile)) {
			fw.write(sw.toString());
		}
	}
	
	public static void writePdf(File input, VisualizerImpl viz) throws FOPException, IOException, TransformerException {
		File pdfFile = new File(input.getParentFile(), input.getName() + ".pdf");
		viz.transformAndSaveToPDF(AppProperties.prop.getProperty("viz.pdf.xsl"), pdfFile.getAbsolutePath());
	}
	
	public static Map<String, String> parseArgs(String[] args) {
		Map<String, String> mappedArgs = new HashMap<>();
		
		for(int i=0; i<args.length; i++) {
			String arg = args[i];
			if(arg == null || arg.isEmpty()) {
				continue;
			}
			
			if(i==0 && !arg.startsWith("-")) {
				mappedArgs.put(FLAG_INPUT_FILE, arg);
			}
			
			if(arg.startsWith("-")) {
				if(args.length <= i + 1) {
					mappedArgs.put(arg, "");
					break;
				}
				
				String value = args[i + 1];
				if(value.startsWith("-")) {
					mappedArgs.put(arg, "");
					continue;
				}
				
				mappedArgs.put(arg, value);
			}
		}
		
		return mappedArgs;
	}
}
