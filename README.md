NFRLocator
==========
This repo contains additions to code from https://github.com/RealsearchGroup/NFRLocator. 
The original project finds and categorizes non-functional requirements within unconstrained natural language documents.It contains NFR category listing, labeled documents, and software

Our additions/interpretation:
Produce a list of requirements given an SRS document in txt format. 

Source Code:
NFRLocator contains an Eclipse which uses Maven do the builds.  Java 1.8 is currently used.

To Run NFRLocator on your local machine:
1. Download WordNetDictionary at http://wordnetcode.princeton.edu/wn3.1.dict.tar.gz (https://wordnet.princeton.edu/wordnet/download/current-version/)
2. Open NFRLocator on Eclipse. 
3. Locate the GCController.java file, then make sure to use the following run configuration:
    Program Arguments: -w "User/PathToTheDictionaryOnYourDevice/WordNetDictionary" -l
    VM Arguments: -Xmx4096m  
4. Make sure that you are using the correct version of Java. This project uses Java 1.8:
  a) If you are using a different version, download Java 1.8; refer to this link (https://www.oracle.com/java/technologies/downloads/).
  b) Go to preferences on Eclipse, switch Java JRE to 1.8, click apply, and close.
  c) Go to Eclipse preferences and switch compiler compliance level to 1.8.
5. You can run the project locally on Eclipse or through the terminal.
