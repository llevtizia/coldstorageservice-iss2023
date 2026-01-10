package test;

import unibo.basicomm23.interfaces.IApplMessage;
import unibo.basicomm23.interfaces.Interaction;
import unibo.basicomm23.msg.ProtocolType;
import unibo.basicomm23.utils.CommUtils;
import unibo.basicomm23.utils.ConnectionFactory;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;

/**
 * utility condivise per tutti i test
 */

public class TestUtils {
    
    // Costanti
    public static final String ALARMS_ACTOR = "alarmdevice"; // ALARMS_CONTEXT 
    public static final String COLDSTORAGE_ACTOR = "coldstorageservice"; //COLDSTORAGE_CONTEXT = "coldstorageservice";
    
    public static final String TROLLEY_ACTOR = "trolley";
    public static final String SONAR_ACTOR = "sonar";
    public static final String LED_ACTOR = "led";
    
    // URL CoAP
    public static final String TROLLEY_COAP_URL = "coap://localhost:8015/ctxcoldstorageservice/trolley";
    
    public static final String ALARMDEVICE_COAP_URL = "coap://localhost:8025/ctxalarms/alarmdevice";
    public static final String SONAR_COAP_URL = "coap://localhost:8025/ctxalarms/sonar";
    
    public static final String WARNINGDEVICE_COAP_URL = "coap://localhost:8025/ctxalarms/warningdevice";
    public static final String LED_COAP_URL = "coap://localhost:8025/ctxalarms/led";
    
    // Connessioni 
    private static Interaction connAlarms;
    private static Interaction connColdStorage;
    
    // Client CoAP 
    private static CoapClient trolleyCoapClient;
    
    private static CoapClient alarmdeviceCoapClient;
    private static CoapClient sonarCoapClient;
    
    private static CoapClient warningdeviceCoapClient;
    private static CoapClient ledCoapClient;
    
    
    /**
     * inizializza le connessioni (@BeforeClass)
     */
    
    public static void initializeConnections() throws Exception {
    	
        CommUtils.delay(5000); // inizializzazione sistema
        
        connAlarms = ConnectionFactory.createClientSupport(ProtocolType.tcp, "localhost", "8025");
        connColdStorage = ConnectionFactory.createClientSupport(ProtocolType.tcp, "localhost", "8015");
        
        // inizializza client CoAP
        trolleyCoapClient = new CoapClient(TROLLEY_COAP_URL);
        
        alarmdeviceCoapClient = new CoapClient(ALARMDEVICE_COAP_URL);
        warningdeviceCoapClient = new CoapClient(WARNINGDEVICE_COAP_URL);
        
        ledCoapClient = new CoapClient(LED_COAP_URL);
        sonarCoapClient = new CoapClient(SONAR_COAP_URL);
        
        System.out.println("Connessioni stabilite");
    }
    
    
    // ------- GETTER PER CONNESSIONI -------
    
    public static Interaction getAlarmsConnection() {
        return connAlarms;
    }
    
    public static Interaction getColdStorageConnection() {
        return connColdStorage;
    }
    
    public static CoapClient getTrolleyCoapClient() {
        return trolleyCoapClient;
    }
    
    public static CoapClient getAlarmdeviceCoapClient() {
        return alarmdeviceCoapClient;
    }
    
    public static CoapClient getWarningdeviceCoapClient() {
        return warningdeviceCoapClient;
    }
    
    public static CoapClient getLedCoapClient() {  
        return ledCoapClient;
    }
    
    public static CoapClient getSonarCoapClient() {  
        return sonarCoapClient;
    }
    
    
    // ------- METODI PER IL SONAR -------
    
    /**
     * ferma il sonar 
     */
    
    public static void stopSonarForManualControl() {
        try {
        	
            IApplMessage stopMsg = CommUtils.buildDispatch("utils", "sonarstop", "sonarstop(manual_control)", ALARMS_ACTOR);
            connAlarms.forward(stopMsg);
            CommUtils.delay(500);
            
        } catch (Exception e) {
            System.err.println("Errore fermando sonar: " + e.getMessage());
        }
    }
    
