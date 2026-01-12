// -----------------------------------------
// COLD STORAGE SERVICE - logica interfaccia
// WebSocket Communication with Facade
// -----------------------------------------


// -----------------------------------------
// 1 - CONFIGURAZIONE: costanti e variabili globali per l'applicazione
// -----------------------------------------
// costanti
const WS_URL = "ws://localhost:8091/accessgui";    // URL WS PORTA 8091
const TICKETTIME = 30;                              // tempo scadenza ticket (aumentato da 15 a 30)
const MAXW = 200;                                   // capacità massima cold room (kg)

// variabili globali
let socket = null;              // connessione websocket
let currentTicket = null;       // ticket corrente mostrato
let ticketExpiryTime = null;    // timestamp scadenza ticket
let timerInterval = null;       // intervallo countdown timer
let currentWeight = 0;          // peso corrente nella coldroom

// -----------------------------------------
// 2 - CONNESSIONE WEBSOCKET
// -----------------------------------------

// Inizializza la connessione WebSocket al server
function connectWebSocket() {
    console.log("Connessione al WebSocket:", WS_URL); // creo la connessione

    try {
        socket = new WebSocket(WS_URL);

        socket.onopen = function() {
            // connessione riuscita
            console.log("WebSocket connessa");
        };

        socket.onmessage = function(event) {
            // ricezione messaggio dal server
            console.log("Messaggio ricevuto:", event.data);
            handleServerMessage(event.data);
        };

        socket.onerror = function(error) {
            console.error("Errore WebSocket:", error);
            showMessage("Errore di connessione", "error");
        };

        socket.onclose = function() {
            console.log("WebSocket disconnessa");
            showMessage("Connessione persa. Riconnessione in corso...", "error");
            // riconnessione automatica dopo 3 secondi
            setTimeout(connectWebSocket, 3000);
        };

    } catch (error) {
        console.error("Errore creazione WebSocket:", error);
        showMessage("Impossibile connettersi al server", "error");
    }
}

/**
* invia un messaggio tramite WebSocket
* messaggio da inviare -> string message
*/
function sendMessage(message) {
    if (socket && socket.readyState === WebSocket.OPEN) {
        console.log("Invio messaggio:", message);
        socket.send(message);
    } else {
        console.error("WebSocket non connessa");
        showMessage("Non connesso al server", "error");
    }
}

// -----------------------------------------
// 3 - GESTIONE MESSAGGI DAL SERVER
// -----------------------------------------

/**
 * processa i messaggi ricevuti dal server
 * messaggio ricevuto -> string message
 */
function handleServerMessage(message) {
    console.log("Processing:", message); // chiamo la funzione handler specifica per ogni messaggio

    // store accepted (prima richiesta accettata)
    if (message.includes("storeaccepted")) {
        handleStoreAccepted(message);
    }
    // store refused (prima richiesta rifiutata)
    else if (message.includes("storerefused")) {
        handleStoreRefused(message);
    }
    // ticket request accettata e carico preso (seconda richiesta accettata)
    else if (message.includes("chargetaken")) {
        handleChargeTaken(message);
    }
    // ticket request rifiutata (seconda richiesta rifiutata)
    else if (message.includes("ticketrefused")) {
        handleTicketRefused(message);
    }
    // aggiornamento peso corrente
    else if (message.includes("currentlystored")) {
        handleCurrentlyStored(message);
    }
    // altri messaggi
    else {
        console.log("Messaggio generico:", message);
    }
}

/**
 * gestisce risposta storeaccepted( TICKET, KG )
 */
