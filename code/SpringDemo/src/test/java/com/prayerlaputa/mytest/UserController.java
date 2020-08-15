package com.prayerlaputa.mytest;

import lombok.Data;

@Data
public class UserController {

    @MyAutowire
    private UserService userService;


}
