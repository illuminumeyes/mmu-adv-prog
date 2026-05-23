package uk.ac.mmu.advprog.assessment.browser;

public class Wine {
    public int id;
    public String name;
    public String type;
    public String wineryName;
    public String country;
    public double abv;
    public String body;
    public String acidity;
    public String blendType;

    public Wine(int id, String name, String type, String wineryName, String country, 
                double abv, String body, String acidity, String blendType) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.wineryName = wineryName;
        this.country = country;
        this.abv = abv;
        this.body = body;
        this.acidity = acidity;
        this.blendType = blendType;
    }

    @Override
    public String toString() {
        return name;
    }
}
