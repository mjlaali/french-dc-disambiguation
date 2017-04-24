package ca.concordia.clac.lexconn;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;

import ca.concordia.clac.discourse.FrConnectiveClassifier;

public class DefaultLexconnReader implements LexconnHandler{
//	public static final String LEXCONN_FILE = "data/fdtb/fdtb1/lexconn/Lexconn_v2.xml";
	public static final String LEXCONN_FILE = "data/lexconn_V2.1/Lexconn_v2.1.xml";
	private SetMultimap<String, String> lexconnDic = TreeMultimap.create();
	private Map<String, String> formToId = new TreeMap<>();
	private Map<String, String> idToCanonicalForm = new TreeMap<>();
	private Map<String, Set<String>> idToForms = new TreeMap<>();
	private SetMultimap<String, String> idToRelations = TreeMultimap.create();
	private Set<String> forms = new TreeSet<>();
	
	public DefaultLexconnReader() {
	}
	
	@Override
	public void process(List<String> forms, List<String> examples, List<String> relations, String type, String cat,
			String id) {

		if (idToForms.put(id, new TreeSet<>(forms)) != null)
			System.err.println("DefaultLexconnReader.process(): Duplicate ID! " + id);
		
		idToRelations.putAll(id, relations);
		idToCanonicalForm.put(id, forms.get(0));
		this.forms.addAll(forms);
	}

	@Override
	public void endDocument() {
		removeDuplicateIds();	//fix idToForms
		mergeRelations();		//fix idToRelations, formToId, idToForms
		setCanonicalForm();		//fix idToCanonicalFrom
	}


	private void setCanonicalForm() {
		Map<String, String> canonicalToId = new TreeMap<>();
		Map<String, String> newIdToCanonicalForm = new TreeMap<>();
		
		
		Set<String> suggestedCanonicalForm = new TreeSet<>(idToCanonicalForm.values());
		for (String id: sortIds()){
			String canonicalForm = null;
			for (String aForm: idToForms.get(id)){
				if (suggestedCanonicalForm.contains(aForm) || canonicalForm == null){
					canonicalForm = aForm;
				}
			}
			String prevId = canonicalToId.put(canonicalForm, id);
			if (prevId == null){
				newIdToCanonicalForm.put(id, canonicalForm);
				continue;
			}
			String idSmallGroup = id;
			String idBigGroup = prevId;
			if (idToForms.get(idSmallGroup).size() > idToForms.get(idBigGroup).size()){
				idSmallGroup = prevId;
				idBigGroup = id;
			}
			
			Set<String> diff = new TreeSet<>(idToForms.get(idBigGroup));
			diff.removeAll(idToForms.get(idSmallGroup));
			diff.removeAll(newIdToCanonicalForm.values());
			if (diff.size() == 0){
				
			}
			String newCanonicalForm = diff.iterator().next();
			canonicalToId.put(canonicalForm, idSmallGroup);
			newIdToCanonicalForm.put(idSmallGroup, canonicalForm);
			canonicalToId.put(newCanonicalForm, idBigGroup);
			newIdToCanonicalForm.put(idBigGroup, newCanonicalForm);
		}
		
		this.idToCanonicalForm = newIdToCanonicalForm;
	}

	private List<String> sortIds() {
		List<String> ids =  new ArrayList<>(idToForms.keySet());	//start to assign id to smallest groups first;
		Comparator<? super String> setComparator = (a, b) -> {
			Set<String> aFrom = idToForms.get(a);
			Set<String> bFrom = idToForms.get(b);
			if (aFrom.containsAll(bFrom))
				return 1;
			if (bFrom.containsAll(aFrom))
				return -1;
			return aFrom.toString().compareTo(bFrom.toString());
		};
		Collections.sort(ids, setComparator);
		return ids;
	}

	private void mergeRelations() {
		SetMultimap<String, String> formToId = TreeMultimap.create();
		for (Entry<String, Set<String>> anEntry: idToForms.entrySet()){
			for (String aForm: anEntry.getValue()){
				formToId.put(aForm, anEntry.getKey());
			}
		}
		
		for (String aForm: formToId.keySet()){
			List<String> ids = new ArrayList<>(formToId.get(aForm));
			String smallest = ids.get(0);
			
			//find the smallest group
			for (String id: ids){
				smallest = id;
				for (String id2: ids){
					if (!idToForms.get(id2).containsAll(idToForms.get(smallest))){
						smallest = null;
						break;
					}
				}
				if (smallest != null)
					break;
			}
			if (smallest == null){
				System.out.printf("DefaultLexconnReader.mergeRelations(), I cannot find the smallest group for %s between %s\n", aForm, ids.toString());
			}
			
			this.formToId.put(aForm, smallest);
			
			Set<String> allSense = new TreeSet<>();
			for (String id: ids){
				allSense.addAll(idToRelations.get(id));
			}
			idToRelations.putAll(smallest, allSense);
		}

		for (String aForm: formToId.keySet()){
			List<String> ids = new ArrayList<>(formToId.get(aForm));

			for (String id: ids){
				if (id.equals(this.formToId.get(aForm)))
					continue;
				idToForms.get(id).remove(aForm);	//This form only belongs to the smallest
			}
		}

	}

