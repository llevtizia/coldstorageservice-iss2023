package test;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import unibo.basicomm23.interfaces.IApplMessage;
import unibo.basicomm23.interfaces.Interaction;
import unibo.basicomm23.msg.ProtocolType;
import unibo.basicomm23.utils.CommUtils;
import unibo.basicomm23.utils.ConnectionFactory;

public class TestRobot {
	
	@Test
	public void testRobotMovement() {
	    try {
	        System.out.println("------- Test Robot Movement -------");
	        
	        // 1 - connessione diretta al basic robot
	        Interaction robotConn = ConnectionFactory.createClientSupport(ProtocolType.tcp, "127.0.0.1", "8020");
	        System.out.println("Connesso al basic robot");
	        
	        // 2 - engage del robot
	        IApplMessage engageMsg = CommUtils.buildRequest("testClient", "engage", "engage(test,150)", "basicrobot");
	        
	        IApplMessage engageReply = robotConn.request(engageMsg);
	        Assert.assertEquals("engagedone", engageReply.msgId());
	        System.out.println("Robot engaged");
	        
	        // 3 - test movimento INDOOR port
	        IApplMessage moveMsg = CommUtils.buildRequest("testClient", "moverobot", "moverobot(0,4)", "basicrobot"); 
	        
	        IApplMessage moveReply = robotConn.request(moveMsg);
	        System.out.println("Move reply: " + moveReply);
	        
	        // il movimento dovrebbe riuscire
	        Assert.assertTrue("moverobotdone".equals(moveReply.msgId()));
	        System.out.println("Movimento completato: " + moveReply.msgId());
	        
	     // 4 - test ritorno HOME
	        IApplMessage returnMsg = CommUtils.buildRequest("testClient", "moverobot", "moverobot(0,0)", "basicrobot");
	        
	        IApplMessage returnReply = robotConn.request(returnMsg);
	        System.out.println("Return reply: " + returnReply);
	        
	        String returnResult = returnReply.msgId();
	        Assert.assertTrue("moverobotdone".equals(returnResult));
	        System.out.println("Ritorno a home completato");
	        
	        // 5 - disengage 
	        IApplMessage disengageMsg = CommUtils.buildDispatch("testClient", "disengage", "disengage(test)", "basicrobot");
	        robotConn.forward(disengageMsg);
	        System.out.println("Robot disengaged");
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        Assert.fail("Test failed with exception: " + e.getMessage());
	    }
	}
}
