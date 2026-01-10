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
            
            for ( int i = 0; i < 10; i++ ) {
                CommUtils.delay(500);
                String currentState = TestUtils.getCurrentAlarmdeviceState();
                
                // verifica che lo stato cambi (riceve eventi)
                if ( !currentState.equals( initialState ) || currentState.contains( "distance" ) ) {
                    eventDetected = true;
                    System.out.println("	Evento rilevato: " + currentState);
                    break;
                }
            }
            
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
            String state1 = TestUtils.getCurrentAlarmdeviceState();
            CommUtils.delay(2000);
            String state2 = TestUtils.getCurrentAlarmdeviceState();
            
            System.out.println("Primo stato dopo stop (t=0): " + state1);
            System.out.println("Secondo stato dopo stop (t=2): " + state2);
            
            // Se il sonar è fermo, lo stato non dovrebbe cambiare 
            Assert.assertEquals("Lo stato dovrebbe essere stabile dopo stop", state1, state2);
            
            System.out.println("Sonar fermato, stato stabile");
            System.out.println("\n------- TEST 3 PASSATO -------");
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test fallito: " + e.getMessage());
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /*
    
    @Test
    public void testSonarGeneratesDistanceEvents() {
        try {
            System.out.println("------- TEST 2: Sonar genera eventi distance -------");
            
            // 1 - avvia il sonar
            IApplMessage startMsg = CommUtils.buildDispatch("test", "sonarstart", "sonarstart(test_distance)", TestUtils.ALARMS_CONTEXT);
            TestUtils.getAlarmsConnection().forward(startMsg);
            
            // 2 - monitora alarmdevice per eventi distance
            String alarmUrl = "coap://localhost:8025/ctxalarms/alarmdevice";
            CoapClient alarmClient = new CoapClient(alarmUrl);
            
            boolean distanceEventDetected = false;
            
            for (int i = 0; i < 20; i++) {
                CoapResponse response = alarmClient.get();
                if (response != null) {
                    String alarmState = response.getResponseText();
                    System.out.println("Monitoraggio [" + i + "]: " + alarmState);
                    
                    // cerco eventi distance
                    if (alarmState.contains("distance") || alarmState.contains("the sonar works")) {
                        distanceEventDetected = true;
                        break;
                    }
                }
                CommUtils.delay(500);
            }
            
            Assert.assertTrue("Il sonar dovrebbe generare eventi distance", distanceEventDetected);
            System.out.println("TEST PASSATO: Sonar genera eventi distance");
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test fallito: " + e.getMessage());
        }
    }
    
    */

    	
}