/** 
 * GoBackNReceiver - Assignment 2 for ECSE 414
 *
 * Receiver implementation of the Go-back-N protocol
 *
 * Michael Rabbat, Syed Haq
 */
 
import java.io.*;
import java.net.*;

class GoBackNReceiver {
	
	private DatagramSocket receiverSocket;
	private byte expectedseqnum;
	private InetAddress senderIPAddress;
	private int senderPort;
	
	/**
	 * Create a new receiver socket and wait for an incoming connection
	 * @throws SocketException 
	 */ 
	public GoBackNReceiver(int port) throws SocketException {
		// STEP 1: Fill in this constructor method
        // Initialize receiverSocket as a DatagramSocket on the specified port
        // Then call waitForConnection()
		
		receiverSocket=new DatagramSocket(port);
		waitForConnection();
		
	}
	
	/**
	 * Wait for a sender to contact us with the Hello message
	 */
	private void waitForConnection() {
		// STEP 2: Block until a Hello packet is received, then initialize the receiver
        // First block until receiveing an incoming message
        // Make sure it's a Hello (otherwise, ignore it and continue waiting)
        // If it's a Hello, initialize expectedseqnum to 0
        // Send an ACK back to the sender using the sendAck() method
        // Call the receiveMessage() method to receive the message
		
		// Allocate space for the received message 
		byte[] receiveData = new byte[3];
		DatagramPacket receivepacket=new DatagramPacket(receiveData,receiveData.length);
		try {
			receiverSocket.receive(receivepacket);
		} catch (Exception e) {
			System.out.println("Receiver error when receiving Hello packet");
			System.out.print(e);
			System.out.println("");
			System.exit(1);
		}
		
		//Get contents of packet received into string
		
		if(receiveData[0]==1){
			
		senderIPAddress=receivepacket.getAddress();
		senderPort=receivepacket.getPort();
		expectedseqnum=0;
		sendAck(expectedseqnum);
		receiveMessage();
		}
		else waitForConnection();
	}
	
	/**
	 * Receive message from the sender
	 */
	private void receiveMessage() {
		// STEP 3: Implement the main portion of the Go-back-N protocol
        // Contine processing Data packets until the Goodbye packet is received
        // When Goodbye is received, close the socket and leave
        // For each data packet received, check the sequence number
        // If it was the expected one, print the data to the command line
        // Send the appropriate ACK to the sender
		
		boolean continu=true;
		while(continu){
			GoBackNPacket dataPacket=receivePacket();
			if(dataPacket.isGoodbye()){
			continu=false;
			receiverSocket.close();
			System.out.println("Sender sent goodbye");
			System.exit(0);
			}
			else{
				if(dataPacket.getSequenceNumber()==expectedseqnum){
				System.out.print(dataPacket.getValue());
				sendAck(expectedseqnum);
				expectedseqnum++;
				
				}
				else{
				expectedseqnum--;
				sendAck(expectedseqnum);
				expectedseqnum++;
				}
				
				}
			}
		}
		
	
	
	/**
	 * Helper method to receive a single packet from the sender
	 */
	private GoBackNPacket receivePacket() {
		byte[] data = new byte[3];
		DatagramPacket dgPacket = new DatagramPacket(data,3);
		try {
			receiverSocket.receive(dgPacket);
		} catch (Exception e) {
			System.out.println("Receiver error when receiving data packet");
			System.out.print(e);
			System.out.println("");
			System.exit(1);
		}
		return new GoBackNPacket(dgPacket);
	}
	
	/**
	 * Helper method to send an ACK for a given sequence number
	 */
	private void sendAck(byte sequenceNumber) {
		GoBackNPacket gbnPacket = new GoBackNPacket(GoBackNPacket.TYPE_ACK, sequenceNumber, 'a');
		DatagramPacket p = gbnPacket.toDatagramPacket();
		p.setAddress(senderIPAddress);
		p.setPort(senderPort);
		try {
			receiverSocket.send(p);
		} catch (Exception e) {
			System.out.println("Receiver error when sending ACK");
			System.out.print(e);
			System.out.println("");
			System.exit(1);
		}
	}
	

	/**
	 * Main method
	 * @throws SocketException 
	 */
	public static void main(String args[]) throws SocketException {
		while (true) {
			GoBackNReceiver receiver = new GoBackNReceiver(9876);
		}
	}
}