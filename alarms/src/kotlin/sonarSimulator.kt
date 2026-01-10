import it.unibo.kactor.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import unibo.basicomm23.interfaces.IApplMessage
import unibo.basicomm23.utils.CommUtils

/*
-------------------------------------------------------------------------------------------------
sonarSimulator.kt
-------------------------------------------------------------------------------------------------
 */

class sonarSimulator ( name : String ) : ActorBasic( name ) {
	var working 			= false		// stato controllo 
	var v0      			= 30		// valore corrente
	var descending 			= true		// direzione: true = scendendo, false = salendo
	
	// per ostacoli
	val DLIMIT				= 25		// distanza limite per ostacolo
	var obstacleDetected	= false		// stato dell'ostacolo
	
	/*
  	private suspend fun updateCoapResource(state: String) {
		forward("updatestate", "updatestate($state)", "sonarcoap")
	}
	 */
   
	
 
	init{
		//autostart
		runBlocking{  autoMsg("simulatorstart","do") } 
	}
	//@kotlinx.coroutines.ObsoleteCoroutinesApi

    override suspend fun actorBody(msg : IApplMessage){
  		CommUtils.outblue("$tt $name | received  $msg "  )
		
  		if( msg.msgId() == "sonarstart") {
			println("$tt $name | SONARSTART RECEIVED!")
			startSimulation(   )
		}
		
  		if( msg.msgId() == "sonarstop")  stopSimulation(   )
		
		//per settare manualmente la distanza
	    if(msg.msgId() == "setdistance") {
	        val newDistance = msg.msgContent() // prende il contenuto del messaggio -> setdistance(15)
				.substringAfter("(")			// prende la parte dopo la parentesi aperta -> 15)
				.substringBefore(")")			// prende la parte prima della parentesi chiusa -> 15
				.toIntOrNull() ?: v0			// converte la stringa in un numero intero (o nullo, nel caso usa v0)
	        
	        CommUtils.outblue("$tt $name | Setting distance to $newDistance")
	        v0 = newDistance
	        
	        // Genera immediatamente l'evento con la nuova distanza
	        if ( working ) {
	            autoMsg("simulatorstart", "do")
	        }
	    }
		
		
		if( msg.msgId() == "simulatorstart" && working) startDataReadSimulation(   )
     }
  	 
    //@kotlinx.coroutines.ObsoleteCoroutinesApi

    suspend fun stopSimulation(    ){
    	CommUtils.outblue("$tt $name | stopSimulation "  )
    	working = false
		
		// aggiorna la risorsa quando si ferma
		//updateCoapResource("stopped")
    }
	
	suspend fun startSimulation(    ){
		CommUtils.outblue("$tt $name | startSimulation "  )
		working = true
		
		// aggiorna la risorsa quando si avvia
		//updateCoapResource("started")
		startDataReadSimulation(    )
	}
   
	suspend fun startDataReadSimulation(    ){

		
		// controllo se c'è un ostacolo (distanza < DLIMIT) --> val = valori attuali del ciclo
		val currentDistance = v0
		val previousObstacleState = obstacleDetected
		
		//println("$tt $name | DEBUG: distance = $currentDistance, DLIMIT = $DLIMIT, previousObstacle = $previousObstacleState")
		
		if ( currentDistance < DLIMIT ) {
			obstacleDetected = true
		} else {
			obstacleDetected = false
		}
		
		//println("$tt $name | DEBUG: new obstacleDetected = $obstacleDetected") 
		
		// invia evento quando cambia stato
		if ( obstacleDetected  && !previousObstacleState ) {
			
			//transizione libero -> ostacolo
			println("$tt $name | DEBUG: SENDING OBSTACLE EVENT!")
			val obstacle = CommUtils.buildEvent( name, "obstacle", "obstacle( detected )" )
			emitLocalStreamEvent(obstacle)
			CommUtils.outred("$tt $name | OBSTACLE DETECTED! Distance: $currentDistance ")
			
			// aggiorno risorsa
			//updateCoapResource("obstacle,$currentDistance")
		}
		
		else if ( !obstacleDetected && previousObstacleState ) {
			
			// transizione: ostacolo -> libero
			println("$tt $name | DEBUG: SENDING FREE EVENT!")
			val free = CommUtils.buildEvent( name, "free", "free( clear )" )
			emitLocalStreamEvent(free)
			CommUtils.outgreen("$tt $name | AREA FREE! Distance: $currentDistance ")
			
			// aggiorno risorsa
			//updateCoapResource("free,$currentDistance")
		}
		
		//updateCoapResource("distance,$currentDistance")
		// invia l'evento
 		val event = CommUtils.buildEvent( name, "distance", "distance( $currentDistance )" )
		emitLocalStreamEvent( event )
		
		 println("$tt $name | generates $event working = $working, next = $v0, direction = ${if ( descending ) "desc" else "asc"} ")
		
		
		// aggiorna il valore per il prossimo giro
		if ( descending ) {
			v0 = v0 - 5
			
			if ( v0 <= 0 ){
				v0 = 0
				descending = false	// cambia direzione
			}
		}
		
		else {
			v0 = v0 + 5
			if ( v0 >= 80 ) {
				v0 = 80
				descending = true
			}
		}

		// prosisma esecuzione
		delay( 500 )
		if( working)
 				runBlocking{ autoMsg("simulatorstart","do") }
  	}			
} 

 
//@kotlinx.coroutines.ObsoleteCoroutinesApi
//
//fun main() = runBlocking{
// //	val startMsg = MsgUtil.buildDispatch("main","start","start","datasimulator")
//	val consumer  = dataConsumer("dataconsumer")
//	val simulator = sonarSimulator( "datasimulator" )
//	val filter    = dataFilter("datafilter", consumer)
//	val logger    = dataLogger("logger")
//	simulator.subscribe( logger ).subscribe( filter ).subscribe( consumer ) 
//	MsgUtil.sendMsg("start","start",simulator)
//	simulator.waitTermination()
// } 
