package com.prayerlaputa.importtest.demo2;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.HashMap;

/**
 * @author chenglong.yu
 * created on 2020/8/21
 */
public class AppWithNoImport {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext configApplicationContext = new AnnotationConfigApplicationContext(UserConfigByConfiguration.class);
        UserService userService = configApplicationContext.getBean(UserServiceImpl.class);
        userService.save(null);
        configApplicationContext.close();
    }
}
