package edu.ncsu.csc.nl;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.pdmodel.*;

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
import java.nio.file.Paths;
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
import java.io.*;


public class ReqSpotter {
	
	private NLDocument _currentDocument;
	private String     _currentDocumentID = "unknown";	
	private int _currentSentenceNumber = -1;
	private File _currentFileLocation = null;
	
	private GCController controller = GCController.getTheGCController();
	
	/**

	This method drives the overall process of extracting requirements from a PDF file.
	It first checks if the learner file exists, if not, it updates the learner from training data
	and saves it to a specified file path. 
	
	Creates a formatted text file from the given PDF file path and retrieves a list of sentences
	from the formatted text file. The sentences are then classified and the requirements are extracted
	from the classified sentences. The extracted requirements are validated against a specified answer key file.
	
	@param pdfPath 			The path to the PDF file to extract requirements from
	@param outputPath 		The path and name of the output txt file where the requirements will be printed
	@throws Exception 		If an error occurs during the process of extracting requirements
	*/
	
	public void driver(String pdfPath, String outputPath) throws Exception {
		InstanceLearner _theInstanceLearner = controller.getInstanceLearner();
		String txtPath = "./reqsTXT/byproduct.txt";
		
		File learnerFile = new File("./learners/learner");
		if(!learnerFile.exists()) { 
			updateLearnerFromTrainingData("./trainingData" );
			saveLearner(_theInstanceLearner, true,"./learners" );
		}
		
		learnerFile = new File("./learners/learner-small");	
		_theInstanceLearner.loadFromSerializedFile(learnerFile);
		
        createFormattedTxtFileFromPDF(pdfPath, txtPath);
		

		ArrayList<Sentence> sentencesToClassify = getSentencesFromTxtDoc(txtPath);
		
		
		// classify sentences 
		Set<String> foundRequirements = classification3(sentencesToClassify);
		
		writeRequirementsToTxtFile(outputPath,foundRequirements);
		
		

	}
	
	/**

	Returns a new instance of the {@code ReqSpotter} class, which is used to locate software requirements in text.
	@return a new instance of the {@code ReqSpotter} class
	*/
	public static ReqSpotter getReqSpotter() {
		return new ReqSpotter();
	}
	
