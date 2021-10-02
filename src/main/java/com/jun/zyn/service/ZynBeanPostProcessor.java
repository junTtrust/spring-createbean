package com.jun.zyn.service;

import com.jun.spring.BeanPostProcessor;
import com.jun.spring.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @Author: yijunjun
 * @Date: 2021/10/2 14:49
 */
@Component
public class ZynBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if("userService".equals(beanName)){
            Object proxyInstance = Proxy.newProxyInstance(ZynBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    // aop
                    System.out.println("切面代理逻辑");
                    return method.invoke(bean,args);
                }
            });
            return proxyInstance;
        }
        System.out.println("初始化后="+beanName);
        return bean;
    }
}
