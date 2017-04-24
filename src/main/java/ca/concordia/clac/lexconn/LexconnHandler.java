package ca.concordia.clac.lexconn;

import java.util.List;

public interface LexconnHandler {

	void process(List<String> forms, List<String> examples, List<String> relations, String type, String cat, String id);

	void endDocument();

}
