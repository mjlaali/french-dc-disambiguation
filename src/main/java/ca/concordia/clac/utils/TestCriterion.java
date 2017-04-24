package ca.concordia.clac.utils;

import weka.classifiers.Evaluation;
import weka.core.Instances;

public interface TestCriterion {
	public Object getId();
	public Evaluation test(Instances all, Instances test) throws Exception;
	public Evaluation getOverallEvaluation();
	public void setExperiment(Experiment experiment);
	public Experiment getExperiment();
}
