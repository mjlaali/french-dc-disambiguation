package ca.concordia.clac.fdtb;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.metadata.CpeDescriptorException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseConnective;
import org.xml.sax.SAXException;

import ca.concordia.clac.batch_process.BatchProcess;
import ca.concordia.clac.discourse.FDTBPipelineFactory;
import ca.concordia.clac.uima.engines.Tools;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class FDTBGoldAnnotator extends JCasAnnotator_ImplBase{
	public static final String PARAM_FDTB_XML_FILE = "PARAM_FDTB_XML_FILE";
	@ConfigurationParameter(
			name = PARAM_FDTB_XML_FILE,
			description = "The xml file of the FDTB",
			mandatory = true)
	private String fdtbXmlFile;

	private Map<String, List<Integer>> connectiveStarts;
	private Map<String, List<Integer>> connectiveEnds;
	private Map<String, String> docTexts;

	public static AnalysisEngineDescription getDescription(String fdtbXmlFile) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(
				FDTBGoldAnnotator.class,
				PARAM_FDTB_XML_FILE,
				fdtbXmlFile);
	}

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);

		FDTBDiscourseConnectiveReader listener = new FDTBDiscourseConnectiveReader();
		try {
			FDTBReader reader = new FDTBReader(listener);
			SAXParserFactory parser = SAXParserFactory.newInstance();
			SAXParser newSAXParser = parser.newSAXParser();
			newSAXParser.parse(new File(fdtbXmlFile), reader);
			connectiveStarts = listener.getConnectiveStarts();
			connectiveEnds = listener.getConnectiveEnds();
			docTexts = listener.getDocTexts();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		String docName = Tools.getDocName(aJCas);
		
		String annText = docTexts.get(docName);
		String jcasDocText = aJCas.getDocumentText();
		Map<Integer, Integer> mapAnnPositions = buildMapping(jcasDocText, annText);

		List<Integer> begins = connectiveStarts.get(docName);
		List<Integer> ends = connectiveEnds.get(docName);
		for (int conn = 0; conn < begins.size(); conn++){
			DiscourseConnective discourseConnective = new DiscourseConnective(aJCas);
			int begin = mapAnnPositions.get(begins.get(conn));
			int end = mapAnnPositions.get(ends.get(conn));
			List<Token> tokens = JCasUtil.selectCovered(aJCas, Token.class, 
					begin, end);
			if (!jcasDocText.substring(begin, end).replaceAll("[ _]", "").equals(annText.substring(begins.get(conn), ends.get(conn)).replaceAll("[ _]", "")))
				throw new RuntimeException("out of sync: '" + jcasDocText.substring(begin, end) + "'<>'" + annText.substring(begins.get(conn), ends.get(conn)) + "'");
			if (tokens.size() == 0){
				int idx = 0;
				tokens = new ArrayList<Token>(JCasUtil.select(aJCas, Token.class));
				for (Token token: tokens){
					if (token.getBegin() < begin)
						++idx;
					else
						break;
				}
				
				throw new RuntimeException("Token size is zero for the connective <" + annText.substring(begins.get(conn), ends.get(conn)) + ">" + idx);
			}
			TokenListTools.initTokenList(discourseConnective, tokens);
			discourseConnective.addToIndexes();
		}

	}

	private Map<Integer, Integer> buildMapping(String documentText, String annText) {
		documentText = documentText.replace('_', ' ');
		annText = annText.replace('_', ' ');
		int docPos = 0;
		Map<Integer, Integer> mapAnnPositions = new HashMap<>();
		
		for (int annPos = 0; annPos < annText.length(); annPos++){
			mapAnnPositions.put(annPos, docPos);
			if (documentText.charAt(docPos) == annText.charAt(annPos)){
				++docPos;
			} 
		}
		
		return mapAnnPositions;
		
	}

	public static void main(String[] args) throws UIMAException, IOException, SAXException, CpeDescriptorException {
		File fdtbConllRawFld = new File("data/fdtb/test/raw");
		File syntaxFile = new File("data/fdtb/test/pdtb-parses.json");
		File outputFld = new File("outputs/test/FDTBGoldAnnotator");

		BatchProcess process = new FDTBPipelineFactory().getPipeWithGoldSyntaxTree(fdtbConllRawFld, syntaxFile, outputFld);

		process.clean();
		process.run();

	}


}
