package ca.concordia.clac.discourse;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.component.ViewTextCopierAnnotator;
import org.cleartk.corpus.conll2015.ConllSyntaxGoldAnnotator;
import org.cleartk.eval.EvaluationConstants;

import ca.concordia.clac.batch_process.BatchProcess;
import ca.concordia.clac.fdtb.FDTBGoldAnnotator;
import ca.concordia.clac.fdtb.FDTBPath;
import ca.concordia.clac.uima.engines.ViewAnnotationCopier;
import de.tudarmstadt.ukp.dkpro.core.berkeleyparser.BerkeleyParser;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;

public class FDTBPipelineFactory {
	public static final boolean useBerkeleyParser = false;
	public static final String INIT_STEP = "init";
	public static final String GOLD_ANNOTATION_STEP = "gold";
	public static final String PARSE_TEXTS = "parse";
	
	private final File baseFld;
	
	public FDTBPipelineFactory() {
		baseFld = new File(System.getProperty("user.dir"));
	}

	public FDTBPipelineFactory(File baseFld) {
		this.baseFld = baseFld;
	}
	
	public BatchProcess getInstance(File outputFld, boolean useBerkeleyParser) throws UIMAException, IOException{
		File fdtbRawFld = new File(baseFld, FDTBPath.CONLL_RAWTEXT);
		File syntaxFile = new File(baseFld, FDTBPath.CONLL_SYNTAXFILE);
		
		if (useBerkeleyParser)
			return getPipeWithBerkelyParser(fdtbRawFld, outputFld);
		return getPipeWithGoldSyntaxTree(fdtbRawFld, syntaxFile, outputFld);
	}

	public BatchProcess getPipeWithGoldSyntaxTree(File inputDir, File syntaxFile, File outputDir) throws IOException, UIMAException{
		return getPipeWithGoldSyntaxTree(new File(baseFld, FDTBPath.FDTB_FILE), inputDir, syntaxFile, outputDir);
	}
	
	public BatchProcess getPipeWithGoldSyntaxTree(File fdtb, File inputDir, File syntaxFile, File outputDir) throws IOException, UIMAException{
		BatchProcess batchProcess;
		if (!(fdtb.exists() && inputDir.exists() && syntaxFile.exists()))
			throw new RuntimeException("One of the files are not exist: " + fdtb.getAbsolutePath() + ", " + inputDir.getAbsolutePath() + ", " + syntaxFile.getAbsolutePath());
		
		batchProcess = new BatchProcess(inputDir, outputDir, "fr", "*");

		//Creates two views, default and gold views, containing texts and syntax annotations
		batchProcess.addProcess(INIT_STEP, 
				createEngineDescription(ViewTextCopierAnnotator.class, 
						ViewTextCopierAnnotator.PARAM_SOURCE_VIEW_NAME, CAS.NAME_DEFAULT_SOFA, 
						ViewTextCopierAnnotator.PARAM_DESTINATION_VIEW_NAME, EvaluationConstants.GOLD_VIEW),
				ConllSyntaxGoldAnnotator.getDescription(syntaxFile),
				createEngineDescription(ViewAnnotationCopier.class, 
						ViewAnnotationCopier.PARAM_TARGET_VIEW_NAME, EvaluationConstants.GOLD_VIEW)
				);
		
		//add discourse annotation to the GOLD view
		batchProcess.addProcess(GOLD_ANNOTATION_STEP, EvaluationConstants.GOLD_VIEW, 
				FDTBGoldAnnotator.getDescription(fdtb.getAbsolutePath()));
		return batchProcess;
	}
	
	public BatchProcess getPipeWithBerkelyParser(File inputDir, File outputDir) throws IOException, UIMAException{
		return getPipeWithBerkelyParser(new File(baseFld, FDTBPath.FDTB_FILE), inputDir, outputDir);
	}
	public BatchProcess getPipeWithBerkelyParser(File fdtb, File inputDir, File outputDir) throws IOException, UIMAException{
		BatchProcess batchProcess;
		
		batchProcess = new BatchProcess(inputDir, outputDir, "fr", "*");

		batchProcess.addProcess(INIT_STEP, 
				createEngineDescription(ViewTextCopierAnnotator.class, 
						ViewTextCopierAnnotator.PARAM_SOURCE_VIEW_NAME, CAS.NAME_DEFAULT_SOFA, 
						ViewTextCopierAnnotator.PARAM_DESTINATION_VIEW_NAME, EvaluationConstants.GOLD_VIEW)
				);
		
		batchProcess.addProcess(PARSE_TEXTS,
				createEngineDescription(OpenNlpSegmenter.class,
						OpenNlpSegmenter.PARAM_LANGUAGE, "fr", 
						OpenNlpSegmenter.PARAM_SEGMENTATION_MODEL_LOCATION, "classpath:/fr-sent.bin", 
						OpenNlpSegmenter.PARAM_TOKENIZATION_MODEL_LOCATION, "classpath:/fr-token.bin"
						),
				createEngineDescription(BerkeleyParser.class, 
						BerkeleyParser.PARAM_LANGUAGE, "fr", 
						BerkeleyParser.PARAM_MODEL_LOCATION, "classpath:/fra_sm5.gr", 
						BerkeleyParser.PARAM_WRITE_POS, true, 
						BerkeleyParser.PARAM_READ_POS, false),
				createEngineDescription(ViewAnnotationCopier.class, 
						ViewAnnotationCopier.PARAM_TARGET_VIEW_NAME, EvaluationConstants.GOLD_VIEW)
				);
		
		
		batchProcess.addProcess(GOLD_ANNOTATION_STEP, EvaluationConstants.GOLD_VIEW, 
				FDTBGoldAnnotator.getDescription(fdtb.getAbsolutePath()));
		return batchProcess;
	}
	
}
