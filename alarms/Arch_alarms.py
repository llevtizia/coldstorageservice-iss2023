### conda install diagrams
from diagrams import Cluster, Diagram, Edge
from diagrams.custom import Custom
import os
os.environ['PATH'] += os.pathsep + 'C:/Program Files/Graphviz/bin/'

graphattr = {     #https://www.graphviz.org/doc/info/attrs.html
    'fontsize': '22',
}

nodeattr = {   
    'fontsize': '22',
    'bgcolor': 'lightyellow'
}

eventedgeattr = {
    'color': 'red',
    'style': 'dotted'
}
evattr = {
    'color': 'darkgreen',
    'style': 'dotted'
}
with Diagram('alarmsArch', show=False, outformat='png', graph_attr=graphattr) as diag:
  with Cluster('env'):
     sys = Custom('','./qakicons/system.png')
### see https://renenyffenegger.ch/notes/tools/Graphviz/attributes/label/HTML-like/index
     with Cluster('ctxcoldstorageservice', graph_attr=nodeattr):
          coldstorageservice=Custom('coldstorageservice(ext)','./qakicons/externalQActor.png')
          trolley=Custom('trolley(ext)','./qakicons/externalQActor.png')
     with Cluster('ctxalarms', graph_attr=nodeattr):
          alarmdevice=Custom('alarmdevice','./qakicons/symActorSmall.png')
          warningdevice=Custom('warningdevice','./qakicons/symActorSmall.png')
          sonar=Custom('sonar(coded)','./qakicons/codedQActor.png')
          led=Custom('led(coded)','./qakicons/codedQActor.png')
     sys >> Edge( label='obstacle', **evattr, decorate='true', fontcolor='darkgreen') >> alarmdevice
     sys >> Edge( label='free', **evattr, decorate='true', fontcolor='darkgreen') >> alarmdevice
     sys >> Edge( label='distance', **evattr, decorate='true', fontcolor='darkgreen') >> alarmdevice
     alarmdevice >> Edge(color='blue', style='solid',  decorate='true', label='<sonarstart &nbsp; sonarstop &nbsp; >',  fontcolor='blue') >> sonar
     warningdevice >> Edge(color='blue', style='solid',  decorate='true', label='<ledoff &nbsp; ledon &nbsp; ledblink &nbsp; >',  fontcolor='blue') >> led
     trolley >> Edge(color='blue', style='solid',  decorate='true', label='<statustrolley &nbsp; >',  fontcolor='blue') >> warningdevice
     alarmdevice >> Edge(color='blue', style='solid',  decorate='true', label='<stoptrolley &nbsp; resumetrolley &nbsp; >',  fontcolor='blue') >> coldstorageservice
diag
