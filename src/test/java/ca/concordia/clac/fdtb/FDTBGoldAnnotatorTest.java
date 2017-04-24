package ca.concordia.clac.fdtb;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.collection.metadata.CpeDescriptorException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.CasIOUtil;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.eval.EvaluationConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.xml.sax.SAXException;

import ca.concordia.clac.batch_process.BatchProcess;
import ca.concordia.clac.discourse.FDTBPipelineFactory;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

@RunWith(Parameterized.class)
public class FDTBGoldAnnotatorTest {
	private static String fileName = "1014.xmi";
	
	private File outputFld;
	
	@Parameters
    public static Collection<Object[]> data() throws UIMAException, IOException, SAXException, CpeDescriptorException, ParserConfigurationException {
    	File dataInput = new File("data/test/FDTBGoldAnnotator/");
    	File testOutput = new File("outputs/test/FDTBGoldAnnotator");
    	File ftb = new File(dataInput, "ftb");
    	File fdtb = new File(dataInput, "fdtb.xml");

    	FileUtils.deleteDirectory(testOutput);
    	
    	//Test 1
    	File syntaxFile = new File(testOutput, "parses.json");
    	File rawFld = new File(testOutput, "raw");
		new FDTBSyntaxReader(fdtb, ftb).run(syntaxFile, rawFld);
    	//syncax file cannot be use because I removed few spaces from raw text to test the functionality. If you want to use syntax file, please regenerate a correct one.
		File outputWithGoldSyntaxFld = new File(testOutput, "gold/");
		FDTBPipelineFactory factory = new FDTBPipelineFactory();
		BatchProcess goldSyntax = factory.getPipeWithGoldSyntaxTree(fdtb, rawFld, syntaxFile, outputWithGoldSyntaxFld);
		goldSyntax.clean();
		goldSyntax.run();

		//Test 2
		File outputWithSpace = new File(testOutput, "gold-with-space/");
    	File fdtbWithSpace = new File(outputWithSpace, "fdtb.xml");
    	FileUtils.writeStringToFile(fdtbWithSpace, 
    			FileUtils.readFileToString(fdtb, "UTF-8").replace("<CONN>", " <CONN>").replace("</CONN>", "</CONN> "),
    			StandardCharsets.UTF_8);
    	File outputWithGoldSyntaxAndSpaceFld = new File(testOutput, "gold-space/");
    	BatchProcess goldSyntaxWithSpace = factory.getPipeWithGoldSyntaxTree(fdtbWithSpace, rawFld, syntaxFile, outputWithGoldSyntaxAndSpaceFld);
    	goldSyntaxWithSpace.clean();
    	goldSyntaxWithSpace.run();
    	
		//Test 3
		File outputWithBerkeleyFld = new File(testOutput, "berkely");
		BatchProcess berkelySyntax = factory.getPipeWithBerkelyParser(fdtb, rawFld, outputWithBerkeleyFld);
		berkelySyntax.clean();
		berkelySyntax.run();
		
		return Arrays.asList(new Object[][]{
			new Object[]{outputWithGoldSyntaxFld},
			new Object[]{outputWithBerkeleyFld},
			new Object[]{outputWithGoldSyntaxAndSpaceFld}
		});

    }

    
    public FDTBGoldAnnotatorTest(File outputFld) {
    	this.outputFld = outputFld;
	}
	
	@Test
	public void whenReadingFDTBThenTwoViewAreCreated() throws IOException, UIMAException{
		JCas aJCas = loadJCas(FDTBPipelineFactory.INIT_STEP);
		
		assertThat(aJCas.getView(EvaluationConstants.GOLD_VIEW)).isNotNull();
		assertThat(aJCas.getView(EvaluationConstants.GOLD_VIEW).getDocumentText()).isEqualTo(aJCas.getDocumentText());
	}
	
	@Test
	public void whenReadingFDTBThenTheNumberOfDiscourseConnectiveIsRight() throws UIMAException, IOException{
		JCas aJCas = loadJCas(FDTBPipelineFactory.GOLD_ANNOTATION_STEP);
		
		
		Collection<DiscourseConnective> discourseConnectives = JCasUtil.select(aJCas.getView(EvaluationConstants.GOLD_VIEW), DiscourseConnective.class);
		assertThat(discourseConnectives).hasSize(11);
		
		List<String> dcTexts = new ArrayList<>(); 
		for (DiscourseConnective discourseConnective: discourseConnectives){
			dcTexts.add(TokenListTools.getTokenListText(discourseConnective));
		}
		
		assertThat(dcTexts).containsOnly("en", "Cependant", "jusqu' à", "et", "mais",
				"aussi", "en", "aussi", "aussi", "et", "Enfin");
	}


	private JCas loadJCas(String step) throws UIMAException, IOException {
		File outFld = new File(outputFld, step);
		File outFile = new File(outFld, fileName);
		
		assertThat(outFile).exists();
		
		JCas aJCas = JCasFactory.createJCas();
		CasIOUtil.readXmi(aJCas, outFile);
		return aJCas;
	}
	
	@Test
	public void whenReadingFDTBThenSyntaxAnnotationsExist() throws UIMAException, IOException{
		JCas aJCas = loadJCas(FDTBPipelineFactory.GOLD_ANNOTATION_STEP);
		
		assertThat(JCasUtil.select(aJCas, Constituent.class)).isNotEmpty();
	}
}
