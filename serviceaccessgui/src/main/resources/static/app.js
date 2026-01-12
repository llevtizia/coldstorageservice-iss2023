// -----------------------------------------
// COLD STORAGE SERVICE - logica interfaccia
// WebSocket Communication with Facade
// -----------------------------------------


// -----------------------------------------
// 1 - CONFIGURAZIONE: costanti e variabili globali per l'applicazione
// -----------------------------------------
// costanti
const WS_URL = "ws://localhost:8091/accessgui";    // URL WS PORTA 8091
const TICKETTIME = 30;                              // tempo scadenza ticket
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
    console.log("🔍 Processing:", message);

    // Store Request - Accettata
    if (message.includes("storeaccepted")) {
        handleStoreAccepted(message);
    }
    // Store Request - Rifiutata
    else if (message.includes("storerefused")) {
        handleStoreRefused(message);
    }
    // Ticket Request - Accettata (carico preso)
    else if (message.includes("chargetaken")) {
        handleChargeTaken(message);
    }
    // Ticket Request - Rifiutata
    else if (message.includes("ticketrefused")) {
        handleTicketRefused(message);
    }
    // Aggiornamento peso corrente (se implementato)
    else if (message.includes("currentlyStored")) {
        handleCurrentlyStored(message);
    }
    // Altri messaggi
    else {
        console.log("ℹ️ Messaggio generico:", message);
    }
}

/**
 * Gestisce risposta storeaccepted(TICKET, KG)
 */
function handleStoreAccepted(message) {
    // Estrae ticket e peso dal messaggio
    // Formato: storeaccepted(1, 50.0)
    const match = message.match(/storeaccepted\s*\(\s*(\d+)\s*,\s*([\d.]+)\s*\)/);

    if (match) {
        const ticket = match[1];
        const weight = parseFloat(match[2]);

        console.log(`✅ Richiesta accettata - Ticket: ${ticket}, Peso: ${weight} kg`);

        // Mostra messaggio di successo
        showStoreResponse(`✅ Richiesta accettata! Ticket: ${ticket} per ${weight} kg`, "success");

        // Mostra il ticket con timer
        showTicket(ticket, weight);

        // Reset form
        document.getElementById("storeForm").reset();

    } else {
        console.error("❌ Formato messaggio non valido:", message);
    }
}

/**
 * Gestisce risposta storerefused(KG)
 */
function handleStoreRefused(message) {
    // Estrae peso dal messaggio
    // Formato: storerefused(50.0)
    const match = message.match(/storerefused\s*\(\s*([\d.]+)\s*\)/);

    if (match) {
        const weight = parseFloat(match[1]);

        console.log(`❌ Richiesta rifiutata - Peso: ${weight} kg`);

        // Calcola spazio disponibile
        const available = MAXW - currentWeight;

        showStoreResponse(
            `❌ Richiesta rifiutata! ${weight} kg supera lo spazio disponibile (${available} kg)`,
            "error"
        );

    } else {
        console.error("❌ Formato messaggio non valido:", message);
    }
}

/**
 * Gestisce risposta chargetaken(TICKET)
 */
function handleChargeTaken(message) {
    // Estrae ticket dal messaggio
    // Formato: chargetaken(1)
    const match = message.match(/chargetaken\s*\(\s*(\d+)\s*\)/);

    if (match) {
        const ticket = match[1];

        console.log(`✅ Carico preso - Ticket: ${ticket}`);

        showTicketResponse(`✅ Carico preso! Ticket ${ticket} processato con successo`, "success");

        // Nascondi display ticket
        hideTicket();

        // Reset form
        document.getElementById("ticketForm").reset();

        // Aggiorna stato (opzionale)
        refreshStatus();

    } else {
        console.error("❌ Formato messaggio non valido:", message);
    }
}

/**
 * Gestisce risposta ticketrefused(TICKET)
 */
