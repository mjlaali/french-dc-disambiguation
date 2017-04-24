package ca.concordia.clac.fdtb;

import java.util.List;

public interface FDTBListener {
	void addSent(String sent, String sentId);
	void addConnective(List<Integer> connStarts, List<Integer> connEnds);
	void startArticle(String id);
	void endArticle();
	void finished();
}
