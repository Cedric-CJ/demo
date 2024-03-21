package com.example.demo;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class Searcher {

    public String search(String fileName) {
        final Path fileForSearch = Paths.get(fileName);

        // Setze das Basisverzeichnis für die Suche
        Path startSearching = Paths.get("Z:\\Serien_und_Filme");
        final StringBuilder result = new StringBuilder("Suchergebnisse:\n");

        try {
            Files.walkFileTree(startSearching, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // System.out.println(file.toAbsolutePath()); // Entfernt für eine saubere Konsolenausgabe
                    if (file.toString().toLowerCase().contains(fileForSearch.toString().toLowerCase())) {
                        result.append(file.toAbsolutePath()).append('\n');
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (result.toString().equals("Suchergebnisse:\n")) {
            result.append("Datei nicht gefunden.\n")
                    .append("Bitte überprüfe die Richtigkeit des Dateinamens.");
        }
        return result.toString();
    }
}
