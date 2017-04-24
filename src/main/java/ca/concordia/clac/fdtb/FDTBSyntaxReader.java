package ca.concordia.clac.fdtb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.cleartk.util.treebank.TreebankFormatParser;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ca.concordia.clac.conll.Sentence;
import ca.concordia.clac.conll.SyntaxInfo;
import ca.concordia.clac.conll.Word;
import ca.concordia.clac.ftb.FTBListener;
import ca.concordia.clac.ftb.FTBReader;

/**
 * Generate raw texts and parses.json file for the FDTB.
 * @author majid
 *
 */
public class FDTBSyntaxReader implements FTBListener{
	private Map<String, String> sentParse = new TreeMap<String, String>();
	private File fdtbFile;
	private File ftbFld;
	public FDTBSyntaxReader( File fdtbFile, File ftbFld) {
		this.fdtbFile = fdtbFile;
		this.ftbFld = ftbFld;
	}

	public void run(File jsonSyntaxFile, File rawFld) throws SAXException, IOException, ParserConfigurationException{
		File errFile = new File("outputs/notsynced.txt");
		PrintStream err = new PrintStream(new FileOutputStream(errFile));
		int cnt = 0;

		//get syntax tree from the French treebank.
		FTBReader reader = new FTBReader();
		Collection<File> listFiles = FileUtils.listFiles(ftbFld, new String[]{"xml"}, true);
		for (File file: listFiles){
			reader.parse(file, this);
		}

		//create raw text files from the French discourse treebank.
		FDTBTextReader fdtbTextReader = new FDTBTextReader();
		Map<String, List<String>> fileSentIds = fdtbTextReader.run(fdtbFile, rawFld);

		Map<String, SyntaxInfo> fileSyntaxInfos = new LinkedHashMap<String, SyntaxInfo>();
		for (Entry<String, List<String>> aFileSentIds: fileSentIds.entrySet()){
			File file = new File(rawFld, aFileSentIds.getKey());
			List<String> lines = FileUtils.readLines(file);
			String fileTxt = FileUtils.readFileToString(file);
			SyntaxInfo fileSyntaxInfo = new SyntaxInfo();

			int offset = 0;
			List<String> sentIds = aFileSentIds.getValue();
			for (int i = 0; i < sentIds.size(); i++){
				String sentId = sentIds.get(i);
				Sentence sent = null;
				try{
					sent = new Sentence(sentParse.get(sentId), offset, lines.get(i));
				}catch (Exception ep1){
					try{
						sent = new Sentence(sentParse.get(sentId).replace("_", ") (N "), offset, lines.get(i));
					} catch (Exception ep){
//						ep.printStackTrace();
						err.println("" + ++cnt + ":" + sentId);
						System.err.println("" + ++cnt + ":" + sentId);
						err.println(" FDTB: " + lines.get(i));
						System.err.println(sentParse.get(sentId));
						err.println("  FTB: " + TreebankFormatParser.inferPlainText(sentParse.get(sentId)));
						err.println("Parse: " + sentParse.get(sentId));
						err.println();
						sent = new Sentence();
						String fakeParseTree = createFakeParseTree(lines.get(i));
						sent = new Sentence(fakeParseTree, offset, lines.get(i));
					}
				}

				check(sent, fileTxt);
				fileSyntaxInfo.sentences.add(sent);
				offset += lines.get(i).length() + 1;
			}
			fileSyntaxInfos.put(aFileSentIds.getKey(), fileSyntaxInfo);
		}
		err.close();
		System.err.println(FileUtils.readFileToString(errFile));
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		PrintStream ps = new PrintStream(new FileOutputStream(jsonSyntaxFile), false, "UTF-8");
		ps.println(gson.toJson(fileSyntaxInfos));
		ps.close();
	}

	private String createFakeParseTree(String sent) {
		String tokenized = sent.replace("(", " ( ").replace(")", " ) ").trim().replaceAll("  +", " ");
		String[] words = tokenized.split(" ");
		StringBuilder buffer = new StringBuilder();

		buffer.append("(TOP");
		for (String word: words){
			switch (word) {
			case "(":
				word = "-LRB-";
				break;
			case ")":
				word = "-RRB-";
				break;
			default:
				break;
			}
			buffer.append(String.format(" (%s %s)", word, word));
		}
		buffer.append(")");
		return buffer.toString();
	}

	private void check(Sentence sent, String txt) {
		for (Object[] wordInfo: sent.words){
			String strWord = FTBReader.escaped((String) wordInfo[0]);
			Word word = (Word) wordInfo[1];

			String textSpan =  FTBReader.escaped(txt.substring(word.CharacterOffsetBegin, word.CharacterOffsetEnd));
			if (!textSpan.equals(strWord)){
				throw new RuntimeException(String.format("Words are not synced: <%s> != <%s>", strWord, textSpan));
			}
		}
	}

	@Override
	public void sent(String sentId, String parse, List<String> words, List<String> poses) {
		sentParse.put(sentId, parse);
	}


	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException {
		FDTBSyntaxReader fdtbSyntaxReader = new FDTBSyntaxReader(new File(FDTBPath.FDTB_FILE),
				new File(FDTBPath.FTB_DIR));
		fdtbSyntaxReader.run(new File(FDTBPath.CONLL_SYNTAXFILE), 
				new File(FDTBPath.CONLL_RAWTEXT) 
				);
		System.out.println("FDTBSyntaxReader.main()");
	}

}
