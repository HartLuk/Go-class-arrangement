package com.proxy;

public class Target implements ProxyService{

	@Override
	public void say(String word) {
		System.out.println("target say " + word);
	}

}
