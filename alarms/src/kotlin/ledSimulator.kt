import it.unibo.kactor.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import unibo.basicomm23.interfaces.IApplMessage
import unibo.basicomm23.utils.CommUtils

/*
-------------------------------------------------------------------------------------------------
ledSimulator.kt
stato del led -> in base allo stato del trolley:
- OFF: trolley in HOME
- BLINK: trolley in movimento  
- ON: trolley fermo (stopped)
-------------------------------------------------------------------------------------------------
*/

class ledSimulator(name: String) : ActorBasic(name) {
    
    var currentState = "OFF"
    var blinking = false
    
    init {
        runBlocking { 
            println("$tt $name | LED Simulator INITIALIZED")
            updateResourceRep("ledstate(OFF)")
        }
    }
    
    override suspend fun actorBody(msg: IApplMessage) {
        CommUtils.outblue("$tt $name | received $msg")
        
        when (msg.msgId()) {
            "ledoff" -> {
                println("$tt $name | LED OFF (trolley at HOME)")
                blinking = false
                currentState = "OFF"
                updateResourceRep("ledstate(OFF)")
            }
            
            "ledon" -> {
                println("$tt $name | LED ON (trolley stopped)")
                blinking = false
                currentState = "ON"
                updateResourceRep("ledstate(ON)")
            }
            
            "ledblink" -> {
                println("$tt $name | LED BLINKS (trolley moving)")
                
                if (!blinking) {
                    blinking = true
                    currentState = "BLINK"
                    
                    // avvia ciclo di blinking con auto-messaggio
                    autoMsg("doblink", "doblink(start)")
                }
            }
            
            "doblink" -> {
                if (blinking) {
                    // alterna tra ON e OFF
                    if (currentState == "BLINK_ON") {
                        println("$tt $name | LED OFF (blink)")
                        currentState = "BLINK_OFF"
                        updateResourceRep("ledstate(BLINK_OFF)")
                    } else {
                        println("$tt $name | LED ON (blink)")
                        currentState = "BLINK_ON"
                        updateResourceRep("ledstate(BLINK_ON)")
                    }
                    
                    delay(500)
                    
                    // continua a blinkare
                    if (blinking) {
                        autoMsg("doblink", "doblink(continue)")
                    }
                }
            }
        }
    }
}