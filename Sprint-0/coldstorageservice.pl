%====================================================================================
% coldstorageservice description   
%====================================================================================
request( engage, engage(OWNER,STEPTIME) ). %richiesta ingaggio
dispatch( disengage, disengage(ARG) ).
request( storerequest, storerequest(KG) ). %richiesta deposito KG
request( ticketrequest, ticketrequest(TICKET) ). %richiesta invio ticket
%====================================================================================
context(ctxcoldstorageservice, "localhost",  "TCP", "8080").
context(ctxbasicrobot, "127.0.0.1",  "TCP", "8020").
 qactor( basicrobot, ctxbasicrobot, "external").
  qactor( serviceaccessgui, ctxcoldstorageservice, "it.unibo.serviceaccessgui.Serviceaccessgui").
 static(serviceaccessgui).
  qactor( coldstorageservice, ctxcoldstorageservice, "it.unibo.coldstorageservice.Coldstorageservice").
 static(coldstorageservice).
  qactor( trolley, ctxcoldstorageservice, "it.unibo.trolley.Trolley").
 static(trolley).
