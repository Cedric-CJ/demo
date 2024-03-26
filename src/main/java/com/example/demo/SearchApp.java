package com.example.demo;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;



public class SearchApp extends JFrame {

    private JTextField inputField;
    private JTree fileTree;
    private JComboBox<String> searchType;
    private JProgressBar progressBar;
    private JButton toggleModeButton;
    private JPanel panel;
    private JButton searchButton; // Stellen Sie sicher, dass searchButton auf Klassenebene definiert ist, falls noch nicht geschehen.
    private boolean isDarkMode = false; // Zustand des Dark Modes
    private JPanel bottomPanel; // Klassenvariable für das untere Panel

    public SearchApp() {
        super("Lokale und Web-Suche");
        initComponents();
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout()); // Setzen des Layouts für das JFrame
        add(panel, BorderLayout.NORTH); // Fügt das Panel mit Suchfeldern und Buttons oben hinzu
        add(new JScrollPane(fileTree), BorderLayout.CENTER); // Fügt den JScrollPane in der Mitte hinzu
        addBottomPanel(); // Methode zum Hinzufügen des unteren Panels
        applyMode();
        setVisible(true);
    }

    private void addBottomPanel() {
        bottomPanel = new JPanel(); // Initialisierung mit der Klassenvariable
        bottomPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        JButton checkNewFilesButton = new JButton("Überprüfen auf neue Dateien");
        checkNewFilesButton.addActionListener(e -> {
            showLoadingOverlay();
        });

        bottomPanel.add(checkNewFilesButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void initComponents() {
        panel = new JPanel();
        inputField = new JTextField(30);
        searchButton = new JButton("Suchen");
        searchType = new JComboBox<>(new String[]{"Dateien suchen", "Im Internet suchen"});
        fileTree = new JTree(new DefaultMutableTreeNode("Suchergebnisse"));
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        JScrollPane treeScroll = new JScrollPane(fileTree);
        toggleModeButton = new JButton("Dark Mode");

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
        fileTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
                    if (node == null || node.isRoot()) return;

                    Object nodeInfo = node.getUserObject();
                    if (nodeInfo instanceof FileInfo) {
                        FileInfo fileInfo = (FileInfo) nodeInfo;
                        openFile(fileInfo.getFullPath().toString()); // Öffnen der Datei basierend auf dem Pfad
                    }
                }
            }
        });
    }

    public void openFile(String filePath) {
        try {
            File fileToOpen = new File(filePath);
            if (fileToOpen.exists()) {
                Desktop.getDesktop().open(fileToOpen);
            } else {
                JOptionPane.showMessageDialog(this, "Datei existiert nicht: " + filePath, "Fehler", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Fehler beim Öffnen der Datei: " + filePath, "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleMode() {
        isDarkMode = !isDarkMode;
        applyMode();
    }

    private void applyMode() {
        if (isDarkMode) {
            setUIColors(new Color(43, 43, 43), Color.LIGHT_GRAY, Color.GREEN, new Color(60, 63, 65), Color.LIGHT_GRAY);
            toggleModeButton.setText("Light Mode");
            if (bottomPanel != null) {
                bottomPanel.setBackground(new Color(43, 43, 43)); // Hintergrundfarbe des unteren Panels
                for (Component comp : bottomPanel.getComponents()) {
                    if (comp instanceof JButton) {
                        comp.setForeground(Color.LIGHT_GRAY); // Textfarbe der Buttons
                        comp.setBackground(new Color(60, 63, 65)); // Hintergrundfarbe der Buttons
                    }
                }
            }
        } else {
            setUIColors(Color.WHITE, Color.BLACK, Color.BLUE, Color.WHITE, Color.BLACK);
            toggleModeButton.setText("Dark Mode");
            if (bottomPanel != null) {
                bottomPanel.setBackground(Color.WHITE); // Hintergrundfarbe des unteren Panels für Light Mode
                for (Component comp : bottomPanel.getComponents()) {
                    if (comp instanceof JButton) {
                        comp.setForeground(Color.BLACK); // Textfarbe der Buttons für Light Mode
                        comp.setBackground(Color.WHITE); // Hintergrundfarbe der Buttons für Light Mode
                    }
                }
            }
        }
        SwingUtilities.updateComponentTreeUI(this);
    }
    public class FileInfo implements Comparable<FileInfo> {
        private String displayName;
        private Path fullPath;

        public FileInfo(String displayName, Path fullPath) {
            this.displayName = displayName;
            this.fullPath = fullPath;
        }

        public Path getFullPath() {
            return fullPath;
        }

        @Override
        public String toString() {
            return displayName;
        }
        @Override
        public int compareTo(FileInfo o) {
            return this.displayName.compareTo(o.displayName);
        }
    }

    private void addFilesToTree(DefaultMutableTreeNode root, Set<String> files, boolean deleted) {
        Map<String, DefaultMutableTreeNode> pathNodes = new TreeMap<>(); // Nutze TreeMap für sortierte Pfade
        for (String filePath : files) {
            Path path = Paths.get(filePath);
            DefaultMutableTreeNode node = root;

            // Durchlaufe den Pfad und erstelle bei Bedarf Knoten
            for (int i = 0; i < path.getNameCount(); i++) {
                String part = path.getName(i).toString();
                if (i == path.getNameCount() - 1 && deleted) {
                    part += " (gelöscht)"; // Markiere die Datei als gelöscht, falls zutreffend
                }
                if (!pathNodes.containsKey(part)) {
                    pathNodes.put(part, new DefaultMutableTreeNode(part));
                }
                DefaultMutableTreeNode childNode = pathNodes.get(part);
                // Füge den Knoten nur hinzu, wenn er noch nicht Teil des aktuellen Knotens ist
                if (node.getIndex(childNode) == -1) {
                    node.add(childNode);
                }
                node = childNode;
            }
        }
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

    private Map<Path, List<Path>> searchFiles() {
        SwingUtilities.invokeLater(() -> progressBar.setValue(0));

        String searchKeyword = inputField.getText().toLowerCase();
        Path startPath = Paths.get("Z:\\Serien_und_Filme");
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Suchergebnisse"); // Verwende einen allgemeineren Wurzelknoten

        Map<Path, List<Path>> filesMap = new TreeMap<>(); // Verwende TreeMap für automatische Sortierung der Pfade

        try {
            Files.walkFileTree(startPath, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toString().toLowerCase().contains(searchKeyword)) {
                        Path parent = startPath.relativize(file.getParent());
                        filesMap.computeIfAbsent(parent, k -> new ArrayList<>()).add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            filesMap.forEach((parentPath, files) -> {
                files.sort(Comparator.comparing(Path::getFileName)); // Sortiere die Dateien alphabetisch nach Namen

                DefaultMutableTreeNode parentNode = findOrCreateNode(root, parentPath);
                files.forEach(file -> parentNode.add(new DefaultMutableTreeNode(file.getFileName().toString())));
                if (!files.isEmpty()) {
                    parentNode.setUserObject(parentPath + " (" + files.size() + ")");
                }
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            fileTree.setModel(new DefaultTreeModel(root));
            expandAllNodes(fileTree, 0, fileTree.getRowCount());
            progressBar.setValue(100);
            enableUI();
        });
        saveSearchResults(filesMap);
        return filesMap;
    }

    private DefaultMutableTreeNode findOrCreateNode(DefaultMutableTreeNode root, Path path) {
        DefaultMutableTreeNode node = root;
        for (Path part : path) {
            boolean found = false;
            for (int i = 0; i < node.getChildCount(); i++) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
                if (child.getUserObject().toString().equals(part.toString())) {
                    node = child;
                    found = true;
                    break;
                }
            }
            if (!found) {
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(part.toString());
                node.add(newNode);
                node = newNode;
            }
        }
        return node;
    }

    private void expandAllNodes(JTree tree, int startingIndex, int rowCount) {
        for (int i = startingIndex; i < rowCount; ++i) {
            tree.expandRow(i);
        }

        if (tree.getRowCount() != rowCount) {
            expandAllNodes(tree, rowCount, tree.getRowCount());
        }
    }

    private void saveSearchResults(Map<Path, List<Path>> filesMap) {
        try (PrintWriter writer = new PrintWriter("last_search_results.txt", StandardCharsets.UTF_8)) {
            for (Map.Entry<Path, List<Path>> entry : filesMap.entrySet()) {
                Path parentPath = entry.getKey();
                List<Path> files = entry.getValue();
                for (Path file : files) {
                    writer.println(parentPath.resolve(file).toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> readLastSearchResults() {
        try {
            return Files.readAllLines(Paths.get("last_search_results.txt"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private void performNewFilesCheck() {
        Set<String> lastSearchResults = new HashSet<>(readLastSearchResults());
        Map<Path, List<Path>> currentSearchMap = searchFiles();
        Set<String> currentPaths = currentSearchMap.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().map(path -> entry.getKey().resolve(path).toString()))
                .collect(Collectors.toSet());

        Set<String> newFiles = new HashSet<>(currentPaths);
        newFiles.removeAll(lastSearchResults);

        Set<String> deletedFiles = new HashSet<>(lastSearchResults);
        deletedFiles.removeAll(currentPaths);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Suchergebnisse");

        if (!newFiles.isEmpty() || !deletedFiles.isEmpty()) {
            addFilesToTree(root, newFiles, false);
            addFilesToTree(root, deletedFiles, true);
        } else {
            root.add(new DefaultMutableTreeNode("Keine neuen Dateien gefunden."));
        }

        SwingUtilities.invokeLater(() -> {
            fileTree.setModel(new DefaultTreeModel(root));
            expandAllNodes(fileTree, 0, fileTree.getRowCount());
        });

        saveSearchResults(currentSearchMap);
    }

    private void showLoadingOverlay() {
        JDialog loadingDialog = new JDialog(this, "Überprüfung läuft...", Dialog.ModalityType.APPLICATION_MODAL);
        loadingDialog.setLayout(new BorderLayout());
        JLabel messageLabel = new JLabel("Bitte warten...", JLabel.CENTER);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        loadingDialog.add(messageLabel, BorderLayout.CENTER);
        loadingDialog.add(progressBar, BorderLayout.SOUTH);
        loadingDialog.setSize(300, 100);
        loadingDialog.setLocationRelativeTo(this);
        loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // Verhindert das Schließen durch den Benutzer

        // Zeige den Dialog in einem separaten Thread an, um die UI reaktionsfähig zu halten
        SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));

        // Führe die Überprüfung auf neue Dateien in einem separaten Thread aus
        new Thread(() -> {
            performNewFilesCheck();
            loadingDialog.dispose(); // Schließe den Dialog, sobald die Überprüfung abgeschlossen ist
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SearchApp::new);
    }
}