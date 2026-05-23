package uk.ac.mmu.advprog.assessment.browser;


import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MainWindow extends JFrame {

    private String dbUrl = "jdbc:sqlite:data/wines.db";

    private Connection conn;

    private SearchPanel searchPanel;

    private ResultsPanel resultsPanel;
    private WinePanel detailsPanel;

    public MainWindow() {

        setTitle("Wine Browser");
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        connectDatabase();

        initUI();
    }

    private void connectDatabase() {

        try {
            conn = DriverManager.getConnection(dbUrl);

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );

            System.exit(1);
        }
    }

    private void initUI() {

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));

        mainPanel.setBackground(new Color(20, 20, 20));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        searchPanel = new SearchPanel();
        searchPanel.setSearchListener((filters, sortBy) -> performSearch(filters, sortBy));

        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        contentPanel.setBackground(new Color(20, 20, 20));

        resultsPanel = new ResultsPanel(conn);
        detailsPanel = new WinePanel(conn);

        resultsPanel.setWineSelectionListener(
                wineId -> detailsPanel.loadWine(wineId)
        );

        contentPanel.add(resultsPanel);
        contentPanel.add(detailsPanel);

        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private void performSearch(java.util.Map<String, String> filters, String sortBy) {
        resultsPanel.searchWinesMultiField(filters, sortBy);
    }
}