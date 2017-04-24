package ca.concordia.clac.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class DefaultExperimentTest {

	@Test
	public void givenFiterNullThenHashFunctionReturnsHashCode(){
		assertThat(new DefaultExperiment().hashCode()).isNotNull();
	}
	
	@Test
	public void givenTwoNewDefaultExperimentThenTheyAreEqual(){
		assertThat(new DefaultExperiment("")).isEqualTo(new DefaultExperiment(""));
	}
	
}
