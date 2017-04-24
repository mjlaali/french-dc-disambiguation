package ca.concordia.clac.fdtb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

public class SentenceWriter implements FDTBListener {
	private PrintStream ps;
	
	public SentenceWriter(File file) throws FileNotFoundException {
		ps = new PrintStream(file);
	}

	public void addSent(String sent, String sentId) {
		ps.println(sent);
	}

	public void addConnective(List<Integer> connStarts, List<Integer> connEnds) {
		
	}

	public void startArticle(String id) {
		
	}

	public void endArticle() {
		
	}
	
	public void finished() {
		ps.close();
	}

	public static void main(String[] args) throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		SAXParser parser = parserFactory.newSAXParser();
		File file = new File("data/sents.txt");
		parser.parse(new File("data/fdtb1/fdtb1.xml"), new FDTBReader(new SentenceWriter(file)));
	}
	
}
