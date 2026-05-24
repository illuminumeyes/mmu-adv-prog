package uk.ac.mmu.advprog.assessment.importer;
import java.io.*;
import java.sql.*;
import java.util.*;



public class WineImporter implements AutoCloseable {
    private Connection conn;

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

    public WineImporter() throws SQLException {
    	this.conn = DriverManager.getConnection("jdbc:sqlite:data/wines.db");
    	this.conn.setAutoCommit(false);
    	
    	createTables(this.conn);
    	
    	prepareStatements();
    	
    }
    
	/**
	 * Creates database tables and connects them to match
     * the assessment brief's structure
	 *
	 * @param conn the database connection
	 * @throws SQLException if a database access error occurs
	 */
	private void createTables(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {

            st.execute("""
                CREATE TABLE IF NOT EXISTS Region (
                    id      INTEGER PRIMARY KEY,
                    name    TEXT NOT NULL,
                    country TEXT,
                    UNIQUE (name, country)
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS Winery (
                    id        INTEGER PRIMARY KEY,
                    name      TEXT NOT NULL,
                    region_id INTEGER NOT NULL,
                    website   TEXT,
                    FOREIGN KEY (region_id) REFERENCES Region(id),
                    UNIQUE (name, region_id)
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
                    FOREIGN KEY (winery_id) REFERENCES Winery(id),
                    UNIQUE (name, winery_id, abv, type)
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
	
	/**
	 * Prepares all SQL statements for insertion
	 *
	 * @throws SQLException if a database access error occurs during statement preparation
	 */
	private void prepareStatements() throws SQLException {
        insertRegionStmt = conn.prepareStatement(
                "INSERT OR IGNORE INTO Region (id, name, country) VALUES (?, ?, ?)");

        
        insertWineryStmt = conn.prepareStatement(
        		"INSERT OR IGNORE INTO Winery (id, name, region_id, website) VALUES (?, ?, ?, ?)");

        insertWineStmt = conn.prepareStatement(
                "INSERT OR IGNORE INTO Wine (id, name, type, blend_type, abv, acidity, body, winery_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        
        insertGrapeStmt = conn.prepareStatement(
                "INSERT OR IGNORE INTO Grape (name) VALUES (?)");
        selectGrapeIDStmt = conn.prepareStatement(
                "SELECT id FROM Grape WHERE LOWER(name) = LOWER(?)");
        
        insertWineGrapeStmt = conn.prepareStatement(
                "INSERT OR IGNORE INTO Wine_Grape (wine_id, grape_id) VALUES (?, ?)");

        insertVintageStmt = conn.prepareStatement(
                "INSERT OR IGNORE INTO Wine_Vintage (wine_id, year) VALUES (?, ?)");

        insertPairingStmt = conn.prepareStatement(
        	    "INSERT OR IGNORE INTO Pairing (food) VALUES (?)");
        selectPairingIDStmt = conn.prepareStatement(
                "SELECT id FROM Pairing WHERE LOWER(food) = LOWER(?)");

        insertWinePairingStmt = conn.prepareStatement(
                "INSERT OR IGNORE INTO Wine_Pairing (wine_id, pairing_id) VALUES (?, ?)");
    }
	
    
    /**
     * Imports wine data from a CSV file into the database.
     * Reads the CSV file line by line, parses each line into a Wine object,
     * and inserts the data into the database with relationships to regions,
     * wineries, grapes, vintages, and pairings. 
     * Committed in batches of 10,000 as suggested by brief
     *
     * @param csvFilePath the file path to the CSV file to import
     * @throws IOException if an I/O error occurs while reading the file
     */
    public void importFromCSV(String csvFilePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            int counter = 0;

            // Skip first header line of CSV
            if ((line = br.readLine()) != null && line.contains("WineID")) {
            }

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Wine wine = parseLineToWine(line);

                insertRegionStmt.setInt(1, wine.regionID); 
                insertRegionStmt.setString(2, wine.regionName);
                insertRegionStmt.setString(3, wine.country);
                insertRegionStmt.executeUpdate();

                insertWineryStmt.setInt(1, wine.wineryID);
                insertWineryStmt.setString(2, wine.wineryName);
                insertWineryStmt.setInt(3, wine.regionID); 
                insertWineryStmt.setString(4, wine.website);
                insertWineryStmt.executeUpdate();

                insertWineStmt.setInt(1, wine.wineID);
                insertWineStmt.setString(2, wine.wineName);
                insertWineStmt.setString(3, wine.type);
                insertWineStmt.setString(4, wine.elaborate);
                insertWineStmt.setDouble(5, wine.abv);
                insertWineStmt.setString(6, wine.acidity);
                insertWineStmt.setString(7, wine.body);
                insertWineStmt.setInt(8, wine.wineryID);
                insertWineStmt.executeUpdate();

                for (String grapeName : wine.grapes) {
                    int grapeID = getOrCreateGrape(grapeName);
                    insertWineGrapeStmt.setInt(1, wine.wineID);
                    insertWineGrapeStmt.setInt(2, grapeID);
                    insertWineGrapeStmt.executeUpdate();
                }

                for (String vintageYear : wine.vintage) {

                    insertVintageStmt.setInt(1, wine.wineID);
                    insertVintageStmt.setString(2, vintageYear);

                    insertVintageStmt.executeUpdate();
                }
                
                for (String pairingFood : wine.harmonise) {

                    if (pairingFood == null || pairingFood.isBlank()) continue;

                    String formattedFood = pairingFood.trim();

                    insertPairingStmt.setString(1, formattedFood);
                    insertPairingStmt.executeUpdate();

                    selectPairingIDStmt.setString(1, formattedFood);
                    int pairingID;

                    try (ResultSet rs = selectPairingIDStmt.executeQuery()) {
                        if (rs.next()) {
                            pairingID = rs.getInt("id");
                        } else {
                            throw new SQLException("Failed to retrieve pairing id for: " + formattedFood);
                        }
                    }

                    insertWinePairingStmt.setInt(1, wine.wineID);
                    insertWinePairingStmt.setInt(2, pairingID);
                    insertWinePairingStmt.executeUpdate();
                }
                
                // Commit in batches of 10k rows
                counter++;
                if (counter % 10000 == 0) {
                    conn.commit();
                }
            }

            conn.commit();
        } catch (SQLException e) {
                throw new RuntimeException("Failed to import from CSV: " + e.getMessage(), e);
            }
        }
    

    
    /**
     * Parses a single CSV line into a Wine object.
     *
     * @param line the CSV line to parse
     * @return a wine object populated with data from the CSV line
     * @throws IllegalArgumentException if the line contains insufficient columns
     */
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

    
    /**
     * Splits a CSV line into fields, respecting quoted values.
     * Handles CSV format where fields can be quoted and may contain commas
     * within the quotes without treating them as field separators.
     *
     * @param line the CSV line to split
     * @return a list of field values
     */
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

  
    /**
     * Parses a list into individual items
     *
     * @param raw the string representation of a list
     * @return a List of parsed items, or an empty list if raw is null/blank
     */
    private List<String> parseList(String raw) {
        List<String> items = new ArrayList<>();

        if (raw == null || raw.isBlank()) {
            return items;
        }

        String trimmed = raw.trim();

        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }

        if (trimmed.isBlank()) {
            return items;
        }

        String[] parts = trimmed.split(",");

        for (String part : parts) {

            String item = part.trim();

            if ((item.startsWith("'") && item.endsWith("'")) ||
                (item.startsWith("\"") && item.endsWith("\""))) {

                item = item.substring(1, item.length() - 1);
            }

            items.add(item);
        }

        return items;
    }

    /**
     * Gets the database ID for a grape variety, creating it if it doesn't exist.
     * If not found, inserts the grape and retrieves its new ID.
     *
     * @param grapeName the name of the grape variety
     * @return the database ID of the grape
     * @throws SQLException if a database access error occurs
     */
    private int getOrCreateGrape(String grapeName) throws SQLException {
        String formattedName = grapeName.trim();
        
        selectGrapeIDStmt.setString(1, formattedName);
        try (ResultSet rs = selectGrapeIDStmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        
        insertGrapeStmt.setString(1, formattedName);
        insertGrapeStmt.executeUpdate();
        
        selectGrapeIDStmt.setString(1, formattedName);
        try (ResultSet rs = selectGrapeIDStmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new SQLException("Failed to retrieve grape id for: " + formattedName);
            }
        }
    }

    @Override
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

