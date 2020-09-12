package com.prayerlaputa.util;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

import java.util.List;

/**
 * @author chenglong.yu
 * created on 2020/8/11
 */
public class ConfigUtil {


    public static final String VPS_SERVER_IP = "vpsServer.ip";
    public static final String VPS_SERVER_ROCKET_MQ_IP_PORT = "vpsServer.rocketmq.ip.port";
    public static final String VPS_SERVER_ROCKETMQ_PORT = "vpsServer.rocketmq.port";

    private static String fileName = "democonfig.properties";
    private static PropertiesConfiguration cfg = null;

    static {
        try {
            cfg = new PropertiesConfiguration(fileName);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        // 当文件的内容发生改变时，配置对象也会刷新
        cfg.setReloadingStrategy(new FileChangedReloadingStrategy());
    }

    /**
     * 从配置文件中读取数据，为了避免泄露个人信息，此处通过gitignore的方式忽略了配置文件
     * @param key
     * @return
     */
    public static String getConfigFromFile(String key){
        return cfg.getString(key);
    }

    public static void main(String[] args) {
        System.out.println(ConfigUtil.getConfigFromFile(VPS_SERVER_IP));
        System.out.println(ConfigUtil.getStringValue(VPS_SERVER_ROCKET_MQ_IP_PORT));
    }

    /**
     * 读String
     * @param key
     * @return
     */
    public static String getStringValue(String key) {
        return cfg.getString(key);
    }

    /**
     * 读int
     * @param key
     * @return
     */
    public static int getIntValue(String key) {
        return cfg.getInt(key);
    }

    /**
     * 读boolean
     * @param key
     * @return
     */
    public static boolean getBooleanValue(String key) {
        return cfg.getBoolean(key);
    }
    /**
     * 读List
     */
    public static List<?> getListValue(String key) {
        return cfg.getList(key);
    }
    /**
     * 读数组
     */
    public static String[] getArrayValue(String key) {
        return cfg.getStringArray(key);
    }
}
