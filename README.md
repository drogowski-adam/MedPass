# MedPass 🩺

**MedPass** to prototypowy system wspierający weryfikację szczególnych potrzeb medycznych podczas kontroli bezpieczeństwa. Umożliwia uprawnionemu pracownikowi ochrony sprawdzenie, jakie przedmioty mogą być dopuszczone u danego uczestnika, bez konieczności okazywania papierowej dokumentacji medycznej przy wejściu.

Projekt powstał z myślą o sytuacjach, w których standardowe regulaminy mogą kolidować z potrzebami zdrowotnymi, np. podczas wydarzeń sportowych i koncertów, w sądach i urzędach czy na egzaminach.

> **Status:** projekt demonstracyjny / prototyp. Wynik weryfikacji ma charakter informacyjny — decyzja o dopuszczeniu przedmiotów zależy od właściwych procedur i regulaminu obiektu.

## Funkcjonalności

- **Logowanie personelu** i rozróżnianie ról `SECURITY` oraz `ADMIN`.
- **Weryfikacja uczestnika** po podaniu identyfikatora dokumentu — system wyszukuje powiązaną kategorię medyczną i listę przypisanych przedmiotów.
- **Prezentacja wyjątków**, np. pompy insulinowej, glukometru, żywności lub innych przedmiotów potrzebnych ze względów zdrowotnych.
- **Rejestracja zdarzeń** logowania (`LOGIN`) oraz weryfikacji (`VERIFY`).
- **Panel administratora** pozwalający przeglądać logi wybranego pracownika ochrony.
- **Haszowanie identyfikatorów** przed wyszukiwaniem i zapisem zdarzeń.

## Technologie

| Warstwa | Technologie |
| --- | --- |
| Aplikacja mobilna | Android, Java, Android SDK 34, Retrofit 2, Gson |
| Backend | Java 21, Spring Boot 4.0.1, Spring Web MVC, Spring JDBC |
| Baza danych | Microsoft SQL Server, JDBC |
| Budowanie projektu | Gradle |
| Komunikacja | REST API, HTTP, JSON |

## Architektura

```text
┌──────────────────────┐         HTTP / JSON         ┌───────────────────────┐
│  Aplikacja Android   │ ◄──────────────────────────► │  Spring Boot REST API │
│  SECURITY / ADMIN    │         Retrofit            │  port 8080            │
└──────────────────────┘                              └───────────┬───────────┘
                                                                 │ JDBC
                                                                 ▼
                                                      ┌───────────────────────┐
                                                      │ Microsoft SQL Server  │
                                                      └───────────────────────┘
```

Podczas weryfikacji backend wylicza skrót SHA-256 identyfikatora uczestnika, odczytuje z bazy przypisaną kategorię medyczną, a następnie pobiera odpowiednie przedmioty z tabeli reguł. Zdarzenie jest zapisywane w logach.

## Struktura repozytorium

```text
MedPass/
├── backend/                 # API Spring Boot i komunikacja z SQL Server
│   ├── src/main/java/com/example/medpassbackend/
│   │   ├── MedPassController.java
│   │   ├── MedPassRepo.java
│   │   └── HashUtil.java
│   ├── src/main/resources/application.properties
│   └── build.gradle
├── mobile_app/              # Aplikacja Android
│   ├── app/src/main/java/com/example/medpass/
│   │   ├── MainActivity.java
│   │   ├── VerifyActivity.java
│   │   ├── AdminActivity.java
│   │   ├── ApiClient.java
│   │   └── MedPassApi.java
│   └── app/build.gradle
└── aplikacja medpass.pdf    # Prezentacja projektu
```

## Model danych

System korzysta z pięciu tabel:

| Tabela | Przeznaczenie |
| --- | --- |
| `Participants` | Skróty identyfikatorów uczestników i przypisane kategorie medyczne |
| `SecurityPersonnel` | Konta personelu, skróty haseł i role |
| `Items` | Katalog przedmiotów |
| `DiseaseRules` | Powiązania kategorii medycznych z przedmiotami |
| `AppLogs` | Historia logowania i weryfikacji |

Kod backendu korzysta również z procedury SQL `LogAccess`. **Repozytorium nie zawiera kompletnego skryptu tworzącego bazę**, więc przed uruchomieniem należy przygotować zgodny schemat SQL Server wraz z procedurą.

## Uruchomienie lokalne

### 1. Wymagania

