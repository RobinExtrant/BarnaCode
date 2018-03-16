package pddl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread{
	private static final int PORT = 8888;
	private byte[] buffer = new byte[2048];
	private volatile boolean stop = false;
	private DatagramSocket dsocket;
	private DatagramPacket packet;
	private List<Item> lastPointsReceived;
	private ServerListener sl;
	private static int xOffset = 0;
	private static int yOffset = 0;
	
	public Server(ServerListener sl){
		super("Server");
		this.sl = sl;
		this.packet = new DatagramPacket(this.buffer, this.buffer.length);
		this.lastPointsReceived	= new ArrayList<Item>();
		
		try {
			this.dsocket = new DatagramSocket(PORT);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		
	}
	
	public static void defineOffset(int x, int y){
		xOffset = x;
		yOffset = y;
	}
	
	@Override
	public void run() {
		this.setPriority(Thread.NORM_PRIORITY);
		while(! isInterrupted() && !this.stop){
			try {
				this.dsocket.receive(this.packet);
			} catch (IOException e) {
				this.stop = true;
			}
			String msg = new String(this.buffer, 0, this.packet.getLength());
			String[] items = msg.split("\n");
			this.lastPointsReceived.clear();
			for (int i = 0; i < items.length; i++) 
	        {
				String[] coord = items[i].split(";");
				if(coord.length == 3){
		        	int x = Integer.parseInt(coord[1]);
		        	int y = 300 - Integer.parseInt(coord[2]);
		        	this.lastPointsReceived.add(new Item((x*10) + xOffset, (y*10) + yOffset));		        	
				}
	        }
			this.sl.receiveRawPoints(this.lastPointsReceived);
			this.packet.setLength(this.buffer.length);
		}
	}
	
	@Override
	public void interrupt(){
		this.dsocket.close();
		this.stop = true;
	}

}
      