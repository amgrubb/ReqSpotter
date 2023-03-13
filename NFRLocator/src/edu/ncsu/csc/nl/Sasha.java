package edu.ncsu.csc.nl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import edu.ncsu.csc.nl.model.NLDocument;
import edu.ncsu.csc.nl.model.Sentence;
import edu.ncsu.csc.nl.model.classification.ClassificationType;
import edu.ncsu.csc.nl.model.distance.LevenshteinSentenceAsWordsDistance;
import edu.ncsu.csc.nl.model.ml.ClassificationResult;
import edu.ncsu.csc.nl.model.ml.InstanceLearner;
import edu.ncsu.csc.nl.Sonora;


public class Sasha {
	
	private NLDocument _currentDocument;
	private String     _currentDocumentID = "unknown";	
	private int _currentSentenceNumber = -1;
	
	private File _currentFileLocation = null;
	
	private GCController controller = GCController.getTheGCController();
	
	public void testMethod(String message) {
		System.out.println(message);
		
		InstanceLearner _theInstanceLearner = controller.getInstanceLearner();
		
		// TODO: make a function that creates a learner from all the JSON files available using Slankas functions
		// TODO: make a function that saves the learner to specified location using Slankas functions
		
		moveSentencesFromJSONtoLearner("./trainingData/amb-parsed.json");
		System.out.println("##### number of sentences in learner: "+_theInstanceLearner.getTrainedSentences().size());
		
		moveSentencesFromJSONtoLearner("./trainingData/CMS-DUA.json");
		System.out.println("##### number of sentences in learner: "+_theInstanceLearner.getTrainedSentences().size());
		
		//call the sonora code to extract txt file from pdf
		Sonora sonora = new Sonora();
		
		
		System.out.println("sentence in learner: ");
		
		System.out.println(_theInstanceLearner.getTrainedSentenceAt(1).getSentence());
		
		
		Sonora.main(new String[]{});

		// read sentences from txt file 
		ArrayList<Sentence> sentencesToClassify = getSentencesFromTxtDoc("./reqsTXT/2010.txt");
		
		ArrayList<String> foundRequirements = new ArrayList<String>();
		// classify sentences 
		
		// cycle over all the sentences we need to classify, do this in 3 iterations
		
		for (int n = 0; n<3;n++) {
			
			for (int i = 0; i<sentencesToClassify.size();i++) {
				// get the sentence to classify
				Sentence s = sentencesToClassify.get(i);
				
				// get the sentence classifications
				getSentenceClassification(s);
				
				// printSentenceClassifications(s);
				
				// add the data to the training set under a condition
				// for now, if it is marked as a requirement add it to training dataset
				
				if (checkIfSentenceRequirement(s)) {
					s.processTrainedSentence();
					// if it is marked as a requirement, add it to a list of requirements, just the sentence itself
					foundRequirements.add(s._orginalSentence);
				}
				
				
				
			}
			
			Set<String> uniqueReqs = new HashSet<String>(foundRequirements);
			
			System.out.println("***********ROUND" + n + " RESULTS***********");
			System.out.println("requirements found: "+uniqueReqs.size());
			
			
			for (String sentence : uniqueReqs) {
				System.out.println(sentence);
			}
			
			
		}
		
		
		
		// get it to write out the list of requirements to a document i guess? 
		
		

	}
	
	private void printSentenceClassifications(Sentence sentence) {
		
		String classificationsString = sentence.getAllBooleanClassificationsAsString();
		
		if (!classificationsString.matches("")) {
			System.out.println(sentence._orginalSentence);
			System.out.println("Boolean classifications: "+sentence.getAllBooleanClassificationsAsString());
			
			checkIfSentenceRequirement(sentence);
		}
		
		
		
	}
	
	private void validateResults(String answerKeyFile, String resultsFile) {
		
		// TODO: make this function
		
		// load answerKeyFile reqs into a Set 
		
		// load resultsFile reqs into an array list
		
		// calculate how many false positives, false negatives, true positives there are
		// maybe figure out how to do true negatives? Do we care? 
		
	}
	
