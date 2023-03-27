package edu.ncsu.csc.nl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFileChooser;

import edu.ncsu.csc.nl.model.NLDocument;
import edu.ncsu.csc.nl.model.Sentence;
import edu.ncsu.csc.nl.model.classification.ClassificationType;
import edu.ncsu.csc.nl.model.distance.LevenshteinSentenceAsWordsDistance;
import edu.ncsu.csc.nl.model.ml.ClassificationResult;
import edu.ncsu.csc.nl.model.ml.Document;
import edu.ncsu.csc.nl.model.ml.InstanceLearner;
import edu.ncsu.csc.nl.Sonora;


public class Sasha {
	
	private NLDocument _currentDocument;
	private String     _currentDocumentID = "unknown";	
	private int _currentSentenceNumber = -1;
	
	private File _currentFileLocation = null;
	
	private GCController controller = GCController.getTheGCController();
//	private InstanceLearner _theInstanceLearner = new InstanceLearner(); //maybe create new learner
	
	
	/**
	*
	Reads training data from folder of JSON files and adds trained sentences to instance learner.
	@param path 					the path to the folder containing the training data
	@throws FileNotFoundException 	if the specified path is not found
	@throws ClassNotFoundException 	if the NLDocument class cannot be found
	@throws IOException 			if an I/O error occurs while reading the training data
	
	*/
	public void updateLearnerFromTrainingData(String path) throws FileNotFoundException, ClassNotFoundException, IOException {
	    File folder = new File(path);
	    if (folder.exists() && folder.isDirectory()) {
	    	java.io.File[] items = folder.listFiles();
	        if (items != null) {
	            for (File item : items) {
	                if (item.isFile() && item.getName().endsWith(".json")) {
	                    NLDocument document = NLDocument.readFromJSONFile(item);
	                    document.addAllTrainedSentencesToInstanceLearner();
	                }
	            }
	        }
	    }
	}
	
	/**

	Saves an instance learner to a file in the specified location. The saved file is either serialized
	or not based on the value of the serialized parameter.
	@param learner 			the instance learner to be saved (if saving default learner, use _theInstanceLearner )
	@param serialized 		true if the learner should be serialized, false otherwise
	@param location 		the path of the directory where the file should be saved
	@throws IOException 	if an error occurs while writing the file
	*/
	
	private void saveLearner(InstanceLearner learner, boolean serialized, String location) throws Exception {
		File folder = new File(location);
	    if (folder.exists() && folder.isDirectory()) {
	        File f = new File(folder, "learner-small");
	        try {
	            if (serialized) {
	                learner.saveToSerializedObjectFile(f);
	            } else {
	                learner.saveToFile(f);
	            }
	            System.out.println("Learner saved successfully.");
	        } catch (IOException e) {
	            System.err.println("Error saving learner: " + e.getMessage());
	        }
	    } else {
	        System.err.println("Invalid folder location specified.");
	    }
	}

	
	private ArrayList<String> classification1(ArrayList<Sentence> sentences) {
		// just go through and classify with learner
		ArrayList<String> foundRequirements = new ArrayList<String>();
		
		for (int i = 0; i<sentences.size();i++) {
			// get the sentence to classify
			Sentence s = sentences.get(i);
			
			// get the sentence classifications
			getSentenceClassification(s);
			
			if (checkIfSentenceRequirement(s)) {
				foundRequirements.add(s._orginalSentence);
			}
		}
		
		return foundRequirements;
		
	}
	
	private ArrayList<String> classification2(ArrayList<Sentence> sentences) {
		// 3 cycles, add as you go
		ArrayList<String> foundRequirements = new ArrayList<String>();
		Set<String> uniqueReqs = new HashSet<String>();
		
		for (int n = 0; n<3;n++) {
			
			for (int i = 0; i<sentences.size();i++) {
				// get the sentence to classify
				Sentence s = sentences.get(i);
				
				if (!foundRequirements.contains(s._orginalSentence)) {
					// get the sentence classifications
					getSentenceClassification(s);
					
					// add the data to the training set under a condition
					// for now, if it is marked as a requirement add it to training dataset
					
					if (checkIfSentenceRequirement(s)) {
						s.processTrainedSentence();
						// if it is marked as a requirement, add it to a list of requirements, just the sentence itself
						foundRequirements.add(s._orginalSentence);
					}
				}
				
			}
			
			uniqueReqs = new HashSet<String>(foundRequirements);
			
//			System.out.println("***********ROUND" + n + " RESULTS***********");
//			System.out.println("requirements found: "+uniqueReqs.size());
//			
//			
//			for (String sentence : uniqueReqs) {
//				System.out.println(sentence);
//			}
			
			
		}
		
		ArrayList<String> uniqueReqArray = new ArrayList<String>();
		
		for (String req: uniqueReqs) {
			uniqueReqArray.add(req);
		}
		
		return uniqueReqArray;
		
	}
	
