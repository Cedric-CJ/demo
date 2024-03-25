package com.example.demo;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class SearchApp extends JFrame {

    private JTextField inputField;
    private JTree fileTree;
    private JComboBox<String> searchType;
    private JProgressBar progressBar;
    private JButton toggleModeButton;
    private JPanel panel;

    private boolean isDarkMode = false; // Zustand des Dark Modes


    public SearchApp() {
        super("Lokale und Web-Suche");
        initComponents();
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void initComponents() {
        inputField = new JTextField(30);
        JButton searchButton = new JButton("Suchen");
        searchType = new JComboBox<>(new String[]{"Dateien suchen", "Im Internet suchen"});
        fileTree = new JTree(new DefaultMutableTreeNode("Suchergebnisse"));
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        JScrollPane treeScroll = new JScrollPane(fileTree);
        toggleModeButton = new JButton("Dark Mode");
        panel = new JPanel();

        panel.add(searchType);
        panel.add(inputField);
        panel.add(searchButton);
        panel.add(progressBar);
        panel.add(toggleModeButton);

        searchButton.addActionListener(this::performSearch);
        toggleModeButton.addActionListener(e -> toggleMode());

        inputField.addActionListener(this::performSearch);

        add(panel, BorderLayout.NORTH);
        add(treeScroll, BorderLayout.CENTER);
        applyMode();
    }

    private void toggleMode() {
        isDarkMode = !isDarkMode;
        applyMode();
    }

    private void applyMode() {
        if (isDarkMode) {
            setUIColors(new Color(43, 43, 43), Color.LIGHT_GRAY, Color.GREEN, new Color(60, 63, 65), Color.LIGHT_GRAY);
            toggleModeButton.setText("Light Mode");
        } else {
            setUIColors(Color.WHITE, Color.BLACK, Color.BLUE, Color.WHITE, Color.BLACK);
            toggleModeButton.setText("Dark Mode");
        }
        SwingUtilities.updateComponentTreeUI(this);
    }

    private void setUIColors(Color background, Color foreground, Color progressForeground, Color inputBackground, Color inputForeground) {
        panel.setBackground(background);
        panel.setForeground(foreground);
        inputField.setBackground(inputBackground);
        inputField.setForeground(inputForeground);
        fileTree.setBackground(inputBackground);
        fileTree.setForeground(inputForeground);
        searchType.setBackground(inputBackground);
        searchType.setForeground(inputForeground);
        progressBar.setBackground(background);
        progressBar.setForeground(progressForeground);
        progressBar.setStringPainted(true);
        UIManager.put("Panel.background", background);
        UIManager.put("Label.foreground", foreground);
        UIManager.put("Tree.textBackground", inputBackground);
        UIManager.put("Tree.textForeground", foreground);
        UIManager.put("TextField.background", inputBackground);
        UIManager.put("TextField.foreground", inputForeground);
        UIManager.put("ComboBox.background", background);
        UIManager.put("ComboBox.foreground", foreground);
        UIManager.put("ProgressBar.background", background);
        UIManager.put("ProgressBar.foreground", progressForeground);
        UIManager.put("ProgressBar.selectionBackground", foreground);
        UIManager.put("ProgressBar.selectionForeground", background);
        UIManager.put("Button.background", background);
        UIManager.put("Button.foreground", foreground);
        UIManager.put("ScrollPane.background", background);
        UIManager.put("Viewport.background", background);
        // This method applies the color scheme immediately to the component and its children
        for (Component c : panel.getComponents()) {
            if (c instanceof JButton) {
                c.setBackground(background);
                c.setForeground(foreground);
            }
        }
    }

    private void showErrorMessage(String message) {
        if (isDarkMode) {
            // Sichert die aktuellen Werte
            Color oldBackground = UIManager.getColor("Panel.background");
            Color oldForeground = UIManager.getColor("Label.foreground");
            Color oldOptionPaneBackground = UIManager.getColor("OptionPane.background");
            Color oldOptionPaneForeground = UIManager.getColor("OptionPane.foreground");

            // Setzt die Farben für den Dark Mode
            UIManager.put("Panel.background", Color.DARK_GRAY);
            UIManager.put("OptionPane.background", Color.DARK_GRAY);
            UIManager.put("Panel.foreground", Color.WHITE);
            UIManager.put("Label.foreground", Color.WHITE);
            UIManager.put("OptionPane.foreground", Color.WHITE);
            UIManager.put("OptionPane.messageForeground", Color.WHITE);

            // Zeigt die Fehlermeldung
            JOptionPane.showMessageDialog(this, message, "Eingabefehler", JOptionPane.ERROR_MESSAGE);

            // Stellt die ursprünglichen Farben wieder her
            UIManager.put("Panel.background", oldBackground);
            UIManager.put("OptionPane.background", oldOptionPaneBackground);
            UIManager.put("Panel.foreground", oldForeground);
            UIManager.put("Label.foreground", oldForeground);
            UIManager.put("OptionPane.foreground", oldOptionPaneForeground);
            UIManager.put("OptionPane.messageForeground", Color.black);

        } else {
            // Zeigt die Fehlermeldung im Light Mode ohne Farbanpassung
            JOptionPane.showMessageDialog(this, message, "Eingabefehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performSearch(ActionEvent e) {
        String searchKeyword = inputField.getText().trim();

        if (searchKeyword.isEmpty()) {
            showErrorMessage("Die Eingabe darf nicht leer sein."); // Verwendet die neue Methode zum Anzeigen der Fehlermeldung
            return;
        }
        disableUI(); // UI deaktivieren, wie zuvor implementiert
        // Prüfung der Auswahl und Starten der Suche...
        if ("Dateien suchen".equals(searchType.getSelectedItem())) {
            new Thread(this::searchFiles).start();
        } else if ("Im Internet suchen".equals(searchType.getSelectedItem())) {
            // Implementiere die Websuche-Funktionalität hier
        }
    }

    private JButton searchButton; // Stellen Sie sicher, dass searchButton auf Klassenebene definiert ist, falls noch nicht geschehen.

    private void disableUI() {
        inputField.setEnabled(false);
        searchType.setVisible(false); // Versteckt die JComboBox während der Suche
        searchButton.setVisible(false); // Versteckt den Such-Button während der Suche

        // Optional: Anpassen der Farben für das deaktivierte Eingabefeld im Dark Mode
        if (isDarkMode) {
            inputField.setBackground(Color.DARK_GRAY); // Dunkler Hintergrund im deaktivierten Zustand
            inputField.setDisabledTextColor(Color.GRAY); // Grauer Text im deaktivierten Zustand
        } else {
            inputField.setBackground(UIManager.getColor("TextField.disabledBackground"));
            inputField.setDisabledTextColor(UIManager.getColor("TextField.disabledForeground"));
        }
    }

    private void enableUI() {
        inputField.setEnabled(true);
        searchType.setVisible(true); // Macht die JComboBox nach der Suche wieder sichtbar
        searchButton.setVisible(true); // Macht den Such-Button nach der Suche wieder sichtbar

        // Setze die Farben für das aktiviertes Textfeld zurück, entsprechend dem aktuellen Modus
        if (isDarkMode) {
            inputField.setForeground(Color.LIGHT_GRAY);
            inputField.setBackground(new Color(60, 63, 65)); // Dunkler Hintergrund
        } else {
            inputField.setForeground(Color.BLACK);
            inputField.setBackground(Color.WHITE); // Heller Hintergrund
        }
    }

    private void searchFiles() {
        SwingUtilities.invokeLater(() -> progressBar.setValue(0)); // Setze den Fortschrittsbalken zurück

        String searchKeyword = inputField.getText().toLowerCase();
        Path startPath = Paths.get("Z:\\Serien_und_Filme");
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(startPath.toString());
        Map<String, Integer> fileCountMap = new HashMap<>();
        AtomicInteger totalFiles = new AtomicInteger();

        try {
            // Zähle zuerst die Gesamtanzahl der Dateien
            Files.walk(startPath).forEach(p -> totalFiles.incrementAndGet());

            Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
                private int visitedFiles = 0;

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    visitedFiles++;
                    int progress = (int) (((double) visitedFiles / totalFiles.get()) * 100);
                    SwingUtilities.invokeLater(() -> progressBar.setValue(progress));

                    if (file.toString().toLowerCase().contains(searchKeyword)) {
                        Path relativePath = startPath.relativize(file.getParent());
                        String key = relativePath.toString();
                        fileCountMap.put(key, fileCountMap.getOrDefault(key, 0) + 1);
                        addNode(root, relativePath.resolve(file.getFileName()).toString(), fileCountMap);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            fileTree.setModel(new DefaultTreeModel(root));
            expandAllNodes(fileTree, 0, fileTree.getRowCount());
            progressBar.setValue(100); // Setze den Fortschrittsbalken auf 100%, wenn die Suche abgeschlossen ist
            enableUI(); // Aktiviere UI-Elemente nach der Suche
        });
    }

    private void addNode(DefaultMutableTreeNode parent, String pathString, Map<String, Integer> fileCountMap) {
        String[] parts = pathString.split(Pattern.quote(File.separator)); // Korrigiert für Plattformunabhängigkeit
        DefaultMutableTreeNode node = parent;

        StringBuilder pathBuilder = new StringBuilder();

        for (String part : parts) {
            pathBuilder.append(part);
            String path = pathBuilder.toString();

            DefaultMutableTreeNode child = getChild(node, part);
            if (child == null) {
                child = new DefaultMutableTreeNode(part);
                node.add(child);
            }
            node = child;

            // Aktualisiere die Anzeige der Dateianzahl nur an Ordnerknoten
            if (Files.isDirectory(Paths.get(path))) {
                updateNodeWithFileCount(node, fileCountMap.getOrDefault(path, 0));
            }

            pathBuilder.append(File.separator);
        }
    }

    private void updateNodeWithFileCount(DefaultMutableTreeNode node, int fileCount) {
        if (fileCount > 0) {
            String nodeName = node.getUserObject().toString();
            // Entferne alte Dateizahl, wenn vorhanden
            nodeName = nodeName.replaceFirst("\\s\\(\\d+\\)$", "");
            // Füge neue Dateizahl hinzu
            node.setUserObject(nodeName + " (" + fileCount + ")");
        }
    }

    private DefaultMutableTreeNode getChild(DefaultMutableTreeNode parent, String childName) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
            if (childName.equals(child.getUserObject().toString())) {
                return child;
            }
        }
        return null;
    }

    private void expandAllNodes(JTree tree, int startingIndex, int rowCount) {
        for (int i = startingIndex; i < rowCount; ++i) {
            tree.expandRow(i);
        }

        if (tree.getRowCount() != rowCount) {
            expandAllNodes(tree, rowCount, tree.getRowCount());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SearchApp::new);
    }
}
