package test2_configcenter;

import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author chenglong.yu@brgroup.com
 * created on 2020/7/5
 */
public class ZKUtils {

    private static ZooKeeper zk;

    //在传入ZooKeeper构造方法、创建ZK对象时，直接限定好路径
    private static String ZK_CLUSTER_ADDR = "62.234.80.253:2181,62.234.80.253:2182,62.234.80.253:2183/test";

    private static DefaultWatch watch = new DefaultWatch();

    private static CountDownLatch latch = new CountDownLatch(1);

    public static ZooKeeper getZK() {
        watch.setCountDownLatch(latch);
        try {
            zk = new ZooKeeper(ZK_CLUSTER_ADDR, 1000, watch);
            latch.await();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return zk;
    }
}