	private ArrayList<String> classification3(ArrayList<Sentence> sentences) {
		// 3 cycles, add after each round
		ArrayList<String> foundRequirements = new ArrayList<String>();
		
		return foundRequirements;
		
	}
	
	private ArrayList<String> classification4(ArrayList<Sentence> sentences) {
		// user input required classification. 
		ArrayList<String> foundRequirements = new ArrayList<String>();
		
		return foundRequirements;
		
	}
	
	
	public void testMethod() throws Exception {
		
		InstanceLearner _theInstanceLearner = controller.getInstanceLearner();
		
		File learnerFile = new File("./learners/learner-small");
		if(!learnerFile.exists()) { 
			// learner doesn't exist
			updateLearnerFromTrainingData("./trainingData" );
			saveLearner(_theInstanceLearner, true,"./learners" );
		}
		
		learnerFile = new File("./learners/learner-small");	
		_theInstanceLearner.loadFromSerializedFile(learnerFile);
		
//		moveSentencesFromJSONtoLearner("./trainingData/amb-parsed.json");
//		moveSentencesFromJSONtoLearner("./trainingData/promisedataALL - parsed.json");
//		saveLearner(_theInstanceLearner, true,"./learners" );
		
	
		//call the sonora code to extract txt file from pdf
		//TODO: change Sonora to take in a string of the txt file of the PDF
		// TODO: change sonora to create just a generic byproduct.txt file? Don't take the same name as original txt file I think
		Sonora sonora = new Sonora();
		Sonora.main(new String[]{});
		
		// Sonora.createFormattedTxtFile("./reqsTXT/2010.txt");

		// read sentences from txt file 
		//TODO: change to take in string of txt file that sonora creates 
		ArrayList<Sentence> sentencesToClassify = getSentencesFromTxtDoc("./reqsTXT/2009-warc-III.txt");
		
		
		// classify sentences 
		ArrayList<String> foundRequirements = classification2(sentencesToClassify);
		
		// get it to write out the list of requirements to a document i guess? 
		
		// TODO: method that writes requirements to txt file
		validateResults("./reqsTXTanswers/2009-warc-III-reqs.txt",foundRequirements);

	}
	
	private void printSentenceClassifications(Sentence sentence) {
		
		String classificationsString = sentence.getAllBooleanClassificationsAsString();
		
		if (!classificationsString.matches("")) {
			System.out.println(sentence._orginalSentence);
			System.out.println("Boolean classifications: "+sentence.getAllBooleanClassificationsAsString());
			
			checkIfSentenceRequirement(sentence);
		}
	}
	
	private void validateResults(String answerKeyFile, ArrayList<String> foundRequirements) {
		Set<String> trueRequirements = getReqsFromAnswerKey(answerKeyFile);
		
		int truePositive = 0;
		int numberFound = foundRequirements.size();
		int numberOfTrueRequirements = trueRequirements.size();
		
		
		for (String foundRequirement: foundRequirements) {
			if (trueRequirements.contains(foundRequirement)) {
				truePositive++;	
			} 
		}
	
		float recall = (float) truePositive/numberOfTrueRequirements;
		float precision = (float) truePositive/numberFound;
		
		System.out.println("RECALL: " + recall);
		System.out.println("PRECISION: " + precision);
		
		printReqsNotRecognized(answerKeyFile, foundRequirements);
	}
	
	private void printReqsNotRecognized(String answerKeyFile, ArrayList<String> foundRequirements) {
		Set<String> trueRequirements = getReqsFromAnswerKey(answerKeyFile);
		
		System.out.println("Reqs not found: **********");
		
		for (String req:trueRequirements) {
			if (!foundRequirements.contains(req)) {
				System.out.println(req);
			}
		}
		
	}
	
	private Set<String> getReqsFromAnswerKey(String answerKeyFileName){
		Set<String> reqs = new HashSet<String>();
		
		try {
            BufferedReader reader = new BufferedReader(new FileReader(answerKeyFileName));
            String line;
            while ((line = reader.readLine()) != null) {
            	reqs.add(line);
                // remove extra spaces within the same sentence
            }
            reader.close();
   
        } catch (IOException e) {
            System.out.println("An error occurred while removing line breaks and extra spaces: " + e.getMessage());
        }	
		
		return reqs;
		
	}
	
	private boolean checkIfSentenceRequirement(Sentence sentence) {
		
		String classificationsString = sentence.getAllBooleanClassificationsAsString();
		
		if (classificationsString.matches("")) {
			return false;
		} 

		return true;
	
		
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
			// SASHA CHANGED THRESHOLD NUMBER
			if (r.averageDistance > (sentence.getNumberOfNodes()*0.85)) {
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