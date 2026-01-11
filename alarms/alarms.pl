%====================================================================================
% alarms description   
%====================================================================================
event( alarm, alarm(X) ).
dispatch( stoptrolley, stoptrolley(X) ).
dispatch( resumetrolley, resumetrolley(X) ).
%====================================================================================
context(ctxcoldstorageservice, "127.0.0.1",  "TCP", "8015").
context(ctxalarms, "localhost",  "TCP", "8025").
 qactor( coldstorageservice, ctxcoldstorageservice, "external").
  qactor( alarmdevice, ctxalarms, "it.unibo.alarmdevice.Alarmdevice").
 static(alarmdevice).
  qactor( warningdevice, ctxalarms, "it.unibo.warningdevice.Warningdevice").
 static(warningdevice).
