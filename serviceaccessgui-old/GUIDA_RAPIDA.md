# 🚀 Guida Rapida - Service Access GUI

## Setup in 3 Passi

### 1️⃣ Copia il Progetto

Copia la directory `serviceaccessgui` nel tuo workspace, allo stesso livello degli altri progetti.

```
workspace/
├── coldstorageservice/
├── test/
├── serviceaccessgui/        ← NUOVO
└── unibolibs/
```

### 2️⃣ Assicurati che unibolibs sia accessibile

Il progetto usa `unibo.basicomm23-1.0.jar` da `../unibolibs`.

Verifica che il file esista:
```bash
ls ../unibolibs/unibo.basicomm23-1.0.jar
```

### 3️⃣ Avvia i Sistemi

**Terminale 1 - ColdStorageService:**
```bash
cd coldstorageservice
./gradlew run
```

Aspetta che appaia:
```
coldstorageservice waiting for requests...
```

**Terminale 2 - Alarms:**
```bash
cd alarms
./gradlew run
```

**Terminale 3 - GUI:**
```bash
cd serviceaccessgui
./gradlew bootRun
```

Aspetta che appaia:
```
🔌 Connessione a ColdStorageService su localhost:8015...
✅ Connesso a ColdStorageService!
```

**Terminale 4 - BasicRobot:**
```bash
cd basicrobot
./gradlew run
```

### 4️⃣ Apri il Browser

Vai su: **http://localhost:8080**

---

## 🎮 Test Manuale

### Test 1: Richiesta Accettata

1. Inserisci peso: `50` kg
2. Clicca "Invia Richiesta"
3. ✅ Dovresti vedere:
   - Messaggio verde "Richiesta accettata! Ticket: 1"
   - Card del ticket con numero, peso e timer
   - Timer che parte da 15:00 e scende

4. Aspetta 2 secondi (simula il truck che arriva)
5. Clicca "Invia Ticket" (il numero è già inserito)
6. ✅ Dovresti vedere:
   - Messaggio verde "Carico preso in consegna!"
   - Il ticket scompare

### Test 2: Richiesta Rifiutata (Spazio Insufficiente)

1. Inserisci peso: `250` kg (oltre MAXW=200)
2. Clicca "Invia Richiesta"
3. ❌ Dovresti vedere:
   - Messaggio rosso "Richiesta rifiutata: spazio insufficiente"
   - Nessun ticket

### Test 3: Ticket Scaduto

1. Inserisci peso: `30` kg
2. Clicca "Invia Richiesta"
3. ⏳ Aspetta che il timer scada (15 secondi)
4. Il timer diventa "SCADUTO" in arancione
5. Clicca "Invia Ticket"
6. ❌ Dovresti vedere:
   - Messaggio rosso "Ticket rifiutato: ticket scaduto o non valido"

### Test 4: Ticket Non Valido

1. Nel campo "Numero Ticket" inserisci: `999`
2. Clicca "Invia Ticket"
3. ❌ Dovresti vedere:
   - Messaggio rosso "Ticket rifiutato"

---

## 🔍 Verifica Funzionamento

### Controllo Console Browser

Apri DevTools (F12) → Console.

Dovresti vedere log come:
```
🚀 ServiceAccessGUI inizializzata
📦 Invio richiesta deposito: 50 kg
✅ Richiesta accettata! Ticket: 1
🎫 Invio ticket: 1
✅ Ticket accettato: Carico preso in consegna!
```

### Controllo Console Spring Boot

Nel terminale della GUI dovresti vedere:
```
🔌 Connessione a ColdStorageService su localhost:8015...
✅ Connesso a ColdStorageService!
📦 Richiesta deposito: 50 kg
📨 Risposta: storeaccepted(1,50)
🎫 Invio ticket: 1
📨 Risposta: chargetaken(1)
```

### Controllo Console ColdStorageService

Nel terminale del sistema dovresti vedere:
```
coldstorageservice received first request to store 50.0 kg
coldstorageservice accepting load of 50.0 kg
coldstorageservice generating ticket n. 1
coldstorageservice received ticket n. 1
coldstorageservice accepting ticket n. 1 (50.0 kg)
```

---