function handleTicketRefused(message) {
    // Estrae ticket dal messaggio
    // Formato: ticketrefused(1)
    const match = message.match(/ticketrefused\s*\(\s*(\d+)\s*\)/);

    if (match) {
        const ticket = match[1];

        console.log(`❌ Ticket rifiutato: ${ticket}`);

        showTicketResponse(`❌ Ticket ${ticket} rifiutato! (Scaduto o non valido)`, "error");

    } else {
        console.error("❌ Formato messaggio non valido:", message);
    }
}

/**
 * Gestisce aggiornamento currentlyStored(KG)
 */
function handleCurrentlyStored(message) {
    // Estrae peso dal messaggio
    // Formato: currentlyStored(50.0)
    const match = message.match(/currentlyStored\s*\(\s*([\d.]+)\s*\)/);

    if (match) {
        currentWeight = parseFloat(match[1]);

        console.log(`📊 Peso aggiornato: ${currentWeight} kg`);

        // Aggiorna UI
        updateWeightDisplay(currentWeight);

    } else {
        console.error("❌ Formato messaggio non valido:", message);
    }
}

// ========================================
// INVIO RICHIESTE
// ========================================

/**
 * Invia richiesta di deposito
 * @param {Event} event - Evento submit del form
 */
function submitStoreRequest(event) {
    event.preventDefault(); // Previene submit normale

    const weightInput = document.getElementById("weightInput");
    const weight = parseInt(weightInput.value);

    // Validazione
    if (!weight || weight < 1 || weight > MAXW) {
        showStoreResponse(`❌ Peso non valido! Inserire un valore tra 1 e ${MAXW} kg`, "error");
        return;
    }

    // Verifica connessione
    if (!socket || socket.readyState !== WebSocket.OPEN) {
        showStoreResponse("❌ Non connesso al server!", "error");
        return;
    }

    // Costruisce e invia messaggio
    // Formato: request/storerequest/storerequest(WEIGHT)
    const message = `request/storerequest/storerequest(${weight})`;
    sendMessage(message);

    // Mostra messaggio di attesa
    showStoreResponse(`⏳ Invio richiesta per ${weight} kg...`, "success");
}

/**
 * Invia ticket
 * @param {Event} event - Evento submit del form
 */
function submitTicketRequest(event) {
    event.preventDefault(); // Previene submit normale

    const ticketInput = document.getElementById("ticketInput");
    const ticket = parseInt(ticketInput.value);

    // Validazione
    if (!ticket || ticket < 1) {
        showTicketResponse("❌ Numero ticket non valido!", "error");
        return;
    }

    // Verifica connessione
    if (!socket || socket.readyState !== WebSocket.OPEN) {
        showTicketResponse("❌ Non connesso al server!", "error");
        return;
    }

    // Costruisce e invia messaggio
    // Formato: request/ticketrequest/ticketrequest(TICKET)
    const message = `request/ticketrequest/ticketrequest(${ticket})`;
    sendMessage(message);

    // Mostra messaggio di attesa
    showTicketResponse(`⏳ Invio ticket ${ticket}...`, "success");
}

/**
 * Richiede aggiornamento stato
 */
function refreshStatus() {
    console.log("🔄 Richiesta aggiornamento stato");
    // L'aggiornamento avviene automaticamente via CoAP Observer
    // Questa funzione è qui per compatibilità con il vecchio app.js
}

// ========================================
// GESTIONE UI
// ========================================

/**
 * Aggiorna display del peso corrente
 * @param {number} weight - Peso corrente in kg
 */
function updateWeightDisplay(weight) {
    const currentWeightEl = document.getElementById("currentWeight");
    currentWeightEl.textContent = `${weight} kg`;

    console.log(`📊 Display aggiornato: ${weight} kg`);
}

/**
 * Mostra messaggio di risposta per store request
 * @param {string} message - Messaggio da mostrare
 * @param {string} type - Tipo: "success" o "error"
 */
function showStoreResponse(message, type) {
    const responseEl = document.getElementById("storeResponse");
    responseEl.textContent = message;
    responseEl.className = `response-message ${type} show`;
}

/**
 * Mostra messaggio di risposta per ticket request
 * @param {string} message - Messaggio da mostrare
 * @param {string} type - Tipo: "success" o "error"
 */
