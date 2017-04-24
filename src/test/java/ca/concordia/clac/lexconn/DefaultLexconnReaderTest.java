package ca.concordia.clac.lexconn;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class DefaultLexconnReaderTest {
	
	private DefaultLexconnReader reader;
	
	@Before
	public void setup() throws ParserConfigurationException, SAXException, IOException{
		reader = DefaultLexconnReader.getLexconnMap(new File(DefaultLexconnReader.LEXCONN_FILE));
	}
	
	@Test
	public void whenThereAreMultipleIdForTheSameFormThenMapToTheSmallest(){
		Map<String, String> formToId = reader.getFormToId();
		String id = formToId.get("si");
		assertThat(id).isEqualTo("c353");

		id = formToId.get("en");
		assertThat(id).isEqualTo("c183");

		id = formToId.get("avant que");
		assertThat(id).isEqualTo("c49");
		
	}
	
	@Test
	public void whenThereAreMultipleIdForTheSameFormThenAggregateRelations(){
		String id = reader.getFormToId().get("si");
		Set<String> relations = reader.getIdToRelations().get(id);
		assertThat(relations).containsExactly("concession", "condition");
	}
	
	@Test
	public void idToCanonicalFormHasUniquForm(){
		List<String> canonicalForms = new ArrayList<>(reader.getIdToCanonicalForm().values());
		int size = canonicalForms.size();
		int uniqSize = new TreeSet<>(canonicalForms).size();
		
		Collections.sort(canonicalForms);
		assertThat(size).isEqualTo(uniqSize);
	}
	
	@Test
	public void noIntesectionBetweenForms(){
		Collection<Set<String>> values = reader.getIdToForms().values();
		
		for (Set<String> aSet: values){
			for (Set<String> bSet: values){
				if (aSet == bSet)
					continue;
				
				for (String aValue: aSet){
					assertThat(bSet).doesNotContain(aValue);
				}
			}
		}
	}
}
