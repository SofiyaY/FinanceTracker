# FinanceTracker

En personlig økonomiapp bygget med Java 17, JavaFX 21 og SQLite.

## Funksjoner

- **Dashboard** – Saldokort, inntekt/utgifter denne måneden, gruppert søylediagram (siste 6 måneder), og kakediagram for utgifter per kategori.
- **Transaksjoner** – Tabell med alle transaksjoner. Legg til, rediger og slett via dialog. Filtrer på måned og kategori. Eksporter til JSON.
- **Budsjett** – Sett månedlig budsjettgrense per kategori. Fremdriftslinje per kategori (rød hvis overskridet).
- **Statistikk** – Linjediagram for balanse over tid, og oppsummeringstabell med utgifter per kategori per måned.

## Krav

- Java 17 eller nyere (JDK)
- Internett-tilkobling ved første bygging (Gradle laster ned avhengigheter)

Sjekk Java-versjon:
```bash
java -version
```

## Kjøre appen

```bash
# Windows
gradlew.bat run

# Linux / macOS
./gradlew run
```

Databasefilen `finance.db` opprettes automatisk i mappen du kjører kommandoen fra, og fylles med eksempeldata ved første oppstart.

## Kjøre tester

```bash
gradlew.bat test          # Windows
./gradlew test            # Linux / macOS
```

Testrapport genereres i `build/reports/tests/test/index.html`.

## Eksportere transaksjoner

Gå til **Transaksjoner** → klikk **Eksporter JSON** → velg lagringsplass. Filen skrives med Jackson og inneholder alle viste transaksjoner.

## Prosjektstruktur

```
src/
  main/
    java/com/financetracker/
      Main.java                  ← Oppstartspunkt
      App.java                   ← JavaFX Application
      model/                     ← Transaction, Category, Budget
      dao/                       ← DatabaseManager, TransactionDAO, CategoryDAO, BudgetDAO
      service/                   ← FinanceService, ExportService
      controller/                ← En controller per view
      util/                      ← CurrencyFormatter
    resources/com/financetracker/
      fxml/                      ← FXML-layoutfiler
      css/styles.css             ← All styling
  test/
    java/com/financetracker/
      FinanceServiceTest.java
      BudgetServiceTest.java
```

## Teknologier

| Teknologi       | Versjon  | Bruk                        |
|-----------------|----------|-----------------------------|
| Java            | 17       | Kjøretidsmiljø              |
| JavaFX          | 21       | GUI (FXML + CSS)            |
| SQLite (JDBC)   | 3.45     | Lokal database              |
| Jackson         | 2.16     | JSON-eksport                |
| JUnit 5         | 5.10     | Enhetstesting               |
| Gradle          | 8.5      | Byggeverktøy                |
