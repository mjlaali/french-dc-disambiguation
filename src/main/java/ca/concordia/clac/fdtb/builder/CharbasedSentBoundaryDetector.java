package ca.concordia.clac.fdtb.builder;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;

public class CharbasedSentBoundaryDetector extends JCasAnnotator_ImplBase {
	
	char punctuation = '\n';
	
	public static AnalysisEngineDescription getEngineDescription() throws ResourceInitializationException{
		return createEngineDescription(CharbasedSentBoundaryDetector.class);
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		String text = aJCas.getDocumentText();
		int begin = 0;
		int end ;
		while ((end = text.indexOf(punctuation, begin)) != -1){
			if (begin != end)
				new Sentence(aJCas, begin, end).addToIndexes();
			begin = end + 1;
		}
	}

}
