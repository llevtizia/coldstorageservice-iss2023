package test;

import org.junit.Test;

import static org.junit.Assert.*;

import java.io.*;
import java.net.Socket;


public class ColdStorageServiceTest {
	
	public static final String ADDR		= "localhost";
	public static final int PORT		= 8015;
	public static final String MSG		= "msg( storerequest, request, serviceaccesgui, coldstorageservice, storerequest(5), 5 )";
	public static final String REPLY	= "storeaccepted";
	
	@Test
	public void storeRequestTest() {
		
		
		try (
				Socket client = new Socket(ADDR, PORT);
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
				BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))
			
		){

				// send store request
				out.write(MSG);
				out.newLine(); // ensure the message ends properly
				out.flush();
				
				//wait for response
				String response = in.readLine();
				assertNotNull("The response is null", response);
				assertTrue("The response doesn't contain 'storeaccepted'", response.contains(REPLY) );
						
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	@Test
	public void handleTicketTest() {
		
		
	    try (
	        Socket client = new Socket(ADDR, PORT);
	        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
	        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))
	    ) {
	        // send store request
	        out.write(MSG);
	        out.newLine();
	        out.flush();

	        // wait for response
	        String response = in.readLine();
	        assertNotNull("The response is null ", response);
	        assertTrue("The response doesn't contain 'storeaccepted' ", response.contains(REPLY));

	        // extract ticket number 
	        String ticketNumber = response.replaceAll("[^0-9]", ""); // extract all digits
	        ticketNumber = ticketNumber.replaceFirst("(\\d+).*", "$1"); // keep only the first number

	        // Wait before sending the second request
	        Thread.sleep(1000); // Or use TimeUnit.MILLISECONDS.sleep(1000);

	        // Send second request using the ticket
	        String ticketRequest = "msg( ticketrequest, request, serviceaccesgui, coldstorageservice, ticketrequest(" + ticketNumber + "), 6 )";
	        out.write(ticketRequest);
	        out.newLine();
	        out.flush();

	        // Wait for response
	        String ticketResponse = in.readLine();
	        assertNotNull("The response to the ticket is null ", ticketResponse);
	        assertTrue("The ticket is expired... ", 
	                   ticketResponse.contains("chargetaken"));

	    } catch (IOException | InterruptedException e) {
	        e.printStackTrace();
	        fail("Exception thrown during test execution: " + e.getMessage());
	    }
	}

	

}
