package com.prayerlaputa.week3.composite.impl;

import com.prayerlaputa.week3.composite.Component;

/**
 * @author chenglong.yu
 * created on 2020/10/4
 */
public class Button extends Component {


    public Button(String name, String val) {
        super(name, val);
    }

    @Override
    public void print() {
        super.print(this.getClass().getSimpleName(), this.val);
    }

    @Override
    public void add(Component component) {

    }

    @Override
    public void remove(Component component) {

    }

    public static void main(String[] args) {
        Button btn = new Button("username", "用户名");
        btn.print();
    }
}
