package ca.concordia.clac.conll;

import java.util.ArrayList;

public class Word {
	public Word() {
	}
	
	public Word(int begin, int end, String pos){
		CharacterOffsetBegin = begin;
		CharacterOffsetEnd = end;
		PartOfSpeech = pos;
	}
	
	public int CharacterOffsetBegin;
	public int CharacterOffsetEnd;
	public ArrayList<String> Linkers = new ArrayList<String>();
	public String PartOfSpeech;
}