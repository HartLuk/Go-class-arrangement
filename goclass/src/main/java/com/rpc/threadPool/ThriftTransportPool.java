package com.rpc.threadPool;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

/**
 * 封装连接池，对外提供操作连接的方法和对连接进行内部管理
 * 
 * @author Administrator
 *
 */
public class ThriftTransportPool {
	/**
	 * 资源控制，用于设定池中可用的资源个数
	 */
	Semaphore access = null;
	
	/**
	 * 连接数组，保存所有连接
	 */
	TransportWrapper[] pool = null;
	
	/**
	 * 连接池大小，默认1
	 */
	int poolSzie = 1;
	
	/**
	 * 最小的已经激活的连接数
	 */
	int minActive = 1;
	
	/**
	 * 最大空闲时间，单位秒，超过该时间的空闲连接将会被关闭
	 */
	int maxIdleSecond = 300;
	
	/**
	 * 每隔多少时间对连接进行空闲检查，单位秒，默认60秒
	 */
	int cheakIntervalSecond = 60;
	
	/**
	 * 是否开启进行空闲检查，默认开启
	 */
	boolean allowCheck = true;
	
	/**
	 * 服务端信息列表
	 */
	List<ServerInfo> serverInfos;
	
	/**
	 * 检查空闲子线程
	 */
	Thread checkThread = null;
	
	/**
	 * 连接池状态，是否可用
	 */
	boolean isAvailable = true;
	
	/**
	 * 构造方法，如果输入不满足预期，则进行默认设置
	 * 
	 * @param poolSzie 默认1
	 * @param minActive 默认1
	 * @param maxIdleSecond 默认300
	 * @param cheakIntervalSecond 默认60
	 * @param serverInfos
	 */
	public ThriftTransportPool(int poolSzie, int minActive, int maxIdleSecond, int cheakIntervalSecond,
			List<ServerInfo> serverInfos) {
		if (poolSzie <= 0) {
			poolSzie = 1;
		}
		if (minActive <= 0) {
			minActive = 1;
		}
		if (minActive > poolSzie) {
			minActive = poolSzie;
		}
		
		if (maxIdleSecond <= 0) {
			/**
			 * 如果设置为0，在检查空闲时，发现空闲连接会马上关闭
			 * */
			maxIdleSecond = 300; 
		}
		
		if (cheakIntervalSecond <= 0) {
			cheakIntervalSecond = 60;
		}
		
		this.poolSzie = poolSzie;
		this.minActive = minActive;
		this.maxIdleSecond = maxIdleSecond;
		this.cheakIntervalSecond = cheakIntervalSecond;
		this.serverInfos = serverInfos;
		
		init();													//初始化连接池
		check();												//开启检查空闲连接线程
	}

	/**
	 * 构造方法，使用默认值创建连接池
	 * 
	 * @default maxIdleSecond = 300
	 * @default cheakIntervalSecond = 60
	 * 
	 * @param poolSzie
	 * @param minActive
	 * @param serverInfos
	 */
	public ThriftTransportPool(int poolSzie, int minActive, List<ServerInfo> serverInfos) {
		this(poolSzie, minActive, 300, 60, serverInfos);
	}
	
	/**
	 * 构造方法，使用默认值创建连接池
	 * 
	 * @default minActive = 1
	 * @default maxIdleSecond = 300
	 * @default cheakIntervalSecond = 60
	 * 
	 * @param poolSzie
	 * @param serverInfos
	 */
	public ThriftTransportPool(int poolSize, List<ServerInfo> serverInfos) {
        this(poolSize, 1, 300, 60, serverInfos);
    }
	
	/**
	 * 构造方法，使用默认值创建连接池，根据服务端列表的大小设定连接池的大小
	 * 
	 * @default minActive = 1
	 * @default maxIdleSecond = 300
	 * @default cheakIntervalSecond = 60
	 * @default poolSzie = serverInfos.size()
	 * 
	 * @param serverInfos
	 */
    public ThriftTransportPool(List<ServerInfo> serverInfos) {
        this(serverInfos.size(), 1, 300, 60, serverInfos);
    }
    
