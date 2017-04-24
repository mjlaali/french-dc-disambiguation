package ca.concordia.clac.ftb;

import java.util.List;

public interface FTBListener {

	public void sent(String sentId, String parse, List<String> words, List<String> poses);
}
