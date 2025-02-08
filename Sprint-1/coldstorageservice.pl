%====================================================================================
% coldstorageservice description   
%====================================================================================
request( engage, engage(OWNER,STEPTIME) ).
reply( engagedone, engagedone(ARG) ).  %%for engage
reply( engagerefused, engagerefused(ARG) ).  %%for engage
dispatch( disengage, disengage(ARG) ).
request( moverobot, moverobot(TARGETX,TARGETY) ).
reply( moverobotdone, moverobotok(ARG) ).  %%for moverobot
reply( moverobotfailed, moverobotfailed(PLANDONE,PLANTODO) ).  %%for moverobot
dispatch( setrobotstate, setpos(X,Y,D) ).
dispatch( setdirection, dir(D) ).
request( storerequest, storerequest(KG) ). %richiesta deposito KG
reply( storeaccepted, storeaccepted(TICKET,KG) ).  %%for storerequest
reply( storerefused, storerefused(X) ).  %%for storerequest
request( ticketrequest, ticketrequest(TICKET) ). %richiesta invio ticket
reply( chargetaken, chargetaken(TICKET) ).  %%for ticketrequest
reply( ticketrefused, ticketrefused(TICKET) ).  %%for ticketrequest
dispatch( gotakecharge, gotakecharge(X,Y) ).
dispatch( statustrolley, statustrolley(X,Y) ).
%====================================================================================
context(ctxcoldstorageservice, "localhost",  "TCP", "8015").
context(ctxbasicrobot, "127.0.0.1",  "TCP", "8020").
 qactor( basicrobot, ctxbasicrobot, "external").
  qactor( coldstorageservice, ctxcoldstorageservice, "it.unibo.coldstorageservice.Coldstorageservice").
 static(coldstorageservice).
  qactor( trolley, ctxcoldstorageservice, "it.unibo.trolley.Trolley").
 static(trolley).