	private void removeDuplicateIds() {
		Map<Set<String>, String> formsToId = new HashMap<>();
		for (String id: new TreeSet<>(idToForms.keySet())){
			String prevId = formsToId.put(idToForms.get(id), id);
			if (prevId == null){
				continue;
			}
			
			idToForms.remove(id);
			Set<String> otherRelations = idToRelations.removeAll(id);
			Set<String> relations = new TreeSet<>(idToRelations.get(prevId));
			relations.addAll(otherRelations);
			idToRelations.putAll(prevId, relations);
		}
		
	}
	
	public SetMultimap<String, String> getLexconnDic() {
		return lexconnDic;
	}
	
	public Map<String, String> getFormToId() {
		return formToId;
	}
	
	public SetMultimap<String, String> getIdToRelations() {
		return idToRelations;
	}
	
	public Map<String, String> getIdToCanonicalForm() {
		return idToCanonicalForm;
	}
	
	public Map<String, Set<String>> getIdToForms() {
		return idToForms;
	}

	public Set<String> getForms() {
		return forms;
	}
	
	public static <T, S, U extends Collection<S>> void write(PrintStream pw, Map<T, U> dictionary) throws FileNotFoundException {

		for (T key: dictionary.keySet()){
			pw.println(key);
			U entries = dictionary.get(key);
			for (S entry: entries){
				pw.print("\t");
				pw.println(entry.toString());
			}
			pw.println();
		}

		pw.close();
	}

	public static URL getDefaultLexconnFile(){
		return FrConnectiveClassifier.getLexconn();
	}

	public static DefaultLexconnReader getLexconnMap() throws ParserConfigurationException, SAXException, IOException{
		return getLexconnMap(getDefaultLexconnFile());
	}

	public static DefaultLexconnReader getLexconnMap(File lexconnFile) throws ParserConfigurationException, SAXException, IOException{
		return getLexconnMap(lexconnFile.toURI().toURL());
	}
	
	public static DefaultLexconnReader getLexconnMap(URL lexconnFile) throws ParserConfigurationException, SAXException, IOException{
		LexconnParser lexconnParser = new LexconnParser();
		DefaultLexconnReader lexconnHandler = new DefaultLexconnReader();
		lexconnParser.parse(lexconnFile, lexconnHandler);
		return lexconnHandler;
	};

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		File output = new File("outputs/lexconn.txt");
		DefaultLexconnReader lexconnReader = getLexconnMap(new File(LEXCONN_FILE));
		DefaultLexconnReader.write(new PrintStream(output), lexconnReader.getLexconnDic().asMap());
		Map<String, Collection<String>> idToRelations = lexconnReader.getIdToRelations().asMap();
		
		System.out.printf("#<DC, REL> = %d\n", lexconnReader.getIdToRelations().size());
		System.out.printf("Connectives with a relation count = %d\n", idToRelations.size());
		Set<String> relations = new HashSet<>();
		idToRelations.forEach((id, rels) -> relations.addAll(rels));
		System.out.println("Relations: " + relations);
		for (String relation: relations){
			Set<String> dcs = new HashSet<>();
			idToRelations.forEach((id, rels) -> {if (rels.contains(relation)) dcs.add(id);});
			System.out.println("Relation = " + relation + ", Cnt = " + dcs.size());
		}
		Map<String, String> idToCanonicalForm = lexconnReader.getIdToCanonicalForm();
		System.out.printf("Total number of connective = %d, total number of forms = %d\n", idToCanonicalForm.size(), lexconnReader.getForms().size());
		Set<String> ids = new TreeSet<>(idToCanonicalForm.keySet());
		ids.removeAll(idToRelations.keySet());
		System.out.printf("Connectives with out relations (%d): %s\n", ids.size(), ids);
		for (String id: ids)
			idToCanonicalForm.remove(id);
		
		Scanner scanner = new Scanner(System.in);
		String connective;
		do{
			System.out.println("Please type a French connective:");
			connective = scanner.nextLine().trim();
			String id = lexconnReader.getFormToId().get(connective);
			if (id != null){
				System.out.println("Connective relations: " + lexconnReader.getIdToRelations().get(id));
			} else if (connective.length() > 0) {
				System.out.println("Cannot find the connective " + connective);
			}
		} while (connective.length() != 0);
		
		scanner.close();
//		Set<String> canonicalForms = new TreeSet<>(idToCanonicalForm.values());
		
//		System.out.printf("All forms of connecives without relations:\n%s", canonicalForms.toString().replace(", ", "\n"));
	}
}
