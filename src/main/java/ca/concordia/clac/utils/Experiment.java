package ca.concordia.clac.utils;

import java.io.Serializable;
import java.util.List;

import weka.core.Instances;

public interface Experiment extends Serializable{
	public Object getId();
	public void train(Instances train) throws Exception;
	public List<TestCriterion> getTests();
}