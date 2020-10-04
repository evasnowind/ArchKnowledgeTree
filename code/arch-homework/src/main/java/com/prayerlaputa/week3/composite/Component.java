package com.prayerlaputa.week3.composite;

import java.util.concurrent.CompletableFuture;

/**
 * @author chenglong.yu
 * created on 2020/10/4
 */
public abstract class Component {

    /**
     * 控件所包含的值
     * 例如：对于Button，此处保存"登录"汉字
     */
    protected String val;
    /**
     * 控件名称
     */
    private String name;

    public Component(String name, String val) {
        this.name = name;
        this.val = val;
    }

    public abstract void print();

    public void print(String componentName, String val) {
        System.out.println("print " + componentName + "(" + val + ")");
    }

    /**
     * 此处add remove方法可以放在：
     * 方式1、Component基类中
     * 2、只放在WinForm/Frame等容器类中
     *
     * 优缺点：
     * 方式1：接口调用统一，但非容器类暴露了无用的add/remove接口（内部需要实现为空方法，保证与业务逻辑一致）
     * 方式2：接口调用不统一，客户端调用时，需要判断是哪种类型的Component
     *
     * 进一步分析可以参考这篇文章： https://www.cnblogs.com/adamjwh/p/9033547.html
     * @param component
     */
    public abstract void add(Component component);
    public abstract void remove(Component component);

    /**
     * 可以进一步抽象，模拟JDK AWT，传入Listener回调接口。
     * 这个不是重点，就懒得写了~.~
     */
//    public abstract void onClickEvent();
}
