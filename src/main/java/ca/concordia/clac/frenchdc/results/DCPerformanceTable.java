package ca.concordia.clac.frenchdc.results;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import ca.concordia.clac.utils.DefaultExperiment;
import ca.concordia.clac.utils.DefaultTestCriterion;
import ca.concordia.clac.utils.ExperimentUtility;
import ca.concordia.clac.utils.TestCriterion;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class DCPerformanceTable {
	private DefaultExperiment baseline = new DefaultExperiment();
	private DefaultExperiment system = new DefaultExperiment();
	private Instances dataset;
	private int cnnFeatureIdx;
	private int outcomeIdx;
	
	private List<String> dcFilters = new ArrayList<>();
	private PrintStream output = System.out;
	
	public DCPerformanceTable(String datasetFile, int cnnFeatureIdx, int outcomeIdx, String... dcs) throws FileNotFoundException, Exception {
		this.cnnFeatureIdx = cnnFeatureIdx;
		this.outcomeIdx = outcomeIdx;
		dataset = DataSource.read(new FileInputStream(datasetFile));
		dataset.setClassIndex(dataset.numAttributes() - 1);
		
		dcFilters = new ArrayList<>();
		String filterTemplate = "weka.filters.unsupervised.instance.RemoveWithValues -S 0.0 -C %d -L %d -V";
		Attribute attribute = dataset.attribute("CON-LStr");
		
		if (dcs.length == 0){
			for (int i = 0; i < attribute.numValues(); i++){
				dcFilters.add(String.format(filterTemplate, attribute.index() + 1, i + 1));
			}
		} else {
			for (String dc: dcs){
				dcFilters.add(String.format(filterTemplate, attribute.index() + 1, attribute.indexOfValue(dc) + 1));
			}
		}
	}
	
	public void setOutput(PrintStream output){
		this.output = output;
	}
	
	private void setupBaseline(int cnnFeatureIdx, int outcomeIdx) throws Exception{
		baseline.setClassifier("weka.classifiers.bayes.NaiveBayes");
		baseline.setFilter(String.format("weka.filters.unsupervised.attribute.Remove -V -R %d,%d", cnnFeatureIdx, outcomeIdx));
		addDCFilters(baseline);
	}

	private void addDCFilters(DefaultExperiment exp) throws Exception{
		for (String dcFilter: dcFilters){
			exp.addTestCriterion(new DefaultTestCriterion(dcFilter));
		}
	}
	
	private void setupSystem() throws Exception{
//		system.setClassifier("weka.classifiers.functions.Logistic -R 1.0E-8 -M -1");
		system.setClassifier("weka.classifiers.trees.J48 -C 0.25 -M 2");
		addDCFilters(system);
	}
	
	public void run() throws Exception{
		setupBaseline(cnnFeatureIdx, outcomeIdx);
		setupSystem();
		
		Map<TestCriterion, List<Evaluation>> experimentsResults = ExperimentUtility.runKFold(10, dataset, Arrays.asList(system, baseline));
		
		Function<Evaluation, Double> getAccuracy = (ev) -> ev.pctCorrect();
		Function<Evaluation, Double> getFScore = (ev) -> ev.fMeasure(1);
		
		Map<TestCriterion, Map<TestCriterion, Boolean>> accuracyStatSig = 
				ExperimentUtility.evaluateStatisticallySignificant(experimentsResults, getAccuracy, 0.05);
		Map<TestCriterion, Map<TestCriterion, Boolean>> fscoreStatSig = 
				ExperimentUtility.evaluateStatisticallySignificant(experimentsResults, getFScore, 0.05);
		
		StringBuilder sb = new StringBuilder();
		
		for (TestCriterion t1: accuracyStatSig.keySet()){
			for (TestCriterion t2: accuracyStatSig.keySet()){
				if (t1.getExperiment().equals(t2.getExperiment()))
					continue;
				String strT1 = t1.toString();
				String strT2 = t2.toString();
				int tagStart = strT1.indexOf("RemoveWithValues");
				int tagEnd = strT1.indexOf(",", tagStart);
				if (tagEnd == -1)
					tagEnd = strT1.length();
				String tag = strT1.substring(tagStart, tagEnd).trim();
				if (!strT2.contains(tag))
					continue;
				print(t1, experimentsResults);
				print(t2, experimentsResults);
				output.printf("F=%s, A=%s\n", fscoreStatSig.get(t1).get(t2), accuracyStatSig.get(t1).get(t2));
				int startDcIdx = tag.indexOf("-L ") + "-L ".length();
				int endDcIdx = tag.indexOf(" ", startDcIdx);
				int dcIdx = Integer.parseInt(tag.substring(startDcIdx, endDcIdx));
				String dc = dataset.attribute("CON-LStr").value(dcIdx - 1);
				output.println(dc);
				output.println("\n==================\n");
				
				sb.append(String.format("%s\t%.1f\t%f\t%f\t%s\n", dc, t1.getOverallEvaluation().numInstances(), t1.getOverallEvaluation().pctCorrect(), 
						t2.getOverallEvaluation().pctCorrect(), accuracyStatSig.get(t1).get(t2)));
			}
		}
		
		output.println(sb.toString());
	}

	private void print(TestCriterion t1, Map<TestCriterion, List<Evaluation>> experimentsResults) throws Exception {
		output.println(t1.toString());
		output.println(t1.getOverallEvaluation().toSummaryString());
		output.println(t1.getOverallEvaluation().toClassDetailsString());
		List<Double> accuracies = experimentsResults.get(t1).stream().map(Evaluation::pctCorrect).collect(Collectors.toList());
		output.println(accuracies.toString());
		output.println("----");

	}
	
	public static void main(String[] args) throws Exception {
//		int outcome = 12; File modelDir = new File("outputs/fdtb-berkely/model/");
		int outcome = 11; File modelDir = new File("/Users/majid/Documents/git/CLaCDiscourseParser/discourse.parser.parent/discourse.parser.dc-disambiguation/outputs/resources/discourse-vs-nondiscourse"); 
		
		File reportFile = new File(modelDir, "report-per-connective.txt");
		PrintStream output = new PrintStream(new FileOutputStream(reportFile), true, "UTF-8");
		
		DCPerformanceTable dcPerformanceTable = new DCPerformanceTable(new File(modelDir, "training-data.arff").getAbsolutePath(), 1, outcome);
		dcPerformanceTable.setOutput(output);
		dcPerformanceTable.run();
		output.close();
//				, "effectivement", "sinon", "alors", "parce que", "puisque", "car"
//		new DCPerformanceTable("resources/results/dataset/pdtb.arff"
//				, "in contrast", "as a result", "besides", "because", "since", "as"
//				).run();
	}
	
}
