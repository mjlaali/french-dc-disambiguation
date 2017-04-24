package ca.concordia.clac.ftb;

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xml.sax.SAXException;

import ca.concordia.clac.ftb.FTBListener;
import ca.concordia.clac.ftb.FTBReader;

@SuppressWarnings("unchecked")
public class FTBReaderTest {
	@Mock FTBListener ftbListener;
	private FTBReader ftbReader = new FTBReader();

	@Before
	public void setUp() throws ParserConfigurationException, SAXException, IOException{
		MockitoAnnotations.initMocks(this);

	}

	@Test
	public void whenReadingAFTBFileThenForEachSentAParanthesisTextIsGenerated() throws ParserConfigurationException, SAXException, IOException {
		File file = new File("data/FrenchTreebank/corpus-constit/lmf3_01000_01499ep.aa.xml");
		ftbReader.parse(file, ftbListener);
		verify(ftbListener, times(494)).sent(anyString(), anyString(), anyList(), anyList());
	}

	@Test
	public void whenReadingSentsFromAFTBFileThenSentIDOfTheFirstSentIsOK() throws ParserConfigurationException, SAXException, IOException{
		File file = new File("data/FrenchTreebank/corpus-fonctions/flmf3_11000_11499ep.aa.xml");
		ftbReader.parse(file, ftbListener);
		verify(ftbListener).sent(eq("flmf3_11000_11499ep-11000"), anyString(), anyList(), anyList());
	}

	@Test
	public void whenReadingParseFromAnXMLThenConstituentsAreInsertedToParenthesisesString() throws ParserConfigurationException, SAXException, IOException{
		String xml = "<SENT nb=\"1000\"> <NP> <PP> <NP> </NP> </PP></NP> </SENT>";
		String parse = "(S (NP (PP (NP ) ) ) )";
		ftbReader.parse(xml, ftbListener);
		verify(ftbListener).sent(eq("1000"), eq(parse), anyList(), anyList());
	}

	@Test
	public void whenReadingParseFromAnXMLThenWordsAreInsertedToParenthesisesString() throws ParserConfigurationException, SAXException, IOException{
		String xml = "<SENT nb=\"1000\"> <VPpart>\n"
				+ "<w cat=\"PONCT\" ee=\"PONCT-W\" ei=\"PONCTW\" lemma=\",\" subcat=\"W\">,</w>\n" 
				+ "<w cat=\"ADV\" ee=\"ADV\" ei=\"ADV\" lemma=\"seulement\">seulement</w>\n" 
				+ "<w cat=\"V\" ee=\"V--Kmp\" ei=\"VKmp\" lemma=\"blesser\" mph=\"Kmp\" subcat=\"\">blessés</w>\n" 
				+ "<w cat=\"PONCT\" ee=\"PONCT-W\" ei=\"PONCTW\" lemma=\",\" subcat=\"W\">,</w>\n" 
				+ "</VPpart> </SENT>";
		String parse = "(S (VPpart (PONCT ,) (ADV seulement) (V blessés) (PONCT ,) ) )";
		ftbReader.parse(xml, ftbListener);
		verify(ftbListener).sent(eq("1000"), eq(parse), anyList(), anyList());

	}

	@Test
	public void whenReadingCompoundNounThenAllWordsApearWithCatintTag() throws ParserConfigurationException, SAXException, IOException{
		String xml = "<SENT nb=\"1000\"> \n" 
				+  "<PP> \n "
				+ "<w cat=\"P\" ee=\"P\" ei=\"P\" lemma=\"en\">en</w> \n"
				+ "<NP> \n "
				+ "<w compound=\"yes\" cat=\"N\" ee=\"N-P-fs\" ei=\"NPfs\" lemma=\"C\u00f4te-d\'Ivoire\" mph=\"fs\" subcat=\"P\"> \n "
				+ "<w catint=\"N\">Côte</w>\n "
				+ "<w catint=\"PONCT\">-</w>\n "
				+ "<w catint=\"P\">d'</w>\n "
				+ "<w catint=\"N\">Ivoire</w>\n "
				+ "</w> \n </NP> \n </PP>\n </SENT>";
		String parse = "(S (PP (P en) (NP (N Côte) (PONCT -) (P d') (N Ivoire) ) ) )";
		ftbReader.parse(xml, ftbListener);
		verify(ftbListener).sent(eq("1000"), eq(parse), anyList(), anyList());
	}

	@Test
	public void whenTokenContainsParenthesisThenParentesisesAreConvertedToLRB_RRB() throws ParserConfigurationException, SAXException, IOException{
		String xml = "<SENT nb=\"392\">\n"
				+ "<NP> \n"
				+ "<w cat=\"N\" compound=\"yes\" ee=\"N-P-s\" ei=\"NPs\" lemma=\"SAINT-DENIS\" mph=\"s\" subcat=\"P\">\n"
				+ "<w catint=\"A\">SAINT</w>\n"
				+ "<w catint=\"PONCT\">-</w>\n"
				+ "<w catint=\"N\">DENIS</w>\n"
				+ "</w> \n"
				+ "<w cat=\"PONCT\" ee=\"PONCT-W\" ei=\"PONCTW\" lemma=\"(\" subcat=\"W\">(</w> \n"
				+ "<NP> \n"
				+ "<w cat=\"N\" ee=\"N-P-fs\" ei=\"NPfs\" lemma=\"Réunion\" mph=\"fs\" subcat=\"P\">Réunion</w> \n"
				+ "</NP> \n"
				+ "<w cat=\"PONCT\" ee=\"PONCT-W\" ei=\"PONCTW\" lemma=\")\" subcat=\"W\">)</w>\n"
				+ "</NP>\n"
				+ "</SENT>";
		String parse = "(S (NP (A SAINT) (PONCT -) (N DENIS) (-LRB- -LRB-) (NP (N Réunion) ) (-RRB- -RRB-) ) )";
		ftbReader.parse(xml, ftbListener);
		verify(ftbListener).sent(eq("392"), eq(parse), anyList(), anyList());
	}
	
