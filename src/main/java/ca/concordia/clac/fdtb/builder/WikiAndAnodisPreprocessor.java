package ca.concordia.clac.fdtb.builder;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.cleartk.corpus.conll2015.ConllSyntaxGoldAnnotator;
import org.xml.sax.SAXException;

import ca.concordia.clac.fdtb.FDTBTextReader;
import ca.concordia.clac.uima.Utils;
import de.tudarmstadt.ukp.dkpro.core.berkeleyparser.BerkeleyParser;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;

/**
 * Split the raw text file to different files for French Wikipedia and Annodis. 
 * This class also parse texts with Berkeley parser.
 * @author majid
 *
 */
public class WikiAndAnodisPreprocessor {

	public void build(File rawTextFile, File discourseAnnFile, File outputDir) throws SAXException, IOException, ParserConfigurationException{
		Map<String, String> idToLine = getLines(rawTextFile);

		//create raw text files from the French discourse treebank.
		FDTBTextReader fdtbTextReader = new FDTBTextReader();
		File rawFld = new File(outputDir, "temp");
		rawFld.mkdirs();
		Map<String, List<String>> filesSentIds = fdtbTextReader.run(discourseAnnFile, rawFld);
		
		for (Entry<String, List<String>> fileSentIds: filesSentIds.entrySet()){
			PrintStream psText = new PrintStream(new FileOutputStream(new File(outputDir, fileSentIds.getKey())), true, StandardCharsets.UTF_8.name());
			for (String id: fileSentIds.getValue()){
				String line = idToLine.get(id);
				if (line == null){
					psText.close();
					throw new RuntimeException("Line " + id + " cannot be found");
				}
				psText.println(line);
			}
			psText.close();
		}
		
		FileUtils.deleteDirectory(rawFld);
		
	}

	private Map<String, String> getLines(File rawTextFile) throws IOException {
		Map<String, String> idToLine = new HashMap<>();
		List<String> lines = FileUtils.readLines(rawTextFile, "UTF-8");
		for (int i = 0; i < lines.size(); i += 2){
			if (idToLine.put(lines.get(i), lines.get(i + 1)) != null){
				throw new RuntimeException();
			}
		}
		return idToLine;
	}
	
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException, UIMAException {
		File rawTextFile = new File("data/fdtb-project/extra/d_sequoia.txt");
		File[] discourseAnnFiles = new File[]{
				new File("data/fdtb-project/original/d_sequoia/d_annodis.er.xml"),
				new File("data/fdtb-project/original/d_sequoia/d_frwiki_50.1000.xml"),
				};
		File outputDir = new File("data/fdtb-project/conll-format");
		
		for (File discourseAnnFile: discourseAnnFiles){
			String name = discourseAnnFile.getName();
			int idx = name.indexOf('_', "d_".length());
			if (idx == -1)
				idx = name.indexOf('.');
			File dir = new File(outputDir, name.substring(2, idx));
			File rawTextDir = new File(dir, "raw");
			new WikiAndAnodisPreprocessor().build(rawTextFile, discourseAnnFile, rawTextDir);
			
			runPipeline(rawTextDir, new File(dir, "xmi"));
			
		}
		
		File datasetLocation = outputDir;
		File fdtbRawFiles = new File(datasetLocation, "fdtb/raw");
		File fdtbSyntaxFile = new File(datasetLocation, "fdtb/parses.json");
		File fdtbOutput = new File(outputDir, "fdtb/xmi");
		runPipelineForFDTB(fdtbRawFiles, fdtbSyntaxFile, fdtbOutput);
		System.out.println("WikiAndAnodisPreprocessor.main()");
	}

	public static void runPipeline(File rawTextDir, File output) throws UIMAException, IOException {
		CollectionReader reader = CollectionReaderFactory.createReader(TextReader.class,
				TextReader.PARAM_SOURCE_LOCATION, rawTextDir,
				TextReader.PARAM_LANGUAGE, "fr", 
				TextReader.PARAM_PATTERNS, "*");
		
		AnalysisEngineDescription sentenceDetector = CharbasedSentBoundaryDetector.getEngineDescription();
		
		AnalysisEngineDescription tokenzier = createEngineDescription(OpenNlpSegmenter.class,
				OpenNlpSegmenter.PARAM_LANGUAGE, "fr", 
				OpenNlpSegmenter.PARAM_WRITE_SENTENCE, false,
				OpenNlpSegmenter.PARAM_SEGMENTATION_MODEL_LOCATION, "classpath:/fr-sent.bin", 
				OpenNlpSegmenter.PARAM_TOKENIZATION_MODEL_LOCATION, "classpath:/fr-token.bin"
				);
		AnalysisEngineDescription parser = createEngineDescription(BerkeleyParser.class, 
				BerkeleyParser.PARAM_LANGUAGE, "fr", 
				BerkeleyParser.PARAM_MODEL_LOCATION, "classpath:/fra_sm5.gr", 
				BerkeleyParser.PARAM_WRITE_POS, true, 
				BerkeleyParser.PARAM_READ_POS, false);
		
		AnalysisEngineDescription xmiWriter = createEngineDescription(XmiWriter.class, 
				XmiWriter.PARAM_TARGET_LOCATION, output,
				XmiWriter.PARAM_OVERWRITE, true);
		
		Utils.runWithProgressbar(reader, sentenceDetector, tokenzier, parser, xmiWriter);
		
	}
	
	public static void runPipelineForFDTB(File rawTextDir, File syntaxFile, File output) throws UIMAException, IOException {
		CollectionReader reader = CollectionReaderFactory.createReader(TextReader.class,
				TextReader.PARAM_SOURCE_LOCATION, rawTextDir,
				TextReader.PARAM_LANGUAGE, "fr", 
				TextReader.PARAM_PATTERNS, "*");

		AnalysisEngineDescription addSyntax = ConllSyntaxGoldAnnotator.getDescription(syntaxFile);
		AnalysisEngineDescription xmiWriter = createEngineDescription(XmiWriter.class, 
				XmiWriter.PARAM_TARGET_LOCATION, output,
				XmiWriter.PARAM_OVERWRITE, true);
		
		Utils.runWithProgressbar(reader, addSyntax, xmiWriter);

	}
}
