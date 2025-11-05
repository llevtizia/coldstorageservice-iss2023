package test;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.junit.Test;
import org.junit.Assert;

import unibo.basicomm23.interfaces.IApplMessage;
import unibo.basicomm23.interfaces.Interaction;
import unibo.basicomm23.msg.ProtocolType;
import unibo.basicomm23.utils.CommUtils;
import unibo.basicomm23.utils.ConnectionFactory;

import java.util.ArrayList;
import java.util.List;

import java.util.stream.Collectors;


public class TestCoapObservation implements CoapHandler {
    
    private List<String> observedMessages = new ArrayList<>();	// per i messaggi osservati
    private CoapObserveRelation observation;					// per osservare
    
    private static Interaction conn;
    private final String SERVICE_NAME = "coldstorageservice";
    
    @Test
    public void testTrolleyStatus() {
        try {
            System.out.println("------- Test 4 - osservabilità trolley -------");
            
            // Reset della lista prima del test
            observedMessages.clear();
            
            // 1 - creo client CoAP per osservare lo stato del trolley
            String url = "coap://localhost:8015/ctxcoldstorageservice/trolley"; 
            /*
             * coap://host:port/context/actor
             * trolley: attore che espone la risorsa
             * */
            
            CoapClient client = new CoapClient(url);
            
            // 2 - osservazione
            observation = client.observe(this); // this -> la classe è l'handler
            System.out.println("Trolley CoAP observation started...");
            
            CommUtils.delay(5000);
            
            // 3 - connessione TCP al contesto -> mando la richiesta
            conn = ConnectionFactory.createClientSupport(ProtocolType.tcp, "localhost", "8015");
            
            System.out.println("start sending request: storerequest...");
            IApplMessage storeRequest = CommUtils.buildRequest("testClient", "storerequest", "storerequest(30)", SERVICE_NAME);
            
            IApplMessage storeReply = conn.request(storeRequest);
            Assert.assertEquals("storeaccepted", storeReply.msgId());
            System.out.println("storeaccepted received");
            
            // estrazione ticket dalla risposta
            String ticket = extractTicketFromReply(storeReply);
            System.out.println("ticket: " + ticket);
            
            System.out.println("waiting for trolley...");
            CommUtils.delay(5000);
            
            System.out.println("sending ticketrequest...");
            IApplMessage ticketRequest = CommUtils.buildRequest("testClient", "ticketrequest", "ticketrequest(" + ticket + ")", SERVICE_NAME);
            
            IApplMessage ticketReply = conn.request(ticketRequest);
            System.out.println("Risposta ticket: " + ticketReply);
            
            
            // 4 - attesa
            CommUtils.delay(12000); 
            
            
            // 5 - stop osservazione
            observation.proactiveCancel();
            
            // 6 - la lista di messaggi deve averne almeno uno
            Assert.assertFalse("Ho ricevuto dei messaggi!", observedMessages.isEmpty());
            
            System.out.println("Ricevute " + observedMessages.size() + " osservazioni");
            observedMessages.forEach(System.out::println);
            
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test failed with exception: " + e.getMessage());
        }
    }
    
    @Override
    public void onLoad(CoapResponse response) {
        String content = response.getResponseText();
        observedMessages.add(content);
        System.out.println("CoAP observation: " + content);
    }
    
    @Override
    public void onError() {
        System.err.println("CoAP observation error");
    }
    
    private String extractTicketFromReply(IApplMessage reply) {
    	
    	String content = reply.msgContent();
        
        // formato: "storeaccepted( TICKET, KG )"
        
        // 1 apertura della parentesi
        int openParenthesis = content.indexOf('(');
        if ( openParenthesis == -1 ) {
            throw new IllegalArgumentException( "Formato messaggio non valido: " + content );
        }
        
        // 2 virgola che separa ticket e kg
        int comma = content.indexOf(',', openParenthesis); // fromIndex -> cerca la virgola da dopo la parentesi
        if (comma == -1) {
            throw new IllegalArgumentException("Formato messaggio non valido: " + content);
        }
        
        // 3. Estrai il ticket tra ( e ,
        String ticket = content.substring( openParenthesis + 1, comma ).trim();
        
        return ticket;
    }
}