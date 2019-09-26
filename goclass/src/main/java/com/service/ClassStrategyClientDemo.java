package com.service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.thrift.TException;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TNonblockingTransport;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.rpc.threadPool.ServerInfo;
import com.rpc.threadPool.ThriftTransportPool;
public class ClassStrategyClientDemo {
	//10.201.133.164
     public static final String SERVER_IP = "10.201.133.164";
     public static final int SERVER_PORT = 8081;
     public static final int TIMEOUT = 100000;
 
 
     public void startClient(ThriftTransportPool pool) throws IOException, InterruptedException {
//        TTransport transport = null;
//        try {
//        	 TAsyncClientManager clientManager = new TAsyncClientManager();
//             transport = new TNonblockingSocket(SERVER_IP,SERVER_PORT, TIMEOUT);
//   
//             TProtocolFactory tprotocol = new TBinaryProtocol.Factory();
//             ClassStrategyService.AsyncClient asyncClient = new ClassStrategyService.AsyncClient(tprotocol, clientManager, (TNonblockingTransport) transport);
//             System.out.println(clientName + " start .....");
//   
//             CountDownLatch latch = new CountDownLatch(1);
//             AsynCallback callBack = new AsynCallback(latch);
//             System.out.println("call method test start ...");
//             asyncClient.test("1", "2", callBack);
//             System.out.println("call method test .... end");
//             boolean wait = latch.await(100, TimeUnit.SECONDS);
//             System.out.println("latch.await =:" + wait);
//          } catch (TTransportException e) {
//               e.printStackTrace();
//          } catch (TException e) {
//              e.printStackTrace();
//          } finally {
//               if (null != transport) {
//                  transport.close();
//             }
//         }
    	 
    	 
		try {
			TTransport scoket = pool.get();
			TProtocol prot = new TBinaryProtocol(scoket);
			ClassStrategyService.Client client = new ClassStrategyService.Client(prot);
			
			String string = client.test("1", "2");
			System.out.println(string);
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     }  
 

  public static void main(String[] args) throws IOException, InterruptedException {
//	  ExecutorService service = Executors.newCachedThreadPool();
//	  
//	  for (int i = 0; i < 4; i++) {
//		  service.execute(new Runnable() {
//			  
//			  @Override
//			  public void run() {
//				  // TODO Auto-generated method stub
//				  ClassStrategyClientDemo client = new ClassStrategyClientDemo();
//				  try {
//					client.startClient("client " + UUID.randomUUID());
//					System.out.println("=========");
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			  }
//		  });
//		
//	  }
	  ThriftTransportPool pool = new ThriftTransportPool(15, 1, 5, 10, getServers());
	  ClassStrategyClientDemo client = new ClassStrategyClientDemo();
	  client.startClient(pool);
//	  for (int i = 0; i < 4; i++) {
//	  }
	  
  }
  
  private static List<ServerInfo> getServers() {
      List<ServerInfo> servers = new ArrayList<ServerInfo>();
      servers.add(new ServerInfo("10.201.133.164", 8081));
//      servers.add(new ServerInfo("localhost", 8081));
      servers.add(new ServerInfo("localhost", 1002));//这一个故意写错的，模拟服务器挂了，连接不上的情景
      return servers;
  }
}