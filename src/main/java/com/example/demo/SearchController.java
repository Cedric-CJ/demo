package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class SearchController {

    @GetMapping("/searchFiles")
    public List<String> searchFiles(@RequestParam String path, @RequestParam String keyword) {
        File directory = new File(path);
        List<File> files = FolderSearcher.searchForFilesWithKeyword(directory, keyword);
        // Konvertiere die Dateiliste in eine Liste von Strings für eine einfache JSON-Antwort
        return files.stream().map(File::getAbsolutePath).collect(Collectors.toList());
    }

    // Implementiere weitere Endpunkte analog
}
