package uk.ac.mmu.advprog.assessment.browser;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class SearchPanel extends JPanel {

    private JTextField[] filterFields;
    private JButton searchButton;
    private SearchListener listener;
    private final String[] filterLabels = {"wineName", "type", "country", "regionName", "wineryName", "body", "acidity", "blend_type", "grape", "abvMin", "abvMax"};
    private final String[] filterDisplayNames = {"Wine Name", "Type", "Country", "Region", "Winery", "Body", "Acidity", "Blend", "Grape Variety", "ABV Min", "ABV Max"};

    public SearchPanel() {
        initUI();
    }

    /**
     * Initializes the search panel UI components.
     * Creates a title, filter input fields for various wine attributes,
     * and a search button to trigger searches.
     */
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

        JComponent filterPanel = createFilterPanel();

        searchButton = new JButton("Search");
        searchButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchButton.addActionListener(e -> performSearch());

        add(title, BorderLayout.NORTH);
        add(filterPanel, BorderLayout.CENTER);
        add(searchButton, BorderLayout.SOUTH);
    }

    /**
     * Creates the filter panel with all search criteria.
     *
     * @return a JComponent containing the filter input fields
     */
    private JComponent createFilterPanel() {
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new GridLayout(6, 2, 10, 10));
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

            listener.onSearch(filters);
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
        void onSearch(Map<String, String> filters);
    }
}
