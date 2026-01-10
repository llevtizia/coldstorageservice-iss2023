package test;

import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.Assert;
import unibo.basicomm23.utils.CommUtils;

/**
 * test del sistema con comandi sonar
 * verifica che il sistema reagisca correttamente agli eventi del sonar
 */

public class TestTrolleyStopResume {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        TestUtils.initializeConnections();
        System.out.println("------- INIZIO TEST SISTEMA SONAR -------");
    }
    
    @Before
    public void setUp() {
    	System.out.println("\n------- Setup test -------");
    	
    	// ferma il sonar prima del reset
        TestUtils.stopSonarForManualControl();
        CommUtils.delay(500);
        
        // reset completo prima di ogni test
        TestUtils.basicReset();
        
        // il trolley torna a casa
        TestUtils.waitForTrolleyHome(10);
        CommUtils.delay(2000);
        
        System.out.println("\n------- Setup completato -------");
    }
    
    @After
    public void tearDown() {
        System.out.println("------- Test completato -------");
        TestUtils.completeCleanupWithVerification();
    }
    
    @AfterClass
    public static void tearDownClass() {
        System.out.println("------- TEST SISTEMA SONAR COMPLETATI -------");
    }
    
    //------- TEST -------
    
    /**
     * TEST 1: Obstacle → Stop → Free → Resume
     */
    @Test
    public void testCompleteStopResumeCycle() {
        try {
            System.out.println("\n------- TEST 1: Ciclo completo STOP/RESUME -------");
            
            // fermo il sonar se no il robot poi si muove
            TestUtils.stopSonarForManualControl();
            CommUtils.delay(1000);
            
            // 1 - avvia processo
            System.out.println("Avvio processo...");
            TestUtils.startTrolleyProcess(12);
            TestUtils.verifyTrolleyIsMoving();
            
            // 2 - OBSTACLE -> STOP (comando diretto)
            System.out.println("Fase 1: OBSTACLE -> STOP");
            TestUtils.forceStop();
            
            Assert.assertTrue("Robot dovrebbe fermarsi dopo obstacle", TestUtils.waitForTrolleyState("stopped", 5));
            System.out.println("Il robot si è fermato!");
            
            // 3 - aspetta con robot fermo e verifica che sia rimasto fermo
            TestUtils.delayWithLog(2, "robot fermo");
            
            String stateAfterWait = TestUtils.getCurrentTrolleyState();
            System.out.println("   Stato dopo attesa: " + stateAfterWait);
            
            Assert.assertTrue("Il robot rimane fermo", stateAfterWait.contains("stopped"));
            System.out.println("Il robot è rimasto fermo!");
            
            // 4 - FREE -> RESUME
            System.out.println("Fase 2: FREE -> RESUME");
            TestUtils.forceResume();
            
            Assert.assertTrue("Robot dovrebbe ripartire dopo free", TestUtils.waitForTrolleyNotState("stopped", 5));
            System.out.println("Il robot è ripartito!");
            
            // 5 - verifica che sia effettivamente in movimento
            System.out.println("Verifica movimento...");
            CommUtils.delay(2000);
            String currentState = TestUtils.getCurrentTrolleyState();
            Assert.assertFalse("Robot NON dovrebbe essere fermo", currentState.contains("stopped"));
            System.out.println("Robot in movimento: " + currentState);
            
            System.out.println("\n------- TEST 1 PASSATO -------");
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test fallito: " + e.getMessage());
        }
    }
    
    
    
    /**
     * TEST 2: Due cicli stop/resume consecutivi
     */
    
    @Test
    public void testTwoStopResumeCycles() {
        try {
            System.out.println("\n------- TEST 2: Due cicli STOP/RESUME consecutivi -------");
            
            // fermo il sonar
            TestUtils.stopSonarForManualControl();
            CommUtils.delay(500);
            
            // avvia processo
            TestUtils.startTrolleyProcess(10);
            TestUtils.verifyTrolleyIsMoving();
            
            // ------- PRIMO CICLO -------
            System.out.println("\n------- PRIMO CICLO -------");
            
            // 1 - primo STOP
            System.out.println("Primo STOP...");
            TestUtils.forceStop();
            Assert.assertTrue("Il primo stop dovrebbe funzionare", TestUtils.waitForTrolleyState("stopped", 5));
            System.out.println("Primo stop OK!");
            
            // 2 - attesa
            TestUtils.delayWithLog(2, "prima pausa");
            
            // 3 - prima RESUME
            System.out.println("Prima RESUME...");
            TestUtils.forceResume();
            Assert.assertTrue("La prima resume dovrebbe funzionare", TestUtils.waitForTrolleyNotState("stopped", 5));
            System.out.println("Prima resume OK!");
            
            // ------- SECONDO CICLO -------
            System.out.println("\n------- SECONDO CICLO -------");
            
            // 4 - attesa prima del secondo ciclo ( per > MINT )
            System.out.println("Attendo 6 secondi (simulazione MINT)...");
            TestUtils.delayWithLog(6, "preparazione secondo ciclo");
            
            // 5 - secondo STOP
            System.out.println("Secondo STOP...");
            TestUtils.forceStop();
            Assert.assertTrue("Il secondo stop dovrebbe funzionare", TestUtils.waitForTrolleyState("stopped", 5));
            System.out.println("Secondo stop OK");
            
            // 6 - attesa
            TestUtils.delayWithLog(2, "seconda pausa");
            
            // 7 - Seconda RESUME
            System.out.println("Seconda RESUME...");
            TestUtils.forceResume();
            Assert.assertTrue("La seconda resume dovrebbe funzionare", TestUtils.waitForTrolleyNotState("stopped", 5));
            System.out.println("Seconda resume OK!");
            
            // 8 - verifica finale movimento
            CommUtils.delay(1000);
            String finalState = TestUtils.getCurrentTrolleyState();
            Assert.assertFalse("Il robot dovrebbe essere in movimento alla fine", finalState.contains("stopped"));
            System.out.println("Robot in movimento: " + finalState);
            
            System.out.println("\n------- TEST 2 PASSATO -------");
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test fallito: " + e.getMessage());
        }
    }
    
    /**
     * TEST 3: Sistema ignora stop quando robot è già fermo
     */
    @Test
    public void testIgnoreStopWhenAlreadyStopped() {
        try {
            System.out.println("\n------- TEST 3: Ignora STOP quando già fermo -------");
            
            // fermo il sonar
            TestUtils.stopSonarForManualControl();
            CommUtils.delay(500);
            
            // avvia processo
            TestUtils.startTrolleyProcess(8);
            TestUtils.verifyTrolleyIsMoving();;
            
            // 1 - primo STOP
            System.out.println("Primo STOP...");
            TestUtils.forceStop();
            
            Assert.assertTrue("Robot dovrebbe fermarsi", TestUtils.waitForTrolleyState("stopped", 5));
            System.out.println("Il robot si è fermato");
            
            // 2 - secondo STOP mentre il robot è già fermo
            System.out.println("Secondo STOP (mentre il robot è già fermo)...");
            TestUtils.forceStop();
            
            // 3 - aspetta e verifica che lo stato non cambia
            CommUtils.delay(2000);
            String stateAfterSecondStop = TestUtils.getCurrentTrolleyState();
            System.out.println("	Stato dopo secondo stop: " + stateAfterSecondStop);
            
            // 4 - il robot deve rimanere fermo
            Assert.assertTrue("Il robot dovrebbe rimanere fermo", stateAfterSecondStop.contains("stopped"));
            System.out.println("	Il robot rimane fermo (secondo stop ignorato)");
            
            // 5 - RESUME per riprendere
            System.out.println("RESUME per riprendere...");
            TestUtils.forceResume();
            
            Assert.assertTrue("Il robot dovrebbe ripartire", TestUtils.waitForTrolleyNotState("stopped", 10));
            System.out.println("Robot ripartito!");
            
            System.out.println("\n------- TEST 3 PASSATO -------");
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test fallito: " + e.getMessage());
        }
    }
}