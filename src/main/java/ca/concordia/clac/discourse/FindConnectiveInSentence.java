package ca.concordia.clac.discourse;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.initialize.ConfigurationParameterInitializer;
import org.apache.uima.fit.component.initialize.ExternalResourceInitializer;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDiscourseGoldAnnotator;
import org.cleartk.corpus.conll2015.ConllSyntaxGoldAnnotator;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.ml.weka.WekaStringOutcomeDataWriter;

import ca.concordia.clac.discourse.parser.dc.disambiguation.DiscourseAnnotationFactory;
import ca.concordia.clac.discourse.parser.dc.disambiguation.DiscourseVsNonDiscourseClassifier;
import ca.concordia.clac.ml.classifier.StringClassifierLabeller;
import ca.concordia.clac.uima.engines.LookupInstanceExtractor;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;

public class FindConnectiveInSentence extends DiscourseVsNonDiscourseClassifier{
	public static final String PARAM_TARGET_DC = "targetDc";
	@ConfigurationParameter(name=PARAM_TARGET_DC)
	private String targetDc;
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		ConfigurationParameterInitializer.initialize(this, context);
	    ExternalResourceInitializer.initialize(this, context);
	}
	
	@Override
	public Function<DiscourseConnective, String> getLabelExtractor(JCas aJCas) {
		Function<DiscourseConnective, String> labelExtractor = super.getLabelExtractor(aJCas);
		
		return (dc) -> {
			String res = labelExtractor.apply(dc);
			if (dc.getCoveredText().toLowerCase().equals(targetDc) && res.equals(Boolean.toString(false))){
				List<Sentence> sentences = JCasUtil.selectCovering(Sentence.class, dc);
				if (sentences.size() > 0)
					System.out.println(sentences.get(0).getCoveredText());
			}
			return res;
		};
	}
	
	public static AnalysisEngineDescription getWriterDescription(File dcList, File outputFld, String targetDc) throws ResourceInitializationException, MalformedURLException{
		return StringClassifierLabeller.getWriterDescription(
				FindConnectiveInSentence.class,
				WekaStringOutcomeDataWriter.class, 
				outputFld, 
				LookupInstanceExtractor.PARAM_LOOKUP_FILE_URL, dcList.toURI().toURL().toString(),
				LookupInstanceExtractor.PARAM_ANNOTATION_FACTORY_CLASS_NAME, DiscourseAnnotationFactory.class.getName(),
				PARAM_TARGET_DC,
				targetDc
				);
	}
	
	public static void main(String[] args) throws UIMAException, MalformedURLException, IOException {
		String dataFld = "data/pdtb/";
		String rawDir = dataFld + "raw";
		String parseTreeFile = dataFld + "pdtb-parses.json";
		String discourseFile = dataFld + "pdtb-data.json";
		String dcHeadList = dataFld + "dcHeadList.txt";
		
		String outputDir = "pdtb";
		
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(TextReader.class, 
				TextReader.PARAM_SOURCE_LOCATION, rawDir, 
				TextReader.PARAM_LANGUAGE, "en",
				TextReader.PARAM_PATTERNS, "wsj_*");
		AnalysisEngineDescription conllSyntaxJsonReader = 
				ConllSyntaxGoldAnnotator.getDescription(new File(parseTreeFile));

		AnalysisEngineDescription conllGoldJsonReader = 
				ConllDiscourseGoldAnnotator.getDescription(new File(discourseFile));

		File dcList = new File(dcHeadList);
		File featureFile = new File(new File("outputs/"), outputDir);
		if (featureFile.exists())
			FileUtils.deleteDirectory(featureFile);
		SimplePipeline.runPipeline(reader,
				conllSyntaxJsonReader, 
				conllGoldJsonReader, 
				getWriterDescription(dcList, featureFile, "because")
				);
	}
}
