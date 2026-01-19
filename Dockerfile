# ==========================================
# STAGE 1: Budowanie silnika (Java/Maven)
# ==========================================
# Uwaga: Używasz Java 24 (EA). Upewnij się, że obraz bazowy ją wspiera.
# Dla bezpieczeństwa używam tu wersji 21, ale jeśli 24 jest wymagana,
# musisz użyć odpowiedniego obrazu (np. od eclipse-temurin bezpośrednio).
FROM maven:3.9-eclipse-temurin-25-alpine AS engine-builder

# Ustawiamy strukturę tak, by pasowała do Twojego relatywnego pom.xml
WORKDIR /build/engine

# KLUCZOWE 1: Tworzymy strukturę katalogów, której oczekuje Twój pom.xml
# Maven chce kopiować do ../client/assets/teavm oraz ../server/handlers/teavm
# W kontenerze relatywnie do /build/engine, są to ścieżki:
# /build/client/assets/teavm
# /build/server/handlers/teavm
RUN mkdir -p ../client/assets/teavm && \
    mkdir -p ../server/handlers/teavm

# Kopiujemy pliki projektu Java
COPY engine/pom.xml .
COPY engine/checkers-core ./checkers-core
COPY engine/checkers-web ./checkers-web

# Budujemy. Maven Resources Plugin zadziała i skopiuje pliki JS 
# do utworzonych wyżej katalogów wewnątrz tego kontenera.
RUN mvn clean package -DskipTests

# ==========================================
# STAGE 2: Budowanie klienta (Vue/Vite)
# ==========================================
FROM node:20-alpine AS client-builder
WORKDIR /build/client

# Instalacja zależności
COPY client/package*.json ./
RUN npm install

# Kopiujemy kod źródłowy Vue
COPY client/ .

# KLUCZOWE 2: Nadpisujemy/Wstrzykujemy plik .mjs wygenerowany przez TeaVM
# Pobieramy go z miejsca, w które Maven go wrzucił w Stage 1 (/build/client/...)
COPY --from=engine-builder /build/client/assets/teavm/checkers-engine.mjs ./assets/teavm/

# Teraz budujemy Vue, mając już świeży plik JS z Javy
RUN npm run build

# ==========================================
# STAGE 3: Finalny obraz serwera (Node.js)
# ==========================================
FROM node:20-alpine
WORKDIR /app

# Instalacja zależności serwera
COPY server/package*.json ./
RUN npm install --production

# Kopiowanie kodu serwera
COPY server/ .

# KLUCZOWE 3: Wstrzykujemy plik .js wygenerowany przez TeaVM dla backendu
# Pobieramy go z miejsca, w które Maven go wrzucił w Stage 1 (/build/server/...)
COPY --from=engine-builder /build/server/handlers/teavm/checkers-engine.js ./handlers/teavm/

# Kopiujemy zbudowanego klienta Vue do folderu public
COPY --from=client-builder /build/client/dist ./public

EXPOSE 3000
CMD ["node", "app.js"]
