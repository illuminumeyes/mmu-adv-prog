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

    private JTextField searchField;

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


        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));

        searchPanel.setBackground(new Color(30, 30, 30));

        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70), 2, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel title = new JLabel("Wine Database");

        // title.setFont(new Font("SansSerif", Font.BOLD, 28));
        // title.setForeground(Color.WHITE);

        searchField = new JTextField();

        // searchField.setFont(new Font("SansSerif", Font.PLAIN, 18));

        JButton searchButton = new JButton("Search");

        searchButton.addActionListener(e -> performSearch());


        searchPanel.add(title, BorderLayout.NORTH);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        contentPanel.setBackground(new Color(20, 20, 20));

        resultsPanel = new ResultsPanel(conn);
        detailsPanel = new WinePanel(conn);

        // resultsPanel.setWineSelectionListener(
        //         wineId -> detailsPanel.loadWine(wineId)
        // );

        contentPanel.add(resultsPanel);
        contentPanel.add(detailsPanel);

        //-----------------------------------

        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private void performSearch() {

        String query = searchField.getText().trim();
        resultsPanel.searchWines(query);
    }
}