package ca.concordia.clac.lexconn;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class LexconnParser extends DefaultHandler{
	public static final String CONNECTURE_ELEMENT = "connecteur";
	public static final String FORM_ELEMENT = "forme";
	public static final String EXAMPLE_ELEMENT = "exemple";

	public static final String TYPE_ATT = "type";
	public static final String ID_ATT = "id";
	public static final String CAT_ATT = "cat";
	public static final String RELATIONS_ATT = "relations";

	private LexconnHandler lexconnHandler;
	private List<String> forms;
	private List<String> examples;
	private List<String> relations;
	private String type;
	private String cat;
	private String id;

	private StringBuilder buffer = new StringBuilder();
	private boolean isForm = false;
	private boolean isExample = false;

	public void parse(File lexconn, LexconnHandler lexconnHandler) throws ParserConfigurationException, SAXException, IOException{
		parse(lexconn.toURI().toURL(), lexconnHandler);
	}
	
	public void parse(URL lexconn, LexconnHandler lexconnHandler) throws ParserConfigurationException, SAXException, IOException{
		SAXParserFactory spf = SAXParserFactory.newInstance();
	    SAXParser saxParser = spf.newSAXParser();
	    this.lexconnHandler = lexconnHandler;
	    InputStream lexconnStream = lexconn.openStream();
	    saxParser.parse(lexconnStream, this);
	    lexconnStream.close();
	}

	@Override
	public void startElement(String namespaceURI,
			String localName,
			String qName, 
			Attributes atts)
					throws SAXException {
		switch (qName){
		case CONNECTURE_ELEMENT:
			forms = new ArrayList<>();
			examples = new ArrayList<>();
			type = atts.getValue(TYPE_ATT);
			cat = atts.getValue(CAT_ATT);
			id = atts.getValue(ID_ATT);
			try{
				relations = Arrays.asList(atts.getValue(RELATIONS_ATT).split(","));
			} catch (NullPointerException e){
				relations = Collections.emptyList();
			}
			break;
		case FORM_ELEMENT:
			isForm = true;
			buffer.setLength(0);
			break;
		case EXAMPLE_ELEMENT:
			isExample  = true;
			buffer.setLength(0);
			break;
		default:
		}
	}

	@Override
	public void characters(char[] ch,
            int start,
            int length) throws SAXException {
		if (isForm | isExample)
			buffer.append(ch, start, length);
	}

	@Override
	public void endElement(String namespaceURI,
			String localName,
			String qName) throws SAXException {
		switch (qName) {
		case CONNECTURE_ELEMENT:
			lexconnHandler.process(forms, examples, relations, type, cat, id);
			break;

		case FORM_ELEMENT:
			isForm = false;
			forms.add(buffer.toString().trim());
			break;

		case EXAMPLE_ELEMENT:
			isExample  = false;
			examples.add(buffer.toString().trim());
			break;
		default:
			break;
		}
		
	}
	
	@Override
	public void endDocument() throws SAXException {
		lexconnHandler.endDocument();
	}

}
