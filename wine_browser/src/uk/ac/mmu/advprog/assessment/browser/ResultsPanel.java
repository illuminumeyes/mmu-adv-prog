package uk.ac.mmu.advprog.assessment.browser;

import java.awt.*;
import java.sql.*;
import java.util.*;
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
        sorter.setComparator(5, this::compareAbv);
        resultsTable.setRowSorter(sorter);
        resultsTable.getSelectionModel().addListSelectionListener(e -> handleRowSelection(e));

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.getViewport().setBackground(new Color(45, 45, 45));

        add(title, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private int compareAbv(Object o1, Object o2) {
        try {
            double v1 = Double.parseDouble(o1.toString().replaceAll("[^0-9.]", ""));
            double v2 = Double.parseDouble(o2.toString().replaceAll("[^0-9.]", ""));
            return Double.compare(v1, v2);
        } catch (NumberFormatException e) {
            return o1.toString().compareTo(o2.toString());
        }
    }

    private void handleRowSelection(javax.swing.event.ListSelectionEvent e) {
        if (e.getValueIsAdjusting() || listener == null) return;
        
        int selectedRow = resultsTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = resultsTable.convertRowIndexToModel(selectedRow);
            try {
                int wineId = Integer.parseInt(tableModel.getValueAt(modelRow, 0).toString());
                listener.onWineSelected(wineId);
            } catch (NumberFormatException ex) {
                System.err.println("Error parsing wine ID");
            }
        }
    }

    public void searchWinesMultiField(Map<String, String> filters) {
        clearTable();
        
        QueryBuilder queryBuilder = new QueryBuilder();
        
        filters.forEach((field, value) -> {
            if (value != null && !value.isBlank()) {
                queryBuilder.addFilter(field, value.trim());
            }
        });
        
        executeQuery(queryBuilder);
    }

    private void executeQuery(QueryBuilder queryBuilder) {
        try (PreparedStatement stmt = conn.prepareStatement(queryBuilder.build())) {
            queryBuilder.setParameters(stmt);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
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
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}