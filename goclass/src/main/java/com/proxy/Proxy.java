package com.proxy;

import java.time.LocalDateTime;

public class Proxy implements ProxyService{
	
	private ProxyService proxyService;
	
	public Proxy(ProxyService proxyService) {
		this.proxyService = proxyService;
	}

	@Override
	public void say(String word) {
		System.out.println("proxy start..." + LocalDateTime.now());
		proxyService.say(word);
		System.out.println("proxy end...." + LocalDateTime.now());
	}

}
