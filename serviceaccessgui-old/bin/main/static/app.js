// Configurazione
const API_BASE_URL = 'http://localhost:8080/api';
const TICKET_TIME = 15; // secondi (TICKETTIME)

// Variabili globali
let currentTicket = null;
let ticketTimer = null;
let ticketExpirationTime = null;

/**
 * Inizializzazione al caricamento della pagina
 */
document.addEventListener('DOMContentLoaded', () => {
    console.log('🚀 ServiceAccessGUI inizializzata');
    refreshStatus();
    
    // Auto-refresh dello stato ogni 5 secondi
    setInterval(refreshStatus, 5000);
});

/**
 * Aggiorna lo stato del sistema
 */
async function refreshStatus() {
    try {
        const response = await fetch(`${API_BASE_URL}/status`);
        const data = await response.json();
        
        document.getElementById('currentWeight').textContent = `${data.currentWeight} kg`;
        
    } catch (error) {
        console.error('❌ Errore nel caricamento dello stato:', error);
        document.getElementById('currentWeight').textContent = 'Errore';
    }
}

/**
 * Invia richiesta di deposito
 */
async function submitStoreRequest(event) {
    event.preventDefault();
    
    const weightInput = document.getElementById('weightInput');
    const weight = parseInt(weightInput.value);
    
    if (!weight || weight < 1 || weight > 200) {
        showMessage('storeResponse', 'Peso non valido. Inserisci un valore tra 1 e 200 kg.', 'error');
        return;
    }
    
    try {
        // Disabilita il pulsante durante la richiesta
        const submitBtn = event.target.querySelector('button[type="submit"]');
        submitBtn.disabled = true;
        submitBtn.textContent = 'Invio in corso...';
        
        console.log(`📦 Invio richiesta deposito: ${weight} kg`);
        
        const response = await fetch(`${API_BASE_URL}/store`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ weight: weight })
        });
        
        const data = await response.json();
        
        // Riabilita il pulsante
        submitBtn.disabled = false;
        submitBtn.textContent = 'Invia Richiesta';
        
        if (data.success) {
            // Richiesta accettata
            console.log(`✅ Richiesta accettata! Ticket: ${data.ticket}`);
            showMessage('storeResponse', data.message, 'success');
            
            // Mostra il ticket
            displayTicket(data.ticket, data.weight);
            
            // Pulisci il form
            weightInput.value = '';
            
            // Auto-fill del campo ticket
            document.getElementById('ticketInput').value = data.ticket;
            
        } else {
            // Richiesta rifiutata
            console.log(`❌ Richiesta rifiutata: ${data.message}`);
            showMessage('storeResponse', data.message, 'error');
            hideTicket();
        }
        
    } catch (error) {
        console.error('❌ Errore nella richiesta:', error);
        showMessage('storeResponse', `Errore di comunicazione: ${error.message}`, 'error');
        
        // Riabilita il pulsante in caso di errore
        const submitBtn = event.target.querySelector('button[type="submit"]');
        submitBtn.disabled = false;
        submitBtn.textContent = 'Invia Richiesta';
    }
}

/**
 * Invia ticket
 */
