package ca.concordia.clac.discourse;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.function.Function;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.initialize.ConfigurationParameterInitializer;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.ml.Feature;
import org.cleartk.ml.opennlp.maxent.MaxentStringOutcomeDataWriter;
import org.xml.sax.SAXException;

import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.Option;

import ca.concordia.clac.discourse.parser.dc.disambiguation.DiscourseAnnotationFactory;
import ca.concordia.clac.discourse.parser.dc.disambiguation.DiscourseVsNonDiscourseClassifier;
import ca.concordia.clac.lexconn.DefaultLexconnReader;
import ca.concordia.clac.ml.classifier.StringClassifierLabeller;
import ca.concordia.clac.tools.XMLGenerator;
import ca.concordia.clac.uima.engines.LookupInstanceExtractor;
import de.tudarmstadt.ukp.dkpro.core.berkeleyparser.BerkeleyParser;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;

public class FrConnectiveClassifier extends DiscourseVsNonDiscourseClassifier{
	public static final String DC_LIST_FILE = "dc.list";
	public static final String PACKAGE_DIR = "clacParser/french/model/";
	public static final String PARAM_LEXCONN_FILE = "lexconnFile";
	public static final String PARAM_GENERATE_DC_LIST = "generateDCList";
	public static final String PARAM_JUST_CANONICAL_FEATURE = "justCanonicalFeature";

	public static final String CANONICAL_FEATURE = "CANONICAL";
	
	@ConfigurationParameter(name=PARAM_LEXCONN_FILE, mandatory = true)
	URL lexconnFile;
	
	@ConfigurationParameter(name=PARAM_JUST_CANONICAL_FEATURE, mandatory = true)
	boolean justCanonicalFeature;

	@ConfigurationParameter(name=PARAM_GENERATE_DC_LIST, mandatory = true)
	boolean generateDCList;

	@ConfigurationParameter(name=LookupInstanceExtractor.PARAM_LOOKUP_FILE_URL)
	private URL lookupFileUrl;

	Map<String, String> formToId;
	Map<String, String> idToCanonicalForm;
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		ConfigurationParameterInitializer.initialize(this, context);
	    
		try {
			DefaultLexconnReader lexconnReader = DefaultLexconnReader.getLexconnMap(lexconnFile);
			formToId = lexconnReader.getFormToId();
			idToCanonicalForm = lexconnReader.getIdToCanonicalForm();
			if (generateDCList){
				TreeSet<String> forms = new TreeSet<>(formToId.keySet());
				FileUtils.writeLines(new File(lookupFileUrl.toURI()), "UTF-8", forms);
			}
		} catch (ParserConfigurationException | SAXException | IOException | URISyntaxException e) {
			throw new ResourceInitializationException(e);
		}
		
