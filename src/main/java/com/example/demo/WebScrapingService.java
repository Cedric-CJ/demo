package com.example.demo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class WebScrapingService {

    public String fetchWebPageContent(String url) throws IOException, IOException {
        Document doc = Jsoup.connect(url).get();
        return doc.text(); // Gibt den reinen Textinhalt der Webseite zurück
    }

    // Weitere Methoden zum Vergleichen der Inhalte, Identifizieren von Medienelementen, etc.
}
