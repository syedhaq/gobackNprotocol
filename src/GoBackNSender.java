/** 
 * GoBackNSender - Assignment 2 for ECSE 414
 *
 * Sender implementation of the Go-back-N protocol
 * 
 * Michael Rabbat, Syed Haq	
 */
 
import java.io.*;
import java.net.*;

class GoBackNSender {

	// UDP socket for communications
	private DatagramSocket senderSocket;
	
	// Receiver's IP Address and port number
	private InetAddress receiverAddress;
	private int receiverPort;
	
	// Go-Back-N Window size and nextseqnum
	private static final int N = 5;
	private byte nextseqnum;
	private byte base;
	int nextseqnump;
	int basep;
	byte received;


	/**
	 * Creates a GoBackNSender and connects to the specified port number on 
	 * the named host.
	 * @throws IOException 
	 */
	public GoBackNSender(String host, int port) throws IOException {
  		// STEP 4: Fill in the GoBackNSender constructor
        // Initialize senderSocket
        // Lookup the specified hostname using InetAddress.getByName()
        // Store the receiverAddress and receiverPort
        // Construct a Hello packet and send it to the receiver
        // Wait for an ACK
        // Initialize base and nextseqnum to zero
		senderSocket = new DatagramSocket();
		try {
			receiverAddress=InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			System.out.println("IP Address not found");
		}
		receiverPort=port;
		GoBackNPacket hellopkt=new GoBackNPacket(GoBackNPacket.TYPE_HELLO,(byte)0,'h');
		DatagramPacket hello=hellopkt.toDatagramPacket();
		hello.setAddress(receiverAddress);
		hello.setPort(receiverPort);
		try {
			senderSocket.send(hello);
		} catch (IOException e) {
			System.out.println("Check construction of hello packet");
			e.printStackTrace();
		}
		
		
		//Wait for Ack of Hello
		try{
		receiveAck(1000);
		
		nextseqnum=0;
		base=0;
		}
		catch(SocketTimeoutException s ){
        	System.out.print(s);
        	System.out.print("did not receive ack within 1 sec");
        	System.exit(0);
        }

		
		
	}
	
	
	/**
	 * Helper method to send a single Go-back-N packet
	 */
	private void sendData(byte seqnum, char c) {
		GoBackNPacket gbnPacket = new GoBackNPacket(GoBackNPacket.TYPE_DATA, seqnum, c);
		DatagramPacket p = gbnPacket.toDatagramPacket();
		p.setAddress(receiverAddress);
		p.setPort(receiverPort);
		try {
			senderSocket.send(p);
		} catch (Exception e) {
			System.out.println("Sender error sending data packet (" + seqnum + ", " + c + ")");
			System.out.print(e);
			System.out.println("");
			System.exit(1);
		}
	}
	
	
	/**
	 * Helper method to receive an ACK.  
	 * When an ACK is received, return the ACKed sequence number
	 * After timeout milliseconds, this method times out and throws an exception
	 */
	private byte receiveAck(int timeout) throws SocketTimeoutException {
		byte[] data = new byte[3];
		DatagramPacket dgPacket = new DatagramPacket(data, 3);
		try {
			senderSocket.setSoTimeout(timeout);
			senderSocket.receive(dgPacket);
		} catch (Exception e) {
			if (e instanceof SocketTimeoutException) {
				throw (SocketTimeoutException)e;
			} else {
				System.out.println("Sender error while receiving ACK");
				System.out.print(e);
				System.out.println("");
				System.exit(1);
			}
		}

		GoBackNPacket gbnPacket = new GoBackNPacket(dgPacket);
		if (!gbnPacket.isAck()) {
			// Print an error message because this isn't an ACK
			System.out.println("Sender error: Expecting an ACK but received something else");
			System.exit(1);
		}
		return gbnPacket.getSequenceNumber();
	}
	
	
	/**
	 * Send a message one character at a time using the Go-back-N reliable
	 * data transfer protocol.
	 */
	public void send(String message) {
		// STEP 5: Implement the main part of the Go-Back-N sender
        // First send one window's worth of packets
        // Then start a loop that iterates until the entire message is received
        // Within the loop, first wait for an ACK using receiveAck(timeout)
        // After the ACK, update base and send new packets as appropriate
        // If receiveAck() times out, catch the exception and retransmit
		basep=(int)base;
		nextseqnump=(int)nextseqnum;
		//Send one window's worth of packets
		for(nextseqnump=0;nextseqnump<5;nextseqnump++){
			sendData((byte)nextseqnump,message.charAt(nextseqnump));
			nextseqnum++;
		}
		
		//Iterate until all characters received
		while(nextseqnump<message.length()-1){
			System.out.println("Base:"+ base);
			
			try {
				received=receiveAck(50);
				System.out.println("Received"+received);
				
				
					byte nextbase= (byte)(received+1);
					while(base!=nextbase){
						base++;
						basep++;
						
					System.out.println("Wraparound detected");
					System.out.println("base:"+base);
					System.out.println("positive base:"+basep);
					//}
					
				

				}
				
			} catch (SocketTimeoutException e) {
				for( nextseqnump=basep;nextseqnump<basep+N;nextseqnump++){
					sendData((byte)nextseqnump,message.charAt(nextseqnump));
					
				}
				nextseqnum= (byte) (base+ N);
				
			}
			
			while(nextseqnump<=basep+N){
				System.out.println("nextseqnum:"+nextseqnum);
				System.out.println("nextseqnump:"+nextseqnump);
				sendData(nextseqnum,message.charAt(nextseqnump));
				nextseqnum++;
				nextseqnump++;
			}
			
			
		}
		
	}
	
	
	/**
	 * Send a goodbye packet
	 */
	public void close() {
		GoBackNPacket gbnPacket = new GoBackNPacket(GoBackNPacket.TYPE_GOODBYE, (byte)0, 'g');
		DatagramPacket p = gbnPacket.toDatagramPacket();
		p.setAddress(receiverAddress);
		p.setPort(receiverPort);
		try {
			senderSocket.send(p);
		} catch (Exception e) {
			System.out.println("Sender error sending goodbye packet");
			System.out.print(e);
			System.out.println("");
			System.exit(1);
		}
		senderSocket.close();
	}
	

	/**
	 * Main method
	 * 
	 * Open a connection to the receiver, both running on this machine, and
	 * transmit a very long String using the GoBackN protocol
	 * @throws IOException 
	 */
	public static void main(String args[]) throws IOException {
		String message = "A long time ago, in a galaxy far, far away...\n\nEpisode IV, A NEW HOPE\n\nIt is a period of civil war. Rebel spaceships, striking from a hidden base, have won their first victory against the evil Galactic Empire. During the battle, Rebel spies managed to steal secret plans to the Empire's ultimate weapon, the DEATH STAR, an armored space station with enough power to destroy an entire planet. Pursued by the Empire's sinister agents, Princess Leia races home aboard her starship, custodian of the stolen plans that can save her people and restore freedom to the galaxy....";
	
		// Instantiate a new GoBackNSender which connects to address
		// 'localhost' on port 9876
		GoBackNSender gbnSender = new GoBackNSender("localhost", 9876);
		
		// Send the message
		gbnSender.send(message);
		
		// Close the connection
		gbnSender.close();
		
		System.out.println("");
	}
}