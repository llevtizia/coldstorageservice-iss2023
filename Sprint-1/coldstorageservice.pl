%====================================================================================
% coldstorageservice description   
%====================================================================================
request( engage, engage(OWNER,STEPTIME) ). %richiesta ingaggio
reply( engagedone, engagedone(ARG) ).  %%for engage
reply( engagerefused, engagerefused(ARG) ).  %%for engage
dispatch( disengage, disengage(ARG) ).
request( moverobot, moverobot(TARGETX,TARGETY) ).
dispatch( setrobotstate, setpos(X,Y,D) ).
dispatch( setdirection, dir(D) ).
request( storerequest, storerequest(X) ). %richiesta deposito KG
request( ticketrequest, ticketrequest(X) ). %richiesta invio ticket
request( gotakecharge, gotakecharge(X) ).
%====================================================================================
context(ctxcoldstorageservice, "localhost",  "TCP", "8015").
context(ctxbasicrobot, "127.0.0.1",  "TCP", "8020").
 qactor( basicrobot, ctxbasicrobot, "external").
  qactor( serviceaccessgui, ctxcoldstorageservice, "it.unibo.serviceaccessgui.Serviceaccessgui").
 static(serviceaccessgui).
  qactor( coldstorageservice, ctxcoldstorageservice, "it.unibo.coldstorageservice.Coldstorageservice").
 static(coldstorageservice).
  qactor( trolley, ctxcoldstorageservice, "it.unibo.trolley.Trolley").
 static(trolley).
