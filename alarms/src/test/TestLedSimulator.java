package test;

import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.Assert;

import unibo.basicomm23.interfaces.IApplMessage;
import unibo.basicomm23.utils.CommUtils;

/**
 * test del LED simulator
 * 
 * - ledoff → OFF
 * - ledon → ON
 * - ledblink → BLINK
 */

public class TestLedSimulator {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        TestUtils.initializeConnections();
        System.out.println("------- INIZIO TEST LED SIMULATOR -------");
    }
    
    @Before
    public void setUp() {
        System.out.println("\n------- Setup test LED -------");
        CommUtils.delay(1000);
        System.out.println("------- Setup completato -------");
    }
    
    @After
    public void tearDown() {
        System.out.println("------- Cleanup test LED -------");
        
        // ferma il blinking se attivo
        try {
            IApplMessage offMsg = CommUtils.buildDispatch("test", "ledoff", "ledoff(cleanup)", TestUtils.LED_ACTOR);
            TestUtils.getAlarmsConnection().forward(offMsg);
            CommUtils.delay(500);
        } catch (Exception e) {
            System.err.println("Errore cleanup: " + e.getMessage());
        }
    }
    
    @AfterClass
    public static void tearDownClass() {
        System.out.println("------- TEST LED SIMULATOR COMPLETATI -------");
    }
    
    /**
     * TEST 1: LED OFF
     */
    @Test
    public void testLedOff() {
        try {
            System.out.println("\n------- TEST 1: LED OFF -------");
            
            // 1 - invia comando ledoff
            System.out.println("Invio comando ledoff...");
            IApplMessage ledoffMsg = CommUtils.buildDispatch("test", "ledoff", "ledoff(test)", TestUtils.LED_ACTOR);
            TestUtils.getAlarmsConnection().forward(ledoffMsg);
            CommUtils.delay(1000);
            
            // 2 - verifica lo stato del LED via CoAP
            String ledState = TestUtils.getCurrentLedState();
            System.out.println("   Stato LED: " + ledState);
            
            Assert.assertTrue("LED dovrebbe essere OFF", ledState.contains("OFF"));
            System.out.println("LED OFF corretto");
            
            System.out.println("\n------- TEST 1 PASSATO -------");
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test fallito: " + e.getMessage());
        }
    }
    
    /**
     * TEST 2: LED ON
     */
    @Test
    public void testLedOn() {
        try {
            System.out.println("\n------- TEST 2: LED ON -------");
            
            // 1 - invia comando ledon
            System.out.println("Invio comando ledon...");
            IApplMessage ledonMsg = CommUtils.buildDispatch("test", "ledon", "ledon(test)", TestUtils.LED_ACTOR);
            TestUtils.getAlarmsConnection().forward(ledonMsg);
            CommUtils.delay(1000);
            
            // 2 - verifica lo stato del LED via CoAP
            String ledState = TestUtils.getCurrentLedState();
            System.out.println("   Stato LED: " + ledState);
            
            Assert.assertTrue("LED dovrebbe essere ON", ledState.contains("ON"));
            System.out.println("LED ON corretto");
            
            System.out.println("\n------- TEST 2 PASSATO -------");
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test fallito: " + e.getMessage());
        }
    }
    
    /**
     * TEST 3: LED lampeggia quando riceve BLINK
     */
    @Test
    public void testLedBlink() {
        try {
            System.out.println("\n------- TEST 3: LED BLINK -------");
            
            // 1 - invia comando ledblink
            System.out.println("Invio comando ledblink...");
            IApplMessage ledblinkMsg = CommUtils.buildDispatch("test", "ledblink", "ledblink(test)", TestUtils.LED_ACTOR);
            TestUtils.getAlarmsConnection().forward(ledblinkMsg);
            CommUtils.delay(1000);
            
            // 2 - monitora per vedere se lampeggia
            System.out.println("Monitoraggio blinking...");
            
            String state1 = TestUtils.getCurrentLedState();
            System.out.println("   t=0s: " + state1);
            
            boolean blinkDetected = false;
            
            for (int i = 0; i < 10; i++) {
                CommUtils.delay(500);
                String currentState = TestUtils.getCurrentLedState();
                
                // 3 - verifica che lo stato cambi (BLINK_ON/BLINK_OFF)
                if ( !currentState.equals( state1 ) ) {
                    blinkDetected = true;
                    System.out.println("   t=" + i + "s: " + currentState + " - cambio rilevato!");
                }
                
                if (i % 2 == 0) {
                    System.out.println("   t=" + i + "s: " + currentState);
                }
            }
            
            Assert.assertTrue("Il LED dovrebbe lampeggiare (lo stato deve cambiare)", blinkDetected);
            System.out.println("	LED lampeggia correttamente");
            
            // 4 - ferma il blinking con ledoff
            System.out.println("Fermo blinking con ledoff...");
            IApplMessage stopMsg = CommUtils.buildDispatch("test", "ledoff", "ledoff(stop_blink)", TestUtils.LED_ACTOR);
            TestUtils.getAlarmsConnection().forward(stopMsg);
            CommUtils.delay(1000);
            
            String finalState = TestUtils.getCurrentLedState();
            Assert.assertTrue("Il LED dovrebbe essere OFF", finalState.contains("OFF"));
            System.out.println("Blinking fermato");
            
            System.out.println("\n------- TEST 3 PASSATO -------");
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test fallito: " + e.getMessage());
        }
    }
    
    /**
     * TEST 4: Transizioni tra stati LED
     */
    /*
    @Test
    public void testLedStateTransitions() {
        try {
            System.out.println("\n------- TEST 4: Transizioni stati LED -------");
            System.out.println("Sequenza: OFF → ON → BLINK → OFF");
            
            // 1. OFF
            System.out.println("\n1. OFF...");
            IApplMessage offMsg = CommUtils.buildDispatch(
                "test", 
                "ledoff", 
                "ledoff(transition)", 
                TestUtils.ALARMS_CONTEXT
            );
            TestUtils.getAlarmsConnection().forward(offMsg);
            CommUtils.delay(1000);
            
            String state1 = TestUtils.getCurrentLedState();
            System.out.println("   Stato: " + state1);
            Assert.assertTrue("LED dovrebbe essere OFF", state1.contains("OFF"));
            System.out.println("   ✓ OFF");
            
            // 2. ON
            System.out.println("\n2. ON...");
            IApplMessage onMsg = CommUtils.buildDispatch(
                "test", 
                "ledon", 
                "ledon(transition)", 
                TestUtils.ALARMS_CONTEXT
            );
            TestUtils.getAlarmsConnection().forward(onMsg);
            CommUtils.delay(1000);
            
            String state2 = TestUtils.getCurrentLedState();
            System.out.println("   Stato: " + state2);
            Assert.assertTrue("LED dovrebbe essere ON", state2.contains("ON"));
            System.out.println("   ✓ ON");
            
            // 3. BLINK
            System.out.println("\n3. BLINK...");
            IApplMessage blinkMsg = CommUtils.buildDispatch(
                "test", 
                "ledblink", 
                "ledblink(transition)", 
                TestUtils.ALARMS_CONTEXT
            );
            TestUtils.getAlarmsConnection().forward(blinkMsg);
            CommUtils.delay(2000);
            
            String state3 = TestUtils.getCurrentLedState();
            System.out.println("   Stato: " + state3);
            Assert.assertTrue("LED dovrebbe essere in BLINK", state3.contains("BLINK"));
            System.out.println("   ✓ BLINK");
            
            // 4. Torna OFF
            System.out.println("\n4. Torna OFF...");
            TestUtils.getAlarmsConnection().forward(offMsg);
            CommUtils.delay(1000);
            
            String state4 = TestUtils.getCurrentLedState();
            System.out.println("   Stato: " + state4);
            Assert.assertTrue("LED dovrebbe essere OFF", state4.contains("OFF"));
            System.out.println("   ✓ OFF");
            
            System.out.println("\n------- TEST 4 PASSATO -------");
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test fallito: " + e.getMessage());
        }
    }
    */
}