	private boolean checkIfSentenceRequirement(Sentence sentence) {
		
		String classificationsString = sentence.getAllBooleanClassificationsAsString();
		
		if (classificationsString.matches("")) {
			return false;
		} 

		return true;
		
		
//		HashMap<String, ClassificationType> classificationsMap = sentence.getClassifications();
//		
//		for (String name: classificationsMap.keySet()) {
//		    String key = name.toString();
//		    String value = classificationsMap.get(name).toString();
//		    System.out.println(key + " " + value);
//		}
//		
		
	}
	
	private void getSentenceClassification( Sentence sentence) {
		// System.out.println(sentence.getRoot().getStringRepresentationUltraCollapsed());
		
		// System.out.println("Evaluating: "+sentence._orginalSentence);
		// System.out.println("\tText sent to parser: "+sentence.getParserSentence());
		
		if (!sentence._processed && (!sentence.isTrained() || GCController.getTheGCController().isSupervisedLearning())) { 
			ClassificationResult r = GCController.getTheGCController().getInstanceLearner().getClassification(sentence,GCController.getTheGCController().getKForInstanceLearner(), new LevenshteinSentenceAsWordsDistance(),true);
			
			try {
				FileWriter outputWriter = new FileWriter("filename.txt",true);
				outputWriter.write("RESULTS: \n");
				outputWriter.write(r.toString());
				outputWriter.write("\n");
				outputWriter.close();
			} catch (IOException e) {
				System.out.println("An error occurred.");
				e.printStackTrace();
			}
				
			//System.out.println(r.k+": "+r);
			
			//Let's add a threshold on this
			if (r.averageDistance > (sentence.getNumberOfNodes()*.85)) {
				// System.out.println("Not using results of IBL - avg distance > .85 * number of nodes");
			}
			else {
				if (!sentence.isTrained()) {
					sentence.setClassifications(r.classifications);
				}

				sentence._processed = false;
			
			}
		}
		
	}
	
	private void moveSentencesFromJSONtoLearner(String fileLocation) {
		System.out.println(_currentFileLocation);
		File learnerF = new File(fileLocation);
		
		try {
			loadJSONFile(learnerF);
		}
		catch (Exception e) {
			System.err.println("Unable to load document: "+e);
			e.printStackTrace();
		}
		
		System.out.println(_currentFileLocation);
		
		ArrayList<Sentence> allSentences = _currentDocument.getSentences();
		
		for (int i = 0; i<allSentences.size();i++) {
			Sentence s = allSentences.get(i);
			// System.out.println(s._orginalSentence);
			s.moveSentenceToInstanceLearner();
			
		}
		
		
	}
	
	private ArrayList<Sentence> getSentencesFromTxtDoc(String fileLocation) {
		System.out.println(_currentFileLocation);
		
		_currentDocument = new NLDocument();
		_currentDocument.setFileLocation(fileLocation);
		_currentDocument.loadAndParse(controller.getPipeline());
		_currentFileLocation = new File(fileLocation);
		
		ArrayList<Sentence> allSentences = _currentDocument.getSentences();
		
		return allSentences;
		
	}
	
	private  Sasha() {
	}
	
	public static Sasha getSasha() {
		return new Sasha();
	}
	
	public void loadJSONFile(File f) throws Exception{
		
		_currentDocument = NLDocument.readFromJSONFile(f);	
		_currentFileLocation = f;
		
		if (_currentDocument.getNumberOfSentences() > 0) {
			_currentDocumentID = _currentDocument.getElementAt(0).getDocumentID();
		}
		
		
		
	}
	
public void loadTxtFile(File f) throws Exception{
		
		_currentDocument = NLDocument.readFromJSONFile(f);	
		_currentFileLocation = f;
		
		if (_currentDocument.getNumberOfSentences() > 0) {
			_currentDocumentID = _currentDocument.getElementAt(0).getDocumentID();
		}
		
		
		
	}

}