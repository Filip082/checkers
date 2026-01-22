# System do Gry w Warcaby Online

Kompleksowa aplikacja internetowa umożliwiająca grę w warcaby w czasie rzeczywistym. Projekt wykorzystuje nowoczesne technologie webowe oraz unikalne podejście do logiki biznesowej, współdzieląc silnik gry napisany w Javie pomiędzy klientem a serwerem dzięki transpilacji TeaVM.

## 🚀 Możliwości

*   **Gra wieloosobowa w czasie rzeczywistym:** Płynna rozgrywka dzięki wykorzystaniu WebSockets (Socket.io).
*   **Współdzielona logika gry:** Silnik gry napisany w Javie zapewnia spójną walidację ruchów zarówno po stronie przeglądarki (szybki feedback), jak i serwera (bezpieczeństwo).
*   **System kont użytkowników:** Rejestracja, logowanie oraz bezpieczne sesje.
*   **Rankingi:** Śledzenie wyników najlepszych graczy.
*   **Lobby:** Tworzenie i dołączanie do gier.

## 🛠 Technologie

Projekt składa się z kilku modułów wykorzystujących różne technologie:

*   **Frontend (`/client`):**
    *   Vue.js 3 (Composition API)
    *   Vite
    *   HTML5 / CSS3
*   **Backend (`/server`):**
    *   Node.js
    *   Express.js
    *   Socket.io
    *   PostgreSQL (Baza danych)
*   **Silnik Gry (`/engine`):**
    *   Java
    *   Maven
    *   **TeaVM:** Transpilacja kodu Java do JavaScript/WebAssembly, co pozwala na używanie tych samych klas `Board`, `Pawn`, `Move` w środowisku Node.js oraz w przeglądarce.
*   **Infrastruktura:**
    *   Docker & Docker Compose

## 📂 Struktura Projektu

*   `client/` - Aplikacja frontendowa (Vue.js).
*   `server/` - Serwer API i WebSocket (Node.js).
*   `engine/` - Logika gry w Javie.
    *   `checkers-core` - Główna logika gry.
    *   `checkers-web` - Wrapper dla TeaVM generujący pliki `.js`.
*   `database/` - Skrypty inicjalizacyjne bazy danych SQL.
*   `docker-compose.yml` - Konfiguracja orkiestracji kontenerów.

## ⚙️ Wymagania

*   Docker Desktop / Docker Engine
*   Docker Compose

## 🚀 Uruchomienie (Docker)

To zalecana metoda uruchomienia całej aplikacji. Proces budowania automatycznie kompiluje silnik Javy, transpiluje go do JS, buduje frontend i uruchamia serwer.

1.  **Konfiguracja środowiska:**
    Skopiuj plik przykładowy i dostosuj go (jeśli to konieczne):
    ```bash
    cp .env.example .env
    ```

2.  **Uruchomienie:**
    W głównym katalogu projektu wykonaj polecenie:
    ```bash
    docker-compose up --build
    ```

3.  **Dostęp:**
    Po zakończeniu budowania aplikacja będzie dostępna pod adresem:
    
    👉 **http://localhost:3000**

## 💻 Uruchomienie lokalne (Development)

Jeśli chcesz rozwijać projekt bez pełnego builda Dockerowego, musisz ręcznie przygotować komponenty.

1.  **Baza danych:** Uruchom instancję PostgreSQL i zaimportuj `database/warcaby.sql`.
2.  **Silnik (Engine):** Musisz mieć zainstalowaną Javę (JDK 25) oraz Maven.
    ```bash
    cd engine
    mvn clean package
    ```
    *To polecenie wygeneruje pliki `.js` i `.mjs` w katalogach `target`, które należy ręcznie lub skryptem przenieść do `client/assets/teavm` oraz `server/handlers/teavm`.*
3.  **Serwer:**
    ```bash
    cd server
    npm install
    npm run dev
    ```
4.  **Klient:**
    ```bash
    cd client
    npm install
    npm run dev
    ```

## 📝 Licencja

Projekt stworzony w celach edukacyjnych.
