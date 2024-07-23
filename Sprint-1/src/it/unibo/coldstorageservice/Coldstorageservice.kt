/* Generated by AN DISI Unibo */ 
package it.unibo.coldstorageservice

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

class Coldstorageservice ( name: String, scope: CoroutineScope, isconfined: Boolean=false  ) : ActorBasicFsm( name, scope, confined=isconfined ){

	override fun getInitialState() : String{
		return "s0"
	}
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		//val interruptedStateTransitions = mutableListOf<Transition>()
		
				var MAXW = 1000			
				var TICKETTIME = 15
				var Current_load = 0
				var TicketNumber = 1	
		return { //this:ActionBasciFsm
				state("s0") { //this:State
					action { //it:State
						CommUtils.outcyan("$name in ${currentState.stateName} | $currentMsg | ${Thread.currentThread().getName()} n=${Thread.activeCount()}")
						 	   
						CommUtils.outgreen("$name START ")
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition( edgeName="goto",targetState="waitrequest", cond=doswitch() )
				}	 
				state("waitrequest") { //this:State
					action { //it:State
						CommUtils.outgreen("$name waiting for requests... ")
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition(edgeName="t02",targetState="handlestore",cond=whenRequest("storerequest"))
				}	 
				state("handlestore") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("storerequest(X)"), Term.createTerm("storerequest(X)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 
												var CounterRequest = payloadArg(0).toInt()
								CommUtils.outgreen("$name received request n. $CounterRequest ")
								answer("storerequest", "storeaccepted", "storeaccepted($CounterRequest)"   )  
								request("gotakecharge", "gotakecharge($CounterRequest)" ,"trolley" )  
						}
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition( edgeName="goto",targetState="waitrequest", cond=doswitch() )
				}	 
			}
		}
} 
