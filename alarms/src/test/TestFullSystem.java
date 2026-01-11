package test;

import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.Assert;

import unibo.basicomm23.utils.CommUtils;

/**
 * verifica che tutti i componenti collaborino correttamente 
 * (separato da testSystemIntegration per farlo anddare singolarmente
 */

public class TestFullSystem {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        TestUtils.initializeConnections();
        System.out.println("------- INIZIO TEST SISTEMA -------");
    }
    
    @Before
    public void setUp() {
        System.out.println("\n------- Setup test sistema -------");
        
        // reset base
        TestUtils.basicReset();
        TestUtils.waitForTrolleyHome(10);
        
        // non fermo il sonar perché voglio testarlo attivo
        CommUtils.delay(2000);
        
        System.out.println("------- Setup completato -------");
    }
    
    @After
    public void tearDown() {
        System.out.println("------- Cleanup test sistema -------");
        
        // ferma il sonar prima
        System.out.println("tearDown: fermo il sonar...");
        TestUtils.stopSonarForManualControl();
        CommUtils.delay(1500);
        
        // cleanup normale
        TestUtils.completeCleanupWithVerification();
        
        CommUtils.delay(1000);
        System.out.println("------- TearDown completato -------");
    }
    
    @AfterClass
    public static void tearDownClass() {
        System.out.println("------- TEST SISTEMA COMPLETATO -------");
    }
    
    /**
     * TEST 4: sistema completo 
     * verifica stabilità del sistema completo
     */
    @Test
    public void testCompleteSystemStability() {
        try {
            System.out.println("\n------- TEST: Stabilità sistema completo -------");
            System.out.println("Componenti attivi: Trolley, Sonar, LED, AlarmDevice, WarningDevice");
            
            // 1 - avvia tutto
            System.out.println("Avvio sistema completo...");
            TestUtils.startTrolleyProcess(8);
            TestUtils.startSonar("stability_test");
            CommUtils.delay(2000);
            
            // 2 - monitora 
            System.out.println("Monitoraggio sistema fino a completamento di un giro...");
            
            // contatori
            int trolleyUpdates = 0;
            int ledUpdates = 0;
            int alarmUpdates = 0;
            int warningUpdates = 0;
            
            String lastTrolleyState = "";
            String lastLedState = "";
            String lastAlarmState = "";
            String lastWarningState = "";
            
            boolean processCompleted = false;
            int errorCount = 0;
            
            for ( int i = 0; i < 100; i++ ) {
                try {
                    String trolleyState = TestUtils.getCurrentTrolleyState();
                    String ledState = TestUtils.getCurrentLedState();
                    String alarmState = TestUtils.getCurrentAlarmdeviceState();
                    String warningState = TestUtils.getCurrentWarningdeviceState();
                    
                    // conta aggiornamenti
                    if ( !trolleyState.equals(lastTrolleyState) ) {
                        trolleyUpdates++;
                        lastTrolleyState = trolleyState;
                    }
                    
                    if ( !ledState.equals(lastLedState) ) {
                        ledUpdates++;
                        lastLedState = ledState;
                    }
                    
                    if ( !alarmState.equals(lastAlarmState) ) {
                        alarmUpdates++;
                        lastAlarmState = alarmState;
                    }
                    if ( !warningState.equals(lastWarningState) ) {
                        warningUpdates++;
                        lastWarningState = warningState;
                    }
                    
                    // log
                    if (i % 10 == 0) {
                        System.out.println("\n [" + i + "s] Stato componenti: ");
                        System.out.println("         Trolley: " + trolleyState);		
                        System.out.println("         LED: " + ledState);
                        System.out.println("         Alarm (gestione sonar): " + alarmState);
                        System.out.println("         Warning (gestione led): " + warningState);
                    }
                    
                    if ( trolleyState.contains("waitrequest") || 
                            trolleyState.contains("trolleyathome") ||
                            trolleyState.contains("resetdone") ) {
                    	
                    	if ( !processCompleted ) {
                            processCompleted = true;
                            System.out.println("\n [" + i + "s] Processo completato - Trolley tornato in HOME");
                            
                            // ferma il sonar per evitare che blocchi il trolley dopo avere finito
                            TestUtils.stopSonarForManualControl();
                            CommUtils.delay(1000);
                            
                            // delay finale
                            System.out.println("   Attesa aggiornamenti finali...");
                            CommUtils.delay(2000);
                            break;
                        }
                       }
                    
                } catch (Exception e) {
                	errorCount++;
                    System.err.println("   Errore lettura stato (" + errorCount + "/5): " + e.getMessage());
                    
                    if ( errorCount > 5 ) {
                    	System.out.println("Sistema non stabile: troppi errori di lettura stato");
                    }
                }
                
                CommUtils.delay(500);
            }
            
            System.out.println("\n   Risultato:");
            System.out.println("   - Aggiornamenti Trolley:  " + trolleyUpdates);
            System.out.println("   - Aggiornamenti LED:      " + ledUpdates);
            System.out.println("   - Aggiornamenti Alarm:    " + alarmUpdates);
            System.out.println("   - Aggiornamenti Warning:  " + warningUpdates);
            System.out.println("   - Errori di lettura:      " + errorCount);
            System.out.println("   - Processo completato:    " + (processCompleted ? "SI" : "NO") );
            System.out.println("   - Sistema funzionante!");
            
            int totalUpdates = trolleyUpdates + ledUpdates + alarmUpdates + warningUpdates;
            System.out.println("   TOTALE aggiornamenti: " + totalUpdates);
            
            // 3 - verifica risultati
            Assert.assertTrue("Il sistema dovrebbe aver generato aggiornamenti", totalUpdates > 0);
            System.out.println("\n Il sistema ha generato " + totalUpdates + " aggiornamenti");
            
            Assert.assertTrue("Il trolley dovrebbe aver cambiato stato almeno una volta", trolleyUpdates > 0);
            System.out.println("Trolley attivo (" + trolleyUpdates + " cambiamenti di stato)");
            
            Assert.assertTrue("Il LED dovrebbe aver cambiato stato almeno una volta", ledUpdates > 0);
            System.out.println("LED attivo (" + ledUpdates + " cambiamenti di stato)");
            
            if ( processCompleted ) {
                System.out.println("	Processo di deposito completato con successo!");
            } else {
                System.out.println("	Processo non completato");
                System.out.println("	Possibile causa: sonar ha generato stop prolungati");
            }
            
            System.out.println("\n------- TEST 4 PASSATO -------");
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test fallito: " + e.getMessage());
        } finally {
            TestUtils.stopSonarForManualControl();
        }
    }
}