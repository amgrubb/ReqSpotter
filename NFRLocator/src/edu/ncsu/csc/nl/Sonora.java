package edu.ncsu.csc.nl;

import java.io.*;

public class Sonora {

    public static void main(String[] args) {

        String fileName = "./reqsTXT/maple-bakery.txt"; 
    
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            StringBuilder contents = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                // remove extra spaces within the same sentence
                line = line.trim().replaceAll("\\s{2,}", " ");
                contents.append(line).append(" "); // add space after each line
            }
            reader.close();
    
            String modifiedContents = contents.toString().replaceAll("\\.\\s+", ".\n"); // replace ". " with ".\n"
    
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(modifiedContents);
            writer.close();
    
            
        } catch (IOException e) {
            System.out.println("An error occurred while removing line breaks and extra spaces: " + e.getMessage());
        }
    }
    
}
