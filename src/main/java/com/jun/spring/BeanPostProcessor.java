package com.jun.spring;

/**
 * @Author: yijunjun
 * @Date: 2021/10/2 14:47
 */
public interface BeanPostProcessor {

    default Object postProcessBeforeInitialization(Object bean,String beanName){
        return bean;
    }

    default Object postProcessAfterInitialization(Object bean,String beanName){
        return bean;
    }
}
