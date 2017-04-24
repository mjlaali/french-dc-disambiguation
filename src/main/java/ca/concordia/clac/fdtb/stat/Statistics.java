package ca.concordia.clac.fdtb.stat;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import ca.concordia.clac.fdtb.DefaultFDTBListener;
import ca.concordia.clac.fdtb.FDTBReader;

public class Statistics extends DefaultFDTBListener {
	private SAXParser parser;
	private int dcCnt; 
	private int wordCnt;
	
	public Statistics() throws ParserConfigurationException, SAXException{
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		parser = parserFactory.newSAXParser();
	}

	public Map<String, Integer> getStats(File fdtbFile) throws SAXException, IOException{
		dcCnt = 0;
		wordCnt = 0;
		
		FDTBReader reader = new FDTBReader(this);
		parser.parse(fdtbFile, reader);
		
		Map<String, Integer> stats = new TreeMap<>();
		stats.put("DC", dcCnt);
		stats.put("Word", wordCnt);
		return stats;
	}
	
	@Override
	public void addConnective(List<Integer> connStarts, List<Integer> connEnds) {
		dcCnt += connStarts.size();
	}
	
	@Override
	public void addSent(String sent, String sentId) {
		wordCnt += sent.split(" ").length;
	}
	
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		File[] fdtbFiles = new File[]{
			new File("data/fdtb-project/original/d_sequoia/d_annodis.er.xml"),
			new File("data/fdtb-project/original/d_sequoia/d_frwiki_50.1000.xml"),
			new File("data/fdtb-project/original/fdtb1/fdtb1.xml"),
		};
		
		Statistics statistics = new Statistics();
		for (File file: fdtbFiles){
			System.out.println(file.getName());
			System.out.println(statistics.getStats(file));
		}
	}
}
