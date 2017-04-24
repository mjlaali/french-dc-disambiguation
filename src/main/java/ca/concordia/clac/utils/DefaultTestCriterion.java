package ca.concordia.clac.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.filters.Filter;

public class DefaultTestCriterion implements TestCriterion{
	private static int cnt = 0;
	private Object id;
	private Classifier classifier;
	private List<Filter> filters = new ArrayList<>();
	private List<String> filterConfigs = new ArrayList<>();
	private Evaluation overallEvaluation;
	private Experiment experiment;

	public DefaultTestCriterion(String... filterConfigs) throws Exception {
		this(new Integer(cnt), filterConfigs);
	}
	
	public DefaultTestCriterion(Object id, String... filterConfigs) throws Exception {
		for (String filterConfig: filterConfigs){
			addFilter(filterConfig);
		}
		this.id = id;
		cnt += 1;
	}
	
	@Override
	public Object getId() {
		return id;
	}

	public void addFilter(String filterConfig) throws Exception{
		this.filterConfigs.add(filterConfig);
		this.filters.add(DefaultExperiment.buildWekaComponent(filterConfig, Filter.class));
	}
	
	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}
	
	@Override
	public void setExperiment(Experiment experiment){
		this.experiment = experiment;
	}
	
	@Override
	public Experiment getExperiment() {
		return experiment;
	}

	@Override
	public Evaluation test(Instances all, Instances test) throws Exception {
		for (Filter filter: filters){
			filter.setInputFormat(all);
			test = Filter.useFilter(test, filter);
			all = Filter.useFilter(all, filter);
		}

		Evaluation overAllEvaluation = getOverAllEvaluation(all);
		Evaluation eval = new Evaluation(test);
		eval.evaluateModel(classifier, test);
		overAllEvaluation.evaluateModel(classifier, test);
		return eval;
	}

	private Evaluation getOverAllEvaluation(Instances all) throws Exception {
		if (overallEvaluation == null){
			overallEvaluation = new Evaluation(all);
		}
		return overallEvaluation;
	}
	
	@Override
	public Evaluation getOverallEvaluation() {
		return overallEvaluation;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(filterConfigs).append(id).hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DefaultTestCriterion)) {
            return false;
        }
        DefaultTestCriterion other  = (DefaultTestCriterion) obj;
		return new EqualsBuilder().append(this.filterConfigs, other.filterConfigs).append(this.id, other.id).isEquals();
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(experiment).append(filterConfigs).toString();
	}
}