function handleStoreAccepted(message) {
    // estrae ticket e peso dal messaggio
    // formato: "storeaccepted(1, 50.0)"
    const match = message.match(/storeaccepted\s*\(\s*(\d+)\s*,\s*([\d.]+)\s*\)/);
    /**
    * storeaccepted     cerca esattamente la stringa (in minuscolo)
    * \s*               zero o più spazi bianchi (spazio, tab, newline)
    * \(                parentesi aperta (\ serve come escape)
    * \s*               zero o più spazi bianchi
    * (\d+)             () -> gruppo di cattura che memorizza il contenuto per estrarre dati -> match[1]
    *                   \d -> cifra (0-9), + -> una o più volte
    *                   cattura il numero del ticket (es. 1, 12, 123)
    * \s*,\s*           \s* -> zero o più spazi bianchi, , -> carattere virgola, altri spazi
    *                   accetta (1, 50.0) o (1,50.0) o (1 , 50.0)
    * ([\d.]+)          [] -> classe di caratteri: accetta qualsiasi carattere tra questi -> in questo caso, cifre o punto
    *                   accetta "50", "50.0", "100.000" -> cattura peso con decimali
    * \s*               spazi bianchi
    * \)                parentesi chiusa (\ serve come escape)
    */

    if (match) {
        const ticket = match[1]; // "1"
        const weight = parseFloat(match[2]); // 50.0

        console.log(`Richiesta accettata! Ticket: ${ticket}, Peso: ${weight} kg`);

        // mostra messaggio di successo
        showStoreResponse(`Richiesta accettata! Ticket: ${ticket} per ${weight} kg`, "success");

        // mostra il ticket con timer
        showTicket(ticket, weight);

        // reset form
        document.getElementById("storeForm").reset();

    } else {
        console.error("Formato messaggio non valido:", message);
    }
}

/**
 * gestisce risposta storerefused(KG)
 */
function handleStoreRefused(message) {
    // estrae peso dal messaggio
    // formato: storerefused(50.0)
    const match = message.match(/storerefused\s*\(\s*([\d.]+)\s*\)/);

    if (match) {
        const weight = parseFloat(match[1]);
        // calcola spazio disponibile
        const available = MAXW - currentWeight;

        console.log(`Richiesta rifiutata - Peso: ${weight} kg`);

        showStoreResponse(`Richiesta rifiutata!
            ${weight} kg supera lo spazio disponibile (${available} kg)`, "error");

    } else {
        console.error("Formato messaggio non valido:", message);
    }
}

/**
 * gestisce risposta chargetaken(TICKET)
 */
function handleChargeTaken(message) {
    // estrae ticket dal messaggio
    // formato: chargetaken(1)
    const match = message.match(/chargetaken\s*\(\s*(\d+)\s*\)/);

    if (match) {
        const ticket = match[1];

        console.log(`Carico preso - Ticket: ${ticket}`);

        showTicketResponse(`Carico preso! Ticket ${ticket} processato con successo`, "success");

        // nasconde display ticket
        hideTicket();

        // reset form
        document.getElementById("ticketForm").reset();

    } else {
        console.error("Formato messaggio non valido:", message);
    }
}

/**
 * gestisce risposta ticketrefused(TICKET)
 */
function handleTicketRefused(message) {
    // estrae ticket dal messaggio
    // formato: ticketrefused(1)
    const match = message.match(/ticketrefused\s*\(\s*(\d+)\s*\)/);

    if (match) {
        const ticket = match[1];

        console.log(`Ticket rifiutato: ${ticket}`);

        showTicketResponse(`Ticket ${ticket} rifiutato! (Scaduto o non valido)`, "error");

    } else {
        console.error("Formato messaggio non valido:", message);
    }
}

/**
 * gestisce aggiornamento currentlystored(KG)
 */
function handleCurrentlyStored(message) {
    // estrae peso dal messaggio
    // formato: currentlystored(50.0)
    const match = message.match(/currentlystored\s*\(\s*([\d.]+)\s*\)/);

    if (match) {
            const weight = parseFloat(match[1]);

            // validazione: peso deve essere tra 0 e MAXW
            if (weight < 0 || weight > MAXW) {
                console.warn(`Peso fuori range: ${weight} kg (max ${MAXW} kg)`);
                return;
            }

            currentWeight = weight;
            console.log(`Peso aggiornato: ${currentWeight} kg`);
            updateWeightDisplay(currentWeight);

        } else {
            console.error("Formato messaggio non valido:", message);
        }
}


// -----------------------------------------
// 4 - INVIO RICHIESTE AL SERVER
// -----------------------------------------

