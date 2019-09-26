package com.rpc.threadPool;

import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TNonblockingTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import com.service.AsynCallback;
import com.service.ClassStrategyService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
 
 
public class PoolTest {
 
    public static void main(String[] args) throws Exception {
 
        //初始化一个连接池（poolsize=15,minsize=1,maxIdleSecond=5,checkInvervalSecond=10）
        final ThriftTransportPool pool = new ThriftTransportPool(15, 1, 5, 10, getServers());
 
 
        //模拟客户端调用
        createClients(pool);
 
        //等候清理空闲连接
        Thread.sleep(30000);
 
        //再模拟一批客户端，验证连接是否会重新增加
        createClients(pool);
 
        System.out.println("输入任意键退出...");
        System.in.read();
        //销毁连接池
        pool.destory();
 
    }
 
 
    private static void createClients(final ThriftTransportPool pool) throws Exception {
 
        //模拟5个client端
        int clientCount = 5;
 
        Thread thread[] = new Thread[clientCount];
        FutureTask<String> task[] = new FutureTask[clientCount];
 
        for (int i = 0; i < clientCount; i++) {
            task[i] = new FutureTask<String>(new Callable<String>() {
                public String call() throws Exception {
                    TSocket scoket = (TSocket) pool.get();//从池中取一个可用连接
                    //模拟调用RPC会持续一段时间
                    System.out.println(Thread.currentThread().getName() + " => " + pool.getTransportInfo(scoket));
                    

//               	 	TAsyncClientManager clientManager = new TAsyncClientManager();
//          
//                    TProtocolFactory tprotocol = new TBinaryProtocol.Factory();
//                    ClassStrategyService.AsyncClient asyncClient = new ClassStrategyService.AsyncClient(tprotocol, clientManager, scoket);
//          
//                    CountDownLatch latch = new CountDownLatch(1);
//                    AsynCallback callBack = new AsynCallback(latch);
//                    System.out.println("call method test start ...");
//                    asyncClient.test("1", "2", callBack);
//                    System.out.println("call method test .... end");
//                    boolean wait = latch.await(100, TimeUnit.SECONDS);
//                    System.out.println("latch.await =:" + wait);
                    
                    TTransport transport = new TFramedTransport(scoket);
                    TProtocol prot = new TBinaryProtocol(transport);
					ClassStrategyService.Client client = new ClassStrategyService.Client(prot );
                    
                    String string = client.test("1", "2");
                    System.out.println(string);
                    
                    
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    pool.release(scoket);//记得每次用完，要将连接释放（恢复可用状态）
                    return Thread.currentThread().getName() + " done.";
                }
            });
            thread[i] = new Thread(task[i], "Thread" + i);
        }
 
        //启用所有client线程
        for (int i = 0; i < clientCount; i++) {
            thread[i].start();
            Thread.sleep(10);
        }
 
        System.out.println("--------------");
 
        //等待所有client调用完成
        for (int i = 0; i < clientCount; i++) {
            System.out.println(task[i].get());
            System.out.println(pool);
            System.out.println("--------------");
            thread[i] = null;
        }
    }
 
    private static List<ServerInfo> getServers() {
        List<ServerInfo> servers = new ArrayList<ServerInfo>();
        servers.add(new ServerInfo("10.201.133.164", 8081));
//        servers.add(new ServerInfo("localhost", 8081));
        servers.add(new ServerInfo("localhost", 1002));//这一个故意写错的，模拟服务器挂了，连接不上的情景
        return servers;
    }
}
