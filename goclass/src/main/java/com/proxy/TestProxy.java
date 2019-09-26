package com.proxy;

public class TestProxy {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Target target = new Target();
//		Proxy proxy = new Proxy(target);
//		proxy.say("i am a javacoder");
		ProxyHandler handler = new ProxyHandler();
		ProxyService service = (ProxyService) handler.bind(target);
		service.say("i am a javacoder");
	}

}
