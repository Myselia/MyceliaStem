package com.myselia.stem.communication.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import com.google.gson.Gson;
import com.myselia.javacommon.communication.mail.Addressable;
import com.myselia.javacommon.communication.mail.MailBox;
import com.myselia.javacommon.communication.mail.MailService;
import com.myselia.javacommon.communication.units.Transmission;
import com.myselia.javacommon.framework.communication.WebSocketHelper;
import com.myselia.stem.communication.CommunicationDock;
import com.myselia.stem.communication.StemClientSession;

public abstract class ComponentHandlerBase implements Handler, Addressable {

	protected String ip = "";
	protected String mac = "";
	protected String hashID = "";

	protected boolean ready = false;
	protected StemClientSession session;
	protected BufferedReader input;
	protected PrintWriter output;
	protected MailBox<Transmission> mb;
	protected Gson jsonInterpreter;
	private String inputToken = "";
	private String outputToken = "";

	public void setSession(StemClientSession session) {
		if (CommunicationDock.getNetworkComponentbyHash(hashID) != null) {
			System.out.println("Reviving dead session.");
			CommunicationDock.getNetworkComponentbyHash(hashID).reviveDeadSession(session);
		} else {
			CommunicationDock.addNewNetworkComponent(hashID, this);
			this.session = session;
			input = (BufferedReader) session.getReader();
			output = (PrintWriter) session.getWriter();
			jsonInterpreter = new Gson();
			mb = new MailBox<Transmission>();
			// TODO Mail Service Stuff !
			MailService.registerAddressable(this);
			//
			ready = true;
		}
	}

	@Override
	public void handleComponent() throws IOException {
		if (session.isHTTP()) {
			int len = 0;
			byte[] buff = new byte[2048];
			byte[] rawPayload;

			if (input.ready()) {
				len = session.getInStream().read(buff);

				if (len > 0) {
					rawPayload = WebSocketHelper.decodeWebSocketPayload(buff, len);

					if (WebSocketHelper.isEndStreamSignal(rawPayload)) {
						throw new IOException();
					} else {
						String payload = new String(rawPayload);
						System.out.println("GOT FROM LENS: " + payload);
						try {
							Transmission t = (jsonInterpreter.fromJson(payload, Transmission.class));
							mb.enqueueOut(t);
						} catch (Exception e) {
							System.err.println("Error parsing json @ " + this);
						}
					}

				}
			}

			if (mb.getInSize() > 0) {
				outputToken = jsonInterpreter.toJson(mb.dequeueIn());
				System.out.println("Sending: " + outputToken);
				session.getOutStream().write(WebSocketHelper.encodeWebSocketPayload(outputToken));
			}

			session.getOutStream().flush();
		} else {
			
			if (input.ready()) {
				if ((inputToken = input.readLine()) != null) {
					mb.enqueueOut(jsonInterpreter.fromJson(inputToken, Transmission.class));
					transmissionReceived();
				}
			}
			if (mb.getInSize() > 0) {
				outputToken = jsonInterpreter.toJson(mb.dequeueIn());
				System.out.println("Sending: " + outputToken);
				output.println(outputToken);
			} 
		}
	}

	protected abstract void transmissionReceived();

	@Override
	public abstract void primeHandler(Map<String, String> setupMap);

	@Override
	public boolean ready() {
		return ready;
	}

	public void reviveDeadSession(StemClientSession session) {
		this.session.resetExistingConnection(session);
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getHashID() {
		return hashID;
	}

	public void setHashID(String hashID) {
		this.hashID = hashID;
	}

	public BufferedReader getInput() {
		return input;
	}

	public PrintWriter getOutput() {
		return output;
	}

	public void resetStreams(PrintWriter w, BufferedReader r) {
		input = (BufferedReader) r;
		output = (PrintWriter) w;
	}

}