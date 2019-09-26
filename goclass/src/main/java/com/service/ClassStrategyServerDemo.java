package com.service;

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TServerSocket;

public class ClassStrategyServerDemo {
    public static final int SERVER_PORT = 8081;
 
    public void startServer() {
        try {
            System.out.println(" ClassStrategyServe TSimpleServer start ....");
 
//            TProcessor tprocessor = new ClassStrategyService.Processor<ClassStrategyService.Iface>(new ClassStrategyServiceImpl());
//            TServerSocket serverTransport = new TServerSocket(SERVER_PORT);
//            TServer.Args tArgs = new TServer.Args(serverTransport);
//            tArgs.processor(tprocessor);
//            tArgs.protocolFactory(new TBinaryProtocol.Factory());
//            // tArgs.protocolFactory(new TCompactProtocol.Factory());
//            // tArgs.protocolFactory(new TJSONProtocol.Factory());
//            TServer server = new TSimpleServer(tArgs);
//            server.serve();
            
            TNonblockingServerSocket tnbSocketTransport = new TNonblockingServerSocket(SERVER_PORT);
            TProcessor tprocessor = new ClassStrategyService.Processor<ClassStrategyService.Iface>(new ClassStrategyServiceImpl());
            TNonblockingServer.Args tnbArgs = new TNonblockingServer.Args(tnbSocketTransport);
            tnbArgs.processor(tprocessor);
            
            // 使用非阻塞式IO，服务端和客户端需要指定TFramedTransport数据传输的方式
            tnbArgs.transportFactory(new TFramedTransport.Factory());
            
            tnbArgs.protocolFactory(new TBinaryProtocol.Factory());
 
            TServer server = new TNonblockingServer(tnbArgs);
            server.serve();
            
        } catch (Exception e) {
            System.out.println("Server start error!!!");
            e.printStackTrace();
        }
    }
 
    public static void main(String[] args) {
    	ClassStrategyServerDemo server = new ClassStrategyServerDemo();
        server.startServer();
    }
}