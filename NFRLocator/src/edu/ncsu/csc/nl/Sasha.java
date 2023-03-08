package edu.ncsu.csc.nl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

import edu.ncsu.csc.nl.model.NLDocument;
import edu.ncsu.csc.nl.model.Sentence;
import edu.ncsu.csc.nl.model.classification.ClassificationType;
import edu.ncsu.csc.nl.model.distance.LevenshteinSentenceAsWordsDistance;
import edu.ncsu.csc.nl.model.ml.ClassificationResult;
import edu.ncsu.csc.nl.model.ml.InstanceLearner;

public class Sasha {
	
	private NLDocument _currentDocument;
	private String     _currentDocumentID = "unknown";	
	private int _currentSentenceNumber = -1;
	
	private File _currentFileLocation = null;
	
	private GCController controller = GCController.getTheGCController();
	
	public void testMethod(String message) {
		System.out.println(message);
		
		moveSentencesFromJSONtoLearner("/Users/sashayeutseyeva/Documents/Smith/Thesis/slankasPresentation/amb-parsed.json");
		
		InstanceLearner _theInstanceLearner = controller.getInstanceLearner();
		
		System.out.println("sentence in learner: ");
		
		System.out.println(_theInstanceLearner.getTrainedSentenceAt(1).getSentence());
		
		// read sentences from txt file 
		
		ArrayList<Sentence> sentencesToClassify = getSentencesFromTxtDoc("/Users/sashayeutseyeva/Documents/Smith/Thesis/slankasPresentation/2009-warc-III.txt");
		
		// classify sentences 
		
		
		
		for (int i = 0; i<sentencesToClassify.size();i++) {
			Sentence s = sentencesToClassify.get(i);
			
			
			try {
				FileWriter outputWriter = new FileWriter("filename.txt",true);
				outputWriter.write("sentence: \n");
				outputWriter.write(s._orginalSentence);
				outputWriter.write("\n");
				outputWriter.close();
			} catch (IOException e) {
				System.out.println("An error occurred.");
				e.printStackTrace();
			}
			
			getSentenceClassification(s);
			
		}
		
		// get it to print out the classifications of each sentence along with the sentence
		
		

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
			
			// HashMap<String, ClassificationType> classificationList = r.classifications;
				
			//System.out.println(r.k+": "+r);
			
			//Let's add a threshold on this
			if (r.averageDistance > (sentence.getNumberOfNodes()*.85)) {
				System.out.println("Not using results of IBL - avg distance > .85 * number of nodes");
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