## 🐛 Problemi Comuni

### Problema: "Errore di comunicazione con il server"

**Causa:** ColdStorageService non è in esecuzione o non è sulla porta 8015.

**Soluzione:**
1. Verifica che il servizio sia avviato
2. Controlla i log del ColdStorageService
3. Verifica che la porta 8015 sia libera

### Problema: "CORS Error" nella console del browser

**Causa:** Problema di configurazione CORS.

**Soluzione:**
- Verifica che l'annotazione `@CrossOrigin` sia presente nel controller
- Riavvia Spring Boot

### Problema: La pagina non si carica

**Causa:** Porta 8080 già in uso.

**Soluzione:**
1. Cambia porta in `application.properties`
2. O termina il processo sulla porta 8080:
   ```bash
   lsof -ti:8080 | xargs kill -9
   ```

### Problema: Il timer non funziona

**Causa:** JavaScript non caricato o errore nel codice.

**Soluzione:**
1. Apri DevTools (F12) → Console
2. Verifica se ci sono errori JavaScript
3. Ricarica la pagina (Ctrl+R)

---

## 📊 Monitoraggio

### Log Dettagliati

Per vedere log più dettagliati, modifica `application.properties`:

```properties
logging.level.it.unibo.serviceaccessgui=TRACE
```

### Test con cURL

Puoi testare l'API anche senza browser:

**Store Request:**
```bash
curl -X POST http://localhost:8080/api/store \
  -H "Content-Type: application/json" \
  -d '{"weight": 50}'
```

**Ticket Request:**
```bash
curl -X POST http://localhost:8080/api/ticket \
  -H "Content-Type: application/json" \
  -d '{"ticketNumber": "1"}'
```

**Status:**
```bash
curl http://localhost:8080/api/status
```

---

## 🎨 Personalizzazione

### Cambiare il Titolo

In `index.html`:
```html
<h1>🏭 Il Mio Storage</h1>
```

### Cambiare i Colori

In `styles.css`:
```css
:root {
    --primary-color: #ff6b6b;  /* Rosso */
}
```

### Aggiungere Campi

1. Aggiungi campo in `index.html`
2. Modifica `submitStoreRequest()` in `app.js`
3. Aggiorna `StoreRequest` in `ColdStorageController.kt`

---

## 📝 Checklist Finale

Prima di considerare il progetto completo:

- [ ] Il sistema ColdStorageService è in esecuzione
- [ ] La GUI si connette correttamente
- [ ] Le richieste di deposito funzionano
- [ ] Il timer del ticket funziona
- [ ] L'invio del ticket funziona
- [ ] I messaggi di errore sono chiari
- [ ] La GUI è responsive (prova su mobile)
- [ ] I log sono chiari e informativi

---

## 🎓 Per l'Esame

### Cosa Mostrare

1. **Funzionalità di base:**
   - Richiesta deposito accettata
   - Visualizzazione ticket con timer
   - Invio ticket con successo

2. **Gestione errori:**
   - Richiesta rifiutata (peso eccessivo)
   - Ticket scaduto
   - Errori di connessione

3. **Design:**
   - Interfaccia moderna e responsive
   - Feedback chiaro all'utente
   - Animazioni e transizioni

### Punti di Forza da Evidenziare

- ✅ Comunicazione REST tra GUI e backend
- ✅ Backend che fa da bridge tra Web e QActor
- ✅ Timer in tempo reale per scadenza ticket
- ✅ Design moderno con CSS moderno
- ✅ Gestione errori completa
- ✅ Logging dettagliato
- ✅ Documentazione completa

### Possibili Domande

**Q: Perché usare Spring Boot?**
A: Fornisce un web server embedded, gestione REST, e integrazione facile con basicomm23.

**Q: Come comunica la GUI con il sistema QActor?**
A: La GUI (JavaScript) → REST → Spring Boot Controller → basicomm23 (TCP) → QActor

**Q: Perché non usare WebSocket?**
A: Per semplicità. REST è più semplice e sufficiente per questo caso d'uso.

**Q: Come gestisci la scadenza del ticket?**
A: Timer JavaScript che aggiorna ogni 100ms e confronta con tempo di scadenza.

---

**Pronto! 🎉**
