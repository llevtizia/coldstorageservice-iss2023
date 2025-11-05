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
	var v0      			= 40		// valore corrente
	var descending 			= true		// direzione: true = scendendo, false = salendo
	
	// per ostacoli
	val DLIMIT				= 25		// distanza limite per ostacolo
	var obstacleDetected	= false		// stato dell'ostacolo

	init{
		//autostart
		runBlocking{  autoMsg("simulatorstart","do") } 
	}
	//@kotlinx.coroutines.ObsoleteCoroutinesApi

    override suspend fun actorBody(msg : IApplMessage){
  		CommUtils.outblue("$tt $name | received  $msg "  )
  		if( msg.msgId() == "sonarstart") startSimulation(   )
  		if( msg.msgId() == "sonarstop")  stopSimulation(   )
		if( msg.msgId() == "simulatorstart" && working) startDataReadSimulation(   )
     }
  	 
    //@kotlinx.coroutines.ObsoleteCoroutinesApi

    suspend fun stopSimulation(    ){
    	CommUtils.outblue("$tt $name | stopiSimulation "  )
    	working = false
    }
	
	suspend fun startSimulation(    ){
		CommUtils.outblue("$tt $name | startSimulation "  )
		working = true
		startDataReadSimulation(    )
	}
   
	suspend fun startDataReadSimulation(    ){

		// genera il valore corrente
		val m1 = "distance( ${v0} )"
		
		// controllo se c'è un ostacolo (distanza < DLIMIT) --> val = valori attuali del ciclo
		val currentDistance = v0
		val previousObstacleState = obstacleDetected
		
		if ( currentDistance < DLIMIT ) {
			obstacleDetected = true
		} else {
			obstacleDetected = false
		}
		
		// invia evento quando cambia stato
		if ( obstacleDetected  && !previousObstacleState ) {
			
			//transizione libero -> ostacolo
			val obstacle = CommUtils.buildEvent( name, "obstacle", "obstacle( detected )" )
			emitLocalStreamEvent(obstacle)
			CommUtils.outred("$tt $name | OBSTACLE DETECTED! Distance: $currentDistance ")
		}
		
		else if ( !obstacleDetected && previousObstacleState ) {
			
			// transizione: ostacolo -> libero
			val free = CommUtils.buildEvent( name, "free", "free( clear )" )
			emitLocalStreamEvent(free)
			CommUtils.outgreen("$tt $name | AREA FREE! Distance: $currentDistance ")
		}
		
		
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

		// invia l'evento
 		val event = CommUtils.buildEvent( name, "distance", m1 )
		emitLocalStreamEvent( event )
		
		println("$tt $name | generates $event working = $working, next = $v0, direction = ${if ( descending ) "desc" else "asc"} ")

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
