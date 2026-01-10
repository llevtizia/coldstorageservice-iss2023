package it.unibo.serviceaccessgui

import org.springframework.web.bind.annotation.*
import unibo.basicomm23.interfaces.IApplMessage
import unibo.basicomm23.interfaces.Interaction
import unibo.basicomm23.msg.ProtocolType
import unibo.basicomm23.utils.CommUtils
import unibo.basicomm23.utils.ConnectionFactory
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy

data class StoreRequest(val weight: Int)
data class TicketRequest(val ticketNumber: String)

data class StoreResponse(
    val success: Boolean,
    val ticket: String? = null,
    val weight: Int? = null,
    val message: String
)

data class TicketResponse(
    val success: Boolean,
    val message: String
)

data class StatusResponse(
    val currentWeight: Float
)

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = ["*"]) // Permetti richieste da qualsiasi origine
class ColdStorageController {
    
    private var conn: Interaction? = null
    private val COLDSTORAGE_HOST = "localhost"
    private val COLDSTORAGE_PORT = "8015"
    
    @PostConstruct
    fun init() {
        try {
            println("🔌 Connessione a ColdStorageService su $COLDSTORAGE_HOST:$COLDSTORAGE_PORT...")
            conn = ConnectionFactory.createClientSupport(
                ProtocolType.tcp,
                COLDSTORAGE_HOST,
                COLDSTORAGE_PORT
            )
            println("✅ Connesso a ColdStorageService!")
        } catch (e: Exception) {
            println("❌ Errore connessione: ${e.message}")
            e.printStackTrace()
        }
    }
    
    @PreDestroy
    fun cleanup() {
        println("🔌 Chiusura connessione...")
        conn?.close()
    }
    
    /**
     * Endpoint per ottenere lo stato corrente del sistema
     */
    @GetMapping("/status")
    fun getStatus(): StatusResponse {
        // TODO: implementare la lettura dello stato reale
        // Per ora ritorniamo un valore mock
        return StatusResponse(currentWeight = 0f)
    }
    
    /**
     * Endpoint per inviare una richiesta di deposito
     * POST /api/store
     * Body: { "weight": 50 }
     */
    @PostMapping("/store")
    fun storeRequest(@RequestBody request: StoreRequest): StoreResponse {
        return try {
            println("📦 Richiesta deposito: ${request.weight} kg")
            
            // Crea il messaggio storerequest
            val msg = CommUtils.buildRequest(
                "gui",
                "storerequest",
                "storerequest(${request.weight})",
                "coldstorageservice"
            )
            
            // Invia e attendi risposta
            val reply = conn?.request(msg)
            
            if (reply == null) {
                return StoreResponse(
                    success = false,
                    message = "Errore di comunicazione con il server"
                )
            }
            
            println("📨 Risposta: ${reply.msgContent()}")
            
            // Parse della risposta
            when (reply.msgId()) {
                "storeaccepted" -> {
                    val ticket = extractTicket(reply)
                    val weight = extractWeight(reply)
                    StoreResponse(
                        success = true,
                        ticket = ticket,
                        weight = weight,
                        message = "Richiesta accettata! Ticket: $ticket"
                    )
                }
                "storerefused" -> {
                    StoreResponse(
                        success = false,
                        message = "Richiesta rifiutata: spazio insufficiente"
                    )
                }
                else -> {
                    StoreResponse(
                        success = false,
                        message = "Risposta inattesa: ${reply.msgId()}"
                    )
                }
            }
            
        } catch (e: Exception) {
            println("❌ Errore: ${e.message}")
            e.printStackTrace()
            StoreResponse(
                success = false,
                message = "Errore: ${e.message}"
            )
        }
    }
    
    /**
     * Endpoint per inviare un ticket
     * POST /api/ticket
     * Body: { "ticketNumber": "1" }
     */
    @PostMapping("/ticket")
    fun ticketRequest(@RequestBody request: TicketRequest): TicketResponse {
        return try {
            println("🎫 Invio ticket: ${request.ticketNumber}")
            
            // Crea il messaggio ticketrequest
            val msg = CommUtils.buildRequest(
                "gui",
                "ticketrequest",
                "ticketrequest(${request.ticketNumber})",
                "coldstorageservice"
            )
            
            // Invia e attendi risposta
            val reply = conn?.request(msg)
            
            if (reply == null) {
                return TicketResponse(
                    success = false,
                    message = "Errore di comunicazione con il server"
                )
            }
            
            println("📨 Risposta: ${reply.msgContent()}")
            
            // Parse della risposta
            when (reply.msgId()) {
                "chargetaken" -> {
                    TicketResponse(
                        success = true,
                        message = "Carico preso in consegna! Il truck può lasciare l'INDOOR."
                    )
                }
                "ticketrefused" -> {
                    TicketResponse(
                        success = false,
                        message = "Ticket rifiutato: ticket scaduto o non valido"
                    )
                }
                else -> {
                    TicketResponse(
                        success = false,
                        message = "Risposta inattesa: ${reply.msgId()}"
                    )
                }
            }
            
        } catch (e: Exception) {
            println("❌ Errore: ${e.message}")
            e.printStackTrace()
            TicketResponse(
                success = false,
                message = "Errore: ${e.message}"
            )
        }
    }
    
    /**
     * Estrae il ticket da una risposta storeaccepted
     * Formato: storeaccepted(TICKET, KG)
     */
    private fun extractTicket(reply: IApplMessage): String {
        val content = reply.msgContent()
        val start = content.indexOf('(') + 1
        val comma = content.indexOf(',')
        return content.substring(start, comma).trim()
    }
    
    /**
     * Estrae il peso da una risposta storeaccepted
     * Formato: storeaccepted(TICKET, KG)
     */
    private fun extractWeight(reply: IApplMessage): Int {
        val content = reply.msgContent()
        val comma = content.indexOf(',')
        val end = content.indexOf(')')
        return content.substring(comma + 1, end).trim().toInt()
    }
}
