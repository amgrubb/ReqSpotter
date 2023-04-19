ReqSpotter for GitRE
==========
This repo contains additions to code from https://github.com/RealsearchGroup/NFRLocator. 
The original project finds and categorizes non-functional requirements within unconstrained natural language documents. It contains NFR category listing, labeled documents, and software

**Our additions/interpretation:**

Produce a txt file of requirements in an SRS document given in PDF format. 

**Source Code:**

NFRLocator contains an Eclipse which uses Maven do the builds.  Java 1.8 is currently used.

## To Run NFRLocator on your local machine: ##

1. Download WordNetDictionary at http://wordnetcode.princeton.edu/wn3.1.dict.tar.gz (https://wordnet.princeton.edu/wordnet/download/current-version/)
2. Open the NFRLocator directory of this repo on Eclipse as a project. 
3. Open the `GCController.java` file, located at NFRLocator/srs/edu/ncsu/csc/nl.
4. Edit the run configurations to contain the following in the Arguments tab:
    **Program Arguments:** -w "User/**PathToTheWordNetDictionaryOnYourDevice**/WordNetDictionary" -l
    
    **VM Arguments:** -Xmx4096m  
    
4. Make sure that you are using the correct version of Java. This project uses Java 1.8:
    
    a) If you are using a different version, download Java 1.8; refer to this link (https://www.oracle.com/java/technologies/downloads/).
    
    b) Go to Eclipse preferences. In Java --> Installed JREs, switch Java JRE to 1.8, click Apply.
    
    c) Go to Eclipse preferences. In Java --> Compiler, switch the compiler compliance level to 1.8.
    
5. You can run the project locally on Eclipse or through the terminal.

TODO: add instructions about how to input PDF of SRS document.
TODO: add instructions about how to run the program.
