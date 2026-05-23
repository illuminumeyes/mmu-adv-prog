package uk.ac.mmu.advprog.assessment.browser;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class SearchPanel extends JPanel {

    private JTextField[] filterFields;
    private JComboBox<String> sortSelector;
    private JButton searchButton;
    private SearchListener listener;
    private String[] filterLabels = {"wineName", "type", "country", "regionName", "wineryName", "body", "acidity", "grape", "abvMin", "abvMax"};
    private String[] filterDisplayNames = {"Wine Name", "Type", "Country", "Region", "Winery", "Body", "Acidity", "Grape Variety", "ABV Min", "ABV Max"};

    public SearchPanel() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(30, 30, 30));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70), 2, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel title = new JLabel("Wine Database");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        // Filter panel
        JComponent filterPanel = createFilterPanel();

        // Sort panel
        JPanel sortPanel = createSortPanel();

        // Search button
        searchButton = new JButton("Search");
        searchButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchButton.addActionListener(e -> performSearch());

        // Bottom panel combining sort and search button
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(new Color(30, 30, 30));
        bottomPanel.add(sortPanel, BorderLayout.CENTER);
        bottomPanel.add(searchButton, BorderLayout.EAST);

        add(title, BorderLayout.NORTH);
        add(filterPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JComponent createFilterPanel() {
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new GridLayout(5, 2, 10, 10));
        filterPanel.setBackground(new Color(30, 30, 30));

        filterFields = new JTextField[filterLabels.length];

        for (int i = 0; i < filterLabels.length; i++) {
            JLabel label = new JLabel(filterDisplayNames[i] + ":");
            label.setForeground(Color.WHITE);
            label.setFont(new Font("SansSerif", Font.PLAIN, 12));

            filterFields[i] = new JTextField();
            filterFields[i].setFont(new Font("SansSerif", Font.PLAIN, 12));
            filterFields[i].setBackground(new Color(45, 45, 45));
            filterFields[i].setForeground(Color.WHITE);
            filterFields[i].setCaretColor(Color.WHITE);
            filterFields[i].addActionListener(e -> performSearch());

            filterPanel.add(label);
            filterPanel.add(filterFields[i]);
        }

        JScrollPane scrollPane = new JScrollPane(filterPanel);
        scrollPane.setBackground(new Color(30, 30, 30));
        scrollPane.getViewport().setBackground(new Color(30, 30, 30));

        return scrollPane;
    }

    private JPanel createSortPanel() {
        JPanel sortPanel = new JPanel(new BorderLayout(10, 10));
        sortPanel.setBackground(new Color(30, 30, 30));

        JLabel sortLabel = new JLabel("Sort By:");
        sortLabel.setForeground(Color.WHITE);
        sortLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        String[] sortOptions = {"Wine Name", "ABV (High to Low)", "ABV (Low to High)"};
        sortSelector = new JComboBox<>(sortOptions);
        sortSelector.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sortSelector.setBackground(new Color(45, 45, 45));
        sortSelector.setForeground(Color.WHITE);

        sortPanel.add(sortLabel, BorderLayout.WEST);
        sortPanel.add(sortSelector, BorderLayout.CENTER);

        return sortPanel;
    }

    private void performSearch() {
        if (listener != null) {
            // Build filters map
            Map<String, String> filters = new HashMap<>();
            for (int i = 0; i < filterLabels.length; i++) {
                String value = filterFields[i].getText().trim();
                if (!value.isEmpty()) {
                    filters.put(filterLabels[i], value);
                }
            }

            if (filters.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter at least one search criterion", "No Search Terms", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String sortBy = (String) sortSelector.getSelectedItem();
            listener.onSearch(filters, sortBy);
        }
    }

    public void setSearchListener(SearchListener listener) {
        this.listener = listener;
    }

    public void clearSearchFields() {
        for (JTextField field : filterFields) {
            field.setText("");
        }
    }

    @FunctionalInterface
    public interface SearchListener {
        void onSearch(Map<String, String> filters, String sortBy);
    }
}
