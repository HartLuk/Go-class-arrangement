package com.rpc.threadPool;

/**
 * 服务端信息
 * 
 * @author Administrator
 *
 */
public class ServerInfo {
	private String host;
	private int port;
	 
	public ServerInfo(String host, int port) {
	    this.host = host;
	    this.port = port;
	}
	 
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}

	public String toString() {
		return "host:" + host + ",port:" + port;
	}
}
