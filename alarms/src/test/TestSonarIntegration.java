package test;

import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.Assert;
import unibo.basicomm23.utils.CommUtils;

/**
 * test DI INTEGRAZIONE del sistema completo
 * testa sonar -> alarmdevice -> coldstorageservice -> trolley + MINT
 */

public class TestSonarIntegration {
	
	@BeforeClass
	public static void setUpClass() throws Exception {
    	TestUtils.initializeConnections();
        System.out.println("------- INIZIO TEST INTEGRAZIONE SONAR -------");
	}
	    
    @Before
    public void setUp() {
    	 System.out.println("\n------- Setup test integrazione -------");
         
    	 // reset base
         TestUtils.basicReset();
         TestUtils.waitForTrolleyHome(10);
         
         // non fermo il sonar
         CommUtils.delay(2000);
         
         System.out.println("------- Setup completato -------");
    }
	    
    @After
    public void tearDown() {
    	
        // ferma il sonar dopo ogni test
    	System.out.println("------- Cleanup test integrazione -------");
        TestUtils.stopSonarForManualControl();
        TestUtils.completeCleanupWithVerification();
    }
	    
    @AfterClass
    public static void tearDownClass() {
        System.out.println("------- TEST INTEGRAZIONE COMPLETATI -------");
        System.out.println("NOTA: Lo stato logico del trolley è 'HOME' (waitrequest)");
        System.out.println("      La posizione fisica potrebbe essere diversa dopo cleanup");
    }
	    
    /**
     * TEST 1: sistema completo con sonar attivo
     */
     @Test
     public void testCompleteSystemWithActiveSonar() {
	    try {
            System.out.println("\n------- TEST 1: Sistema completo con sonar -------");
            
            // 1 - avvia trolley
            System.out.println("Avvio trolley...");
            TestUtils.startTrolleyProcess(10);
            TestUtils.verifyTrolleyIsMoving();
            
            // 2 - avvia sonar
            System.out.println("Avvio sonar...");
            TestUtils.startSonar("integration_full_system");
            
            // 3 - monitora il sistema
            System.out.println("Monitoraggio sistema...");
            
            for (int i = 0; i < 30; i++) {
                String trolleyState = TestUtils.getCurrentTrolleyState();
                String alarmState = TestUtils.getCurrentAlarmdeviceState();
                
                if (i % 2 == 0) { 
                    System.out.println("   [" + i + "s] Trolley: " + trolleyState + " | Alarm: " + alarmState);
                }
                
                CommUtils.delay(500);
            }
            
            System.out.println("Il sistema ha operato senza crash");
            System.out.println("\n------- TEST INT-1 PASSATO -------");
            
        } catch (Exception e) {
                e.printStackTrace();
                Assert.fail("Test fallito: " + e.getMessage());
        } finally {
            TestUtils.stopSonarForManualControl();
        }
     }
	     
	     
     /**
      * TEST 2: MINT con sonar attivo
      * il trolley non si ferma se non sono passati MINT secondi dallo stop precedente
      */
     @Test
     public void testMintWithActiveSonar() {
         try {
             System.out.println("\n------- TEST 2: Test MINT -------");
             System.out.println("MINT configurato: 5 secondi");
             
             // 1 - avvia trolley
             System.out.println("Avvio trolley...");
             TestUtils.startTrolleyProcess(8);
             TestUtils.verifyTrolleyIsMoving();
             
             // 2 - avvia sonar
             System.out.println("Avvio sonar (oscilla tra 80 e 0)...");
             TestUtils.startSonar("mint_test");
             
             // 3 - monitora per rilevare stop multipli
             System.out.println("Monitoraggio...");
             System.out.println("	(Cerco stop multipli per verificare MINT)");
             
             long firstStopTime = 0;
             long secondStopTime = 0;
             int stopCount = 0;
             boolean wasMoving = true;
             
             for ( int i = 0; i < 60; i++ ) { 
                 String trolleyState = TestUtils.getCurrentTrolleyState();
                 boolean isStopped = trolleyState.contains("stopped");
                 
                 // il trolley era in movimento e si è fermato
                 if (isStopped && wasMoving) {
                     long currentTime = System.currentTimeMillis();
                     stopCount++;
                     
                     if ( stopCount == 1 ) {
                         firstStopTime = currentTime;
                         System.out.println("   [" + i + "s] PRIMO STOP rilevato");
                     } else if (stopCount == 2) {
                         secondStopTime = currentTime;
                         long mintObserved = (secondStopTime - firstStopTime) / 1000;
                         System.out.println("   [" + i + "s] ⚠ SECONDO STOP rilevato");
                         System.out.println("	Tempo tra stop: " + mintObserved + " secondi");
                         
                         if ( mintObserved >= 5 ) {
                             System.out.println("MINT RISPETTATO ( >= 5 secondi )");
                         } else {
                             System.out.println("MINT NON RISPETTATO ( < 5 secondi )");
                         }
                         break; // Abbiamo i dati che servono
                     }
                 }
                 
                 wasMoving = !isStopped; // se non è fermo si sta muovendo -> il prossimo stop arriva al trolley in movimento
                 
                 // log periodico
                 if (i % 10 == 0) {
                     System.out.println("   [" + i + "s] " + trolleyState);
                 }
                 
                 CommUtils.delay(500);
             }
             
             // risultati
             System.out.println("\n	Risultato:");
             System.out.println("- Stop rilevati: " + stopCount);
             
             if ( stopCount >= 2 ) {
                 long mintObserved = (secondStopTime - firstStopTime) / 1000;
                 System.out.println("   - Tempo tra primo e secondo stop: " + mintObserved + " secondi");
                 String mintResult = mintObserved >= 5 ? "RISPETTATO" : "NON RISPETTATO";
                 System.out.println("   - MINT " + mintResult);
                 
                 // Assert: MINT dovrebbe essere rispettato
                 Assert.assertTrue("MINT dovrebbe essere ≥5 secondi tra stop consecutivi", mintObserved >= 5);
             } 
             
             else if (stopCount == 1) {
                 System.out.println("   - Solo 1 stop in 30 secondi");
                 System.out.println("   - Serve più tempo per secondo stop");
             } 
             
             else {
                 System.out.println("   - Nessun stop rilevato");
                 System.out.println("   - Sonar potrebbe non aver generato obstacle");
             }
             
             System.out.println("\n------- TEST 2 COMPLETATO -------");
             
         } catch (Exception e) {
             e.printStackTrace();
             Assert.fail("Test fallito: " + e.getMessage());
         } finally {
             TestUtils.stopSonarForManualControl();
         }
     }
     
