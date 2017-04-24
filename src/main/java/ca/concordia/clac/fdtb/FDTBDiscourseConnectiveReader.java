package ca.concordia.clac.fdtb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class FDTBDiscourseConnectiveReader extends DefaultFDTBListener{
	private Map<String, List<Integer>> connectiveStarts = new TreeMap<>();
	private Map<String, List<Integer>> connectiveEnds = new TreeMap<>();
	private Map<String, String> docTexts = new HashMap<>();
	
	private List<Integer> starts;
	private List<Integer> ends;
	private int offset;
	private int nextSentOffset;
	private StringBuilder buffer = new StringBuilder();
	private String articleId;

	@Override
	public void startArticle(String id) {
		articleId = id;
		starts = new ArrayList<>();
		connectiveStarts.put(id, starts);
		ends = new ArrayList<>();
		connectiveEnds.put(id, ends);
		offset = 0;
		nextSentOffset = 0;
	}
	
	@Override
	public void endArticle() {
		docTexts.put(articleId, buffer.toString());
		buffer.setLength(0);

		super.endArticle();
	}
	
	@Override
	public void addSent(String sent, String sentId) {
		offset = nextSentOffset;
		nextSentOffset += sent.length() + 1;
		buffer.append(sent);
		buffer.append('\n');
	}
	
	@Override
	public void addConnective(List<Integer> connStarts, List<Integer> connEnds) {
		starts.addAll(connStarts.stream().map((pos) -> offset + pos).collect(Collectors.toList()));
		ends.addAll(connEnds.stream().map((pos) -> offset + pos).collect(Collectors.toList()));
	}

	public Map<String, List<Integer>> getConnectiveEnds() {
		return connectiveEnds;
	}
	
	public Map<String, List<Integer>> getConnectiveStarts() {
		return connectiveStarts;
	}
	
	public Map<String, String> getDocTexts() {
		return docTexts;
	}
}
