package ca.concordia.clac.conll;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ca.concordia.clac.conll.Sentence;
import ca.concordia.clac.conll.SyntaxInfo;
import ca.concordia.clac.conll.Word;

public class SyntaxInfoTest {
	private Gson gson;

	@Before
	public void setUp(){
		GsonBuilder builder = new GsonBuilder();
		gson = builder.create();
	}
	
	@Test
	public void whenConvertinToJsonThenTheFormatIsFine(){
		Map<String, SyntaxInfo> map = new TreeMap<String, SyntaxInfo>();

		Word word = new Word();
		word.CharacterOffsetBegin = 10;
		word.CharacterOffsetEnd = 20;
		word.PartOfSpeech = "JJ";

		Sentence sentence = new Sentence();
		sentence.dependencies.add(new String[]{"amod", "members-2", "Influential-1"});
		sentence.parsetree = "( S (NP (DT the) (N book) ) )";
		sentence.words.add(new Object[]{"usual", word});

		SyntaxInfo syntaxInfo = new SyntaxInfo();
		syntaxInfo.sentences.add(sentence);
		map.put("wsj_2200", syntaxInfo);

		String output = "{\"wsj_2200\":{\"sentences\":[{\"dependencies\":[[\"amod\",\"members-2\",\"Influential-1\"]],\"parsetree\":\"( S (NP (DT the) (N book) ) )\",\"words\":[[\"usual\",{\"CharacterOffsetBegin\":10,\"CharacterOffsetEnd\":20,\"Linkers\":[],\"PartOfSpeech\":\"JJ\"}]]}]}}";
		assertThat(gson.toJson(map)).isEqualTo(output);
	}
	
	@Test
	public void whenConveringFromStringThenTheOutputIsFine(){
		String parseTree = "( (NP (NNP Telecussed) ) )";
		int offset = 180; 
		String output = "{\"dependencies\":[],\"parsetree\":\"( (NP (NNP Telecussed) ) )\",\"words\":[[\"Telecussed\",{\"CharacterOffsetBegin\":180,\"CharacterOffsetEnd\":190,\"Linkers\":[],\"PartOfSpeech\":\"NNP\"}]]}"; 
		
		Sentence sentence = new Sentence(parseTree, offset);
		assertThat(gson.toJson(sentence)).isEqualTo(output);
		
	}

