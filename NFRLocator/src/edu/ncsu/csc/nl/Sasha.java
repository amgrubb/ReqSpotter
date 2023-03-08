package edu.ncsu.csc.nl;

import java.io.File;
import java.util.ArrayList;

import edu.ncsu.csc.nl.model.NLDocument;
import edu.ncsu.csc.nl.model.Sentence;
import edu.ncsu.csc.nl.model.ml.InstanceLearner;

public class Sasha {
	
	private NLDocument _currentDocument;
	private String     _currentDocumentID = "unknown";	
	private int _currentSentenceNumber = -1;
	
	private File _currentFileLocation = null;
	
	public void testMethod(String message) {
		System.out.println(message);
		
		System.out.println(_currentFileLocation);
		File f = new File("/Users/sashayeutseyeva/Documents/Smith/Thesis/slankasPresentation/amb-parsed.json");
		
		try {
			loadJSONFile(f);
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
		
		InstanceLearner _theInstanceLearner = GCController.getTheGCController().getInstanceLearner();
		
		System.out.println("sentence in learner: ");
		
		System.out.println(_theInstanceLearner.getTrainedSentenceAt(1).getSentence());
		
		// create a sentence and classify it and see what happens
		// get it to print out the classifications of the sentence
		
		// next, try loading stuff to learner, and then importing a txt file and making a loop and classifying the sentences
		

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

}