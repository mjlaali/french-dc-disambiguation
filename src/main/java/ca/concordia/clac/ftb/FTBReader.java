package ca.concordia.clac.ftb;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

enum FTBElement{
	SENT, NON, w
}

public class FTBReader extends DefaultHandler{
	private static SAXParserFactory parserFactory = SAXParserFactory.newInstance();
	
	private FTBListener listener;
	
	private String sentId;
	private File parsedFile;
	private StringBuilder parse = new StringBuilder();
	private StringBuilder buffer = new StringBuilder();
	private List<String> words;
	private List<String> poses;
	
	private String wordCat;
	

	public void parse(String aParse, FTBListener ftbListener) throws ParserConfigurationException, SAXException, IOException {
		this.listener = ftbListener;
		InputStream stream = new ByteArrayInputStream(aParse.getBytes(StandardCharsets.UTF_8));
		
		SAXParser parser = parserFactory.newSAXParser();
		parser.parse(stream, this);
	}
	
	public void parse(File file, FTBListener ftbListener) throws ParserConfigurationException, SAXException, IOException {
		SAXParser parser = parserFactory.newSAXParser();
		this.listener = ftbListener;
		
		this.parsedFile = file;
		parser.parse(file, this);
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		FTBElement element = getElement(qName);

		switch (element) {
		case SENT:
			sentId = attributes.getValue("nb");
			parse.setLength(0);
			words = new ArrayList<String>();
			poses = new ArrayList<String>();
			openConstituent("S");
			break;
		case w:
			buffer.setLength(0);
			String cat = attributes.getValue("cat");
			String lemma = attributes.getValue("lemma");
			String escaped = escaped(lemma);
			if (cat == null)
				cat = attributes.getValue("catint");
			if (lemma != null && !escaped.equals(lemma))
				cat = escaped;
			wordCat = cat;
			break;
		default:
			openConstituent(qName);
			break;
		}
		
	}

	
	public static String escaped(String word){
		if (word == null)
			return word;
	
		word = word.replace("(", "-LRB-").replace(")", "-RRB-").replace("\u00A0","");
		switch (word) {
		case "(":
			word = "-LRB-";
			break;
		case ")":
			word = "-RRB-";
			break;
		case "f": 	//typically FDTB replace f with PP. I do not know why! but this keep the error minimom
			word = "PP";
			break;
		default:
			break;
		}
		return word.replace(" ", "_");
	}

	private void openConstituent(String qName) {
		parse.append("(");
		parse.append(qName);
		parse.append(" ");
	}

	private FTBElement getElement(String qName) {
		FTBElement element = FTBElement.NON;
		try {
			element = FTBElement.valueOf(qName);
		} catch (IllegalArgumentException e) {
		}
		return element;
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		buffer.append(ch, start, length);
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		FTBElement element = getElement(qName);
		switch (element) {
		case SENT:
			String name = "";
			if (parsedFile != null){
				name = parsedFile.getName();
				name = name.substring(0, name.indexOf('.')) + "-";
			}
			closeConstituent("");
			String parseTree = parse.toString().trim();
			listener.sent(name + sentId, parseTree, words, poses);
			break;
		case w:
			String word = buffer.toString().trim();
			word = escaped(word);
			if (word.equals("à "))
				System.out.println("FTBReader.endElement()");
			buffer.setLength(0);
			if (!word.isEmpty()){	//do not include empty words
				poses.add(wordCat);
				openConstituent(wordCat);
				closeConstituent(word);
				words.add(word);
			}
			break;
		default:
			closeConstituent("");
			break;
		}
	}

	private void closeConstituent(String terminal) {
		parse.append(terminal + ") ");
	}
		
}
