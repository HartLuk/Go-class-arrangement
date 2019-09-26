package com.service;

import java.util.concurrent.CountDownLatch;

import org.apache.thrift.async.AsyncMethodCallback;


public class AsynCallback implements AsyncMethodCallback<String>{
       private CountDownLatch latch;
 
       public AsynCallback(CountDownLatch latch) {
           this.latch = latch;
       }
 
       @Override
       public void onComplete(String response) {
           System.out.println("onComplete");
           try {
              Thread.sleep(1000L * 1);
              System.out.println("AsynCall result =:" + response);
           } catch (Exception e) {
              e.printStackTrace();
           } finally {
              latch.countDown();
           }
       }
 
       @Override
       public void onError(Exception exception) {
           System.out.println("onError :" + exception.getMessage());
           latch.countDown();
       }
    }