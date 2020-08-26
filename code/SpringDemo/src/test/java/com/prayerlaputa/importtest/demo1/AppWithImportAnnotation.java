package com.prayerlaputa.importtest.demo1;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * @author chenglong.yu
 * created on 2020/8/21
 */
@ComponentScan
//@Import({Dog.class, Cat.class})
@Import({MyConfig.class})
public class AppWithImportAnnotation {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppWithImportAnnotation.class);
        System.out.println(context.getBean(Dog.class));
        System.out.println(context.getBean(Cat.class));
        context.close();
    }
}
