package test2_configcenter;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author chenglong.yu@brgroup.com
 * created on 2020/7/5
 */
public class TestConfig {

    ZooKeeper zk;


    @Before
    public void conn() {
        zk = ZKUtils.getZK();
    }

    @Test
    public void getConfig() {
        //需要首先判断目录是否存在, exist两大类、4种实现方式
        /*
        1、阻塞
        2、异步

        此处使用异步 回调方式
         */

        /*
        自定义的WatchCallback既是watch又是callback
         */
        WatchCallBack watchCallBack = new WatchCallBack();
        watchCallBack.setZk(zk);
        zk.exists("/AppConf", watchCallBack, watchCallBack, "abc");
    }

    @After
    public void closeZK() {
        try {
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
