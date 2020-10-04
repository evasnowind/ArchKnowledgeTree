package com.prayerlaputa.week3.composite.impl;

import com.prayerlaputa.week3.composite.Component;

import java.util.ArrayList;

/**
 * @author chenglong.yu
 * created on 2020/10/4
 */
public class WinForm extends Component {

    private ArrayList<Component> componentList = new ArrayList<>();

    public WinForm(String name, String val) {
        super(name, val);
    }

    @Override
    public void print() {
        super.print(this.getClass().getSimpleName(), this.val);
        for (Component component : componentList) {
            component.print();
        }
    }

    @Override
    public void add(Component component) {
        componentList.add(component);
    }

    @Override
    public void remove(Component component) {
        componentList.add(component);
    }
}
