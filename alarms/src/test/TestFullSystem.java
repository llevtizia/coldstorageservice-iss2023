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
            System.out.println("\n------- TEST 4: Stabilità sistema completo -------");
            
            // 1 - avvia tutto
            System.out.println("Avvio sistema completo...");
            TestUtils.startTrolleyProcess(8);
            TestUtils.startSonar("stability_test");
            CommUtils.delay(2000);
            
            // 2 - monitora 
            System.out.println("Monitoraggio...");
            
            int updates = 0;
            String lastTrolleyState = "";
            String lastLedState = "";
            
            for ( int i = 0; i < 60; i++ ) {
                try {
                    String trolleyState = TestUtils.getCurrentTrolleyState();
                    String ledState = TestUtils.getCurrentLedState();
                    String alarmState = TestUtils.getCurrentAlarmdeviceState();
                    String warningState = TestUtils.getCurrentWarningdeviceState();
                    
                    // conta aggiornamenti
                    if ( !trolleyState.equals(lastTrolleyState) || !ledState.equals(lastLedState)) {
                        updates++;
                    }
                    
                    lastTrolleyState = trolleyState;
                    lastLedState = ledState;
                    
                    // log
                    if (i % 10 == 0) {
                        System.out.println("   [" + i + "s] Trolley: " + trolleyState);
                        System.out.println("         LED: " + ledState);
                        System.out.println("         Alarm (gestione sonar): " + alarmState);
                        System.out.println("         Warning (gestione led): " + warningState);
                    }
                    
                } catch (Exception e) {
                    System.err.println("   Errore lettura stato: " + e.getMessage());
                    Assert.fail("Sistema non stabile: " + e.getMessage());
                }
                
                CommUtils.delay(500);
            }
            
            System.out.println("\n   Risultato:");
            System.out.println("   - Aggiornamenti rilevati: " + updates);
            System.out.println("   - Sistema funzionante!");
            
            Assert.assertTrue("Sistema dovrebbe aver generato aggiornamenti", updates > 0);
            
            System.out.println("\n------- TEST 4 PASSATO -------");
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test fallito: " + e.getMessage());
        } finally {
            TestUtils.stopSonarForManualControl();
        }
    }
}