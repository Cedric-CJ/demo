package com.example.demo;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SearchApp extends JFrame {

    private JTextField inputField;
    private JTree fileTree;
    private JComboBox<String> searchType;

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
        JScrollPane treeScroll = new JScrollPane(fileTree);

        JPanel panel = new JPanel();
        panel.add(searchType);
        panel.add(inputField);
        panel.add(searchButton);

        searchButton.addActionListener(this::performSearch);

        add(panel, BorderLayout.NORTH);
        add(treeScroll, BorderLayout.CENTER);
    }

    private void performSearch(ActionEvent e) {
        String selection = (String) searchType.getSelectedItem();
        if ("Dateien suchen".equals(selection)) {
            searchFiles();
        } else if ("Im Internet suchen".equals(selection)) {
            // Implementiere die Websuche-Funktionalität hier
        }
    }

    private void searchFiles() {
        String searchKeyword = inputField.getText().toLowerCase();
        Path startPath = Paths.get("Z:\\Serien_und_Filme");
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(startPath.toString());
        Map<String, Integer> fileCountMap = new HashMap<>();

        try {
            Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
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

        fileTree.setModel(new DefaultTreeModel(root));
        expandAllNodes(fileTree, 0, fileTree.getRowCount());
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
