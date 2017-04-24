package ca.concordia.clac.fdtb;

public class FDTBPath{
	public static final String FDTB_DIR = "data/fdtb-project/original/fdtb1";
	public static final String FDTB_FILE = FDTB_DIR + "/fdtb1.xml";
	
	public static final String OUT_DIR = "outputs";
	public static final String XMI_DIR = OUT_DIR + "/xmi";
	public static final String MODEL_DIR = OUT_DIR + "/model";
	
	public static final String FTB_DIR = "data/FrenchTreebank";
	
	public static final String CONLL_HOME = "data/fdtb-project/conll-format/fdtb";
	public static final String CONLL_RAWTEXT = CONLL_HOME + "/raw";
	public static final String CONLL_SYNTAXFILE = CONLL_HOME + "/parses.json";
	
	public String getXmiOutDir() {
		return XMI_DIR;
	}
	
	public String getModelDir() {
		return MODEL_DIR;
	}
	
	
	public String getRawTextsFld() {
		return CONLL_RAWTEXT;
	}
	

	public String getSyntaxAnnotationFlie() {
		return CONLL_SYNTAXFILE;
	}
	
	
}
