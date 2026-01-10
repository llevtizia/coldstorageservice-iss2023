# 📋 Service Access GUI - Progetto Completo

## ✨ Cosa Ho Creato

Ho creato una **Service Access GUI moderna e completa** per il tuo progetto Cold Storage Service, con:

### 🎯 Funzionalità Implementate

1. **Visualizzazione Stato Sistema**
   - Peso corrente nella ColdRoom
   - Capacità massima
   - Auto-refresh ogni 5 secondi

2. **Richiesta di Deposito**
   - Form per inserire il peso
   - Validazione input (1-200 kg)
   - Feedback immediato (accettato/rifiutato)

3. **Gestione Ticket**
   - Visualizzazione ticket ricevuto
   - **Timer in tempo reale** (countdown da 15 secondi)
   - Avviso quando il ticket sta per scadere
   - Auto-fill del campo ticket

4. **Invio Ticket**
   - Form per inviare il ticket
   - Conferma quando il carico è preso
   - Gestione ticket scaduti/non validi

5. **Design Moderno**
   - Interfaccia responsive (funziona su mobile)
   - Colori moderni e accattivanti
   - Animazioni fluide
   - Messaggi di feedback chiari

### 🏗️ Architettura

```
┌─────────────┐         ┌──────────────────┐         ┌─────────────────────┐
│   Browser   │  HTTP   │   Spring Boot    │   TCP   │  ColdStorageService │
│  (HTML/JS)  │ ───────>│   Controller     │ ───────>│      (QActor)       │
│             │  REST   │ (basicomm23)     │         │                     │
└─────────────┘         └──────────────────┘         └─────────────────────┘
```

**Stack Tecnologico:**
- **Frontend:** HTML5 + CSS3 + JavaScript (vanilla, nessun framework)
- **Backend:** Spring Boot 3.2 + Kotlin
- **Comunicazione:** REST API + basicomm23 (TCP)
- **Build:** Gradle 7+

---

## 📦 Struttura del Progetto

```
serviceaccessgui/
│
├── build.gradle                    # Configurazione Gradle
├── settings.gradle                 # Nome progetto
├── README.md                       # Documentazione completa
├── GUIDA_RAPIDA.md                # Guida setup e test
│
└── src/
    └── main/
        ├── kotlin/it/unibo/serviceaccessgui/
        │   ├── ServiceAccessGuiApp.kt          # Main Spring Boot
        │   └── ColdStorageController.kt        # REST API Controller
        │
        └── resources/
            ├── application.properties          # Configurazione
            └── static/
                ├── index.html                  # Interfaccia web
                ├── styles.css                  # Stili moderni
                └── app.js                      # Logica applicazione
```

---

## 🚀 Come Usare

### Setup Rapido

1. **Copia il progetto** nella tua workspace:
   ```bash
   cp -r serviceaccessgui /path/to/workspace/
   ```

2. **Assicurati che il ColdStorageService sia in esecuzione:**
   ```bash
   cd coldstorageservice
   ./gradlew run
   ```

3. **Avvia la GUI:**
   ```bash
   cd serviceaccessgui
   ./gradlew bootRun
   ```

4. **Apri il browser:**
   ```
   http://localhost:8080
   ```

### Test Veloce

1. Inserisci peso: `50` kg
2. Clicca "Invia Richiesta"
3. Vedi il ticket con timer
4. Aspetta 2 secondi
5. Clicca "Invia Ticket"
6. Vedi conferma "Carico preso in consegna!"

---

## 🎨 Caratteristiche del Design

### Interfaccia Utente

- **Sfondo con gradiente** (viola/blu)
- **Card bianche** con ombre per ogni sezione
- **Colori semantici:**
  - 🔵 Blu per azioni primarie
  - 🟢 Verde per successi
  - 🔴 Rosso per errori
  - 🟠 Arancione per avvisi

- **Animazioni:**
  - Slide-in per messaggi
  - Pulse per timer in scadenza
  - Hover effects sui bottoni

- **Responsive:**
  - Si adatta a desktop, tablet e mobile
  - Layout flessibile con CSS Grid

### User Experience

- **Feedback immediato** per ogni azione
- **Messaggi chiari** e comprensibili
- **Auto-fill** del ticket nel form
- **Auto-refresh** dello stato ogni 5 secondi
- **Disabilitazione pulsanti** durante le richieste
- **Countdown visuale** per scadenza ticket

---

## 🔌 API REST Endpoints

### GET /api/status
Ottiene lo stato corrente.
```json
Response: { "currentWeight": 100.0 }
```

