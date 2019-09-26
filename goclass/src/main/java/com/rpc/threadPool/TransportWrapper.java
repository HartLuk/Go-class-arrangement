package com.rpc.threadPool;

import java.util.Date;

import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

/**
 * 对连接进行封装，添加监控属性
 * 
 * @author Administrator
 *
 */
public class TransportWrapper {
	
	private TTransport transport;
	
	/**
	 * 服务端主机地址
	 */
	private String host;
	
	/**
	 * 服务端主机端口
	 */
	private int port;

	/**
	 * 是否处于忙碌状态
	 */
	private boolean isBusy = false;
	
	/**
	 * 是否已经死亡，即连接失效
	 */
	private boolean isDead = false;
	
	/**
	 * 最后使用时间
	 */
	private Date lastUserTime;
	
	
	
	/**
	 * 构造方法
	 * 
	 * @param transport TTransport实例或其子类实例，通常是一个TSocket实例
	 * @param isOpen TTransport实例是否激活
	 * @param host 服务端主机地址
	 * @param port 服务端主机端口
	 */
	public TransportWrapper(TTransport transport, boolean isOpen, String host, int port) {
		this.transport = transport;
		this.host = host;
		this.port = port;
		this.lastUserTime = new Date();
		
		//根据激活参数激活连接
		if (isOpen) {
			//需要捕获连接异常，可能由于网络或服务主机原因
			try {
				transport.open();
			} catch (TTransportException e) {
				System.err.println(host + ":" + port + " " + e.getMessage());
                isDead = true;
			}
		}
	}
	
	/**
	 * 构造方法，默认连接处于不激活状态
	 * 
	 * @param transport
	 * @param host
	 * @param port
	 */
	public TransportWrapper(TTransport transport, String host, int port) {
        this(transport, false, host, port);
    }
	
	/**
     * 当前transport是否可用
     *
     * @return
     */
    public boolean isAvailable() {
        return !isBusy && !isDead && transport.isOpen();
    }
    
    
	public TTransport getTransport() {
		return transport;
	}

	public void setTransport(TTransport transport) {
		this.transport = transport;
	}

	public boolean isBusy() {
		return isBusy;
	}

	public void setBusy(boolean isBusy) {
		this.isBusy = isBusy;
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

	public Date getLastUserTime() {
		return lastUserTime;
	}

	public void setLastUserTime(Date lastUserTime) {
		this.lastUserTime = lastUserTime;
	}
	
	public boolean isDead() {
		return isDead;
	}

	public void setDead(boolean isDead) {
		this.isDead = isDead;
	}
	@Override
	public String toString() {
		return "TransportWrapper [transport=" + transport + ", host=" + host + ", port=" + port + ", isBusy=" + isBusy
				+ ", isDead=" + isDead + ", lastUserTime=" + lastUserTime + "]";
	}
	
	
}