	/**
	*
	* Writes found requirements to a txt file. 
	Extracts text from PDF and writes it into  txt file so that each line represents a sentence.
	@param outputFileName 				path to txt file to be written
	@param requirements				set of strings of requirements
	
	*/
	public static void writeRequirementsToTxtFile(String outputFileName, Set<String> requirements) {        
        StringBuilder contents = new StringBuilder();
        for (String requirement : requirements) {
        	contents.append(requirement).append("\n");
        }
        
        try {
            // write the modified contents to the output text file
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));
            writer.write(contents.toString());
            writer.close();
            System.out.println("and done writing the text");
        	
        } catch (IOException e) {
	        System.out.println("An error occurred while writing reqs to txt file: " + e.getMessage());
        }
		
	}
	
	/**
	*
	Extracts text from PDF and writes it into  txt file so that each line represents a sentence.
	@param txtFileName 				path to txt file to be written
	@param pdfFileName				path to pdf file to be used
	@catches IOException 			if an I/O error occurs while removing line breaks and extra spaces
	
	*/
	public static void createFormattedTxtFileFromPDF(String pdfFileName, String txtFileName) {
		try {
	        // create PDF document object
	        PDDocument document = PDDocument.load(new File(pdfFileName));

	        // create PDF text stripper object to extract text
	        PDFTextStripper stripper = new PDFTextStripper();

	        // set the line separator for text extraction
	        stripper.setLineSeparator("\n");

	        // get the extracted text
	        String extractedText = stripper.getText(document);

	        // close the PDF document
	        document.close();
	    	

	     // process the extracted text
	        StringBuilder contents = new StringBuilder();
	        String[] sentences = extractedText.split("(?<=[.?!])\\s+");
	        for (String sentence : sentences) {
	            // detect headers
	            boolean isHeader = sentence.matches("^[A-Z][a-z]+(\\s[A-Za-z]+){0,4}$");
	            if (!isHeader) {
	                // remove extra spaces within the same sentence
	                sentence = sentence.trim().replaceAll("\\s{2,}", " ");
	                contents.append(sentence).append("\n"); // add newline after each line
	            } else {
	                // don't add space after header line
	                contents.append(sentence).append("\n");
	            }
	        }


	        // write the modified contents to the output text file
	        BufferedWriter writer = new BufferedWriter(new FileWriter(txtFileName));
	        writer.write(contents.toString());
	        writer.close();
	        System.out.println("and done writing the text");

	    } catch (IOException e) {
	        System.out.println("An error occurred while processing the PDF file: " + e.getMessage());
	    }
	}


	
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
	 * 
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
	        File f = new File(folder, "learner");
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

	
	/**

	This method takes in an ArrayList of sentences and classifies them using a learner.
	It then checks each sentence to see if it is a requirement and adds any requirements found to
	an ArrayList of requirements.
	
	@param 			sentences an ArrayList of Sentence objects to be classified and checked for requirements
	@return 		ArrayList of String objects representing the original sentences that were classified as requirements
	*/
	
	private Set<String> classification1(ArrayList<Sentence> sentences) {
		Set<String> foundReqs = new HashSet<String>();
		
		for (int i = 0; i<sentences.size();i++) {
			// get the sentence to classify
			Sentence s = sentences.get(i);
			
			// get the sentence classifications
			getSentenceClassification(s);
			
			if (checkIfSentenceRequirement(s)) {
				foundReqs.add(s._orginalSentence);
			}
		}
		
		return foundReqs;
		
	}
	
	private Set<String> classification2(ArrayList<Sentence> sentences) {
		Set<String> foundReqs = new HashSet<String>();
		
		boolean newReqFound = true;
		
		while (newReqFound) {
			newReqFound = false;
			for (int i = 0; i<sentences.size();i++) {
				// get the sentence to classify
				Sentence s = sentences.get(i);
				if (!foundReqs.contains(s._orginalSentence)) {
					// get the sentence classifications
					getSentenceClassification(s);
					
					if (checkIfSentenceRequirement(s)) {
						newReqFound = true;
						s.processTrainedSentence();
						// if it is marked as a requirement, add it to a list of requirements, just the sentence itself
						foundReqs.add(s._orginalSentence);
					}
				}
			}
			
		}
		
		return foundReqs;
		
	}
	
	private Set<String> classification3(ArrayList<Sentence> sentences) {
		Set<String> foundReqs = new HashSet<String>();
		
		Set<Sentence> sentencesToAdd = new HashSet<Sentence>();
		
		boolean newReqFound = true;
		
		while (newReqFound) {
			newReqFound = false;
			for (int i = 0; i<sentences.size();i++) {
				// get the sentence to classify
				Sentence s = sentences.get(i);
				if (!foundReqs.contains(s._orginalSentence)) {
					// get the sentence classifications
					getSentenceClassification(s);
					
					if (checkIfSentenceRequirement(s)) {
						newReqFound = true;
						sentencesToAdd.add(s);
						
						// if it is marked as a requirement, add it to a list of requirements, just the sentence itself
						foundReqs.add(s._orginalSentence);
					}
				}
			}
			
			// add reqs to learner here 
			for (Sentence s : sentencesToAdd) {
				s.processTrainedSentence();
			}
			sentencesToAdd.clear();
						
		}
		
		return foundReqs;	
	}
	
	
	
	/**

	Prints the boolean classifications of a given sentence, along with the original sentence.
	If the sentence does not have any boolean classifications, this method does nothing.
	@param 			sentence The sentence whose boolean classifications are to be printed.
	*/
	private void printSentenceClassifications(Sentence sentence) {
		String classificationsString = sentence.getAllBooleanClassificationsAsString();

		if (!classificationsString.matches("")) {
			System.out.println(sentence._orginalSentence);
			System.out.println("Boolean classifications: "+sentence.getAllBooleanClassificationsAsString());
			
			checkIfSentenceRequirement(sentence);
		}
	}
	
	/**

	Validates the results by comparing the found requirements with the true requirements
	in the answer key file. Calculates the recall and precision scores and prints them
	to the console. Also prints the requirements that were not recognized.

	@param answerKeyFile 		the file location of the answer key containing the true requirements
	@param foundRequirements 	an ArrayList of Strings containing the requirements found by the system
	*/
	private void validateResults(String answerKeyFile, Set<String> foundRequirements) {
		Set<String> trueRequirements = getReqsFromAnswerKey(answerKeyFile);
		
		int truePositive = 0;
		int numberFound = foundRequirements.size();
		int numberOfTrueRequirements = trueRequirements.size();
		
		
		for (String foundRequirement: foundRequirements) {
			if (trueRequirements.contains(foundRequirement)) {
				truePositive++;	
			} else {
				System.out.println("FALSE REQ: "+foundRequirement);
			}
		}
	
		float recall = (float) truePositive/numberOfTrueRequirements;
		float precision = (float) truePositive/numberFound;
		
		System.out.println("RECALL: " + recall);
		System.out.println("PRECISION: " + precision);
		
	}
	
	
	/**

	Prints a list of requirements that were not recognized by the system, based on a given answer key file and a list of found requirements.
	@param answerKeyFile 			The file containing the correct requirements for the system to recognize.
	@param foundRequirements 		ArrayList containing the requirements found by the system.
	*/
	private void printReqsNotRecognized(String answerKeyFile, ArrayList<String> foundRequirements) {
		Set<String> trueRequirements = getReqsFromAnswerKey(answerKeyFile);
		
		System.out.println("Reqs not found: **********");
		
		for (String req:trueRequirements) {
			if (!foundRequirements.contains(req)) {
				System.out.println(req);
			}
		}
		
	}
	
	/**

	Reads the requirements from an answer key file and returns them as a set of strings.

	@param answerKeyFileName the name of the file containing the answer key
	@return a set of strings representing the requirements
	*/
	private Set<String> getReqsFromAnswerKey(String answerKeyFileName){
		Set<String> reqs = new HashSet<String>();
		
		try {
            BufferedReader reader = new BufferedReader(new FileReader(answerKeyFileName));
            String line;
            while ((line = reader.readLine()) != null) {
            	reqs.add(line);
            }
            reader.close();
   
        } catch (IOException e) {
            System.out.println("An error occurred while removing line breaks and extra spaces: " + e.getMessage());
        }	
		
		return reqs;
		
	}
	
	/**

	Checks if a given sentence is classified as a requirement.

	@param 			sentence the sentence to be checked
	@return 		true if the sentence is classified as a requirement, false otherwise
	*/
	private boolean checkIfSentenceRequirement(Sentence sentence) {
		
		String classificationsString = sentence.getAllBooleanClassificationsAsString();
		
		if (classificationsString.matches("")) {
			return false;
		} 
		return true;
	}
	
	/**

	Gets the classification of a given sentence using the instance learner.

	@param sentence the Sentence object to classify
	*/
	private void getSentenceClassification( Sentence sentence) {
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
				
			if (r.averageDistance > (sentence.getNumberOfNodes()*0.99)) {
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
	
	
	/**

	Loads a JSON file and moves all the sentences to the instance learner.

	@param fileLocation the location of the JSON file to load
	*/
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
			s.moveSentenceToInstanceLearner();	
		}
	}
	
	/**

	Retrieves sentences from a text document located at the specified file location and returns them as an ArrayList of Sentence objects.

	@param fileLocation the file location of the text document to retrieve sentences from

	@return an ArrayList of Sentence objects representing the sentences in the text document
	*/
	private ArrayList<Sentence> getSentencesFromTxtDoc(String fileLocation) {
		System.out.println(_currentFileLocation);
		
		_currentDocument = new NLDocument();
		_currentDocument.setFileLocation(fileLocation);
		_currentDocument.loadAndParse(controller.getPipeline());
		_currentFileLocation = new File(fileLocation);
		
		ArrayList<Sentence> allSentences = _currentDocument.getSentences();
		
		return allSentences;
		
	}
	
	
	/**

	This method loads a JSON file and creates an NLDocument object from it.
	The loaded document becomes the current document and its location becomes the current file location.
	If the loaded document contains any sentences, the document ID of the first sentence becomes the
	current document ID.
	@param 					f a File object representing the JSON file to be loaded
	@throws 				Exception if there is an error loading the file or creating an NLDocument object from it
	*/
	public void loadJSONFile(File f) throws Exception{
		
		_currentDocument = NLDocument.readFromJSONFile(f);	
		_currentFileLocation = f;
		
		if (_currentDocument.getNumberOfSentences() > 0) {
			_currentDocumentID = _currentDocument.getElementAt(0).getDocumentID();
		}
	}

	/**
	
	This method loads a text file in JSON format and creates an NLDocument object from it.
	The loaded document becomes the current document and its location becomes the current file location.
	If the loaded document contains any sentences, the document ID of the first sentence becomes the
	current document ID.
	@param 				f a File object representing the JSON file to be loaded
	@throws 			Exception if there is an error loading the file or creating an NLDocument object from it
	*/
	public void loadTxtFile(File f) throws Exception{
			
			_currentDocument = NLDocument.readFromJSONFile(f);	
			_currentFileLocation = f;
			
			if (_currentDocument.getNumberOfSentences() > 0) {
				_currentDocumentID = _currentDocument.getElementAt(0).getDocumentID();
			}
	}
}