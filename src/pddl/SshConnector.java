package pddl;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.*;

public class SshConnector {
	String user;
	String password;
	String host;
	int port;
	String programPath;
	
	public SshConnector() {
		this.user = "root";
		this.password = "root";
		this.host = "192.168.43.211";
		this.port=22;
		this.programPath="/home/lejos/programs/";
	}

	public void copyFile() {
		try
		{
			JSch jsch = new JSch();
			Session session = jsch.getSession(user, host, port);
			session.setPassword(password);
			session.setConfig("StrictHostKeyChecking", "no");
			System.out.println("Establishing Connection...");
			session.connect();
			System.out.println("Connection established.");
			System.out.println("Crating SFTP Channel.");
			Channel channel = session.openChannel( "sftp" );
	        ChannelSftp sftp = ( ChannelSftp ) channel;
	        sftp.connect();
	        channel = session.openChannel("sftp");
	        channel.connect();
			System.out.println("SFTP Channel created.");

			File f = new File("barnaplan/testssh.txt");
			sftp.put(new FileInputStream(f), programPath + "plan.txt");
			
			channel.disconnect();
	        sftp.disconnect();
		}
		catch(Exception e){System.err.print(e);}
	}
}
