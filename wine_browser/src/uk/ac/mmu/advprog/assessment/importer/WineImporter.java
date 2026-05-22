package uk.ac.mmu.advprog.assessment.importer;
import java.io.*;
import java.sql.*;
import java.util.*;



public class WineImporter implements AutoCloseable {
	/* JDBC connection */
    private Connection conn;

    /* Pre‑compiled SQL statements */
    private PreparedStatement insertRegionStmt;
    private PreparedStatement insertWineryStmt;
    private PreparedStatement insertWineStmt;
    private PreparedStatement insertGrapeStmt;
    private PreparedStatement selectGrapeIDStmt;
    private PreparedStatement insertWineGrapeStmt;
    private PreparedStatement insertVintageStmt;
    private PreparedStatement insertPairingStmt;
    private PreparedStatement selectPairingIDStmt;
    private PreparedStatement insertWinePairingStmt;


    /* ------------------------------------------------------------ */
    /*  Construction / Database initialisation                      */
    /* ------------------------------------------------------------ */

    public WineImporter() throws SQLException {
   	
    	this.conn = DriverManager.getConnection("jdbc:sqlite:data/wines.db");
    	this.conn.setAutoCommit(false);
    	
    	createTables(this.conn);
    	
    	prepareStatements();
    	
    }
    
	private void createTables(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {

            st.execute("""
                CREATE TABLE IF NOT EXISTS Region (
                    id      INTEGER PRIMARY KEY,
                    name    TEXT NOT NULL,
                    country TEXT
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS Winery (
                    id        INTEGER PRIMARY KEY,
                    name      TEXT NOT NULL,
                    region_id INTEGER NOT NULL,
                    website   TEXT,
                    FOREIGN KEY (region_id) REFERENCES Region(id)
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS Wine (
                    id          INTEGER PRIMARY KEY,
                    name        TEXT NOT NULL,
                    type        TEXT,
                    blend_type  TEXT,
                    abv         REAL,
                    acidity     TEXT,
                    body        TEXT,
                    winery_id   INTEGER NOT NULL,
                    FOREIGN KEY (winery_id) REFERENCES Winery(id)
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS Grape (
                    id   INTEGER PRIMARY KEY,
                    name TEXT NOT NULL UNIQUE
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS Wine_Grape (
                    wine_id  INTEGER NOT NULL,
                    grape_id INTEGER NOT NULL,
                    PRIMARY KEY (wine_id, grape_id),
                    FOREIGN KEY (wine_id)  REFERENCES Wine(id) ,
                    FOREIGN KEY (grape_id) REFERENCES Grape(id)
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS Wine_Vintage (
                    wine_id INTEGER NOT NULL,
                    year    TEXT NOT NULL,
                    PRIMARY KEY (wine_id, year),
                    FOREIGN KEY (wine_id) REFERENCES Wine(id)
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS Pairing (
                    id   INTEGER PRIMARY KEY,
                    food TEXT NOT NULL UNIQUE
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS Wine_Pairing (
                    wine_id    INTEGER NOT NULL,
                    pairing_id INTEGER NOT NULL,
                    PRIMARY KEY (wine_id, pairing_id),
                    FOREIGN KEY (wine_id)    REFERENCES Wine(id)   ,
                    FOREIGN KEY (pairing_id) REFERENCES Pairing(id)
                )
            """);
        }
    }
	
