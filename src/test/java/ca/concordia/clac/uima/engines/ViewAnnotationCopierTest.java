package ca.concordia.clac.uima.engines;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.metadata.CpeDescriptorException;
import org.apache.uima.fit.component.ViewTextCopierAnnotator;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.junit.Test;
import org.xml.sax.SAXException;

import ca.concordia.clac.batch_process.BatchProcess;

public class ViewAnnotationCopierTest {
	File inputDir = new File("resources/test/ViewAnnotationCopierTest/input");
	File inputFile = new File(inputDir, "a.txt");
	File outDir = new File("outputs/test/ViewAnnotationCopierTest/output");


	@Test
	public void checkForNullPointerException() throws UIMAException, IOException, SAXException, CpeDescriptorException{
		String targetView = "GOLD_VIEW";

		BatchProcess batchProcess;

		batchProcess = new BatchProcess(inputDir, outDir, "fr", "*");
		batchProcess.addProcess("goldView", 
				AnalysisEngineFactory.createEngineDescription(ViewTextCopierAnnotator.class, 
						ViewTextCopierAnnotator.PARAM_SOURCE_VIEW_NAME, CAS.NAME_DEFAULT_SOFA, 
						ViewTextCopierAnnotator.PARAM_DESTINATION_VIEW_NAME, targetView));

		batchProcess.addProcess("init", 
				AnalysisEngineFactory.createEngineDescription(ViewAnnotationCopier.class, 
						ViewAnnotationCopier.PARAM_TARGET_VIEW_NAME, targetView)
				);

		batchProcess.clean();
		batchProcess.run();

	}
}