    /**
     * imposta manualmente la distanza del sonar (per simulazione)
     */
    public static void setSonarDistance(int distance) {
        try {
	        IApplMessage setDistMsg = CommUtils.buildDispatch("utils", "setdistance", "setdistance(" + distance + ")", "sonar");
	        connAlarms.forward(setDistMsg);
	        CommUtils.delay(500); // Aspetta che il sonar elabori
        } catch (Exception e) {
            System.err.println("Errore impostando distanza: " + e.getMessage());
        }
    }
    
    /**
     * simula un ostacolo impostando distanza < DLIMIT
     */
    public static void simulateObstacle() {
        setSonarDistance(15); // sotto DLIMIT (25)
    }

    /**
     * simula area libera impostando distanza > DLIMIT  
     */
    public static void simulateFree() {
        setSonarDistance(50); // sopra DLIMIT (25)
    }
    
    /**
     * avvia il sonar
     */
    public static void startSonar(String testName) {
        try {
        	
            IApplMessage startMsg = CommUtils.buildDispatch("utils", "sonarstart", "sonarstart(" + testName + ")", ALARMS_ACTOR);
            connAlarms.forward(startMsg);
            CommUtils.delay(1000);
            
        } catch (Exception e) {
            System.err.println("Errore avviando sonar: " + e.getMessage());
        }
    }
    
    /**
     * forza lo stop del trolley (bypass alarmdevice per test se no sonar continua ad oscillare)
     */
    public static void forceStop() {
        try {
        	
            System.out.println("	[Forzando STOP del trolley...]");
            IApplMessage stopMsg = CommUtils.buildDispatch("utils", "stoptrolley", "stoptrolley(test_force)", COLDSTORAGE_ACTOR);
            connColdStorage.forward(stopMsg);
            CommUtils.delay(500);
            
        } catch (Exception e) {
            System.err.println("Errore forzando stop: " + e.getMessage());
        }
    }

    /**
     * forza il resume del trolley (come sopra)
     */
    public static void forceResume() {
        try {
        	
            System.out.println("	[Forzando RESUME del trolley...]");
            IApplMessage resumeMsg = CommUtils.buildDispatch("utils", "resumetrolley", "resumetrolley(test_force)", COLDSTORAGE_ACTOR);
            connColdStorage.forward(resumeMsg);
            CommUtils.delay(500);
            
        } catch (Exception e) {
            System.err.println("Errore forzando resume: " + e.getMessage());
        }
    }
    
    /**
     * legge lo stato corrente del sonar
     */
    public static String getCurrentSonarState() {
        try {
        	CoapResponse response = sonarCoapClient.get();
        	return response != null ? response.getResponseText() : "unreachable";
            
        } catch (Exception e) {
            System.err.println("Errore lettura stato sonar: " + e.getMessage());
            return "error";
        }
    }
    
