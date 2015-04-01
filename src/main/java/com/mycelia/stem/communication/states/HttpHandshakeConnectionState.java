package com.mycelia.stem.communication.states;

import java.io.BufferedReader;
import java.io.IOException;

import com.google.gson.Gson;
import com.mycelia.common.communication.units.Transmission;
import com.mycelia.common.communication.units.TransmissionBuilder;
import com.mycelia.common.constants.opcode.ActionType;
import com.mycelia.common.constants.opcode.ComponentType;
import com.mycelia.common.constants.opcode.OpcodeAccessor;
import com.mycelia.common.constants.opcode.operations.LensOperation;
import com.mycelia.common.constants.opcode.operations.StemOperation;
import com.mycelia.common.framework.communication.WebSocketHelper;
import com.mycelia.stem.communication.StemClientSession;
import com.mycelia.stem.communication.handlers.ComponentHandlerBase;
import com.mycelia.stem.communication.handlers.ComponentHandlerFactory;

public class HttpHandshakeConnectionState implements ConnectionState {

	private ComponentHandlerBase handler;
	private StemClientSession session;
	private String inputS = null;
	private BufferedReader input = null;
	private String webSocketKey = null;
	private String keyStringSearch = "Sec-WebSocket-Key: ";
	private Gson jsonParser;
	boolean connectionEstablished = false;
	boolean handshakeFinished = false;
	static int count = 0;
	
	@Override
	public void primeConnectionState(StemClientSession session) {
		this.session = session;
		this.input = (BufferedReader)session.getReader();
		this.jsonParser = new Gson();
	}
	
	@Override
	public void process() throws IOException {
		if (!connectionEstablished) {
			while ((inputS = input.readLine()) != null) {
				System.out.println("RECV IN HTTP: " + inputS);

				handleHeaders(inputS);

				if (connectionEstablished)
					break;
			}
		} else {
			try {
				//Send a ready packet to the component
				int len = 0;
				byte[] buff = new byte[2048];
				if (input.ready()) {
					len = session.getInStream().read(buff);
					if (len > 0) {
						System.out.println("RECV FROM WEBSOCK: " + WebSocketHelper.decodeWebSocketPayload(buff, len));
						handleSetupPacket(WebSocketHelper.decodeWebSocketPayload(buff, len));
					}
				}

			} catch (IOException e1) {
				e1.printStackTrace();
			}
			System.out.println("!!!!!!!!!!!SENDING TEST BITS!!!!!!!!!!!");
			session.getOutStream().write(WebSocketHelper.encodeWebSocketPayload(connectionReadyPacket()));
			count++;
			session.getOutStream().flush();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public ComponentHandlerBase getHandler() {
		return handler;
	}

	@Override
	public void setHandler(ComponentHandlerBase handler) {
		this.handler = handler;
	}
	
	private void handleHeaders(String input) {
		if (!connectionEstablished) {
			// Get the webSocketKey
			if (input.startsWith(WebSocketHelper.keyStringSearch)) {
				webSocketKey = input.substring(keyStringSearch.length());
				System.out.println("KEY IS:" + webSocketKey);
			}

			// They're done
			if (input.equals(""))
				connectionEstablished = true;

			// We need a webSocketKey to continue
			if (webSocketKey == null)
				System.err.println("No Sec-WebSocket-Key was passed at: " + session);
		}

		if (connectionEstablished && webSocketKey != null) {
			// Send our headers
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.out.println("!!!!!SENDING RESPONSE HEADERS!!!!!!");
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			WebSocketHelper.sendHandshakeResponse(session.getWriter(), webSocketKey);
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.out.println("!!!!!!!!!!!!!DONE!!!!!!!!!!!!!!!!!!");
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void handleSetupPacket(String s) {
		System.out.print("Setting up received packet...");
		ComponentHandlerBase handler = null;
		try {
			Transmission setupTransmission = jsonParser.fromJson(s, Transmission.class);
			handler = ComponentHandlerFactory.createHandler(setupTransmission, session);
			
			System.out.println("VALUE OF READY IS: " + handler.ready() );
			if (handler.ready()) {
				this.setHandler(handler);
				session.setComponentHandler(handler);
				session.getStateContainer().getConnectedState().setHandler(handler);
				session.setConnectionState(session.getStateContainer().getConnectedState());
			} else {
				//Tell the session thread to die
				session.die();
			}
		} catch (Exception e) {
			System.out.println("Setup packet from component is malformed!");
			e.printStackTrace();
		}
		System.out.println("....done");
	}
	

	
	private String connectionReadyPacket() {
		TransmissionBuilder tb = new TransmissionBuilder();
		String from = OpcodeAccessor.make(ComponentType.STEM, ActionType.DATA, StemOperation.TEST);
		String to = OpcodeAccessor.make(ComponentType.LENS, ActionType.DATA, LensOperation.TEST);
		tb.newTransmission(from, to);
		tb.addAtom("ready", "boolean", "true");
		Transmission t = tb.getTransmission();
		Gson gson = new Gson();
		return gson.toJson(t);
	}
	
	public String toString() {
		return "HTTP Handshaker";
	}
	
}