	@Test
	public void whenConveringFromComplexParseTreeThenTheOutputIsFine(){
		String parseTree = "( (S (NP (DT The) (NN stat) (S (VP (TO to) (VP (VB reckon) (PP (IN with) (NP (RB here))))))) (VP (VBZ says) (SBAR (IN that) (S (NP (NP (QP (IN about) (CD three) (IN of) (CD four)) (NNS clubs)) (PRN (-LRB- -LRB-) (NP (QP (CD 29) (IN of) (CD 39))) (-RRB- -RRB-)) (SBAR (WHNP (WDT that)) (S (VP (VBD took) (NP (CD 2-0) (NN Series) (NNS leads)))))) (VP (VBD went) (PRT (RP on)) (S (VP (TO to) (VP (VB win) (NP (PRP it) (DT all))))))))) (. .)) )";
		int offset = 2808; 
		String output = "{\"dependencies\":[],\"parsetree\":\"( (S (NP (DT The) (NN stat) (S (VP (TO to) (VP (VB reckon) (PP (IN with) (NP (RB here))))))) (VP (VBZ says) (SBAR (IN that) (S (NP (NP (QP (IN about) (CD three) (IN of) (CD four)) (NNS clubs)) (PRN (-LRB- -LRB-) (NP (QP (CD 29) (IN of) (CD 39))) (-RRB- -RRB-)) (SBAR (WHNP (WDT that)) (S (VP (VBD took) (NP (CD 2-0) (NN Series) (NNS leads)))))) (VP (VBD went) (PRT (RP on)) (S (VP (TO to) (VP (VB win) (NP (PRP it) (DT all))))))))) (. .)) )\",\"words\":[[\"The\",{\"CharacterOffsetBegin\":2808,\"CharacterOffsetEnd\":2811,\"Linkers\":[],\"PartOfSpeech\":\"DT\"}],[\"stat\",{\"CharacterOffsetBegin\":2812,\"CharacterOffsetEnd\":2816,\"Linkers\":[],\"PartOfSpeech\":\"NN\"}],[\"to\",{\"CharacterOffsetBegin\":2817,\"CharacterOffsetEnd\":2819,\"Linkers\":[],\"PartOfSpeech\":\"TO\"}],[\"reckon\",{\"CharacterOffsetBegin\":2820,\"CharacterOffsetEnd\":2826,\"Linkers\":[],\"PartOfSpeech\":\"VB\"}],[\"with\",{\"CharacterOffsetBegin\":2827,\"CharacterOffsetEnd\":2831,\"Linkers\":[],\"PartOfSpeech\":\"IN\"}],[\"here\",{\"CharacterOffsetBegin\":2832,\"CharacterOffsetEnd\":2836,\"Linkers\":[],\"PartOfSpeech\":\"RB\"}],[\"says\",{\"CharacterOffsetBegin\":2837,\"CharacterOffsetEnd\":2841,\"Linkers\":[],\"PartOfSpeech\":\"VBZ\"}],[\"that\",{\"CharacterOffsetBegin\":2842,\"CharacterOffsetEnd\":2846,\"Linkers\":[],\"PartOfSpeech\":\"IN\"}],[\"about\",{\"CharacterOffsetBegin\":2847,\"CharacterOffsetEnd\":2852,\"Linkers\":[],\"PartOfSpeech\":\"IN\"}],[\"three\",{\"CharacterOffsetBegin\":2853,\"CharacterOffsetEnd\":2858,\"Linkers\":[],\"PartOfSpeech\":\"CD\"}],[\"of\",{\"CharacterOffsetBegin\":2859,\"CharacterOffsetEnd\":2861,\"Linkers\":[],\"PartOfSpeech\":\"IN\"}],[\"four\",{\"CharacterOffsetBegin\":2862,\"CharacterOffsetEnd\":2866,\"Linkers\":[],\"PartOfSpeech\":\"CD\"}],[\"clubs\",{\"CharacterOffsetBegin\":2867,\"CharacterOffsetEnd\":2872,\"Linkers\":[],\"PartOfSpeech\":\"NNS\"}],[\"(\",{\"CharacterOffsetBegin\":2873,\"CharacterOffsetEnd\":2874,\"Linkers\":[],\"PartOfSpeech\":\"-LRB-\"}],[\"29\",{\"CharacterOffsetBegin\":2874,\"CharacterOffsetEnd\":2876,\"Linkers\":[],\"PartOfSpeech\":\"CD\"}],[\"of\",{\"CharacterOffsetBegin\":2877,\"CharacterOffsetEnd\":2879,\"Linkers\":[],\"PartOfSpeech\":\"IN\"}],[\"39\",{\"CharacterOffsetBegin\":2880,\"CharacterOffsetEnd\":2882,\"Linkers\":[],\"PartOfSpeech\":\"CD\"}],[\")\",{\"CharacterOffsetBegin\":2882,\"CharacterOffsetEnd\":2883,\"Linkers\":[],\"PartOfSpeech\":\"-RRB-\"}],[\"that\",{\"CharacterOffsetBegin\":2884,\"CharacterOffsetEnd\":2888,\"Linkers\":[],\"PartOfSpeech\":\"WDT\"}],[\"took\",{\"CharacterOffsetBegin\":2889,\"CharacterOffsetEnd\":2893,\"Linkers\":[],\"PartOfSpeech\":\"VBD\"}],[\"2-0\",{\"CharacterOffsetBegin\":2894,\"CharacterOffsetEnd\":2897,\"Linkers\":[],\"PartOfSpeech\":\"CD\"}],[\"Series\",{\"CharacterOffsetBegin\":2898,\"CharacterOffsetEnd\":2904,\"Linkers\":[],\"PartOfSpeech\":\"NN\"}],[\"leads\",{\"CharacterOffsetBegin\":2905,\"CharacterOffsetEnd\":2910,\"Linkers\":[],\"PartOfSpeech\":\"NNS\"}],[\"went\",{\"CharacterOffsetBegin\":2911,\"CharacterOffsetEnd\":2915,\"Linkers\":[],\"PartOfSpeech\":\"VBD\"}],[\"on\",{\"CharacterOffsetBegin\":2916,\"CharacterOffsetEnd\":2918,\"Linkers\":[],\"PartOfSpeech\":\"RP\"}],[\"to\",{\"CharacterOffsetBegin\":2919,\"CharacterOffsetEnd\":2921,\"Linkers\":[],\"PartOfSpeech\":\"TO\"}],[\"win\",{\"CharacterOffsetBegin\":2922,\"CharacterOffsetEnd\":2925,\"Linkers\":[],\"PartOfSpeech\":\"VB\"}],[\"it\",{\"CharacterOffsetBegin\":2926,\"CharacterOffsetEnd\":2928,\"Linkers\":[],\"PartOfSpeech\":\"PRP\"}],[\"all\",{\"CharacterOffsetBegin\":2929,\"CharacterOffsetEnd\":2932,\"Linkers\":[],\"PartOfSpeech\":\"DT\"}],[\".\",{\"CharacterOffsetBegin\":2932,\"CharacterOffsetEnd\":2933,\"Linkers\":[],\"PartOfSpeech\":\".\"}]]}"; 

		Sentence sentence = new Sentence(parseTree, offset);
		System.out.println(gson.toJson(sentence));
		assertThat(gson.toJson(sentence)).isEqualTo(output);
	}
	