		super.initialize(context);

	}
	
	
	public class CustomFeatures implements Function<DiscourseConnective, List<Feature>>{
		List<Function<DiscourseConnective, List<Feature>>> featureExtractor;

		public CustomFeatures(List<Function<DiscourseConnective, List<Feature>>> featureExtractor) {
			this.featureExtractor = featureExtractor;
		}
		
		@Override
		public List<Feature> apply(DiscourseConnective t) {
			List<Feature> features = new ArrayList<>();
			
			featureExtractor.stream().forEach((f) -> features.addAll(f.apply(t)));
			
			return update(features);
		}

		private List<Feature> update(List<Feature> features) {
			List<Feature> newFeatures = new ArrayList<>();
			Feature connLstr = null;
			for (Feature feature: features){
				if (feature.getName().equals(DiscourseVsNonDiscourseClassifier.CONN_LStr)){
					connLstr = feature;
					newFeatures.add(new Feature(CANONICAL_FEATURE, getCanonicalForm((String)feature.getValue())));
				}
			}
			features.addAll(newFeatures);
			if (justCanonicalFeature)
				return Arrays.asList(connLstr);
			return features;
		}

		private String getCanonicalForm(String value) {
			try {
				return idToCanonicalForm.get(formToId.get(value));
			} catch (NullPointerException e) {
				System.err.println("FrConnectiveClassifier.CustomFeatures.getCanonicalForm(): cannot find the canonical form for '" + value + "'");
				return "";
			}
					
		}
		
	}
	
	@Override
	public List<Function<DiscourseConnective, List<Feature>>> getFeatureExtractor(JCas aJCas) {
		
		return Arrays.asList(new CustomFeatures(super.getFeatureExtractor(aJCas)));
	}
	
	public static AnalysisEngineDescription getWriterDescription(File outputFld, File lexconnFile) throws ResourceInitializationException, MalformedURLException{
		return getWriterDescription(outputFld, lexconnFile);
	}
	
	public static AnalysisEngineDescription getWriterDescription(File outputFld, URL lexconnFile) throws ResourceInitializationException, MalformedURLException{
		File dcList = new File(outputFld, DC_LIST_FILE);
		return StringClassifierLabeller.getWriterDescription(
				FrConnectiveClassifier.class,
				MaxentStringOutcomeDataWriter.class,
				outputFld, 
				FrConnectiveClassifier.PARAM_GENERATE_DC_LIST, true,
				FrConnectiveClassifier.PARAM_JUST_CANONICAL_FEATURE, false,
				LookupInstanceExtractor.PARAM_LOOKUP_FILE_URL, dcList.toURI().toURL().toString(),
				LookupInstanceExtractor.PARAM_ANNOTATION_FACTORY_CLASS_NAME, DiscourseAnnotationFactory.class.getName(), 
				FrConnectiveClassifier.PARAM_LEXCONN_FILE, lexconnFile
				);
	}
	
	public static AnalysisEngineDescription getClassifierDescription(URL model, boolean justCanonicalFeature) throws ResourceInitializationException, MalformedURLException, URISyntaxException{
		URL dcList = ClassLoader.getSystemClassLoader().getResource(PACKAGE_DIR + DC_LIST_FILE);
		URL lexconnFile = getLexconn();
		return getClassifierDescription(dcList, model, lexconnFile, justCanonicalFeature);
	}

	public static URL getLexconn() {
		URL lexconnFile = ClassLoader.getSystemClassLoader().getResource(PACKAGE_DIR + "Lexconn_v2.1.xml");
		return lexconnFile;
	}
	
	public static AnalysisEngineDescription getClassifierDescription(URL model) throws ResourceInitializationException, MalformedURLException, URISyntaxException{
		URL dcList = ClassLoader.getSystemClassLoader().getResource(PACKAGE_DIR + DC_LIST_FILE);
		URL lexconnFile = getLexconn();
		return getClassifierDescription(dcList, model, lexconnFile, false);
	}
	
	public static AnalysisEngineDescription getClassifierDescription() throws ResourceInitializationException, MalformedURLException, URISyntaxException{
		URL model = ClassLoader.getSystemClassLoader().getResource(PACKAGE_DIR + "model.jar");
		return getClassifierDescription(model);
	}
	
	public static AnalysisEngineDescription getClassifierDescription(URL dcList, URL model, URL lexconnFile, boolean justCanonicalFeature) throws ResourceInitializationException, MalformedURLException, URISyntaxException{
		return StringClassifierLabeller.getClassifierDescription(
					FrConnectiveClassifier.class,
					model,
					FrConnectiveClassifier.PARAM_GENERATE_DC_LIST, false,
					LookupInstanceExtractor.PARAM_LOOKUP_FILE_URL, dcList,
					FrConnectiveClassifier.PARAM_LEXCONN_FILE, lexconnFile,
					FrConnectiveClassifier.PARAM_JUST_CANONICAL_FEATURE, justCanonicalFeature,
					LookupInstanceExtractor.PARAM_ANNOTATION_FACTORY_CLASS_NAME, DiscourseAnnotationFactory.class.getName()
					);
	}
	
	interface Options{
		@Option(
				shortName = "m",
				longName = "model", 
				description = "model dir",
				defaultToNull = true)
		public File getModelDir();

		@Option(
				shortName = "i",
				longName = "input", 
				description = "a directory that contains text files. If it is not provided, the classifier read text from the console",
				defaultToNull = true)
		public File getInputText();

		@Option(
				shortName = "o",
				longName = "output", 
				description = "the output directory to store the results. If the input direcotry is not presented will be ignored.",
				defaultToNull = true)
		public File getOutputDir();
		
	}
	
	public static void main(String[] args) throws Exception{
		Options options = CliFactory.parseArguments(Options.class, args);
		
		if (options.getModelDir() == null)
			System.out.println("The pre-trained model will be loaded");
		else
			System.out.println("Model = " + options.getModelDir());
		
		try{
			File model = options.getModelDir();
			File inputDir = options.getInputText();
			JCas jcas = null;
			CollectionReaderDescription reader = null;
			if (inputDir == null){
				Scanner scanner = new Scanner(System.in);
				StringBuilder sb = new StringBuilder();
				while (scanner.hasNext()){
					sb.append(scanner.nextLine());
					sb.append('\n');
				}
				scanner.close();
				jcas = JCasFactory.createJCas();
				jcas.setDocumentText(sb.toString());
				jcas.setDocumentLanguage("fr");
			} else {
				reader = CollectionReaderFactory.createReaderDescription(TextReader.class,
						TextReader.PARAM_SOURCE_LOCATION, inputDir,
						TextReader.PARAM_LANGUAGE, "fr", 
						TextReader.PARAM_PATTERNS, "*");
			}
			
			AggregateBuilder pipline = new AggregateBuilder();
			pipline.add(createEngineDescription(OpenNlpSegmenter.class,
					OpenNlpSegmenter.PARAM_LANGUAGE, "fr", 
					OpenNlpSegmenter.PARAM_SEGMENTATION_MODEL_LOCATION, "classpath:/fr-sent.bin", 
					OpenNlpSegmenter.PARAM_TOKENIZATION_MODEL_LOCATION, "classpath:/fr-token.bin"
					));
			pipline.add(createEngineDescription(BerkeleyParser.class, 
					BerkeleyParser.PARAM_LANGUAGE, "fr", 
					BerkeleyParser.PARAM_MODEL_LOCATION, "classpath:/fra_sm5.gr", 
					BerkeleyParser.PARAM_WRITE_POS, true, 
					BerkeleyParser.PARAM_READ_POS, false));
			
			pipline.add(model == null ? getClassifierDescription() : getClassifierDescription(model.toURI().toURL()));
			
			File outputDir = options.getOutputDir() == null ? new File("out") : options.getOutputDir();
			if (jcas != null)
				outputDir = null;
			pipline.add(XMLGenerator.getDescription(outputDir, "", true, "DiscourseConnective"));
			
			if (jcas != null)
				SimplePipeline.runPipeline(jcas, pipline.createAggregateDescription());
			else
				SimplePipeline.runPipeline(reader, pipline.createAggregateDescription());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

}


