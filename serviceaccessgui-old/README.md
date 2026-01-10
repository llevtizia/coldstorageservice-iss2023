# Service Access GUI

Interfaccia web per il sistema Cold Storage Service.

## 🎯 Funzionalità

- ✅ Visualizzazione peso corrente nella ColdRoom
- ✅ Richiesta deposito materiale
- ✅ Ricezione ticket con timer di scadenza
- ✅ Invio ticket quando il truck è all'INDOOR
- ✅ Messaggi di feedback chiari
- ✅ Design moderno e responsive

## 📋 Requisiti

- Java 17+
- Gradle 7.0+
- Il sistema ColdStorageService deve essere in esecuzione sulla porta 8015

## 🚀 Come Eseguire

### 1. Assicurati che il ColdStorageService sia in esecuzione

```bash
cd <directory-del-progetto-principale>
./gradlew run
```

Il servizio deve essere attivo su `localhost:8015`.

### 2. Compila e avvia la GUI

```bash
cd serviceaccessgui
./gradlew bootRun
```

L'applicazione sarà disponibile su: **http://localhost:8080**

## 🖥️ Come Usare

### Richiesta di Deposito

1. Inserisci il peso da depositare (in kg) nel campo "Peso da depositare"
2. Clicca su "Invia Richiesta"
3. Se la richiesta è accettata:
   - Riceverai un **ticket number**
   - Vedrai un **timer** che indica quando scade il ticket (15 secondi)
   - Il ticket number sarà già inserito nel campo "Invio Ticket"

### Invio del Ticket

1. Quando il truck è all'INDOOR, clicca su "Invia Ticket"
2. Se il ticket è valido, riceverai conferma che il carico è stato preso in consegna
3. Il truck può lasciare l'INDOOR

## 📁 Struttura del Progetto

```
serviceaccessgui/
├── build.gradle                          # Configurazione Gradle
├── src/
│   └── main/
│       ├── kotlin/
│       │   └── it/unibo/serviceaccessgui/
│       │       ├── ServiceAccessGuiApp.kt         # Main application
│       │       └── ColdStorageController.kt       # REST API controller
│       └── resources/
│           ├── application.properties             # Configurazione Spring Boot
│           └── static/
│               ├── index.html                     # Interfaccia web
│               ├── styles.css                     # Stili
│               └── app.js                         # Logica JavaScript
```

## 🔧 Configurazione

### Porta del Server

Per cambiare la porta della GUI (default: 8080), modifica `src/main/resources/application.properties`:

```properties
server.port=8090
```

### Connessione al ColdStorageService

Per cambiare host/porta del ColdStorageService, modifica in `ColdStorageController.kt`:

```kotlin
private val COLDSTORAGE_HOST = "localhost"
private val COLDSTORAGE_PORT = "8015"
```

### Tempo di Scadenza del Ticket

Per cambiare il tempo di scadenza (default: 15 secondi), modifica in `app.js`:

```javascript
const TICKET_TIME = 15; // secondi
```

**IMPORTANTE:** Questo valore deve corrispondere a `TICKETTIME` nel sistema QActor!

## 🌐 API Endpoints

La GUI espone i seguenti endpoints REST:

### GET /api/status
Ottiene lo stato corrente del sistema.

**Risposta:**
```json
{
  "currentWeight": 100.0
}
```

### POST /api/store
Invia richiesta di deposito.

**Request:**
```json
{
  "weight": 50
}
```

**Risposta (successo):**
```json
{
  "success": true,
  "ticket": "1",
  "weight": 50,
  "message": "Richiesta accettata! Ticket: 1"
}
```

**Risposta (rifiuto):**
```json
{
  "success": false,
  "message": "Richiesta rifiutata: spazio insufficiente"
}
```

### POST /api/ticket
Invia un ticket.

**Request:**
```json
{
  "ticketNumber": "1"
}
```

**Risposta (successo):**
```json
{
  "success": true,
  "message": "Carico preso in consegna! Il truck può lasciare l'INDOOR."
}
```

**Risposta (rifiuto):**
```json
{
  "success": false,
  "message": "Ticket rifiutato: ticket scaduto o non valido"
}
```

## 🐛 Troubleshooting

### Errore di connessione al ColdStorageService

Se vedi errori come "Errore di comunicazione con il server":

1. Verifica che il ColdStorageService sia in esecuzione
2. Controlla che sia sulla porta corretta (8015)
3. Verifica i log del controller per dettagli

### La pagina non si carica

1. Verifica che Spring Boot sia avviato correttamente
2. Controlla i log per errori
3. Assicurati che la porta 8080 non sia già in uso

### Il timer non funziona

Assicurati che il valore di `TICKET_TIME` in `app.js` corrisponda al valore di `TICKETTIME` nel sistema QActor.

## 📝 Note

- La GUI usa **CORS** per permettere richieste da qualsiasi origine
- Il peso corrente viene aggiornato automaticamente ogni 5 secondi
- I messaggi di feedback scompaiono automaticamente dopo 5 secondi
- Il timer del ticket ha una precisione di 100ms per una visualizzazione fluida

## 🎨 Personalizzazione

### Colori

Per cambiare i colori della GUI, modifica le variabili CSS in `styles.css`:

```css
:root {
    --primary-color: #2563eb;    /* Blu principale */
    --success-color: #10b981;    /* Verde successo */
    --danger-color: #ef4444;     /* Rosso errore */
    /* ... */
}
```

### Testi

Tutti i testi sono modificabili nei file `index.html` e `app.js`.

## 📄 Licenza

Progetto per esame universitario - Ingegneria dei Sistemi Software 2023