/**
 * invia richiesta di deposito
 * evento submit del form
 */
function submitStoreRequest(event) {
    event.preventDefault(); // stop submit del form perché invio i dati sulla websocket -> blocco reload pagina

    const weightInput = document.getElementById("weightInput");
    const weight = parseInt(weightInput.value);

    // validazione
    if (!weight || weight < 1 || weight > MAXW) {
        showStoreResponse(`Peso non valido! Inserire un valore tra 1 e ${MAXW} kg`, "error");
        return;
    }

    // verifica connessione
    if (!socket || socket.readyState !== WebSocket.OPEN) {
        showStoreResponse("Non connesso al server!", "error");
        return;
    }

    // costruisce e invia messaggio
    // formato: request/storerequest/storerequest(WEIGHT)
    const message = `request/storerequest/storerequest(${weight})`;
    sendMessage(message);

    // Mostra messaggio di attesa
    showStoreResponse(`Invio richiesta per ${weight} kg...`, "success");
}

/**
 * invia ticket
 * evento submit del form
 */
function submitTicketRequest(event) {
    event.preventDefault(); // come per store request

    const ticketInput = document.getElementById("ticketInput");
    const ticket = parseInt(ticketInput.value);

    // validazione
    if ( !ticket || ticket < 1 ) {
        showTicketResponse("Numero ticket non valido!", "error");
        return;
    }

    // verifica connessione
    if ( !socket || socket.readyState !== WebSocket.OPEN ) {
        showTicketResponse("Non connesso al server!", "error");
        return;
    }

    // costruisce e invia messaggio
    // formato: request/ticketrequest/ticketrequest(TICKET)
    const message = `request/ticketrequest/ticketrequest(${ticket})`;
    sendMessage(message);

    // Mostra messaggio di attesa
    showTicketResponse(`Invio ticket ${ticket}...`, "success");
}



// -----------------------------------------
// 5 - GESTIONE UI
// -----------------------------------------

/**
 * aggiorna display del peso corrente
 * peso corrente in kg -> numero
 */
function updateWeightDisplay(weight) {
    const currentWeightEl = document.getElementById("currentWeight");

    // flash quando cambia
    currentWeightEl.classList.add('updating');
    // aggiorna testo
    currentWeightEl.textContent = `${weight} kg`;
    // indicatore colore
    const percentage = (weight / MAXW) * 100;

    // rimuovi classi colore precedenti
    currentWeightEl.classList.remove('weight-low', 'weight-medium', 'weight-high');

    // aggiungi classe appropriata
    if (percentage < 50) {
        currentWeightEl.classList.add('weight-low');  // verde
    } else if (percentage < 80) {
        currentWeightEl.classList.add('weight-medium');  // arancione
    } else {
        currentWeightEl.classList.add('weight-high');  // rosso
    }

    // rimuovi classe animazione dopo 500ms
    setTimeout(() => {
        currentWeightEl.classList.remove('updating');
    }, 500);

    console.log(`Display aggiornato: ${weight} kg (${percentage.toFixed(1)}%)`);
}

/**
 * mostra il messaggio di risposta per store request sotto il form
 * messaggio da mostrare -> stringa
 * tipo: "success" o "error"
 */
function showStoreResponse(message, type) {
    const responseEl = document.getElementById("storeResponse");
    responseEl.textContent = message;
    responseEl.className = `response-message ${type} show`;
}

/**
 * mostra messaggio di risposta per ticket request
 * uguale alla store response
 */
function showTicketResponse(message, type) {
    const responseEl = document.getElementById("ticketResponse");
    responseEl.textContent = message;
    responseEl.className = `response-message ${type} show`;
}

/**
 * mostra messaggio generico (usato per connessione WebSocket)
 * messaggio, tipo -> stringhe
 */
function showMessage(message, type) {
    console.log(`${type.toUpperCase()}: ${message}`);
    // Potremmo aggiungere un toast notification se vuoi
}


// -----------------------------------------
// 6 - GESTIONE TICKET
// -----------------------------------------

/**
 * mostra il ticket ricevuto con timer
 * parametri -> numero ticket, peso in kg
 */
