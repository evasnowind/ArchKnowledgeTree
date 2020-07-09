package com.prayerlaputa.test1_simple;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author chenglong.yu@brgroup.com
 * created on 2020/7/4
 */
public class SimpleTests {



    public static void main(String[] args) throws Exception {

        System.out.println("开始");

        final CountDownLatch latch = new CountDownLatch(1);

        final ZooKeeper zk = new ZooKeeper("62.234.80.253:2181,62.234.80.253:2182,62.234.80.253:2183", 3000, new Watcher() {

            public void process(WatchedEvent watchedEvent) {
                Event.KeeperState keeperState = watchedEvent.getState();
                Event.EventType eventType = watchedEvent.getType();
                String path = watchedEvent.getPath();

                System.out.println("new zk watch: " + watchedEvent);

                switch (keeperState) {
                    case Unknown:
                        break;
                    case Disconnected:
                        break;
                    case NoSyncConnected:
                        break;
                    case SyncConnected:
                        System.out.println("connected state...");
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
                    default:
                        throw new IllegalStateException("Unexpected value: " + keeperState);
                }

                switch (eventType) {
                    case None:
                        break;
                    case NodeCreated:
                        break;
                    case NodeDeleted:
                        break;
                    case NodeDataChanged:
                        break;
                    case NodeChildrenChanged:
                        break;
                    case DataWatchRemoved:
                        break;
                    case ChildWatchRemoved:
                        break;
                    case PersistentWatchRemoved:
                        break;
                }
            }
        });

        /*
        构造ZooKeeper对象时，构造方法并没有阻塞。为了保证在执行下面逻辑之前，zk已经初始化、完成连接操作，
        使用CountDownLatch进行阻塞
         */
        latch.await();

        ZooKeeper.States zkState = zk.getState();
        switch (zkState) {
            case CONNECTING:
                System.out.println("zk connecting...");
                break;
            case ASSOCIATING:
                break;
            case CONNECTED:
                System.out.println("zk connected...");
                break;
            case CONNECTEDREADONLY:
                break;
            case CLOSED:
                break;
            case AUTH_FAILED:
                break;
            case NOT_CONNECTED:
                break;
        }


        final String testPath = "/test1";
        String pathName = zk.create(testPath, "test1 data".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

        final Stat nodeStat = new Stat();
        final byte[] node = zk.getData(testPath, new Watcher() {
            public void process(WatchedEvent watchedEvent) {
                System.out.println("getData watch:" + watchedEvent.toString());
                try {
                    /*
                    此处务必注意一点：ZK中Watch是一次性触发，如果某个节点上的Watch已经被触发过，后续还想
                    继续接收这个变化时间，必须重新设置Watch。
                    而重新设置Watch仍需要调用读方法、设置Watch。
                    主要使用getData exists方法设置Watch
                    注意传参不一样，设置的Watcher也不一样，这非常重要，因为如果设置错、将会触发不同的Watcher！！！

                    以本程序为例，
                    zk.getData(testPath, this, nodeStat);
                    注册的仍是当前匿名Watcher类对象，也就是注册同样的Watcher、后续采用相同的处理逻辑。
                    若重复调用两次（比如下面的逻辑）

        Stat stat1 = zk.setData(testPath, "test1 newdata".getBytes(), 0);
        Stat stat2 = zk.setData(testPath, "test1 newdata2".getBytes(), stat1.getVersion());

                    日志输出：
new zk watch: WatchedEvent state:SyncConnected type:None path:null
connected state...
zk connected...
/test1 node data=test1 data
getData watch:WatchedEvent state:SyncConnected type:NodeDataChanged path:/test1
getData watch:WatchedEvent state:SyncConnected type:NodeDataChanged path:/test1

                    此处getData还有不同的实现，比如
                    zk.getData(testPath,true, nodeStat);
                    再看输出，可以发现日志如下：
new zk watch: WatchedEvent state:SyncConnected type:None path:null
connected state...
zk connected...
/test1 node data=test1 data
getData watch:WatchedEvent state:SyncConnected type:NodeDataChanged path:/test1
new zk watch: WatchedEvent state:SyncConnected type:NodeDataChanged path:/test1
connected state...

                也就是说，此时生效的是zk初始化时传入的默认default watcher!!!
                若传false，即zk.getData(testPath,false, nodeStat);  则是告知zk后续不用加Wather，第二次执行时也就不会触发任何日志。
new zk watch: WatchedEvent state:SyncConnected type:None path:null
connected state...
zk connected...
/test1 node data=test1 data
getData watch:WatchedEvent state:SyncConnected type:NodeDataChanged path:/test1

                     */
                    zk.getData(testPath, this, nodeStat);
//                    zk.getData(testPath,true, nodeStat);
//                    zk.getData(testPath,false, nodeStat);
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, nodeStat);

        System.out.println(testPath + " node data=" + new String(node));

        Stat stat1 = zk.setData(testPath, "test1 newdata".getBytes(), 0);
        Stat stat2 = zk.setData(testPath, "test1 newdata2".getBytes(), stat1.getVersion());

        zk.getData(testPath, false, new AsyncCallback.DataCallback() {
            public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
                System.out.println("-------async call back----------");
                System.out.println("async context=" + ctx + " data=" + new String(data));
            }
        }, "abcd");

        System.out.println("zk test over.");

        TimeUnit.MINUTES.sleep(1);
    }
}
