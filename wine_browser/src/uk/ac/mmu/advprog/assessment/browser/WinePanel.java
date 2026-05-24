package uk.ac.mmu.advprog.assessment.browser;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class WinePanel extends JPanel {

    private Connection conn;

    private JTextArea detailsArea;

    public WinePanel(Connection conn) {

        this.conn = conn;

        initUI();
    }

    private void initUI() {

        setLayout(new BorderLayout(10, 10));

        setBackground(new Color(30, 30, 30));

        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70), 2, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel title = new JLabel("Wine Details");

        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        detailsArea = new JTextArea();

        detailsArea.setEditable(false);

        detailsArea.setBackground(new Color(45, 45, 45));
        detailsArea.setForeground(Color.WHITE);

        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 15));

        JScrollPane scrollPane = new JScrollPane(detailsArea);

        add(title, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Loads and displays detailed information for a specific wine.
     * Retrieves wine details from the database including name, type, ABV, body,
     * acidity, winery, region, and appends grape varieties, available vintages,
     * and food pairing suggestions.
     *
     * @param wineId the database ID of the wine to load
     */
    public void loadWine(int wineId) {

        String sql = """
                SELECT
                    Wine.name AS wine_name,
                    Wine.type,
                    Wine.blend_type,
                    Wine.abv,
                    Wine.body,
                    Wine.acidity,
                    Winery.name AS winery_name,
                    Winery.website,
                    Region.name AS region_name,
                    Region.country
                FROM Wine
                JOIN Winery ON Wine.winery_id = Winery.id
                JOIN Region ON Winery.region_id = Region.id
                WHERE Wine.id = ?
                """;

        try (
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, wineId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    StringBuilder sb = new StringBuilder();

                    sb.append("Wine Name: ")
                            .append(rs.getString("wine_name"))
                            .append("\n\n");

                    sb.append("Type: ")
                            .append(rs.getString("type"))
                            .append("\n");

                    sb.append("Blend: ")
                            .append(rs.getString("blend_type"))
                            .append("\n");

                    sb.append("ABV: ")
                            .append(rs.getDouble("abv"))
                            .append("%\n");

                    sb.append("Body: ")
                            .append(rs.getString("body"))
                            .append("\n");

                    sb.append("Acidity: ")
                            .append(rs.getString("acidity"))
                            .append("\n");

                    sb.append("Winery: ")
                            .append(rs.getString("winery_name"))
                            .append("\n");

                    String website = rs.getString("website");
                    if (website != null && !website.isEmpty()) {
                        sb.append("Website: ")
                                .append(website)
                                .append("\n");
                    }

                    sb.append("Region: ")
                            .append(rs.getString("region_name"))
                            .append("\n");

                    sb.append("Country: ")
                            .append(rs.getString("country"))
                            .append("\n");

                    appendGrapeVarieties(sb, wineId);
                    appendVintages(sb, wineId);
                    appendPairings(sb, wineId);

                    detailsArea.setText(sb.toString());
                } else {
                    detailsArea.setText("No wine details found for ID: " + wineId);
                }
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Appends the grape varieties for a wine to the details display.
     * Queries the database for all grapes associated with the wine and formats
     * them as a comma-separated list.
     *
     * @param sb the StringBuilder to append grape data to
     * @param wineId the database ID of the wine
     */
    private void appendGrapeVarieties(StringBuilder sb, int wineId) {
        String sql = """
                SELECT DISTINCT Grape.name
                FROM Wine_Grape
                JOIN Grape ON Wine_Grape.grape_id = Grape.id
                WHERE Wine_Grape.wine_id = ?
                ORDER BY Grape.name
                """;

        try (
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, wineId);
            try (ResultSet rs = stmt.executeQuery()) {
                boolean hasGrapes = false;
                while (rs.next()) {
                    if (!hasGrapes) {
                        sb.append("Grapes: ");
                        hasGrapes = true;
                    } else {
                        sb.append(", ");
                    }
                    sb.append(rs.getString("name"));
                }
                if (hasGrapes) {
                    sb.append("\n\n");
                }
            }
        } catch (SQLException e) {
            sb.append("Grapes: [Error loading]\n\n");
        }
    }

    /**
     * Appends the available vintages for a wine to the details display.
     * Queries the database for all vintage years associated with the wine
     * and displays them in reverse chronological order.
     *
     * @param sb the StringBuilder to append vintage data to
     * @param wineId the database ID of the wine
     */
    private void appendVintages(StringBuilder sb, int wineId) {
        String sql = """
                SELECT DISTINCT Wine_Vintage.year
                FROM Wine_Vintage
                WHERE Wine_Vintage.wine_id = ?
                ORDER BY Wine_Vintage.year DESC
                """;

        try (
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, wineId);
            try (ResultSet rs = stmt.executeQuery()) {
                boolean hasVintages = false;
                while (rs.next()) {
                    if (!hasVintages) {
                        sb.append("Available Vintages: ");
                        hasVintages = true;
                    } else {
                        sb.append(", ");
                    }
                    sb.append(rs.getString("year"));
                }
                if (hasVintages) {
                    sb.append("\n\n");
                }
            }
        } catch (SQLException e) {
            sb.append("Available Vintages: [Error loading]\n\n");
        }
    }

    /**
     * Appends food pairing suggestions for a wine to the details display.
     * Queries the database for all food pairing suggestions associated with
     * the wine and formats them as a comma-separated list.
     *
     * @param sb the StringBuilder to append pairing data to
     * @param wineId the database ID of the wine
     */
    private void appendPairings(StringBuilder sb, int wineId) {
        String sql = """
                SELECT DISTINCT Pairing.food
                FROM Wine_Pairing
                JOIN Pairing ON Wine_Pairing.pairing_id = Pairing.id
                WHERE Wine_Pairing.wine_id = ?
                ORDER BY Pairing.food
                """;

        try (
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, wineId);
            try (ResultSet rs = stmt.executeQuery()) {
                boolean hasPairings = false;
                while (rs.next()) {
                    if (!hasPairings) {
                        sb.append("Food Pairings: ");
                        hasPairings = true;
                    } else {
                        sb.append(", ");
                    }
                    sb.append(rs.getString("food"));
                }
                if (hasPairings) {
                    sb.append("\n\n");
                }
            }
        } catch (SQLException e) {
            sb.append("Food Pairings: [Error loading]\n\n");
        }
    }
}