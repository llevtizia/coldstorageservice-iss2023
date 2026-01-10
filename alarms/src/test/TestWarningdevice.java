package test;

import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.Assert;

import unibo.basicomm23.utils.CommUtils;

/**
 * test funzionale del warningdevice
 * verifica che il warningdevice osservi correttamente il trolley e comandi il LED in base allo stato:
 * - trolley in HOME -> LED OFF
 * - trolley in movimento -> LED BLINK
 * - trolley fermo (stopped) -> LED ON
 */

public class TestWarningdevice {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        TestUtils.initializeConnections();
        System.out.println("------- INIZIO TEST WARNINGDEVICE -------");
    }
    
    @Before
    public void setUp() {
        System.out.println("\n------- Setup test warningdevice -------");
        
        // ferma il sonar
        TestUtils.stopSonarForManualControl();
        CommUtils.delay(500);
        
        // reset completo
        TestUtils.basicReset();
        TestUtils.waitForTrolleyHome(10);
        CommUtils.delay(2000);
        
        System.out.println("------- Setup completato -------");
    }
    
    @After
    public void tearDown() {
        System.out.println("------- Cleanup test warningdevice -------");
        TestUtils.completeCleanupWithVerification();
    }
    
    @AfterClass
    public static void tearDownClass() {
        System.out.println("------- TEST WARNINGDEVICE COMPLETATI -------");
        System.out.println("NOTA: Lo stato logico del trolley è 'HOME' (waitrequest)");
        System.out.println("      La posizione fisica potrebbe essere diversa dopo cleanup");
    }
    
    /**
     * TEST 1: LED OFF quando trolley in HOME
     */
    @Test
    public void testLedOffWhenTrolleyAtHome() {
        try {
            System.out.println("\n------- TEST 1: LED OFF quando trolley in HOME -------");
            
            // 1 - verifica stato iniziale
            System.out.println("Verifica stato iniziale...");
            String trolleyState = TestUtils.getCurrentTrolleyState();
            String ledState = TestUtils.getCurrentLedState();
            String warningState = TestUtils.getCurrentWarningdeviceState();
            
            System.out.println("	Trolley: " + trolleyState);
            System.out.println("	LED: " + ledState);
            System.out.println("	Warningdevice: " + warningState);
            
            // 2 - verifica che trolley sia in HOME
            Assert.assertTrue("Trolley dovrebbe essere in HOME", 
            		trolleyState.contains("waitrequest") || 
            		trolleyState.contains("trolleyathome") ||
                    trolleyState.contains("resetdone"));
            
            // 3 - verifica che LED sia OFF
            Assert.assertTrue("LED dovrebbe essere OFF quando trolley in HOME", ledState.contains("OFF"));
            
            System.out.println("LED OFF corretto!");
            System.out.println("\n------- TEST 1 PASSATO -------");
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test fallito: " + e.getMessage());
        }
    }
    
    /**
     * TEST 2: LED BLINK quando trolley si muove
     */
    @Test
    public void testLedBlinkWhenTrolleyMoving() {
        try {
            System.out.println("\n------- TEST 2: LED BLINK quando trolley si muove -------");
            
            // 1 - avvia processo
            System.out.println("Avvio processo...");
            TestUtils.startTrolleyProcess(10);
            CommUtils.delay(2000);
            
            // 2 - verifica che il trolley si muova
            System.out.println("Verifico movimento trolley...");
            String trolleyState = TestUtils.getCurrentTrolleyState();
            System.out.println("Trolley: " + trolleyState);
            
            Assert.assertFalse("Trolley dovrebbe essere in movimento", 
                             trolleyState.contains("waitrequest") || 
                             trolleyState.contains("stopped"));
            System.out.println("	Trolley in movimento!");
            
            // 3 - verifica che il LED lampeggi
            System.out.println("Verifica LED lampeggiante...");
            String ledState = TestUtils.getCurrentLedState();
            System.out.println("   LED: " + ledState);
            
            Assert.assertTrue("Il LED dovrebbe lampeggiare quando il trolley si muove", ledState.contains("BLINK"));
            System.out.println("LED lampeggia!");
            
            System.out.println("\n------- TEST 2 PASSATO -------");
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test fallito: " + e.getMessage());
        }
    }
    
    /**
     * TEST 3: LED ON quando trolley fermo (stopped)
     */
    @Test
    public void testLedOnWhenTrolleyStopped() {
        try {
            System.out.println("\n------- TEST 3: LED ON quando trolley stopped -------");
            
            // 1 - avvia processo
            System.out.println("Avvio processo...");
            TestUtils.startTrolleyProcess(8);
            TestUtils.verifyTrolleyIsMoving();
            CommUtils.delay(1000);
            
            // 2 - ferma il trolley
            System.out.println("Fermo il trolley...");
            TestUtils.forceStop();
            
            Assert.assertTrue("Trolley dovrebbe fermarsi", TestUtils.waitForTrolleyState("stopped", 5));
            System.out.println("	Trolley fermato");
            
            // 3 - verifica che il LED sia ON
            System.out.println("Verifico che il LED sia ON...");
            CommUtils.delay(1000); // aspetta che il warningdevice reagisca
            
            String ledState = TestUtils.getCurrentLedState();
            System.out.println("   LED: " + ledState);
            
            Assert.assertTrue("LED dovrebbe essere ON quando il trolley è fermo (stopped)", 
                            ledState.contains("ON") && !ledState.contains("BLINK"));
            System.out.println("LED ON!");
            
            System.out.println("\n------- TEST 3 PASSATO -------");
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test fallito: " + e.getMessage());
        }
    }
    
    /**
     * TEST 4: Ciclo completo HOME → MOVING → STOPPED → MOVING → HOME
     */
    @Test
    public void testCompleteLedCycle() {
        try {
            System.out.println("\n------- TEST 4: Ciclo completo LED -------");
            System.out.println("Sequenza: HOME(OFF) -> MOVING(BLINK) -> STOPPED(ON) -> MOVING(BLINK) -> HOME(OFF)");
            
            // 1 - HOME -> LED OFF
            System.out.println("\n Stato iniziale: HOME -> LED OFF");
            String led1 = TestUtils.getCurrentLedState();
            System.out.println("   LED: " + led1);
            Assert.assertTrue("LED dovrebbe essere OFF", led1.contains("OFF"));
            System.out.println("OFF ok!");
            
            // 2 - MOVING -> LED BLINK
            System.out.println("\n Avvio processo: MOVING -> LED BLINK");
            TestUtils.startTrolleyProcess(12);
            CommUtils.delay(2000);
            
            String led2 = TestUtils.getCurrentLedState();
            System.out.println("   LED: " + led2);
            Assert.assertTrue("LED dovrebbe blinkare", led2.contains("BLINK"));
            System.out.println("BLINK ok!");
            
            // 3 - STOPPED -> LED ON
            System.out.println("\n Stop trolley: STOPPED -> LED ON");
            TestUtils.forceStop();
            TestUtils.waitForTrolleyState("stopped", 5);
            CommUtils.delay(1000);
            
            String led3 = TestUtils.getCurrentLedState();
            System.out.println("   LED: " + led3);
            Assert.assertTrue("LED dovrebbe essere ON", led3.contains("ON") && !led3.contains("BLINK"));
            System.out.println("ON ok!");
            
            // 4 - MOVING -> LED BLINK
            System.out.println("\n Resume trolley: MOVING -> LED BLINK");
            TestUtils.forceResume();
            TestUtils.waitForTrolleyNotState("stopped", 5);
            CommUtils.delay(1000);
            
            String led4 = TestUtils.getCurrentLedState();
            System.out.println("   LED: " + led4);
            Assert.assertTrue("LED dovrebbe blinkare di nuovo", led4.contains("BLINK"));
            System.out.println("BLINK ok!");
            
            System.out.println("\n Attesa completamento processo...");
            System.out.println("   (Il trolley completa e torna in HOME autonomamente)");
            
            System.out.println("\n------- TEST 4 PASSATO -------");
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test fallito: " + e.getMessage());
        }
    }
    
    /**
     * TEST 5: Warningdevice continua ad osservare dopo più cicli
     */
    @Test
    public void testWarningdevicePersistence() {
        try {
            System.out.println("\n------- TEST 5: Warningdevice osserva per più cicli -------");
            
            // Ciclo 1
            System.out.println("\n--- CICLO 1 ---");
            TestUtils.startTrolleyProcess(8);
            CommUtils.delay(1000);
            Assert.assertTrue("LED dovrebbe blinkare", TestUtils.getCurrentLedState().contains("BLINK"));
            System.out.println("	LED BLINK!");
            
            TestUtils.forceStop();
            TestUtils.waitForTrolleyState("stopped", 5);
            CommUtils.delay(1000);
            Assert.assertTrue("LED dovrebbe essere ON", TestUtils.getCurrentLedState().contains("ON"));
            System.out.println("	LED ON!");
            
            TestUtils.forceResume();
            TestUtils.waitForTrolleyNotState("stopped", 5);
            
            // Attendi completamento
            CommUtils.delay(8000);
            TestUtils.waitForTrolleyHome(10);
            
            // Ciclo 2
            System.out.println("\n--- CICLO 2 ---");
            TestUtils.startTrolleyProcess(6);
            CommUtils.delay(1000);
            Assert.assertTrue("LED dovrebbe blinkare ancora", TestUtils.getCurrentLedState().contains("BLINK"));
            System.out.println("	LED BLINK (ciclo 2)!");
            
            System.out.println("\nWarningdevice funziona per cicli multipli");
            System.out.println("\n------- TEST 5 PASSATO -------");
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test fallito: " + e.getMessage());
        }
    }
}