package uk.ac.mmu.advprog.assessment.browser;

import java.awt.*;
import java.sql.*;
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

        // Create table model with column names
        String[] columnNames = {"Name", "Type", "Winery", "Country", "ABV"};
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

        // Add sorting support
        sorter = new TableRowSorter<>(tableModel);
        resultsTable.setRowSorter(sorter);

        // Add selection listener
        resultsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = resultsTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int modelRow = resultsTable.convertRowIndexToModel(selectedRow);
                    Object wineIdObj = tableModel.getValueAt(modelRow, 5); // Hidden ID column
                    if (wineIdObj != null && listener != null) {
                        listener.onWineSelected((Integer) wineIdObj);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.getViewport().setBackground(new Color(45, 45, 45));

        add(title, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void searchWines(String query, String label, String sortBy) {
        clearTable();

        String[] allowedFields = {"wineName", "type", "body", "acidity", "country", "regionName", "wineryName", "elaborate"};
        if (!isValidField(label, allowedFields)) {
            showError(new IllegalArgumentException("Invalid search field: " + label));
            return;
        }

        String orderBy = getOrderBy(sortBy);

        String sql = "SELECT Wine.id, Wine.name, Wine.type, Winery.name AS winery_name, Region.country, Wine.abv "
                + "FROM Wine "
                + "JOIN Winery ON Wine.winery_id = Winery.id "
                + "JOIN Region ON Winery.region_id = Region.id "
                + "WHERE LOWER(" + label + ") LIKE ? "
                + "ORDER BY " + orderBy;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + query.toLowerCase() + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    addRow(rs.getInt("id"), rs.getString("name"), rs.getString("type"),
                           rs.getString("winery_name"), rs.getString("country"), rs.getDouble("abv"));
                }
            }
        } catch (SQLException e) {
            showError(e);
        }
    }

    public void searchWinesMultiField(java.util.Map<String, String> filters, String sortBy) {
        clearTable();

        StringBuilder sql = new StringBuilder(
                "SELECT Wine.id, Wine.name, Wine.type, Winery.name AS winery_name, Region.country, Wine.abv "
                + "FROM Wine "
                + "JOIN Winery ON Wine.winery_id = Winery.id "
                + "JOIN Region ON Winery.region_id = Region.id "
                + "WHERE 1=1 ");

        java.util.List<String> params = new java.util.ArrayList<>();

        for (String field : filters.keySet()) {
            String value = filters.get(field);
            if (value == null || value.trim().isEmpty()) continue;

            if (field.equals("abvMin")) {
                try {
                    double minAbv = Double.parseDouble(value);
                    sql.append(" AND Wine.abv >= ? ");
                    params.add(String.valueOf(minAbv));
                } catch (NumberFormatException e) {
                    // Skip invalid ABV
                }
            } else if (field.equals("abvMax")) {
                try {
                    double maxAbv = Double.parseDouble(value);
                    sql.append(" AND Wine.abv <= ? ");
                    params.add(String.valueOf(maxAbv));
                } catch (NumberFormatException e) {
                    // Skip invalid ABV
                }
            } else if (field.equals("grape")) {
                sql.append(" AND Wine.id IN (SELECT wine_id FROM Wine_Grape JOIN Grape ON Wine_Grape.grape_id = Grape.id WHERE LOWER(Grape.name) LIKE ?) ");
                params.add("%" + value.toLowerCase() + "%");
            } else {
                String columnName = getColumnName(field);
                sql.append(" AND LOWER(").append(columnName).append(") LIKE ? ");
                params.add("%" + value.toLowerCase() + "%");
            }
        }

        String orderBy = getOrderBy(sortBy);
        sql.append(" ORDER BY ").append(orderBy);

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            for (String param : params) {
                if (param.matches("-?\\d+(\\.\\d+)?")) {
                    stmt.setDouble(paramIndex++, Double.parseDouble(param));
                } else {
                    stmt.setString(paramIndex++, param);
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    addRow(rs.getInt("id"), rs.getString("name"), rs.getString("type"),
                           rs.getString("winery_name"), rs.getString("country"), rs.getDouble("abv"));
                }
            }
        } catch (SQLException e) {
            showError(e);
        }
    }

    private boolean isValidField(String field, String[] allowedFields) {
        for (String allowed : allowedFields) {
            if (allowed.equals(field)) {
                return true;
            }
        }
        return false;
    }

    private String getColumnName(String field) {
        switch (field) {
            case "wineName":
                return "Wine.name";
            case "type":
                return "Wine.type";
            case "country":
                return "Region.country";
            case "regionName":
                return "Region.name";
            case "wineryName":
                return "Winery.name";
            case "body":
                return "Wine.body";
            case "acidity":
                return "Wine.acidity";
            default:
                return field; // fallback
        }
    }

    private String getOrderBy(String sortBy) {
        if ("ABV (High to Low)".equals(sortBy)) {
            return "Wine.abv DESC";
        } else if ("ABV (Low to High)".equals(sortBy)) {
            return "Wine.abv ASC";
        }
        return "Wine.name";
    }

    private void addRow(int id, String name, String type, String winery, String country, double abv) {
        tableModel.addRow(new Object[]{name, type, winery, country, String.format("%.1f%%", abv), id});
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