    /**
     * 检查空闲连接，
     */
	private void check() {

		checkThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (allowCheck) {
					System.out.println("开始检查连接。。。");
					for (int i = 0; i < pool.length; i++) {
						//当连接处于可用状态
						if (pool[i].isAvailable() && pool[i].getLastUserTime() != null) {
							long idleTmie = new Date().getTime() - pool[i].getLastUserTime().getTime();
							
							//检查是否大于最大空闲时间
							if (idleTmie > maxIdleSecond * 1000) {
								 // 检查激活的连接是否大于最小激活连接数,大于则进行关闭，减少资源浪费，否则不关闭
								if (getActiveCount() > minActive) {
									pool[i].getTransport().close();
									pool[i].setBusy(false);
									System.out.println(pool[i].hashCode() + 
											", " + 
											pool[i].getHost() + ":" + 
											pool[i].getPort() + 
											",连接超时已被断开！");
									
								}
							}
						}
					}
					
					System.out.println("当前活动连接数：" + getActiveCount());
                    try {
                        Thread.sleep(cheakIntervalSecond * 1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
				}
			}
		});
	}

	/**
	 * @return 返回已激活的连接数
	 */
	public int getActiveCount() {
		int count = 0;
		for (int i = 0; i < pool.length; i++) {
			if (!pool[i].isDead() && pool[i].getTransport().isOpen()) {
				count++;
			}
		}
		return count;
	}

	/**
	 * 连接池初始化
	 */
	private void init() {
		if (serverInfos.size() > poolSzie) {
			throw new IllegalArgumentException("服务端列表项个数大于连接池大小，会导致某些服务端无法获得连接，请重新进行设置！");
		}
		access = new Semaphore(poolSzie);
		pool = new TransportWrapper[poolSzie];
		
		/**
		 * 根据poolSize进行实例化TransportWrapper，
		 * 由于服务端可能不同并且无法对poolSize进行整除，
		 * 下面只保证了都能够和每一个服务端至少有一个连接,但是不保证均匀分配
		 */
		int size = serverInfos.size();
		for (int i = 0; i < pool.length; i++) {
			int j = i % size;
			TSocket socket = new TSocket(serverInfos.get(j).getHost(), serverInfos.get(j).getPort());
			
			//激活minActive个连接
			if (i < minActive) {
				pool[i] = new TransportWrapper(socket, true, serverInfos.get(j).getHost(), serverInfos.get(j).getPort());
			}else {
				pool[i] = new TransportWrapper(socket, serverInfos.get(j).getHost(), serverInfos.get(j).getPort());
			}
		}
	}
	
	/**
	 * 从池中获取一个可用的连接
	 * @return
	 */
	public TTransport get() {
		try {
			if (access.tryAcquire(3, TimeUnit.SECONDS)) {
				synchronized (this) {
					for (int i = 0; i < pool.length; i++) {
						if (pool[i].isAvailable()) {
							pool[i].setBusy(true);
							pool[i].setLastUserTime(new Date());
							return pool[i].getTransport();
						}
					}
					
					//当上面操作没有成功返回连接，可能是已激活的连接已被占用完，需要激活一个新的连接，进行尝试激活操作
					for (int i = 0; i < pool.length; i++) {
						
						//激活过程可能连接被服务端释放掉了或网络问题，需要进行异常捕获
						try {
							if (!pool[i].isBusy() && !pool[i].isDead() && !pool[i].getTransport().isOpen()) {
								pool[i].getTransport().open();
								pool[i].setBusy(true);
								pool[i].setLastUserTime(new Date());
								return pool[i].getTransport();
							}
						}catch (Exception e) {
							System.err.println(pool[i].getHost() + ":" + pool[i].getPort() + " " + e.getMessage());
							//需要进行重连接
							pool[i].setDead(true);
						}
					}
					
					//获得和激活连接失败
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("can not get available client");
		}
		
		throw new RuntimeException("没有可用的连接，请稍后再试。。。");
	}
	
	/**
	 * 释放连接，需要进行手动释放
	 * 
	 * @param transport
	 */
	public void release(TTransport transport) {
		if (transport == null) {
			throw new NullPointerException("TTransport transport is null");
		}
		boolean release = false;
		synchronized (this) {
			for (int i = 0; i < pool.length; i++) {
				if (transport == pool[i].getTransport()) {
					pool[i].setBusy(false);
					release = true;
					break;
				}
			}
		}
		
		if (release) {
			access.release();
		}else {
			//执行该段代码表示可能传入的连接参数，不属于该连接池
			throw new IllegalArgumentException("该连接资源不属于这个连接池，释放失败。。。");
		}
	}
	
	/**
	 * 销毁连接池
	 */
	public void destory() {
		if (pool != null) {
			for (int i = 0; i < pool.length; i++) {
				pool[i].getTransport().close();
			}
		}
		
		allowCheck = false;		//检查空闲线程将会在下一次检查时运行结束
		
		checkThread = null;		//释放资源
		
		isAvailable = false;	//修改连接池状态
		
		System.out.println("连接池已被销毁！");
	}
	
	/**
     * 获取当前繁忙状态的连接数
     *
     * @return
     */
    public int getBusyCount() {
        int count = 0;
        for (int i = 0; i < pool.length; i++) {
            if (!pool[i].isDead() && pool[i].isBusy()) {
            	count++;
            }
        }
        return count;
    }
    
    /**
     * 获取当前死亡状态的连接数
     *
     * @return
     */
    public int getDeadCount() {
        int count = 0;
        for (int i = 0; i < pool.length; i++) {
            if (pool[i].isDead()) {
            	count++;
            }
        }
        return count;
    }
    
    /**
     * 获取连接池的状态
     * 
     * @return
     */
    public boolean getPoolStatus() {
		return isAvailable;
	}
	
    /**
     * 获取某个连接的信息
     */
    public String getTransportInfo(TTransport transport) {
		if (transport != null) {
			for (int i = 0; i < pool.length; i++) {
				if (transport == pool[i].getTransport()) {
					return pool[i].toString();
				}
			}
		}
		return "";
    }

	@Override
	public String toString() {
		return "ThriftTransportPool [poolSzie=" + poolSzie + ", minActive=" + minActive + ", maxIdleSecond="
				+ maxIdleSecond + ", cheakIntervalSecond=" + cheakIntervalSecond + ", allowCheck=" + allowCheck
				+ ", serverInfos=" + serverInfos + ", isAvailable=" + isAvailable
				+ ", activeCount:" + getActiveCount() + ", busyCount:" + getBusyCount() + ", deadCount:" + getDeadCount() + "]";
	}
    
    
}
