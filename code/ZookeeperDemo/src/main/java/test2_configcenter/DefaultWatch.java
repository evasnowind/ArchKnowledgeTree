package test2_configcenter;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.concurrent.CountDownLatch;

/**
 * @author chenglong.yu@brgroup.com
 * created on 2020/7/5
 */
public class DefaultWatch implements Watcher {

    CountDownLatch latch;

    public void setCountDownLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    public void process(WatchedEvent watchedEvent) {
        System.out.println(watchedEvent);

        switch (watchedEvent.getState()) {
            case Unknown:
                break;
            case Disconnected:
                break;
            case NoSyncConnected:
                break;
            case SyncConnected:
                System.out.println("连接zk成功");
                latch.countDown();
                break;
            case AuthFailed:
                break;
            case ConnectedReadOnly:
                break;
            case SaslAuthenticated:
                break;
            case Expired:
                break;
            case Closed:
                break;
        }
    }
}