function showTicketResponse(message, type) {
    const responseEl = document.getElementById("ticketResponse");
    responseEl.textContent = message;
    responseEl.className = `response-message ${type} show`;
}

/**
 * Mostra messaggio generico (usato per connessione WebSocket)
 * @param {string} message - Messaggio
 * @param {string} type - Tipo
 */
function showMessage(message, type) {
    console.log(`💬 ${type.toUpperCase()}: ${message}`);
    // Potremmo aggiungere un toast notification se vuoi
}

// ========================================
// GESTIONE TICKET
// ========================================

/**
 * Mostra il ticket ricevuto con timer
 * @param {string} ticket - Numero ticket
 * @param {number} weight - Peso in kg
 */
function showTicket(ticket, weight) {
    // Salva ticket corrente
    currentTicket = ticket;
    ticketExpiryTime = Date.now() + (TICKETTIME * 1000);

    // Aggiorna UI
    document.getElementById("ticketNumber").textContent = ticket;
    document.getElementById("ticketWeight").textContent = `${weight} kg`;
    document.getElementById("ticketDisplay").style.display = "block";

    // Auto-compila campo ticket
    document.getElementById("ticketInput").value = ticket;

    // Avvia timer
    startTicketTimer();

    console.log(`🎫 Ticket mostrato: ${ticket}, scadenza: ${TICKETTIME}s`);
}

/**
 * Nascondi ticket display
 */
function hideTicket() {
    document.getElementById("ticketDisplay").style.display = "none";
    currentTicket = null;
    ticketExpiryTime = null;
    stopTicketTimer();

    console.log("🎫 Ticket nascosto");
}

/**
 * Avvia countdown timer del ticket
 */
function startTicketTimer() {
    // Ferma timer precedente se esiste
    stopTicketTimer();

    timerInterval = setInterval(() => {
        if (!ticketExpiryTime) {
            stopTicketTimer();
            return;
        }

        const remaining = Math.max(0, ticketExpiryTime - Date.now());
        const seconds = Math.ceil(remaining / 1000);

        if (seconds <= 0) {
            // Ticket scaduto
            handleTicketExpired();
        } else {
            // Aggiorna display
            const minutes = Math.floor(seconds / 60);
            const secs = seconds % 60;
            const timerEl = document.getElementById("ticketTimer");
            timerEl.textContent = `${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;

            // Cambia colore se < 5 secondi
            if (seconds <= 5) {
                timerEl.classList.add("warning");
            } else {
                timerEl.classList.remove("warning");
            }
        }
    }, 100); // Aggiorna ogni 100ms per precisione
}

/**
 * Ferma timer
 */
function stopTicketTimer() {
    if (timerInterval) {
        clearInterval(timerInterval);
        timerInterval = null;
    }
}

/**
 * Gestisce scadenza ticket
 */
function handleTicketExpired() {
    console.log(`⏰ Ticket ${currentTicket} SCADUTO!`);

    showStoreResponse(`⏰ Ticket ${currentTicket} è scaduto!`, "error");
    hideTicket();
}

// ========================================
// INIZIALIZZAZIONE
// ========================================

/**
 * Inizializza l'applicazione quando la pagina è caricata
 */
window.addEventListener('load', () => {
    console.log("🚀 Inizializzazione applicazione...");

    // Connetti WebSocket
    connectWebSocket();

    // Imposta peso iniziale
    updateWeightDisplay(0);

    console.log("✅ Applicazione pronta");
});

// ========================================
// UTILITY
// ========================================

/**
 * Debug: stampa stato corrente
 */
function debugState() {
    console.log("=== DEBUG STATE ===");
    console.log("WebSocket:", socket ? socket.readyState : "null");
    console.log("Current Weight:", currentWeight);
    console.log("Current Ticket:", currentTicket);
    console.log("Ticket Expiry:", ticketExpiryTime ? new Date(ticketExpiryTime) : "null");
    console.log("==================");
}

// Esponi funzione debug globalmente per console
window.debugState = debugState;