	@Test
	public void whenReadingParseThenOutputWordsAndPosesToo() throws ParserConfigurationException, SAXException, IOException{
		String xml = "<SENT nb=\"392\">\n"
				+ "<NP> \n"
				+ "<w cat=\"N\" compound=\"yes\" ee=\"N-P-s\" ei=\"NPs\" lemma=\"SAINT-DENIS\" mph=\"s\" subcat=\"P\">\n"
				+ "<w catint=\"A\">SAINT</w>\n"
				+ "<w catint=\"PONCT\">-</w>\n"
				+ "<w catint=\"N\">DENIS</w>\n"
				+ "</w> \n"
				+ "<w cat=\"PONCT\" ee=\"PONCT-W\" ei=\"PONCTW\" lemma=\"(\" subcat=\"W\">(</w> \n"
				+ "<NP> \n"
				+ "<w cat=\"N\" ee=\"N-P-fs\" ei=\"NPfs\" lemma=\"Réunion\" mph=\"fs\" subcat=\"P\">Réunion</w> \n"
				+ "</NP> \n"
				+ "<w cat=\"PONCT\" ee=\"PONCT-W\" ei=\"PONCTW\" lemma=\")\" subcat=\"W\">)</w>\n"
				+ "</NP>\n"
				+ "</SENT>";
		String parse = "(S (NP (A SAINT) (PONCT -) (N DENIS) (-LRB- -LRB-) (NP (N Réunion) ) (-RRB- -RRB-) ) )";
		List<String> words = Arrays.asList(new String[]{"SAINT", "-", "DENIS", "-LRB-", "Réunion", "-RRB-"});
		List<String> poses = Arrays.asList(new String[]{"A", "PONCT", "N", "-LRB-", "N", "-RRB-"});
		ftbReader.parse(xml, ftbListener);
		verify(ftbListener).sent(eq("392"), eq(parse), eq(words), eq(poses));
		
	}
	
	@Test
	public void whenTheConstituentDoesNotHaveTextThenItIsRemovedFromParseTree() throws ParserConfigurationException, SAXException, IOException{
		String xml = "<SENT nb=\"11013\" textID=\"1014\" date=\"1990-01-00\" author=\"LEMONDE\">\n" +
				 " <w cat=\"ADV\" ee=\"ADV\" ei=\"ADV\" lemma=\"enfin\">Enfin</w>\n" +
				 " <w cat=\"PONCT\" ee=\"PONCT-W\" ei=\"PONCTW\" lemma=\",\" subcat=\"W\">,</w>\n" +
				 " <NP fct=\"SUJ\">\n" +
				 " <w cat=\"D\" ee=\"D-def-ms\" ei=\"Dms\" lemma=\"le\" mph=\"ms\" subcat=\"def\">le</w>\n" +
				 " <w cat=\"N\" ee=\"N-C-ms\" ei=\"NCms\" lemma=\"patron\" mph=\"ms\" subcat=\"C\">patron</w>\n" +
				 " <PP>\n" +
				 " <w cat=\"P\" ee=\"P\" ei=\"P\" lemma=\"de\">des</w>\n" +
				 " <NP>\n" +
				 " <w cat=\"D\" ee=\"D-def-mp\" ei=\"Dmp\" lemma=\"le\" mph=\"mp\" subcat=\"def\"/>\n" +
				 " <w cat=\"N\" ee=\"N-P-mp\" ei=\"NPmp\" lemma=\"Chargeurs\" mph=\"mp\" subcat=\"P\">Chargeurs</w>\n" +
				 " </NP>\n" +
				 " </PP>\n" +
				 " </NP>" + 
				 " </SENT>";
		String parse = "(S (ADV Enfin) (PONCT ,) (NP (D le) (N patron) (PP (P des) (NP (N Chargeurs) ) ) ) )";
		ftbReader.parse(xml, ftbListener);
		verify(ftbListener).sent(eq("11013"), eq(parse), anyList(), anyList());
	}
	
	@Test
	public void whenThereIsCompoundWord() throws ParserConfigurationException, SAXException, IOException{
		String xml = "<SENT nb=\"11013\" textID=\"1014\" date=\"1990-01-00\" author=\"LEMONDE\">\n" +
				"<PP fct=\"MOD\">\r\n              <w compound=\"yes\" cat=\"P\" ee=\"P\" ei=\"P\" lemma=\"jusqu'\u00E0\"> <w catint=\"P\">jusqu'</w> <w catint=\"P\">à</w> </w></PP>" + 
				"</SENT>";
		String parse = "(S (PP (P jusqu') (P à) ) )";
		ftbReader.parse(xml, ftbListener);
		verify(ftbListener).sent(eq("11013"), eq(parse), anyList(), anyList());
				
	}
	
}