	private void prepareStatements() throws SQLException {
        // Region
        insertRegionStmt = conn.prepareStatement(
                "INSERT OR IGNORE INTO Region (id, name, country) VALUES (?, ?,?)");

        // Winery
        insertWineryStmt = conn.prepareStatement(
        		"INSERT OR IGNORE INTO Winery (id, name, region_id, website) VALUES (?, ?, ?, ?)");

        // Wine
        insertWineStmt = conn.prepareStatement(
                "INSERT OR IGNORE INTO Wine (id, name, type, blend_type, abv, acidity, body, winery_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

        // Grape
        insertGrapeStmt = conn.prepareStatement(
                "INSERT OR IGNORE INTO Grape (name) VALUES (?)");
        selectGrapeIDStmt = conn.prepareStatement(
                "SELECT id FROM Grape WHERE name = ?");

        // Wine_Grape
        insertWineGrapeStmt = conn.prepareStatement(
                "INSERT OR IGNORE INTO Wine_Grape (wine_id, grape_id) VALUES (?, ?)");

        // Wine_Vintage
        insertVintageStmt = conn.prepareStatement(
                "INSERT OR IGNORE INTO Wine_Vintage (wine_id, year) VALUES (?, ?)");

        // Pairing
        insertPairingStmt = conn.prepareStatement(
        	    "INSERT OR IGNORE INTO Pairing (food) VALUES (?)");
        selectPairingIDStmt = conn.prepareStatement(
                "SELECT id FROM Pairing WHERE food = ?");

        // Wine_Pairing
        insertWinePairingStmt = conn.prepareStatement(
                "INSERT OR IGNORE INTO Wine_Pairing (wine_id, pairing_id) VALUES (?, ?)");
    }
	
    
    public void importFromCSV(String csvFilePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            int counter = 0;

            if ((line = br.readLine()) != null && line.contains("WineID")) {
                // Skips header line
            }

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Wine wine = parseLineToWine(line);

                // Insert region
                insertRegionStmt.setInt(1, wine.wineID); 
                insertRegionStmt.setString(2, wine.regionName);
                insertRegionStmt.setString(3, wine.country);
                insertRegionStmt.executeUpdate();

                // Insert winery
                insertWineryStmt.setInt(1, wine.wineryID);
                insertWineryStmt.setString(2, wine.wineryName);
                insertWineryStmt.setInt(3, wine.regionID); 
                insertWineryStmt.setString(4, wine.website);
                insertWineryStmt.executeUpdate();

                // Insert wine
                insertWineStmt.setInt(1, wine.wineID);
                insertWineStmt.setString(2, wine.wineName);
                insertWineStmt.setString(3, wine.type);
                insertWineStmt.setString(4, wine.elaborate);
                insertWineStmt.setDouble(5, wine.abv);
                insertWineStmt.setString(6, wine.acidity);
                insertWineStmt.setString(7, wine.body);
                insertWineStmt.setInt(8, wine.wineryID);
                insertWineStmt.executeUpdate();

                // Insert grapes and wine-grape relationships
                for (String grapeName : wine.grapes) {
                    int grapeID = getOrCreateGrape(grapeName);
                    insertWineGrapeStmt.setInt(1, wine.wineID);
                    insertWineGrapeStmt.setInt(2, grapeID);
                    insertWineGrapeStmt.executeUpdate();
                }

                // Insert vintages and wine-vintage relationships
                for (String vintageYear : wine.vintage) {

                    insertVintageStmt.setInt(1, wine.wineID);
                    insertVintageStmt.setString(2, vintageYear);

                    insertVintageStmt.executeUpdate();
                }
                
                for (String pairingFood : wine.harmonise) {

                    if (pairingFood == null || pairingFood.isBlank()) continue;

                    // 1. Insert pairing if not exists
                    insertPairingStmt.setString(1, pairingFood);
                    insertPairingStmt.executeUpdate();

                    // 2. Get pairing ID
                    selectPairingIDStmt.setString(1, pairingFood);
                    int pairingID;

                    try (ResultSet rs = selectPairingIDStmt.executeQuery()) {
                        if (rs.next()) {
                            pairingID = rs.getInt("id");
                        } else {
                            throw new SQLException("Failed to retrieve pairing id for: " + pairingFood);
                        }
                    }

                    // 3. Insert into join table
                    insertWinePairingStmt.setInt(1, wine.wineID);
                    insertWinePairingStmt.setInt(2, pairingID);
                    insertWinePairingStmt.executeUpdate();
                }
                
                
                counter++;
                if (counter % 10000 == 0) {
                    conn.commit();
                }
            }

            // Final commit
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollEx) {
                throw new RuntimeException("Rollback failed: " + rollEx.getMessage(), rollEx);
            }
            throw new RuntimeException("Failed to import from CSV: " + e.getMessage(), e);
        }
    }

    
    private Wine parseLineToWine(String line) {
        List<String> fields = splitCsvLine(line);
        if (fields.size() < 17) {
            throw new IllegalArgumentException("CSV line does not contain enough columns: " + line);
        }

        int wineID = Integer.parseInt(fields.get(0));
        String wineName = fields.get(1);
        
        String type = fields.get(2);
        String elaborate = fields.get(3);
        List<String> grapes = parseList(fields.get(4));
        List<String> harmonise = parseList(fields.get(5));
        double abv = Double.parseDouble(fields.get(6));
        String body = fields.get(7);
        String acidity = fields.get(8);
        String country = fields.get(10);
        int regionID = Integer.parseInt(fields.get(11));
        String regionName = fields.get(12);
        
        int wineryID = Integer.parseInt(fields.get(13));
        String wineryName = fields.get(14);
        String website = fields.get(15);
        List<String> vintages = parseList(fields.get(16));
        

        return new Wine(wineID, wineName, type, elaborate, grapes, harmonise, 
        		abv, body, acidity, regionID, regionName, country, wineryID, 
        		wineryName, website, vintages);
    }

    
    private List<String> splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();

        boolean inQuotes = false;

        for (char c : line.toCharArray()) {

            if (c == '"') {
                inQuotes = !inQuotes;

            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField.setLength(0);

            } else {
                currentField.append(c);
            }
        }

        fields.add(currentField.toString());

        return fields;
    }

  
    private List<String> parseList(String raw) {
        List<String> items = new ArrayList<>();

        if (raw == null || raw.isBlank()) {
            return items;
        }

        String cleaned = raw.trim();

        // Remove outer brackets
        if (cleaned.startsWith("[") && cleaned.endsWith("]")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }

        if (cleaned.isBlank()) {
            return items;
        }

        String[] parts = cleaned.split(",");

        for (String part : parts) {

            String item = part.trim();

            // Remove surrounding quotes
            if ((item.startsWith("'") && item.endsWith("'")) ||
                (item.startsWith("\"") && item.endsWith("\""))) {

                item = item.substring(1, item.length() - 1);
            }

            items.add(item);
        }

        return items;
    }

    /**
     * Retrieves or creates a grape record and returns its primary key.
     *
     * @param grapeName the grape name
     * @return the grape's primary key
     * @throws SQLException if a database error occurs
     */
    private int getOrCreateGrape(String grapeName) throws SQLException {
        // First, try to get existing grape
        selectGrapeIDStmt.setString(1, grapeName);
        try (ResultSet rs = selectGrapeIDStmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        
        // Grape doesn't exist, insert it
        insertGrapeStmt.setString(1, grapeName);
        insertGrapeStmt.executeUpdate();
        
        // Now retrieve the newly inserted grape's ID
        selectGrapeIDStmt.setString(1, grapeName);
        try (ResultSet rs = selectGrapeIDStmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new SQLException("Failed to retrieve grape id for: " + grapeName);
            }
        }
    }


    /**
     * Closes all prepared statements and the underlying connection.
     *
     * @throws SQLException if a database error occurs
     */
    public void close() throws SQLException {
        if (insertRegionStmt != null) insertRegionStmt.close();
        if (insertWineryStmt != null) insertWineryStmt.close();
        if (insertWineStmt != null) insertWineStmt.close();
        if (insertGrapeStmt != null) insertGrapeStmt.close();
        if (selectGrapeIDStmt != null) selectGrapeIDStmt.close();
        if (insertWineGrapeStmt != null) insertWineGrapeStmt.close();
        if (insertVintageStmt != null) insertVintageStmt.close();
        if (insertPairingStmt != null) insertPairingStmt.close();
        if (selectPairingIDStmt != null) selectPairingIDStmt.close();
        if (insertWinePairingStmt != null) insertWinePairingStmt.close();
        if (conn != null && !conn.isClosed()) conn.close();
    }
    
    
}

