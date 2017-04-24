package ca.concordia.clac.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileInputStream;
import java.util.Random;

import org.junit.Test;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class ExperimentsToolsTest {

	@Test
	public void whenRandomizingDatasetThenTheOrderOfAttributeValueIsNotChanged() throws Exception{
		Instances dataset = DataSource.read(new FileInputStream("resources/results/dataset/pdtb.arff"));
		dataset.setClassIndex(dataset.numAttributes() - 1);
		
		Instances randData;
		Random rand = new Random();
		randData = new Instances(dataset);
		randData.randomize(rand);
		if (randData.classAttribute().isNominal())
			randData.stratify(10);
		
		int idx = dataset.attribute("CON-LStr").index();
		assertThat(idx).isEqualTo(randData.attribute("CON-LStr").index());
		System.out.println(idx);
		for (int i = 0;i < 100; i++)
			assertThat(dataset.attribute(idx).value(i)).isEqualTo(randData.attribute(idx).value(i));
		
		System.out.println(dataset.attribute(idx).indexOfValue("because"));
		
	}
}
