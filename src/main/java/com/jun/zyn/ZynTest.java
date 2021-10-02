package com.jun.zyn;

import com.jun.spring.ZynAnnotationConfigApplicationContext;
import com.jun.zyn.service.OrderService;
import com.jun.zyn.service.UserService;
import com.jun.zyn.service.UserServiceInterface;
import com.sun.org.apache.xpath.internal.operations.Or;

/**
 * @Author: yijunjun
 * @Date: 2021/9/25 17:18
 */
public class ZynTest {
    public static void main(String[] args) {
        // 创建单例bean
        ZynAnnotationConfigApplicationContext context = new ZynAnnotationConfigApplicationContext(AppConfig.class);
        UserServiceInterface userService = (UserServiceInterface)context.getBean("userService");
        userService.test();

        System.out.println("-------------------");
        OrderService orderService = (OrderService)context.getBean("orderService");
        orderService.test();
    }
}
