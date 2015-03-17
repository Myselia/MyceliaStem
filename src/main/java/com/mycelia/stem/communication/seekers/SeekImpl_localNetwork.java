package com.mycelia.stem.communication.seekers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.mycelia.stem.communication.ComDock;

public class SeekImpl_localNetwork implements ISeek {
	private volatile static SeekImpl_localNetwork uniqueInstance;
	private int port;
	private DatagramSocket discoverSocket;
	private String seekerName = "Local Network Seeker";
	
	private SeekImpl_localNetwork() {
	}

	public synchronized void discoverComponents(byte[] infoPacket) throws IOException {
		DatagramPacket networkPacket = new DatagramPacket(infoPacket, infoPacket.length,
				InetAddress.getByName("255.255.255.255"), ComDock.Component_Listen_Port);
		discoverSocket.send(networkPacket);
	}

	public synchronized void openInternalSocket() {
		try {
			discoverSocket = new DatagramSocket(port);
		} catch (Exception e) {
			System.err.println("SeekImpl_localNetwork: Cant open discover port on "
					+ port);
		}
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public void setPort(int port) {
		this.port = port;
	}
	
	public String printStatus(String componentType, String packet) {
		return	seekerName
				+ "\n\t|-> Looking for: " + componentType + " on local port: " + port
				+ "\n\t|-> With packet: " + packet;
	}
	
	public static SeekImpl_localNetwork getInstance() {
		if (uniqueInstance == null) {
			synchronized (SeekImpl_localNetwork.class) {
				if (uniqueInstance == null) {
					uniqueInstance = new SeekImpl_localNetwork();
				}
			}
		}
		return uniqueInstance;
	}
}