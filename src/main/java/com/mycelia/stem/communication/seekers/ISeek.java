package com.mycelia.stem.communication.seekers;

import java.io.IOException;
import java.net.UnknownHostException;

public interface ISeek {

	public void discoverComponents(byte[] infoPacket)
			throws IOException;

	public void openInternalSocket();
	public int getPort();
	public void setPort(int port);

}