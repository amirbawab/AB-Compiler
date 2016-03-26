package gui.center.console.components;

import gui.center.console.TabbedConsolePanel;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Amir on 3/26/2016.
 */
public class ConsoleTableNavigation extends JPanel {

    // Components
    private TabbedConsolePanel tabbedPane;
    private JPanel navigationPanel;
    private Map<String, Integer> entryLink;

    public ConsoleTableNavigation() {

        // Init components
        tabbedPane = new TabbedConsolePanel();
        navigationPanel = new JPanel();
        entryLink = new HashMap<>();

        // Set layout
        setLayout(new BorderLayout());

        // Labels
        JLabel backwardLabel = new JLabel("<html><u>&lt;&lt; Backward</u></html>");
        JLabel forwardLabel = new JLabel("<html><u>Forward &gt;&gt;</u></html>");

        // Add labels
        navigationPanel.add(backwardLabel);
        navigationPanel.add(Box.createRigidArea(new Dimension(10,0)));
        navigationPanel.add(forwardLabel);

        // Add components
        add(tabbedPane, BorderLayout.CENTER);
        add(navigationPanel, BorderLayout.NORTH);
    }

    public TabbedConsolePanel getTabbedPane() {
        return tabbedPane;
    }

    public void addEntryLink(String panelTitle, int row, int link) {
        entryLink.put(panelTitle+"#"+row, link);
    }

    public int getEntryLink(String panelTitle, int row, int link) {
        return entryLink.get(panelTitle+"#"+row);
    }

    public void removeTables() {
        tabbedPane.removeAll();
        tabbedPane.getTabPanelsMap().clear();
        entryLink.clear();
    }
}
