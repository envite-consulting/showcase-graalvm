# Lecture

## TODOs
### Anwendung
- [x] MongoDB uri als Umgebungsvariable
- [x] Graal und noGraal in eine Anwendung kombinieren und verscheidene Maven Profile für Graal und noGraal anlegen
- [x] Daten bei POST direkt bei request mitgeben, nicht mehr über GET von nodejs-api holen
- [x] Konstante Last konfigurieren über extra App in Cloud Run mit verschiedene Testszenarien
    - über parameter steuerbar (anfragen, endpunkte,...)
    - mit Verlaufsdiagrammen für einzelne Szenarien
    - benchmark warm up inklusive, hier wird warm up Zeit ignoriert
    - [x] Test-Umgebung dokumentieren (Anzahl CPUs, Memory)
- [ ] GraalVM image auf Windows bauen: Wie? + Tutorial/Anleitung
- [x] README mit build prozess vervollständigen
- [x] README mit prozess für local builden
- [x] Root pom.xml, dass alles auf einmal gebaut werden kann
- [x] Übung gestalten
- [x] Automatische Benchmark laufen lassen mit Tool (Lecture-Utilizer mehrmals aufrufen)
- [x] docker compose datei übersichtlicher machen
- [ ] stromverbrauch in dashboard einbauen
- [x] python requirements.txt für auto install von requirements

### Umgebung
- [ ] MongoDB Doku in Doku einbauen (mind. automatisches Setup von DB)
- [ ] Verschiedene Tabellen in DB, sodass jeder Student eigene Tabelle bekommt (automatisiert in terraform)
- [ ] Übersichts Dashboard für relevanten Metriken (für einzelne Teams?)
- [ ] Terraform Skripte zum deployment der Anwendung(en) erstellen, alles in einem deployen (Graal und noGraal und jeweils 1 Tester, gesamt 4 seperate Anwendungen)
- [ ] Nutzer für Studierende anlegen Terraform
- [ ] Tutorial / Übung aufbauen

### Kleinigkeiten
- [x] Bücher List auf 100 erweitern (gute baseline)
- [x] (Unnötige) Liste aus else Block ausbauen
- [x] (Docker Container ressourcen begrenzen zur konsistenz)?
- [x] Util Anwendung: Zeit messen die eine bestimmte Anzahl von Anfragen dauert und ausgeben
- [x] Offizielles Maven Plugin für GraalVm verwenden anstelle der lokal installierten Version
- [x] Umgebungsvariablen von Dockerfile zu docker compose verschieben
- [x] SingleInsert zu Util Anwendung bewegen: Anzahl der Bücher pro request festlegen
- [x] Anzahl der Bücher: Zusätzlicher Parameter
- [x] Parallel Bücher Senden: Anzahl der User die gleichzeitig an die Anwendung Bücher schicken
- [x] Evtl von csv zu JSON wechseln
- [x] README in lecture-utilizer vervollständigen
- [ ] In Doku/Tutorial Umgebungsvariablen setzen für MongoDB Connection String, Java, GraalVM
- [ ] In Doku/Tutorial Erklärung Maven Profile
- [ ] In Doku/Tutorial Erklärung Testszenarios

**Leitfragen**:
Wann ist GraalVM besser als die native JVM?
Bei welchen Anwendungen würde eine Umstellung Sinn machen?
Unterschiede zwischen Community und Enterprice Version von GraalVM?

"Soft Facts": Auswirkungen auf die Entwicklung, wie kompliziert ist diese, Debugging, Wartbarkeit

## Szenarien
Auswirkungen von GraalVM vs nativ auf die folgenden Eigenschaften.
- Build zeit (viel builden vs wenig builden)
- Image Ladezeit (größe des Image: Festplattenplatz / Netzwerkauslastung)
- Startzeit (unabhängig von Image Ladezeiten)
    - wenig Komonenten
        - ohne reflection (micronaut)
        - mit reflection (springboot)
    - viele Komonenten
        - ohne reflection (micronaut)
        - mit reflection (springboot)
    - künstlich: Daten aus DB laden bei Start
- Datenverarbeitung / Algorithmen (Vorteile von JIT compiler ausnutzen, z.B. reguläre Ausdrücke)
    - viele requests (parallel oder seriell)
        - Lange vs kurze requests (Latenz)
    - wenige requests (parallel oder seriell)
        - Lange vs kurze requests (Latenz)
- Zeit zum herunterfahren der Anwendung

Qualitative Fragestellungen:
- Wie sieht Debugging von native Images aus? (Evtl. Antwort: Größeres Image, kann sogar verlangsamen) [link](https://www.graalvm.org/latest/reference-manual/native-image/debugging-and-diagnostics/DebugInfo/)
- Auswirkung auf den täglichen Entwicklungsprozess (Wartbarkeit des Codes)

Metriken:
- CPU-Zeit (s oder ms)
- CPU-Utilization
- Memory-Total
- Heap
- Start up Zeit
- Latenzen

Aktuelles Szenario:
- wenige, einfache requests (zu kurz für Cloud Metriken)
