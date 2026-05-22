package uk.ac.mmu.advprog.assessment.browser;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ResultsPanel extends JPanel {

    private Connection conn;

    private DefaultListModel<WineResult> model;
    private JList<WineResult> list;

    private WineSelectionListener listener;

    public ResultsPanel(Connection conn) {

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

        JLabel title = new JLabel("Search Results");

        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        model = new DefaultListModel<>();

        list = new JList<>(model);

        list.setBackground(new Color(45, 45, 45));
        list.setForeground(Color.WHITE);

        list.setFont(new Font("SansSerif", Font.PLAIN, 16));

        list.addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()) {

                WineResult selected = list.getSelectedValue();

                if (selected != null && listener != null) {
                    listener.onWineSelected(selected.id);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(list);

        add(title, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadAllWines() {

        model.clear();

        String sql = """
                SELECT id, name
                FROM Wine
                ORDER BY name
                """;

        try (
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)
        ) {

            while (rs.next()) {

                model.addElement(
                        new WineResult(
                                rs.getInt("id"),
                                rs.getString("name")
                        )
                );
            }

        } catch (SQLException e) {
            showError(e);
        }
    }

    public void searchWines(String query) {

        model.clear();

        String sql = """
                SELECT id, name
                FROM Wine
                WHERE LOWER(name) LIKE ?
                ORDER BY name
                """;

        try (
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setString(1, "%" + query.toLowerCase() + "%");

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    model.addElement(
                            new WineResult(
                                    rs.getInt("id"),
                                    rs.getString("name")
                            )
                    );
                }
            }

        } catch (SQLException e) {
            showError(e);
        }
    }

    public void setWineSelectionListener(WineSelectionListener listener) {
        this.listener = listener;
    }

    private void showError(Exception e) {

        JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private static class WineResult {

        int id;
        String name;

        public WineResult(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}