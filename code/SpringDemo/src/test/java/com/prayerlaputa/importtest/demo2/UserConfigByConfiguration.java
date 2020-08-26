package com.prayerlaputa.importtest.demo2;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author chenglong.yu
 * created on 2020/8/21
 */
@Configuration
@Import({UserServiceImpl.class})
public class UserConfigByConfiguration {
}
