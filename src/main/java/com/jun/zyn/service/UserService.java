package com.jun.zyn.service;

import com.jun.spring.*;

/**
 * @Author: yijunjun
 * @Date: 2021/10/1 9:50
 */
@Component("userService")
@Scope("singleton")
public class UserService implements InitializingBean,UserServiceInterface, BeanNameAware {

    @Autowired
    private OrderService orderService;

    private String beanName;

    public void test(){
        System.out.println(orderService);
    }

    @Override
    public void afterPropertiesSet(){
        System.out.println("初始化");
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
        System.out.println("setBeanName = "+beanName);
    }
}
