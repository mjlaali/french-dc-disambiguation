package ca.concordia.clac.utils;

import java.io.Serializable;
import java.util.List;

import weka.core.Instances;

/**
 * A Weka experiment, including training over given instances and then running test criterion on a given test set.
 * @author majid
 *
 */
public interface Experiment extends Serializable{
	public Object getId();
	public void train(Instances train) throws Exception;
	public List<TestCriterion> getTests();
}