function showTicket(ticket, weight) {
    // salva ticket corrente e timestamp scadenza
    currentTicket = ticket;
    ticketExpiryTime = Date.now() + (TICKETTIME * 1000); // ticket time = 30 ms * 1000 = 30 secondi

    // aggiorna UI
    document.getElementById("ticketNumber").textContent = ticket;
    document.getElementById("ticketWeight").textContent = `${weight} kg`;
    document.getElementById("ticketDisplay").style.display = "block"; // visualizza la card del ticket

    // precompila il campo ticket
    document.getElementById("ticketInput").value = ticket;

    // avvia countdown
    startTicketTimer();

    console.log(`Ticket mostrato: ${ticket}, scadenza: ${TICKETTIME}s`);
}

/**
 * nasconde ticket display e ferma il timer
 */
function hideTicket() {
    document.getElementById("ticketDisplay").style.display = "none";
    currentTicket = null;
    ticketExpiryTime = null;
    stopTicketTimer();

    console.log("Ticket nascosto");
}

/**
 * avvia countdown timer del ticket
 */
function startTicketTimer() {
    // ferma timer precedente se esiste
    stopTicketTimer();

    timerInterval = setInterval(() => {
        if (!ticketExpiryTime) { // se ticketExpiryTime viene cancellato prima che il timer si fermi -> il ticket è stato gestito
            stopTicketTimer();   // ticketExpiryTime = null in hideTicket (ticket processato con successo/ticket scaduto)
            return;              // stoppo il timer ed esco per sicurezza
        }

        // se arriva qui ticketExpiryTime esiste
        const remaining = Math.max(0, ticketExpiryTime - Date.now());
        const seconds = Math.ceil(remaining / 1000);

        if (seconds <= 0) { // se il tempo rimasto è finito
            // il ticket è scaduto
            handleTicketExpired();
        } else {
            // aggiorna display e calcola minuti e secondi
            const minutes = Math.floor(seconds / 60);
            const secs = seconds % 60; // quanti secondi sono rimasti
            // aggiorna display
            const timerEl = document.getElementById("ticketTimer");
            timerEl.textContent = `${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
            // formato `${minutes}:${secs}` tipo orologio digitale
            // converto il numero in stringa -> padding con zeri (aggiungo zeri a sinistra per avere una stringa lunga 2)

            // cambia colore se < 5 secondi
            if (seconds <= 5) {
                timerEl.classList.add("warning"); // arancione + pulse
            } else {
                timerEl.classList.remove("warning");
            }
        }
    }, 100); // Aggiorna ogni 100ms per precisione
}

/**
 * ferma timer
 */
function stopTicketTimer() {
    if (timerInterval) {
        clearInterval(timerInterval);
        timerInterval = null;
    }
}

/**
 * gestisce scadenza ticket
 */
function handleTicketExpired() {
    console.log(`Ticket ${currentTicket} SCADUTO!`);

    showStoreResponse(`Ticket ${currentTicket} è scaduto!`, "error"); // mostra errore
    hideTicket(); // nasconde card ticket
}


// -----------------------------------------
// 7 - INIZIALIZZAZIONE
// -----------------------------------------

/**
 * inizializza l'applicazione quando la pagina è caricata -> entry point
 * eseguito al caricamento della pagina
 */
window.addEventListener('load', () => {
    console.log("Inizializzazione applicazione...");

    // connetti WebSocket
    connectWebSocket();

    // imposta peso iniziale
    updateWeightDisplay(0);

    console.log("Applicazione pronta");
});

// -----------------------------------------
// 8 - UTILITY
// -----------------------------------------

/**
 * debug: stampa stato corrente
 */
function debugState() {
    console.log("----- DEBUG STATE -----");
    console.log("WebSocket:", socket ? socket.readyState : "null");
    console.log("Current Weight:", currentWeight);
    console.log("Current Ticket:", currentTicket);
    console.log("Ticket Expiry:", ticketExpiryTime ? new Date(ticketExpiryTime) : "null");
    console.log("-----------------------");
}

// funzione di debug eseguibile dalla console del browser
window.debugState = debugState;
