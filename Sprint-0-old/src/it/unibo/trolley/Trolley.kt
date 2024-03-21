/* Generated by AN DISI Unibo */ 
package it.unibo.trolley

import it.unibo.kactor.*
import alice.tuprolog.*
import unibo.basicomm23.*
import unibo.basicomm23.interfaces.*
import unibo.basicomm23.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import it.unibo.kactor.sysUtil.createActor   //Sept2023

//User imports JAN2024

class Trolley ( name: String, scope: CoroutineScope, isconfined: Boolean=false  ) : ActorBasicFsm( name, scope, confined=isconfined ){

	override fun getInitialState() : String{
		return "s0"
	}
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		//val interruptedStateTransitions = mutableListOf<Transition>()
		return { //this:ActionBasciFsm
				state("s0") { //this:State
					action { //it:State
						CommUtils.outcyan("$name in ${currentState.stateName} | $currentMsg | ${Thread.currentThread().getName()} n=${Thread.activeCount()}")
						 	   
						CommUtils.outmagenta("$name START ")
						CommUtils.outmagenta("$name engage BASIC ROBOT ")
						request("engage", "engage(trolley,300)" ,"basicrobot" )  
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition(edgeName="t06",targetState="waitrequest",cond=whenReply("engagedone"))
				}	 
				state("waitrequest") { //this:State
					action { //it:State
						CommUtils.outmagenta("$name waiting for requests...")
						 CommUtils.waitTheUser("$name HIT to send takeCharge command to the trolley ")  
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition( edgeName="goto",targetState="gotoindoor", cond=doswitch() )
				}	 
				state("gotoindoor") { //this:State
					action { //it:State
						CommUtils.outmagenta("$name moving to INDOOR")
						delay(2000) 
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition( edgeName="goto",targetState="takeload", cond=doswitch() )
				}	 
				state("takeload") { //this:State
					action { //it:State
						CommUtils.outmagenta("$name taking the load...")
						 CommUtils.waitTheUser("$name HIT to terminate load")  
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition( edgeName="goto",targetState="gotocoldroom", cond=doswitch() )
				}	 
				state("gotocoldroom") { //this:State
					action { //it:State
						CommUtils.outmagenta("$name moving to COLDROOM")
						delay(2000) 
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition( edgeName="goto",targetState="storeload", cond=doswitch() )
				}	 
				state("storeload") { //this:State
					action { //it:State
						CommUtils.outmagenta("$name storing the load...")
						 CommUtils.waitTheUser("$name HIT to terminate.")  
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
				 	 		stateTimer = TimerActor("timer_storeload", 
				 	 					  scope, context!!, "local_tout_"+name+"_storeload", 2000.toLong() )  //OCT2023
					}	 	 
					 transition(edgeName="t07",targetState="gohome",cond=whenTimeout("local_tout_"+name+"_storeload"))   
				}	 
				state("gohome") { //this:State
					action { //it:State
						CommUtils.outmagenta("$name going HOME...")
						delay(2000) 
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition( edgeName="goto",targetState="trolleyathome", cond=doswitch() )
				}	 
				state("trolleyathome") { //this:State
					action { //it:State
						CommUtils.outmagenta("$name trolley at home")
						forward("disengage", "disengage(trolley)" ,"basicrobot" ) 
						CommUtils.outmagenta("$name  disengaged")
						delay(1000) 
						 System.exit(0)  
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
				}	 
			}
		}
} 