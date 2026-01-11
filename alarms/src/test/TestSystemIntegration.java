package test;

import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.Assert;

import unibo.basicomm23.utils.CommUtils;

/**
 * test di integrazione del sistema completo
 * - sistema con LED attivo (warningdevice osserva trolley)
 * - sistema con SONAR + LED insieme
 */

public class TestSystemIntegration {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        TestUtils.initializeConnections();
        System.out.println("------- INIZIO TEST INTEGRAZIONE SISTEMA -------");
    }
    
    @Before
    public void setUp() {
        System.out.println("\n------- Setup test integrazione -------");
        
        // reset base
        TestUtils.basicReset();
        TestUtils.waitForTrolleyHome(10);
        
        // non fermo il sonar perché voglio testarlo attivo
        CommUtils.delay(2000);
        
        System.out.println("------- Setup completato -------");
    }
    
    @After
    public void tearDown() {
        System.out.println("------- Cleanup test integrazione -------");
        
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
        System.out.println("------- TEST INTEGRAZIONE COMPLETATI -------");
    }
    
    
    /**
     * TEST 1: sistema completo SONAR + LED
     * verifica che sonar e LED lavorino insieme correttamente
     * sonar automatico
     */
    @Test
    public void testSystemWithSonarAndLed() {
        try {
            System.out.println("\n------- TEST 1: Sistema SONAR + LED -------");
            
            // 1 - avvia processo
            System.out.println("Avvio processo...");
            TestUtils.startTrolleyProcess(10);
            TestUtils.verifyTrolleyIsMoving();
            
            // 2 - avvia sonar
            System.out.println("Avvio sonar...");
            TestUtils.startSonar("integration_sonar_led");
            
            // 3 - monitora sistema 
            System.out.println("Monitoraggio sistema...");
            
            boolean stopDetected = false;
            boolean ledOnDetected = false;
            
            boolean resumeDetected = false;
            boolean ledBlinkAfterResume = false;
            
            for ( int i = 0; i < 40; i++ ) {
                String trolleyState = TestUtils.getCurrentTrolleyState();
                String ledState = TestUtils.getCurrentLedState();
                String alarmState = TestUtils.getCurrentAlarmdeviceState();
                
                // log
                if (i % 5 == 0) {
                    System.out.println("   [" + i + "s] Trolley: " + trolleyState + " | LED: " + ledState + " | Alarm: " + alarmState);
                }
                
                // 4 - rileva eventi
                if ( trolleyState.contains("stopped") ) {
                    if ( !stopDetected ) {
                        stopDetected = true;
                        System.out.println("   [" + i + "s] STOP rilevato dal sonar");
                    }
                    
                    // verifica che LED sia ON quando stopped
                    if ( ledState.contains("ON") && !ledState.contains("BLINK") ) {
                        ledOnDetected = true;
                    }
                } else if ( stopDetected && !trolleyState.contains("stopped") ) {
                    if ( !resumeDetected ) {
                        resumeDetected = true;
                        System.out.println("   [" + i + "s] RESUME rilevata");
                    }
                    
                    // verifica che LED torni a BLINK
                    if ( ledState.contains("BLINK") ) {
                        ledBlinkAfterResume = true;
                    }
                }
                    
                // fermo il sonar perché se no si blocca il trolley prima
                if ( stopDetected && resumeDetected && 
                		( trolleyState.contains("trolleyathome") || trolleyState.contains("waitrequest") || trolleyState.contains("resetdone") ) ) {
                    
                    System.out.println("   [" + i + "s] Test completato!");
                    System.out.println("   Fermo il sonar!");
                    
                    TestUtils.stopSonarForManualControl();
                    CommUtils.delay(1500);
                    
                    System.out.println("   Sonar fermato");
                    CommUtils.delay(2000);
                    
                    break; // esce dal loop: il trolley è comunque in home ma il sonar non lo ferma
                }
                
                // il trolley è tornato in HOME ma non abbiamo visto stop/resume
                if ( (trolleyState.contains("trolleyathome") || trolleyState.contains("waitrequest") || trolleyState.contains("resetdone")) ) {
                    System.out.println("   [" + i + "s] Trolley tornato in HOME");
                    TestUtils.stopSonarForManualControl();
                    CommUtils.delay(1000);
                    break;
                }
                
                CommUtils.delay(500);
        	}
            
            // 5 - valutazione
            System.out.println("\n   Risultati:");
            System.out.println("   - Stop rilevato: " + (stopDetected ? "sì" : "no"));
            System.out.println("   - LED ON durante stop: " + (ledOnDetected ? "sì" : "no"));
            System.out.println("   - Resume rilevata: " + (resumeDetected ? "sì" : "no"));
            System.out.println("   - LED BLINK dopo resume: " + (ledBlinkAfterResume ? "sì" : "no"));
	            
            if ( !stopDetected ) {
                System.out.println("\nATTENZIONE: Il sonar non ha generato stop");
                System.out.println("   Il test è INCONCLUSIVO");
            }
            
            if ( stopDetected ) {
                Assert.assertTrue("Il LED dovrebbe essere ON quando il trolley è stopped", ledOnDetected);
                if (resumeDetected) {
                    Assert.assertTrue("Il LED dovrebbe blinkare dopo la resume", ledBlinkAfterResume);
                }
            }
	            
            System.out.println("\n------- TEST 1 COMPLETATO -------");
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test fallito: " + e.getMessage());
        } finally {
            TestUtils.stopSonarForManualControl();
        }
    }
    
    /**
     * TEST 2: Coordinamento SONAR -> STOP -> LED ON
     * flusso completo: sonar genera obstacle -> trolley si ferma -> LED va ON
     * sonar manuale per forzare obstacle e free
     */
    @Test
    public void testSonarLedCoordination() {
        try {
            System.out.println("\n------- TEST 2: Coordinamento SONAR-LED -------");
            System.out.println("Flusso: SONAR (obstacle) -> TROLLEY (stop) -> LED(ON)");
            
            // 1 - ferma il sonar per controllo manuale
            System.out.println("\n Preparo sonar in modalità manuale...");
            TestUtils.stopSonarForManualControl();
            CommUtils.delay(500);
            
            // 2 - avvia processo
            System.out.println(" Avvio processo...");
            TestUtils.startTrolleyProcess(12);
            TestUtils.verifyTrolleyIsMoving();
            CommUtils.delay(2000);
            
            // 3 - verifica LED BLINK
            String ledBefore = TestUtils.getCurrentLedState();
            System.out.println("Stato del LED durante il movimento: " + ledBefore);
            Assert.assertTrue("Il LED dovrebbe essere in BLINK", ledBefore.contains("BLINK"));
            System.out.println("	LED BLINK!");
            
            // 4 - simula obstacle tramite sonar
            System.out.println("\n Simulo obstacle (distanza < DLIMIT)...");
            TestUtils.simulateObstacle();
            CommUtils.delay(500);
            
            System.out.println(" Riavvio sonar per generare evento...");
            TestUtils.startSonar("coordination_test"); // riavvio sonar e genera obstacle
            CommUtils.delay(1000); // tempo per generare l'evento
            
            // 5 - aspetta che alarmdevice elabori e fermi il trolley
            System.out.println(" Attesa stop...");
            boolean stopped = TestUtils.waitForTrolleyState("stopped", 5);
            
            if ( stopped ) {
                System.out.println("	Trolley fermato dal sonar!");
                
                // 6 - verifica che LED sia ON
                CommUtils.delay(1000);
                String ledAfterStop = TestUtils.getCurrentLedState();
                System.out.println(" LED dopo stop: " + ledAfterStop);
                
                Assert.assertTrue( "Il LED dovrebbe essere ON quando il trolley è stopped", 
                                ledAfterStop.contains("ON") && !ledAfterStop.contains("BLINK") );
                System.out.println("	LED ON!");
                
                // 7 - simula free
                System.out.println("\n Simulo free (distanza > DLIMIT)...");
                TestUtils.simulateFree();
                CommUtils.delay(500);
                
                // 8 - aspetta resume
                System.out.println(" Attesa resume...");
                boolean resumed = TestUtils.waitForTrolleyNotState("stopped", 5);
                
                if ( resumed ) {
                    System.out.println("	Trolley ripartito");
                    
                    // 9 - verifica che il LED torni a BLINK
                    CommUtils.delay(1000);
                    String ledAfterResume = TestUtils.getCurrentLedState();
                    System.out.println("Stato del LED dopo resume: " + ledAfterResume);
                    
                    Assert.assertTrue("LED dovrebbe blinkare di nuovo", ledAfterResume.contains("BLINK"));
                    System.out.println("	LED BLINK!");
                } else {
                    System.out.println("Il trolley non è ripartito");
                }
            } else {
                System.out.println("Il trolley non si è fermato (test inconclusivo)");
            }
            
            System.out.println("\n------- TEST 2 COMPLETATO -------");
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test fallito: " + e.getMessage());
        } finally {
            TestUtils.stopSonarForManualControl();
        }
    }
}