### POST /api/store
Richiesta di deposito.
```json
Request:  { "weight": 50 }
Response: {
  "success": true,
  "ticket": "1",
  "weight": 50,
  "message": "Richiesta accettata! Ticket: 1"
}
```

### POST /api/ticket
Invio ticket.
```json
Request:  { "ticketNumber": "1" }
Response: {
  "success": true,
  "message": "Carico preso in consegna! Il truck può lasciare l'INDOOR."
}
```

---

## ⚙️ Configurazione

### Porte

- **GUI:** 8080 (configurabile in `application.properties`)
- **ColdStorageService:** 8015 (configurabile in `ColdStorageController.kt`)

### Timer Ticket

- **Default:** 15 secondi
- **Modificabile in:** `app.js` → `TICKET_TIME`
- **Deve corrispondere** a `TICKETTIME` nel sistema QActor!

---

## 🧪 Testing

### Test Manuali

1. ✅ Richiesta accettata
2. ❌ Richiesta rifiutata (peso > 200)
3. ⏳ Ticket scaduto
4. ❌ Ticket non valido

Vedi `GUIDA_RAPIDA.md` per test dettagliati.

### Test con cURL

```bash
# Store request
curl -X POST http://localhost:8080/api/store \
  -H "Content-Type: application/json" \
  -d '{"weight": 50}'

# Ticket request
curl -X POST http://localhost:8080/api/ticket \
  -H "Content-Type: application/json" \
  -d '{"ticketNumber": "1"}'

# Status
curl http://localhost:8080/api/status
```

---

## 💡 Vantaggi di Questa Soluzione

### ✅ Semplicità
- **Nessun framework frontend** complesso (React, Angular, Vue)
- **HTML/CSS/JS vanilla** = facile da capire e modificare
- **Spring Boot minimale** = solo lo stretto necessario

### ✅ Modernità
- **Design attuale** con CSS moderno
- **API REST** standard
- **Responsive** per ogni dispositivo

### ✅ Completezza
- **Tutte le funzionalità** richieste dal tema
- **Gestione errori** completa
- **Logging dettagliato**
- **Documentazione estesa**

### ✅ Manutenibilità
- **Codice ben organizzato**
- **Commenti chiari**
- **Separazione frontend/backend**
- **Facile da estendere**

---

## 🎓 Per l'Esame

### Punti Forti da Evidenziare

1. **Architettura a 3 livelli:**
   - Presentation (HTML/JS)
   - Business Logic (Spring Boot)
   - Data (QActor System)

2. **Comunicazione:**
   - Frontend ↔️ Backend: REST API
   - Backend ↔️ QActor: TCP (basicomm23)

3. **Timer in Tempo Reale:**
   - JavaScript countdown
   - Avviso visuale quando sta per scadere
   - Gestione scadenza lato client

4. **User Experience:**
   - Interfaccia intuitiva
   - Feedback immediato
   - Design professionale

### Possibili Estensioni

Se hai tempo, puoi aggiungere:

1. **WebSocket** per aggiornamenti in tempo reale
2. **Storico richieste** con database
3. **Autenticazione** utenti
4. **Dashboard Service Manager** (ServiceStatusGUI)
5. **Grafici** del peso nel tempo

---

## 📚 Documentazione Inclusa

1. **README.md** - Documentazione completa del progetto
2. **GUIDA_RAPIDA.md** - Setup e test in 5 minuti
3. **Questo file** - Panoramica generale

Tutti i file sono ben commentati e autodocumentanti.

---

## 🔧 Modifiche Facili

### Cambiare Colori

In `styles.css`:
```css
:root {
    --primary-color: #2563eb;  /* Cambia qui! */
}
```

### Cambiare Testi

Tutti in `index.html` e `app.js`, facili da trovare.

### Aggiungere Campi

1. HTML: aggiungi input
2. JS: modifica `submitStoreRequest()`
3. Kotlin: aggiorna `StoreRequest` data class

### Aggiungere Endpoints

1. Kotlin: aggiungi metodo in `ColdStorageController`
2. JS: aggiungi chiamata `fetch()` in `app.js`

---

## 🎉 Pronto all'Uso!

Il progetto è **completo e funzionante**. Devi solo:

1. Copiarlo nella tua workspace
2. Avviare i sistemi
3. Aprire il browser

Tutto il resto è già fatto! 🚀

---

## 📞 Prossimi Passi

1. **Testa il progetto** con la GUIDA_RAPIDA.md
2. **Personalizza** colori e testi se vuoi
3. **Aggiungi screenshot** nel README per l'esame
4. **Opzionale:** aggiungi ServiceStatusGUI se richiesta

---

**Buon lavoro! 🎓**
