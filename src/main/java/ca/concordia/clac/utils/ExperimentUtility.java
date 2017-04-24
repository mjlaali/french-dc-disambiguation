package ca.concordia.clac.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.inference.TTest;

import weka.classifiers.Evaluation;
import weka.core.Instances;

public class ExperimentUtility {
	public static Map<TestCriterion, Evaluation> run(Instances train, Instances test, List<Experiment> experiments) throws Exception{
		Map<TestCriterion, Evaluation> experimentsResults = new HashMap<>();
		train = randomizeData(train);
		for (Experiment experiment: experiments){
//			System.out.print(experiment.getId() + ": ");
			experiment.train(train);
			for (TestCriterion aTest: experiment.getTests()){
//				System.out.print(aTest.getId() + ", ");
				experimentsResults.put(aTest, aTest.test(test, test));
			}
//			System.out.println();
		}
		
		return experimentsResults;
	}
	
	public static Map<TestCriterion, List<Evaluation>> runKFold(int folds, Instances dataset, List<Experiment> experiments) throws Exception{
		Instances randData = randomizeData(dataset);
		if (randData.classAttribute().isNominal())
			randData.stratify(folds);

		
		Map<TestCriterion, List<Evaluation>> experimentsResults = new HashMap<>();
		
		for (Experiment experiment: experiments){
			for (TestCriterion aTest: experiment.getTests()){
				experimentsResults.put(aTest, new ArrayList<>());
			}
		}
		
		for (int n = 0; n < folds; n++){
			System.out.println("ModelComparator.evaluate(): Fold " + n);
			Instances train = randData.trainCV(folds, n);
			Instances test = randData.testCV(folds, n);
			
			for (Experiment experiment: experiments){
				experiment.train(train);
				for (TestCriterion aTest: experiment.getTests()){
					experimentsResults.get(aTest).add(aTest.test(randData, test));
				}
			}
		}
		
		return experimentsResults;
	}

	private static Instances randomizeData(Instances dataset) {
		Instances randData;
		Random rand = new Random();
		randData = new Instances(dataset);
		randData.randomize(rand);
		return randData;
	}
	
	public static Map<TestCriterion, Map<TestCriterion, Boolean>> evaluateStatisticallySignificant(
			Map<TestCriterion, List<Evaluation>> experimentsResults,
			Function<Evaluation, Double> getTargetMeasure,
			double pVal){
		
		Map<TestCriterion, List<Double>> targetValues = new HashMap<>();
		Map<TestCriterion, List<String>> toStringSummaries = new HashMap<>();
		
		experimentsResults.forEach((exp, evals) -> targetValues.put(exp, 
				evals.stream().map(getTargetMeasure).collect(Collectors.toList())));
		
		Function<Evaluation, String> evalToString = (e) -> {
			String res;
			try {
				res = e.toSummaryString() + "\n" + e.toMatrixString();
			} catch (Exception e1) {
				return e.toSummaryString() + "\n**null";
			}
			return res;
		};
		
		experimentsResults.forEach((exp, evals) -> toStringSummaries.put(exp, 
				evals.stream().map(evalToString).collect(Collectors.toList())));
		
		Map<TestCriterion, Map<TestCriterion, Boolean>> results = new HashMap<>();
		TTest tTest = new TTest(); 
		for (TestCriterion exp1: targetValues.keySet()){
			results.put(exp1, new HashMap<>());
			for (TestCriterion exp2: targetValues.keySet()){
				
				double[] prevSystem = convertToDoubles(targetValues, exp1);
				double[] currentSystem = convertToDoubles(targetValues, exp2);
				
				Boolean ttTest = null;
				try {
					ttTest = tTest.pairedTTest(prevSystem, currentSystem, pVal);
				} catch (Exception e) {
					
				}
				results.get(exp1).put(exp2, ttTest);
			}
		}
		
		return results;
		
	}

	private static double[] convertToDoubles(Map<TestCriterion, List<Double>> targetValues, TestCriterion exp) {
		List<Double> list = targetValues.get(exp);
		double[] result = list.stream().filter((d) -> !Double.isNaN(d)).mapToDouble(Double::doubleValue).toArray();
		return result;
	}
}
