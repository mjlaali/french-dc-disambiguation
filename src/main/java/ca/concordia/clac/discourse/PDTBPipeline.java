package ca.concordia.clac.discourse;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDiscourseGoldAnnotator;
import org.cleartk.corpus.conll2015.ConllSyntaxGoldAnnotator;

import ca.concordia.clac.discourse.parser.dc.disambiguation.DiscourseVsNonDiscourseClassifier;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;

public class PDTBPipeline {
	
	public static void main(String[] args) throws ResourceInitializationException, UIMAException, MalformedURLException, IOException {
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
				DiscourseVsNonDiscourseClassifier.getWriterDescription(dcList, featureFile)
				);
	}

}
