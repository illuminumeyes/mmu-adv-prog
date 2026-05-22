package uk.ac.mmu.advprog.assessment.browser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

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
                            .append("\n\n");

                    sb.append("Winery: ")
                            .append(rs.getString("winery_name"))
                            .append("\n");

                    sb.append("Region: ")
                            .append(rs.getString("region_name"))
                            .append("\n");

                    sb.append("Country: ")
                            .append(rs.getString("country"))
                            .append("\n");

                    detailsArea.setText(sb.toString());
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
}