- JDK **21** (backend).
- Android Studio z Android SDK (aplikacja ma `minSdk 24`, `compileSdk 34`).
- Działający **Microsoft SQL Server** z odpowiednimi tabelami i procedurą `LogAccess`.
- Gradle Wrapper z katalogu `backend/` (dla backendu).

### 2. Klonowanie repozytorium

```bash
git clone https://github.com/drogowski-adam/MedPass.git
cd MedPass
```

### 3. Konfiguracja bazy danych

Uzupełnij `backend/src/main/resources/application.properties` danymi własnej instancji:

```properties
spring.application.name=medpassbackend
server.address=0.0.0.0
server.port=8080

spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=MedPass;encrypt=true;trustServerCertificate=true
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
```

Przykład używa lokalnej instancji SQL Server i `trustServerCertificate=true` **wyłącznie dla środowiska deweloperskiego**. Dostosuj adres, nazwę bazy i ustawienia TLS do swojej konfiguracji. Nie commituj rzeczywistych haseł do repozytorium — najlepiej podawaj je przez zmienne środowiskowe lub zewnętrzną konfigurację.

### 4. Uruchomienie backendu

W katalogu `backend`:

**Windows:**

```powershell
.\gradlew.bat bootRun
```

**Linux / macOS:**

```bash
./gradlew bootRun
```

Po starcie sprawdź endpoint:

```text
GET http://localhost:8080/health
```

Oczekiwana odpowiedź: `OK`.

### 5. Uruchomienie aplikacji Android

1. Otwórz katalog `mobile_app/` w Android Studio.
2. W pliku `mobile_app/app/src/main/java/com/example/medpass/ApiClient.java` ustaw `BASE_URL` na adres osiągalny z urządzenia lub emulatora. Domyślna wartość w repozytorium wskazuje konkretny prywatny adres IP.
3. Dla standardowego emulatora Android można użyć `http://10.0.2.2:8080/`; na telefonie w tej samej sieci użyj adresu LAN komputera z backendem.
4. Uruchom backend, a następnie zbuduj i uruchom aplikację.

**Uwaga:** plik `AndroidManifest.xml` deklaruje `ResultActivity`, której nie ma w obecnej strukturze źródeł. Jeśli powoduje to problem przy uruchamianiu, należy usunąć nieużywaną deklarację lub dodać brakującą aktywność.

## REST API

| Metoda | Endpoint | Opis |
| --- | --- | --- |
| `POST` | `/login` | Logowanie personelu; JSON zawiera `username` i `password` |
| `GET` | `/verify?doc=...&guard=...` | Weryfikacja uczestnika i rejestracja operacji |
| `GET` | `/admin/logs?guard=...` | Pobranie logów wybranego pracownika |
| `GET` | `/health` | Test działania serwera (`OK`) |

Przykład żądania logowania:

```bash
curl -X POST http://localhost:8080/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"example","password":"example"}'
```

## Bezpieczeństwo i ograniczenia

MedPass przetwarza informacje powiązane ze zdrowiem, dlatego wymaga szczególnej ostrożności przy wdrożeniu. W aktualnym kodzie:

- Identyfikatory uczestników i nazwy użytkowników są haszowane przez **SHA-256**; hasła są również porównywane jako zwykłe skróty SHA-256. **SHA-256 bez soli nie jest odpowiednią metodą przechowywania haseł w systemie produkcyjnym** — zalecane są Argon2id, bcrypt lub PBKDF2.
- API komunikuje się po **HTTP**, a identyfikator dokumentu jest przesyłany jako parametr adresu URL. Dla rzeczywistego wdrożenia należy używać **HTTPS** i ograniczyć przesyłanie wrażliwych danych w URL.
- Endpoint `/admin/logs` w pokazanym kodzie nie weryfikuje tokenu ani uprawnień administratora. Wdrożenie produkcyjne wymaga uwierzytelniania i autoryzacji po stronie serwera.
- Logowanie i dostęp do danych wymagają dodatkowych zabezpieczeń, m.in. ograniczeń liczby prób, kontroli uprawnień, zarządzania sekretami i oceny zgodności z RODO.

Projekt należy traktować jako **prototyp edukacyjny**, a nie gotowe rozwiązanie do obsługi rzeczywistych danych medycznych.

## Autorzy

- **Magdalena Forowicz**
- **Adam Drogowski**

## Materiały

Szczegółowe założenia, przykładowe scenariusze i zrzuty ekranów znajdują się w pliku [`aplikacja medpass.pdf`](./aplikacja%20medpass.pdf).
