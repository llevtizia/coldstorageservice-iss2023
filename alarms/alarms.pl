%====================================================================================
% alarms description   
%====================================================================================
event( distance, distance(D) ).
event( obstacle, obstacle(X) ).
event( free, free(X) ).
dispatch( stoptrolley, stoptrolley(X) ).
dispatch( resumetrolley, resumetrolley(X) ).
dispatch( sonarstart, sonarstart(X) ).
dispatch( sonarstop, sonarstop(X) ).
dispatch( setdistance, setdistance(D) ).
dispatch( ledoff, ledoff(X) ).
dispatch( ledon, ledon(X) ).
dispatch( ledblink, ledblink(X) ).
dispatch( statustrolley, statustrolley(X,Y) ).
%====================================================================================
context(ctxcoldstorageservice, "127.0.0.1",  "TCP", "8015").
context(ctxalarms, "localhost",  "TCP", "8025").
 qactor( coldstorageservice, ctxcoldstorageservice, "external").
  qactor( trolley, ctxcoldstorageservice, "external").
  qactor( sonar, ctxalarms, "sonarSimulator").
 static(sonar).
  qactor( led, ctxalarms, "ledSimulator").
 static(led).
  qactor( alarmdevice, ctxalarms, "it.unibo.alarmdevice.Alarmdevice").
 static(alarmdevice).
  qactor( warningdevice, ctxalarms, "it.unibo.warningdevice.Warningdevice").
 static(warningdevice).
