package ca.concordia.clac.frenchdc.results;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
	
	private List<String> dcFilters = new ArrayList<>();
	
	public DCPerformanceTable(String datasetFile, String... dcs) throws FileNotFoundException, Exception {
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
	
	private void setupBaseline() throws Exception{
		baseline.setClassifier("weka.classifiers.bayes.NaiveBayes");
		baseline.setFilter("weka.filters.unsupervised.attribute.Remove -V -R 1,7");
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
		setupBaseline();
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
				System.out.printf("F=%s, A=%s\n", fscoreStatSig.get(t1).get(t2), accuracyStatSig.get(t1).get(t2));
				int startDcIdx = tag.indexOf("-L ") + "-L ".length();
				int endDcIdx = tag.indexOf(" ", startDcIdx);
				int dcIdx = Integer.parseInt(tag.substring(startDcIdx, endDcIdx));
				String dc = dataset.attribute("CON-LStr").value(dcIdx - 1);
				System.out.println(dc);
				System.out.println("\n==================\n");
				
				sb.append(String.format("%s\t%.1f\t%f\t%f\t%s\n", dc, t1.getOverallEvaluation().numInstances(), t1.getOverallEvaluation().pctCorrect(), 
						t2.getOverallEvaluation().pctCorrect(), accuracyStatSig.get(t1).get(t2)));
			}
		}
		
		System.out.println(sb.toString());
	}

	private void print(TestCriterion t1, Map<TestCriterion, List<Evaluation>> experimentsResults) throws Exception {
		System.out.println(t1.toString());
		System.out.println(t1.getOverallEvaluation().toSummaryString());
		System.out.println(t1.getOverallEvaluation().toClassDetailsString());
		List<Double> accuracies = experimentsResults.get(t1).stream().map(Evaluation::pctCorrect).collect(Collectors.toList());
		System.out.println(accuracies.toString());
		System.out.println("----");

	}
	
	public static void main(String[] args) throws Exception {
		new DCPerformanceTable("resources/results/dataset/fdtb-gold.arff"
//				, "effectivement", "sinon", "alors", "parce que", "puisque", "car"
				).run();
//		new DCPerformanceTable("resources/results/dataset/pdtb.arff"
//				, "in contrast", "as a result", "besides", "because", "since", "as"
//				).run();
	}
	
}
