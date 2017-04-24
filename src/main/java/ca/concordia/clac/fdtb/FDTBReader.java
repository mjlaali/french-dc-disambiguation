package ca.concordia.clac.fdtb;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class FDTBReader extends DefaultHandler{
	private static final String SENT = "SENT";
	private static final String CONN = "CONN";
	private static final String ARTICLE = "ARTICLE";
	private static final String ID = "id";
	
	private String sentId;
	private StringBuilder buffer = new StringBuilder();
	private List<Integer> connStarts;
	private List<Integer> connEnds;
	private FDTBListener listener;

	public FDTBReader(FDTBListener listener) throws FileNotFoundException {
		this.listener = listener;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		
		if (qName.equals(SENT)){
			buffer.setLength(0);
			sentId = attributes.getValue(ID);
			connStarts = new ArrayList<Integer>();
			connEnds = new ArrayList<Integer>();
		} else if (qName.equals(CONN)){
			connStarts.add(buffer.length());
		} else if (qName.equals(ARTICLE)){
			listener.startArticle(attributes.getValue(ID));
		}
	}
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		buffer.append(ch, start, length);
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (qName.equals(SENT)){
			listener.addSent(buffer.toString(), sentId);
			listener.addConnective(connStarts, connEnds);
		} else if (qName.equals(CONN)){
			connEnds.add(buffer.length());
		} else if (qName.equals(ARTICLE)){
			listener.endArticle();
		}
	}
	
	@Override
	public void endDocument() throws SAXException {
		listener.finished();
	}
}
