package ca.concordia.clac.discourse;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.cleartk.eval.EvaluationConstants;
import org.cleartk.ml.jar.JarClassifierBuilder;

import ca.concordia.clac.batch_process.BatchProcess;
import ca.concordia.clac.lexconn.DefaultLexconnReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;

public class TrainModel {
	public void train() throws Exception{
		String tag = FDTBPipelineFactory.useBerkeleyParser ? "berkely" : "gold";
		File output = new File(String.format("outputs/fdtb-%s/", tag));
		File modelDir = new File(output, "model/");
		modelDir.mkdirs();
		BatchProcess process = new FDTBPipelineFactory().getInstance(new File(output, "process"), FDTBPipelineFactory.useBerkeleyParser);
		
		File xmiOutput = new File("data/fdtb-project/conll-format/fdtb/xmi-" + tag);
		AnalysisEngineDescription xmiWriter = createEngineDescription(XmiWriter.class, 
				XmiWriter.PARAM_TARGET_LOCATION, xmiOutput, 
				XmiWriter.PARAM_OVERWRITE, true);
		process.addProcess("save", xmiWriter); 
				
		process.addProcess("train", EvaluationConstants.GOLD_VIEW, 
				FrConnectiveClassifier.getWriterDescription(modelDir, new File(DefaultLexconnReader.LEXCONN_FILE)));
		
		process.clean("train");
		process.run();
		
		FileUtils.deleteDirectory(modelDir);
		trainModel(modelDir);
	}
	
	public static void trainModel(File modelDir) throws Exception {
		if (!modelDir.exists())
			modelDir.mkdirs();
		JarClassifierBuilder.trainAndPackage(modelDir, "weka.classifiers.trees.J48", "-C 0.25 -M 2");
//		Train.main(modelDir, "1000", "5");
	}
	

	public static void main(String[] args) throws Exception {
		new TrainModel().train();
		System.out.println("TrainModel.main(): Done.");
	}
}
