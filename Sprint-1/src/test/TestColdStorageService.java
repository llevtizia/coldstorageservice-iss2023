package test;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import unibo.basicomm23.interfaces.IApplMessage;
import unibo.basicomm23.interfaces.Interaction;
import unibo.basicomm23.msg.ProtocolType;
import unibo.basicomm23.utils.CommUtils;
import unibo.basicomm23.utils.ConnectionFactory;

public class TestColdStorageService {
    
    private static Interaction conn;
    private final String SERVICE_NAME = "coldstorageservice";
    
    @BeforeClass // prima di tutto
    public static void setUpClass() throws Exception {
        // avvia il sistema 
    	// it.unibo.ctxcoldstorageservice.main();
    	
        CommUtils.delay(3000); // Attendi l'inizializzazione
        
        // connessione TCP al contesto
        conn = ConnectionFactory.createClientSupport(ProtocolType.tcp, "localhost", "8015");
    }
    
    @Before // prima di ogni test
    public void setUp() {
        CommUtils.delay(1000);
    }
    
    @Test
    public void testStoreRequestAccepted() {
        try {
            System.out.println("------- Test 1 - store request accepted -------");
            
            // invia richiesta di deposito
            IApplMessage storeRequest = CommUtils.buildRequest("testClient", "storerequest", "storerequest(50)", SERVICE_NAME);
            
            IApplMessage reply = conn.request(storeRequest);
            
            System.out.println("Reply received: " + reply);
            
            // verifica che la risposta sia storeaccepted
            Assert.assertEquals("storeaccepted", reply.msgId());
            Assert.assertTrue(reply.msgContent().contains("storeaccepted"));
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test failed with exception: " + e.getMessage());
        }
    }
    
    @Test
    public void testStoreRequestRefused() {
        try {
            System.out.println("------- Test 2 - store request refused -------");
            
            // inizia richiesta con peso > 200
            IApplMessage storeRequest = CommUtils.buildRequest("testClient", "storerequest", "storerequest(250)", SERVICE_NAME);
            
            IApplMessage reply = conn.request(storeRequest);
            
            System.out.println("Reply received: " + reply);
            
            // Verifica che la risposta sia storerefused
            Assert.assertEquals("storerefused", reply.msgId());
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test failed with exception: " + e.getMessage());
        }
    }
    
    @Test
    public void testFullExchange() {
        try {
            System.out.println("------- Test 3 - store + ticket request -------");
            
            // 1 - richiesta deposito -> storerequest
            System.out.println("1 - invio storerequest...");
            IApplMessage storeRequest = CommUtils.buildRequest("testClient", "storerequest", "storerequest(30)", SERVICE_NAME);
            
            IApplMessage storeReply = conn.request(storeRequest);
            Assert.assertEquals("storeaccepted", storeReply.msgId());
            System.out.println("storeaccepted ricevuto");
            
            // 2 - estrazione ticket dalla risposta
            String ticket = extractTicketFromReply(storeReply);
            System.out.println("2 - ticket estratto dalla risposta: " + ticket);
            
            // 3 - simula attesa per movimento a INDOOR
            System.out.println("3 - attendo movimento trolley...");
            CommUtils.delay(5000);
            
            // 4 -  richiesta ticket -> ticketrequest
            System.out.println("4 - invio ticketrequest...");
            IApplMessage ticketRequest = CommUtils.buildRequest("testClient", "ticketrequest", "ticketrequest(" + ticket + ")", SERVICE_NAME);
            
            IApplMessage ticketReply = conn.request(ticketRequest);
            System.out.println("Risposta ticket: " + ticketReply);
            
            // 5 - il carico è stato preso
            Assert.assertEquals("chargetaken", ticketReply.msgId());
            System.out.println("5 - test completato con successo");
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test failed with exception: " + e.getMessage());
        }
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