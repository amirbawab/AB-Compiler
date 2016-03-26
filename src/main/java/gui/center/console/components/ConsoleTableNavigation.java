package gui.center.console.components;

import gui.center.console.TabbedConsolePanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

/**
 * Created by Amir on 3/26/2016.
 */
public class ConsoleTableNavigation extends JPanel {

    // Components
    private TabbedConsolePanel tabbedPane;
    private JPanel navigationPanel;
    private Map<String, Integer> entryLink;
    private Stack<Integer> pageStack;
    private int lastPage;

    public ConsoleTableNavigation() {

        // Init components
        tabbedPane = new TabbedConsolePanel();
        navigationPanel = new JPanel();
        entryLink = new HashMap<>();
        pageStack = new Stack<>();

        // Set layout
        setLayout(new BorderLayout());

        // Labels
        JLabel backwardLabel = new JLabel("<html><u>&lt;&lt; Backward</u></html>");

        // Add label listener
        backwardLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(!pageStack.isEmpty()) {
                    lastPage = pageStack.peek();
                    tabbedPane.setSelectedIndex(pageStack.pop());
                }
            }
        });

        // Add on tab change
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if(tabbedPane.getSelectedIndex() != lastPage) {
                    pageStack.push(lastPage);
                    lastPage = tabbedPane.getSelectedIndex();
                }
            }
        });

        // Add labels
        navigationPanel.add(backwardLabel);

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

    public Integer getEntryLink(String panelTitle, int row) {
        return entryLink.get(panelTitle+"#"+row);
    }

    public void setSelectedIndex(int i) {
        tabbedPane.setSelectedIndex(i);
    }

    public void removeTables() {
        tabbedPane.removeAll();
        tabbedPane.getTabPanelsMap().clear();
        entryLink.clear();
        pageStack.clear();
        lastPage = 0;
    }
}
