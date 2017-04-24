package ca.concordia.clac.conll;

import java.util.ArrayList;
import java.util.List;

import org.cleartk.util.treebank.TopTreebankNode;
import org.cleartk.util.treebank.TreebankFormatParser;
import org.cleartk.util.treebank.TreebankNode;

public class Sentence {
	public List<String[]> dependencies = new ArrayList<String[]>();
	public String parsetree;
	public List<Object[]> words = new ArrayList<Object[]>();
	
	public Sentence() {
	}
	
	public Sentence(String parsetree, int offset){
		this(parsetree, offset, TreebankFormatParser.inferPlainText(parsetree));
	}
	
	public Sentence(String parsetree, int offset, String text){
		setParsetree(parsetree);
		TopTreebankNode root = null;
		try{
			root = TreebankFormatParser.parse(parsetree, text, 0);
		} catch (IllegalArgumentException ia){
			TreebankFormatParser.parse(parsetree, text, 0);
			return;
		}
		ArrayList<TreebankNode> leaves = new ArrayList<TreebankNode>();
		getChilderen(root, leaves);
		
		for (TreebankNode leaf: leaves){
			int textBegin = leaf.getTextBegin();
			int textEnd = leaf.getTextEnd();
			words.add(new Object[]{leaf.getText(), new Word(offset + textBegin, offset + textEnd, leaf.getType())});
			if (!leaf.getText().equals(text.substring(textBegin, textEnd))){
				throw new RuntimeException(String.format("Text is not synced with the parse tree: word = <%s>, matched = <%s>\n%s\n%s\n"
						, leaf.getText(), text.substring(textBegin, textEnd), parsetree, text));
			}
		}
	}
	
	public void setParsetree(String parsetree) {
		this.parsetree = parsetree;
	}

	private void getChilderen(TreebankNode node, List<TreebankNode> leaves){
		if (node.isLeaf())
			leaves.add(node);
		else
			for (TreebankNode aChild: node.getChildren()){
				getChilderen(aChild, leaves);
			}
	}
}