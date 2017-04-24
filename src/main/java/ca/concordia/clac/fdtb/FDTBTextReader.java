package ca.concordia.clac.fdtb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

public class FDTBTextReader extends DefaultFDTBListener{
	private PrintStream psText = null;
	private Map<String, List<String>> fileLineIds = new TreeMap<String, List<String>>();
	private List<String> currentFileLineIds;
	private File fld;
	
	public Map<String, List<String>> run(File file, File fld) throws SAXException, IOException, ParserConfigurationException{
		this.fld = fld;
		if (fld.exists())
			FileUtils.deleteDirectory(fld);
		fld.mkdirs();
		FDTBReader reader = new FDTBReader(this);
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		SAXParser parser = parserFactory.newSAXParser();
		parser.parse(file, reader);
		return fileLineIds;
	}
	
	@Override
	public void addSent(String sent, String sentId) {
		psText.println(sent);
		currentFileLineIds.add(sentId);
	}

	@Override
	public void startArticle(String id) {
		try {
			currentFileLineIds = new ArrayList<String>();
			fileLineIds.put(id, currentFileLineIds);
			if (psText != null){
				psText.close();
			}
			psText = new PrintStream(new FileOutputStream(new File(fld, id)), true, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void finished() {
		psText.close();
	}

	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException {
		FDTBTextReader fdtbTextReader = new FDTBTextReader();
		fdtbTextReader.run(new File(FDTBPath.FDTB_FILE), new File(FDTBPath.CONLL_RAWTEXT));
	}
}
