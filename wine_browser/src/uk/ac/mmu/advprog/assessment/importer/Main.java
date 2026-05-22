package uk.ac.mmu.advprog.assessment.importer;

public class Main {
public static void main(String[] args) {
     
        
        
        try(WineImporter importer = new WineImporter()){
        	importer.importFromCSV("data/XWines_Full_100K_wines.csv");
        }
        catch(Exception e) {
        	System.out.println(e);
        }
        

    }
}
