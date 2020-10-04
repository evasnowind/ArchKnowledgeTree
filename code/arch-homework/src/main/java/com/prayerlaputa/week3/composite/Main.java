package com.prayerlaputa.week3.composite;

import com.prayerlaputa.week3.composite.impl.Button;
import com.prayerlaputa.week3.composite.impl.CheckBox;
import com.prayerlaputa.week3.composite.impl.Frame;
import com.prayerlaputa.week3.composite.impl.Label;
import com.prayerlaputa.week3.composite.impl.LinkLabel;
import com.prayerlaputa.week3.composite.impl.PasswordBox;
import com.prayerlaputa.week3.composite.impl.Picture;
import com.prayerlaputa.week3.composite.impl.TextBox;
import com.prayerlaputa.week3.composite.impl.WinForm;

/**
 * @author chenglong.yu
 * created on 2020/10/4
 */
public class Main {
    public static void main(String[] args) {
        WinForm winForm = new WinForm("main", "WINDOW窗口");
        winForm.add(new Picture("logo", "LOGO图片"));

        Frame frame = new Frame("loginFrame", "FRAME1");
        frame.add(new Label("usernameLabel", "用户名"));
        frame.add(new TextBox("usernameTextBox", "文本框"));
        frame.add(new Label("passwordLabel", "密码"));
        frame.add(new PasswordBox("passwordBox", "密码框"));
        frame.add(new CheckBox("rememberMeCheck", "复选框"));
        frame.add(new TextBox("rememberMeBox", "记住用户名"));
        frame.add(new LinkLabel("forgotLabel", "忘记密码"));

        winForm.add(frame);
        winForm.add(new Button("loginButton", "登录"));
        winForm.add(new Button("regButton", "注册"));

        winForm.print();
    }
}
