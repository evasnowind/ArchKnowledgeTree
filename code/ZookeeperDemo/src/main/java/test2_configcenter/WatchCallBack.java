package test2_configcenter;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
 * @author chenglong.yu@brgroup.com
 * created on 2020/7/5
 */
public class WatchCallBack implements Watcher, AsyncCallback.StatCallback, AsyncCallback.DataCallback {

    ZooKeeper zk;

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

    /**
     * watch 监测节点，保证能持续检测节点
     *
     * @param watchedEvent
     */
    public void process(WatchedEvent watchedEvent) {

    }

    /**
     * 来自StatCallback
     * 判断状态
     *
     *
     * @param rc
     * @param path
     * @param ctx
     * @param stat
     */
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        if (stat != null) {
            /*
            stat不为空，说明节点存在，此时就可以调用getData获取数据
            getData需要有zk，因而在调用
             */

        }
    }

    /**
     * 来自DataCallback
     *
     * byte[] 拿到数据
     *
     * @param rc
     * @param path
     * @param ctx
     * @param data
     * @param stat
     */
    public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {

    }
}
