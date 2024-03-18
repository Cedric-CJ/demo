package com.example.demo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FolderSearcher {

    /**
     * Sucht rekursiv nach Dateien in einem Verzeichnis, die den angegebenen Keyword im Namen enthalten.
     *
     * @param directory Das Verzeichnis, in dem gesucht werden soll.
     * @param keyword Das Keyword, nach dem im Dateinamen gesucht werden soll.
     * @return Eine Liste von Dateien, die das Keyword enthalten.
     */
    public static List<File> searchForFilesWithKeyword(File directory, String keyword) {
        List<File> matchingFiles = new ArrayList<>();
        searchDirectory(directory, keyword, matchingFiles);
        return matchingFiles;
    }

    /**
     * Unterstützende rekursive Methode, die tatsächlich durch das Verzeichnis navigiert und die Suche durchführt.
     *
     * @param directory Das aktuell durchsuchte Verzeichnis.
     * @param keyword Das Such-Keyword.
     * @param matchingFiles Eine Liste, in der gefundene Dateien gesammelt werden.
     */
    private static void searchDirectory(File directory, String keyword, List<File> matchingFiles) {
        File[] files = directory.listFiles(); // Listet alle Dateien und Verzeichnisse im aktuellen Verzeichnis auf

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    searchDirectory(file, keyword, matchingFiles); // Rekursiver Aufruf, um Unterordner zu durchsuchen
                } else {
                    if (file.getName().toLowerCase().contains(keyword.toLowerCase())) {
                        matchingFiles.add(file); // Fügt die Datei zur Liste hinzu, wenn der Dateiname das Keyword enthält
                    }
                }
            }
        }
    }
}
