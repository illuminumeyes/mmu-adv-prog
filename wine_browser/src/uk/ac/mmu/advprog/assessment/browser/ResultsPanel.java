package uk.ac.mmu.advprog.assessment.browser;

import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class ResultsPanel extends JPanel {

    private final Connection conn;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
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

        String[] columnNames = {"ID", "Name", "Type", "Winery", "Country", "ABV"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resultsTable = new JTable(tableModel);
        resultsTable.setBackground(new Color(45, 45, 45));
        resultsTable.setForeground(Color.WHITE);
        resultsTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        resultsTable.setSelectionBackground(new Color(70, 70, 70));
        resultsTable.getTableHeader().setBackground(new Color(40, 40, 40));
        resultsTable.getTableHeader().setForeground(Color.WHITE);
        resultsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));

        resultsTable.removeColumn(resultsTable.getColumnModel().getColumn(0));

        sorter = new TableRowSorter<>(tableModel);
        
        sorter.setComparator(5, (o1, o2) -> {
            try {
                String s1 = o1.toString().replaceAll("[^0-9.]", "");
                String s2 = o2.toString().replaceAll("[^0-9.]", "");
                double v1 = s1.isEmpty() ? 0 : Double.parseDouble(s1);
                double v2 = s2.isEmpty() ? 0 : Double.parseDouble(s2);
                return Double.compare(v1, v2);
            } catch (NumberFormatException e) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        
        resultsTable.setRowSorter(sorter);

        resultsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = resultsTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int modelRow = resultsTable.convertRowIndexToModel(selectedRow);
                    Object wineIdObj = tableModel.getValueAt(modelRow, 0); // Hidden ID column
                    if (wineIdObj != null && listener != null) {
                        try {
                            int wineId = Integer.parseInt(wineIdObj.toString());
                            listener.onWineSelected(wineId);
                        } catch (NumberFormatException ex) {
                            System.err.println("Error parsing wine ID: " + wineIdObj);
                        }
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.getViewport().setBackground(new Color(45, 45, 45));

        add(title, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }


    /**
     * Searches for wines
     * Builds and executes a dynamic SQL query with filters
     * Results are displayed in the results table and sorted by wine name.
     *
     * @param filters a Map containing filter field names and values
     */
    public void searchWinesMultiField(Map<String, String> filters) {

        clearTable();

        StringBuilder sql = new StringBuilder("""
            SELECT DISTINCT
                Wine.id,
                Wine.name,
                Wine.type,
                Winery.name AS winery_name,
                Region.country,
                Wine.abv
            FROM Wine
            LEFT JOIN Winery
                ON Wine.winery_id = Winery.id
            LEFT JOIN Region
                ON Winery.region_id = Region.id
            LEFT JOIN Wine_Grape
                ON Wine.id = Wine_Grape.wine_id
            LEFT JOIN Grape
                ON Wine_Grape.grape_id = Grape.id
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        for (Map.Entry<String, String> entry : filters.entrySet()) {

            String field = entry.getKey();
            String value = entry.getValue();

            if (value == null || value.isBlank()) {
                continue;
            }

            value = value.trim();

            switch (field) {

                case "abvMin" -> {

                    try {
                        double minAbv = Double.parseDouble(value);

                        sql.append(" AND Wine.abv >= ? ");
                        params.add(minAbv);

                    } catch (NumberFormatException ignored) {
                    }
                }

                case "abvMax" -> {

                    try {
                        double maxAbv = Double.parseDouble(value);

                        sql.append(" AND Wine.abv <= ? ");
                        params.add(maxAbv);

                    } catch (NumberFormatException ignored) {
                    }
                }

                case "grape" -> {

                    sql.append("""
                        AND EXISTS (
                            SELECT 1
                            FROM Wine_Grape
                            JOIN Grape
                                ON Wine_Grape.grape_id = Grape.id
                            WHERE Wine_Grape.wine_id = Wine.id
                            AND LOWER(Grape.name) LIKE ?
                        )
                    """);

                    params.add("%" + value.toLowerCase() + "%");
                }

                case "elaborate" -> {

                    sql.append(" AND LOWER(Wine.elaborate) LIKE ? ");
                    params.add("%" + value.toLowerCase() + "%");
                }

                default -> {

                    String columnName = getColumnName(field);

                    if (columnName == null || columnName.isBlank()) {
                        continue;
                    }

                    sql.append(" AND LOWER(")
                       .append(columnName)
                       .append(") LIKE ? ");

                    params.add("%" + value.toLowerCase() + "%");
                }
            }
        }

        sql.append(" ORDER BY Wine.name ASC");

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {

                Object param = params.get(i);

                if (param instanceof Double d) {
                    stmt.setDouble(i + 1, d);

                } else if (param != null) {
                    stmt.setString(i + 1, param.toString());
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {

                int rowCount = 0;
                while (rs.next()) {
                    rowCount++;
                    addRow(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getString("winery_name"),
                        rs.getString("country"),
                        rs.getDouble("abv")
                    );
                }
            }

        } catch (SQLException e) {

            showError(e);
        }
    }

    private String getColumnName(String field) {
        return switch (field) {
            case "wineName" -> "Wine.name";
            case "type" -> "Wine.type";
            case "country" -> "Region.country";
            case "regionName" -> "Region.name";
            case "wineryName" -> "Winery.name";
            case "body" -> "Wine.body";
            case "acidity" -> "Wine.acidity";
            default -> field;
        };
    }

    private void addRow(int id, String name, String type, String winery, String country, double abv) {
        tableModel.addRow(new Object[]{id, name, type, winery, country, String.format("%.1f%%", abv)});
    }

    private void clearTable() {
        tableModel.setRowCount(0);
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
}