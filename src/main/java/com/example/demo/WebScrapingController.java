package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class WebScrapingController {

    @Autowired
    private WebScrapingService webScrapingService;

    @GetMapping("/checkWebsite")
    public ResponseEntity<?> checkWebsite(@RequestParam String url) {
        // Implementiere die Logik zum Abrufen, Analysieren und Vergleichen der Webseite
        return ResponseEntity.ok().body("Implementiere mich");
    }

    // Weitere Endpunkte
}
