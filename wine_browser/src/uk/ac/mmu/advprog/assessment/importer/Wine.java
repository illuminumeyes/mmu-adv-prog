package uk.ac.mmu.advprog.assessment.importer;

import java.util.List;

public class Wine {
	public int wineID;
	public String wineName;
	public String type;
	public String elaborate;
	public List<String> grapes;
	public List<String> harmonise;
	public double abv;
	public String body;
	public String acidity;
    public int regionId;
    public String regionName;
    public String country;
    public int wineryId;
    public String wineryName;
    public String website;
    public List<String> vintage;
    
    
    
    public Wine(int wineId,
            String wineName,
            String type,
            String elaborate,
            List<String> grapes,
            List<String> harmonise,
            double abv,
            String body,
            String acidity,
            int regionId,
            String regionName,
            String country,
            int wineryId,
            String wineryName,
            String website,
            List<String> vintage) {

    this.wineID      = wineId;
    this.wineName    = wineName;
    this.type        = type;
    this.elaborate   = elaborate;
    this.grapes      = grapes;
    this.harmonise   = harmonise;
    this.abv         = abv;
    this.body        = body;
    this.acidity     = acidity;
    this.regionId    = regionId;
    this.regionName  = regionName;
    this.country     = country;
    this.wineryId    = wineryId;
    this.wineryName  = wineryName;
    this.website     = website;
    this.vintage     = vintage;
}

@Override
public String toString() {
    return "Wine{" + "wineId=" + wineID + ", wineName='" + wineName + '\'' + '}';
}
}