async function submitTicketRequest(event) {
    event.preventDefault();
    
    const ticketInput = document.getElementById('ticketInput');
    const ticketNumber = ticketInput.value.trim();
    
    if (!ticketNumber) {
        showMessage('ticketResponse', 'Inserisci un numero di ticket valido.', 'error');
        return;
    }
    
    try {
        // Disabilita il pulsante durante la richiesta
        const submitBtn = event.target.querySelector('button[type="submit"]');
        submitBtn.disabled = true;
        submitBtn.textContent = 'Invio in corso...';
        
        console.log(`🎫 Invio ticket: ${ticketNumber}`);
        
        const response = await fetch(`${API_BASE_URL}/ticket`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ ticketNumber: ticketNumber })
        });
        
        const data = await response.json();
        
        // Riabilita il pulsante
        submitBtn.disabled = false;
        submitBtn.textContent = 'Invia Ticket';
        
        if (data.success) {
            // Ticket accettato
            console.log(`✅ Ticket accettato: ${data.message}`);
            showMessage('ticketResponse', data.message, 'success');
            
            // Pulisci il form
            ticketInput.value = '';
            
            // Nascondi il ticket display
            hideTicket();
            
            // Aggiorna lo stato
            refreshStatus();
            
        } else {
            // Ticket rifiutato
            console.log(`❌ Ticket rifiutato: ${data.message}`);
            showMessage('ticketResponse', data.message, 'error');
        }
        
    } catch (error) {
        console.error('❌ Errore nell\'invio del ticket:', error);
        showMessage('ticketResponse', `Errore di comunicazione: ${error.message}`, 'error');
        
        // Riabilita il pulsante in caso di errore
        const submitBtn = event.target.querySelector('button[type="submit"]');
        submitBtn.disabled = false;
        submitBtn.textContent = 'Invia Ticket';
    }
}

/**
 * Mostra un messaggio di feedback
 */
function showMessage(elementId, message, type) {
    const element = document.getElementById(elementId);
    element.textContent = message;
    element.className = `response-message show ${type}`;
    
    // Nascondi dopo 5 secondi
    setTimeout(() => {
        element.classList.remove('show');
    }, 5000);
}

/**
 * Mostra il ticket ricevuto e avvia il timer
 */
function displayTicket(ticket, weight) {
    currentTicket = ticket;
    
    // Mostra il display del ticket
    const ticketDisplay = document.getElementById('ticketDisplay');
    ticketDisplay.style.display = 'block';
    
    // Imposta i valori
    document.getElementById('ticketNumber').textContent = ticket;
    document.getElementById('ticketWeight').textContent = `${weight} kg`;
    
    // Avvia il timer di scadenza
    ticketExpirationTime = Date.now() + (TICKET_TIME * 1000);
    startTicketTimer();
}

/**
 * Nasconde il display del ticket
 */
function hideTicket() {
    document.getElementById('ticketDisplay').style.display = 'none';
    stopTicketTimer();
    currentTicket = null;
}

/**
 * Avvia il timer di scadenza del ticket
 */
function startTicketTimer() {
    stopTicketTimer(); // Ferma eventuali timer precedenti
    
    ticketTimer = setInterval(() => {
        const now = Date.now();
        const remaining = Math.max(0, ticketExpirationTime - now);
        
        if (remaining === 0) {
            // Ticket scaduto
            stopTicketTimer();
            document.getElementById('ticketTimer').textContent = 'SCADUTO';
            document.getElementById('ticketTimer').classList.add('warning');
            showMessage('storeResponse', 'Il ticket è scaduto!', 'error');
            return;
        }
        
        // Calcola minuti e secondi rimanenti
        const totalSeconds = Math.floor(remaining / 1000);
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = totalSeconds % 60;
        
        // Formatta il tempo
        const formattedTime = `${minutes}:${seconds.toString().padStart(2, '0')}`;
        const timerElement = document.getElementById('ticketTimer');
        timerElement.textContent = formattedTime;
        
        // Aggiungi warning se mancano meno di 5 secondi
        if (totalSeconds <= 5) {
            timerElement.classList.add('warning');
        } else {
            timerElement.classList.remove('warning');
        }
        
    }, 100); // Aggiorna ogni 100ms per una visualizzazione fluida
}

/**
 * Ferma il timer di scadenza del ticket
 */
function stopTicketTimer() {
    if (ticketTimer) {
        clearInterval(ticketTimer);
        ticketTimer = null;
    }
}

/**
 * Utility: formatta un numero con zeri iniziali
 */
function padZero(num, size = 2) {
    let s = num + "";
    while (s.length < size) s = "0" + s;
    return s;
}
