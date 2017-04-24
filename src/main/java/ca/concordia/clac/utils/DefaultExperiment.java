package ca.concordia.clac.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;

@SuppressWarnings("serial")
public class DefaultExperiment implements Experiment{
	private static Integer cnt = 0;
	private Object id;
	private String classifierConfig;
	private String filterConfig;
	private String testFilterConfig;
	private Classifier classifier;
	private Filter filter;
	private Classifier learnedClassifier;
	private List<TestCriterion> testCriteria = new ArrayList<>();

	public DefaultExperiment() {
		this(cnt);
	}
	
	public DefaultExperiment(Object id) {
		cnt += 1;
		this.id = id;
	}
	
	
	public void setClassifier(String classifierConfig) throws Exception{
		this.classifierConfig = classifierConfig;
		classifier = buildWekaComponent(classifierConfig, Classifier.class);
	}
	
	public void setFilter(String filterConfig) throws Exception{
		if (testCriteria.size() > 0)
			throw new RuntimeException("Cannot add filter after adding test criteria.");
		this.filterConfig = filterConfig;
		this.filter = buildWekaComponent(filterConfig, Filter.class);
	}
	
	public void addTestCriterion(DefaultTestCriterion testCriterion) throws Exception{
		if (filterConfig != null)
			testCriterion.addFilter(filterConfig);
		testCriterion.setExperiment(this);
		testCriteria.add(testCriterion);
	}
	
	@Override
	public Object getId() {
		return id;
	}
	
	@Override
	public void train(Instances train) throws Exception {
		if (filter != null){
			filter.setInputFormat(train);
			train = Filter.useFilter(train, filter);
		}
		learnedClassifier = AbstractClassifier.makeCopy(classifier);
		learnedClassifier.buildClassifier(train);
	}


	@SuppressWarnings("unchecked")
	public static <T> T buildWekaComponent(String aConfig, Class<T> clazz) throws Exception {
		String classifierClassName;
		String[] options;
		int optionsStart = aConfig.indexOf(' ');
		if (optionsStart == -1)
			optionsStart = aConfig.length();
		classifierClassName = aConfig.substring(0, optionsStart);
		options = Utils.splitOptions(aConfig.substring(optionsStart).trim());

		return (T) Utils.forName(clazz, classifierClassName, options);
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(filterConfig)
				.append(testFilterConfig).append(classifierConfig).hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(filterConfig).append(testFilterConfig).append(classifierConfig).toString();
	}

	@Override
	public List<TestCriterion> getTests() {
		for (TestCriterion criterion: testCriteria){
			((DefaultTestCriterion)criterion).setClassifier(learnedClassifier);
		}
		return testCriteria;
	}
}
