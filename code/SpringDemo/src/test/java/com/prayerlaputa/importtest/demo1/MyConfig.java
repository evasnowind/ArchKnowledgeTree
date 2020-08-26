package com.prayerlaputa.importtest.demo1;

import com.prayerlaputa.importtest.demo1.Cat;
import com.prayerlaputa.importtest.demo1.Dog;
import org.springframework.context.annotation.Bean;

/**
 * @author chenglong.yu
 * created on 2020/8/21
 */

public class MyConfig {


    @Bean
    public Cat cat() {
        return new Cat();
    }

    @Bean
    public Dog dog() {
        return new Dog();
    }
}
