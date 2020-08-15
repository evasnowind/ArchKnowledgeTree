package com.prayerlaputa.mytest;

import org.junit.Test;

import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

public class DemoTest {


    @Test
    public void testField() throws Exception {
        UserController userController = new UserController();
        Class<? extends UserController> clazz = userController.getClass();
//        Field[] declaredFields = clazz.getDeclaredFields();
//        Arrays.asList(declaredFields).stream().forEach(System.out::println);
        UserService userService = new UserService();

        Field userServiceField = clazz.getDeclaredField("userService");;
        //将访问标识符修改后，即可以访问private 变量
        userServiceField.setAccessible(true);
        String name = userServiceField.getName();
        name = name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
        String setMethodName = "set" + name;

        Method method = clazz.getMethod(setMethodName, UserService.class);
        method.invoke(userController, userService);


//        userServiceField.set(userController, userService);
        System.out.println(userController.getUserService());
    }

    @Test
    public void testMyAutowire() throws Exception {
        UserController userController = new UserController();
        Class<? extends UserController> clazz = userController.getClass();
//        Field[] declaredFields = clazz.getDeclaredFields();
//        Arrays.asList(declaredFields).stream().forEach(System.out::println);
        UserService userService = new UserService();



        Stream.of(clazz.getDeclaredFields()).forEach(field -> {
            field.setAccessible(true);

        });

        Field userServiceField = clazz.getDeclaredField("userService");;
        //将访问标识符修改后，即可以访问private 变量
        userServiceField.setAccessible(true);
        String name = userServiceField.getName();
        name = name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
        String setMethodName = "set" + name;

        Method method = clazz.getMethod(setMethodName, UserService.class);
        method.invoke(userController, userService);


//        userServiceField.set(userController, userService);
        System.out.println(userController.getUserService());
    }
}
