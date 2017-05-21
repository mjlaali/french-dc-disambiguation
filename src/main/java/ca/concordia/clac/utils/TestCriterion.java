package ca.concordia.clac.utils;

import weka.classifiers.Evaluation;
import weka.core.Instances;

/**
 * A test criterion for a given test set. 
 * @author majid
 *
 */
public interface TestCriterion {
	public Object getId();
	/**
	 * Test classifier on a given test set.
	 * @param all
	 * @param test: given test set.
	 * @return
	 * @throws Exception
	 */
	public Evaluation test(Instances all, Instances test) throws Exception;
	/**
	 * The result of the evaluation on the given test set.
	 * @return
	 */
	public Evaluation getOverallEvaluation();
	public void setExperiment(Experiment experiment);
	public Experiment getExperiment();
}