    // ------- METODI PER IL LED -------
    /**
     * legge lo stato corrente del LED via CoAP
     */
    public static String getCurrentLedState() {
        try {
        	CoapResponse response = ledCoapClient.get();
        	return response != null ? response.getResponseText() : "unreachable";
            
        } catch (Exception e) {
            System.err.println("Errore lettura stato LED: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Legge lo stato del warningdevice via CoAP
     */
    public static String getCurrentWarningdeviceState() {
        try {
            CoapResponse response = warningdeviceCoapClient.get();
            
            return response != null ? response.getResponseText() : "unreachable";
            
        } catch (Exception e) {
            System.err.println("Errore lettura warningdevice: " + e.getMessage());
            return "error";
        }
    }
    
    
    // ------- METODI PER IL TROLLEY -------
    
    /**
     * legge lo stato corrente del trolley via CoAP
     */
    public static String getCurrentTrolleyState() {
        try {
        	
            CoapResponse response = trolleyCoapClient.get();
            return response != null ? response.getResponseText() : "unreachable";
            
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    
    /**
     * ottiene lo stato corrente dell'alarmdevice via CoAP
     */
    public static String getCurrentAlarmdeviceState() {
        try {
        	
            CoapResponse response = alarmdeviceCoapClient.get();
            return response != null ? response.getResponseText() : "unreachable";
            
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    
    /**
     * attende che il trolley sia in uno stato specifico
     */
    public static boolean waitForTrolleyState(String expectedState, int maxSeconds) {
    	
        for ( int i = 0; i < maxSeconds * 2; i++ ) {
            try {
            	
                CoapResponse response = trolleyCoapClient.get();
                if ( response != null && response.getResponseText().contains( expectedState ) ) {
                    return true;
                }
            } catch (Exception e) {
                // Ignora errori 
            }
            CommUtils.delay(500);
            
        }
        return false;
    }
    
    /**
     * attende che il trolley non sia in uno stato specifico
     */
    public static boolean waitForTrolleyNotState( String notExpectedState, int maxSeconds ) {
        
    	for ( int i = 0; i < maxSeconds * 2; i++ ) {
            try {
            	
                CoapResponse response = trolleyCoapClient.get();
                if ( response != null && !response.getResponseText().contains(notExpectedState)) {
                    return true;
                }
            } catch (Exception e) {
                // Ignora errori temporanei
            }
            CommUtils.delay(500);
            
        }
        return false;
    }
    
    /**
     * avvia un processo normale (store + ticket) per far muovere il trolley
     */
    public static String startTrolleyProcess(int weight) throws Exception {
    	
        IApplMessage storerequest = CommUtils.buildRequest("utils", "storerequest", "storerequest(" + weight + ")", COLDSTORAGE_ACTOR);
        IApplMessage storereply = connColdStorage.request(storerequest);
        
        String ticket = extractTicketFromReply(storereply);
        
        IApplMessage ticketrequest = CommUtils.buildRequest("utils", "ticketrequest", "ticketrequest(" + ticket + ")", COLDSTORAGE_ACTOR);
        connColdStorage.request(ticketrequest);
        
        return ticket;
    }
    
    /**
     * verifica che il trolley sia in movimento (non stopped)
     */
    public static void verifyTrolleyIsMoving() {
    	
        String state = getCurrentTrolleyState();
        
        if (state.contains("stopped")) {
            throw new AssertionError("Trolley dovrebbe essere in movimento, invece è: " + state);
        }
    }
    
    // ------- METODI DI RESET/CLEANUP -------
    
    /**
     * reset base del sistema
     */
    public static void basicReset() {
        try {
        	
            // 1 - ferma il sonar se è attivo
            IApplMessage stopSonarMsg = CommUtils.buildDispatch("utils", "sonarstop", "sonarstop(reset)", ALARMS_ACTOR);
            connAlarms.forward(stopSonarMsg);
            CommUtils.delay(500);
            
            // 2 - assicura che non ci siano ostacoli
            IApplMessage freeMsg = CommUtils.buildEvent("utils", "free", "free(clear)");
            connAlarms.forward(freeMsg);
            
            // 3 - invia reset al trolley per farlo tornare in HOME
            IApplMessage resetMsg = CommUtils.buildDispatch("utils", "reset", "reset(basic)", COLDSTORAGE_ACTOR);
            connColdStorage.forward(resetMsg);
            
            CommUtils.delay(2000);
            
        } catch (Exception e) {
            System.err.println("Errore in basicReset: " + e.getMessage());
        }
    }
    
   
    /**
     * attende che il trolley torni a casa
     */
    public static boolean waitForTrolleyHome(int maxSeconds) {
    	for (int i = 0; i < maxSeconds * 2; i++) {
    		
    		try {
    	            String state = getCurrentTrolleyState();
    	            if ( state.contains("waitrequest") || state.contains("trolleyathome") || state.contains("resetdone") ) {
    	            	return true;
                    }
    	            
	        } catch (Exception e) {
	            System.err.println("Errore in waitForTrolleyHome: " + e.getMessage());
	            return false;
	        }
    		 CommUtils.delay(500);
    	}
    	return false;
    }
    
    /**
     * cleanup completo con verifica che il robot torni in HOME
     */
    public static void completeCleanupWithVerification() {
        try {
        	
        	System.out.println("Cleanup: invio reset...");
            
            // 1 - ferma il sonar se attivo
        	System.out.println("Cleanup: fermo il sonar...");
            stopSonarForManualControl();
            CommUtils.delay(1000);
            
            // 2 - controlla lo stato corrente
            String currentState = getCurrentTrolleyState();
            System.out.println("Stato corrente prima del cleanup: " + currentState);
            
            // 3 - se il robot è fermo manda un resume
            int resumeRetries = 0;
            while ( currentState.contains("stopped") && resumeRetries < 3 ) {
                resumeRetries++;
                System.out.println("Cleanup: robot STOPPED, invio resume (tentativo " + resumeRetries + "/3)...");
                forceResume();
                CommUtils.delay(1500);
                
                currentState = getCurrentTrolleyState();
                System.out.println("   Stato dopo resume: " + currentState);
            }
            
            if ( currentState.contains("stopped") ) {
                System.err.println("ATTENZIONE: robot ancora STOPPED dopo 3 tentativi");
            }
            
            // 4 - aspetta che si stabilizzi
            CommUtils.delay(1000);
            
            // 5 - invia reset
            System.out.println("Cleanup: invio reset...");
            IApplMessage resetMsg = CommUtils.buildDispatch("utils", "reset", "reset(cleanup)", COLDSTORAGE_ACTOR);
            connColdStorage.forward(resetMsg);
            
            // 6 - aspetta che torni in HOME
            System.out.println("Cleanup: attesa ritorno in HOME...");
            boolean isHome = waitForTrolleyHome(20);  // 20 secondi di timeout
            
            if ( isHome ) {
                System.out.println("Cleanup completato: il robot è tornato in HOME");
            } else {
                System.err.println("WARNING: il robot potrebbe non essere in HOME dopo cleanup");
                logSystemState("Stato dopo cleanup fallito");
                
                // tentativo di recovery
                System.out.println("Tentativo recovery...");
                IApplMessage forceResetMsg = CommUtils.buildDispatch("utils", "reset", "reset(force_recovery)", COLDSTORAGE_ACTOR);
                connColdStorage.forward(forceResetMsg);
                CommUtils.delay(2000);
                
                waitForTrolleyHome(10);
            }
            
            CommUtils.delay(1000);
            
        } catch (Exception e) {
            System.err.println("Errore in cleanup: " + e.getMessage());
        }
    }
    
    
    // ------- METODI HELPER GENERALI -------   
    
    /**
     * estrae il ticket da una reply storeaccepted
     */
    public static String extractTicketFromReply(IApplMessage reply) {
        
    	String content = reply.msgContent();
    	
    	// formato: "storeaccepted( TICKET, KG )"
    	
    	// 1 - apertura della parentesi
        int openParenthesis = content.indexOf('(');
        if (openParenthesis == -1) {
            throw new IllegalArgumentException("Formato messaggio non valido: " + content);
        }
        
        // 2 - virgola che separa ticket e kg
        int comma = content.indexOf(',', openParenthesis); // fromIndex -> cerca la virgola da dopo la parentesi
        if (comma == -1) {
            throw new IllegalArgumentException("Formato messaggio non valido: " + content);
        }
        
        // 3 - estrae il ticket tra ( e ,
        String ticket = content.substring( openParenthesis + 1, comma ).trim();

        return ticket;
    }
    
    /**
     * logga lo stato del sistema (per debug)
     */
    public static void logSystemState(String description) {
        System.out.println("\n------- " + description + " -------");
        System.out.println("Trolley: " + getCurrentTrolleyState());
        System.out.println("Alarmdevice: " + getCurrentAlarmdeviceState());
    }
    
    /**
     * attende un certo numero di secondi con logging
     */
    public static void delayWithLog(int seconds, String activity) {
        System.out.println("Attesa " + seconds + " secondi per " + activity + "...");
        
        for ( int i = 1; i <= seconds; i++ ) {
            System.out.println("  " + i + "/" + seconds + " secondi");
            CommUtils.delay(1000);
        }
    }  
    
}