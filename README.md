# Web-basiertes Such- und Analysetool

Dieses Projekt implementiert ein web-basiertes Tool, das Benutzern ermöglicht, sowohl lokale Dateisysteme zu durchsuchen als auch Webseiten zu analysieren. Es bietet zwei Hauptfunktionen: die Suche nach Dateien in einem angegebenen Verzeichnis basierend auf einem Suchbegriff und die Analyse des Inhalts von Webseiten, um spezifische Informationen zu extrahieren.

## Funktionen

- **Dateisuche**: Durchsucht rekursiv ein angegebenes Verzeichnis nach Dateien, die einen bestimmten Text im Dateinamen enthalten. Die Ergebnisse werden als Liste von Dateipfaden zurückgegeben.
- **Webseiten-Analyse**: Lädt den Inhalt einer angegebenen URL herunter und analysiert diesen auf das Vorkommen bestimmter Schlüsselwörter. Die gefundenen Informationen werden extrahiert und dem Benutzer präsentiert.

## Technologiestack

- **Backend**: Spring Boot
- **Web Scraping**: Jsoup
- **Frontend**: HTML, JavaScript

## Voraussetzungen

Um das Projekt lokal auszuführen, benötigst du:

- JDK 11 oder neuer
- Maven (wenn du Maven als Build-Tool verwendest)
- Ein IDE deiner Wahl (z.B. IntelliJ IDEA, Eclipse)

## Benutzung

- Um **Dateien zu suchen**, navigiere zum entsprechenden Abschnitt der Anwendung, gib den Pfad zum Verzeichnis und den Suchbegriff ein.
- Um eine **Webseite zu analysieren**, gib die URL in das vorgesehene Feld ein und starte die Analyse.

## Lizenz

*Dieses Projekt ist unter der MIT Lizenz lizenziert - siehe die [LICENSE](LICENSE) Datei für Details.

## Beitrag

Beiträge sind herzlich willkommen. Bitte erstelle einen Pull Request oder ein Issue, um Vorschläge zur Verbesserung des Projekts zu machen.

