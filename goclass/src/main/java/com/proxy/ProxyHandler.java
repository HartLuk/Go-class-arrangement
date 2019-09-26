package com.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;

/**
 * 动态代理，实现InvocationHandler的类,用于产生代理对象和进行回调处理
 * 
 * 调用流程：
 * -->生成目标对象
 * -->生成实现InvocationHandler的类的对象，称为中介对象吧
 * -->使用中介对象绑定目标对象，return一个代理对象(代码无需自己写，jdk生成，只需要相应参数)
 * -->代理对象调用与目标对象相同名字的方法，实际上调用的是经过加工的方法
 * -->经过加工的方法调用实现InvocationHandler的类的invoke方法
 * -->在invoke中调用目标对象的方法，调用前后可以进行逻辑处理
 * 
 * @author Administrator
 *
 */
public class ProxyHandler implements InvocationHandler{
	private Object target;
	
	public Object bind(Object object) {
		this.target = object;
		return Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), this);
	}
	
	
	@Override
	public Object invoke(Object obj, Method method, Object[] args) throws Throwable {
		System.out.println("dynaProxy start....." + LocalDateTime.now());
		method.invoke(target, args);
		System.out.println("dynaProxy end....." + LocalDateTime.now());
		
		return null;
	}

}
