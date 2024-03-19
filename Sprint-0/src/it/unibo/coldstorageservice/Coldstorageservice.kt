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
				var Temp_load = 0
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
					 transition(edgeName="t04",targetState="handlestore",cond=whenRequest("storerequest"))
					transition(edgeName="t05",targetState="handleticket",cond=whenRequest("ticketrequest"))
				}	 
				state("handlestore") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("storerequest(KG)"), Term.createTerm("storerequest(KG)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								if(  payloadArg(0) < MAXW - Temp_load  
								 ){CommUtils.outgreen("$name accepting load of ${payloadArg(0) kg")
								CommUtils.outgreen("$name generating ticket n. $TicketNumber")
								answer("storerequest", "storeaccepted", "storeaccepted($TicketNumber)","serviceaccessgui"   )  
								}
								else
								 {CommUtils.outgreen("$name refusing load of ${payloadArg(0) kg")
								 answer("storerequest", "storerefused", "storerefused(payloadArg(0))","serviceaccessgui"   )  
								 }
						}
						//genTimer( actor, state )
					}
					//After Lenzi Aug2002
					sysaction { //it:State
					}	 	 
					 transition( edgeName="goto",targetState="waitrequest", cond=doswitch() )
				}	 
				state("handleticket") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("ticketrequest(TICKET)"), Term.createTerm("ticketrequest(TICKET)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 var Time = 10   
								if(  Time < TICKETTIME  
								 ){CommUtils.outgreen("$name accepting ticket n.${payloadArg(0)")
								answer("ticketrequest", "chargetaken", "chargetaken(payloadArg(0))","serviceaccessgui"   )  
								}
								else
								 {CommUtils.outgreen("$name refusing ticket n.${payloadArg(0)")
								 answer("ticketrequest", "chargerefused", "chargerefused(payloadArg(0))","serviceaccessgui"   )  
								 }
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
