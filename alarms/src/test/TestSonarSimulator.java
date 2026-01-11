package test;

import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.Assert;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import unibo.basicomm23.interfaces.IApplMessage;
import unibo.basicomm23.utils.CommUtils;

/**
 * test del SONAR SIMULATOR
 * verifico che il sonar generi correttamente gli eventi distance, obstacle e free
 */

public class TestSonarSimulator {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
    	TestUtils.initializeConnections();
        System.out.println("------- INIZIO TEST SONAR SIMULATOR -------");
    }
    
    @Before
    public void setUp() {
    	 System.out.println("\n------- Setup test sonar -------");
         
         // fermo il sonar prima di ogni test
         TestUtils.stopSonarForManualControl();
         CommUtils.delay(1000);
         
         System.out.println("------- Setup completato -------");
    }
    
    @After
    public void tearDown() {
    	
        // ferma il sonar dopo ogni test
    	System.out.println("------- Cleanup test sonar -------");
        TestUtils.stopSonarForManualControl();
        CommUtils.delay(500);
    }
    
    @AfterClass
    public static void tearDownClass() {
        System.out.println("------- TEST SONAR SIMULATOR COMPLETATI -------");
    }
    
    /**
     * TEST 1: Verifica che il sonar si avvii correttamente e generi eventi
     */
    @Test
    public void testSonarStartsAndGeneratesEvents() {
        try {
            System.out.println("------- TEST 1: Sonar si avvia e genera eventi -------");
            
        	// 1 - avvia il sonar
            TestUtils.startSonar("test_start");
            CommUtils.delay(1000);
            
            // 2 - verifica che alarmdevice riceva eventi
            System.out.println("Monitoraggio eventi...");
            
            String initialState = TestUtils.getCurrentAlarmdeviceState();
            System.out.println("   Stato iniziale: " + initialState);
            
            boolean eventDetected = false;
            int count = 0;
            
            for ( int i = 0; i < 10; i++ ) {
                CommUtils.delay(500);
                String currentState = TestUtils.getCurrentAlarmdeviceState();
                
                // verifica che lo stato cambi (riceve eventi)
                if ( !currentState.equals( initialState ) || currentState.contains( "distance" ) ) {
                    eventDetected = true;
                    count++;
                    System.out.println("	Evento rilevato: " + currentState);;
                }
            }
            
            System.out.println("   Eventi generati: " + count);
            Assert.assertTrue("Il sonar dovrebbe generare eventi", eventDetected);
            System.out.println("\n------- TEST 1 PASSATO -------");
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test fallito: " + e.getMessage());
        }
    }
    
    /**
     * TEST 2: Verifica che il sonar si fermi
     */
    @Test
    public void testSonarStopsCorrectly() {
        try {
            System.out.println("------- TEST 2: Sonar si ferma correttamente -------");
            
            // 1 - avvia il sonar
            TestUtils.startSonar("test_stop");
            CommUtils.delay(1000);
            
            String stateWhileRunning = TestUtils.getCurrentAlarmdeviceState();
            System.out.println("Stato durante esecuzione: " + stateWhileRunning);
            
            // 2 - ferma il sonar
            System.out.println("Fermo il sonar...");
            TestUtils.stopSonarForManualControl();
            CommUtils.delay(2000);
            
            // 3 - verifica che non stia più generando eventi
            System.out.println("Il sonar è fermo e non dovrebbe generare eventi");
            System.out.println("	Monitoraggio...");
            
            String lastState = TestUtils.getCurrentSonarState();
            int changeCount = 0;
            
            for ( int i = 0; i < 10; i++ ) { 
                CommUtils.delay(500);
                String currentState = TestUtils.getCurrentSonarState();
                
                if ( !currentState.equals(lastState) ) {
                    changeCount++;
                    System.out.println("  [" + i + "s] Cambio rilevato: " + currentState);
                    lastState = currentState;
                }
            }
            
            System.out.println("\nCambiamenti rilevati: " + changeCount);
            
            // Se il sonar è fermo, non dovrebbero esserci cambiamenti
            Assert.assertEquals("Il sonar fermo non dovrebbe generare cambiamenti", 0, changeCount);
            
            System.out.println("Sonar fermato correttamente!");
            System.out.println("\n------- TEST 2 PASSATO -------");
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test fallito: " + e.getMessage());
        }
    }
    	
}