package ca.concordia.clac.fdtb.stat;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import ca.concordia.clac.fdtb.DefaultFDTBListener;
import ca.concordia.clac.fdtb.FDTBReader;
import ca.concordia.clac.lexconn.DefaultLexconnReader;

public class Statistics extends DefaultFDTBListener {
	private SAXParser parser;
	private int dcCnt; 
	private int wordCnt;
	
	private String sent;
	Map<String, Integer> dcsCnt = new HashMap<>();
	private final Map<String, String> formToId;

	public Statistics(final Map<String, String> formToId) throws ParserConfigurationException, SAXException{
		this.formToId = formToId;
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
	
	public Map<String, Integer> getDcsCnt() {
		return dcsCnt;
	}
	
	@Override
	public void addConnective(List<Integer> connStarts, List<Integer> connEnds) {
		dcCnt += connStarts.size();
		for (int i = 0; i < connStarts.size(); i++){
			String dc = sent.substring(connStarts.get(i), connEnds.get(i)).toLowerCase().trim().replace('_', ' ');
			String id = formToId.get(dc);
			if (id == null)
				System.err.println("Statistics.addConnective()-The connective is not listed in LEXCONN:" + dc);
			else {
				Integer cnt = dcsCnt.get(id);
				if (cnt == null)
					cnt = 0;
				dcsCnt.put(id, cnt + 1);
			}
		}
	}
	
	@Override
	public void addSent(String sent, String sentId) {
		this.sent = sent;
		wordCnt += sent.split(" ").length;
	}
	
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		File[] fdtbFiles = new File[]{
			new File("data/fdtb-project/original/d_sequoia/d_annodis.er.xml"),
			new File("data/fdtb-project/original/d_sequoia/d_frwiki_50.1000.xml"),
			new File("data/fdtb-project/original/fdtb1/fdtb1.xml"),
		};
		
		DefaultLexconnReader reader = DefaultLexconnReader.getLexconnMap();
		Statistics statistics = new Statistics(reader.getFormToId());
		for (File file: fdtbFiles){
			System.out.println(file.getName());
			System.out.println(statistics.getStats(file));
		}
		
		System.out.println(statistics.getDcsCnt());
	}
}
