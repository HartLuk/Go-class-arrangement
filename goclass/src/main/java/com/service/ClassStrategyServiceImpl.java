package com.service;

import org.apache.thrift.TException;

public class ClassStrategyServiceImpl implements ClassStrategyService.Iface{

	public ClassStrategyServiceImpl() {
	}
	
	public String test(String chart1, String chatr2) throws TException {

		System.out.println(chart1 + ", " + chatr2);
		return "123";
	}

}