	@Test
	public void whenConvertingFrenchParseTreeThenTheOutputIsSynced(){
		String parseTree = "( (S (NP (DT The) (NN stat) (S (VP (TO to) (VP (VB reckon) (PP (IN with) (NP (RB here))))))) (VP (VBZ says) (SBAR (IN that) (S (NP (NP (QP (IN about) (CD three) (IN of) (CD four)) (NNS clubs)) (PRN (-LRB- -LRB-) (NP (QP (CD 29) (IN of) (CD 39))) (-RRB- -RRB-)) (SBAR (WHNP (WDT that)) (S (VP (VBD took) (NP (CD 2-0) (NN Series) (NNS leads)))))) (VP (VBD went) (PRT (RP on)) (S (VP (TO to) (VP (VB win) (NP (PRP it) (DT all))))))))) (. .)) )";
		int offset = 2808; 
		String output = "{\"dependencies\":[],\"parsetree\":\"( (S (NP (DT The) (NN stat) (S (VP (TO to) (VP (VB reckon) (PP (IN with) (NP (RB here))))))) (VP (VBZ says) (SBAR (IN that) (S (NP (NP (QP (IN about) (CD three) (IN of) (CD four)) (NNS clubs)) (PRN (-LRB- -LRB-) (NP (QP (CD 29) (IN of) (CD 39))) (-RRB- -RRB-)) (SBAR (WHNP (WDT that)) (S (VP (VBD took) (NP (CD 2-0) (NN Series) (NNS leads)))))) (VP (VBD went) (PRT (RP on)) (S (VP (TO to) (VP (VB win) (NP (PRP it) (DT all))))))))) (. .)) )\",\"words\":[[\"The\",{\"CharacterOffsetBegin\":2808,\"CharacterOffsetEnd\":2811,\"Linkers\":[],\"PartOfSpeech\":\"DT\"}],[\"stat\",{\"CharacterOffsetBegin\":2812,\"CharacterOffsetEnd\":2816,\"Linkers\":[],\"PartOfSpeech\":\"NN\"}],[\"to\",{\"CharacterOffsetBegin\":2817,\"CharacterOffsetEnd\":2819,\"Linkers\":[],\"PartOfSpeech\":\"TO\"}],[\"reckon\",{\"CharacterOffsetBegin\":2820,\"CharacterOffsetEnd\":2826,\"Linkers\":[],\"PartOfSpeech\":\"VB\"}],[\"with\",{\"CharacterOffsetBegin\":2827,\"CharacterOffsetEnd\":2831,\"Linkers\":[],\"PartOfSpeech\":\"IN\"}],[\"here\",{\"CharacterOffsetBegin\":2832,\"CharacterOffsetEnd\":2836,\"Linkers\":[],\"PartOfSpeech\":\"RB\"}],[\"says\",{\"CharacterOffsetBegin\":2837,\"CharacterOffsetEnd\":2841,\"Linkers\":[],\"PartOfSpeech\":\"VBZ\"}],[\"that\",{\"CharacterOffsetBegin\":2842,\"CharacterOffsetEnd\":2846,\"Linkers\":[],\"PartOfSpeech\":\"IN\"}],[\"about\",{\"CharacterOffsetBegin\":2847,\"CharacterOffsetEnd\":2852,\"Linkers\":[],\"PartOfSpeech\":\"IN\"}],[\"three\",{\"CharacterOffsetBegin\":2853,\"CharacterOffsetEnd\":2858,\"Linkers\":[],\"PartOfSpeech\":\"CD\"}],[\"of\",{\"CharacterOffsetBegin\":2859,\"CharacterOffsetEnd\":2861,\"Linkers\":[],\"PartOfSpeech\":\"IN\"}],[\"four\",{\"CharacterOffsetBegin\":2862,\"CharacterOffsetEnd\":2866,\"Linkers\":[],\"PartOfSpeech\":\"CD\"}],[\"clubs\",{\"CharacterOffsetBegin\":2867,\"CharacterOffsetEnd\":2872,\"Linkers\":[],\"PartOfSpeech\":\"NNS\"}],[\"(\",{\"CharacterOffsetBegin\":2873,\"CharacterOffsetEnd\":2874,\"Linkers\":[],\"PartOfSpeech\":\"-LRB-\"}],[\"29\",{\"CharacterOffsetBegin\":2874,\"CharacterOffsetEnd\":2876,\"Linkers\":[],\"PartOfSpeech\":\"CD\"}],[\"of\",{\"CharacterOffsetBegin\":2877,\"CharacterOffsetEnd\":2879,\"Linkers\":[],\"PartOfSpeech\":\"IN\"}],[\"39\",{\"CharacterOffsetBegin\":2880,\"CharacterOffsetEnd\":2882,\"Linkers\":[],\"PartOfSpeech\":\"CD\"}],[\")\",{\"CharacterOffsetBegin\":2882,\"CharacterOffsetEnd\":2883,\"Linkers\":[],\"PartOfSpeech\":\"-RRB-\"}],[\"that\",{\"CharacterOffsetBegin\":2884,\"CharacterOffsetEnd\":2888,\"Linkers\":[],\"PartOfSpeech\":\"WDT\"}],[\"took\",{\"CharacterOffsetBegin\":2889,\"CharacterOffsetEnd\":2893,\"Linkers\":[],\"PartOfSpeech\":\"VBD\"}],[\"2-0\",{\"CharacterOffsetBegin\":2894,\"CharacterOffsetEnd\":2897,\"Linkers\":[],\"PartOfSpeech\":\"CD\"}],[\"Series\",{\"CharacterOffsetBegin\":2898,\"CharacterOffsetEnd\":2904,\"Linkers\":[],\"PartOfSpeech\":\"NN\"}],[\"leads\",{\"CharacterOffsetBegin\":2905,\"CharacterOffsetEnd\":2910,\"Linkers\":[],\"PartOfSpeech\":\"NNS\"}],[\"went\",{\"CharacterOffsetBegin\":2911,\"CharacterOffsetEnd\":2915,\"Linkers\":[],\"PartOfSpeech\":\"VBD\"}],[\"on\",{\"CharacterOffsetBegin\":2916,\"CharacterOffsetEnd\":2918,\"Linkers\":[],\"PartOfSpeech\":\"RP\"}],[\"to\",{\"CharacterOffsetBegin\":2919,\"CharacterOffsetEnd\":2921,\"Linkers\":[],\"PartOfSpeech\":\"TO\"}],[\"win\",{\"CharacterOffsetBegin\":2922,\"CharacterOffsetEnd\":2925,\"Linkers\":[],\"PartOfSpeech\":\"VB\"}],[\"it\",{\"CharacterOffsetBegin\":2926,\"CharacterOffsetEnd\":2928,\"Linkers\":[],\"PartOfSpeech\":\"PRP\"}],[\"all\",{\"CharacterOffsetBegin\":2929,\"CharacterOffsetEnd\":2932,\"Linkers\":[],\"PartOfSpeech\":\"DT\"}],[\".\",{\"CharacterOffsetBegin\":2932,\"CharacterOffsetEnd\":2933,\"Linkers\":[],\"PartOfSpeech\":\".\"}]]}"; 

		Sentence sentence = new Sentence(parseTree, offset);
		System.out.println(gson.toJson(sentence));
		assertThat(gson.toJson(sentence)).isEqualTo(output);
	}

}
