package com.prayerlaputa.importtest;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;


/**
 * @author chenglong.yu
 * created on 2020/8/21
 */
public class MyClass implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata annotationMetadata) {
        return new String[]{"com.prayerlaputa.importtest"};
    }



}
