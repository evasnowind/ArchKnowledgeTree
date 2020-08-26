package com.prayerlaputa.importtest.demo2;

/**
 * @author chenglong.yu
 * created on 2020/8/21
 */
public class UserServiceImpl implements UserService {
    @Override
    public int save(User user) {
        System.out.println("UserServiceImpl - save 方法调用");
        return 1;
    }
}