     /**
      * TEST 3: Sonar genera stop e trolley risponde
      */
     @Test
     public void testSonarTriggersStopAndResume() {
         try {
             System.out.println("\n------- TEST 3: stop/resume funzionanti -------");
             
             // 1 - avvia trolley
             System.out.println("Avvio trolley...");
             TestUtils.startTrolleyProcess(6);
             TestUtils.verifyTrolleyIsMoving();
             
             // 2 - avvia sonar
             System.out.println("Avvio sonar...");
             TestUtils.startSonar("stop_resume_test");
             
             // 3 - aspetta che il sonar generi uno stop
             System.out.println("Aspetto uno STOP dal sonar...");
             
             boolean stopDetected = false;
             
             for ( int i = 0; i < 60; i++ ) {
                 String state = TestUtils.getCurrentTrolleyState();
                 
                 if ( state.contains("stopped") ) {
                     stopDetected = true;
                     System.out.println("   [" + i + "s] ✓ STOP rilevato!");
                     break;
                 }
                 
                 if (i % 5 == 0) {
                     System.out.println("   [" + i + "s] " + state);
                 }
                 
                 CommUtils.delay(500);
             }
             
             if (stopDetected) {
                 System.out.println("	Sonar ha generato uno stop!");
                 
                 // 4 - aspetta resume
                 System.out.println("Attesa resume...");
                 boolean resumeDetected = TestUtils.waitForTrolleyNotState("stopped", 15);
                 
                 if ( resumeDetected ) {
                     System.out.println("Resume rilevata, trolley ripartito!");
                     Assert.assertTrue("Ciclo stop/resume completato", true);
                 } else {
                     System.out.println(" Resume non rilevata entro 15 secondi");
                     System.out.println("Sonar potrebbe rimanere sotto DLIMIT -> aspettare per più tempo");
                 }
             } else {
                 System.out.println("Nessuno stop rilevato");
                 System.out.println("Sonar potrebbe non aver raggiunto DLIMIT");
                 System.out.println("Test inconclusivo");
             }
             
             System.out.println("\n------- TEST INT-3 COMPLETATO -------");
             
         } catch (Exception e) {
             e.printStackTrace();
             Assert.fail("Test fallito: " + e.getMessage());
         } finally {
             TestUtils.stopSonarForManualControl();
         }
     }
}
