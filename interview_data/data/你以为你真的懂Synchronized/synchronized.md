# 用户态与内核态

JDK早期，synchronized 叫做重量级锁， 因为申请锁资源必须通过kernel, 系统调用

```assembly
;hello.asm
;write(int fd, const void *buffer, size_t nbytes)

section data
    msg db "Hello", 0xA
    len equ $ - msg

section .text
global _start
_start:

    mov edx, len
    mov ecx, msg
    mov ebx, 1 ;文件描述符1 std_out
    mov eax, 4 ;write函数系统调用号 4
    int 0x80

    mov ebx, 0
    mov eax, 1 ;exit函数系统调用号
    int 0x80

```



# CAS



Compare And Swap (Compare And Exchange) / 自旋 / 自旋锁 / 无锁 （无重量锁）

因为经常配合循环操作，直到完成为止，所以泛指一类操作

cas(v, a, b) ，变量v，期待值a, 修改值b

ABA问题，你的女朋友在离开你的这段儿时间经历了别的人，自旋就是你空转等待，一直等到她接纳你为止

解决办法（版本号 AtomicStampedReference），基础类型简单值不需要版本号

# Unsafe

AtomicInteger:

```java
public final int incrementAndGet() {
        for (;;) {
            int current = get();
            int next = current + 1;
            if (compareAndSet(current, next))
                return next;
        }
    }

public final boolean compareAndSet(int expect, int update) {
        return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
    }
```

Unsafe:

```java
public final native boolean compareAndSwapInt(Object var1, long var2, int var4, int var5);
```

运用：

```java
package com.mashibing.jol;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class T02_TestUnsafe {

    int i = 0;
    private static T02_TestUnsafe t = new T02_TestUnsafe();

    public static void main(String[] args) throws Exception {
        //Unsafe unsafe = Unsafe.getUnsafe();

        Field unsafeField = Unsafe.class.getDeclaredFields()[0];
        unsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe) unsafeField.get(null);

        Field f = T02_TestUnsafe.class.getDeclaredField("i");
        long offset = unsafe.objectFieldOffset(f);
        System.out.println(offset);

        boolean success = unsafe.compareAndSwapInt(t, offset, 0, 1);
        System.out.println(success);
        System.out.println(t.i);
        //unsafe.compareAndSwapInt()
    }
}
```

jdk8u: unsafe.cpp:

cmpxchg = compare and exchange

```c++
UNSAFE_ENTRY(jboolean, Unsafe_CompareAndSwapInt(JNIEnv *env, jobject unsafe, jobject obj, jlong offset, jint e, jint x))
  UnsafeWrapper("Unsafe_CompareAndSwapInt");
  oop p = JNIHandles::resolve(obj);
  jint* addr = (jint *) index_oop_from_field_offset_long(p, offset);
  return (jint)(Atomic::cmpxchg(x, addr, e)) == e;
UNSAFE_END
```

jdk8u: atomic_linux_x86.inline.hpp **93行**

is_MP = Multi Processor  

```c++
inline jint     Atomic::cmpxchg    (jint     exchange_value, volatile jint*     dest, jint     compare_value) {
  int mp = os::is_MP();
  __asm__ volatile (LOCK_IF_MP(%4) "cmpxchgl %1,(%3)"
                    : "=a" (exchange_value)
                    : "r" (exchange_value), "a" (compare_value), "r" (dest), "r" (mp)
                    : "cc", "memory");
  return exchange_value;
}
```

jdk8u: os.hpp is_MP()

```c++
  static inline bool is_MP() {
    // During bootstrap if _processor_count is not yet initialized
    // we claim to be MP as that is safest. If any platform has a
    // stub generator that might be triggered in this phase and for
    // which being declared MP when in fact not, is a problem - then
    // the bootstrap routine for the stub generator needs to check
    // the processor count directly and leave the bootstrap routine
    // in place until called after initialization has ocurred.
    return (_processor_count != 1) || AssumeMP;
  }
```

jdk8u: atomic_linux_x86.inline.hpp

```c++
#define LOCK_IF_MP(mp) "cmp $0, " #mp "; je 1f; lock; 1: "
```

最终实现：

cmpxchg = cas修改变量值

```assembly
lock cmpxchg 指令
```

硬件：

lock指令在执行后面指令的时候锁定一个北桥信号

（不采用锁总线的方式）



# markword

# 工具：JOL = Java Object Layout

```xml
<dependencies>
        <!-- https://mvnrepository.com/artifact/org.openjdk.jol/jol-core -->
        <dependency>
            <groupId>org.openjdk.jol</groupId>
            <artifactId>jol-core</artifactId>
            <version>0.9</version>
        </dependency>
    </dependencies>
```



jdk8u: markOop.hpp

```java
// Bit-format of an object header (most significant first, big endian layout below):
//
//  32 bits:
//  --------
//             hash:25 ------------>| age:4    biased_lock:1 lock:2 (normal object)
//             JavaThread*:23 epoch:2 age:4    biased_lock:1 lock:2 (biased object)
//             size:32 ------------------------------------------>| (CMS free block)
//             PromotedObject*:29 ---------->| promo_bits:3 ----->| (CMS promoted object)
//
//  64 bits:
//  --------
//  unused:25 hash:31 -->| unused:1   age:4    biased_lock:1 lock:2 (normal object)
//  JavaThread*:54 epoch:2 unused:1   age:4    biased_lock:1 lock:2 (biased object)
//  PromotedObject*:61 --------------------->| promo_bits:3 ----->| (CMS promoted object)
//  size:64 ----------------------------------------------------->| (CMS free block)
//
//  unused:25 hash:31 -->| cms_free:1 age:4    biased_lock:1 lock:2 (COOPs && normal object)
//  JavaThread*:54 epoch:2 cms_free:1 age:4    biased_lock:1 lock:2 (COOPs && biased object)
//  narrowOop:32 unused:24 cms_free:1 unused:4 promo_bits:3 ----->| (COOPs && CMS promoted object)
//  unused:21 size:35 -->| cms_free:1 unused:7 ------------------>| (COOPs && CMS free block)
```





# synchronized的横切面详解

1. synchronized原理
2. 升级过程
3. 汇编实现
4. vs reentrantLock的区别

## java源码层级

synchronized(o) 

## 字节码层级

monitorenter moniterexit

## JVM层级（Hotspot）

```java
package com.mashibing.insidesync;

import org.openjdk.jol.info.ClassLayout;

public class T01_Sync1 {
  

    public static void main(String[] args) {
        Object o = new Object();

        System.out.println(ClassLayout.parseInstance(o).toPrintable());
    }
}
```

```java
com.mashibing.insidesync.T01_Sync1$Lock object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4   (object header)  05 00 00 00 (00000101 00000000 00000000 00000000) (5)
      4     4   (object header)  00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4   (object header)  49 ce 00 20 (01001001 11001110 00000000 00100000) (536923721)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total
```

```java
com.mashibing.insidesync.T02_Sync2$Lock object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4   (object header)  05 90 2e 1e (00000101 10010000 00101110 00011110) (506368005)
      4     4   (object header)  1b 02 00 00 (00011011 00000010 00000000 00000000) (539)
      8     4   (object header)  49 ce 00 20 (01001001 11001110 00000000 00100000) (536923721)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes tota
```

InterpreterRuntime:: monitorenter方法

```c++
IRT_ENTRY_NO_ASYNC(void, InterpreterRuntime::monitorenter(JavaThread* thread, BasicObjectLock* elem))
#ifdef ASSERT
  thread->last_frame().interpreter_frame_verify_monitor(elem);
#endif
  if (PrintBiasedLockingStatistics) {
    Atomic::inc(BiasedLocking::slow_path_entry_count_addr());
  }
  Handle h_obj(thread, elem->obj());
  assert(Universe::heap()->is_in_reserved_or_null(h_obj()),
         "must be NULL or an object");
  if (UseBiasedLocking) {
    // Retry fast entry if bias is revoked to avoid unnecessary inflation
    ObjectSynchronizer::fast_enter(h_obj, elem->lock(), true, CHECK);
  } else {
    ObjectSynchronizer::slow_enter(h_obj, elem->lock(), CHECK);
  }
  assert(Universe::heap()->is_in_reserved_or_null(elem->obj()),
         "must be NULL or an object");
#ifdef ASSERT
  thread->last_frame().interpreter_frame_verify_monitor(elem);
#endif
IRT_END
```

synchronizer.cpp

revoke_and_rebias

```c++
void ObjectSynchronizer::fast_enter(Handle obj, BasicLock* lock, bool attempt_rebias, TRAPS) {
 if (UseBiasedLocking) {
    if (!SafepointSynchronize::is_at_safepoint()) {
      BiasedLocking::Condition cond = BiasedLocking::revoke_and_rebias(obj, attempt_rebias, THREAD);
      if (cond == BiasedLocking::BIAS_REVOKED_AND_REBIASED) {
        return;
      }
    } else {
      assert(!attempt_rebias, "can not rebias toward VM thread");
      BiasedLocking::revoke_at_safepoint(obj);
    }
    assert(!obj->mark()->has_bias_pattern(), "biases should be revoked by now");
 }

 slow_enter (obj, lock, THREAD) ;
}
```

```c++
void ObjectSynchronizer::slow_enter(Handle obj, BasicLock* lock, TRAPS) {
  markOop mark = obj->mark();
  assert(!mark->has_bias_pattern(), "should not see bias pattern here");

  if (mark->is_neutral()) {
    // Anticipate successful CAS -- the ST of the displaced mark must
    // be visible <= the ST performed by the CAS.
    lock->set_displaced_header(mark);
    if (mark == (markOop) Atomic::cmpxchg_ptr(lock, obj()->mark_addr(), mark)) {
      TEVENT (slow_enter: release stacklock) ;
      return ;
    }
    // Fall through to inflate() ...
  } else
  if (mark->has_locker() && THREAD->is_lock_owned((address)mark->locker())) {
    assert(lock != mark->locker(), "must not re-lock the same lock");
    assert(lock != (BasicLock*)obj->mark(), "don't relock with same BasicLock");
    lock->set_displaced_header(NULL);
    return;
  }

#if 0
  // The following optimization isn't particularly useful.
  if (mark->has_monitor() && mark->monitor()->is_entered(THREAD)) {
    lock->set_displaced_header (NULL) ;
    return ;
  }
#endif

  // The object header will never be displaced to this lock,
  // so it does not matter what the value is, except that it
  // must be non-zero to avoid looking like a re-entrant lock,
  // and must not look locked either.
  lock->set_displaced_header(markOopDesc::unused_mark());
  ObjectSynchronizer::inflate(THREAD, obj())->enter(THREAD);
}
```

inflate方法：膨胀为重量级锁



# 锁升级过程



## JDK8 markword实现表：

![image-20200419213508934](lock_step.png)

![](markword-64.png)

**自旋锁什么时候升级为重量级锁？**

**为什么有自旋锁还需要重量级锁？**

> 自旋是消耗CPU资源的，如果锁的时间长，或者自旋线程多，CPU会被大量消耗
>
> 重量级锁有等待队列，所有拿不到锁的进入等待队列，不需要消耗CPU资源

**偏向锁是否一定比自旋锁效率高？**

> 不一定，在明确知道会有多线程竞争的情况下，偏向锁肯定会涉及锁撤销，这时候直接使用自旋锁
>
> JVM启动过程，会有很多线程竞争（明确），所以默认情况启动时不打开偏向锁，过一段儿时间再打开



new - 偏向锁 - 轻量级锁 （无锁, 自旋锁，自适应自旋）- 重量级锁

synchronized优化的过程和markword息息相关

用markword中最低的三位代表锁状态 其中1位是偏向锁位 两位是普通锁位

1. Object o = new Object()
   锁 = 0 01 无锁态 
注意：如果偏向锁打开，默认是匿名偏向状态
   
2. o.hashCode()
   001 + hashcode

   ```java
   00000001 10101101 00110100 00110110
   01011001 00000000 00000000 00000000
   ```

   little endian big endian 

   00000000 00000000 00000000 01011001 00110110 00110100 10101101 00000000

3. 默认synchronized(o) 
   00 -> 轻量级锁
   默认情况 偏向锁有个时延，默认是4秒
   why? 因为JVM虚拟机自己有一些默认启动的线程，里面有好多sync代码，这些sync代码启动时就知道肯定会有竞争，如果使用偏向锁，就会造成偏向锁不断的进行锁撤销和锁升级的操作，效率较低。

   ```shell
   -XX:BiasedLockingStartupDelay=0
   ```

4. 如果设定上述参数
   new Object () - > 101 偏向锁 ->线程ID为0 -> Anonymous BiasedLock 
   打开偏向锁，new出来的对象，默认就是一个可偏向匿名对象101

5. 如果有线程上锁
   上偏向锁，指的就是，把markword的线程ID改为自己线程ID的过程
   偏向锁不可重偏向 批量偏向 批量撤销

6. 如果有线程竞争
   撤销偏向锁，升级轻量级锁
   线程在自己的线程栈生成LockRecord ，用CAS操作将markword设置为指向自己这个线程的LR的指针，设置成功者得到锁

7. 如果竞争加剧
   竞争加剧：有线程超过10次自旋， -XX:PreBlockSpin， 或者自旋线程数超过CPU核数的一半， 1.6之后，加入自适应自旋 Adapative Self Spinning ， JVM自己控制
   升级重量级锁：-> 向操作系统申请资源，linux mutex , CPU从3级-0级系统调用，线程挂起，进入等待队列，等待操作系统的调度，然后再映射回用户空间

(以上实验环境是JDK11，打开就是偏向锁，而JDK8默认对象头是无锁)

偏向锁默认是打开的，但是有一个时延，如果要观察到偏向锁，应该设定参数

**如果计算过对象的hashCode，则对象无法进入偏向状态！**

> 轻量级锁重量级锁的hashCode存在与什么地方？
>
> 答案：线程栈中，轻量级锁的LR中，或是代表重量级锁的ObjectMonitor的成员中

关于epoch: (不重要)

> **批量重偏向与批量撤销**渊源：从偏向锁的加锁解锁过程中可看出，当只有一个线程反复进入同步块时，偏向锁带来的性能开销基本可以忽略，但是当有其他线程尝试获得锁时，就需要等到safe point时，再将偏向锁撤销为无锁状态或升级为轻量级，会消耗一定的性能，所以在多线程竞争频繁的情况下，偏向锁不仅不能提高性能，还会导致性能下降。于是，就有了批量重偏向与批量撤销的机制。
>
> **原理**以class为单位，为每个class维护**解决场景**批量重偏向（bulk rebias）机制是为了解决：一个线程创建了大量对象并执行了初始的同步操作，后来另一个线程也来将这些对象作为锁对象进行操作，这样会导致大量的偏向锁撤销操作。批量撤销（bulk revoke）机制是为了解决：在明显多线程竞争剧烈的场景下使用偏向锁是不合适的。
>
> 一个偏向锁撤销计数器，每一次该class的对象发生偏向撤销操作时，该计数器+1，当这个值达到重偏向阈值（默认20）时，JVM就认为该class的偏向锁有问题，因此会进行批量重偏向。每个class对象会有一个对应的epoch字段，每个处于偏向锁状态对象的Mark Word中也有该字段，其初始值为创建该对象时class中的epoch的值。每次发生批量重偏向时，就将该值+1，同时遍历JVM中所有线程的栈，找到该class所有正处于加锁状态的偏向锁，将其epoch字段改为新值。下次获得锁时，发现当前对象的epoch值和class的epoch不相等，那就算当前已经偏向了其他线程，也不会执行撤销操作，而是直接通过CAS操作将其Mark Word的Thread Id 改成当前线程Id。当达到重偏向阈值后，假设该class计数器继续增长，当其达到批量撤销的阈值后（默认40），JVM就认为该class的使用场景存在多线程竞争，会标记该class为不可偏向，之后，对于该class的锁，直接走轻量级锁的逻辑。



没错，我就是厕所所长

加锁，指的是锁定对象

锁升级的过程

JDK较早的版本 OS的资源 互斥量 用户态 -> 内核态的转换 重量级 效率比较低

现代版本进行了优化

无锁 - 偏向锁 -轻量级锁（自旋锁）-重量级锁



偏向锁 - markword 上记录当前线程指针，下次同一个线程加锁的时候，不需要争用，只需要判断线程指针是否同一个，所以，偏向锁，偏向加锁的第一个线程 。hashCode备份在线程栈上 线程销毁，锁降级为无锁

有争用 - 锁升级为轻量级锁 - 每个线程有自己的LockRecord在自己的线程栈上，用CAS去争用markword的LR的指针，指针指向哪个线程的LR，哪个线程就拥有锁

自旋超过10次，升级为重量级锁 - 如果太多线程自旋 CPU消耗过大，不如升级为重量级锁，进入等待队列（不消耗CPU）-XX:PreBlockSpin



自旋锁在 JDK1.4.2 中引入，使用 -XX:+UseSpinning 来开启。JDK 6 中变为默认开启，并且引入了自适应的自旋锁（适应性自旋锁）。

自适应自旋锁意味着自旋的时间（次数）不再固定，而是由前一次在同一个锁上的自旋时间及锁的拥有者的状态来决定。如果在同一个锁对象上，自旋等待刚刚成功获得过锁，并且持有锁的线程正在运行中，那么虚拟机就会认为这次自旋也是很有可能再次成功，进而它将允许自旋等待持续相对更长的时间。如果对于某个锁，自旋很少成功获得过，那在以后尝试获取这个锁时将可能省略掉自旋过程，直接阻塞线程，避免浪费处理器资源。



偏向锁由于有锁撤销的过程revoke，会消耗系统资源，所以，在锁争用特别激烈的时候，用偏向锁未必效率高。还不如直接使用轻量级锁。

## 锁重入

sychronized是可重入锁

重入次数必须记录，因为要解锁几次必须得对应

偏向锁 自旋锁 -> 线程栈 -> LR + 1

重量级锁 -> ? ObjectMonitor字段上

## synchronized最底层实现

```java

public class T {
    static volatile int i = 0;
    
    public static void n() { i++; }
    
    public static synchronized void m() {}
    
    publics static void main(String[] args) {
        for(int j=0; j<1000_000; j++) {
            m();
            n();
        }
    }
}

```

java -XX:+UnlockDiagnosticVMOptions -XX:+PrintAssembly T

C1 Compile Level 1 (一级优化)

C2 Compile Level 2 (二级优化)

找到m() n()方法的汇编码，会看到 lock comxchg .....指令

## synchronized vs Lock (CAS)

```
 在高争用 高耗时的环境下synchronized效率更高
 在低争用 低耗时的环境下CAS效率更高
 synchronized到重量级之后是等待队列（不消耗CPU）
 CAS（等待期间消耗CPU）
 
 一切以实测为准
```



# 锁消除 lock eliminate

```java
public void add(String str1,String str2){
         StringBuffer sb = new StringBuffer();
         sb.append(str1).append(str2);
}
```

我们都知道 StringBuffer 是线程安全的，因为它的关键方法都是被 synchronized 修饰过的，但我们看上面这段代码，我们会发现，sb 这个引用只会在 add 方法中使用，不可能被其它线程引用（因为是局部变量，栈私有），因此 sb 是不可能共享的资源，JVM 会自动消除 StringBuffer 对象内部的锁。

# 锁粗化 lock coarsening

```java
public String test(String str){
       
       int i = 0;
       StringBuffer sb = new StringBuffer():
       while(i < 100){
           sb.append(str);
           i++;
       }
       return sb.toString():
}
```

JVM 会检测到这样一连串的操作都对同一个对象加锁（while 循环内 100 次执行 append，没有锁粗化的就要进行 100  次加锁/解锁），此时 JVM 就会将加锁的范围粗化到这一连串的操作的外部（比如 while 虚幻体外），使得这一连串操作只需要加一次锁即可。

# 锁降级（不重要）

https://www.zhihu.com/question/63859501

其实，只被VMThread访问，降级也就没啥意义了。所以可以简单认为锁降级不存在！

# 超线程

一个ALU + 两组Registers + PC

# 参考资料

http://openjdk.java.net/groups/hotspot/docs/HotSpotGlossary.html



# volatile的用途

## 1.线程可见性

```java
package com.mashibing.testvolatile;

public class T01_ThreadVisibility {
    private static volatile boolean flag = true;

    public static void main(String[] args) throws InterruptedException {
        new Thread(()-> {
            while (flag) {
                //do sth
            }
            System.out.println("end");
        }, "server").start();


        Thread.sleep(1000);

        flag = false;
    }
}
```

## 2.防止指令重排序

### 问题：DCL单例需不需要加volatile？

### CPU的基础知识

* 缓存行对齐
  缓存行64个字节是CPU同步的基本单位，缓存行隔离会比伪共享效率要高
  Disruptor

* **需要注意，JDK8引入了@sun.misc.Contended注解，来保证缓存行隔离效果**
  要使用此注解，必须去掉限制参数：-XX:-RestrictContended
  
* 另外，java编译器或者JIT编译器有可能会去除没用的字段，所以填充字段必须加上volatile
  
  ```java
package com.mashibing.juc.c_028_FalseSharing;
  
  public class T02_CacheLinePadding {
      private static class Padding {
          public volatile long p1, p2, p3, p4, p5, p6, p7; //
      }
  
      private static class T extends Padding {
          public volatile long x = 0L;
      }
  
      public static T[] arr = new T[2];
  
      static {
          arr[0] = new T();
          arr[1] = new T();
      }
  
      public static void main(String[] args) throws Exception {
          Thread t1 = new Thread(()->{
              for (long i = 0; i < 1000_0000L; i++) {
                  arr[0].x = i;
              }
          });
  
          Thread t2 = new Thread(()->{
              for (long i = 0; i < 1000_0000L; i++) {
                  arr[1].x = i;
              }
          });
  
          final long start = System.nanoTime();
          t1.start();
          t2.start();
          t1.join();
          t2.join();
          System.out.println((System.nanoTime() - start)/100_0000);
      }
  }
  
  ```
  
  MESI

* 伪共享

* 合并写
  CPU内部的4个字节的Buffer

  ```java
  package com.mashibing.juc.c_029_WriteCombining;
  
  public final class WriteCombining {
  
      private static final int ITERATIONS = Integer.MAX_VALUE;
      private static final int ITEMS = 1 << 24;
      private static final int MASK = ITEMS - 1;
  
      private static final byte[] arrayA = new byte[ITEMS];
      private static final byte[] arrayB = new byte[ITEMS];
      private static final byte[] arrayC = new byte[ITEMS];
      private static final byte[] arrayD = new byte[ITEMS];
      private static final byte[] arrayE = new byte[ITEMS];
      private static final byte[] arrayF = new byte[ITEMS];
  
      public static void main(final String[] args) {
  
          for (int i = 1; i <= 3; i++) {
              System.out.println(i + " SingleLoop duration (ns) = " + runCaseOne());
              System.out.println(i + " SplitLoop  duration (ns) = " + runCaseTwo());
          }
      }
  
      public static long runCaseOne() {
          long start = System.nanoTime();
          int i = ITERATIONS;
  
          while (--i != 0) {
              int slot = i & MASK;
              byte b = (byte) i;
              arrayA[slot] = b;
              arrayB[slot] = b;
              arrayC[slot] = b;
              arrayD[slot] = b;
              arrayE[slot] = b;
              arrayF[slot] = b;
          }
          return System.nanoTime() - start;
      }
  
      public static long runCaseTwo() {
          long start = System.nanoTime();
          int i = ITERATIONS;
          while (--i != 0) {
              int slot = i & MASK;
              byte b = (byte) i;
              arrayA[slot] = b;
              arrayB[slot] = b;
              arrayC[slot] = b;
          }
          i = ITERATIONS;
          while (--i != 0) {
              int slot = i & MASK;
              byte b = (byte) i;
              arrayD[slot] = b;
              arrayE[slot] = b;
              arrayF[slot] = b;
          }
          return System.nanoTime() - start;
      }
  }
  
  ```

  

* 指令重排序

  ```java
  package com.mashibing.jvm.c3_jmm;
  
  public class T04_Disorder {
      private static int x = 0, y = 0;
      private static int a = 0, b =0;
  
      public static void main(String[] args) throws InterruptedException {
          int i = 0;
          for(;;) {
              i++;
              x = 0; y = 0;
              a = 0; b = 0;
              Thread one = new Thread(new Runnable() {
                  public void run() {
                      //由于线程one先启动，下面这句话让它等一等线程two. 读着可根据自己电脑的实际性能适当调整等待时间.
                      //shortWait(100000);
                      a = 1;
                      x = b;
                  }
              });
  
              Thread other = new Thread(new Runnable() {
                  public void run() {
                      b = 1;
                      y = a;
                  }
              });
              one.start();other.start();
              one.join();other.join();
              String result = "第" + i + "次 (" + x + "," + y + "）";
              if(x == 0 && y == 0) {
                  System.err.println(result);
                  break;
              } else {
                  //System.out.println(result);
              }
          }
      }
  
  
      public static void shortWait(long interval){
          long start = System.nanoTime();
          long end;
          do{
              end = System.nanoTime();
          }while(start + interval >= end);
      }
  }
  ```

  

### 系统底层如何实现数据一致性

1. MESI如果能解决，就使用MESI
2. 如果不能，就锁总线

### 系统底层如何保证有序性

1. 内存屏障sfence mfence lfence等系统原语
2. 锁总线

### volatile如何解决指令重排序

1: volatile i

2: ACC_VOLATILE

3: JVM的内存屏障

​	屏障两边的指令不可以重排！保障有序！

​    happends-before 

​    as - if - serial

4：hotspot实现

bytecodeinterpreter.cpp

```c++
int field_offset = cache->f2_as_index();
          if (cache->is_volatile()) {
            if (support_IRIW_for_not_multiple_copy_atomic_cpu) {
              OrderAccess::fence();
            }
```

orderaccess_linux_x86.inline.hpp

```c++
inline void OrderAccess::fence() {
  if (os::is_MP()) {
    // always use locked addl since mfence is sometimes expensive
#ifdef AMD64
    __asm__ volatile ("lock; addl $0,0(%%rsp)" : : : "cc", "memory");
#else
    __asm__ volatile ("lock; addl $0,0(%%esp)" : : : "cc", "memory");
#endif
  }
}
```

> **LOCK 用于在多处理器中执行指令时对共享内存的独占使用。
> 它的作用是能够将当前处理器对应缓存的内容刷新到内存，并使其他处理器对应的缓存失效。**
> **另外还提供了有序的指令无法越过这个内存屏障的作用。**

# 用hsdis观察synchronized和volatile

1. 安装hsdis (自行百度)

2. 代码

   ```java
   public class T {
   
     public static volatile int i = 0;
   
     public static void main(String[] args) {
       for(int i=0; i<1000000; i++) {
          m();
          n();
       }
     }
     
     public static synchronized void m() {
       
     }
   
     public static void n() {
       i = 1;
     }
   }
   ```

3. ```css
   java -XX:+UnlockDiagnosticVMOptions -XX:+PrintAssembly T > 1.txt
   ```

## 输出结果

由于JIT会为所有代码生成汇编，请搜索T::m T::n，来找到m() 和 n()方法的汇编码

```htm

============================= C1-compiled nmethod ==============================
----------------------------------- Assembly -----------------------------------

Compiled method (c1)      67    1       3       java.lang.Object::<init> (1 bytes)
 total in heap  [0x00007f81d4d33010,0x00007f81d4d33360] = 848
 relocation     [0x00007f81d4d33170,0x00007f81d4d33198] = 40
 main code      [0x00007f81d4d331a0,0x00007f81d4d33260] = 192
 stub code      [0x00007f81d4d33260,0x00007f81d4d332f0] = 144
 metadata       [0x00007f81d4d332f0,0x00007f81d4d33300] = 16
 scopes data    [0x00007f81d4d33300,0x00007f81d4d33318] = 24
 scopes pcs     [0x00007f81d4d33318,0x00007f81d4d33358] = 64
 dependencies   [0x00007f81d4d33358,0x00007f81d4d33360] = 8

--------------------------------------------------------------------------------
[Constant Pool (empty)]

--------------------------------------------------------------------------------

[Entry Point]
  # {method} {0x00007f81d3cfe650} '<init>' '()V' in 'java/lang/Object'
  #           [sp+0x40]  (sp of caller)
  0x00007f81d4d331a0:   mov    0x8(%rsi),%r10d
  0x00007f81d4d331a4:   shl    $0x3,%r10
  0x00007f81d4d331a8:   cmp    %rax,%r10
  0x00007f81d4d331ab:   jne    0x00007f81d47eed00           ;   {runtime_call ic_miss_stub}
  0x00007f81d4d331b1:   data16 data16 nopw 0x0(%rax,%rax,1)
  0x00007f81d4d331bc:   data16 data16 xchg %ax,%ax
[Verified Entry Point]
  0x00007f81d4d331c0:   mov    %eax,-0x14000(%rsp)
  0x00007f81d4d331c7:   push   %rbp
  0x00007f81d4d331c8:   sub    $0x30,%rsp
  0x00007f81d4d331cc:   movabs $0x7f81d3f33388,%rdi         ;   {metadata(method data for {method} {0x00007f81d3cfe650} '<init>' '()V' in 'java/lang/Object')}
  0x00007f81d4d331d6:   mov    0x13c(%rdi),%ebx
  0x00007f81d4d331dc:   add    $0x8,%ebx
  0x00007f81d4d331df:   mov    %ebx,0x13c(%rdi)
  0x00007f81d4d331e5:   and    $0x1ff8,%ebx
  0x00007f81d4d331eb:   cmp    $0x0,%ebx
  0x00007f81d4d331ee:   je     0x00007f81d4d33204           ;*return {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.Object::<init>@0 (line 50)
  0x00007f81d4d331f4:   add    $0x30,%rsp
  0x00007f81d4d331f8:   pop    %rbp
  0x00007f81d4d331f9:   mov    0x108(%r15),%r10
  0x00007f81d4d33200:   test   %eax,(%r10)                  ;   {poll_return}
  0x00007f81d4d33203:   retq   
  0x00007f81d4d33204:   movabs $0x7f81d3cfe650,%r10         ;   {metadata({method} {0x00007f81d3cfe650} '<init>' '()V' in 'java/lang/Object')}
  0x00007f81d4d3320e:   mov    %r10,0x8(%rsp)
  0x00007f81d4d33213:   movq   $0xffffffffffffffff,(%rsp)
  0x00007f81d4d3321b:   callq  0x00007f81d489e000           ; ImmutableOopMap {rsi=Oop }
                                                            ;*synchronization entry
                                                            ; - java.lang.Object::<init>@-1 (line 50)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d33220:   jmp    0x00007f81d4d331f4
  0x00007f81d4d33222:   nop
  0x00007f81d4d33223:   nop
  0x00007f81d4d33224:   mov    0x3f0(%r15),%rax
  0x00007f81d4d3322b:   movabs $0x0,%r10
  0x00007f81d4d33235:   mov    %r10,0x3f0(%r15)
  0x00007f81d4d3323c:   movabs $0x0,%r10
  0x00007f81d4d33246:   mov    %r10,0x3f8(%r15)
  0x00007f81d4d3324d:   add    $0x30,%rsp
  0x00007f81d4d33251:   pop    %rbp
  0x00007f81d4d33252:   jmpq   0x00007f81d480be80           ;   {runtime_call unwind_exception Runtime1 stub}
  0x00007f81d4d33257:   hlt    
  0x00007f81d4d33258:   hlt    
  0x00007f81d4d33259:   hlt    
  0x00007f81d4d3325a:   hlt    
  0x00007f81d4d3325b:   hlt    
  0x00007f81d4d3325c:   hlt    
  0x00007f81d4d3325d:   hlt    
  0x00007f81d4d3325e:   hlt    
  0x00007f81d4d3325f:   hlt    
[Exception Handler]
  0x00007f81d4d33260:   callq  0x00007f81d489ad00           ;   {no_reloc}
  0x00007f81d4d33265:   mov    %rsp,-0x28(%rsp)
  0x00007f81d4d3326a:   sub    $0x80,%rsp
  0x00007f81d4d33271:   mov    %rax,0x78(%rsp)
  0x00007f81d4d33276:   mov    %rcx,0x70(%rsp)
  0x00007f81d4d3327b:   mov    %rdx,0x68(%rsp)
  0x00007f81d4d33280:   mov    %rbx,0x60(%rsp)
  0x00007f81d4d33285:   mov    %rbp,0x50(%rsp)
  0x00007f81d4d3328a:   mov    %rsi,0x48(%rsp)
  0x00007f81d4d3328f:   mov    %rdi,0x40(%rsp)
  0x00007f81d4d33294:   mov    %r8,0x38(%rsp)
  0x00007f81d4d33299:   mov    %r9,0x30(%rsp)
  0x00007f81d4d3329e:   mov    %r10,0x28(%rsp)
  0x00007f81d4d332a3:   mov    %r11,0x20(%rsp)
  0x00007f81d4d332a8:   mov    %r12,0x18(%rsp)
  0x00007f81d4d332ad:   mov    %r13,0x10(%rsp)
  0x00007f81d4d332b2:   mov    %r14,0x8(%rsp)
  0x00007f81d4d332b7:   mov    %r15,(%rsp)
  0x00007f81d4d332bb:   movabs $0x7f81f15ff3e2,%rdi         ;   {external_word}
  0x00007f81d4d332c5:   movabs $0x7f81d4d33265,%rsi         ;   {internal_word}
  0x00007f81d4d332cf:   mov    %rsp,%rdx
  0x00007f81d4d332d2:   and    $0xfffffffffffffff0,%rsp
  0x00007f81d4d332d6:   callq  0x00007f81f1108240           ;   {runtime_call}
  0x00007f81d4d332db:   hlt    
[Deopt Handler Code]
  0x00007f81d4d332dc:   movabs $0x7f81d4d332dc,%r10         ;   {section_word}
  0x00007f81d4d332e6:   push   %r10
  0x00007f81d4d332e8:   jmpq   0x00007f81d47ed0a0           ;   {runtime_call DeoptimizationBlob}
  0x00007f81d4d332ed:   hlt    
  0x00007f81d4d332ee:   hlt    
  0x00007f81d4d332ef:   hlt    
--------------------------------------------------------------------------------

============================= C1-compiled nmethod ==============================
----------------------------------- Assembly -----------------------------------

Compiled method (c1)      74    2       3       java.lang.StringLatin1::hashCode (42 bytes)
 total in heap  [0x00007f81d4d33390,0x00007f81d4d338a8] = 1304
 relocation     [0x00007f81d4d334f0,0x00007f81d4d33528] = 56
 main code      [0x00007f81d4d33540,0x00007f81d4d336c0] = 384
 stub code      [0x00007f81d4d336c0,0x00007f81d4d33750] = 144
 metadata       [0x00007f81d4d33750,0x00007f81d4d33758] = 8
 scopes data    [0x00007f81d4d33758,0x00007f81d4d337c0] = 104
 scopes pcs     [0x00007f81d4d337c0,0x00007f81d4d33890] = 208
 dependencies   [0x00007f81d4d33890,0x00007f81d4d33898] = 8
 nul chk table  [0x00007f81d4d33898,0x00007f81d4d338a8] = 16

--------------------------------------------------------------------------------
[Constant Pool (empty)]

--------------------------------------------------------------------------------

[Verified Entry Point]
  # {method} {0x00007f81d3e6ddd0} 'hashCode' '([B)I' in 'java/lang/StringLatin1'
  # parm0:    rsi:rsi   = '[B'
  #           [sp+0x40]  (sp of caller)
  0x00007f81d4d33540:   mov    %eax,-0x14000(%rsp)
  0x00007f81d4d33547:   push   %rbp
  0x00007f81d4d33548:   sub    $0x30,%rsp
  0x00007f81d4d3354c:   movabs $0x7f81d3f33980,%rax         ;   {metadata(method data for {method} {0x00007f81d3e6ddd0} 'hashCode' '([B)I' in 'java/lang/StringLatin1')}
  0x00007f81d4d33556:   mov    0x13c(%rax),%edi
  0x00007f81d4d3355c:   add    $0x8,%edi
  0x00007f81d4d3355f:   mov    %edi,0x13c(%rax)
  0x00007f81d4d33565:   and    $0x1ff8,%edi
  0x00007f81d4d3356b:   cmp    $0x0,%edi
  0x00007f81d4d3356e:   je     0x00007f81d4d3362f           ;*iconst_0 {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.StringLatin1::hashCode@0 (line 195)
  0x00007f81d4d33574:   mov    0xc(%rsi),%eax               ; implicit exception: dispatches to 0x00007f81d4d33650
                                                            ;*arraylength {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.StringLatin1::hashCode@5 (line 196)
  0x00007f81d4d33577:   mov    $0x0,%edi
  0x00007f81d4d3357c:   mov    $0x0,%ebx
  0x00007f81d4d33581:   jmpq   0x00007f81d4d335e4           ;*iload {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.StringLatin1::hashCode@10 (line 196)
  0x00007f81d4d33586:   xchg   %ax,%ax
  0x00007f81d4d33588:   movslq %edi,%rdx
  0x00007f81d4d3358b:   movsbl 0x10(%rsi,%rdx,1),%edx       ;*baload {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.StringLatin1::hashCode@19 (line 196)
  0x00007f81d4d33590:   mov    %rbx,%rcx
  0x00007f81d4d33593:   shl    $0x5,%ebx
  0x00007f81d4d33596:   sub    %ecx,%ebx
  0x00007f81d4d33598:   and    $0xff,%edx
  0x00007f81d4d3359e:   add    %edx,%ebx
  0x00007f81d4d335a0:   inc    %edi
  0x00007f81d4d335a2:   movabs $0x7f81d3f33980,%rdx         ;   {metadata(method data for {method} {0x00007f81d3e6ddd0} 'hashCode' '([B)I' in 'java/lang/StringLatin1')}
  0x00007f81d4d335ac:   mov    0x140(%rdx),%ecx
  0x00007f81d4d335b2:   add    $0x8,%ecx
  0x00007f81d4d335b5:   mov    %ecx,0x140(%rdx)
  0x00007f81d4d335bb:   and    $0xfff8,%ecx
  0x00007f81d4d335c1:   cmp    $0x0,%ecx
  0x00007f81d4d335c4:   je     0x00007f81d4d33655           ;*goto {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.StringLatin1::hashCode@37 (line 196)
  0x00007f81d4d335ca:   mov    0x108(%r15),%r10             ; ImmutableOopMap {rsi=Oop }
                                                            ;*goto {reexecute=1 rethrow=0 return_oop=0}
                                                            ; - (reexecute) java.lang.StringLatin1::hashCode@37 (line 196)
  0x00007f81d4d335d1:   test   %eax,(%r10)                  ;   {poll}
  0x00007f81d4d335d4:   movabs $0x7f81d3f33980,%rdx         ;   {metadata(method data for {method} {0x00007f81d3e6ddd0} 'hashCode' '([B)I' in 'java/lang/StringLatin1')}
  0x00007f81d4d335de:   incl   0x1a0(%rdx)                  ;*goto {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.StringLatin1::hashCode@37 (line 196)
  0x00007f81d4d335e4:   cmp    %eax,%edi
  0x00007f81d4d335e6:   movabs $0x7f81d3f33980,%rdx         ;   {metadata(method data for {method} {0x00007f81d3e6ddd0} 'hashCode' '([B)I' in 'java/lang/StringLatin1')}
  0x00007f81d4d335f0:   movabs $0x190,%rcx
  0x00007f81d4d335fa:   jl     0x00007f81d4d3360a
  0x00007f81d4d33600:   movabs $0x180,%rcx
  0x00007f81d4d3360a:   mov    (%rdx,%rcx,1),%r8
  0x00007f81d4d3360e:   lea    0x1(%r8),%r8
  0x00007f81d4d33612:   mov    %r8,(%rdx,%rcx,1)
  0x00007f81d4d33616:   jl     0x00007f81d4d33588           ;*if_icmpge {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.StringLatin1::hashCode@13 (line 196)
  0x00007f81d4d3361c:   mov    %rbx,%rax
  0x00007f81d4d3361f:   add    $0x30,%rsp
  0x00007f81d4d33623:   pop    %rbp
  0x00007f81d4d33624:   mov    0x108(%r15),%r10
  0x00007f81d4d3362b:   test   %eax,(%r10)                  ;   {poll_return}
  0x00007f81d4d3362e:   retq   
  0x00007f81d4d3362f:   movabs $0x7f81d3e6ddd0,%r10         ;   {metadata({method} {0x00007f81d3e6ddd0} 'hashCode' '([B)I' in 'java/lang/StringLatin1')}
  0x00007f81d4d33639:   mov    %r10,0x8(%rsp)
  0x00007f81d4d3363e:   movq   $0xffffffffffffffff,(%rsp)
  0x00007f81d4d33646:   callq  0x00007f81d489e000           ; ImmutableOopMap {rsi=Oop }
                                                            ;*synchronization entry
                                                            ; - java.lang.StringLatin1::hashCode@-1 (line 195)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d3364b:   jmpq   0x00007f81d4d33574
  0x00007f81d4d33650:   callq  0x00007f81d480afa0           ; ImmutableOopMap {rsi=Oop }
                                                            ;*arraylength {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.StringLatin1::hashCode@5 (line 196)
                                                            ;   {runtime_call throw_null_pointer_exception Runtime1 stub}
  0x00007f81d4d33655:   movabs $0x7f81d3e6ddd0,%r10         ;   {metadata({method} {0x00007f81d3e6ddd0} 'hashCode' '([B)I' in 'java/lang/StringLatin1')}
  0x00007f81d4d3365f:   mov    %r10,0x8(%rsp)
  0x00007f81d4d33664:   movq   $0x25,(%rsp)
  0x00007f81d4d3366c:   callq  0x00007f81d489e000           ; ImmutableOopMap {rsi=Oop }
                                                            ;*goto {reexecute=1 rethrow=0 return_oop=0}
                                                            ; - (reexecute) java.lang.StringLatin1::hashCode@37 (line 196)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d33671:   jmpq   0x00007f81d4d335ca
  0x00007f81d4d33676:   nop
  0x00007f81d4d33677:   nop
  0x00007f81d4d33678:   mov    0x3f0(%r15),%rax
  0x00007f81d4d3367f:   movabs $0x0,%r10
  0x00007f81d4d33689:   mov    %r10,0x3f0(%r15)
  0x00007f81d4d33690:   movabs $0x0,%r10
  0x00007f81d4d3369a:   mov    %r10,0x3f8(%r15)
  0x00007f81d4d336a1:   add    $0x30,%rsp
  0x00007f81d4d336a5:   pop    %rbp
  0x00007f81d4d336a6:   jmpq   0x00007f81d480be80           ;   {runtime_call unwind_exception Runtime1 stub}
  0x00007f81d4d336ab:   hlt    
  0x00007f81d4d336ac:   hlt    
  0x00007f81d4d336ad:   hlt    
  0x00007f81d4d336ae:   hlt    
  0x00007f81d4d336af:   hlt    
  0x00007f81d4d336b0:   hlt    
  0x00007f81d4d336b1:   hlt    
  0x00007f81d4d336b2:   hlt    
  0x00007f81d4d336b3:   hlt    
  0x00007f81d4d336b4:   hlt    
  0x00007f81d4d336b5:   hlt    
  0x00007f81d4d336b6:   hlt    
  0x00007f81d4d336b7:   hlt    
  0x00007f81d4d336b8:   hlt    
  0x00007f81d4d336b9:   hlt    
  0x00007f81d4d336ba:   hlt    
  0x00007f81d4d336bb:   hlt    
  0x00007f81d4d336bc:   hlt    
  0x00007f81d4d336bd:   hlt    
  0x00007f81d4d336be:   hlt    
  0x00007f81d4d336bf:   hlt    
[Exception Handler]
  0x00007f81d4d336c0:   callq  0x00007f81d489ad00           ;   {no_reloc}
  0x00007f81d4d336c5:   mov    %rsp,-0x28(%rsp)
  0x00007f81d4d336ca:   sub    $0x80,%rsp
  0x00007f81d4d336d1:   mov    %rax,0x78(%rsp)
  0x00007f81d4d336d6:   mov    %rcx,0x70(%rsp)
  0x00007f81d4d336db:   mov    %rdx,0x68(%rsp)
  0x00007f81d4d336e0:   mov    %rbx,0x60(%rsp)
  0x00007f81d4d336e5:   mov    %rbp,0x50(%rsp)
  0x00007f81d4d336ea:   mov    %rsi,0x48(%rsp)
  0x00007f81d4d336ef:   mov    %rdi,0x40(%rsp)
  0x00007f81d4d336f4:   mov    %r8,0x38(%rsp)
  0x00007f81d4d336f9:   mov    %r9,0x30(%rsp)
  0x00007f81d4d336fe:   mov    %r10,0x28(%rsp)
  0x00007f81d4d33703:   mov    %r11,0x20(%rsp)
  0x00007f81d4d33708:   mov    %r12,0x18(%rsp)
  0x00007f81d4d3370d:   mov    %r13,0x10(%rsp)
  0x00007f81d4d33712:   mov    %r14,0x8(%rsp)
  0x00007f81d4d33717:   mov    %r15,(%rsp)
  0x00007f81d4d3371b:   movabs $0x7f81f15ff3e2,%rdi         ;   {external_word}
  0x00007f81d4d33725:   movabs $0x7f81d4d336c5,%rsi         ;   {internal_word}
  0x00007f81d4d3372f:   mov    %rsp,%rdx
  0x00007f81d4d33732:   and    $0xfffffffffffffff0,%rsp
  0x00007f81d4d33736:   callq  0x00007f81f1108240           ;   {runtime_call}
  0x00007f81d4d3373b:   hlt    
[Deopt Handler Code]
  0x00007f81d4d3373c:   movabs $0x7f81d4d3373c,%r10         ;   {section_word}
  0x00007f81d4d33746:   push   %r10
  0x00007f81d4d33748:   jmpq   0x00007f81d47ed0a0           ;   {runtime_call DeoptimizationBlob}
  0x00007f81d4d3374d:   hlt    
  0x00007f81d4d3374e:   hlt    
  0x00007f81d4d3374f:   hlt    
--------------------------------------------------------------------------------

============================= C2-compiled nmethod ==============================
----------------------------------- Assembly -----------------------------------

Compiled method (c2)      82   11       4       java.lang.Object::<init> (1 bytes)
 total in heap  [0x00007f81dc26b010,0x00007f81dc26b228] = 536
 relocation     [0x00007f81dc26b170,0x00007f81dc26b180] = 16
 main code      [0x00007f81dc26b180,0x00007f81dc26b1c0] = 64
 stub code      [0x00007f81dc26b1c0,0x00007f81dc26b1d8] = 24
 metadata       [0x00007f81dc26b1d8,0x00007f81dc26b1e8] = 16
 scopes data    [0x00007f81dc26b1e8,0x00007f81dc26b1f0] = 8
 scopes pcs     [0x00007f81dc26b1f0,0x00007f81dc26b220] = 48
 dependencies   [0x00007f81dc26b220,0x00007f81dc26b228] = 8

--------------------------------------------------------------------------------
[Constant Pool (empty)]

--------------------------------------------------------------------------------

[Entry Point]
  # {method} {0x00007f81d3cfe650} '<init>' '()V' in 'java/lang/Object'
  #           [sp+0x20]  (sp of caller)
  0x00007f81dc26b180:   mov    0x8(%rsi),%r10d
  0x00007f81dc26b184:   shl    $0x3,%r10
  0x00007f81dc26b188:   cmp    %r10,%rax
  0x00007f81dc26b18b:   jne    0x00007f81d47eed00           ;   {runtime_call ic_miss_stub}
  0x00007f81dc26b191:   data16 xchg %ax,%ax
  0x00007f81dc26b194:   nopl   0x0(%rax,%rax,1)
  0x00007f81dc26b19c:   data16 data16 xchg %ax,%ax
[Verified Entry Point]
  0x00007f81dc26b1a0:   sub    $0x18,%rsp
  0x00007f81dc26b1a7:   mov    %rbp,0x10(%rsp)              ;*synchronization entry
                                                            ; - java.lang.Object::<init>@-1 (line 50)
  0x00007f81dc26b1ac:   add    $0x10,%rsp
  0x00007f81dc26b1b0:   pop    %rbp
  0x00007f81dc26b1b1:   mov    0x108(%r15),%r10
  0x00007f81dc26b1b8:   test   %eax,(%r10)                  ;   {poll_return}
  0x00007f81dc26b1bb:   retq   
  0x00007f81dc26b1bc:   hlt    
  0x00007f81dc26b1bd:   hlt    
  0x00007f81dc26b1be:   hlt    
  0x00007f81dc26b1bf:   hlt    
[Exception Handler]
  0x00007f81dc26b1c0:   jmpq   0x00007f81d4809300           ;   {no_reloc}
[Deopt Handler Code]
  0x00007f81dc26b1c5:   callq  0x00007f81dc26b1ca
  0x00007f81dc26b1ca:   subq   $0x5,(%rsp)
  0x00007f81dc26b1cf:   jmpq   0x00007f81d47ed0a0           ;   {runtime_call DeoptimizationBlob}
  0x00007f81dc26b1d4:   hlt    
  0x00007f81dc26b1d5:   hlt    
  0x00007f81dc26b1d6:   hlt    
  0x00007f81dc26b1d7:   hlt    
--------------------------------------------------------------------------------

============================= C1-compiled nmethod ==============================
----------------------------------- Assembly -----------------------------------

Compiled method (c1)      86    5       3       java.util.ImmutableCollections$SetN::probe (56 bytes)
 total in heap  [0x00007f81d4d33910,0x00007f81d4d343a8] = 2712
 relocation     [0x00007f81d4d33a70,0x00007f81d4d33ae8] = 120
 main code      [0x00007f81d4d33b00,0x00007f81d4d33fa0] = 1184
 stub code      [0x00007f81d4d33fa0,0x00007f81d4d34058] = 184
 metadata       [0x00007f81d4d34058,0x00007f81d4d34068] = 16
 scopes data    [0x00007f81d4d34068,0x00007f81d4d34170] = 264
 scopes pcs     [0x00007f81d4d34170,0x00007f81d4d34370] = 512
 dependencies   [0x00007f81d4d34370,0x00007f81d4d34378] = 8
 nul chk table  [0x00007f81d4d34378,0x00007f81d4d343a8] = 48

--------------------------------------------------------------------------------
[Constant Pool (empty)]

--------------------------------------------------------------------------------

[Entry Point]
  # {method} {0x00007f81d3f11bb8} 'probe' '(Ljava/lang/Object;)I' in 'java/util/ImmutableCollections$SetN'
  # this:     rsi:rsi   = 'java/util/ImmutableCollections$SetN'
  # parm0:    rdx:rdx   = 'java/lang/Object'
  #           [sp+0x60]  (sp of caller)
  0x00007f81d4d33b00:   mov    0x8(%rsi),%r10d
  0x00007f81d4d33b04:   shl    $0x3,%r10
  0x00007f81d4d33b08:   cmp    %rax,%r10
  0x00007f81d4d33b0b:   jne    0x00007f81d47eed00           ;   {runtime_call ic_miss_stub}
  0x00007f81d4d33b11:   data16 data16 nopw 0x0(%rax,%rax,1)
  0x00007f81d4d33b1c:   data16 data16 xchg %ax,%ax
[Verified Entry Point]
  0x00007f81d4d33b20:   mov    %eax,-0x14000(%rsp)
  0x00007f81d4d33b27:   push   %rbp
  0x00007f81d4d33b28:   sub    $0x50,%rsp
  0x00007f81d4d33b2c:   mov    %rsi,0x30(%rsp)
  0x00007f81d4d33b31:   mov    %rdx,0x38(%rsp)
  0x00007f81d4d33b36:   movabs $0x7f81d3f43b88,%rdi         ;   {metadata(method data for {method} {0x00007f81d3f11bb8} 'probe' '(Ljava/lang/Object;)I' in 'java/util/ImmutableCollections$SetN')}
  0x00007f81d4d33b40:   mov    0x13c(%rdi),%ebx
  0x00007f81d4d33b46:   add    $0x8,%ebx
  0x00007f81d4d33b49:   mov    %ebx,0x13c(%rdi)
  0x00007f81d4d33b4f:   and    $0x1ff8,%ebx
  0x00007f81d4d33b55:   cmp    $0x0,%ebx
  0x00007f81d4d33b58:   je     0x00007f81d4d33ee0           ;*aload_1 {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN::probe@0 (line 796)
  0x00007f81d4d33b5e:   cmp    (%rdx),%rax                  ; implicit exception: dispatches to 0x00007f81d4d33f01
  0x00007f81d4d33b61:   mov    %rdx,%rdi
  0x00007f81d4d33b64:   movabs $0x7f81d3f43b88,%rbx         ;   {metadata(method data for {method} {0x00007f81d3f11bb8} 'probe' '(Ljava/lang/Object;)I' in 'java/util/ImmutableCollections$SetN')}
  0x00007f81d4d33b6e:   mov    0x8(%rdi),%edi
  0x00007f81d4d33b71:   shl    $0x3,%rdi
  0x00007f81d4d33b75:   cmp    0x190(%rbx),%rdi
  0x00007f81d4d33b7c:   jne    0x00007f81d4d33b8b
  0x00007f81d4d33b7e:   addq   $0x1,0x198(%rbx)
  0x00007f81d4d33b86:   jmpq   0x00007f81d4d33bf1
  0x00007f81d4d33b8b:   cmp    0x1a0(%rbx),%rdi
  0x00007f81d4d33b92:   jne    0x00007f81d4d33ba1
  0x00007f81d4d33b94:   addq   $0x1,0x1a8(%rbx)
  0x00007f81d4d33b9c:   jmpq   0x00007f81d4d33bf1
  0x00007f81d4d33ba1:   cmpq   $0x0,0x190(%rbx)
  0x00007f81d4d33bac:   jne    0x00007f81d4d33bc5
  0x00007f81d4d33bae:   mov    %rdi,0x190(%rbx)
  0x00007f81d4d33bb5:   movq   $0x1,0x198(%rbx)
  0x00007f81d4d33bc0:   jmpq   0x00007f81d4d33bf1
  0x00007f81d4d33bc5:   cmpq   $0x0,0x1a0(%rbx)
  0x00007f81d4d33bd0:   jne    0x00007f81d4d33be9
  0x00007f81d4d33bd2:   mov    %rdi,0x1a0(%rbx)
  0x00007f81d4d33bd9:   movq   $0x1,0x1a8(%rbx)
  0x00007f81d4d33be4:   jmpq   0x00007f81d4d33bf1
  0x00007f81d4d33be9:   addq   $0x1,0x180(%rbx)
  0x00007f81d4d33bf1:   mov    %rdx,%rsi                    ;*invokevirtual hashCode {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN::probe@1 (line 796)
  0x00007f81d4d33bf4:   nop
  0x00007f81d4d33bf5:   movabs $0xffffffffffffffff,%rax
  0x00007f81d4d33bff:   callq  0x00007f81d47ee700           ; ImmutableOopMap {[48]=Oop [56]=Oop }
                                                            ;*invokevirtual hashCode {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN::probe@1 (line 796)
                                                            ;   {virtual_call}
  0x00007f81d4d33c04:   mov    0x30(%rsp),%rsi
  0x00007f81d4d33c09:   mov    0x10(%rsi),%edx              ;*getfield elements {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN::probe@5 (line 796)
  0x00007f81d4d33c0c:   mov    0xc(%rdx),%edi               ; implicit exception: dispatches to 0x00007f81d4d33f06
                                                            ;*arraylength {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN::probe@8 (line 796)
  0x00007f81d4d33c0f:   movabs $0x7f81d3f43b88,%rdx         ;   {metadata(method data for {method} {0x00007f81d3f11bb8} 'probe' '(Ljava/lang/Object;)I' in 'java/util/ImmutableCollections$SetN')}
  0x00007f81d4d33c19:   addq   $0x1,0x1b8(%rdx)
  0x00007f81d4d33c21:   movabs $0x7f81d3f439a0,%rdx         ;   {metadata(method data for {method} {0x00007f81d3e376e8} 'floorMod' '(II)I' in 'java/lang/Math')}
  0x00007f81d4d33c2b:   mov    0x13c(%rdx),%ebx
  0x00007f81d4d33c31:   add    $0x8,%ebx
  0x00007f81d4d33c34:   mov    %ebx,0x13c(%rdx)
  0x00007f81d4d33c3a:   and    $0x7ffff8,%ebx
  0x00007f81d4d33c40:   cmp    $0x0,%ebx
  0x00007f81d4d33c43:   je     0x00007f81d4d33f0b
  0x00007f81d4d33c49:   cmp    $0x80000000,%eax
  0x00007f81d4d33c4f:   jne    0x00007f81d4d33c60
  0x00007f81d4d33c55:   xor    %edx,%edx
  0x00007f81d4d33c57:   cmp    $0xffffffff,%edi
  0x00007f81d4d33c5a:   je     0x00007f81d4d33c63
  0x00007f81d4d33c60:   cltd   
  0x00007f81d4d33c61:   idiv   %edi                         ; implicit exception: dispatches to 0x00007f81d4d33f2c
                                                            ;*irem {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.Math::floorMod@2 (line 1277)
                                                            ; - java.util.ImmutableCollections$SetN::probe@9 (line 796)
  0x00007f81d4d33c63:   mov    %rdx,%rbx
  0x00007f81d4d33c66:   xor    %rdi,%rbx
  0x00007f81d4d33c69:   cmp    $0x0,%ebx
  0x00007f81d4d33c6c:   movabs $0x7f81d3f439a0,%rbx         ;   {metadata(method data for {method} {0x00007f81d3e376e8} 'floorMod' '(II)I' in 'java/lang/Math')}
  0x00007f81d4d33c76:   movabs $0x180,%rax
  0x00007f81d4d33c80:   jge    0x00007f81d4d33c90
  0x00007f81d4d33c86:   movabs $0x190,%rax
  0x00007f81d4d33c90:   mov    (%rbx,%rax,1),%rcx
  0x00007f81d4d33c94:   lea    0x1(%rcx),%rcx
  0x00007f81d4d33c98:   mov    %rcx,(%rbx,%rax,1)
  0x00007f81d4d33c9c:   jge    0x00007f81d4d33ce0           ;*ifge {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.Math::floorMod@7 (line 1279)
                                                            ; - java.util.ImmutableCollections$SetN::probe@9 (line 796)
  0x00007f81d4d33ca2:   cmp    $0x0,%edx
  0x00007f81d4d33ca5:   movabs $0x7f81d3f439a0,%rbx         ;   {metadata(method data for {method} {0x00007f81d3e376e8} 'floorMod' '(II)I' in 'java/lang/Math')}
  0x00007f81d4d33caf:   movabs $0x1a0,%rax
  0x00007f81d4d33cb9:   je     0x00007f81d4d33cc9
  0x00007f81d4d33cbf:   movabs $0x1b0,%rax
  0x00007f81d4d33cc9:   mov    (%rbx,%rax,1),%rcx
  0x00007f81d4d33ccd:   lea    0x1(%rcx),%rcx
  0x00007f81d4d33cd1:   mov    %rcx,(%rbx,%rax,1)
  0x00007f81d4d33cd5:   je     0x00007f81d4d33ce0           ;*ifeq {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.Math::floorMod@11 (line 1279)
                                                            ; - java.util.ImmutableCollections$SetN::probe@9 (line 796)
  0x00007f81d4d33cdb:   add    %edi,%edx
  0x00007f81d4d33cdd:   data16 xchg %ax,%ax
  0x00007f81d4d33ce0:   mov    0x38(%rsp),%rdi
  0x00007f81d4d33ce5:   mov    0x10(%rsi),%ebx              ;*getfield elements {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN::probe@14 (line 798)
  0x00007f81d4d33ce8:   mov    0xc(%rbx),%eax               ; implicit exception: dispatches to 0x00007f81d4d33f31
  0x00007f81d4d33ceb:   cmp    %edx,%eax
  0x00007f81d4d33ced:   jbe    0x00007f81d4d33f36
  0x00007f81d4d33cf3:   movslq %edx,%rax
  0x00007f81d4d33cf6:   mov    0x10(%rbx,%rax,4),%ebx       ;*aaload {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN::probe@18 (line 798)
  0x00007f81d4d33cfa:   cmp    $0x0,%rbx
  0x00007f81d4d33cfe:   movabs $0x7f81d3f43b88,%rax         ;   {metadata(method data for {method} {0x00007f81d3f11bb8} 'probe' '(Ljava/lang/Object;)I' in 'java/util/ImmutableCollections$SetN')}
  0x00007f81d4d33d08:   movabs $0x1d8,%rcx
  0x00007f81d4d33d12:   je     0x00007f81d4d33d22
  0x00007f81d4d33d18:   movabs $0x1c8,%rcx
  0x00007f81d4d33d22:   mov    (%rax,%rcx,1),%r8
  0x00007f81d4d33d26:   lea    0x1(%r8),%r8
  0x00007f81d4d33d2a:   mov    %r8,(%rax,%rcx,1)
  0x00007f81d4d33d2e:   je     0x00007f81d4d33ec9           ;*ifnonnull {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN::probe@21 (line 799)
  0x00007f81d4d33d34:   mov    %edx,0x40(%rsp)
  0x00007f81d4d33d38:   mov    %rdi,%rax
  0x00007f81d4d33d3b:   movabs $0x7f81d3f43b88,%rcx         ;   {metadata(method data for {method} {0x00007f81d3f11bb8} 'probe' '(Ljava/lang/Object;)I' in 'java/util/ImmutableCollections$SetN')}
  0x00007f81d4d33d45:   mov    0x8(%rax),%eax
  0x00007f81d4d33d48:   shl    $0x3,%rax
  0x00007f81d4d33d4c:   cmp    0x1f8(%rcx),%rax
  0x00007f81d4d33d53:   jne    0x00007f81d4d33d62
  0x00007f81d4d33d55:   addq   $0x1,0x200(%rcx)
  0x00007f81d4d33d5d:   jmpq   0x00007f81d4d33dc8
  0x00007f81d4d33d62:   cmp    0x208(%rcx),%rax
  0x00007f81d4d33d69:   jne    0x00007f81d4d33d78
  0x00007f81d4d33d6b:   addq   $0x1,0x210(%rcx)
  0x00007f81d4d33d73:   jmpq   0x00007f81d4d33dc8
  0x00007f81d4d33d78:   cmpq   $0x0,0x1f8(%rcx)
  0x00007f81d4d33d83:   jne    0x00007f81d4d33d9c
  0x00007f81d4d33d85:   mov    %rax,0x1f8(%rcx)
  0x00007f81d4d33d8c:   movq   $0x1,0x200(%rcx)
  0x00007f81d4d33d97:   jmpq   0x00007f81d4d33dc8
  0x00007f81d4d33d9c:   cmpq   $0x0,0x208(%rcx)
  0x00007f81d4d33da7:   jne    0x00007f81d4d33dc0
  0x00007f81d4d33da9:   mov    %rax,0x208(%rcx)
  0x00007f81d4d33db0:   movq   $0x1,0x210(%rcx)
  0x00007f81d4d33dbb:   jmpq   0x00007f81d4d33dc8
  0x00007f81d4d33dc0:   addq   $0x1,0x1e8(%rcx)
  0x00007f81d4d33dc8:   mov    %rbx,%rdx
  0x00007f81d4d33dcb:   mov    %rdi,%rsi                    ;*invokevirtual equals {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN::probe@31 (line 801)
  0x00007f81d4d33dce:   nopl   0x0(%rax)
  0x00007f81d4d33dd5:   movabs $0xffffffffffffffff,%rax
  0x00007f81d4d33ddf:   callq  0x00007f81d47ee700           ; ImmutableOopMap {[48]=Oop [56]=Oop }
                                                            ;*invokevirtual equals {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN::probe@31 (line 801)
                                                            ;   {virtual_call}
  0x00007f81d4d33de4:   cmp    $0x0,%eax
  0x00007f81d4d33de7:   movabs $0x7f81d3f43b88,%rax         ;   {metadata(method data for {method} {0x00007f81d3f11bb8} 'probe' '(Ljava/lang/Object;)I' in 'java/util/ImmutableCollections$SetN')}
  0x00007f81d4d33df1:   movabs $0x230,%rsi
  0x00007f81d4d33dfb:   jne    0x00007f81d4d33e0b
  0x00007f81d4d33e01:   movabs $0x220,%rsi
  0x00007f81d4d33e0b:   mov    (%rax,%rsi,1),%rdi
  0x00007f81d4d33e0f:   lea    0x1(%rdi),%rdi
  0x00007f81d4d33e13:   mov    %rdi,(%rax,%rsi,1)
  0x00007f81d4d33e17:   jne    0x00007f81d4d33eb2           ;*ifeq {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN::probe@34 (line 801)
  0x00007f81d4d33e1d:   mov    0x30(%rsp),%rsi
  0x00007f81d4d33e22:   mov    0x40(%rsp),%edx
  0x00007f81d4d33e26:   inc    %edx
  0x00007f81d4d33e28:   mov    0x10(%rsi),%eax              ;*getfield elements {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN::probe@44 (line 803)
  0x00007f81d4d33e2b:   mov    0xc(%rax),%eax               ; implicit exception: dispatches to 0x00007f81d4d33f44
                                                            ;*arraylength {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN::probe@47 (line 803)
  0x00007f81d4d33e2e:   cmp    %eax,%edx
  0x00007f81d4d33e30:   movabs $0x7f81d3f43b88,%rax         ;   {metadata(method data for {method} {0x00007f81d3f11bb8} 'probe' '(Ljava/lang/Object;)I' in 'java/util/ImmutableCollections$SetN')}
  0x00007f81d4d33e3a:   movabs $0x240,%rdi
  0x00007f81d4d33e44:   jne    0x00007f81d4d33e54
  0x00007f81d4d33e4a:   movabs $0x250,%rdi
  0x00007f81d4d33e54:   mov    (%rax,%rdi,1),%rbx
  0x00007f81d4d33e58:   lea    0x1(%rbx),%rbx
  0x00007f81d4d33e5c:   mov    %rbx,(%rax,%rdi,1)
  0x00007f81d4d33e60:   jne    0x00007f81d4d33e6b           ;*if_icmpne {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN::probe@48 (line 803)
  0x00007f81d4d33e66:   mov    $0x0,%edx
  0x00007f81d4d33e6b:   movabs $0x7f81d3f43b88,%rax         ;   {metadata(method data for {method} {0x00007f81d3f11bb8} 'probe' '(Ljava/lang/Object;)I' in 'java/util/ImmutableCollections$SetN')}
  0x00007f81d4d33e75:   mov    0x140(%rax),%edi
  0x00007f81d4d33e7b:   add    $0x8,%edi
  0x00007f81d4d33e7e:   mov    %edi,0x140(%rax)
  0x00007f81d4d33e84:   and    $0xfff8,%edi
  0x00007f81d4d33e8a:   cmp    $0x0,%edi
  0x00007f81d4d33e8d:   je     0x00007f81d4d33f49           ;*goto {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN::probe@53 (line 806)
  0x00007f81d4d33e93:   mov    0x108(%r15),%r10             ; ImmutableOopMap {[56]=Oop rsi=Oop [48]=Oop }
                                                            ;*goto {reexecute=1 rethrow=0 return_oop=0}
                                                            ; - (reexecute) java.util.ImmutableCollections$SetN::probe@53 (line 806)
  0x00007f81d4d33e9a:   test   %eax,(%r10)                  ;   {poll}
  0x00007f81d4d33e9d:   movabs $0x7f81d3f43b88,%rax         ;   {metadata(method data for {method} {0x00007f81d3f11bb8} 'probe' '(Ljava/lang/Object;)I' in 'java/util/ImmutableCollections$SetN')}
  0x00007f81d4d33ea7:   incl   0x260(%rax)
  0x00007f81d4d33ead:   jmpq   0x00007f81d4d33ce0           ;*goto {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN::probe@53 (line 806)
  0x00007f81d4d33eb2:   mov    0x40(%rsp),%edx
  0x00007f81d4d33eb6:   mov    %rdx,%rax
  0x00007f81d4d33eb9:   add    $0x50,%rsp
  0x00007f81d4d33ebd:   pop    %rbp
  0x00007f81d4d33ebe:   mov    0x108(%r15),%r10
  0x00007f81d4d33ec5:   test   %eax,(%r10)                  ;   {poll_return}
  0x00007f81d4d33ec8:   retq                                ;*ireturn {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN::probe@38 (line 802)
  0x00007f81d4d33ec9:   neg    %edx
  0x00007f81d4d33ecb:   mov    %rdx,%rax
  0x00007f81d4d33ece:   dec    %eax
  0x00007f81d4d33ed0:   add    $0x50,%rsp
  0x00007f81d4d33ed4:   pop    %rbp
  0x00007f81d4d33ed5:   mov    0x108(%r15),%r10
  0x00007f81d4d33edc:   test   %eax,(%r10)                  ;   {poll_return}
  0x00007f81d4d33edf:   retq   
  0x00007f81d4d33ee0:   movabs $0x7f81d3f11bb8,%r10         ;   {metadata({method} {0x00007f81d3f11bb8} 'probe' '(Ljava/lang/Object;)I' in 'java/util/ImmutableCollections$SetN')}
  0x00007f81d4d33eea:   mov    %r10,0x8(%rsp)
  0x00007f81d4d33eef:   movq   $0xffffffffffffffff,(%rsp)
  0x00007f81d4d33ef7:   callq  0x00007f81d489e000           ; ImmutableOopMap {rsi=Oop [48]=Oop rdx=Oop [56]=Oop }
                                                            ;*synchronization entry
                                                            ; - java.util.ImmutableCollections$SetN::probe@-1 (line 796)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d33efc:   jmpq   0x00007f81d4d33b5e
  0x00007f81d4d33f01:   callq  0x00007f81d480afa0           ; ImmutableOopMap {rdx=Oop [56]=Oop [48]=Oop }
                                                            ;*invokevirtual hashCode {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN::probe@1 (line 796)
                                                            ;   {runtime_call throw_null_pointer_exception Runtime1 stub}
  0x00007f81d4d33f06:   callq  0x00007f81d480afa0           ; ImmutableOopMap {[56]=Oop rsi=Oop [48]=Oop }
                                                            ;*arraylength {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN::probe@8 (line 796)
                                                            ;   {runtime_call throw_null_pointer_exception Runtime1 stub}
  0x00007f81d4d33f0b:   movabs $0x7f81d3e376e8,%r10         ;   {metadata({method} {0x00007f81d3e376e8} 'floorMod' '(II)I' in 'java/lang/Math')}
  0x00007f81d4d33f15:   mov    %r10,0x8(%rsp)
  0x00007f81d4d33f1a:   movq   $0xffffffffffffffff,(%rsp)
  0x00007f81d4d33f22:   callq  0x00007f81d489e000           ; ImmutableOopMap {[56]=Oop rsi=Oop [48]=Oop }
                                                            ;*synchronization entry
                                                            ; - java.lang.Math::floorMod@-1 (line 1277)
                                                            ; - java.util.ImmutableCollections$SetN::probe@9 (line 796)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d33f27:   jmpq   0x00007f81d4d33c49
  0x00007f81d4d33f2c:   callq  0x00007f81d480b2a0           ; ImmutableOopMap {[56]=Oop rsi=Oop [48]=Oop }
                                                            ;*irem {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.Math::floorMod@2 (line 1277)
                                                            ; - java.util.ImmutableCollections$SetN::probe@9 (line 796)
                                                            ;   {runtime_call throw_div0_exception Runtime1 stub}
  0x00007f81d4d33f31:   callq  0x00007f81d480afa0           ; ImmutableOopMap {rsi=Oop [48]=Oop rdi=Oop [56]=Oop rbx=Oop }
                                                            ;*aaload {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN::probe@18 (line 798)
                                                            ;   {runtime_call throw_null_pointer_exception Runtime1 stub}
  0x00007f81d4d33f36:   mov    %rdx,(%rsp)
  0x00007f81d4d33f3a:   mov    %rbx,0x8(%rsp)
  0x00007f81d4d33f3f:   callq  0x00007f81d480b8a0           ; ImmutableOopMap {rsi=Oop [48]=Oop rdi=Oop [56]=Oop rbx=Oop }
                                                            ;*aaload {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN::probe@18 (line 798)
                                                            ;   {runtime_call throw_range_check_failed Runtime1 stub}
  0x00007f81d4d33f44:   callq  0x00007f81d480afa0           ; ImmutableOopMap {[56]=Oop rsi=Oop [48]=Oop }
                                                            ;*arraylength {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN::probe@47 (line 803)
                                                            ;   {runtime_call throw_null_pointer_exception Runtime1 stub}
  0x00007f81d4d33f49:   movabs $0x7f81d3f11bb8,%r10         ;   {metadata({method} {0x00007f81d3f11bb8} 'probe' '(Ljava/lang/Object;)I' in 'java/util/ImmutableCollections$SetN')}
  0x00007f81d4d33f53:   mov    %r10,0x8(%rsp)
  0x00007f81d4d33f58:   movq   $0x35,(%rsp)
  0x00007f81d4d33f60:   callq  0x00007f81d489e000           ; ImmutableOopMap {[56]=Oop rsi=Oop [48]=Oop }
                                                            ;*goto {reexecute=1 rethrow=0 return_oop=0}
                                                            ; - (reexecute) java.util.ImmutableCollections$SetN::probe@53 (line 806)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d33f65:   jmpq   0x00007f81d4d33e93
  0x00007f81d4d33f6a:   nop
  0x00007f81d4d33f6b:   nop
  0x00007f81d4d33f6c:   mov    0x3f0(%r15),%rax
  0x00007f81d4d33f73:   movabs $0x0,%r10
  0x00007f81d4d33f7d:   mov    %r10,0x3f0(%r15)
  0x00007f81d4d33f84:   movabs $0x0,%r10
  0x00007f81d4d33f8e:   mov    %r10,0x3f8(%r15)
  0x00007f81d4d33f95:   add    $0x50,%rsp
  0x00007f81d4d33f99:   pop    %rbp
  0x00007f81d4d33f9a:   jmpq   0x00007f81d480be80           ;   {runtime_call unwind_exception Runtime1 stub}
  0x00007f81d4d33f9f:   hlt    
[Stub Code]
  0x00007f81d4d33fa0:   nopl   0x0(%rax,%rax,1)             ;   {no_reloc}
  0x00007f81d4d33fa5:   movabs $0x0,%rbx                    ;   {static_stub}
  0x00007f81d4d33faf:   jmpq   0x00007f81d4d33faf           ;   {runtime_call}
  0x00007f81d4d33fb4:   nop
  0x00007f81d4d33fb5:   movabs $0x0,%rbx                    ;   {static_stub}
  0x00007f81d4d33fbf:   jmpq   0x00007f81d4d33fbf           ;   {runtime_call}
[Exception Handler]
  0x00007f81d4d33fc4:   callq  0x00007f81d489ad00           ;   {runtime_call handle_exception_from_callee Runtime1 stub}
  0x00007f81d4d33fc9:   mov    %rsp,-0x28(%rsp)
  0x00007f81d4d33fce:   sub    $0x80,%rsp
  0x00007f81d4d33fd5:   mov    %rax,0x78(%rsp)
  0x00007f81d4d33fda:   mov    %rcx,0x70(%rsp)
  0x00007f81d4d33fdf:   mov    %rdx,0x68(%rsp)
  0x00007f81d4d33fe4:   mov    %rbx,0x60(%rsp)
  0x00007f81d4d33fe9:   mov    %rbp,0x50(%rsp)
  0x00007f81d4d33fee:   mov    %rsi,0x48(%rsp)
  0x00007f81d4d33ff3:   mov    %rdi,0x40(%rsp)
  0x00007f81d4d33ff8:   mov    %r8,0x38(%rsp)
  0x00007f81d4d33ffd:   mov    %r9,0x30(%rsp)
  0x00007f81d4d34002:   mov    %r10,0x28(%rsp)
  0x00007f81d4d34007:   mov    %r11,0x20(%rsp)
  0x00007f81d4d3400c:   mov    %r12,0x18(%rsp)
  0x00007f81d4d34011:   mov    %r13,0x10(%rsp)
  0x00007f81d4d34016:   mov    %r14,0x8(%rsp)
  0x00007f81d4d3401b:   mov    %r15,(%rsp)
  0x00007f81d4d3401f:   movabs $0x7f81f15ff3e2,%rdi         ;   {external_word}
  0x00007f81d4d34029:   movabs $0x7f81d4d33fc9,%rsi         ;   {internal_word}
  0x00007f81d4d34033:   mov    %rsp,%rdx
  0x00007f81d4d34036:   and    $0xfffffffffffffff0,%rsp
  0x00007f81d4d3403a:   callq  0x00007f81f1108240           ;   {runtime_call}
  0x00007f81d4d3403f:   hlt    
[Deopt Handler Code]
  0x00007f81d4d34040:   movabs $0x7f81d4d34040,%r10         ;   {section_word}
  0x00007f81d4d3404a:   push   %r10
  0x00007f81d4d3404c:   jmpq   0x00007f81d47ed0a0           ;   {runtime_call DeoptimizationBlob}
  0x00007f81d4d34051:   hlt    
  0x00007f81d4d34052:   hlt    
  0x00007f81d4d34053:   hlt    
  0x00007f81d4d34054:   hlt    
  0x00007f81d4d34055:   hlt    
  0x00007f81d4d34056:   hlt    
  0x00007f81d4d34057:   hlt    
--------------------------------------------------------------------------------

Compiled method (n/a)     103   23     n 0       jdk.internal.misc.Unsafe::getReferenceVolatile (native)
 total in heap  [0x00007f81dc26b310,0x00007f81dc26b6b0] = 928
 relocation     [0x00007f81dc26b470,0x00007f81dc26b4a0] = 48
 main code      [0x00007f81dc26b4a0,0x00007f81dc26b6b0] = 528

--------------------------------------------------------------------------------
[Constant Pool (empty)]

--------------------------------------------------------------------------------

[Entry Point]
  # {method} {0x00007f81d3dbfb08} 'getReferenceVolatile' '(Ljava/lang/Object;J)Ljava/lang/Object;' in 'jdk/internal/misc/Unsafe'
  # this:     rsi:rsi   = 'jdk/internal/misc/Unsafe'
  # parm0:    rdx:rdx   = 'java/lang/Object'
  # parm1:    rcx:rcx   = long
  #           [sp+0x50]  (sp of caller)
  0x00007f81dc26b4a0:   mov    0x8(%rsi),%r10d
  0x00007f81dc26b4a4:   shl    $0x3,%r10
  0x00007f81dc26b4a8:   cmp    %r10,%rax
  0x00007f81dc26b4ab:   je     0x00007f81dc26b4b8
  0x00007f81dc26b4b1:   jmpq   0x00007f81d47eed00           ;   {runtime_call ic_miss_stub}
  0x00007f81dc26b4b6:   xchg   %ax,%ax
[Verified Entry Point]
  0x00007f81dc26b4b8:   mov    %eax,-0x14000(%rsp)
  0x00007f81dc26b4bf:   push   %rbp
  0x00007f81dc26b4c0:   mov    %rsp,%rbp
  0x00007f81dc26b4c3:   sub    $0x40,%rsp
  0x00007f81dc26b4c7:   mov    %rdx,0x8(%rsp)
  0x00007f81dc26b4cc:   cmp    $0x0,%rdx
  0x00007f81dc26b4d0:   lea    0x8(%rsp),%rdx
  0x00007f81dc26b4d5:   cmove  0x8(%rsp),%rdx
  0x00007f81dc26b4db:   mov    %rsi,(%rsp)
  0x00007f81dc26b4df:   cmp    $0x0,%rsi
  0x00007f81dc26b4e3:   lea    (%rsp),%rsi
  0x00007f81dc26b4e7:   cmove  (%rsp),%rsi
  0x00007f81dc26b4ec:   vzeroupper 
  0x00007f81dc26b4ef:   movabs $0x7f81dc26b4ec,%r10         ;   {internal_word}
  0x00007f81dc26b4f9:   mov    %r10,0x2f8(%r15)
  0x00007f81dc26b500:   mov    %rsp,0x2f0(%r15)
  0x00007f81dc26b507:   cmpb   $0x0,0x1572b1c9(%rip)        # 0x00007f81f19966d7
                                                            ;   {external_word}
  0x00007f81dc26b50e:   je     0x00007f81dc26b54a
  0x00007f81dc26b514:   push   %rsi
  0x00007f81dc26b515:   push   %rdx
  0x00007f81dc26b516:   push   %rcx
  0x00007f81dc26b517:   movabs $0x7f81d3dbfb08,%rsi         ;   {metadata({method} {0x00007f81d3dbfb08} 'getReferenceVolatile' '(Ljava/lang/Object;J)Ljava/lang/Object;' in 'jdk/internal/misc/Unsafe')}
  0x00007f81dc26b521:   mov    %r15,%rdi
  0x00007f81dc26b524:   test   $0xf,%esp
  0x00007f81dc26b52a:   je     0x00007f81dc26b542
  0x00007f81dc26b530:   sub    $0x8,%rsp
  0x00007f81dc26b534:   callq  0x00007f81f12e97a0           ;   {runtime_call}
  0x00007f81dc26b539:   add    $0x8,%rsp
  0x00007f81dc26b53d:   jmpq   0x00007f81dc26b547
  0x00007f81dc26b542:   callq  0x00007f81f12e97a0           ;   {runtime_call}
  0x00007f81dc26b547:   pop    %rcx
  0x00007f81dc26b548:   pop    %rdx
  0x00007f81dc26b549:   pop    %rsi
  0x00007f81dc26b54a:   lea    0x310(%r15),%rdi
  0x00007f81dc26b551:   movl   $0x4,0x388(%r15)
  0x00007f81dc26b55c:   callq  0x00007f81f152bdf0           ;   {runtime_call}
  0x00007f81dc26b561:   vzeroupper 
  0x00007f81dc26b564:   movl   $0x5,0x388(%r15)
  0x00007f81dc26b56f:   lock addl $0x0,-0x40(%rsp)
  0x00007f81dc26b575:   testb  $0x8,0x108(%r15)
  0x00007f81dc26b57d:   jne    0x00007f81dc26b594
  0x00007f81dc26b583:   cmpl   $0x0,0xd8(%r15)
  0x00007f81dc26b58e:   je     0x00007f81dc26b5b8
  0x00007f81dc26b594:   vzeroupper 
  0x00007f81dc26b597:   mov    %rax,-0x8(%rbp)
  0x00007f81dc26b59b:   mov    %r15,%rdi
  0x00007f81dc26b59e:   mov    %rsp,%r12
  0x00007f81dc26b5a1:   sub    $0x0,%rsp
  0x00007f81dc26b5a5:   and    $0xfffffffffffffff0,%rsp
  0x00007f81dc26b5a9:   callq  0x00007f81f14fd040           ;   {runtime_call}
  0x00007f81dc26b5ae:   mov    %r12,%rsp
  0x00007f81dc26b5b1:   xor    %r12,%r12
  0x00007f81dc26b5b4:   mov    -0x8(%rbp),%rax
  0x00007f81dc26b5b8:   movl   $0x8,0x388(%r15)
  0x00007f81dc26b5c3:   cmpl   $0x2,0x3d8(%r15)
  0x00007f81dc26b5ce:   je     0x00007f81dc26b685
  0x00007f81dc26b5d4:   cmpb   $0x0,0x1572b0fc(%rip)        # 0x00007f81f19966d7
                                                            ;   {external_word}
  0x00007f81dc26b5db:   je     0x00007f81dc26b619
  0x00007f81dc26b5e1:   mov    %rax,-0x8(%rbp)
  0x00007f81dc26b5e5:   movabs $0x7f81d3dbfb08,%rsi         ;   {metadata({method} {0x00007f81d3dbfb08} 'getReferenceVolatile' '(Ljava/lang/Object;J)Ljava/lang/Object;' in 'jdk/internal/misc/Unsafe')}
  0x00007f81dc26b5ef:   mov    %r15,%rdi
  0x00007f81dc26b5f2:   test   $0xf,%esp
  0x00007f81dc26b5f8:   je     0x00007f81dc26b610
  0x00007f81dc26b5fe:   sub    $0x8,%rsp
  0x00007f81dc26b602:   callq  0x00007f81f12e97c0           ;   {runtime_call}
  0x00007f81dc26b607:   add    $0x8,%rsp
  0x00007f81dc26b60b:   jmpq   0x00007f81dc26b615
  0x00007f81dc26b610:   callq  0x00007f81f12e97c0           ;   {runtime_call}
  0x00007f81dc26b615:   mov    -0x8(%rbp),%rax
  0x00007f81dc26b619:   movabs $0x0,%r10
  0x00007f81dc26b623:   mov    %r10,0x2f0(%r15)
  0x00007f81dc26b62a:   movabs $0x0,%r10
  0x00007f81dc26b634:   mov    %r10,0x2f8(%r15)
  0x00007f81dc26b63b:   vzeroupper 
  0x00007f81dc26b63e:   test   %rax,%rax
  0x00007f81dc26b641:   je     0x00007f81dc26b65f
  0x00007f81dc26b647:   test   $0x1,%rax
  0x00007f81dc26b64d:   je     0x00007f81dc26b65c
  0x00007f81dc26b653:   mov    -0x1(%rax),%rax
  0x00007f81dc26b657:   jmpq   0x00007f81dc26b65f
  0x00007f81dc26b65c:   mov    (%rax),%rax
  0x00007f81dc26b65f:   mov    0xe0(%r15),%rcx
  0x00007f81dc26b666:   movl   $0x0,0x100(%rcx)
  0x00007f81dc26b670:   leaveq 
  0x00007f81dc26b671:   cmpq   $0x0,0x8(%r15)
  0x00007f81dc26b679:   jne    0x00007f81dc26b680
  0x00007f81dc26b67f:   retq   
  0x00007f81dc26b680:   jmpq   Stub::forward exception      ;   {runtime_call StubRoutines (1)}
  0x00007f81dc26b685:   vzeroupper 
  0x00007f81dc26b688:   mov    %rax,-0x8(%rbp)
  0x00007f81dc26b68c:   mov    %rsp,%r12
  0x00007f81dc26b68f:   sub    $0x0,%rsp
  0x00007f81dc26b693:   and    $0xfffffffffffffff0,%rsp
  0x00007f81dc26b697:   callq  0x00007f81f12ea530           ;   {runtime_call}
  0x00007f81dc26b69c:   mov    %r12,%rsp
  0x00007f81dc26b69f:   xor    %r12,%r12
  0x00007f81dc26b6a2:   mov    -0x8(%rbp),%rax
  0x00007f81dc26b6a6:   jmpq   0x00007f81dc26b5d4
  0x00007f81dc26b6ab:   hlt    
  0x00007f81dc26b6ac:   hlt    
  0x00007f81dc26b6ad:   hlt    
  0x00007f81dc26b6ae:   hlt    
  0x00007f81dc26b6af:   hlt    
--------------------------------------------------------------------------------

============================= C1-compiled nmethod ==============================
----------------------------------- Assembly -----------------------------------

Compiled method (c1)     109   20       3       java.util.ImmutableCollections$SetN$SetNIterator::next (47 bytes)
 total in heap  [0x00007f81d4d34410,0x00007f81d4d34b88] = 1912
 relocation     [0x00007f81d4d34570,0x00007f81d4d345d0] = 96
 main code      [0x00007f81d4d345e0,0x00007f81d4d34860] = 640
 stub code      [0x00007f81d4d34860,0x00007f81d4d34918] = 184
 metadata       [0x00007f81d4d34918,0x00007f81d4d34920] = 8
 scopes data    [0x00007f81d4d34920,0x00007f81d4d349b8] = 152
 scopes pcs     [0x00007f81d4d349b8,0x00007f81d4d34b68] = 432
 dependencies   [0x00007f81d4d34b68,0x00007f81d4d34b70] = 8
 nul chk table  [0x00007f81d4d34b70,0x00007f81d4d34b88] = 24

--------------------------------------------------------------------------------
[Constant Pool (empty)]

--------------------------------------------------------------------------------

[Entry Point]
  # {method} {0x00007f81d3f883f8} 'next' '()Ljava/lang/Object;' in 'java/util/ImmutableCollections$SetN$SetNIterator'
  #           [sp+0x50]  (sp of caller)
  0x00007f81d4d345e0:   mov    0x8(%rsi),%r10d
  0x00007f81d4d345e4:   shl    $0x3,%r10
  0x00007f81d4d345e8:   cmp    %rax,%r10
  0x00007f81d4d345eb:   jne    0x00007f81d47eed00           ;   {runtime_call ic_miss_stub}
  0x00007f81d4d345f1:   data16 data16 nopw 0x0(%rax,%rax,1)
  0x00007f81d4d345fc:   data16 data16 xchg %ax,%ax
[Verified Entry Point]
  0x00007f81d4d34600:   mov    %eax,-0x14000(%rsp)
  0x00007f81d4d34607:   push   %rbp
  0x00007f81d4d34608:   sub    $0x40,%rsp
  0x00007f81d4d3460c:   mov    %rsi,0x28(%rsp)
  0x00007f81d4d34611:   movabs $0x7f81d3f95558,%rdi         ;   {metadata(method data for {method} {0x00007f81d3f883f8} 'next' '()Ljava/lang/Object;' in 'java/util/ImmutableCollections$SetN$SetNIterator')}
  0x00007f81d4d3461b:   mov    0x13c(%rdi),%ebx
  0x00007f81d4d34621:   add    $0x8,%ebx
  0x00007f81d4d34624:   mov    %ebx,0x13c(%rdi)
  0x00007f81d4d3462a:   and    $0x1ff8,%ebx
  0x00007f81d4d34630:   cmp    $0x0,%ebx
  0x00007f81d4d34633:   je     0x00007f81d4d347a6           ;*aload_0 {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::next@0 (line 763)
  0x00007f81d4d34639:   mov    0xc(%rsi),%edi               ;*getfield remaining {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::next@1 (line 763)
  0x00007f81d4d3463c:   cmp    $0x0,%edi
  0x00007f81d4d3463f:   movabs $0x7f81d3f95558,%rdi         ;   {metadata(method data for {method} {0x00007f81d3f883f8} 'next' '()Ljava/lang/Object;' in 'java/util/ImmutableCollections$SetN$SetNIterator')}
  0x00007f81d4d34649:   movabs $0x180,%rbx
  0x00007f81d4d34653:   jle    0x00007f81d4d34663
  0x00007f81d4d34659:   movabs $0x190,%rbx
  0x00007f81d4d34663:   mov    (%rdi,%rbx,1),%rax
  0x00007f81d4d34667:   lea    0x1(%rax),%rax
  0x00007f81d4d3466b:   mov    %rax,(%rdi,%rbx,1)
  0x00007f81d4d3466f:   jle    0x00007f81d4d34762
  0x00007f81d4d34675:   jmpq   0x00007f81d4d346c7           ;*ifle {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::next@4 (line 763)
  0x00007f81d4d3467a:   nopw   0x0(%rax,%rax,1)
  0x00007f81d4d34680:   movabs $0x7f81d3f95558,%rax         ;   {metadata(method data for {method} {0x00007f81d3f883f8} 'next' '()Ljava/lang/Object;' in 'java/util/ImmutableCollections$SetN$SetNIterator')}
  0x00007f81d4d3468a:   mov    0x140(%rax),%edx
  0x00007f81d4d34690:   add    $0x8,%edx
  0x00007f81d4d34693:   mov    %edx,0x140(%rax)
  0x00007f81d4d34699:   and    $0xfff8,%edx
  0x00007f81d4d3469f:   cmp    $0x0,%edx
  0x00007f81d4d346a2:   je     0x00007f81d4d347c7           ;*goto {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::next@24 (line 766)
  0x00007f81d4d346a8:   mov    0x108(%r15),%r10             ; ImmutableOopMap {[40]=Oop }
                                                            ;*goto {reexecute=1 rethrow=0 return_oop=0}
                                                            ; - (reexecute) java.util.ImmutableCollections$SetN$SetNIterator::next@24 (line 766)
  0x00007f81d4d346af:   test   %eax,(%r10)                  ;   {poll}
  0x00007f81d4d346b2:   movabs $0x7f81d3f95558,%rax         ;   {metadata(method data for {method} {0x00007f81d3f883f8} 'next' '()Ljava/lang/Object;' in 'java/util/ImmutableCollections$SetN$SetNIterator')}
  0x00007f81d4d346bc:   incl   0x1f8(%rax)                  ;*goto {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::next@24 (line 766)
  0x00007f81d4d346c2:   mov    0x28(%rsp),%rsi
  0x00007f81d4d346c7:   mov    0x14(%rsi),%edi              ;*getfield this$0 {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::next@8 (line 766)
  0x00007f81d4d346ca:   mov    0x10(%rdi),%edi              ; implicit exception: dispatches to 0x00007f81d4d347e8
                                                            ;*getfield elements {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::next@11 (line 766)
  0x00007f81d4d346cd:   mov    %rsi,%rbx
  0x00007f81d4d346d0:   movabs $0x7f81d3f95558,%rax         ;   {metadata(method data for {method} {0x00007f81d3f883f8} 'next' '()Ljava/lang/Object;' in 'java/util/ImmutableCollections$SetN$SetNIterator')}
  0x00007f81d4d346da:   addq   $0x1,0x1a0(%rax)
  0x00007f81d4d346e2:   mov    %rsi,%rbx
  0x00007f81d4d346e5:   mov    %rbx,%rsi                    ;*invokevirtual nextIndex {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::next@15 (line 766)
  0x00007f81d4d346e8:   mov    %rdi,0x20(%rsp)
  0x00007f81d4d346ed:   xchg   %ax,%ax
  0x00007f81d4d346ef:   callq  0x00007f81d47eea00           ; ImmutableOopMap {[40]=Oop [32]=Oop }
                                                            ;*invokevirtual nextIndex {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::next@15 (line 766)
                                                            ;   {optimized virtual_call}
  0x00007f81d4d346f4:   mov    0x20(%rsp),%rdi
  0x00007f81d4d346f9:   mov    0xc(%rdi),%edx               ; implicit exception: dispatches to 0x00007f81d4d347ed
  0x00007f81d4d346fc:   cmp    %eax,%edx
  0x00007f81d4d346fe:   jbe    0x00007f81d4d347f2
  0x00007f81d4d34704:   movslq %eax,%rax
  0x00007f81d4d34707:   mov    0x10(%rdi,%rax,4),%eax       ;*aaload {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::next@18 (line 766)
  0x00007f81d4d3470b:   cmp    $0x0,%rax
  0x00007f81d4d3470f:   movabs $0x7f81d3f95558,%rdx         ;   {metadata(method data for {method} {0x00007f81d3f883f8} 'next' '()Ljava/lang/Object;' in 'java/util/ImmutableCollections$SetN$SetNIterator')}
  0x00007f81d4d34719:   movabs $0x1e8,%rsi
  0x00007f81d4d34723:   je     0x00007f81d4d34733
  0x00007f81d4d34729:   movabs $0x1d8,%rsi
  0x00007f81d4d34733:   mov    (%rdx,%rsi,1),%rdi
  0x00007f81d4d34737:   lea    0x1(%rdi),%rdi
  0x00007f81d4d3473b:   mov    %rdi,(%rdx,%rsi,1)
  0x00007f81d4d3473f:   je     0x00007f81d4d34680           ;*ifnonnull {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::next@21 (line 766)
  0x00007f81d4d34745:   mov    0x28(%rsp),%rsi
  0x00007f81d4d3474a:   mov    0xc(%rsi),%edx               ;*getfield remaining {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::next@29 (line 767)
  0x00007f81d4d3474d:   dec    %edx
  0x00007f81d4d3474f:   mov    %edx,0xc(%rsi)               ;*putfield remaining {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::next@34 (line 767)
  0x00007f81d4d34752:   add    $0x40,%rsp
  0x00007f81d4d34756:   pop    %rbp
  0x00007f81d4d34757:   mov    0x108(%r15),%r10
  0x00007f81d4d3475e:   test   %eax,(%r10)                  ;   {poll_return}
  0x00007f81d4d34761:   retq                                ;*areturn {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::next@38 (line 768)
  0x00007f81d4d34762:   nopw   0x0(%rax,%rax,1)
  0x00007f81d4d34768:   jmpq   0x00007f81d4d3480f           ;   {no_reloc}
  0x00007f81d4d3476d:   add    %al,(%rax)
  0x00007f81d4d3476f:   add    %al,(%rax)
  0x00007f81d4d34771:   add    %ch,%cl
  0x00007f81d4d34773:   movabs %al,0xbf48f08b48000000       ;*new {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::next@39 (line 770)
                                                            ;   {metadata(method data for {method} {0x00007f81d3f883f8} 'next' '()Ljava/lang/Object;' in 'java/util/ImmutableCollections$SetN$SetNIterator')}
  0x00007f81d4d3477c:   pop    %rax
  0x00007f81d4d3477d:   push   %rbp
  0x00007f81d4d3477e:   stc    
  0x00007f81d4d3477f:   roll   %cl,0x4800007f(%rcx)
  0x00007f81d4d34785:   addl   $0x1,0x210(%rdi)
  0x00007f81d4d3478c:   mov    %rax,%rsi                    ;*invokespecial <init> {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::next@43 (line 770)
  0x00007f81d4d3478f:   mov    %rax,0x30(%rsp)
  0x00007f81d4d34794:   data16 xchg %ax,%ax
  0x00007f81d4d34797:   callq  0x00007f81d47eea00           ; ImmutableOopMap {[48]=Oop }
                                                            ;*invokespecial <init> {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::next@43 (line 770)
                                                            ;   {optimized virtual_call}
  0x00007f81d4d3479c:   mov    0x30(%rsp),%rax
  0x00007f81d4d347a1:   jmpq   0x00007f81d4d34851
  0x00007f81d4d347a6:   movabs $0x7f81d3f883f8,%r10         ;   {metadata({method} {0x00007f81d3f883f8} 'next' '()Ljava/lang/Object;' in 'java/util/ImmutableCollections$SetN$SetNIterator')}
  0x00007f81d4d347b0:   mov    %r10,0x8(%rsp)
  0x00007f81d4d347b5:   movq   $0xffffffffffffffff,(%rsp)
  0x00007f81d4d347bd:   callq  0x00007f81d489e000           ; ImmutableOopMap {rsi=Oop [40]=Oop }
                                                            ;*synchronization entry
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::next@-1 (line 763)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d347c2:   jmpq   0x00007f81d4d34639
  0x00007f81d4d347c7:   movabs $0x7f81d3f883f8,%r10         ;   {metadata({method} {0x00007f81d3f883f8} 'next' '()Ljava/lang/Object;' in 'java/util/ImmutableCollections$SetN$SetNIterator')}
  0x00007f81d4d347d1:   mov    %r10,0x8(%rsp)
  0x00007f81d4d347d6:   movq   $0x18,(%rsp)
  0x00007f81d4d347de:   callq  0x00007f81d489e000           ; ImmutableOopMap {[40]=Oop }
                                                            ;*goto {reexecute=1 rethrow=0 return_oop=0}
                                                            ; - (reexecute) java.util.ImmutableCollections$SetN$SetNIterator::next@24 (line 766)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d347e3:   jmpq   0x00007f81d4d346a8
  0x00007f81d4d347e8:   callq  0x00007f81d480afa0           ; ImmutableOopMap {rsi=Oop [40]=Oop }
                                                            ;*getfield elements {reexecute=1 rethrow=0 return_oop=0}
                                                            ; - (reexecute) java.util.ImmutableCollections$SetN$SetNIterator::next@11 (line 766)
                                                            ;   {runtime_call throw_null_pointer_exception Runtime1 stub}
  0x00007f81d4d347ed:   callq  0x00007f81d480afa0           ; ImmutableOopMap {[40]=Oop rdi=Oop }
                                                            ;*aaload {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::next@18 (line 766)
                                                            ;   {runtime_call throw_null_pointer_exception Runtime1 stub}
  0x00007f81d4d347f2:   mov    %rax,(%rsp)
  0x00007f81d4d347f6:   mov    %rdi,0x8(%rsp)
  0x00007f81d4d347fb:   callq  0x00007f81d480b8a0           ; ImmutableOopMap {[40]=Oop rdi=Oop }
                                                            ;*aaload {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::next@18 (line 766)
                                                            ;   {runtime_call throw_range_check_failed Runtime1 stub}
  0x00007f81d4d34800:   movabs $0x0,%rdx                    ;   {metadata(NULL)}
  0x00007f81d4d3480a:   mov    $0xa050f00,%eax
  0x00007f81d4d3480f:   callq  0x00007f81d489cf80           ; ImmutableOopMap {}
                                                            ;*new {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::next@39 (line 770)
                                                            ;   {runtime_call load_klass_patching Runtime1 stub}
  0x00007f81d4d34814:   jmpq   0x00007f81d4d34768
  0x00007f81d4d34819:   mov    %rdx,%rdx
  0x00007f81d4d3481c:   callq  0x00007f81d480a980           ; ImmutableOopMap {}
                                                            ;*new {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::next@39 (line 770)
                                                            ;   {runtime_call new_instance Runtime1 stub}
  0x00007f81d4d34821:   jmpq   0x00007f81d4d34777
  0x00007f81d4d34826:   nop
  0x00007f81d4d34827:   nop
  0x00007f81d4d34828:   mov    0x3f0(%r15),%rax
  0x00007f81d4d3482f:   movabs $0x0,%r10
  0x00007f81d4d34839:   mov    %r10,0x3f0(%r15)
  0x00007f81d4d34840:   movabs $0x0,%r10
  0x00007f81d4d3484a:   mov    %r10,0x3f8(%r15)
  0x00007f81d4d34851:   add    $0x40,%rsp
  0x00007f81d4d34855:   pop    %rbp
  0x00007f81d4d34856:   jmpq   0x00007f81d480be80           ;   {runtime_call unwind_exception Runtime1 stub}
  0x00007f81d4d3485b:   hlt    
  0x00007f81d4d3485c:   hlt    
  0x00007f81d4d3485d:   hlt    
  0x00007f81d4d3485e:   hlt    
  0x00007f81d4d3485f:   hlt    
[Stub Code]
  0x00007f81d4d34860:   nopl   0x0(%rax,%rax,1)             ;   {no_reloc}
  0x00007f81d4d34865:   movabs $0x0,%rbx                    ;   {static_stub}
  0x00007f81d4d3486f:   jmpq   0x00007f81d4d3486f           ;   {runtime_call}
  0x00007f81d4d34874:   nop
  0x00007f81d4d34875:   movabs $0x0,%rbx                    ;   {static_stub}
  0x00007f81d4d3487f:   jmpq   0x00007f81d4d3487f           ;   {runtime_call}
[Exception Handler]
  0x00007f81d4d34884:   callq  0x00007f81d489ad00           ;   {runtime_call handle_exception_from_callee Runtime1 stub}
  0x00007f81d4d34889:   mov    %rsp,-0x28(%rsp)
  0x00007f81d4d3488e:   sub    $0x80,%rsp
  0x00007f81d4d34895:   mov    %rax,0x78(%rsp)
  0x00007f81d4d3489a:   mov    %rcx,0x70(%rsp)
  0x00007f81d4d3489f:   mov    %rdx,0x68(%rsp)
  0x00007f81d4d348a4:   mov    %rbx,0x60(%rsp)
  0x00007f81d4d348a9:   mov    %rbp,0x50(%rsp)
  0x00007f81d4d348ae:   mov    %rsi,0x48(%rsp)
  0x00007f81d4d348b3:   mov    %rdi,0x40(%rsp)
  0x00007f81d4d348b8:   mov    %r8,0x38(%rsp)
  0x00007f81d4d348bd:   mov    %r9,0x30(%rsp)
  0x00007f81d4d348c2:   mov    %r10,0x28(%rsp)
  0x00007f81d4d348c7:   mov    %r11,0x20(%rsp)
  0x00007f81d4d348cc:   mov    %r12,0x18(%rsp)
  0x00007f81d4d348d1:   mov    %r13,0x10(%rsp)
  0x00007f81d4d348d6:   mov    %r14,0x8(%rsp)
  0x00007f81d4d348db:   mov    %r15,(%rsp)
  0x00007f81d4d348df:   movabs $0x7f81f15ff3e2,%rdi         ;   {external_word}
  0x00007f81d4d348e9:   movabs $0x7f81d4d34889,%rsi         ;   {internal_word}
  0x00007f81d4d348f3:   mov    %rsp,%rdx
  0x00007f81d4d348f6:   and    $0xfffffffffffffff0,%rsp
  0x00007f81d4d348fa:   callq  0x00007f81f1108240           ;   {runtime_call}
  0x00007f81d4d348ff:   hlt    
[Deopt Handler Code]
  0x00007f81d4d34900:   movabs $0x7f81d4d34900,%r10         ;   {section_word}
  0x00007f81d4d3490a:   push   %r10
  0x00007f81d4d3490c:   jmpq   0x00007f81d47ed0a0           ;   {runtime_call DeoptimizationBlob}
  0x00007f81d4d34911:   hlt    
  0x00007f81d4d34912:   hlt    
  0x00007f81d4d34913:   hlt    
  0x00007f81d4d34914:   hlt    
  0x00007f81d4d34915:   hlt    
  0x00007f81d4d34916:   hlt    
  0x00007f81d4d34917:   hlt    
--------------------------------------------------------------------------------

============================= C1-compiled nmethod ==============================
----------------------------------- Assembly -----------------------------------

Compiled method (c1)     119    8       3       java.lang.StringLatin1::equals (36 bytes)
 total in heap  [0x00007f81d4d34c10,0x00007f81d4d35268] = 1624
 relocation     [0x00007f81d4d34d70,0x00007f81d4d34db0] = 64
 main code      [0x00007f81d4d34dc0,0x00007f81d4d34fe0] = 544
 stub code      [0x00007f81d4d34fe0,0x00007f81d4d35070] = 144
 metadata       [0x00007f81d4d35070,0x00007f81d4d35078] = 8
 scopes data    [0x00007f81d4d35078,0x00007f81d4d350f8] = 128
 scopes pcs     [0x00007f81d4d350f8,0x00007f81d4d35248] = 336
 dependencies   [0x00007f81d4d35248,0x00007f81d4d35250] = 8
 nul chk table  [0x00007f81d4d35250,0x00007f81d4d35268] = 24

--------------------------------------------------------------------------------
[Constant Pool (empty)]

--------------------------------------------------------------------------------

[Verified Entry Point]
  # {method} {0x00007f81d3e6d4a0} 'equals' '([B[B)Z' in 'java/lang/StringLatin1'
  # parm0:    rsi:rsi   = '[B'
  # parm1:    rdx:rdx   = '[B'
  #           [sp+0x40]  (sp of caller)
  0x00007f81d4d34dc0:   mov    %eax,-0x14000(%rsp)
  0x00007f81d4d34dc7:   push   %rbp
  0x00007f81d4d34dc8:   sub    $0x30,%rsp
  0x00007f81d4d34dcc:   movabs $0x7f81d3f45a40,%rax         ;   {metadata(method data for {method} {0x00007f81d3e6d4a0} 'equals' '([B[B)Z' in 'java/lang/StringLatin1')}
  0x00007f81d4d34dd6:   mov    0x13c(%rax),%edi
  0x00007f81d4d34ddc:   add    $0x8,%edi
  0x00007f81d4d34ddf:   mov    %edi,0x13c(%rax)
  0x00007f81d4d34de5:   and    $0x1ff8,%edi
  0x00007f81d4d34deb:   cmp    $0x0,%edi
  0x00007f81d4d34dee:   je     0x00007f81d4d34f51           ;*aload_0 {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.StringLatin1::equals@0 (line 95)
  0x00007f81d4d34df4:   mov    0xc(%rsi),%eax               ; implicit exception: dispatches to 0x00007f81d4d34f72
                                                            ;*arraylength {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.StringLatin1::equals@1 (line 95)
  0x00007f81d4d34df7:   mov    0xc(%rdx),%edi               ; implicit exception: dispatches to 0x00007f81d4d34f77
                                                            ;*arraylength {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.StringLatin1::equals@3 (line 95)
  0x00007f81d4d34dfa:   cmp    %edi,%eax
  0x00007f81d4d34dfc:   movabs $0x7f81d3f45a40,%rdi         ;   {metadata(method data for {method} {0x00007f81d3e6d4a0} 'equals' '([B[B)Z' in 'java/lang/StringLatin1')}
  0x00007f81d4d34e06:   movabs $0x180,%rbx
  0x00007f81d4d34e10:   jne    0x00007f81d4d34e20
  0x00007f81d4d34e16:   movabs $0x190,%rbx
  0x00007f81d4d34e20:   mov    (%rdi,%rbx,1),%rcx
  0x00007f81d4d34e24:   lea    0x1(%rcx),%rcx
  0x00007f81d4d34e28:   mov    %rcx,(%rdi,%rbx,1)
  0x00007f81d4d34e2c:   jne    0x00007f81d4d34f3c           ;*if_icmpne {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.StringLatin1::equals@4 (line 95)
  0x00007f81d4d34e32:   mov    $0x0,%edi
  0x00007f81d4d34e37:   jmpq   0x00007f81d4d34ed5           ;*iload_2 {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.StringLatin1::equals@9 (line 96)
  0x00007f81d4d34e3c:   nopl   0x0(%rax)
  0x00007f81d4d34e40:   movslq %edi,%rbx
  0x00007f81d4d34e43:   movsbl 0x10(%rsi,%rbx,1),%ebx       ;*baload {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.StringLatin1::equals@17 (line 97)
  0x00007f81d4d34e48:   cmp    0xc(%rdx),%edi
  0x00007f81d4d34e4b:   jae    0x00007f81d4d34f7c
  0x00007f81d4d34e51:   movslq %edi,%rcx
  0x00007f81d4d34e54:   movsbl 0x10(%rdx,%rcx,1),%ecx       ;*baload {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.StringLatin1::equals@20 (line 97)
  0x00007f81d4d34e59:   cmp    %ecx,%ebx
  0x00007f81d4d34e5b:   movabs $0x7f81d3f45a40,%rbx         ;   {metadata(method data for {method} {0x00007f81d3e6d4a0} 'equals' '([B[B)Z' in 'java/lang/StringLatin1')}
  0x00007f81d4d34e65:   movabs $0x1d0,%rcx
  0x00007f81d4d34e6f:   jne    0x00007f81d4d34e7f
  0x00007f81d4d34e75:   movabs $0x1c0,%rcx
  0x00007f81d4d34e7f:   mov    (%rbx,%rcx,1),%r8
  0x00007f81d4d34e83:   lea    0x1(%r8),%r8
  0x00007f81d4d34e87:   mov    %r8,(%rbx,%rcx,1)
  0x00007f81d4d34e8b:   jne    0x00007f81d4d34f12           ;*if_icmpeq {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.StringLatin1::equals@21 (line 97)
  0x00007f81d4d34e91:   inc    %edi
  0x00007f81d4d34e93:   movabs $0x7f81d3f45a40,%rbx         ;   {metadata(method data for {method} {0x00007f81d3e6d4a0} 'equals' '([B[B)Z' in 'java/lang/StringLatin1')}
  0x00007f81d4d34e9d:   mov    0x140(%rbx),%ecx
  0x00007f81d4d34ea3:   add    $0x8,%ecx
  0x00007f81d4d34ea6:   mov    %ecx,0x140(%rbx)
  0x00007f81d4d34eac:   and    $0xfff8,%ecx
  0x00007f81d4d34eb2:   cmp    $0x0,%ecx
  0x00007f81d4d34eb5:   je     0x00007f81d4d34f8a           ;*goto {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.StringLatin1::equals@29 (line 96)
  0x00007f81d4d34ebb:   mov    0x108(%r15),%r10             ; ImmutableOopMap {rsi=Oop rdx=Oop }
                                                            ;*goto {reexecute=1 rethrow=0 return_oop=0}
                                                            ; - (reexecute) java.lang.StringLatin1::equals@29 (line 96)
  0x00007f81d4d34ec2:   test   %eax,(%r10)                  ;   {poll}
  0x00007f81d4d34ec5:   movabs $0x7f81d3f45a40,%rbx         ;   {metadata(method data for {method} {0x00007f81d3e6d4a0} 'equals' '([B[B)Z' in 'java/lang/StringLatin1')}
  0x00007f81d4d34ecf:   incl   0x1e0(%rbx)                  ;*goto {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.StringLatin1::equals@29 (line 96)
  0x00007f81d4d34ed5:   cmp    %eax,%edi
  0x00007f81d4d34ed7:   movabs $0x7f81d3f45a40,%rbx         ;   {metadata(method data for {method} {0x00007f81d3e6d4a0} 'equals' '([B[B)Z' in 'java/lang/StringLatin1')}
  0x00007f81d4d34ee1:   movabs $0x1a0,%rcx
  0x00007f81d4d34eeb:   jge    0x00007f81d4d34efb
  0x00007f81d4d34ef1:   movabs $0x1b0,%rcx
  0x00007f81d4d34efb:   mov    (%rbx,%rcx,1),%r8
  0x00007f81d4d34eff:   lea    0x1(%r8),%r8
  0x00007f81d4d34f03:   mov    %r8,(%rbx,%rcx,1)
  0x00007f81d4d34f07:   jge    0x00007f81d4d34f27
  0x00007f81d4d34f0d:   jmpq   0x00007f81d4d34e40           ;*if_icmpge {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.StringLatin1::equals@12 (line 96)
  0x00007f81d4d34f12:   mov    $0x0,%eax
  0x00007f81d4d34f17:   add    $0x30,%rsp
  0x00007f81d4d34f1b:   pop    %rbp
  0x00007f81d4d34f1c:   mov    0x108(%r15),%r10
  0x00007f81d4d34f23:   test   %eax,(%r10)                  ;   {poll_return}
  0x00007f81d4d34f26:   retq                                ;*ireturn {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.StringLatin1::equals@25 (line 98)
  0x00007f81d4d34f27:   mov    $0x1,%eax
  0x00007f81d4d34f2c:   add    $0x30,%rsp
  0x00007f81d4d34f30:   pop    %rbp
  0x00007f81d4d34f31:   mov    0x108(%r15),%r10
  0x00007f81d4d34f38:   test   %eax,(%r10)                  ;   {poll_return}
  0x00007f81d4d34f3b:   retq                                ;*ireturn {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.StringLatin1::equals@33 (line 101)
  0x00007f81d4d34f3c:   mov    $0x0,%eax
  0x00007f81d4d34f41:   add    $0x30,%rsp
  0x00007f81d4d34f45:   pop    %rbp
  0x00007f81d4d34f46:   mov    0x108(%r15),%r10
  0x00007f81d4d34f4d:   test   %eax,(%r10)                  ;   {poll_return}
  0x00007f81d4d34f50:   retq   
  0x00007f81d4d34f51:   movabs $0x7f81d3e6d4a0,%r10         ;   {metadata({method} {0x00007f81d3e6d4a0} 'equals' '([B[B)Z' in 'java/lang/StringLatin1')}
  0x00007f81d4d34f5b:   mov    %r10,0x8(%rsp)
  0x00007f81d4d34f60:   movq   $0xffffffffffffffff,(%rsp)
  0x00007f81d4d34f68:   callq  0x00007f81d489e000           ; ImmutableOopMap {rsi=Oop rdx=Oop }
                                                            ;*synchronization entry
                                                            ; - java.lang.StringLatin1::equals@-1 (line 95)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d34f6d:   jmpq   0x00007f81d4d34df4
  0x00007f81d4d34f72:   callq  0x00007f81d480afa0           ; ImmutableOopMap {rsi=Oop rdx=Oop }
                                                            ;*arraylength {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.StringLatin1::equals@1 (line 95)
                                                            ;   {runtime_call throw_null_pointer_exception Runtime1 stub}
  0x00007f81d4d34f77:   callq  0x00007f81d480afa0           ; ImmutableOopMap {rsi=Oop rdx=Oop }
                                                            ;*arraylength {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.StringLatin1::equals@3 (line 95)
                                                            ;   {runtime_call throw_null_pointer_exception Runtime1 stub}
  0x00007f81d4d34f7c:   mov    %rdi,(%rsp)
  0x00007f81d4d34f80:   mov    %rdx,0x8(%rsp)
  0x00007f81d4d34f85:   callq  0x00007f81d480b8a0           ; ImmutableOopMap {rsi=Oop rdx=Oop }
                                                            ;*baload {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.StringLatin1::equals@20 (line 97)
                                                            ;   {runtime_call throw_range_check_failed Runtime1 stub}
  0x00007f81d4d34f8a:   movabs $0x7f81d3e6d4a0,%r10         ;   {metadata({method} {0x00007f81d3e6d4a0} 'equals' '([B[B)Z' in 'java/lang/StringLatin1')}
  0x00007f81d4d34f94:   mov    %r10,0x8(%rsp)
  0x00007f81d4d34f99:   movq   $0x1d,(%rsp)
  0x00007f81d4d34fa1:   callq  0x00007f81d489e000           ; ImmutableOopMap {rsi=Oop rdx=Oop }
                                                            ;*goto {reexecute=1 rethrow=0 return_oop=0}
                                                            ; - (reexecute) java.lang.StringLatin1::equals@29 (line 96)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d34fa6:   jmpq   0x00007f81d4d34ebb
  0x00007f81d4d34fab:   nop
  0x00007f81d4d34fac:   nop
  0x00007f81d4d34fad:   mov    0x3f0(%r15),%rax
  0x00007f81d4d34fb4:   movabs $0x0,%r10
  0x00007f81d4d34fbe:   mov    %r10,0x3f0(%r15)
  0x00007f81d4d34fc5:   movabs $0x0,%r10
  0x00007f81d4d34fcf:   mov    %r10,0x3f8(%r15)
  0x00007f81d4d34fd6:   add    $0x30,%rsp
  0x00007f81d4d34fda:   pop    %rbp
  0x00007f81d4d34fdb:   jmpq   0x00007f81d480be80           ;   {runtime_call unwind_exception Runtime1 stub}
[Exception Handler]
  0x00007f81d4d34fe0:   callq  0x00007f81d489ad00           ;   {no_reloc}
  0x00007f81d4d34fe5:   mov    %rsp,-0x28(%rsp)
  0x00007f81d4d34fea:   sub    $0x80,%rsp
  0x00007f81d4d34ff1:   mov    %rax,0x78(%rsp)
  0x00007f81d4d34ff6:   mov    %rcx,0x70(%rsp)
  0x00007f81d4d34ffb:   mov    %rdx,0x68(%rsp)
  0x00007f81d4d35000:   mov    %rbx,0x60(%rsp)
  0x00007f81d4d35005:   mov    %rbp,0x50(%rsp)
  0x00007f81d4d3500a:   mov    %rsi,0x48(%rsp)
  0x00007f81d4d3500f:   mov    %rdi,0x40(%rsp)
  0x00007f81d4d35014:   mov    %r8,0x38(%rsp)
  0x00007f81d4d35019:   mov    %r9,0x30(%rsp)
  0x00007f81d4d3501e:   mov    %r10,0x28(%rsp)
  0x00007f81d4d35023:   mov    %r11,0x20(%rsp)
  0x00007f81d4d35028:   mov    %r12,0x18(%rsp)
  0x00007f81d4d3502d:   mov    %r13,0x10(%rsp)
  0x00007f81d4d35032:   mov    %r14,0x8(%rsp)
  0x00007f81d4d35037:   mov    %r15,(%rsp)
  0x00007f81d4d3503b:   movabs $0x7f81f15ff3e2,%rdi         ;   {external_word}
  0x00007f81d4d35045:   movabs $0x7f81d4d34fe5,%rsi         ;   {internal_word}
  0x00007f81d4d3504f:   mov    %rsp,%rdx
  0x00007f81d4d35052:   and    $0xfffffffffffffff0,%rsp
  0x00007f81d4d35056:   callq  0x00007f81f1108240           ;   {runtime_call}
  0x00007f81d4d3505b:   hlt    
[Deopt Handler Code]
  0x00007f81d4d3505c:   movabs $0x7f81d4d3505c,%r10         ;   {section_word}
  0x00007f81d4d35066:   push   %r10
  0x00007f81d4d35068:   jmpq   0x00007f81d47ed0a0           ;   {runtime_call DeoptimizationBlob}
  0x00007f81d4d3506d:   hlt    
  0x00007f81d4d3506e:   hlt    
  0x00007f81d4d3506f:   hlt    
--------------------------------------------------------------------------------

============================= C1-compiled nmethod ==============================
----------------------------------- Assembly -----------------------------------

Compiled method (c1)     127   33       3       java.util.HashMap::putVal (300 bytes)
 total in heap  [0x00007f81d4d35290,0x00007f81d4d373b8] = 8488
 relocation     [0x00007f81d4d353f0,0x00007f81d4d35530] = 320
 main code      [0x00007f81d4d35540,0x00007f81d4d366e0] = 4512
 stub code      [0x00007f81d4d366e0,0x00007f81d4d367d8] = 248
 metadata       [0x00007f81d4d367d8,0x00007f81d4d36810] = 56
 scopes data    [0x00007f81d4d36810,0x00007f81d4d36ca8] = 1176
 scopes pcs     [0x00007f81d4d36ca8,0x00007f81d4d37348] = 1696
 dependencies   [0x00007f81d4d37348,0x00007f81d4d37350] = 8
 nul chk table  [0x00007f81d4d37350,0x00007f81d4d373b8] = 104

--------------------------------------------------------------------------------
[Constant Pool (empty)]

--------------------------------------------------------------------------------

[Entry Point]
  # {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap'
  # this:     rsi:rsi   = 'java/util/HashMap'
  # parm0:    rdx       = int
  # parm1:    rcx:rcx   = 'java/lang/Object'
  # parm2:    r8:r8     = 'java/lang/Object'
  # parm3:    r9        = boolean
  # parm4:    rdi       = boolean
  #           [sp+0xf0]  (sp of caller)
  0x00007f81d4d35540:   mov    0x8(%rsi),%r10d
  0x00007f81d4d35544:   shl    $0x3,%r10
  0x00007f81d4d35548:   cmp    %rax,%r10
  0x00007f81d4d3554b:   jne    0x00007f81d47eed00           ;   {runtime_call ic_miss_stub}
  0x00007f81d4d35551:   data16 data16 nopw 0x0(%rax,%rax,1)
  0x00007f81d4d3555c:   data16 data16 xchg %ax,%ax
[Verified Entry Point]
  0x00007f81d4d35560:   mov    %eax,-0x14000(%rsp)
  0x00007f81d4d35567:   push   %rbp
  0x00007f81d4d35568:   sub    $0xe0,%rsp
  0x00007f81d4d3556f:   mov    %rsi,0xa0(%rsp)
  0x00007f81d4d35577:   mov    %edx,0x98(%rsp)
  0x00007f81d4d3557e:   mov    %rcx,0xa8(%rsp)
  0x00007f81d4d35586:   mov    %r8,0xb0(%rsp)
  0x00007f81d4d3558e:   mov    %edi,0xb8(%rsp)
  0x00007f81d4d35595:   movabs $0x7f81d3f9df18,%rbx         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d3559f:   mov    0x13c(%rbx),%eax
  0x00007f81d4d355a5:   add    $0x8,%eax
  0x00007f81d4d355a8:   mov    %eax,0x13c(%rbx)
  0x00007f81d4d355ae:   and    $0x1ff8,%eax
  0x00007f81d4d355b4:   cmp    $0x0,%eax
  0x00007f81d4d355b7:   je     0x00007f81d4d364bb           ;*aload_0 {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@0 (line 628)
  0x00007f81d4d355bd:   mov    0x24(%rsi),%ebx              ;*getfield table {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@1 (line 628)
  0x00007f81d4d355c0:   cmp    $0x0,%rbx
  0x00007f81d4d355c4:   movabs $0x7f81d3f9df18,%rax         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d355ce:   movabs $0x180,%r11
  0x00007f81d4d355d8:   je     0x00007f81d4d355e8
  0x00007f81d4d355de:   movabs $0x190,%r11
  0x00007f81d4d355e8:   mov    (%rax,%r11,1),%r13
  0x00007f81d4d355ec:   lea    0x1(%r13),%r13
  0x00007f81d4d355f0:   mov    %r13,(%rax,%r11,1)
  0x00007f81d4d355f4:   je     0x00007f81d4d35643           ;*ifnull {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@7 (line 628)
  0x00007f81d4d355fa:   mov    0xc(%rbx),%eax               ; implicit exception: dispatches to 0x00007f81d4d364dc
                                                            ;*arraylength {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@12 (line 628)
  0x00007f81d4d355fd:   cmp    $0x0,%eax
  0x00007f81d4d35600:   movabs $0x7f81d3f9df18,%r11         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d3560a:   movabs $0x1b0,%r13
  0x00007f81d4d35614:   je     0x00007f81d4d35624
  0x00007f81d4d3561a:   movabs $0x1a0,%r13
  0x00007f81d4d35624:   mov    (%r11,%r13,1),%r14
  0x00007f81d4d35628:   lea    0x1(%r14),%r14
  0x00007f81d4d3562c:   mov    %r14,(%r11,%r13,1)
  0x00007f81d4d35630:   je     0x00007f81d4d35643           ;*ifne {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@16 (line 628)
  0x00007f81d4d35636:   mov    %r9d,0x9c(%rsp)
  0x00007f81d4d3563e:   jmpq   0x00007f81d4d35678           ;*aload {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@29 (line 630)
  0x00007f81d4d35643:   mov    %r9d,0x9c(%rsp)
  0x00007f81d4d3564b:   mov    %rsi,%rbx
  0x00007f81d4d3564e:   movabs $0x7f81d3f9df18,%rax         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d35658:   addq   $0x1,0x1c0(%rax)
  0x00007f81d4d35660:   mov    %rsi,%rbx
  0x00007f81d4d35663:   mov    %rbx,%rsi                    ;*invokevirtual resize {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@20 (line 629)
  0x00007f81d4d35666:   nop
  0x00007f81d4d35667:   callq  0x00007f81d47eea00           ; ImmutableOopMap {[160]=Oop [176]=Oop [168]=Oop }
                                                            ;*invokevirtual resize {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@20 (line 629)
                                                            ;   {optimized virtual_call}
  0x00007f81d4d3566c:   mov    %rax,%rdx                    ;*invokevirtual resize {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@20 (line 629)
  0x00007f81d4d3566f:   mov    0xc(%rdx),%esi               ; implicit exception: dispatches to 0x00007f81d4d364e1
                                                            ;*arraylength {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@26 (line 629)
  0x00007f81d4d35672:   mov    %rsi,%rax
  0x00007f81d4d35675:   mov    %rdx,%rbx                    ;*aload {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@29 (line 630)
  0x00007f81d4d35678:   dec    %eax
  0x00007f81d4d3567a:   mov    %rax,%r8
  0x00007f81d4d3567d:   and    0x98(%rsp),%r8d
  0x00007f81d4d35685:   cmp    0xc(%rbx),%r8d               ; implicit exception: dispatches to 0x00007f81d4d364e6
  0x00007f81d4d35689:   jae    0x00007f81d4d364f0
  0x00007f81d4d3568f:   movslq %r8d,%rdx
  0x00007f81d4d35692:   mov    0x10(%rbx,%rdx,4),%edi       ;*aaload {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@40 (line 630)
  0x00007f81d4d35696:   cmp    $0x0,%rdi
  0x00007f81d4d3569a:   movabs $0x7f81d3f9df18,%rdx         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d356a4:   movabs $0x1f8,%rsi
  0x00007f81d4d356ae:   jne    0x00007f81d4d356be
  0x00007f81d4d356b4:   movabs $0x208,%rsi
  0x00007f81d4d356be:   mov    (%rdx,%rsi,1),%rax
  0x00007f81d4d356c2:   lea    0x1(%rax),%rax
  0x00007f81d4d356c6:   mov    %rax,(%rdx,%rsi,1)
  0x00007f81d4d356ca:   mov    0xb0(%rsp),%r9
  0x00007f81d4d356d2:   jne    0x00007f81d4d3595e           ;*ifnonnull {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@44 (line 630)
  0x00007f81d4d356d8:   mov    0xa8(%rsp),%rcx
  0x00007f81d4d356e0:   mov    0x98(%rsp),%edx
  0x00007f81d4d356e7:   mov    0xa0(%rsp),%rsi
  0x00007f81d4d356ef:   movabs $0x7f81d3f9df18,%rdi         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d356f9:   addq   $0x1,0x230(%rdi)
  0x00007f81d4d35701:   movabs $0x7f81d3f9ed80,%rsi         ;   {metadata(method data for {method} {0x00007f81d3e43970} 'newNode' '(ILjava/lang/Object;Ljava/lang/Object;Ljava/util/HashMap$Node;)Ljava/util/HashMap$Node;' in 'java/util/HashMap')}
  0x00007f81d4d3570b:   mov    0x13c(%rsi),%edi
  0x00007f81d4d35711:   add    $0x8,%edi
  0x00007f81d4d35714:   mov    %edi,0x13c(%rsi)
  0x00007f81d4d3571a:   and    $0x7ffff8,%edi
  0x00007f81d4d35720:   cmp    $0x0,%edi
  0x00007f81d4d35723:   je     0x00007f81d4d364fe
  0x00007f81d4d35729:   mov    %rdx,%r11
  0x00007f81d4d3572c:   movabs $0x100020330,%rdx            ;   {metadata('java/util/HashMap$Node')}
  0x00007f81d4d35736:   mov    %rcx,%r13
  0x00007f81d4d35739:   mov    0x118(%r15),%rax
  0x00007f81d4d35740:   lea    0x20(%rax),%rdi
  0x00007f81d4d35744:   cmp    0x128(%r15),%rdi
  0x00007f81d4d3574b:   ja     0x00007f81d4d3651f
  0x00007f81d4d35751:   mov    %rdi,0x118(%r15)
  0x00007f81d4d35758:   mov    0xb8(%rdx),%rcx
  0x00007f81d4d3575f:   mov    %rcx,(%rax)
  0x00007f81d4d35762:   mov    %rdx,%rcx
  0x00007f81d4d35765:   shr    $0x3,%rcx
  0x00007f81d4d35769:   mov    %ecx,0x8(%rax)
  0x00007f81d4d3576c:   xor    %rcx,%rcx
  0x00007f81d4d3576f:   mov    %ecx,0xc(%rax)
  0x00007f81d4d35772:   xor    %rcx,%rcx
  0x00007f81d4d35775:   mov    %rcx,0x10(%rax)
  0x00007f81d4d35779:   mov    %rcx,0x18(%rax)              ;*new {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::newNode@0 (line 1799)
                                                            ; - java.util.HashMap::putVal@56 (line 631)
  0x00007f81d4d3577d:   mov    %rax,%rdx
  0x00007f81d4d35780:   movabs $0x7f81d3f9ed80,%rsi         ;   {metadata(method data for {method} {0x00007f81d3e43970} 'newNode' '(ILjava/lang/Object;Ljava/lang/Object;Ljava/util/HashMap$Node;)Ljava/util/HashMap$Node;' in 'java/util/HashMap')}
  0x00007f81d4d3578a:   addq   $0x1,0x180(%rsi)
  0x00007f81d4d35792:   movabs $0x7f81d3f9ef50,%rdx         ;   {metadata(method data for {method} {0x00007f81d3e710e8} '<init>' '(ILjava/lang/Object;Ljava/lang/Object;Ljava/util/HashMap$Node;)V' in 'java/util/HashMap$Node')}
  0x00007f81d4d3579c:   mov    0x13c(%rdx),%esi
  0x00007f81d4d357a2:   add    $0x8,%esi
  0x00007f81d4d357a5:   mov    %esi,0x13c(%rdx)
  0x00007f81d4d357ab:   and    $0x7ffff8,%esi
  0x00007f81d4d357b1:   cmp    $0x0,%esi
  0x00007f81d4d357b4:   je     0x00007f81d4d3652c
  0x00007f81d4d357ba:   mov    %rax,%rdx
  0x00007f81d4d357bd:   movabs $0x7f81d3f9ef50,%rsi         ;   {metadata(method data for {method} {0x00007f81d3e710e8} '<init>' '(ILjava/lang/Object;Ljava/lang/Object;Ljava/util/HashMap$Node;)V' in 'java/util/HashMap$Node')}
  0x00007f81d4d357c7:   addq   $0x1,0x180(%rsi)
  0x00007f81d4d357cf:   movabs $0x7f81d3f33388,%rdx         ;   {metadata(method data for {method} {0x00007f81d3cfe650} '<init>' '()V' in 'java/lang/Object')}
  0x00007f81d4d357d9:   mov    0x13c(%rdx),%esi
  0x00007f81d4d357df:   add    $0x8,%esi
  0x00007f81d4d357e2:   mov    %esi,0x13c(%rdx)
  0x00007f81d4d357e8:   and    $0x7ffff8,%esi
  0x00007f81d4d357ee:   cmp    $0x0,%esi
  0x00007f81d4d357f1:   je     0x00007f81d4d3654d
  0x00007f81d4d357f7:   mov    %r11d,0xc(%rax)              ;*putfield hash {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap$Node::<init>@6 (line 286)
                                                            ; - java.util.HashMap::newNode@9 (line 1799)
                                                            ; - java.util.HashMap::putVal@56 (line 631)
  0x00007f81d4d357fb:   mov    %r13,%r10
  0x00007f81d4d357fe:   mov    %r10d,0x10(%rax)
  0x00007f81d4d35802:   mov    %rax,%rdx
  0x00007f81d4d35805:   shr    $0x9,%rdx
  0x00007f81d4d35809:   movabs $0x7f81d3eb8000,%rsi
  0x00007f81d4d35813:   movb   $0x0,(%rdx,%rsi,1)           ;*putfield key {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap$Node::<init>@11 (line 287)
                                                            ; - java.util.HashMap::newNode@9 (line 1799)
                                                            ; - java.util.HashMap::putVal@56 (line 631)
  0x00007f81d4d35817:   mov    %r9,%r10
  0x00007f81d4d3581a:   mov    %r10d,0x14(%rax)
  0x00007f81d4d3581e:   mov    %rax,%rdx
  0x00007f81d4d35821:   shr    $0x9,%rdx
  0x00007f81d4d35825:   movb   $0x0,(%rdx,%rsi,1)           ;*putfield value {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap$Node::<init>@16 (line 288)
                                                            ; - java.util.HashMap::newNode@9 (line 1799)
                                                            ; - java.util.HashMap::putVal@56 (line 631)
  0x00007f81d4d35829:   cmp    0xc(%rbx),%r8d
  0x00007f81d4d3582d:   jae    0x00007f81d4d3656e
  0x00007f81d4d35833:   cmp    $0x0,%rax
  0x00007f81d4d35837:   jne    0x00007f81d4d3584f
  0x00007f81d4d35839:   movabs $0x7f81d3f9df18,%rdi         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d35843:   orb    $0x1,0x249(%rdi)
  0x00007f81d4d3584a:   jmpq   0x00007f81d4d35933
  0x00007f81d4d3584f:   mov    0x8(%rbx),%edx               ; implicit exception: dispatches to 0x00007f81d4d3657c
  0x00007f81d4d35852:   shl    $0x3,%rdx
  0x00007f81d4d35856:   mov    0x8(%rax),%edi
  0x00007f81d4d35859:   shl    $0x3,%rdi
  0x00007f81d4d3585d:   mov    0xe8(%rdx),%rdx
  0x00007f81d4d35864:   cmp    %rdx,%rdi
  0x00007f81d4d35867:   je     0x00007f81d4d35895
  0x00007f81d4d3586d:   mov    0x10(%rdx),%ecx
  0x00007f81d4d35870:   cmp    (%rdi,%rcx,1),%rdx
  0x00007f81d4d35874:   je     0x00007f81d4d35895
  0x00007f81d4d3587a:   cmp    $0x20,%ecx
  0x00007f81d4d3587d:   jne    0x00007f81d4d3591c
  0x00007f81d4d35883:   push   %rdi
  0x00007f81d4d35884:   push   %rdx
  0x00007f81d4d35885:   callq  0x00007f81d489b900           ;   {runtime_call slow_subtype_check Runtime1 stub}
  0x00007f81d4d3588a:   pop    %rdi
  0x00007f81d4d3588b:   pop    %rdx
  0x00007f81d4d3588c:   cmp    $0x0,%edx
  0x00007f81d4d3588f:   je     0x00007f81d4d3591c
  0x00007f81d4d35895:   movabs $0x7f81d3f9df18,%rdi         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d3589f:   mov    0x8(%rax),%edx
  0x00007f81d4d358a2:   shl    $0x3,%rdx
  0x00007f81d4d358a6:   cmp    0x260(%rdi),%rdx
  0x00007f81d4d358ad:   jne    0x00007f81d4d358bc
  0x00007f81d4d358af:   addq   $0x1,0x268(%rdi)
  0x00007f81d4d358b7:   jmpq   0x00007f81d4d35933
  0x00007f81d4d358bc:   cmp    0x270(%rdi),%rdx
  0x00007f81d4d358c3:   jne    0x00007f81d4d358d2
  0x00007f81d4d358c5:   addq   $0x1,0x278(%rdi)
  0x00007f81d4d358cd:   jmpq   0x00007f81d4d35933
  0x00007f81d4d358d2:   cmpq   $0x0,0x260(%rdi)
  0x00007f81d4d358dd:   jne    0x00007f81d4d358f6
  0x00007f81d4d358df:   mov    %rdx,0x260(%rdi)
  0x00007f81d4d358e6:   movq   $0x1,0x268(%rdi)
  0x00007f81d4d358f1:   jmpq   0x00007f81d4d35933
  0x00007f81d4d358f6:   cmpq   $0x0,0x270(%rdi)
  0x00007f81d4d35901:   jne    0x00007f81d4d3591a
  0x00007f81d4d35903:   mov    %rdx,0x270(%rdi)
  0x00007f81d4d3590a:   movq   $0x1,0x278(%rdi)
  0x00007f81d4d35915:   jmpq   0x00007f81d4d35933
  0x00007f81d4d3591a:   jmp    0x00007f81d4d35933
  0x00007f81d4d3591c:   movabs $0x7f81d3f9df18,%rdi         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d35926:   subq   $0x1,0x250(%rdi)
  0x00007f81d4d3592e:   jmpq   0x00007f81d4d36581
  0x00007f81d4d35933:   movslq %r8d,%r8
  0x00007f81d4d35936:   lea    0x10(%rbx,%r8,4),%rdx
  0x00007f81d4d3593b:   mov    %rax,%r10
  0x00007f81d4d3593e:   mov    %r10d,(%rdx)
  0x00007f81d4d35941:   shr    $0x9,%rdx
  0x00007f81d4d35945:   movb   $0x0,(%rdx,%rsi,1)           ;*aastore {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@59 (line 631)
  0x00007f81d4d35949:   movabs $0x7f81d3f9df18,%rdx         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d35953:   incl   0x288(%rdx)
  0x00007f81d4d35959:   jmpq   0x00007f81d4d362e6           ;*goto {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@60 (line 631)
  0x00007f81d4d3595e:   mov    0xa8(%rsp),%r13
  0x00007f81d4d35966:   mov    0x98(%rsp),%r11d
  0x00007f81d4d3596e:   mov    0xc(%rdi),%edx               ; implicit exception: dispatches to 0x00007f81d4d3658a
                                                            ;*getfield hash {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@65 (line 634)
  0x00007f81d4d35971:   cmp    %r11d,%edx
  0x00007f81d4d35974:   movabs $0x7f81d3f9df18,%rdx         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d3597e:   movabs $0x2b0,%rsi
  0x00007f81d4d35988:   je     0x00007f81d4d35998
  0x00007f81d4d3598e:   movabs $0x2a0,%rsi
  0x00007f81d4d35998:   mov    (%rdx,%rsi,1),%rax
  0x00007f81d4d3599c:   lea    0x1(%rax),%rax
  0x00007f81d4d359a0:   mov    %rax,(%rdx,%rsi,1)
  0x00007f81d4d359a4:   je     0x00007f81d4d359b7           ;*if_icmpne {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@69 (line 634)
  0x00007f81d4d359aa:   mov    %rbx,0xc8(%rsp)
  0x00007f81d4d359b2:   jmpq   0x00007f81d4d35b35           ;*aload {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@104 (line 637)
  0x00007f81d4d359b7:   mov    0x10(%rdi),%edx              ;*getfield key {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@74 (line 634)
  0x00007f81d4d359ba:   cmp    %r13,%rdx
  0x00007f81d4d359bd:   movabs $0x7f81d3f9df18,%rsi         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d359c7:   movabs $0x2c0,%rax
  0x00007f81d4d359d1:   je     0x00007f81d4d359e1
  0x00007f81d4d359d7:   movabs $0x2d0,%rax
  0x00007f81d4d359e1:   mov    (%rsi,%rax,1),%rcx
  0x00007f81d4d359e5:   lea    0x1(%rcx),%rcx
  0x00007f81d4d359e9:   mov    %rcx,(%rsi,%rax,1)
  0x00007f81d4d359ed:   je     0x00007f81d4d36299           ;*if_acmpeq {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@81 (line 634)
  0x00007f81d4d359f3:   cmp    $0x0,%r13
  0x00007f81d4d359f7:   movabs $0x7f81d3f9df18,%rsi         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d35a01:   movabs $0x2f0,%rax
  0x00007f81d4d35a0b:   jne    0x00007f81d4d35a1b
  0x00007f81d4d35a11:   movabs $0x2e0,%rax
  0x00007f81d4d35a1b:   mov    (%rsi,%rax,1),%rcx
  0x00007f81d4d35a1f:   lea    0x1(%rcx),%rcx
  0x00007f81d4d35a23:   mov    %rcx,(%rsi,%rax,1)
  0x00007f81d4d35a27:   jne    0x00007f81d4d35a3a           ;*ifnull {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@85 (line 634)
  0x00007f81d4d35a2d:   mov    %rbx,0xc8(%rsp)
  0x00007f81d4d35a35:   jmpq   0x00007f81d4d35b35           ;*aload {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@104 (line 637)
  0x00007f81d4d35a3a:   mov    %rdi,0xc0(%rsp)
  0x00007f81d4d35a42:   mov    %rbx,0xc8(%rsp)
  0x00007f81d4d35a4a:   cmp    0x0(%r13),%rax               ; implicit exception: dispatches to 0x00007f81d4d3658f
  0x00007f81d4d35a4e:   mov    %r13,%rcx
  0x00007f81d4d35a51:   movabs $0x7f81d3f9df18,%rsi         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d35a5b:   mov    0x8(%rcx),%ecx
  0x00007f81d4d35a5e:   shl    $0x3,%rcx
  0x00007f81d4d35a62:   cmp    0x310(%rsi),%rcx
  0x00007f81d4d35a69:   jne    0x00007f81d4d35a78
  0x00007f81d4d35a6b:   addq   $0x1,0x318(%rsi)
  0x00007f81d4d35a73:   jmpq   0x00007f81d4d35ade
  0x00007f81d4d35a78:   cmp    0x320(%rsi),%rcx
  0x00007f81d4d35a7f:   jne    0x00007f81d4d35a8e
  0x00007f81d4d35a81:   addq   $0x1,0x328(%rsi)
  0x00007f81d4d35a89:   jmpq   0x00007f81d4d35ade
  0x00007f81d4d35a8e:   cmpq   $0x0,0x310(%rsi)
  0x00007f81d4d35a99:   jne    0x00007f81d4d35ab2
  0x00007f81d4d35a9b:   mov    %rcx,0x310(%rsi)
  0x00007f81d4d35aa2:   movq   $0x1,0x318(%rsi)
  0x00007f81d4d35aad:   jmpq   0x00007f81d4d35ade
  0x00007f81d4d35ab2:   cmpq   $0x0,0x320(%rsi)
  0x00007f81d4d35abd:   jne    0x00007f81d4d35ad6
  0x00007f81d4d35abf:   mov    %rcx,0x320(%rsi)
  0x00007f81d4d35ac6:   movq   $0x1,0x328(%rsi)
  0x00007f81d4d35ad1:   jmpq   0x00007f81d4d35ade
  0x00007f81d4d35ad6:   addq   $0x1,0x300(%rsi)
  0x00007f81d4d35ade:   mov    %r13,%rsi                    ;*invokevirtual equals {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@91 (line 635)
  0x00007f81d4d35ae1:   nopl   0x0(%rax)
  0x00007f81d4d35ae5:   movabs $0xffffffffffffffff,%rax
  0x00007f81d4d35aef:   callq  0x00007f81d47ee700           ; ImmutableOopMap {[160]=Oop [176]=Oop [200]=Oop [192]=Oop [168]=Oop }
                                                            ;*invokevirtual equals {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@91 (line 635)
                                                            ;   {virtual_call}
  0x00007f81d4d35af4:   cmp    $0x0,%eax
  0x00007f81d4d35af7:   movabs $0x7f81d3f9df18,%rdx         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d35b01:   movabs $0x348,%rcx
  0x00007f81d4d35b0b:   jne    0x00007f81d4d35b1b
  0x00007f81d4d35b11:   movabs $0x338,%rcx
  0x00007f81d4d35b1b:   mov    (%rdx,%rcx,1),%r8
  0x00007f81d4d35b1f:   lea    0x1(%r8),%r8
  0x00007f81d4d35b23:   mov    %r8,(%rdx,%rcx,1)
  0x00007f81d4d35b27:   mov    0xc0(%rsp),%rdi
  0x00007f81d4d35b2f:   jne    0x00007f81d4d36299           ;*ifeq {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@94 (line 635)
  0x00007f81d4d35b35:   cmp    $0x0,%rdi
  0x00007f81d4d35b39:   jne    0x00007f81d4d35b52
  0x00007f81d4d35b3b:   movabs $0x7f81d3f9df18,%r8          ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d35b45:   orb    $0x1,0x369(%r8)
  0x00007f81d4d35b4d:   jmpq   0x00007f81d4d35c13
  0x00007f81d4d35b52:   movabs $0x1000209d0,%r9             ;   {metadata('java/util/HashMap$TreeNode')}
  0x00007f81d4d35b5c:   mov    0x8(%rdi),%ecx
  0x00007f81d4d35b5f:   shl    $0x3,%rcx
  0x00007f81d4d35b63:   cmp    %rcx,%r9
  0x00007f81d4d35b66:   jne    0x00007f81d4d35bf7
  0x00007f81d4d35b6c:   movabs $0x7f81d3f9df18,%r8          ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d35b76:   mov    0x8(%rdi),%r9d
  0x00007f81d4d35b7a:   shl    $0x3,%r9
  0x00007f81d4d35b7e:   cmp    0x380(%r8),%r9
  0x00007f81d4d35b85:   jne    0x00007f81d4d35b94
  0x00007f81d4d35b87:   addq   $0x1,0x388(%r8)
  0x00007f81d4d35b8f:   jmpq   0x00007f81d4d35c18
  0x00007f81d4d35b94:   cmp    0x390(%r8),%r9
  0x00007f81d4d35b9b:   jne    0x00007f81d4d35baa
  0x00007f81d4d35b9d:   addq   $0x1,0x398(%r8)
  0x00007f81d4d35ba5:   jmpq   0x00007f81d4d35c18
  0x00007f81d4d35baa:   cmpq   $0x0,0x380(%r8)
  0x00007f81d4d35bb5:   jne    0x00007f81d4d35bce
  0x00007f81d4d35bb7:   mov    %r9,0x380(%r8)
  0x00007f81d4d35bbe:   movq   $0x1,0x388(%r8)
  0x00007f81d4d35bc9:   jmpq   0x00007f81d4d35c18
  0x00007f81d4d35bce:   cmpq   $0x0,0x390(%r8)
  0x00007f81d4d35bd9:   jne    0x00007f81d4d35bf2
  0x00007f81d4d35bdb:   mov    %r9,0x390(%r8)
  0x00007f81d4d35be2:   movq   $0x1,0x398(%r8)
  0x00007f81d4d35bed:   jmpq   0x00007f81d4d35c18
  0x00007f81d4d35bf2:   jmpq   0x00007f81d4d35c18
  0x00007f81d4d35bf7:   movabs $0x7f81d3f9df18,%r8          ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d35c01:   subq   $0x1,0x370(%r8)
  0x00007f81d4d35c09:   jmpq   0x00007f81d4d35c13
  0x00007f81d4d35c0e:   jmpq   0x00007f81d4d35c18
  0x00007f81d4d35c13:   xor    %rdx,%rdx
  0x00007f81d4d35c16:   jmp    0x00007f81d4d35c22
  0x00007f81d4d35c18:   movabs $0x1,%rdx                    ;*instanceof {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@106 (line 637)
  0x00007f81d4d35c22:   cmp    $0x0,%edx
  0x00007f81d4d35c25:   movabs $0x7f81d3f9df18,%rdx         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d35c2f:   movabs $0x3a8,%rcx
  0x00007f81d4d35c39:   je     0x00007f81d4d35c49
  0x00007f81d4d35c3f:   movabs $0x3b8,%rcx
  0x00007f81d4d35c49:   mov    (%rdx,%rcx,1),%r8
  0x00007f81d4d35c4d:   lea    0x1(%r8),%r8
  0x00007f81d4d35c51:   mov    %r8,(%rdx,%rcx,1)
  0x00007f81d4d35c55:   je     0x00007f81d4d35e11           ;*ifeq {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@109 (line 637)
  0x00007f81d4d35c5b:   cmp    $0x0,%rdi
  0x00007f81d4d35c5f:   jne    0x00007f81d4d35c77
  0x00007f81d4d35c61:   movabs $0x7f81d3f9df18,%rcx         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d35c6b:   orb    $0x1,0x3c1(%rcx)
  0x00007f81d4d35c72:   jmpq   0x00007f81d4d35d38
  0x00007f81d4d35c77:   movabs $0x1000209d0,%r8             ;   {metadata('java/util/HashMap$TreeNode')}
  0x00007f81d4d35c81:   mov    0x8(%rdi),%edx
  0x00007f81d4d35c84:   shl    $0x3,%rdx
  0x00007f81d4d35c88:   cmp    %rdx,%r8
  0x00007f81d4d35c8b:   jne    0x00007f81d4d35d1c
  0x00007f81d4d35c91:   movabs $0x7f81d3f9df18,%rcx         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d35c9b:   mov    0x8(%rdi),%r8d
  0x00007f81d4d35c9f:   shl    $0x3,%r8
  0x00007f81d4d35ca3:   cmp    0x3d8(%rcx),%r8
  0x00007f81d4d35caa:   jne    0x00007f81d4d35cb9
  0x00007f81d4d35cac:   addq   $0x1,0x3e0(%rcx)
  0x00007f81d4d35cb4:   jmpq   0x00007f81d4d35d38
  0x00007f81d4d35cb9:   cmp    0x3e8(%rcx),%r8
  0x00007f81d4d35cc0:   jne    0x00007f81d4d35ccf
  0x00007f81d4d35cc2:   addq   $0x1,0x3f0(%rcx)
  0x00007f81d4d35cca:   jmpq   0x00007f81d4d35d38
  0x00007f81d4d35ccf:   cmpq   $0x0,0x3d8(%rcx)
  0x00007f81d4d35cda:   jne    0x00007f81d4d35cf3
  0x00007f81d4d35cdc:   mov    %r8,0x3d8(%rcx)
  0x00007f81d4d35ce3:   movq   $0x1,0x3e0(%rcx)
  0x00007f81d4d35cee:   jmpq   0x00007f81d4d35d38
  0x00007f81d4d35cf3:   cmpq   $0x0,0x3e8(%rcx)
  0x00007f81d4d35cfe:   jne    0x00007f81d4d35d17
  0x00007f81d4d35d00:   mov    %r8,0x3e8(%rcx)
  0x00007f81d4d35d07:   movq   $0x1,0x3f0(%rcx)
  0x00007f81d4d35d12:   jmpq   0x00007f81d4d35d38
  0x00007f81d4d35d17:   jmpq   0x00007f81d4d35d38
  0x00007f81d4d35d1c:   movabs $0x7f81d3f9df18,%rcx         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d35d26:   subq   $0x1,0x3c8(%rcx)
  0x00007f81d4d35d2e:   jmpq   0x00007f81d4d36594
  0x00007f81d4d35d33:   jmpq   0x00007f81d4d35d38
  0x00007f81d4d35d38:   mov    %rdi,%rsi                    ;*checkcast {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@114 (line 638)
  0x00007f81d4d35d3b:   cmp    (%rsi),%rax                  ; implicit exception: dispatches to 0x00007f81d4d3659d
  0x00007f81d4d35d3e:   mov    %rsi,%rdx
  0x00007f81d4d35d41:   movabs $0x7f81d3f9df18,%rcx         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d35d4b:   mov    0x8(%rdx),%edx
  0x00007f81d4d35d4e:   shl    $0x3,%rdx
  0x00007f81d4d35d52:   cmp    0x410(%rcx),%rdx
  0x00007f81d4d35d59:   jne    0x00007f81d4d35d68
  0x00007f81d4d35d5b:   addq   $0x1,0x418(%rcx)
  0x00007f81d4d35d63:   jmpq   0x00007f81d4d35dce
  0x00007f81d4d35d68:   cmp    0x420(%rcx),%rdx
  0x00007f81d4d35d6f:   jne    0x00007f81d4d35d7e
  0x00007f81d4d35d71:   addq   $0x1,0x428(%rcx)
  0x00007f81d4d35d79:   jmpq   0x00007f81d4d35dce
  0x00007f81d4d35d7e:   cmpq   $0x0,0x410(%rcx)
  0x00007f81d4d35d89:   jne    0x00007f81d4d35da2
  0x00007f81d4d35d8b:   mov    %rdx,0x410(%rcx)
  0x00007f81d4d35d92:   movq   $0x1,0x418(%rcx)
  0x00007f81d4d35d9d:   jmpq   0x00007f81d4d35dce
  0x00007f81d4d35da2:   cmpq   $0x0,0x420(%rcx)
  0x00007f81d4d35dad:   jne    0x00007f81d4d35dc6
  0x00007f81d4d35daf:   mov    %rdx,0x420(%rcx)
  0x00007f81d4d35db6:   movq   $0x1,0x428(%rcx)
  0x00007f81d4d35dc1:   jmpq   0x00007f81d4d35dce
  0x00007f81d4d35dc6:   addq   $0x1,0x400(%rcx)
  0x00007f81d4d35dce:   mov    0xa0(%rsp),%rdx
  0x00007f81d4d35dd6:   mov    0xc8(%rsp),%rcx
  0x00007f81d4d35dde:   mov    0x98(%rsp),%r8d
  0x00007f81d4d35de6:   mov    0xa8(%rsp),%r9
  0x00007f81d4d35dee:   mov    0xb0(%rsp),%rdi              ;*invokevirtual putTreeVal {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@123 (line 638)
  0x00007f81d4d35df6:   nop
  0x00007f81d4d35df7:   callq  0x00007f81d47eea00           ; ImmutableOopMap {[160]=Oop [176]=Oop }
                                                            ;*invokevirtual putTreeVal {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@123 (line 638)
                                                            ;   {optimized virtual_call}
  0x00007f81d4d35dfc:   movabs $0x7f81d3f9df18,%rdx         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d35e06:   incl   0x438(%rdx)
  0x00007f81d4d35e0c:   jmpq   0x00007f81d4d362ac           ;*goto {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@128 (line 638)
  0x00007f81d4d35e11:   mov    %rdi,%rbx
  0x00007f81d4d35e14:   mov    $0x0,%edi                    ;*aload {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@134 (line 641)
  0x00007f81d4d35e19:   mov    %edi,0xbc(%rsp)
  0x00007f81d4d35e20:   mov    0xa8(%rsp),%rcx
  0x00007f81d4d35e28:   mov    0x98(%rsp),%edx
  0x00007f81d4d35e2f:   mov    0x18(%rbx),%eax              ; implicit exception: dispatches to 0x00007f81d4d365a2
                                                            ;*getfield next {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@136 (line 641)
  0x00007f81d4d35e32:   mov    %rax,0xd0(%rsp)
  0x00007f81d4d35e3a:   cmp    $0x0,%rax
  0x00007f81d4d35e3e:   movabs $0x7f81d3f9df18,%rsi         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d35e48:   movabs $0x460,%r8
  0x00007f81d4d35e52:   je     0x00007f81d4d35e62
  0x00007f81d4d35e58:   movabs $0x450,%r8
  0x00007f81d4d35e62:   mov    (%rsi,%r8,1),%r9
  0x00007f81d4d35e66:   lea    0x1(%r9),%r9
  0x00007f81d4d35e6a:   mov    %r9,(%rsi,%r8,1)
  0x00007f81d4d35e6e:   je     0x00007f81d4d3609a           ;*ifnonnull {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@142 (line 641)
  0x00007f81d4d35e74:   mov    0xc(%rax),%esi               ; implicit exception: dispatches to 0x00007f81d4d365a7
                                                            ;*getfield hash {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@177 (line 647)
  0x00007f81d4d35e77:   cmp    %edx,%esi
  0x00007f81d4d35e79:   movabs $0x7f81d3f9df18,%rsi         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d35e83:   movabs $0x518,%rbx
  0x00007f81d4d35e8d:   jne    0x00007f81d4d35e9d
  0x00007f81d4d35e93:   movabs $0x528,%rbx
  0x00007f81d4d35e9d:   mov    (%rsi,%rbx,1),%r8
  0x00007f81d4d35ea1:   lea    0x1(%r8),%r8
  0x00007f81d4d35ea5:   mov    %r8,(%rsi,%rbx,1)
  0x00007f81d4d35ea9:   jne    0x00007f81d4d3600d           ;*if_icmpne {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@181 (line 647)
  0x00007f81d4d35eaf:   mov    0x10(%rax),%esi              ;*getfield key {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@186 (line 647)
  0x00007f81d4d35eb2:   cmp    %rcx,%rsi
  0x00007f81d4d35eb5:   movabs $0x7f81d3f9df18,%rbx         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d35ebf:   movabs $0x538,%r8
  0x00007f81d4d35ec9:   je     0x00007f81d4d35ed9
  0x00007f81d4d35ecf:   movabs $0x548,%r8
  0x00007f81d4d35ed9:   mov    (%rbx,%r8,1),%r9
  0x00007f81d4d35edd:   lea    0x1(%r9),%r9
  0x00007f81d4d35ee1:   mov    %r9,(%rbx,%r8,1)
  0x00007f81d4d35ee5:   je     0x00007f81d4d36070           ;*if_acmpeq {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@193 (line 647)
  0x00007f81d4d35eeb:   cmp    $0x0,%rcx
  0x00007f81d4d35eef:   movabs $0x7f81d3f9df18,%rbx         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d35ef9:   movabs $0x558,%r8
  0x00007f81d4d35f03:   je     0x00007f81d4d35f13
  0x00007f81d4d35f09:   movabs $0x568,%r8
  0x00007f81d4d35f13:   mov    (%rbx,%r8,1),%r9
  0x00007f81d4d35f17:   lea    0x1(%r9),%r9
  0x00007f81d4d35f1b:   mov    %r9,(%rbx,%r8,1)
  0x00007f81d4d35f1f:   je     0x00007f81d4d3600d           ;*ifnull {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@197 (line 647)
  0x00007f81d4d35f25:   cmp    (%rcx),%rax                  ; implicit exception: dispatches to 0x00007f81d4d365ac
  0x00007f81d4d35f28:   mov    %rcx,%rbx
  0x00007f81d4d35f2b:   movabs $0x7f81d3f9df18,%r8          ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d35f35:   mov    0x8(%rbx),%ebx
  0x00007f81d4d35f38:   shl    $0x3,%rbx
  0x00007f81d4d35f3c:   cmp    0x588(%r8),%rbx
  0x00007f81d4d35f43:   jne    0x00007f81d4d35f52
  0x00007f81d4d35f45:   addq   $0x1,0x590(%r8)
  0x00007f81d4d35f4d:   jmpq   0x00007f81d4d35fb8
  0x00007f81d4d35f52:   cmp    0x598(%r8),%rbx
  0x00007f81d4d35f59:   jne    0x00007f81d4d35f68
  0x00007f81d4d35f5b:   addq   $0x1,0x5a0(%r8)
  0x00007f81d4d35f63:   jmpq   0x00007f81d4d35fb8
  0x00007f81d4d35f68:   cmpq   $0x0,0x588(%r8)
  0x00007f81d4d35f73:   jne    0x00007f81d4d35f8c
  0x00007f81d4d35f75:   mov    %rbx,0x588(%r8)
  0x00007f81d4d35f7c:   movq   $0x1,0x590(%r8)
  0x00007f81d4d35f87:   jmpq   0x00007f81d4d35fb8
  0x00007f81d4d35f8c:   cmpq   $0x0,0x598(%r8)
  0x00007f81d4d35f97:   jne    0x00007f81d4d35fb0
  0x00007f81d4d35f99:   mov    %rbx,0x598(%r8)
  0x00007f81d4d35fa0:   movq   $0x1,0x5a0(%r8)
  0x00007f81d4d35fab:   jmpq   0x00007f81d4d35fb8
  0x00007f81d4d35fb0:   addq   $0x1,0x578(%r8)
  0x00007f81d4d35fb8:   mov    %rsi,%rdx
  0x00007f81d4d35fbb:   mov    %rcx,%rsi                    ;*invokevirtual equals {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@203 (line 648)
  0x00007f81d4d35fbe:   nopl   0x0(%rax)
  0x00007f81d4d35fc5:   movabs $0xffffffffffffffff,%rax
  0x00007f81d4d35fcf:   callq  0x00007f81d47ee700           ; ImmutableOopMap {[160]=Oop [176]=Oop [200]=Oop [208]=Oop [168]=Oop }
                                                            ;*invokevirtual equals {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@203 (line 648)
                                                            ;   {virtual_call}
  0x00007f81d4d35fd4:   cmp    $0x0,%eax
  0x00007f81d4d35fd7:   movabs $0x7f81d3f9df18,%rdx         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d35fe1:   movabs $0x5c0,%rsi
  0x00007f81d4d35feb:   jne    0x00007f81d4d35ffb
  0x00007f81d4d35ff1:   movabs $0x5b0,%rsi
  0x00007f81d4d35ffb:   mov    (%rdx,%rsi,1),%rdi
  0x00007f81d4d35fff:   lea    0x1(%rdi),%rdi
  0x00007f81d4d36003:   mov    %rdi,(%rdx,%rsi,1)
  0x00007f81d4d36007:   jne    0x00007f81d4d3607d           ;*ifeq {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@206 (line 648)
  0x00007f81d4d3600d:   mov    0xbc(%rsp),%edi
  0x00007f81d4d36014:   inc    %edi
  0x00007f81d4d36016:   movabs $0x7f81d3f9df18,%rdx         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d36020:   mov    0x140(%rdx),%esi
  0x00007f81d4d36026:   add    $0x8,%esi
  0x00007f81d4d36029:   mov    %esi,0x140(%rdx)
  0x00007f81d4d3602f:   and    $0xfff8,%esi
  0x00007f81d4d36035:   cmp    $0x0,%esi
  0x00007f81d4d36038:   je     0x00007f81d4d365b1           ;*goto {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@219 (line 640)
  0x00007f81d4d3603e:   mov    0x108(%r15),%r10             ; ImmutableOopMap {[160]=Oop [176]=Oop [200]=Oop [208]=Oop [168]=Oop }
                                                            ;*goto {reexecute=1 rethrow=0 return_oop=0}
                                                            ; - (reexecute) java.util.HashMap::putVal@219 (line 640)
  0x00007f81d4d36045:   test   %eax,(%r10)                  ;   {poll}
  0x00007f81d4d36048:   movabs $0x7f81d3f9df18,%rdx         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d36052:   incl   0x5e8(%rdx)
  0x00007f81d4d36058:   mov    %rdi,%r8
  0x00007f81d4d3605b:   mov    0xd0(%rsp),%rbx
  0x00007f81d4d36063:   mov    %r8d,0xbc(%rsp)
  0x00007f81d4d3606b:   jmpq   0x00007f81d4d35e20           ;*goto {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@219 (line 640)
  0x00007f81d4d36070:   mov    0xd0(%rsp),%rax
  0x00007f81d4d36078:   jmpq   0x00007f81d4d362ac           ;*aload {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@222 (line 653)
  0x00007f81d4d3607d:   movabs $0x7f81d3f9df18,%rdx         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d36087:   incl   0x5d0(%rdx)
  0x00007f81d4d3608d:   mov    0xd0(%rsp),%rax
  0x00007f81d4d36095:   jmpq   0x00007f81d4d362ac           ;*goto {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@209 (line 649)
  0x00007f81d4d3609a:   mov    0xbc(%rsp),%r8d
  0x00007f81d4d360a2:   mov    0xb0(%rsp),%r13
  0x00007f81d4d360aa:   mov    %rcx,%r11
  0x00007f81d4d360ad:   mov    %rdx,%r9
  0x00007f81d4d360b0:   mov    0xa0(%rsp),%rsi
  0x00007f81d4d360b8:   movabs $0x7f81d3f9df18,%rdx         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d360c2:   addq   $0x1,0x488(%rdx)
  0x00007f81d4d360ca:   movabs $0x7f81d3f9ed80,%rdx         ;   {metadata(method data for {method} {0x00007f81d3e43970} 'newNode' '(ILjava/lang/Object;Ljava/lang/Object;Ljava/util/HashMap$Node;)Ljava/util/HashMap$Node;' in 'java/util/HashMap')}
  0x00007f81d4d360d4:   mov    0x13c(%rdx),%esi
  0x00007f81d4d360da:   add    $0x8,%esi
  0x00007f81d4d360dd:   mov    %esi,0x13c(%rdx)
  0x00007f81d4d360e3:   and    $0x7ffff8,%esi
  0x00007f81d4d360e9:   cmp    $0x0,%esi
  0x00007f81d4d360ec:   je     0x00007f81d4d365d2
  0x00007f81d4d360f2:   movabs $0x100020330,%rdx            ;   {metadata('java/util/HashMap$Node')}
  0x00007f81d4d360fc:   mov    0x118(%r15),%rax
  0x00007f81d4d36103:   lea    0x20(%rax),%rdi
  0x00007f81d4d36107:   cmp    0x128(%r15),%rdi
  0x00007f81d4d3610e:   ja     0x00007f81d4d365f3
  0x00007f81d4d36114:   mov    %rdi,0x118(%r15)
  0x00007f81d4d3611b:   mov    0xb8(%rdx),%rcx
  0x00007f81d4d36122:   mov    %rcx,(%rax)
  0x00007f81d4d36125:   mov    %rdx,%rcx
  0x00007f81d4d36128:   shr    $0x3,%rcx
  0x00007f81d4d3612c:   mov    %ecx,0x8(%rax)
  0x00007f81d4d3612f:   xor    %rcx,%rcx
  0x00007f81d4d36132:   mov    %ecx,0xc(%rax)
  0x00007f81d4d36135:   xor    %rcx,%rcx
  0x00007f81d4d36138:   mov    %rcx,0x10(%rax)
  0x00007f81d4d3613c:   mov    %rcx,0x18(%rax)              ;*new {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::newNode@0 (line 1799)
                                                            ; - java.util.HashMap::putVal@152 (line 642)
  0x00007f81d4d36140:   mov    %rax,%rdx
  0x00007f81d4d36143:   movabs $0x7f81d3f9ed80,%rcx         ;   {metadata(method data for {method} {0x00007f81d3e43970} 'newNode' '(ILjava/lang/Object;Ljava/lang/Object;Ljava/util/HashMap$Node;)Ljava/util/HashMap$Node;' in 'java/util/HashMap')}
  0x00007f81d4d3614d:   addq   $0x1,0x180(%rcx)
  0x00007f81d4d36155:   movabs $0x7f81d3f9ef50,%rdx         ;   {metadata(method data for {method} {0x00007f81d3e710e8} '<init>' '(ILjava/lang/Object;Ljava/lang/Object;Ljava/util/HashMap$Node;)V' in 'java/util/HashMap$Node')}
  0x00007f81d4d3615f:   mov    0x13c(%rdx),%ecx
  0x00007f81d4d36165:   add    $0x8,%ecx
  0x00007f81d4d36168:   mov    %ecx,0x13c(%rdx)
  0x00007f81d4d3616e:   and    $0x7ffff8,%ecx
  0x00007f81d4d36174:   cmp    $0x0,%ecx
  0x00007f81d4d36177:   je     0x00007f81d4d36600
  0x00007f81d4d3617d:   mov    %rax,%rdx
  0x00007f81d4d36180:   movabs $0x7f81d3f9ef50,%rcx         ;   {metadata(method data for {method} {0x00007f81d3e710e8} '<init>' '(ILjava/lang/Object;Ljava/lang/Object;Ljava/util/HashMap$Node;)V' in 'java/util/HashMap$Node')}
  0x00007f81d4d3618a:   addq   $0x1,0x180(%rcx)
  0x00007f81d4d36192:   movabs $0x7f81d3f33388,%rdx         ;   {metadata(method data for {method} {0x00007f81d3cfe650} '<init>' '()V' in 'java/lang/Object')}
  0x00007f81d4d3619c:   mov    0x13c(%rdx),%ecx
  0x00007f81d4d361a2:   add    $0x8,%ecx
  0x00007f81d4d361a5:   mov    %ecx,0x13c(%rdx)
  0x00007f81d4d361ab:   and    $0x7ffff8,%ecx
  0x00007f81d4d361b1:   cmp    $0x0,%ecx
  0x00007f81d4d361b4:   je     0x00007f81d4d36621
  0x00007f81d4d361ba:   mov    %r9d,0xc(%rax)               ;*putfield hash {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap$Node::<init>@6 (line 286)
                                                            ; - java.util.HashMap::newNode@9 (line 1799)
                                                            ; - java.util.HashMap::putVal@152 (line 642)
  0x00007f81d4d361be:   mov    %r11,%r10
  0x00007f81d4d361c1:   mov    %r10d,0x10(%rax)
  0x00007f81d4d361c5:   mov    %rax,%rdx
  0x00007f81d4d361c8:   shr    $0x9,%rdx
  0x00007f81d4d361cc:   movabs $0x7f81d3eb8000,%rcx
  0x00007f81d4d361d6:   movb   $0x0,(%rdx,%rcx,1)           ;*putfield key {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap$Node::<init>@11 (line 287)
                                                            ; - java.util.HashMap::newNode@9 (line 1799)
                                                            ; - java.util.HashMap::putVal@152 (line 642)
  0x00007f81d4d361da:   mov    %r13,%r10
  0x00007f81d4d361dd:   mov    %r10d,0x14(%rax)
  0x00007f81d4d361e1:   mov    %rax,%rdx
  0x00007f81d4d361e4:   shr    $0x9,%rdx
  0x00007f81d4d361e8:   movb   $0x0,(%rdx,%rcx,1)           ;*putfield value {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap$Node::<init>@16 (line 288)
                                                            ; - java.util.HashMap::newNode@9 (line 1799)
                                                            ; - java.util.HashMap::putVal@152 (line 642)
  0x00007f81d4d361ec:   mov    %rax,%r10
  0x00007f81d4d361ef:   mov    %r10d,0x18(%rbx)
  0x00007f81d4d361f3:   shr    $0x9,%rbx
  0x00007f81d4d361f7:   movb   $0x0,(%rbx,%rcx,1)           ;*putfield next {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@155 (line 642)
  0x00007f81d4d361fb:   cmp    $0x7,%r8d
  0x00007f81d4d361ff:   movabs $0x7f81d3f9df18,%rdx         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d36209:   movabs $0x4b8,%rcx
  0x00007f81d4d36213:   jge    0x00007f81d4d36223
  0x00007f81d4d36219:   movabs $0x4a8,%rcx
  0x00007f81d4d36223:   mov    (%rdx,%rcx,1),%rsi
  0x00007f81d4d36227:   lea    0x1(%rsi),%rsi
  0x00007f81d4d3622b:   mov    %rsi,(%rdx,%rcx,1)
  0x00007f81d4d3622f:   jge    0x00007f81d4d36242           ;*if_icmplt {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@162 (line 643)
  0x00007f81d4d36235:   mov    0xd0(%rsp),%rax
  0x00007f81d4d3623d:   jmpq   0x00007f81d4d362ac           ;*aload {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@222 (line 653)
  0x00007f81d4d36242:   mov    0xc8(%rsp),%rbx
  0x00007f81d4d3624a:   mov    0xa0(%rsp),%rsi
  0x00007f81d4d36252:   movabs $0x7f81d3f9df18,%rdx         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d3625c:   addq   $0x1,0x4c8(%rdx)
  0x00007f81d4d36264:   mov    %rbx,%rdx
  0x00007f81d4d36267:   mov    %r9,%rcx
  0x00007f81d4d3626a:   mov    0xa0(%rsp),%rsi              ;*invokevirtual treeifyBin {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@169 (line 644)
  0x00007f81d4d36272:   nopl   0x0(%rax,%rax,1)
  0x00007f81d4d36277:   callq  0x00007f81d47eea00           ; ImmutableOopMap {[160]=Oop [176]=Oop [208]=Oop }
                                                            ;*invokevirtual treeifyBin {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@169 (line 644)
                                                            ;   {optimized virtual_call}
  0x00007f81d4d3627c:   movabs $0x7f81d3f9df18,%rax         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d36286:   incl   0x500(%rax)
  0x00007f81d4d3628c:   mov    0xd0(%rsp),%rax
  0x00007f81d4d36294:   jmpq   0x00007f81d4d362ac           ;*goto {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@172 (line 644)
  0x00007f81d4d36299:   movabs $0x7f81d3f9df18,%rax         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d362a3:   incl   0x358(%rax)
  0x00007f81d4d362a9:   mov    %rdi,%rax                    ;*goto {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@101 (line 636)
  0x00007f81d4d362ac:   cmp    $0x0,%rax
  0x00007f81d4d362b0:   movabs $0x7f81d3f9df18,%rsi         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d362ba:   movabs $0x610,%rdi
  0x00007f81d4d362c4:   jne    0x00007f81d4d362d4
  0x00007f81d4d362ca:   movabs $0x600,%rdi
  0x00007f81d4d362d4:   mov    (%rsi,%rdi,1),%rbx
  0x00007f81d4d362d8:   lea    0x1(%rbx),%rbx
  0x00007f81d4d362dc:   mov    %rbx,(%rsi,%rdi,1)
  0x00007f81d4d362e0:   jne    0x00007f81d4d3633e           ;*ifnull {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@224 (line 653)
  0x00007f81d4d362e6:   mov    0xa0(%rsp),%rsi
  0x00007f81d4d362ee:   mov    0x18(%rsi),%eax              ;*getfield modCount {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@261 (line 661)
  0x00007f81d4d362f1:   inc    %eax
  0x00007f81d4d362f3:   mov    %eax,0x18(%rsi)              ;*putfield modCount {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@266 (line 661)
  0x00007f81d4d362f6:   mov    0x14(%rsi),%eax              ;*getfield size {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@271 (line 662)
  0x00007f81d4d362f9:   inc    %eax
  0x00007f81d4d362fb:   mov    %eax,0x14(%rsi)              ;*putfield size {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@277 (line 662)
  0x00007f81d4d362fe:   mov    0x1c(%rsi),%edi              ;*getfield threshold {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@281 (line 662)
  0x00007f81d4d36301:   cmp    %edi,%eax
  0x00007f81d4d36303:   movabs $0x7f81d3f9df18,%rax         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d3630d:   movabs $0x698,%rdi
  0x00007f81d4d36317:   jle    0x00007f81d4d36327
  0x00007f81d4d3631d:   movabs $0x6a8,%rdi
  0x00007f81d4d36327:   mov    (%rax,%rdi,1),%rbx
  0x00007f81d4d3632b:   lea    0x1(%rbx),%rbx
  0x00007f81d4d3632f:   mov    %rbx,(%rax,%rdi,1)
  0x00007f81d4d36333:   jle    0x00007f81d4d36464
  0x00007f81d4d36339:   jmpq   0x00007f81d4d36436           ;*if_icmple {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@284 (line 662)
  0x00007f81d4d3633e:   mov    0x9c(%rsp),%r9d
  0x00007f81d4d36346:   mov    0xa0(%rsp),%rsi
  0x00007f81d4d3634e:   mov    0x14(%rax),%edi              ; implicit exception: dispatches to 0x00007f81d4d36642
                                                            ;*getfield value {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@229 (line 654)
  0x00007f81d4d36351:   cmp    $0x0,%r9d
  0x00007f81d4d36355:   movabs $0x7f81d3f9df18,%rbx         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d3635f:   movabs $0x620,%rdx
  0x00007f81d4d36369:   je     0x00007f81d4d36379
  0x00007f81d4d3636f:   movabs $0x630,%rdx
  0x00007f81d4d36379:   mov    (%rbx,%rdx,1),%rcx
  0x00007f81d4d3637d:   lea    0x1(%rcx),%rcx
  0x00007f81d4d36381:   mov    %rcx,(%rbx,%rdx,1)
  0x00007f81d4d36385:   je     0x00007f81d4d363c5           ;*ifeq {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@236 (line 655)
  0x00007f81d4d3638b:   cmp    $0x0,%rdi
  0x00007f81d4d3638f:   movabs $0x7f81d3f9df18,%rbx         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d36399:   movabs $0x640,%rdx
  0x00007f81d4d363a3:   jne    0x00007f81d4d363b3
  0x00007f81d4d363a9:   movabs $0x650,%rdx
  0x00007f81d4d363b3:   mov    (%rbx,%rdx,1),%rcx
  0x00007f81d4d363b7:   lea    0x1(%rcx),%rcx
  0x00007f81d4d363bb:   mov    %rcx,(%rbx,%rdx,1)
  0x00007f81d4d363bf:   jne    0x00007f81d4d363e6           ;*ifnonnull {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@241 (line 655)
  0x00007f81d4d363c5:   mov    0xb0(%rsp),%r8
  0x00007f81d4d363cd:   mov    %r8,%r10
  0x00007f81d4d363d0:   mov    %r10d,0x14(%rax)
  0x00007f81d4d363d4:   shr    $0x9,%rax
  0x00007f81d4d363d8:   movabs $0x7f81d3eb8000,%rbx
  0x00007f81d4d363e2:   movb   $0x0,(%rax,%rbx,1)           ;*putfield value {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@247 (line 656)
  0x00007f81d4d363e6:   movabs $0x7f81d3f9df18,%rax         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d363f0:   addq   $0x1,0x678(%rax)
  0x00007f81d4d363f8:   movabs $0x7f81d3fb00a0,%rax         ;   {metadata(method data for {method} {0x00007f81d3e43d90} 'afterNodeAccess' '(Ljava/util/HashMap$Node;)V' in 'java/util/HashMap')}
  0x00007f81d4d36402:   mov    0x13c(%rax),%esi
  0x00007f81d4d36408:   add    $0x8,%esi
  0x00007f81d4d3640b:   mov    %esi,0x13c(%rax)
  0x00007f81d4d36411:   and    $0x7ffff8,%esi
  0x00007f81d4d36417:   cmp    $0x0,%esi
  0x00007f81d4d3641a:   je     0x00007f81d4d36647
  0x00007f81d4d36420:   mov    %rdi,%rax
  0x00007f81d4d36423:   add    $0xe0,%rsp
  0x00007f81d4d3642a:   pop    %rbp
  0x00007f81d4d3642b:   mov    0x108(%r15),%r10
  0x00007f81d4d36432:   test   %eax,(%r10)                  ;   {poll_return}
  0x00007f81d4d36435:   retq                                ;*areturn {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@258 (line 658)
  0x00007f81d4d36436:   mov    %rsi,%rbx
  0x00007f81d4d36439:   movabs $0x7f81d3f9df18,%rax         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d36443:   addq   $0x1,0x6b8(%rax)
  0x00007f81d4d3644b:   mov    %rsi,%rbx
  0x00007f81d4d3644e:   mov    %rbx,%rsi                    ;*invokevirtual resize {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@288 (line 663)
  0x00007f81d4d36451:   nopw   0x0(%rax,%rax,1)
  0x00007f81d4d36457:   callq  0x00007f81d47eea00           ; ImmutableOopMap {[160]=Oop }
                                                            ;*invokevirtual resize {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@288 (line 663)
                                                            ;   {optimized virtual_call}
  0x00007f81d4d3645c:   mov    0xa0(%rsp),%rsi
  0x00007f81d4d36464:   movabs $0x7f81d3f9df18,%rax         ;   {metadata(method data for {method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d3646e:   addq   $0x1,0x708(%rax)
  0x00007f81d4d36476:   movabs $0x7f81d3f9f130,%rax         ;   {metadata(method data for {method} {0x00007f81d3e43e40} 'afterNodeInsertion' '(Z)V' in 'java/util/HashMap')}
  0x00007f81d4d36480:   mov    0x13c(%rax),%esi
  0x00007f81d4d36486:   add    $0x8,%esi
  0x00007f81d4d36489:   mov    %esi,0x13c(%rax)
  0x00007f81d4d3648f:   and    $0x7ffff8,%esi
  0x00007f81d4d36495:   cmp    $0x0,%esi
  0x00007f81d4d36498:   je     0x00007f81d4d36668
  0x00007f81d4d3649e:   movabs $0x0,%rax                    ;   {oop(NULL)}
  0x00007f81d4d364a8:   add    $0xe0,%rsp
  0x00007f81d4d364af:   pop    %rbp
  0x00007f81d4d364b0:   mov    0x108(%r15),%r10
  0x00007f81d4d364b7:   test   %eax,(%r10)                  ;   {poll_return}
  0x00007f81d4d364ba:   retq   
  0x00007f81d4d364bb:   movabs $0x7f81d3e413e0,%r10         ;   {metadata({method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d364c5:   mov    %r10,0x8(%rsp)
  0x00007f81d4d364ca:   movq   $0xffffffffffffffff,(%rsp)
  0x00007f81d4d364d2:   callq  0x00007f81d489e000           ; ImmutableOopMap {rsi=Oop [160]=Oop rcx=Oop [168]=Oop r8=Oop [176]=Oop }
                                                            ;*synchronization entry
                                                            ; - java.util.HashMap::putVal@-1 (line 628)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d364d7:   jmpq   0x00007f81d4d355bd
  0x00007f81d4d364dc:   callq  0x00007f81d480afa0           ; ImmutableOopMap {rsi=Oop [160]=Oop rcx=Oop [168]=Oop r8=Oop [176]=Oop rbx=Oop }
                                                            ;*arraylength {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@12 (line 628)
                                                            ;   {runtime_call throw_null_pointer_exception Runtime1 stub}
  0x00007f81d4d364e1:   callq  0x00007f81d480afa0           ; ImmutableOopMap {[160]=Oop [176]=Oop [168]=Oop rdx=Oop }
                                                            ;*arraylength {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@26 (line 629)
                                                            ;   {runtime_call throw_null_pointer_exception Runtime1 stub}
  0x00007f81d4d364e6:   callq  0x00007f81d480afa0           ; ImmutableOopMap {[160]=Oop [176]=Oop [168]=Oop rbx=Oop }
                                                            ;*aaload {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@40 (line 630)
                                                            ;   {runtime_call throw_null_pointer_exception Runtime1 stub}
  0x00007f81d4d364eb:   callq  0x00007f81d480afa0           ; ImmutableOopMap {[160]=Oop [176]=Oop [168]=Oop rbx=Oop }
                                                            ;*aaload {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@40 (line 630)
                                                            ;   {runtime_call throw_null_pointer_exception Runtime1 stub}
  0x00007f81d4d364f0:   mov    %r8,(%rsp)
  0x00007f81d4d364f4:   mov    %rbx,0x8(%rsp)
  0x00007f81d4d364f9:   callq  0x00007f81d480b8a0           ; ImmutableOopMap {[160]=Oop [176]=Oop [168]=Oop rbx=Oop }
                                                            ;*aaload {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@40 (line 630)
                                                            ;   {runtime_call throw_range_check_failed Runtime1 stub}
  0x00007f81d4d364fe:   movabs $0x7f81d3e43970,%r10         ;   {metadata({method} {0x00007f81d3e43970} 'newNode' '(ILjava/lang/Object;Ljava/lang/Object;Ljava/util/HashMap$Node;)Ljava/util/HashMap$Node;' in 'java/util/HashMap')}
  0x00007f81d4d36508:   mov    %r10,0x8(%rsp)
  0x00007f81d4d3650d:   movq   $0xffffffffffffffff,(%rsp)
  0x00007f81d4d36515:   callq  0x00007f81d489e000           ; ImmutableOopMap {[160]=Oop rbx=Oop r9=Oop [176]=Oop rcx=Oop [168]=Oop }
                                                            ;*synchronization entry
                                                            ; - java.util.HashMap::newNode@-1 (line 1799)
                                                            ; - java.util.HashMap::putVal@56 (line 631)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d3651a:   jmpq   0x00007f81d4d35729
  0x00007f81d4d3651f:   mov    %rdx,%rdx
  0x00007f81d4d36522:   callq  0x00007f81d480a680           ; ImmutableOopMap {[160]=Oop rbx=Oop r9=Oop [176]=Oop r13=Oop [168]=Oop }
                                                            ;*new {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::newNode@0 (line 1799)
                                                            ; - java.util.HashMap::putVal@56 (line 631)
                                                            ;   {runtime_call fast_new_instance Runtime1 stub}
  0x00007f81d4d36527:   jmpq   0x00007f81d4d3577d
  0x00007f81d4d3652c:   movabs $0x7f81d3e710e8,%r10         ;   {metadata({method} {0x00007f81d3e710e8} '<init>' '(ILjava/lang/Object;Ljava/lang/Object;Ljava/util/HashMap$Node;)V' in 'java/util/HashMap$Node')}
  0x00007f81d4d36536:   mov    %r10,0x8(%rsp)
  0x00007f81d4d3653b:   movq   $0xffffffffffffffff,(%rsp)
  0x00007f81d4d36543:   callq  0x00007f81d489e000           ; ImmutableOopMap {[160]=Oop rbx=Oop r9=Oop [176]=Oop r13=Oop [168]=Oop rax=Oop }
                                                            ;*synchronization entry
                                                            ; - java.util.HashMap$Node::<init>@-1 (line 285)
                                                            ; - java.util.HashMap::newNode@9 (line 1799)
                                                            ; - java.util.HashMap::putVal@56 (line 631)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d36548:   jmpq   0x00007f81d4d357ba
  0x00007f81d4d3654d:   movabs $0x7f81d3cfe650,%r10         ;   {metadata({method} {0x00007f81d3cfe650} '<init>' '()V' in 'java/lang/Object')}
  0x00007f81d4d36557:   mov    %r10,0x8(%rsp)
  0x00007f81d4d3655c:   movq   $0xffffffffffffffff,(%rsp)
  0x00007f81d4d36564:   callq  0x00007f81d489e000           ; ImmutableOopMap {[160]=Oop rbx=Oop r9=Oop [176]=Oop r13=Oop [168]=Oop rax=Oop }
                                                            ;*synchronization entry
                                                            ; - java.lang.Object::<init>@-1 (line 50)
                                                            ; - java.util.HashMap$Node::<init>@1 (line 285)
                                                            ; - java.util.HashMap::newNode@9 (line 1799)
                                                            ; - java.util.HashMap::putVal@56 (line 631)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d36569:   jmpq   0x00007f81d4d357f7
  0x00007f81d4d3656e:   mov    %r8,(%rsp)
  0x00007f81d4d36572:   mov    %rbx,0x8(%rsp)
  0x00007f81d4d36577:   callq  0x00007f81d480b8a0           ; ImmutableOopMap {[160]=Oop rbx=Oop rax=Oop }
                                                            ;*aastore {reexecute=1 rethrow=0 return_oop=0}
                                                            ; - (reexecute) java.util.HashMap::putVal@59 (line 631)
                                                            ;   {runtime_call throw_range_check_failed Runtime1 stub}
  0x00007f81d4d3657c:   callq  0x00007f81d480afa0           ; ImmutableOopMap {[160]=Oop rbx=Oop rax=Oop }
                                                            ;*aastore {reexecute=1 rethrow=0 return_oop=0}
                                                            ; - (reexecute) java.util.HashMap::putVal@59 (line 631)
                                                            ;   {runtime_call throw_null_pointer_exception Runtime1 stub}
  0x00007f81d4d36581:   mov    %rax,(%rsp)
  0x00007f81d4d36585:   callq  0x00007f81d489b020           ; ImmutableOopMap {[160]=Oop rbx=Oop rax=Oop }
                                                            ;*aastore {reexecute=1 rethrow=0 return_oop=0}
                                                            ; - (reexecute) java.util.HashMap::putVal@59 (line 631)
                                                            ;   {runtime_call throw_array_store_exception Runtime1 stub}
  0x00007f81d4d3658a:   callq  0x00007f81d480afa0           ; ImmutableOopMap {[160]=Oop rdi=Oop r9=Oop [176]=Oop r13=Oop [168]=Oop rbx=Oop }
                                                            ;*getfield hash {reexecute=1 rethrow=0 return_oop=0}
                                                            ; - (reexecute) java.util.HashMap::putVal@65 (line 634)
                                                            ;   {runtime_call throw_null_pointer_exception Runtime1 stub}
  0x00007f81d4d3658f:   callq  0x00007f81d480afa0           ; ImmutableOopMap {[160]=Oop r13=Oop [168]=Oop [176]=Oop [192]=Oop [200]=Oop rdx=Oop }
                                                            ;*invokevirtual equals {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@91 (line 635)
                                                            ;   {runtime_call throw_null_pointer_exception Runtime1 stub}
  0x00007f81d4d36594:   mov    %rdi,(%rsp)
  0x00007f81d4d36598:   callq  0x00007f81d489b320           ; ImmutableOopMap {[160]=Oop [176]=Oop [168]=Oop [200]=Oop }
                                                            ;*checkcast {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@114 (line 638)
                                                            ;   {runtime_call throw_class_cast_exception Runtime1 stub}
  0x00007f81d4d3659d:   callq  0x00007f81d480afa0           ; ImmutableOopMap {[160]=Oop [176]=Oop [168]=Oop [200]=Oop rsi=Oop }
                                                            ;*invokevirtual putTreeVal {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@123 (line 638)
                                                            ;   {runtime_call throw_null_pointer_exception Runtime1 stub}
  0x00007f81d4d365a2:   callq  0x00007f81d480afa0           ; ImmutableOopMap {[160]=Oop [176]=Oop [200]=Oop rbx=Oop rcx=Oop [168]=Oop }
                                                            ;*getfield next {reexecute=1 rethrow=0 return_oop=0}
                                                            ; - (reexecute) java.util.HashMap::putVal@136 (line 641)
                                                            ;   {runtime_call throw_null_pointer_exception Runtime1 stub}
  0x00007f81d4d365a7:   callq  0x00007f81d480afa0           ; ImmutableOopMap {[160]=Oop [176]=Oop [200]=Oop rcx=Oop [168]=Oop rax=Oop [208]=Oop }
                                                            ;*getfield hash {reexecute=1 rethrow=0 return_oop=0}
                                                            ; - (reexecute) java.util.HashMap::putVal@177 (line 647)
                                                            ;   {runtime_call throw_null_pointer_exception Runtime1 stub}
  0x00007f81d4d365ac:   callq  0x00007f81d480afa0           ; ImmutableOopMap {[160]=Oop [176]=Oop [200]=Oop rcx=Oop [168]=Oop [208]=Oop rsi=Oop }
                                                            ;*invokevirtual equals {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::putVal@203 (line 648)
                                                            ;   {runtime_call throw_null_pointer_exception Runtime1 stub}
  0x00007f81d4d365b1:   movabs $0x7f81d3e413e0,%r10         ;   {metadata({method} {0x00007f81d3e413e0} 'putVal' '(ILjava/lang/Object;Ljava/lang/Object;ZZ)Ljava/lang/Object;' in 'java/util/HashMap')}
  0x00007f81d4d365bb:   mov    %r10,0x8(%rsp)
  0x00007f81d4d365c0:   movq   $0xdb,(%rsp)
  0x00007f81d4d365c8:   callq  0x00007f81d489e000           ; ImmutableOopMap {[160]=Oop [176]=Oop [200]=Oop [208]=Oop [168]=Oop }
                                                            ;*goto {reexecute=1 rethrow=0 return_oop=0}
                                                            ; - (reexecute) java.util.HashMap::putVal@219 (line 640)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d365cd:   jmpq   0x00007f81d4d3603e
  0x00007f81d4d365d2:   movabs $0x7f81d3e43970,%r10         ;   {metadata({method} {0x00007f81d3e43970} 'newNode' '(ILjava/lang/Object;Ljava/lang/Object;Ljava/util/HashMap$Node;)Ljava/util/HashMap$Node;' in 'java/util/HashMap')}
  0x00007f81d4d365dc:   mov    %r10,0x8(%rsp)
  0x00007f81d4d365e1:   movq   $0xffffffffffffffff,(%rsp)
  0x00007f81d4d365e9:   callq  0x00007f81d489e000           ; ImmutableOopMap {[160]=Oop r11=Oop [168]=Oop r13=Oop [176]=Oop rbx=Oop [208]=Oop [200]=Oop }
                                                            ;*synchronization entry
                                                            ; - java.util.HashMap::newNode@-1 (line 1799)
                                                            ; - java.util.HashMap::putVal@152 (line 642)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d365ee:   jmpq   0x00007f81d4d360f2
  0x00007f81d4d365f3:   mov    %rdx,%rdx
  0x00007f81d4d365f6:   callq  0x00007f81d480a680           ; ImmutableOopMap {[160]=Oop r11=Oop [168]=Oop r13=Oop [176]=Oop rbx=Oop [208]=Oop [200]=Oop }
                                                            ;*new {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::newNode@0 (line 1799)
                                                            ; - java.util.HashMap::putVal@152 (line 642)
                                                            ;   {runtime_call fast_new_instance Runtime1 stub}
  0x00007f81d4d365fb:   jmpq   0x00007f81d4d36140
  0x00007f81d4d36600:   movabs $0x7f81d3e710e8,%r10         ;   {metadata({method} {0x00007f81d3e710e8} '<init>' '(ILjava/lang/Object;Ljava/lang/Object;Ljava/util/HashMap$Node;)V' in 'java/util/HashMap$Node')}
  0x00007f81d4d3660a:   mov    %r10,0x8(%rsp)
  0x00007f81d4d3660f:   movq   $0xffffffffffffffff,(%rsp)
  0x00007f81d4d36617:   callq  0x00007f81d489e000           ; ImmutableOopMap {[160]=Oop r11=Oop [168]=Oop r13=Oop [176]=Oop rbx=Oop [208]=Oop [200]=Oop rax=Oop }
                                                            ;*synchronization entry
                                                            ; - java.util.HashMap$Node::<init>@-1 (line 285)
                                                            ; - java.util.HashMap::newNode@9 (line 1799)
                                                            ; - java.util.HashMap::putVal@152 (line 642)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d3661c:   jmpq   0x00007f81d4d3617d
  0x00007f81d4d36621:   movabs $0x7f81d3cfe650,%r10         ;   {metadata({method} {0x00007f81d3cfe650} '<init>' '()V' in 'java/lang/Object')}
  0x00007f81d4d3662b:   mov    %r10,0x8(%rsp)
  0x00007f81d4d36630:   movq   $0xffffffffffffffff,(%rsp)
  0x00007f81d4d36638:   callq  0x00007f81d489e000           ; ImmutableOopMap {[160]=Oop r11=Oop [168]=Oop r13=Oop [176]=Oop rbx=Oop [208]=Oop [200]=Oop rax=Oop }
                                                            ;*synchronization entry
                                                            ; - java.lang.Object::<init>@-1 (line 50)
                                                            ; - java.util.HashMap$Node::<init>@1 (line 285)
                                                            ; - java.util.HashMap::newNode@9 (line 1799)
                                                            ; - java.util.HashMap::putVal@152 (line 642)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d3663d:   jmpq   0x00007f81d4d361ba
  0x00007f81d4d36642:   callq  0x00007f81d480afa0           ; ImmutableOopMap {rsi=Oop [160]=Oop [176]=Oop rax=Oop }
                                                            ;*getfield value {reexecute=1 rethrow=0 return_oop=0}
                                                            ; - (reexecute) java.util.HashMap::putVal@229 (line 654)
                                                            ;   {runtime_call throw_null_pointer_exception Runtime1 stub}
  0x00007f81d4d36647:   movabs $0x7f81d3e43d90,%r10         ;   {metadata({method} {0x00007f81d3e43d90} 'afterNodeAccess' '(Ljava/util/HashMap$Node;)V' in 'java/util/HashMap')}
  0x00007f81d4d36651:   mov    %r10,0x8(%rsp)
  0x00007f81d4d36656:   movq   $0xffffffffffffffff,(%rsp)
  0x00007f81d4d3665e:   callq  0x00007f81d489e000           ; ImmutableOopMap {rdi=Oop }
                                                            ;*synchronization entry
                                                            ; - java.util.HashMap::afterNodeAccess@-1 (line 1831)
                                                            ; - java.util.HashMap::putVal@253 (line 657)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d36663:   jmpq   0x00007f81d4d36420
  0x00007f81d4d36668:   movabs $0x7f81d3e43e40,%r10         ;   {metadata({method} {0x00007f81d3e43e40} 'afterNodeInsertion' '(Z)V' in 'java/util/HashMap')}
  0x00007f81d4d36672:   mov    %r10,0x8(%rsp)
  0x00007f81d4d36677:   movq   $0xffffffffffffffff,(%rsp)
  0x00007f81d4d3667f:   callq  0x00007f81d489e000           ; ImmutableOopMap {}
                                                            ;*synchronization entry
                                                            ; - java.util.HashMap::afterNodeInsertion@-1 (line 1832)
                                                            ; - java.util.HashMap::putVal@295 (line 664)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d36684:   jmpq   0x00007f81d4d3649e
  0x00007f81d4d36689:   nop
  0x00007f81d4d3668a:   nop
  0x00007f81d4d3668b:   mov    0x3f0(%r15),%rax
  0x00007f81d4d36692:   movabs $0x0,%r10
  0x00007f81d4d3669c:   mov    %r10,0x3f0(%r15)
  0x00007f81d4d366a3:   movabs $0x0,%r10
  0x00007f81d4d366ad:   mov    %r10,0x3f8(%r15)
  0x00007f81d4d366b4:   add    $0xe0,%rsp
  0x00007f81d4d366bb:   pop    %rbp
  0x00007f81d4d366bc:   jmpq   0x00007f81d480be80           ;   {runtime_call unwind_exception Runtime1 stub}
  0x00007f81d4d366c1:   hlt    
  0x00007f81d4d366c2:   hlt    
  0x00007f81d4d366c3:   hlt    
  0x00007f81d4d366c4:   hlt    
  0x00007f81d4d366c5:   hlt    
  0x00007f81d4d366c6:   hlt    
  0x00007f81d4d366c7:   hlt    
  0x00007f81d4d366c8:   hlt    
  0x00007f81d4d366c9:   hlt    
  0x00007f81d4d366ca:   hlt    
  0x00007f81d4d366cb:   hlt    
  0x00007f81d4d366cc:   hlt    
  0x00007f81d4d366cd:   hlt    
  0x00007f81d4d366ce:   hlt    
  0x00007f81d4d366cf:   hlt    
  0x00007f81d4d366d0:   hlt    
  0x00007f81d4d366d1:   hlt    
  0x00007f81d4d366d2:   hlt    
  0x00007f81d4d366d3:   hlt    
  0x00007f81d4d366d4:   hlt    
  0x00007f81d4d366d5:   hlt    
  0x00007f81d4d366d6:   hlt    
  0x00007f81d4d366d7:   hlt    
  0x00007f81d4d366d8:   hlt    
  0x00007f81d4d366d9:   hlt    
  0x00007f81d4d366da:   hlt    
  0x00007f81d4d366db:   hlt    
  0x00007f81d4d366dc:   hlt    
  0x00007f81d4d366dd:   hlt    
  0x00007f81d4d366de:   hlt    
  0x00007f81d4d366df:   hlt    
[Stub Code]
  0x00007f81d4d366e0:   nopl   0x0(%rax,%rax,1)             ;   {no_reloc}
  0x00007f81d4d366e5:   movabs $0x0,%rbx                    ;   {static_stub}
  0x00007f81d4d366ef:   jmpq   0x00007f81d4d366ef           ;   {runtime_call}
  0x00007f81d4d366f4:   nop
  0x00007f81d4d366f5:   movabs $0x0,%rbx                    ;   {static_stub}
  0x00007f81d4d366ff:   jmpq   0x00007f81d4d366ff           ;   {runtime_call}
  0x00007f81d4d36704:   nop
  0x00007f81d4d36705:   movabs $0x0,%rbx                    ;   {static_stub}
  0x00007f81d4d3670f:   jmpq   0x00007f81d4d3670f           ;   {runtime_call}
  0x00007f81d4d36714:   nop
  0x00007f81d4d36715:   movabs $0x0,%rbx                    ;   {static_stub}
  0x00007f81d4d3671f:   jmpq   0x00007f81d4d3671f           ;   {runtime_call}
  0x00007f81d4d36724:   nop
  0x00007f81d4d36725:   movabs $0x0,%rbx                    ;   {static_stub}
  0x00007f81d4d3672f:   jmpq   0x00007f81d4d3672f           ;   {runtime_call}
  0x00007f81d4d36734:   nop
  0x00007f81d4d36735:   movabs $0x0,%rbx                    ;   {static_stub}
  0x00007f81d4d3673f:   jmpq   0x00007f81d4d3673f           ;   {runtime_call}
[Exception Handler]
  0x00007f81d4d36744:   callq  0x00007f81d489ad00           ;   {runtime_call handle_exception_from_callee Runtime1 stub}
  0x00007f81d4d36749:   mov    %rsp,-0x28(%rsp)
  0x00007f81d4d3674e:   sub    $0x80,%rsp
  0x00007f81d4d36755:   mov    %rax,0x78(%rsp)
  0x00007f81d4d3675a:   mov    %rcx,0x70(%rsp)
  0x00007f81d4d3675f:   mov    %rdx,0x68(%rsp)
  0x00007f81d4d36764:   mov    %rbx,0x60(%rsp)
  0x00007f81d4d36769:   mov    %rbp,0x50(%rsp)
  0x00007f81d4d3676e:   mov    %rsi,0x48(%rsp)
  0x00007f81d4d36773:   mov    %rdi,0x40(%rsp)
  0x00007f81d4d36778:   mov    %r8,0x38(%rsp)
  0x00007f81d4d3677d:   mov    %r9,0x30(%rsp)
  0x00007f81d4d36782:   mov    %r10,0x28(%rsp)
  0x00007f81d4d36787:   mov    %r11,0x20(%rsp)
  0x00007f81d4d3678c:   mov    %r12,0x18(%rsp)
  0x00007f81d4d36791:   mov    %r13,0x10(%rsp)
  0x00007f81d4d36796:   mov    %r14,0x8(%rsp)
  0x00007f81d4d3679b:   mov    %r15,(%rsp)
  0x00007f81d4d3679f:   movabs $0x7f81f15ff3e2,%rdi         ;   {external_word}
  0x00007f81d4d367a9:   movabs $0x7f81d4d36749,%rsi         ;   {internal_word}
  0x00007f81d4d367b3:   mov    %rsp,%rdx
  0x00007f81d4d367b6:   and    $0xfffffffffffffff0,%rsp
  0x00007f81d4d367ba:   callq  0x00007f81f1108240           ;   {runtime_call}
  0x00007f81d4d367bf:   hlt    
[Deopt Handler Code]
  0x00007f81d4d367c0:   movabs $0x7f81d4d367c0,%r10         ;   {section_word}
  0x00007f81d4d367ca:   push   %r10
  0x00007f81d4d367cc:   jmpq   0x00007f81d47ed0a0           ;   {runtime_call DeoptimizationBlob}
  0x00007f81d4d367d1:   hlt    
  0x00007f81d4d367d2:   hlt    
  0x00007f81d4d367d3:   hlt    
  0x00007f81d4d367d4:   hlt    
  0x00007f81d4d367d5:   hlt    
  0x00007f81d4d367d6:   hlt    
  0x00007f81d4d367d7:   hlt    
--------------------------------------------------------------------------------

============================= C1-compiled nmethod ==============================
----------------------------------- Assembly -----------------------------------

Compiled method (c1)     155    3       2       java.lang.String::hashCode (60 bytes)
 total in heap  [0x00007f81d4d37410,0x00007f81d4d379c0] = 1456
 relocation     [0x00007f81d4d37570,0x00007f81d4d375b0] = 64
 main code      [0x00007f81d4d375c0,0x00007f81d4d37700] = 320
 stub code      [0x00007f81d4d37700,0x00007f81d4d377b8] = 184
 metadata       [0x00007f81d4d377b8,0x00007f81d4d377c8] = 16
 scopes data    [0x00007f81d4d377c8,0x00007f81d4d37858] = 144
 scopes pcs     [0x00007f81d4d37858,0x00007f81d4d379b8] = 352
 dependencies   [0x00007f81d4d379b8,0x00007f81d4d379c0] = 8

--------------------------------------------------------------------------------
[Constant Pool (empty)]

--------------------------------------------------------------------------------

[Entry Point]
  # {method} {0x00007f81d3d056e0} 'hashCode' '()I' in 'java/lang/String'
  #           [sp+0x50]  (sp of caller)
  0x00007f81d4d375c0:   mov    0x8(%rsi),%r10d
  0x00007f81d4d375c4:   shl    $0x3,%r10
  0x00007f81d4d375c8:   cmp    %rax,%r10
  0x00007f81d4d375cb:   jne    0x00007f81d47eed00           ;   {runtime_call ic_miss_stub}
  0x00007f81d4d375d1:   data16 data16 nopw 0x0(%rax,%rax,1)
  0x00007f81d4d375dc:   data16 data16 xchg %ax,%ax
[Verified Entry Point]
  0x00007f81d4d375e0:   mov    %eax,-0x14000(%rsp)
  0x00007f81d4d375e7:   push   %rbp
  0x00007f81d4d375e8:   sub    $0x40,%rsp
  0x00007f81d4d375ec:   mov    %rsi,0x28(%rsp)
  0x00007f81d4d375f1:   movabs $0x7f81d3e6bb28,%rdi
  0x00007f81d4d375fb:   mov    0x18(%rdi),%ebx
  0x00007f81d4d375fe:   add    $0x8,%ebx
  0x00007f81d4d37601:   mov    %ebx,0x18(%rdi)
  0x00007f81d4d37604:   and    $0x3ff8,%ebx
  0x00007f81d4d3760a:   cmp    $0x0,%ebx
  0x00007f81d4d3760d:   je     0x00007f81d4d3769f           ;*aload_0 {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::hashCode@0 (line 1531)
  0x00007f81d4d37613:   mov    0x10(%rsi),%edi              ;*getfield hash {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::hashCode@1 (line 1531)
  0x00007f81d4d37616:   cmp    $0x0,%edi
  0x00007f81d4d37619:   jne    0x00007f81d4d3768c           ;*ifne {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::hashCode@6 (line 1532)
  0x00007f81d4d3761f:   movsbl 0x15(%rsi),%ebx              ;*getfield hashIsZero {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::hashCode@10 (line 1532)
  0x00007f81d4d37623:   cmp    $0x0,%ebx
  0x00007f81d4d37626:   jne    0x00007f81d4d3768c           ;*ifne {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::hashCode@13 (line 1532)
  0x00007f81d4d3762c:   movsbl 0x14(%rsi),%edi              ;*getfield coder {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::isLatin1@7 (line 3667)
                                                            ; - java.lang.String::hashCode@17 (line 1533)
  0x00007f81d4d37630:   cmp    $0x0,%edi
  0x00007f81d4d37633:   mov    $0x0,%edi
  0x00007f81d4d37638:   jne    0x00007f81d4d37643
  0x00007f81d4d3763e:   mov    $0x1,%edi
  0x00007f81d4d37643:   and    $0x1,%edi
  0x00007f81d4d37646:   cmp    $0x0,%edi
  0x00007f81d4d37649:   je     0x00007f81d4d37661           ;*ifeq {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::hashCode@20 (line 1533)
  0x00007f81d4d3764f:   mov    0xc(%rsi),%edi               ;*getfield value {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::hashCode@24 (line 1533)
  0x00007f81d4d37652:   mov    %rdi,%rsi                    ;*invokestatic hashCode {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::hashCode@27 (line 1533)
  0x00007f81d4d37655:   xchg   %ax,%ax
  0x00007f81d4d37657:   callq  0x00007f81d47ee400           ; ImmutableOopMap {[40]=Oop }
                                                            ;*invokestatic hashCode {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::hashCode@27 (line 1533)
                                                            ;   {static_call}
  0x00007f81d4d3765c:   jmpq   0x00007f81d4d3766c           ;*goto {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::hashCode@30 (line 1533)
  0x00007f81d4d37661:   mov    0xc(%rsi),%edi               ;*getfield value {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::hashCode@34 (line 1534)
  0x00007f81d4d37664:   mov    %rdi,%rsi                    ;*invokestatic hashCode {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::hashCode@37 (line 1534)
  0x00007f81d4d37667:   callq  0x00007f81d47ee400           ; ImmutableOopMap {[40]=Oop }
                                                            ;*invokestatic hashCode {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::hashCode@37 (line 1534)
                                                            ;   {static_call}
  0x00007f81d4d3766c:   cmp    $0x0,%eax
  0x00007f81d4d3766f:   mov    0x28(%rsp),%rsi
  0x00007f81d4d37674:   jne    0x00007f81d4d37686           ;*ifne {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::hashCode@42 (line 1535)
  0x00007f81d4d3767a:   movb   $0x1,0x15(%rsi)              ;*putfield hashIsZero {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::hashCode@47 (line 1536)
  0x00007f81d4d3767e:   mov    %rax,%rdi
  0x00007f81d4d37681:   jmpq   0x00007f81d4d3768c           ;*goto {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::hashCode@50 (line 1536)
  0x00007f81d4d37686:   mov    %eax,0x10(%rsi)              ;*putfield hash {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::hashCode@55 (line 1538)
  0x00007f81d4d37689:   mov    %rax,%rdi                    ;*iload_1 {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::hashCode@58 (line 1541)
  0x00007f81d4d3768c:   mov    %rdi,%rax
  0x00007f81d4d3768f:   add    $0x40,%rsp
  0x00007f81d4d37693:   pop    %rbp
  0x00007f81d4d37694:   mov    0x108(%r15),%r10
  0x00007f81d4d3769b:   test   %eax,(%r10)                  ;   {poll_return}
  0x00007f81d4d3769e:   retq   
  0x00007f81d4d3769f:   movabs $0x7f81d3d056e0,%r10         ;   {metadata({method} {0x00007f81d3d056e0} 'hashCode' '()I' in 'java/lang/String')}
  0x00007f81d4d376a9:   mov    %r10,0x8(%rsp)
  0x00007f81d4d376ae:   movq   $0xffffffffffffffff,(%rsp)
  0x00007f81d4d376b6:   callq  0x00007f81d489e000           ; ImmutableOopMap {rsi=Oop [40]=Oop }
                                                            ;*synchronization entry
                                                            ; - java.lang.String::hashCode@-1 (line 1531)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d376bb:   jmpq   0x00007f81d4d37613
  0x00007f81d4d376c0:   nop
  0x00007f81d4d376c1:   nop
  0x00007f81d4d376c2:   mov    0x3f0(%r15),%rax
  0x00007f81d4d376c9:   movabs $0x0,%r10
  0x00007f81d4d376d3:   mov    %r10,0x3f0(%r15)
  0x00007f81d4d376da:   movabs $0x0,%r10
  0x00007f81d4d376e4:   mov    %r10,0x3f8(%r15)
  0x00007f81d4d376eb:   add    $0x40,%rsp
  0x00007f81d4d376ef:   pop    %rbp
  0x00007f81d4d376f0:   jmpq   0x00007f81d480be80           ;   {runtime_call unwind_exception Runtime1 stub}
  0x00007f81d4d376f5:   hlt    
  0x00007f81d4d376f6:   hlt    
  0x00007f81d4d376f7:   hlt    
  0x00007f81d4d376f8:   hlt    
  0x00007f81d4d376f9:   hlt    
  0x00007f81d4d376fa:   hlt    
  0x00007f81d4d376fb:   hlt    
  0x00007f81d4d376fc:   hlt    
  0x00007f81d4d376fd:   hlt    
  0x00007f81d4d376fe:   hlt    
  0x00007f81d4d376ff:   hlt    
[Stub Code]
  0x00007f81d4d37700:   nopl   0x0(%rax,%rax,1)             ;   {no_reloc}
  0x00007f81d4d37705:   movabs $0x0,%rbx                    ;   {static_stub}
  0x00007f81d4d3770f:   jmpq   0x00007f81d4d3770f           ;   {runtime_call}
  0x00007f81d4d37714:   nop
  0x00007f81d4d37715:   movabs $0x0,%rbx                    ;   {static_stub}
  0x00007f81d4d3771f:   jmpq   0x00007f81d4d3771f           ;   {runtime_call}
[Exception Handler]
  0x00007f81d4d37724:   callq  0x00007f81d489ad00           ;   {runtime_call handle_exception_from_callee Runtime1 stub}
  0x00007f81d4d37729:   mov    %rsp,-0x28(%rsp)
  0x00007f81d4d3772e:   sub    $0x80,%rsp
  0x00007f81d4d37735:   mov    %rax,0x78(%rsp)
  0x00007f81d4d3773a:   mov    %rcx,0x70(%rsp)
  0x00007f81d4d3773f:   mov    %rdx,0x68(%rsp)
  0x00007f81d4d37744:   mov    %rbx,0x60(%rsp)
  0x00007f81d4d37749:   mov    %rbp,0x50(%rsp)
  0x00007f81d4d3774e:   mov    %rsi,0x48(%rsp)
  0x00007f81d4d37753:   mov    %rdi,0x40(%rsp)
  0x00007f81d4d37758:   mov    %r8,0x38(%rsp)
  0x00007f81d4d3775d:   mov    %r9,0x30(%rsp)
  0x00007f81d4d37762:   mov    %r10,0x28(%rsp)
  0x00007f81d4d37767:   mov    %r11,0x20(%rsp)
  0x00007f81d4d3776c:   mov    %r12,0x18(%rsp)
  0x00007f81d4d37771:   mov    %r13,0x10(%rsp)
  0x00007f81d4d37776:   mov    %r14,0x8(%rsp)
  0x00007f81d4d3777b:   mov    %r15,(%rsp)
  0x00007f81d4d3777f:   movabs $0x7f81f15ff3e2,%rdi         ;   {external_word}
  0x00007f81d4d37789:   movabs $0x7f81d4d37729,%rsi         ;   {internal_word}
  0x00007f81d4d37793:   mov    %rsp,%rdx
  0x00007f81d4d37796:   and    $0xfffffffffffffff0,%rsp
  0x00007f81d4d3779a:   callq  0x00007f81f1108240           ;   {runtime_call}
  0x00007f81d4d3779f:   hlt    
[Deopt Handler Code]
  0x00007f81d4d377a0:   movabs $0x7f81d4d377a0,%r10         ;   {section_word}
  0x00007f81d4d377aa:   push   %r10
  0x00007f81d4d377ac:   jmpq   0x00007f81d47ed0a0           ;   {runtime_call DeoptimizationBlob}
  0x00007f81d4d377b1:   hlt    
  0x00007f81d4d377b2:   hlt    
  0x00007f81d4d377b3:   hlt    
  0x00007f81d4d377b4:   hlt    
  0x00007f81d4d377b5:   hlt    
  0x00007f81d4d377b6:   hlt    
  0x00007f81d4d377b7:   hlt    
--------------------------------------------------------------------------------

============================= C1-compiled nmethod ==============================
----------------------------------- Assembly -----------------------------------

Compiled method (c1)     169   16       2       java.util.ImmutableCollections$SetN$SetNIterator::nextIndex (56 bytes)
 total in heap  [0x00007f81d4d37a10,0x00007f81d4d37e58] = 1096
 relocation     [0x00007f81d4d37b70,0x00007f81d4d37ba0] = 48
 main code      [0x00007f81d4d37ba0,0x00007f81d4d37c80] = 224
 stub code      [0x00007f81d4d37c80,0x00007f81d4d37d10] = 144
 metadata       [0x00007f81d4d37d10,0x00007f81d4d37d18] = 8
 scopes data    [0x00007f81d4d37d18,0x00007f81d4d37d68] = 80
 scopes pcs     [0x00007f81d4d37d68,0x00007f81d4d37e38] = 208
 dependencies   [0x00007f81d4d37e38,0x00007f81d4d37e40] = 8
 nul chk table  [0x00007f81d4d37e40,0x00007f81d4d37e58] = 24

--------------------------------------------------------------------------------
[Constant Pool (empty)]

--------------------------------------------------------------------------------

[Entry Point]
  # {method} {0x00007f81d3f88310} 'nextIndex' '()I' in 'java/util/ImmutableCollections$SetN$SetNIterator'
  #           [sp+0x40]  (sp of caller)
  0x00007f81d4d37ba0:   mov    0x8(%rsi),%r10d
  0x00007f81d4d37ba4:   shl    $0x3,%r10
  0x00007f81d4d37ba8:   cmp    %rax,%r10
  0x00007f81d4d37bab:   jne    0x00007f81d47eed00           ;   {runtime_call ic_miss_stub}
  0x00007f81d4d37bb1:   data16 data16 nopw 0x0(%rax,%rax,1)
  0x00007f81d4d37bbc:   data16 data16 xchg %ax,%ax
[Verified Entry Point]
  0x00007f81d4d37bc0:   mov    %eax,-0x14000(%rsp)
  0x00007f81d4d37bc7:   push   %rbp
  0x00007f81d4d37bc8:   sub    $0x30,%rsp
  0x00007f81d4d37bcc:   movabs $0x7f81d3f89148,%rax
  0x00007f81d4d37bd6:   mov    0x18(%rax),%edi
  0x00007f81d4d37bd9:   add    $0x8,%edi
  0x00007f81d4d37bdc:   mov    %edi,0x18(%rax)
  0x00007f81d4d37bdf:   and    $0x3ff8,%edi
  0x00007f81d4d37be5:   cmp    $0x0,%edi
  0x00007f81d4d37be8:   je     0x00007f81d4d37c1c           ;*aload_0 {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::nextIndex@0 (line 748)
  0x00007f81d4d37bee:   mov    0x10(%rsi),%eax              ;*getfield idx {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::nextIndex@1 (line 748)
  0x00007f81d4d37bf1:   inc    %eax
  0x00007f81d4d37bf3:   mov    0x14(%rsi),%edi              ;*getfield this$0 {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::nextIndex@16 (line 750)
  0x00007f81d4d37bf6:   mov    0x10(%rdi),%edi              ; implicit exception: dispatches to 0x00007f81d4d37c3a
                                                            ;*getfield elements {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::nextIndex@19 (line 750)
  0x00007f81d4d37bf9:   mov    0xc(%rdi),%edi               ; implicit exception: dispatches to 0x00007f81d4d37c3f
                                                            ;*arraylength {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::nextIndex@22 (line 750)
  0x00007f81d4d37bfc:   cmp    %edi,%eax
  0x00007f81d4d37bfe:   jl     0x00007f81d4d37c09           ;*if_icmplt {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::nextIndex@23 (line 750)
  0x00007f81d4d37c04:   mov    $0x0,%eax                    ;*goto {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::nextIndex@28 (line 751)
  0x00007f81d4d37c09:   mov    %eax,0x10(%rsi)              ;*putfield idx {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::nextIndex@52 (line 758)
  0x00007f81d4d37c0c:   add    $0x30,%rsp
  0x00007f81d4d37c10:   pop    %rbp
  0x00007f81d4d37c11:   mov    0x108(%r15),%r10
  0x00007f81d4d37c18:   test   %eax,(%r10)                  ;   {poll_return}
  0x00007f81d4d37c1b:   retq   
  0x00007f81d4d37c1c:   movabs $0x7f81d3f88310,%r10         ;   {metadata({method} {0x00007f81d3f88310} 'nextIndex' '()I' in 'java/util/ImmutableCollections$SetN$SetNIterator')}
  0x00007f81d4d37c26:   mov    %r10,0x8(%rsp)
  0x00007f81d4d37c2b:   movq   $0xffffffffffffffff,(%rsp)
  0x00007f81d4d37c33:   callq  0x00007f81d489e000           ; ImmutableOopMap {rsi=Oop }
                                                            ;*synchronization entry
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::nextIndex@-1 (line 748)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d37c38:   jmp    0x00007f81d4d37bee
  0x00007f81d4d37c3a:   callq  0x00007f81d480afa0           ; ImmutableOopMap {rsi=Oop }
                                                            ;*getfield elements {reexecute=1 rethrow=0 return_oop=0}
                                                            ; - (reexecute) java.util.ImmutableCollections$SetN$SetNIterator::nextIndex@19 (line 750)
                                                            ;   {runtime_call throw_null_pointer_exception Runtime1 stub}
  0x00007f81d4d37c3f:   callq  0x00007f81d480afa0           ; ImmutableOopMap {rsi=Oop }
                                                            ;*arraylength {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::nextIndex@22 (line 750)
                                                            ;   {runtime_call throw_null_pointer_exception Runtime1 stub}
  0x00007f81d4d37c44:   nop
  0x00007f81d4d37c45:   nop
  0x00007f81d4d37c46:   mov    0x3f0(%r15),%rax
  0x00007f81d4d37c4d:   movabs $0x0,%r10
  0x00007f81d4d37c57:   mov    %r10,0x3f0(%r15)
  0x00007f81d4d37c5e:   movabs $0x0,%r10
  0x00007f81d4d37c68:   mov    %r10,0x3f8(%r15)
  0x00007f81d4d37c6f:   add    $0x30,%rsp
  0x00007f81d4d37c73:   pop    %rbp
  0x00007f81d4d37c74:   jmpq   0x00007f81d480be80           ;   {runtime_call unwind_exception Runtime1 stub}
  0x00007f81d4d37c79:   hlt    
  0x00007f81d4d37c7a:   hlt    
  0x00007f81d4d37c7b:   hlt    
  0x00007f81d4d37c7c:   hlt    
  0x00007f81d4d37c7d:   hlt    
  0x00007f81d4d37c7e:   hlt    
  0x00007f81d4d37c7f:   hlt    
[Exception Handler]
  0x00007f81d4d37c80:   callq  0x00007f81d489ad00           ;   {no_reloc}
  0x00007f81d4d37c85:   mov    %rsp,-0x28(%rsp)
  0x00007f81d4d37c8a:   sub    $0x80,%rsp
  0x00007f81d4d37c91:   mov    %rax,0x78(%rsp)
  0x00007f81d4d37c96:   mov    %rcx,0x70(%rsp)
  0x00007f81d4d37c9b:   mov    %rdx,0x68(%rsp)
  0x00007f81d4d37ca0:   mov    %rbx,0x60(%rsp)
  0x00007f81d4d37ca5:   mov    %rbp,0x50(%rsp)
  0x00007f81d4d37caa:   mov    %rsi,0x48(%rsp)
  0x00007f81d4d37caf:   mov    %rdi,0x40(%rsp)
  0x00007f81d4d37cb4:   mov    %r8,0x38(%rsp)
  0x00007f81d4d37cb9:   mov    %r9,0x30(%rsp)
  0x00007f81d4d37cbe:   mov    %r10,0x28(%rsp)
  0x00007f81d4d37cc3:   mov    %r11,0x20(%rsp)
  0x00007f81d4d37cc8:   mov    %r12,0x18(%rsp)
  0x00007f81d4d37ccd:   mov    %r13,0x10(%rsp)
  0x00007f81d4d37cd2:   mov    %r14,0x8(%rsp)
  0x00007f81d4d37cd7:   mov    %r15,(%rsp)
  0x00007f81d4d37cdb:   movabs $0x7f81f15ff3e2,%rdi         ;   {external_word}
  0x00007f81d4d37ce5:   movabs $0x7f81d4d37c85,%rsi         ;   {internal_word}
  0x00007f81d4d37cef:   mov    %rsp,%rdx
  0x00007f81d4d37cf2:   and    $0xfffffffffffffff0,%rsp
  0x00007f81d4d37cf6:   callq  0x00007f81f1108240           ;   {runtime_call}
  0x00007f81d4d37cfb:   hlt    
[Deopt Handler Code]
  0x00007f81d4d37cfc:   movabs $0x7f81d4d37cfc,%r10         ;   {section_word}
  0x00007f81d4d37d06:   push   %r10
  0x00007f81d4d37d08:   jmpq   0x00007f81d47ed0a0           ;   {runtime_call DeoptimizationBlob}
  0x00007f81d4d37d0d:   hlt    
  0x00007f81d4d37d0e:   hlt    
  0x00007f81d4d37d0f:   hlt    
--------------------------------------------------------------------------------

============================= C1-compiled nmethod ==============================
----------------------------------- Assembly -----------------------------------

Compiled method (c1)     176    6       3       java.lang.String::isLatin1 (19 bytes)
 total in heap  [0x00007f81d4d37e90,0x00007f81d4d382c0] = 1072
 relocation     [0x00007f81d4d37ff0,0x00007f81d4d38020] = 48
 main code      [0x00007f81d4d38020,0x00007f81d4d38160] = 320
 stub code      [0x00007f81d4d38160,0x00007f81d4d381f0] = 144
 metadata       [0x00007f81d4d381f0,0x00007f81d4d381f8] = 8
 scopes data    [0x00007f81d4d381f8,0x00007f81d4d38228] = 48
 scopes pcs     [0x00007f81d4d38228,0x00007f81d4d382b8] = 144
 dependencies   [0x00007f81d4d382b8,0x00007f81d4d382c0] = 8

--------------------------------------------------------------------------------
[Constant Pool (empty)]

--------------------------------------------------------------------------------

[Entry Point]
  # {method} {0x00007f81d3d09750} 'isLatin1' '()Z' in 'java/lang/String'
  #           [sp+0x40]  (sp of caller)
  0x00007f81d4d38020:   mov    0x8(%rsi),%r10d
  0x00007f81d4d38024:   shl    $0x3,%r10
  0x00007f81d4d38028:   cmp    %rax,%r10
  0x00007f81d4d3802b:   jne    0x00007f81d47eed00           ;   {runtime_call ic_miss_stub}
  0x00007f81d4d38031:   data16 data16 nopw 0x0(%rax,%rax,1)
  0x00007f81d4d3803c:   data16 data16 xchg %ax,%ax
[Verified Entry Point]
  0x00007f81d4d38040:   mov    %eax,-0x14000(%rsp)
  0x00007f81d4d38047:   push   %rbp
  0x00007f81d4d38048:   sub    $0x30,%rsp
  0x00007f81d4d3804c:   movabs $0x7f81d3f43f38,%rax         ;   {metadata(method data for {method} {0x00007f81d3d09750} 'isLatin1' '()Z' in 'java/lang/String')}
  0x00007f81d4d38056:   mov    0x13c(%rax),%edi
  0x00007f81d4d3805c:   add    $0x8,%edi
  0x00007f81d4d3805f:   mov    %edi,0x13c(%rax)
  0x00007f81d4d38065:   and    $0x1ff8,%edi
  0x00007f81d4d3806b:   cmp    $0x0,%edi
  0x00007f81d4d3806e:   je     0x00007f81d4d380f3           ;*getstatic COMPACT_STRINGS {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::isLatin1@0 (line 3667)
  0x00007f81d4d38074:   movabs $0x7f81d3f43f38,%rax         ;   {metadata(method data for {method} {0x00007f81d3d09750} 'isLatin1' '()Z' in 'java/lang/String')}
  0x00007f81d4d3807e:   incl   0x190(%rax)                  ;*ifeq {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::isLatin1@3 (line 3667)
  0x00007f81d4d38084:   movsbl 0x14(%rsi),%eax              ;*getfield coder {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::isLatin1@7 (line 3667)
  0x00007f81d4d38088:   cmp    $0x0,%eax
  0x00007f81d4d3808b:   movabs $0x7f81d3f43f38,%rax         ;   {metadata(method data for {method} {0x00007f81d3d09750} 'isLatin1' '()Z' in 'java/lang/String')}
  0x00007f81d4d38095:   movabs $0x1a0,%rsi
  0x00007f81d4d3809f:   jne    0x00007f81d4d380af
  0x00007f81d4d380a5:   movabs $0x1b0,%rsi
  0x00007f81d4d380af:   mov    (%rax,%rsi,1),%rdi
  0x00007f81d4d380b3:   lea    0x1(%rdi),%rdi
  0x00007f81d4d380b7:   mov    %rdi,(%rax,%rsi,1)
  0x00007f81d4d380bb:   jne    0x00007f81d4d380db           ;*ifne {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::isLatin1@10 (line 3667)
  0x00007f81d4d380c1:   movabs $0x7f81d3f43f38,%rax         ;   {metadata(method data for {method} {0x00007f81d3d09750} 'isLatin1' '()Z' in 'java/lang/String')}
  0x00007f81d4d380cb:   incl   0x1c0(%rax)
  0x00007f81d4d380d1:   mov    $0x1,%eax
  0x00007f81d4d380d6:   jmpq   0x00007f81d4d380e0           ;*goto {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::isLatin1@14 (line 3667)
  0x00007f81d4d380db:   mov    $0x0,%eax                    ;*ireturn {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::isLatin1@18 (line 3667)
  0x00007f81d4d380e0:   and    $0x1,%eax
  0x00007f81d4d380e3:   add    $0x30,%rsp
  0x00007f81d4d380e7:   pop    %rbp
  0x00007f81d4d380e8:   mov    0x108(%r15),%r10
  0x00007f81d4d380ef:   test   %eax,(%r10)                  ;   {poll_return}
  0x00007f81d4d380f2:   retq   
  0x00007f81d4d380f3:   movabs $0x7f81d3d09750,%r10         ;   {metadata({method} {0x00007f81d3d09750} 'isLatin1' '()Z' in 'java/lang/String')}
  0x00007f81d4d380fd:   mov    %r10,0x8(%rsp)
  0x00007f81d4d38102:   movq   $0xffffffffffffffff,(%rsp)
  0x00007f81d4d3810a:   callq  0x00007f81d489e000           ; ImmutableOopMap {rsi=Oop }
                                                            ;*synchronization entry
                                                            ; - java.lang.String::isLatin1@-1 (line 3667)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d3810f:   jmpq   0x00007f81d4d38074
  0x00007f81d4d38114:   nop
  0x00007f81d4d38115:   nop
  0x00007f81d4d38116:   mov    0x3f0(%r15),%rax
  0x00007f81d4d3811d:   movabs $0x0,%r10
  0x00007f81d4d38127:   mov    %r10,0x3f0(%r15)
  0x00007f81d4d3812e:   movabs $0x0,%r10
  0x00007f81d4d38138:   mov    %r10,0x3f8(%r15)
  0x00007f81d4d3813f:   add    $0x30,%rsp
  0x00007f81d4d38143:   pop    %rbp
  0x00007f81d4d38144:   jmpq   0x00007f81d480be80           ;   {runtime_call unwind_exception Runtime1 stub}
  0x00007f81d4d38149:   hlt    
  0x00007f81d4d3814a:   hlt    
  0x00007f81d4d3814b:   hlt    
  0x00007f81d4d3814c:   hlt    
  0x00007f81d4d3814d:   hlt    
  0x00007f81d4d3814e:   hlt    
  0x00007f81d4d3814f:   hlt    
  0x00007f81d4d38150:   hlt    
  0x00007f81d4d38151:   hlt    
  0x00007f81d4d38152:   hlt    
  0x00007f81d4d38153:   hlt    
  0x00007f81d4d38154:   hlt    
  0x00007f81d4d38155:   hlt    
  0x00007f81d4d38156:   hlt    
  0x00007f81d4d38157:   hlt    
  0x00007f81d4d38158:   hlt    
  0x00007f81d4d38159:   hlt    
  0x00007f81d4d3815a:   hlt    
  0x00007f81d4d3815b:   hlt    
  0x00007f81d4d3815c:   hlt    
  0x00007f81d4d3815d:   hlt    
  0x00007f81d4d3815e:   hlt    
  0x00007f81d4d3815f:   hlt    
[Exception Handler]
  0x00007f81d4d38160:   callq  0x00007f81d489ad00           ;   {no_reloc}
  0x00007f81d4d38165:   mov    %rsp,-0x28(%rsp)
  0x00007f81d4d3816a:   sub    $0x80,%rsp
  0x00007f81d4d38171:   mov    %rax,0x78(%rsp)
  0x00007f81d4d38176:   mov    %rcx,0x70(%rsp)
  0x00007f81d4d3817b:   mov    %rdx,0x68(%rsp)
  0x00007f81d4d38180:   mov    %rbx,0x60(%rsp)
  0x00007f81d4d38185:   mov    %rbp,0x50(%rsp)
  0x00007f81d4d3818a:   mov    %rsi,0x48(%rsp)
  0x00007f81d4d3818f:   mov    %rdi,0x40(%rsp)
  0x00007f81d4d38194:   mov    %r8,0x38(%rsp)
  0x00007f81d4d38199:   mov    %r9,0x30(%rsp)
  0x00007f81d4d3819e:   mov    %r10,0x28(%rsp)
  0x00007f81d4d381a3:   mov    %r11,0x20(%rsp)
  0x00007f81d4d381a8:   mov    %r12,0x18(%rsp)
  0x00007f81d4d381ad:   mov    %r13,0x10(%rsp)
  0x00007f81d4d381b2:   mov    %r14,0x8(%rsp)
  0x00007f81d4d381b7:   mov    %r15,(%rsp)
  0x00007f81d4d381bb:   movabs $0x7f81f15ff3e2,%rdi         ;   {external_word}
  0x00007f81d4d381c5:   movabs $0x7f81d4d38165,%rsi         ;   {internal_word}
  0x00007f81d4d381cf:   mov    %rsp,%rdx
  0x00007f81d4d381d2:   and    $0xfffffffffffffff0,%rsp
  0x00007f81d4d381d6:   callq  0x00007f81f1108240           ;   {runtime_call}
  0x00007f81d4d381db:   hlt    
[Deopt Handler Code]
  0x00007f81d4d381dc:   movabs $0x7f81d4d381dc,%r10         ;   {section_word}
  0x00007f81d4d381e6:   push   %r10
  0x00007f81d4d381e8:   jmpq   0x00007f81d47ed0a0           ;   {runtime_call DeoptimizationBlob}
  0x00007f81d4d381ed:   hlt    
  0x00007f81d4d381ee:   hlt    
  0x00007f81d4d381ef:   hlt    
--------------------------------------------------------------------------------

============================= C1-compiled nmethod ==============================
----------------------------------- Assembly -----------------------------------

Compiled method (c1)     183   24       3       java.util.ImmutableCollections$SetN$SetNIterator::hasNext (13 bytes)
 total in heap  [0x00007f81d4d38310,0x00007f81d4d38710] = 1024
 relocation     [0x00007f81d4d38470,0x00007f81d4d384a0] = 48
 main code      [0x00007f81d4d384a0,0x00007f81d4d385c0] = 288
 stub code      [0x00007f81d4d385c0,0x00007f81d4d38650] = 144
 metadata       [0x00007f81d4d38650,0x00007f81d4d38658] = 8
 scopes data    [0x00007f81d4d38658,0x00007f81d4d38688] = 48
 scopes pcs     [0x00007f81d4d38688,0x00007f81d4d38708] = 128
 dependencies   [0x00007f81d4d38708,0x00007f81d4d38710] = 8

--------------------------------------------------------------------------------
[Constant Pool (empty)]

--------------------------------------------------------------------------------

[Entry Point]
  # {method} {0x00007f81d3f88220} 'hasNext' '()Z' in 'java/util/ImmutableCollections$SetN$SetNIterator'
  #           [sp+0x40]  (sp of caller)
  0x00007f81d4d384a0:   mov    0x8(%rsi),%r10d
  0x00007f81d4d384a4:   shl    $0x3,%r10
  0x00007f81d4d384a8:   cmp    %rax,%r10
  0x00007f81d4d384ab:   jne    0x00007f81d47eed00           ;   {runtime_call ic_miss_stub}
  0x00007f81d4d384b1:   data16 data16 nopw 0x0(%rax,%rax,1)
  0x00007f81d4d384bc:   data16 data16 xchg %ax,%ax
[Verified Entry Point]
  0x00007f81d4d384c0:   mov    %eax,-0x14000(%rsp)
  0x00007f81d4d384c7:   push   %rbp
  0x00007f81d4d384c8:   sub    $0x30,%rsp
  0x00007f81d4d384cc:   movabs $0x7f81d3f957b8,%rax         ;   {metadata(method data for {method} {0x00007f81d3f88220} 'hasNext' '()Z' in 'java/util/ImmutableCollections$SetN$SetNIterator')}
  0x00007f81d4d384d6:   mov    0x13c(%rax),%edi
  0x00007f81d4d384dc:   add    $0x8,%edi
  0x00007f81d4d384df:   mov    %edi,0x13c(%rax)
  0x00007f81d4d384e5:   and    $0x1ff8,%edi
  0x00007f81d4d384eb:   cmp    $0x0,%edi
  0x00007f81d4d384ee:   je     0x00007f81d4d38562           ;*aload_0 {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::hasNext@0 (line 744)
  0x00007f81d4d384f4:   mov    0xc(%rsi),%eax               ;*getfield remaining {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::hasNext@1 (line 744)
  0x00007f81d4d384f7:   cmp    $0x0,%eax
  0x00007f81d4d384fa:   movabs $0x7f81d3f957b8,%rax         ;   {metadata(method data for {method} {0x00007f81d3f88220} 'hasNext' '()Z' in 'java/util/ImmutableCollections$SetN$SetNIterator')}
  0x00007f81d4d38504:   movabs $0x180,%rsi
  0x00007f81d4d3850e:   jle    0x00007f81d4d3851e
  0x00007f81d4d38514:   movabs $0x190,%rsi
  0x00007f81d4d3851e:   mov    (%rax,%rsi,1),%rdi
  0x00007f81d4d38522:   lea    0x1(%rdi),%rdi
  0x00007f81d4d38526:   mov    %rdi,(%rax,%rsi,1)
  0x00007f81d4d3852a:   jle    0x00007f81d4d3854a           ;*ifle {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::hasNext@4 (line 744)
  0x00007f81d4d38530:   movabs $0x7f81d3f957b8,%rax         ;   {metadata(method data for {method} {0x00007f81d3f88220} 'hasNext' '()Z' in 'java/util/ImmutableCollections$SetN$SetNIterator')}
  0x00007f81d4d3853a:   incl   0x1a0(%rax)
  0x00007f81d4d38540:   mov    $0x1,%eax
  0x00007f81d4d38545:   jmpq   0x00007f81d4d3854f           ;*goto {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::hasNext@8 (line 744)
  0x00007f81d4d3854a:   mov    $0x0,%eax                    ;*ireturn {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::hasNext@12 (line 744)
  0x00007f81d4d3854f:   and    $0x1,%eax
  0x00007f81d4d38552:   add    $0x30,%rsp
  0x00007f81d4d38556:   pop    %rbp
  0x00007f81d4d38557:   mov    0x108(%r15),%r10
  0x00007f81d4d3855e:   test   %eax,(%r10)                  ;   {poll_return}
  0x00007f81d4d38561:   retq   
  0x00007f81d4d38562:   movabs $0x7f81d3f88220,%r10         ;   {metadata({method} {0x00007f81d3f88220} 'hasNext' '()Z' in 'java/util/ImmutableCollections$SetN$SetNIterator')}
  0x00007f81d4d3856c:   mov    %r10,0x8(%rsp)
  0x00007f81d4d38571:   movq   $0xffffffffffffffff,(%rsp)
  0x00007f81d4d38579:   callq  0x00007f81d489e000           ; ImmutableOopMap {rsi=Oop }
                                                            ;*synchronization entry
                                                            ; - java.util.ImmutableCollections$SetN$SetNIterator::hasNext@-1 (line 744)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d3857e:   jmpq   0x00007f81d4d384f4
  0x00007f81d4d38583:   nop
  0x00007f81d4d38584:   nop
  0x00007f81d4d38585:   mov    0x3f0(%r15),%rax
  0x00007f81d4d3858c:   movabs $0x0,%r10
  0x00007f81d4d38596:   mov    %r10,0x3f0(%r15)
  0x00007f81d4d3859d:   movabs $0x0,%r10
  0x00007f81d4d385a7:   mov    %r10,0x3f8(%r15)
  0x00007f81d4d385ae:   add    $0x30,%rsp
  0x00007f81d4d385b2:   pop    %rbp
  0x00007f81d4d385b3:   jmpq   0x00007f81d480be80           ;   {runtime_call unwind_exception Runtime1 stub}
  0x00007f81d4d385b8:   hlt    
  0x00007f81d4d385b9:   hlt    
  0x00007f81d4d385ba:   hlt    
  0x00007f81d4d385bb:   hlt    
  0x00007f81d4d385bc:   hlt    
  0x00007f81d4d385bd:   hlt    
  0x00007f81d4d385be:   hlt    
  0x00007f81d4d385bf:   hlt    
[Exception Handler]
  0x00007f81d4d385c0:   callq  0x00007f81d489ad00           ;   {no_reloc}
  0x00007f81d4d385c5:   mov    %rsp,-0x28(%rsp)
  0x00007f81d4d385ca:   sub    $0x80,%rsp
  0x00007f81d4d385d1:   mov    %rax,0x78(%rsp)
  0x00007f81d4d385d6:   mov    %rcx,0x70(%rsp)
  0x00007f81d4d385db:   mov    %rdx,0x68(%rsp)
  0x00007f81d4d385e0:   mov    %rbx,0x60(%rsp)
  0x00007f81d4d385e5:   mov    %rbp,0x50(%rsp)
  0x00007f81d4d385ea:   mov    %rsi,0x48(%rsp)
  0x00007f81d4d385ef:   mov    %rdi,0x40(%rsp)
  0x00007f81d4d385f4:   mov    %r8,0x38(%rsp)
  0x00007f81d4d385f9:   mov    %r9,0x30(%rsp)
  0x00007f81d4d385fe:   mov    %r10,0x28(%rsp)
  0x00007f81d4d38603:   mov    %r11,0x20(%rsp)
  0x00007f81d4d38608:   mov    %r12,0x18(%rsp)
  0x00007f81d4d3860d:   mov    %r13,0x10(%rsp)
  0x00007f81d4d38612:   mov    %r14,0x8(%rsp)
  0x00007f81d4d38617:   mov    %r15,(%rsp)
  0x00007f81d4d3861b:   movabs $0x7f81f15ff3e2,%rdi         ;   {external_word}
  0x00007f81d4d38625:   movabs $0x7f81d4d385c5,%rsi         ;   {internal_word}
  0x00007f81d4d3862f:   mov    %rsp,%rdx
  0x00007f81d4d38632:   and    $0xfffffffffffffff0,%rsp
  0x00007f81d4d38636:   callq  0x00007f81f1108240           ;   {runtime_call}
  0x00007f81d4d3863b:   hlt    
[Deopt Handler Code]
  0x00007f81d4d3863c:   movabs $0x7f81d4d3863c,%r10         ;   {section_word}
  0x00007f81d4d38646:   push   %r10
  0x00007f81d4d38648:   jmpq   0x00007f81d47ed0a0           ;   {runtime_call DeoptimizationBlob}
  0x00007f81d4d3864d:   hlt    
  0x00007f81d4d3864e:   hlt    
  0x00007f81d4d3864f:   hlt    
--------------------------------------------------------------------------------

============================= C1-compiled nmethod ==============================
----------------------------------- Assembly -----------------------------------

Compiled method (c1)     191   38  s    2       T::m (1 bytes)
 total in heap  [0x00007f81d4d38790,0x00007f81d4d38c30] = 1184
 relocation     [0x00007f81d4d388f0,0x00007f81d4d38920] = 48
 main code      [0x00007f81d4d38920,0x00007f81d4d38b20] = 512
 stub code      [0x00007f81d4d38b20,0x00007f81d4d38bb0] = 144
 oops           [0x00007f81d4d38bb0,0x00007f81d4d38bb8] = 8
 metadata       [0x00007f81d4d38bb8,0x00007f81d4d38bc0] = 8
 scopes data    [0x00007f81d4d38bc0,0x00007f81d4d38bd8] = 24
 scopes pcs     [0x00007f81d4d38bd8,0x00007f81d4d38c28] = 80
 dependencies   [0x00007f81d4d38c28,0x00007f81d4d38c30] = 8

--------------------------------------------------------------------------------
[Constant Pool (empty)]

--------------------------------------------------------------------------------

[Verified Entry Point]
  # {method} {0x00007f81d40fe388} 'm' '()V' in 'T'
  #           [sp+0x50]  (sp of caller)
  0x00007f81d4d38920:   mov    %eax,-0x14000(%rsp)
  0x00007f81d4d38927:   push   %rbp
  0x00007f81d4d38928:   sub    $0x40,%rsp
  0x00007f81d4d3892c:   movabs $0xe1692578,%rsi             ;   {oop(a 'java/lang/Class'{0x00000000e1692578} = 'T')}
  0x00007f81d4d38936:   lea    0x20(%rsp),%rdi
  0x00007f81d4d3893b:   mov    %rsi,0x8(%rdi)
  0x00007f81d4d3893f:   mov    (%rsi),%rax
  0x00007f81d4d38942:   mov    %rax,%rbx
  0x00007f81d4d38945:   and    $0x7,%rbx
  0x00007f81d4d38949:   cmp    $0x5,%rbx
  0x00007f81d4d3894d:   jne    0x00007f81d4d389d4
  0x00007f81d4d38953:   mov    0x8(%rsi),%ebx
  0x00007f81d4d38956:   shl    $0x3,%rbx
  0x00007f81d4d3895a:   mov    0xb8(%rbx),%rbx
  0x00007f81d4d38961:   or     %r15,%rbx
  0x00007f81d4d38964:   xor    %rax,%rbx
  0x00007f81d4d38967:   and    $0xffffffffffffff87,%rbx
  0x00007f81d4d3896b:   je     0x00007f81d4d389fc
  0x00007f81d4d38971:   test   $0x7,%rbx
  0x00007f81d4d38978:   jne    0x00007f81d4d389c1
  0x00007f81d4d3897a:   test   $0x300,%rbx
  0x00007f81d4d38981:   jne    0x00007f81d4d389a0
  0x00007f81d4d38983:   and    $0x37f,%rax
  0x00007f81d4d3898a:   mov    %rax,%rbx
  0x00007f81d4d3898d:   or     %r15,%rbx
  0x00007f81d4d38990:   lock cmpxchg %rbx,(%rsi)
  0x00007f81d4d38995:   jne    0x00007f81d4d38a5f
  0x00007f81d4d3899b:   jmpq   0x00007f81d4d389fc
  0x00007f81d4d389a0:   mov    0x8(%rsi),%ebx
  0x00007f81d4d389a3:   shl    $0x3,%rbx
  0x00007f81d4d389a7:   mov    0xb8(%rbx),%rbx
  0x00007f81d4d389ae:   or     %r15,%rbx
  0x00007f81d4d389b1:   lock cmpxchg %rbx,(%rsi)
  0x00007f81d4d389b6:   jne    0x00007f81d4d38a5f
  0x00007f81d4d389bc:   jmpq   0x00007f81d4d389fc
  0x00007f81d4d389c1:   mov    0x8(%rsi),%ebx
  0x00007f81d4d389c4:   shl    $0x3,%rbx
  0x00007f81d4d389c8:   mov    0xb8(%rbx),%rbx
  0x00007f81d4d389cf:   lock cmpxchg %rbx,(%rsi)
  0x00007f81d4d389d4:   mov    (%rsi),%rax
  0x00007f81d4d389d7:   or     $0x1,%rax
  0x00007f81d4d389db:   mov    %rax,(%rdi)
  0x00007f81d4d389de:   lock cmpxchg %rdi,(%rsi)
  0x00007f81d4d389e3:   je     0x00007f81d4d389fc
  0x00007f81d4d389e9:   sub    %rsp,%rax
  0x00007f81d4d389ec:   and    $0xfffffffffffff007,%rax
  0x00007f81d4d389f3:   mov    %rax,(%rdi)
  0x00007f81d4d389f6:   jne    0x00007f81d4d38a5f
  0x00007f81d4d389fc:   movabs $0x7f81d40fe660,%rax
  0x00007f81d4d38a06:   mov    0x18(%rax),%esi
  0x00007f81d4d38a09:   add    $0x8,%esi
  0x00007f81d4d38a0c:   mov    %esi,0x18(%rax)
  0x00007f81d4d38a0f:   and    $0x3ff8,%esi
  0x00007f81d4d38a15:   cmp    $0x0,%esi
  0x00007f81d4d38a18:   je     0x00007f81d4d38a6f
  0x00007f81d4d38a1e:   lea    0x20(%rsp),%rax
  0x00007f81d4d38a23:   mov    0x8(%rax),%rdi
  0x00007f81d4d38a27:   mov    (%rdi),%rsi
  0x00007f81d4d38a2a:   and    $0x7,%rsi
  0x00007f81d4d38a2e:   cmp    $0x5,%rsi
  0x00007f81d4d38a32:   je     0x00007f81d4d38a4f
  0x00007f81d4d38a38:   mov    (%rax),%rsi
  0x00007f81d4d38a3b:   test   %rsi,%rsi
  0x00007f81d4d38a3e:   je     0x00007f81d4d38a4f
  0x00007f81d4d38a44:   lock cmpxchg %rsi,(%rdi)
  0x00007f81d4d38a49:   jne    0x00007f81d4d38a8d           ;*return {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - T::m@0 (line 14)
  0x00007f81d4d38a4f:   add    $0x40,%rsp
  0x00007f81d4d38a53:   pop    %rbp
  0x00007f81d4d38a54:   mov    0x108(%r15),%r10
  0x00007f81d4d38a5b:   test   %eax,(%r10)                  ;   {poll_return}
  0x00007f81d4d38a5e:   retq   
  0x00007f81d4d38a5f:   mov    %rsi,0x8(%rsp)
  0x00007f81d4d38a64:   mov    %rdi,(%rsp)
  0x00007f81d4d38a68:   callq  0x00007f81d489bf00           ; ImmutableOopMap {rsi=Oop [40]=Oop }
                                                            ;*synchronization entry
                                                            ; - T::m@-1 (line 14)
                                                            ;   {runtime_call monitorenter_nofpu Runtime1 stub}
  0x00007f81d4d38a6d:   jmp    0x00007f81d4d389fc
  0x00007f81d4d38a6f:   movabs $0x7f81d40fe388,%r10         ;   {metadata({method} {0x00007f81d40fe388} 'm' '()V' in 'T')}
  0x00007f81d4d38a79:   mov    %r10,0x8(%rsp)
  0x00007f81d4d38a7e:   movq   $0xffffffffffffffff,(%rsp)
  0x00007f81d4d38a86:   callq  0x00007f81d489e000           ; ImmutableOopMap {[40]=Oop }
                                                            ;*synchronization entry
                                                            ; - T::m@-1 (line 14)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d38a8b:   jmp    0x00007f81d4d38a1e
  0x00007f81d4d38a8d:   lea    0x20(%rsp),%rax
  0x00007f81d4d38a92:   mov    %rax,(%rsp)
  0x00007f81d4d38a96:   callq  0x00007f81d489c500           ;   {runtime_call monitorexit_nofpu Runtime1 stub}
  0x00007f81d4d38a9b:   jmp    0x00007f81d4d38a4f
  0x00007f81d4d38a9d:   nop
  0x00007f81d4d38a9e:   nop
  0x00007f81d4d38a9f:   mov    0x3f0(%r15),%rax
  0x00007f81d4d38aa6:   movabs $0x0,%r10
  0x00007f81d4d38ab0:   mov    %r10,0x3f0(%r15)
  0x00007f81d4d38ab7:   movabs $0x0,%r10
  0x00007f81d4d38ac1:   mov    %r10,0x3f8(%r15)
  0x00007f81d4d38ac8:   mov    %rax,%rbx
  0x00007f81d4d38acb:   lea    0x20(%rsp),%rax
  0x00007f81d4d38ad0:   mov    0x8(%rax),%rsi
  0x00007f81d4d38ad4:   mov    (%rsi),%rdi
  0x00007f81d4d38ad7:   and    $0x7,%rdi
  0x00007f81d4d38adb:   cmp    $0x5,%rdi
  0x00007f81d4d38adf:   je     0x00007f81d4d38afc
  0x00007f81d4d38ae5:   mov    (%rax),%rdi
  0x00007f81d4d38ae8:   test   %rdi,%rdi
  0x00007f81d4d38aeb:   je     0x00007f81d4d38afc
  0x00007f81d4d38af1:   lock cmpxchg %rdi,(%rsi)
  0x00007f81d4d38af6:   jne    0x00007f81d4d38b09
  0x00007f81d4d38afc:   mov    %rbx,%rax
  0x00007f81d4d38aff:   add    $0x40,%rsp
  0x00007f81d4d38b03:   pop    %rbp
  0x00007f81d4d38b04:   jmpq   0x00007f81d480be80           ;   {runtime_call unwind_exception Runtime1 stub}
  0x00007f81d4d38b09:   lea    0x20(%rsp),%rax
  0x00007f81d4d38b0e:   mov    %rax,(%rsp)
  0x00007f81d4d38b12:   callq  0x00007f81d489c500           ;   {runtime_call monitorexit_nofpu Runtime1 stub}
  0x00007f81d4d38b17:   jmp    0x00007f81d4d38afc
  0x00007f81d4d38b19:   hlt    
  0x00007f81d4d38b1a:   hlt    
  0x00007f81d4d38b1b:   hlt    
  0x00007f81d4d38b1c:   hlt    
  0x00007f81d4d38b1d:   hlt    
  0x00007f81d4d38b1e:   hlt    
  0x00007f81d4d38b1f:   hlt    
[Exception Handler]
  0x00007f81d4d38b20:   callq  0x00007f81d489ad00           ;   {no_reloc}
  0x00007f81d4d38b25:   mov    %rsp,-0x28(%rsp)
  0x00007f81d4d38b2a:   sub    $0x80,%rsp
  0x00007f81d4d38b31:   mov    %rax,0x78(%rsp)
  0x00007f81d4d38b36:   mov    %rcx,0x70(%rsp)
  0x00007f81d4d38b3b:   mov    %rdx,0x68(%rsp)
  0x00007f81d4d38b40:   mov    %rbx,0x60(%rsp)
  0x00007f81d4d38b45:   mov    %rbp,0x50(%rsp)
  0x00007f81d4d38b4a:   mov    %rsi,0x48(%rsp)
  0x00007f81d4d38b4f:   mov    %rdi,0x40(%rsp)
  0x00007f81d4d38b54:   mov    %r8,0x38(%rsp)
  0x00007f81d4d38b59:   mov    %r9,0x30(%rsp)
  0x00007f81d4d38b5e:   mov    %r10,0x28(%rsp)
  0x00007f81d4d38b63:   mov    %r11,0x20(%rsp)
  0x00007f81d4d38b68:   mov    %r12,0x18(%rsp)
  0x00007f81d4d38b6d:   mov    %r13,0x10(%rsp)
  0x00007f81d4d38b72:   mov    %r14,0x8(%rsp)
  0x00007f81d4d38b77:   mov    %r15,(%rsp)
  0x00007f81d4d38b7b:   movabs $0x7f81f15ff3e2,%rdi         ;   {external_word}
  0x00007f81d4d38b85:   movabs $0x7f81d4d38b25,%rsi         ;   {internal_word}
  0x00007f81d4d38b8f:   mov    %rsp,%rdx
  0x00007f81d4d38b92:   and    $0xfffffffffffffff0,%rsp
  0x00007f81d4d38b96:   callq  0x00007f81f1108240           ;   {runtime_call}
  0x00007f81d4d38b9b:   hlt    
[Deopt Handler Code]
  0x00007f81d4d38b9c:   movabs $0x7f81d4d38b9c,%r10         ;   {section_word}
  0x00007f81d4d38ba6:   push   %r10
  0x00007f81d4d38ba8:   jmpq   0x00007f81d47ed0a0           ;   {runtime_call DeoptimizationBlob}
  0x00007f81d4d38bad:   hlt    
  0x00007f81d4d38bae:   hlt    
  0x00007f81d4d38baf:   hlt    
--------------------------------------------------------------------------------

============================= C1-compiled nmethod ==============================
----------------------------------- Assembly -----------------------------------

Compiled method (c1)     194   39       2       T::n (5 bytes)
 total in heap  [0x00007f81d4d38c90,0x00007f81d4d38ff0] = 864
 relocation     [0x00007f81d4d38df0,0x00007f81d4d38e18] = 40
 main code      [0x00007f81d4d38e20,0x00007f81d4d38ee0] = 192
 stub code      [0x00007f81d4d38ee0,0x00007f81d4d38f70] = 144
 oops           [0x00007f81d4d38f70,0x00007f81d4d38f78] = 8
 metadata       [0x00007f81d4d38f78,0x00007f81d4d38f80] = 8
 scopes data    [0x00007f81d4d38f80,0x00007f81d4d38f98] = 24
 scopes pcs     [0x00007f81d4d38f98,0x00007f81d4d38fe8] = 80
 dependencies   [0x00007f81d4d38fe8,0x00007f81d4d38ff0] = 8

--------------------------------------------------------------------------------
[Constant Pool (empty)]

--------------------------------------------------------------------------------

[Verified Entry Point]
  # {method} {0x00007f81d40fe428} 'n' '()V' in 'T'
  #           [sp+0x40]  (sp of caller)
  0x00007f81d4d38e20:   mov    %eax,-0x14000(%rsp)
  0x00007f81d4d38e27:   push   %rbp
  0x00007f81d4d38e28:   sub    $0x30,%rsp
  0x00007f81d4d38e2c:   movabs $0x7f81d40fe6b0,%rsi
  0x00007f81d4d38e36:   mov    0x18(%rsi),%edi
  0x00007f81d4d38e39:   add    $0x8,%edi
  0x00007f81d4d38e3c:   mov    %edi,0x18(%rsi)
  0x00007f81d4d38e3f:   and    $0x3ff8,%edi
  0x00007f81d4d38e45:   cmp    $0x0,%edi
  0x00007f81d4d38e48:   je     0x00007f81d4d38e76           ;*iconst_1 {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - T::n@0 (line 17)
  0x00007f81d4d38e4e:   movabs $0xe1692578,%rsi             ;   {oop(a 'java/lang/Class'{0x00000000e1692578} = 'T')}
  0x00007f81d4d38e58:   mov    $0x1,%edi
  0x00007f81d4d38e5d:   mov    %edi,0x70(%rsi)
  0x00007f81d4d38e60:   lock addl $0x0,-0x40(%rsp)          ;*putstatic i {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - T::n@1 (line 17)
  0x00007f81d4d38e66:   add    $0x30,%rsp
  0x00007f81d4d38e6a:   pop    %rbp
  0x00007f81d4d38e6b:   mov    0x108(%r15),%r10
  0x00007f81d4d38e72:   test   %eax,(%r10)                  ;   {poll_return}
  0x00007f81d4d38e75:   retq   
  0x00007f81d4d38e76:   movabs $0x7f81d40fe428,%r10         ;   {metadata({method} {0x00007f81d40fe428} 'n' '()V' in 'T')}
  0x00007f81d4d38e80:   mov    %r10,0x8(%rsp)
  0x00007f81d4d38e85:   movq   $0xffffffffffffffff,(%rsp)
  0x00007f81d4d38e8d:   callq  0x00007f81d489e000           ; ImmutableOopMap {}
                                                            ;*synchronization entry
                                                            ; - T::n@-1 (line 17)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d38e92:   jmp    0x00007f81d4d38e4e
  0x00007f81d4d38e94:   nop
  0x00007f81d4d38e95:   nop
  0x00007f81d4d38e96:   mov    0x3f0(%r15),%rax
  0x00007f81d4d38e9d:   movabs $0x0,%r10
  0x00007f81d4d38ea7:   mov    %r10,0x3f0(%r15)
  0x00007f81d4d38eae:   movabs $0x0,%r10
  0x00007f81d4d38eb8:   mov    %r10,0x3f8(%r15)
  0x00007f81d4d38ebf:   add    $0x30,%rsp
  0x00007f81d4d38ec3:   pop    %rbp
  0x00007f81d4d38ec4:   jmpq   0x00007f81d480be80           ;   {runtime_call unwind_exception Runtime1 stub}
  0x00007f81d4d38ec9:   hlt    
  0x00007f81d4d38eca:   hlt    
  0x00007f81d4d38ecb:   hlt    
  0x00007f81d4d38ecc:   hlt    
  0x00007f81d4d38ecd:   hlt    
  0x00007f81d4d38ece:   hlt    
  0x00007f81d4d38ecf:   hlt    
  0x00007f81d4d38ed0:   hlt    
  0x00007f81d4d38ed1:   hlt    
  0x00007f81d4d38ed2:   hlt    
  0x00007f81d4d38ed3:   hlt    
  0x00007f81d4d38ed4:   hlt    
  0x00007f81d4d38ed5:   hlt    
  0x00007f81d4d38ed6:   hlt    
  0x00007f81d4d38ed7:   hlt    
  0x00007f81d4d38ed8:   hlt    
  0x00007f81d4d38ed9:   hlt    
  0x00007f81d4d38eda:   hlt    
  0x00007f81d4d38edb:   hlt    
  0x00007f81d4d38edc:   hlt    
  0x00007f81d4d38edd:   hlt    
  0x00007f81d4d38ede:   hlt    
  0x00007f81d4d38edf:   hlt    
[Exception Handler]
  0x00007f81d4d38ee0:   callq  0x00007f81d489ad00           ;   {no_reloc}
  0x00007f81d4d38ee5:   mov    %rsp,-0x28(%rsp)
  0x00007f81d4d38eea:   sub    $0x80,%rsp
  0x00007f81d4d38ef1:   mov    %rax,0x78(%rsp)
  0x00007f81d4d38ef6:   mov    %rcx,0x70(%rsp)
  0x00007f81d4d38efb:   mov    %rdx,0x68(%rsp)
  0x00007f81d4d38f00:   mov    %rbx,0x60(%rsp)
  0x00007f81d4d38f05:   mov    %rbp,0x50(%rsp)
  0x00007f81d4d38f0a:   mov    %rsi,0x48(%rsp)
  0x00007f81d4d38f0f:   mov    %rdi,0x40(%rsp)
  0x00007f81d4d38f14:   mov    %r8,0x38(%rsp)
  0x00007f81d4d38f19:   mov    %r9,0x30(%rsp)
  0x00007f81d4d38f1e:   mov    %r10,0x28(%rsp)
  0x00007f81d4d38f23:   mov    %r11,0x20(%rsp)
  0x00007f81d4d38f28:   mov    %r12,0x18(%rsp)
  0x00007f81d4d38f2d:   mov    %r13,0x10(%rsp)
  0x00007f81d4d38f32:   mov    %r14,0x8(%rsp)
  0x00007f81d4d38f37:   mov    %r15,(%rsp)
  0x00007f81d4d38f3b:   movabs $0x7f81f15ff3e2,%rdi         ;   {external_word}
  0x00007f81d4d38f45:   movabs $0x7f81d4d38ee5,%rsi         ;   {internal_word}
  0x00007f81d4d38f4f:   mov    %rsp,%rdx
  0x00007f81d4d38f52:   and    $0xfffffffffffffff0,%rsp
  0x00007f81d4d38f56:   callq  0x00007f81f1108240           ;   {runtime_call}
  0x00007f81d4d38f5b:   hlt    
[Deopt Handler Code]
  0x00007f81d4d38f5c:   movabs $0x7f81d4d38f5c,%r10         ;   {section_word}
  0x00007f81d4d38f66:   push   %r10
  0x00007f81d4d38f68:   jmpq   0x00007f81d47ed0a0           ;   {runtime_call DeoptimizationBlob}
  0x00007f81d4d38f6d:   hlt    
  0x00007f81d4d38f6e:   hlt    
  0x00007f81d4d38f6f:   hlt    
--------------------------------------------------------------------------------

============================= C2-compiled nmethod ==============================
----------------------------------- Assembly -----------------------------------

Compiled method (c2)     200   40  s    4       T::m (1 bytes)
 total in heap  [0x00007f81dc26b710,0x00007f81dc26bb58] = 1096
 relocation     [0x00007f81dc26b870,0x00007f81dc26b890] = 32
 main code      [0x00007f81dc26b8a0,0x00007f81dc26baa0] = 512
 stub code      [0x00007f81dc26baa0,0x00007f81dc26bab8] = 24
 oops           [0x00007f81dc26bab8,0x00007f81dc26bac8] = 16
 metadata       [0x00007f81dc26bac8,0x00007f81dc26bad8] = 16
 scopes data    [0x00007f81dc26bad8,0x00007f81dc26baf0] = 24
 scopes pcs     [0x00007f81dc26baf0,0x00007f81dc26bb50] = 96
 dependencies   [0x00007f81dc26bb50,0x00007f81dc26bb58] = 8

--------------------------------------------------------------------------------
[Constant Pool (empty)]

--------------------------------------------------------------------------------

[Verified Entry Point]
  # {method} {0x00007f81d40fe388} 'm' '()V' in 'T'
  #           [sp+0x20]  (sp of caller)
  0x00007f81dc26b8a0:   mov    %eax,-0x14000(%rsp)
  0x00007f81dc26b8a7:   push   %rbp
  0x00007f81dc26b8a8:   sub    $0x10,%rsp
  0x00007f81dc26b8ac:   movabs $0xe1692578,%r10             ;   {oop(a 'java/lang/Class'{0x00000000e1692578} = 'T')}
  0x00007f81dc26b8b6:   mov    (%r10),%rax
  0x00007f81dc26b8b9:   mov    %rax,%r10
  0x00007f81dc26b8bc:   and    $0x7,%r10
  0x00007f81dc26b8c0:   cmp    $0x5,%r10
  0x00007f81dc26b8c4:   jne    0x00007f81dc26b936
  0x00007f81dc26b8c6:   mov    $0x20000528,%r11d            ;   {metadata('java/lang/Class')}
  0x00007f81dc26b8cc:   movabs $0x0,%r10
  0x00007f81dc26b8d6:   lea    (%r10,%r11,8),%r10
  0x00007f81dc26b8da:   mov    0xb8(%r10),%r10
  0x00007f81dc26b8e1:   mov    %r10,%r11
  0x00007f81dc26b8e4:   or     %r15,%r11
  0x00007f81dc26b8e7:   mov    %r11,%r8
  0x00007f81dc26b8ea:   xor    %rax,%r8
  0x00007f81dc26b8ed:   test   $0xffffffffffffff87,%r8
  0x00007f81dc26b8f4:   jne    0x00007f81dc26ba5d
  0x00007f81dc26b8fa:   mov    $0x7,%r10d
  0x00007f81dc26b900:   movabs $0xe1692578,%r11             ;   {oop(a 'java/lang/Class'{0x00000000e1692578} = 'T')}
  0x00007f81dc26b90a:   and    (%r11),%r10
  0x00007f81dc26b90d:   cmp    $0x5,%r10
  0x00007f81dc26b911:   jne    0x00007f81dc26b9a9           ;*return {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - T::m@0 (line 14)
  0x00007f81dc26b917:   add    $0x10,%rsp
  0x00007f81dc26b91b:   pop    %rbp
  0x00007f81dc26b91c:   mov    0x108(%r15),%r10
  0x00007f81dc26b923:   test   %eax,(%r10)                  ;   {poll_return}
  0x00007f81dc26b926:   retq   
  0x00007f81dc26b927:   movabs $0xe1692578,%r11             ;   {oop(a 'java/lang/Class'{0x00000000e1692578} = 'T')}
  0x00007f81dc26b931:   lock cmpxchg %r10,(%r11)
  0x00007f81dc26b936:   movabs $0xe1692578,%r11             ;   {oop(a 'java/lang/Class'{0x00000000e1692578} = 'T')}
  0x00007f81dc26b940:   lea    0x0(%rsp),%rbx
  0x00007f81dc26b945:   mov    (%r11),%rax
  0x00007f81dc26b948:   test   $0x2,%rax
  0x00007f81dc26b94e:   jne    0x00007f81dc26b974
  0x00007f81dc26b950:   or     $0x1,%rax
  0x00007f81dc26b954:   mov    %rax,(%rbx)
  0x00007f81dc26b957:   lock cmpxchg %rbx,(%r11)
  0x00007f81dc26b95c:   je     0x00007f81dc26b987
  0x00007f81dc26b962:   sub    %rsp,%rax
  0x00007f81dc26b965:   and    $0xfffffffffffff007,%rax
  0x00007f81dc26b96c:   mov    %rax,(%rbx)
  0x00007f81dc26b96f:   jmpq   0x00007f81dc26b987
  0x00007f81dc26b974:   mov    %rax,%r10
  0x00007f81dc26b977:   xor    %rax,%rax
  0x00007f81dc26b97a:   lock cmpxchg %r15,0x7e(%r10)
  0x00007f81dc26b980:   movq   $0x3,(%rbx)
  0x00007f81dc26b987:   je     0x00007f81dc26b8fa
  0x00007f81dc26b98d:   movabs $0xe1692578,%rsi             ;   {oop(a 'java/lang/Class'{0x00000000e1692578} = 'T')}
  0x00007f81dc26b997:   lea    0x0(%rsp),%rdx
  0x00007f81dc26b99c:   data16 xchg %ax,%ax
  0x00007f81dc26b99f:   callq  0x00007f81d480d380           ; ImmutableOopMap {}
                                                            ;*synchronization entry
                                                            ; - T::m@-1 (line 14)
                                                            ;   {runtime_call _complete_monitor_locking_Java}
  0x00007f81dc26b9a4:   jmpq   0x00007f81dc26b8fa
  0x00007f81dc26b9a9:   lea    0x0(%rsp),%rax
  0x00007f81dc26b9ae:   cmpq   $0x0,(%rax)
  0x00007f81dc26b9b5:   je     0x00007f81dc26ba33
  0x00007f81dc26b9bb:   mov    (%r11),%r10
  0x00007f81dc26b9be:   test   $0x2,%r10
  0x00007f81dc26b9c5:   je     0x00007f81dc26ba2b
  0x00007f81dc26b9c7:   xor    %rax,%rax
  0x00007f81dc26b9ca:   or     0x8e(%r10),%rax
  0x00007f81dc26b9d1:   jne    0x00007f81dc26ba33
  0x00007f81dc26b9d3:   mov    0x9e(%r10),%rax
  0x00007f81dc26b9da:   or     0x96(%r10),%rax
  0x00007f81dc26b9e1:   jne    0x00007f81dc26b9ed
  0x00007f81dc26b9e3:   movq   $0x0,0x7e(%r10)
  0x00007f81dc26b9eb:   jmp    0x00007f81dc26ba33
  0x00007f81dc26b9ed:   cmpq   $0x0,0xa6(%r10)
  0x00007f81dc26b9f8:   je     0x00007f81dc26ba1f
  0x00007f81dc26b9fa:   xor    %rax,%rax
  0x00007f81dc26b9fd:   movq   $0x0,0x7e(%r10)
  0x00007f81dc26ba05:   lock addl $0x0,(%rsp)
  0x00007f81dc26ba0a:   cmpq   $0x0,0xa6(%r10)
  0x00007f81dc26ba15:   jne    0x00007f81dc26ba24
  0x00007f81dc26ba17:   lock cmpxchg %r15,0x7e(%r10)
  0x00007f81dc26ba1d:   jne    0x00007f81dc26ba24
  0x00007f81dc26ba1f:   or     $0x1,%eax
  0x00007f81dc26ba22:   jmp    0x00007f81dc26ba33
  0x00007f81dc26ba24:   test   $0x0,%eax
  0x00007f81dc26ba29:   jmp    0x00007f81dc26ba33
  0x00007f81dc26ba2b:   mov    (%rax),%r10
  0x00007f81dc26ba2e:   lock cmpxchg %r10,(%r11)
  0x00007f81dc26ba33:   je     0x00007f81dc26b917
  0x00007f81dc26ba39:   movabs $0xe1692578,%rdi             ;   {oop(a 'java/lang/Class'{0x00000000e1692578} = 'T')}
  0x00007f81dc26ba43:   lea    0x0(%rsp),%rsi               ;*synchronization entry
                                                            ; - T::m@-1 (line 14)
  0x00007f81dc26ba48:   mov    %r15,%rdx
  0x00007f81dc26ba4b:   movabs $0x7f81f12ea710,%r10
  0x00007f81dc26ba55:   callq  *%r10                        ;*return {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - T::m@0 (line 14)
  0x00007f81dc26ba58:   jmpq   0x00007f81dc26b917
  0x00007f81dc26ba5d:   test   $0x7,%r8
  0x00007f81dc26ba64:   jne    0x00007f81dc26b927
  0x00007f81dc26ba6a:   test   $0x300,%r8
  0x00007f81dc26ba71:   jne    0x00007f81dc26ba80
  0x00007f81dc26ba73:   and    $0x37f,%rax
  0x00007f81dc26ba7a:   mov    %rax,%r11
  0x00007f81dc26ba7d:   or     %r15,%r11
  0x00007f81dc26ba80:   movabs $0xe1692578,%r10             ;   {oop(a 'java/lang/Class'{0x00000000e1692578} = 'T')}
  0x00007f81dc26ba8a:   lock cmpxchg %r11,(%r10)
  0x00007f81dc26ba8f:   jne    0x00007f81dc26b98d
  0x00007f81dc26ba95:   jmpq   0x00007f81dc26b8fa
  0x00007f81dc26ba9a:   hlt    
  0x00007f81dc26ba9b:   hlt    
  0x00007f81dc26ba9c:   hlt    
  0x00007f81dc26ba9d:   hlt    
  0x00007f81dc26ba9e:   hlt    
  0x00007f81dc26ba9f:   hlt    
[Exception Handler]
  0x00007f81dc26baa0:   jmpq   0x00007f81d4809300           ;   {no_reloc}
[Deopt Handler Code]
  0x00007f81dc26baa5:   callq  0x00007f81dc26baaa
  0x00007f81dc26baaa:   subq   $0x5,(%rsp)
  0x00007f81dc26baaf:   jmpq   0x00007f81d47ed0a0           ;   {runtime_call DeoptimizationBlob}
  0x00007f81dc26bab4:   hlt    
  0x00007f81dc26bab5:   hlt    
  0x00007f81dc26bab6:   hlt    
  0x00007f81dc26bab7:   hlt    
--------------------------------------------------------------------------------

============================= C1-compiled nmethod ==============================
----------------------------------- Assembly -----------------------------------

Compiled method (c1)     202    4       3       java.lang.Math::floorMod (20 bytes)
 total in heap  [0x00007f81d4d39010,0x00007f81d4d39448] = 1080
 relocation     [0x00007f81d4d39170,0x00007f81d4d391a0] = 48
 main code      [0x00007f81d4d391a0,0x00007f81d4d392e0] = 320
 stub code      [0x00007f81d4d392e0,0x00007f81d4d39370] = 144
 metadata       [0x00007f81d4d39370,0x00007f81d4d39378] = 8
 scopes data    [0x00007f81d4d39378,0x00007f81d4d393b0] = 56
 scopes pcs     [0x00007f81d4d393b0,0x00007f81d4d39430] = 128
 dependencies   [0x00007f81d4d39430,0x00007f81d4d39438] = 8
 nul chk table  [0x00007f81d4d39438,0x00007f81d4d39448] = 16

--------------------------------------------------------------------------------
[Constant Pool (empty)]

--------------------------------------------------------------------------------

[Verified Entry Point]
  # {method} {0x00007f81d3e376e8} 'floorMod' '(II)I' in 'java/lang/Math'
  # parm0:    rsi       = int
  # parm1:    rdx       = int
  #           [sp+0x40]  (sp of caller)
  0x00007f81d4d391a0:   mov    %eax,-0x14000(%rsp)
  0x00007f81d4d391a7:   push   %rbp
  0x00007f81d4d391a8:   sub    $0x30,%rsp
  0x00007f81d4d391ac:   mov    %rdx,%rdi
  0x00007f81d4d391af:   movabs $0x7f81d3f439a0,%rax         ;   {metadata(method data for {method} {0x00007f81d3e376e8} 'floorMod' '(II)I' in 'java/lang/Math')}
  0x00007f81d4d391b9:   mov    0x13c(%rax),%edx
  0x00007f81d4d391bf:   add    $0x8,%edx
  0x00007f81d4d391c2:   mov    %edx,0x13c(%rax)
  0x00007f81d4d391c8:   and    $0x1ff8,%edx
  0x00007f81d4d391ce:   cmp    $0x0,%edx
  0x00007f81d4d391d1:   je     0x00007f81d4d39281           ;*iload_0 {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.Math::floorMod@0 (line 1277)
  0x00007f81d4d391d7:   mov    %rsi,%rax
  0x00007f81d4d391da:   cmp    $0x80000000,%eax
  0x00007f81d4d391e0:   jne    0x00007f81d4d391f1
  0x00007f81d4d391e6:   xor    %edx,%edx
  0x00007f81d4d391e8:   cmp    $0xffffffff,%edi
  0x00007f81d4d391eb:   je     0x00007f81d4d391f4
  0x00007f81d4d391f1:   cltd   
  0x00007f81d4d391f2:   idiv   %edi                         ; implicit exception: dispatches to 0x00007f81d4d392a2
                                                            ;*irem {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.Math::floorMod@2 (line 1277)
  0x00007f81d4d391f4:   mov    %rdx,%rax
  0x00007f81d4d391f7:   xor    %rdi,%rax
  0x00007f81d4d391fa:   cmp    $0x0,%eax
  0x00007f81d4d391fd:   movabs $0x7f81d3f439a0,%rax         ;   {metadata(method data for {method} {0x00007f81d3e376e8} 'floorMod' '(II)I' in 'java/lang/Math')}
  0x00007f81d4d39207:   movabs $0x180,%rsi
  0x00007f81d4d39211:   jge    0x00007f81d4d39221
  0x00007f81d4d39217:   movabs $0x190,%rsi
  0x00007f81d4d39221:   mov    (%rax,%rsi,1),%rbx
  0x00007f81d4d39225:   lea    0x1(%rbx),%rbx
  0x00007f81d4d39229:   mov    %rbx,(%rax,%rsi,1)
  0x00007f81d4d3922d:   jge    0x00007f81d4d3926e           ;*ifge {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.Math::floorMod@7 (line 1279)
  0x00007f81d4d39233:   cmp    $0x0,%edx
  0x00007f81d4d39236:   movabs $0x7f81d3f439a0,%rax         ;   {metadata(method data for {method} {0x00007f81d3e376e8} 'floorMod' '(II)I' in 'java/lang/Math')}
  0x00007f81d4d39240:   movabs $0x1a0,%rsi
  0x00007f81d4d3924a:   je     0x00007f81d4d3925a
  0x00007f81d4d39250:   movabs $0x1b0,%rsi
  0x00007f81d4d3925a:   mov    (%rax,%rsi,1),%rbx
  0x00007f81d4d3925e:   lea    0x1(%rbx),%rbx
  0x00007f81d4d39262:   mov    %rbx,(%rax,%rsi,1)
  0x00007f81d4d39266:   je     0x00007f81d4d3926e           ;*ifeq {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.Math::floorMod@11 (line 1279)
  0x00007f81d4d3926c:   add    %edi,%edx
  0x00007f81d4d3926e:   mov    %rdx,%rax
  0x00007f81d4d39271:   add    $0x30,%rsp
  0x00007f81d4d39275:   pop    %rbp
  0x00007f81d4d39276:   mov    0x108(%r15),%r10
  0x00007f81d4d3927d:   test   %eax,(%r10)                  ;   {poll_return}
  0x00007f81d4d39280:   retq   
  0x00007f81d4d39281:   movabs $0x7f81d3e376e8,%r10         ;   {metadata({method} {0x00007f81d3e376e8} 'floorMod' '(II)I' in 'java/lang/Math')}
  0x00007f81d4d3928b:   mov    %r10,0x8(%rsp)
  0x00007f81d4d39290:   movq   $0xffffffffffffffff,(%rsp)
  0x00007f81d4d39298:   callq  0x00007f81d489e000           ; ImmutableOopMap {}
                                                            ;*synchronization entry
                                                            ; - java.lang.Math::floorMod@-1 (line 1277)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d3929d:   jmpq   0x00007f81d4d391d7
  0x00007f81d4d392a2:   callq  0x00007f81d480b2a0           ; ImmutableOopMap {}
                                                            ;*irem {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.Math::floorMod@2 (line 1277)
                                                            ;   {runtime_call throw_div0_exception Runtime1 stub}
  0x00007f81d4d392a7:   nop
  0x00007f81d4d392a8:   nop
  0x00007f81d4d392a9:   mov    0x3f0(%r15),%rax
  0x00007f81d4d392b0:   movabs $0x0,%r10
  0x00007f81d4d392ba:   mov    %r10,0x3f0(%r15)
  0x00007f81d4d392c1:   movabs $0x0,%r10
  0x00007f81d4d392cb:   mov    %r10,0x3f8(%r15)
  0x00007f81d4d392d2:   add    $0x30,%rsp
  0x00007f81d4d392d6:   pop    %rbp
  0x00007f81d4d392d7:   jmpq   0x00007f81d480be80           ;   {runtime_call unwind_exception Runtime1 stub}
  0x00007f81d4d392dc:   hlt    
  0x00007f81d4d392dd:   hlt    
  0x00007f81d4d392de:   hlt    
  0x00007f81d4d392df:   hlt    
[Exception Handler]
  0x00007f81d4d392e0:   callq  0x00007f81d489ad00           ;   {no_reloc}
  0x00007f81d4d392e5:   mov    %rsp,-0x28(%rsp)
  0x00007f81d4d392ea:   sub    $0x80,%rsp
  0x00007f81d4d392f1:   mov    %rax,0x78(%rsp)
  0x00007f81d4d392f6:   mov    %rcx,0x70(%rsp)
  0x00007f81d4d392fb:   mov    %rdx,0x68(%rsp)
  0x00007f81d4d39300:   mov    %rbx,0x60(%rsp)
  0x00007f81d4d39305:   mov    %rbp,0x50(%rsp)
  0x00007f81d4d3930a:   mov    %rsi,0x48(%rsp)
  0x00007f81d4d3930f:   mov    %rdi,0x40(%rsp)
  0x00007f81d4d39314:   mov    %r8,0x38(%rsp)
  0x00007f81d4d39319:   mov    %r9,0x30(%rsp)
  0x00007f81d4d3931e:   mov    %r10,0x28(%rsp)
  0x00007f81d4d39323:   mov    %r11,0x20(%rsp)
  0x00007f81d4d39328:   mov    %r12,0x18(%rsp)
  0x00007f81d4d3932d:   mov    %r13,0x10(%rsp)
  0x00007f81d4d39332:   mov    %r14,0x8(%rsp)
  0x00007f81d4d39337:   mov    %r15,(%rsp)
  0x00007f81d4d3933b:   movabs $0x7f81f15ff3e2,%rdi         ;   {external_word}
  0x00007f81d4d39345:   movabs $0x7f81d4d392e5,%rsi         ;   {internal_word}
  0x00007f81d4d3934f:   mov    %rsp,%rdx
  0x00007f81d4d39352:   and    $0xfffffffffffffff0,%rsp
  0x00007f81d4d39356:   callq  0x00007f81f1108240           ;   {runtime_call}
  0x00007f81d4d3935b:   hlt    
[Deopt Handler Code]
  0x00007f81d4d3935c:   movabs $0x7f81d4d3935c,%r10         ;   {section_word}
  0x00007f81d4d39366:   push   %r10
  0x00007f81d4d39368:   jmpq   0x00007f81d47ed0a0           ;   {runtime_call DeoptimizationBlob}
  0x00007f81d4d3936d:   hlt    
  0x00007f81d4d3936e:   hlt    
  0x00007f81d4d3936f:   hlt    
--------------------------------------------------------------------------------

============================= C2-compiled nmethod ==============================
----------------------------------- Assembly -----------------------------------

Compiled method (c2)     213   41       4       T::n (5 bytes)
 total in heap  [0x00007f81dc26bb90,0x00007f81dc26bda8] = 536
 relocation     [0x00007f81dc26bcf0,0x00007f81dc26bd00] = 16
 main code      [0x00007f81dc26bd00,0x00007f81dc26bd40] = 64
 stub code      [0x00007f81dc26bd40,0x00007f81dc26bd58] = 24
 oops           [0x00007f81dc26bd58,0x00007f81dc26bd60] = 8
 metadata       [0x00007f81dc26bd60,0x00007f81dc26bd68] = 8
 scopes data    [0x00007f81dc26bd68,0x00007f81dc26bd70] = 8
 scopes pcs     [0x00007f81dc26bd70,0x00007f81dc26bda0] = 48
 dependencies   [0x00007f81dc26bda0,0x00007f81dc26bda8] = 8

--------------------------------------------------------------------------------
[Constant Pool (empty)]

--------------------------------------------------------------------------------

[Verified Entry Point]
  # {method} {0x00007f81d40fe428} 'n' '()V' in 'T'
  #           [sp+0x20]  (sp of caller)
  0x00007f81dc26bd00:   sub    $0x18,%rsp
  0x00007f81dc26bd07:   mov    %rbp,0x10(%rsp)
  0x00007f81dc26bd0c:   movabs $0xe1692578,%r10             ;   {oop(a 'java/lang/Class'{0x00000000e1692578} = 'T')}
  0x00007f81dc26bd16:   movl   $0x1,0x70(%r10)
  0x00007f81dc26bd1e:   lock addl $0x0,-0x40(%rsp)          ;*putstatic i {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - T::n@1 (line 17)
  0x00007f81dc26bd24:   add    $0x10,%rsp
  0x00007f81dc26bd28:   pop    %rbp
  0x00007f81dc26bd29:   mov    0x108(%r15),%r10
  0x00007f81dc26bd30:   test   %eax,(%r10)                  ;   {poll_return}
  0x00007f81dc26bd33:   retq   
  0x00007f81dc26bd34:   hlt    
  0x00007f81dc26bd35:   hlt    
  0x00007f81dc26bd36:   hlt    
  0x00007f81dc26bd37:   hlt    
  0x00007f81dc26bd38:   hlt    
  0x00007f81dc26bd39:   hlt    
  0x00007f81dc26bd3a:   hlt    
  0x00007f81dc26bd3b:   hlt    
  0x00007f81dc26bd3c:   hlt    
  0x00007f81dc26bd3d:   hlt    
  0x00007f81dc26bd3e:   hlt    
  0x00007f81dc26bd3f:   hlt    
[Exception Handler]
  0x00007f81dc26bd40:   jmpq   0x00007f81d4809300           ;   {no_reloc}
[Deopt Handler Code]
  0x00007f81dc26bd45:   callq  0x00007f81dc26bd4a
  0x00007f81dc26bd4a:   subq   $0x5,(%rsp)
  0x00007f81dc26bd4f:   jmpq   0x00007f81d47ed0a0           ;   {runtime_call DeoptimizationBlob}
  0x00007f81dc26bd54:   hlt    
  0x00007f81dc26bd55:   hlt    
  0x00007f81dc26bd56:   hlt    
  0x00007f81dc26bd57:   hlt    
--------------------------------------------------------------------------------

============================= C1-compiled nmethod ==============================
----------------------------------- Assembly -----------------------------------

Compiled method (c1)     214   26       3       java.util.HashMap::hash (20 bytes)
 total in heap  [0x00007f81d4d39490,0x00007f81d4d39988] = 1272
 relocation     [0x00007f81d4d395f0,0x00007f81d4d39630] = 64
 main code      [0x00007f81d4d39640,0x00007f81d4d39800] = 448
 stub code      [0x00007f81d4d39800,0x00007f81d4d398a8] = 168
 metadata       [0x00007f81d4d398a8,0x00007f81d4d398b0] = 8
 scopes data    [0x00007f81d4d398b0,0x00007f81d4d398e0] = 48
 scopes pcs     [0x00007f81d4d398e0,0x00007f81d4d39970] = 144
 dependencies   [0x00007f81d4d39970,0x00007f81d4d39978] = 8
 nul chk table  [0x00007f81d4d39978,0x00007f81d4d39988] = 16

--------------------------------------------------------------------------------
[Constant Pool (empty)]

--------------------------------------------------------------------------------

[Verified Entry Point]
  # {method} {0x00007f81d3e40380} 'hash' '(Ljava/lang/Object;)I' in 'java/util/HashMap'
  # parm0:    rsi:rsi   = 'java/lang/Object'
  #           [sp+0x40]  (sp of caller)
  0x00007f81d4d39640:   mov    %eax,-0x14000(%rsp)
  0x00007f81d4d39647:   push   %rbp
  0x00007f81d4d39648:   sub    $0x30,%rsp
  0x00007f81d4d3964c:   movabs $0x7f81d3f9d1c0,%rdi         ;   {metadata(method data for {method} {0x00007f81d3e40380} 'hash' '(Ljava/lang/Object;)I' in 'java/util/HashMap')}
  0x00007f81d4d39656:   mov    0x13c(%rdi),%ebx
  0x00007f81d4d3965c:   add    $0x8,%ebx
  0x00007f81d4d3965f:   mov    %ebx,0x13c(%rdi)
  0x00007f81d4d39665:   and    $0x1ff8,%ebx
  0x00007f81d4d3966b:   cmp    $0x0,%ebx
  0x00007f81d4d3966e:   je     0x00007f81d4d39788           ;*aload_0 {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::hash@0 (line 339)
  0x00007f81d4d39674:   cmp    $0x0,%rsi
  0x00007f81d4d39678:   movabs $0x7f81d3f9d1c0,%rdi         ;   {metadata(method data for {method} {0x00007f81d3e40380} 'hash' '(Ljava/lang/Object;)I' in 'java/util/HashMap')}
  0x00007f81d4d39682:   movabs $0x180,%rbx
  0x00007f81d4d3968c:   jne    0x00007f81d4d3969c
  0x00007f81d4d39692:   movabs $0x190,%rbx
  0x00007f81d4d3969c:   mov    (%rdi,%rbx,1),%rax
  0x00007f81d4d396a0:   lea    0x1(%rax),%rax
  0x00007f81d4d396a4:   mov    %rax,(%rdi,%rbx,1)
  0x00007f81d4d396a8:   jne    0x00007f81d4d396c8           ;*ifnonnull {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::hash@1 (line 339)
  0x00007f81d4d396ae:   movabs $0x7f81d3f9d1c0,%rsi         ;   {metadata(method data for {method} {0x00007f81d3e40380} 'hash' '(Ljava/lang/Object;)I' in 'java/util/HashMap')}
  0x00007f81d4d396b8:   incl   0x1a0(%rsi)
  0x00007f81d4d396be:   mov    $0x0,%esi
  0x00007f81d4d396c3:   jmpq   0x00007f81d4d39775           ;*goto {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::hash@5 (line 339)
  0x00007f81d4d396c8:   cmp    (%rsi),%rax                  ; implicit exception: dispatches to 0x00007f81d4d397a9
                                                            ;*invokevirtual hashCode {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::hash@9 (line 339)
  0x00007f81d4d396cb:   mov    %rsi,%rdi
  0x00007f81d4d396ce:   movabs $0x7f81d3f9d1c0,%rbx         ;   {metadata(method data for {method} {0x00007f81d3e40380} 'hash' '(Ljava/lang/Object;)I' in 'java/util/HashMap')}
  0x00007f81d4d396d8:   mov    0x8(%rdi),%edi
  0x00007f81d4d396db:   shl    $0x3,%rdi
  0x00007f81d4d396df:   cmp    0x1c8(%rbx),%rdi
  0x00007f81d4d396e6:   jne    0x00007f81d4d396f5
  0x00007f81d4d396e8:   addq   $0x1,0x1d0(%rbx)
  0x00007f81d4d396f0:   jmpq   0x00007f81d4d3975b
  0x00007f81d4d396f5:   cmp    0x1d8(%rbx),%rdi
  0x00007f81d4d396fc:   jne    0x00007f81d4d3970b
  0x00007f81d4d396fe:   addq   $0x1,0x1e0(%rbx)
  0x00007f81d4d39706:   jmpq   0x00007f81d4d3975b
  0x00007f81d4d3970b:   cmpq   $0x0,0x1c8(%rbx)
  0x00007f81d4d39716:   jne    0x00007f81d4d3972f
  0x00007f81d4d39718:   mov    %rdi,0x1c8(%rbx)
  0x00007f81d4d3971f:   movq   $0x1,0x1d0(%rbx)
  0x00007f81d4d3972a:   jmpq   0x00007f81d4d3975b
  0x00007f81d4d3972f:   cmpq   $0x0,0x1d8(%rbx)
  0x00007f81d4d3973a:   jne    0x00007f81d4d39753
  0x00007f81d4d3973c:   mov    %rdi,0x1d8(%rbx)
  0x00007f81d4d39743:   movq   $0x1,0x1e0(%rbx)
  0x00007f81d4d3974e:   jmpq   0x00007f81d4d3975b
  0x00007f81d4d39753:   addq   $0x1,0x1b8(%rbx)
  0x00007f81d4d3975b:   xchg   %ax,%ax
  0x00007f81d4d3975d:   movabs $0xffffffffffffffff,%rax
  0x00007f81d4d39767:   callq  0x00007f81d47ee700           ; ImmutableOopMap {}
                                                            ;*invokevirtual hashCode {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::hash@9 (line 339)
                                                            ;   {virtual_call}
  0x00007f81d4d3976c:   mov    %rax,%rsi
  0x00007f81d4d3976f:   shr    $0x10,%esi
  0x00007f81d4d39772:   xor    %rax,%rsi
  0x00007f81d4d39775:   mov    %rsi,%rax
  0x00007f81d4d39778:   add    $0x30,%rsp
  0x00007f81d4d3977c:   pop    %rbp
  0x00007f81d4d3977d:   mov    0x108(%r15),%r10
  0x00007f81d4d39784:   test   %eax,(%r10)                  ;   {poll_return}
  0x00007f81d4d39787:   retq   
  0x00007f81d4d39788:   movabs $0x7f81d3e40380,%r10         ;   {metadata({method} {0x00007f81d3e40380} 'hash' '(Ljava/lang/Object;)I' in 'java/util/HashMap')}
  0x00007f81d4d39792:   mov    %r10,0x8(%rsp)
  0x00007f81d4d39797:   movq   $0xffffffffffffffff,(%rsp)
  0x00007f81d4d3979f:   callq  0x00007f81d489e000           ; ImmutableOopMap {rsi=Oop }
                                                            ;*synchronization entry
                                                            ; - java.util.HashMap::hash@-1 (line 339)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d397a4:   jmpq   0x00007f81d4d39674
  0x00007f81d4d397a9:   callq  0x00007f81d480afa0           ; ImmutableOopMap {rsi=Oop }
                                                            ;*invokevirtual hashCode {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.HashMap::hash@9 (line 339)
                                                            ;   {runtime_call throw_null_pointer_exception Runtime1 stub}
  0x00007f81d4d397ae:   nop
  0x00007f81d4d397af:   nop
  0x00007f81d4d397b0:   mov    0x3f0(%r15),%rax
  0x00007f81d4d397b7:   movabs $0x0,%r10
  0x00007f81d4d397c1:   mov    %r10,0x3f0(%r15)
  0x00007f81d4d397c8:   movabs $0x0,%r10
  0x00007f81d4d397d2:   mov    %r10,0x3f8(%r15)
  0x00007f81d4d397d9:   add    $0x30,%rsp
  0x00007f81d4d397dd:   pop    %rbp
  0x00007f81d4d397de:   jmpq   0x00007f81d480be80           ;   {runtime_call unwind_exception Runtime1 stub}
  0x00007f81d4d397e3:   hlt    
  0x00007f81d4d397e4:   hlt    
  0x00007f81d4d397e5:   hlt    
  0x00007f81d4d397e6:   hlt    
  0x00007f81d4d397e7:   hlt    
  0x00007f81d4d397e8:   hlt    
  0x00007f81d4d397e9:   hlt    
  0x00007f81d4d397ea:   hlt    
  0x00007f81d4d397eb:   hlt    
  0x00007f81d4d397ec:   hlt    
  0x00007f81d4d397ed:   hlt    
  0x00007f81d4d397ee:   hlt    
  0x00007f81d4d397ef:   hlt    
  0x00007f81d4d397f0:   hlt    
  0x00007f81d4d397f1:   hlt    
  0x00007f81d4d397f2:   hlt    
  0x00007f81d4d397f3:   hlt    
  0x00007f81d4d397f4:   hlt    
  0x00007f81d4d397f5:   hlt    
  0x00007f81d4d397f6:   hlt    
  0x00007f81d4d397f7:   hlt    
  0x00007f81d4d397f8:   hlt    
  0x00007f81d4d397f9:   hlt    
  0x00007f81d4d397fa:   hlt    
  0x00007f81d4d397fb:   hlt    
  0x00007f81d4d397fc:   hlt    
  0x00007f81d4d397fd:   hlt    
  0x00007f81d4d397fe:   hlt    
  0x00007f81d4d397ff:   hlt    
[Stub Code]
  0x00007f81d4d39800:   nopl   0x0(%rax,%rax,1)             ;   {no_reloc}
  0x00007f81d4d39805:   movabs $0x0,%rbx                    ;   {static_stub}
  0x00007f81d4d3980f:   jmpq   0x00007f81d4d3980f           ;   {runtime_call}
[Exception Handler]
  0x00007f81d4d39814:   callq  0x00007f81d489ad00           ;   {runtime_call handle_exception_from_callee Runtime1 stub}
  0x00007f81d4d39819:   mov    %rsp,-0x28(%rsp)
  0x00007f81d4d3981e:   sub    $0x80,%rsp
  0x00007f81d4d39825:   mov    %rax,0x78(%rsp)
  0x00007f81d4d3982a:   mov    %rcx,0x70(%rsp)
  0x00007f81d4d3982f:   mov    %rdx,0x68(%rsp)
  0x00007f81d4d39834:   mov    %rbx,0x60(%rsp)
  0x00007f81d4d39839:   mov    %rbp,0x50(%rsp)
  0x00007f81d4d3983e:   mov    %rsi,0x48(%rsp)
  0x00007f81d4d39843:   mov    %rdi,0x40(%rsp)
  0x00007f81d4d39848:   mov    %r8,0x38(%rsp)
  0x00007f81d4d3984d:   mov    %r9,0x30(%rsp)
  0x00007f81d4d39852:   mov    %r10,0x28(%rsp)
  0x00007f81d4d39857:   mov    %r11,0x20(%rsp)
  0x00007f81d4d3985c:   mov    %r12,0x18(%rsp)
  0x00007f81d4d39861:   mov    %r13,0x10(%rsp)
  0x00007f81d4d39866:   mov    %r14,0x8(%rsp)
  0x00007f81d4d3986b:   mov    %r15,(%rsp)
  0x00007f81d4d3986f:   movabs $0x7f81f15ff3e2,%rdi         ;   {external_word}
  0x00007f81d4d39879:   movabs $0x7f81d4d39819,%rsi         ;   {internal_word}
  0x00007f81d4d39883:   mov    %rsp,%rdx
  0x00007f81d4d39886:   and    $0xfffffffffffffff0,%rsp
  0x00007f81d4d3988a:   callq  0x00007f81f1108240           ;   {runtime_call}
  0x00007f81d4d3988f:   hlt    
[Deopt Handler Code]
  0x00007f81d4d39890:   movabs $0x7f81d4d39890,%r10         ;   {section_word}
  0x00007f81d4d3989a:   push   %r10
  0x00007f81d4d3989c:   jmpq   0x00007f81d47ed0a0           ;   {runtime_call DeoptimizationBlob}
  0x00007f81d4d398a1:   hlt    
  0x00007f81d4d398a2:   hlt    
  0x00007f81d4d398a3:   hlt    
  0x00007f81d4d398a4:   hlt    
  0x00007f81d4d398a5:   hlt    
  0x00007f81d4d398a6:   hlt    
  0x00007f81d4d398a7:   hlt    
--------------------------------------------------------------------------------

============================= C2-compiled nmethod ==============================
----------------------------------- Assembly -----------------------------------

Compiled method (c2)     231   42 %     4       T::main @ 2 (21 bytes)
 total in heap  [0x00007f81dc26be90,0x00007f81dc26c1c0] = 816
 relocation     [0x00007f81dc26bff0,0x00007f81dc26c010] = 32
 main code      [0x00007f81dc26c020,0x00007f81dc26c0c0] = 160
 stub code      [0x00007f81dc26c0c0,0x00007f81dc26c0e8] = 40
 oops           [0x00007f81dc26c0e8,0x00007f81dc26c0f0] = 8
 metadata       [0x00007f81dc26c0f0,0x00007f81dc26c0f8] = 8
 scopes data    [0x00007f81dc26c0f8,0x00007f81dc26c120] = 40
 scopes pcs     [0x00007f81dc26c120,0x00007f81dc26c1a0] = 128
 dependencies   [0x00007f81dc26c1a0,0x00007f81dc26c1a8] = 8
 handler table  [0x00007f81dc26c1a8,0x00007f81dc26c1c0] = 24

--------------------------------------------------------------------------------
[Constant Pool (empty)]

--------------------------------------------------------------------------------

[Verified Entry Point]
  # {method} {0x00007f81d40fe2d8} 'main' '([Ljava/lang/String;)V' in 'T'
  0x00007f81dc26c020:   callq  0x00007f81f121be60           ;   {runtime_call}
  0x00007f81dc26c025:   data16 data16 nopw 0x0(%rax,%rax,1)
  0x00007f81dc26c030:   mov    %eax,-0x14000(%rsp)
  0x00007f81dc26c037:   push   %rbp
  0x00007f81dc26c038:   sub    $0x10,%rsp
  0x00007f81dc26c03c:   mov    (%rsi),%ebp
  0x00007f81dc26c03e:   mov    %rsi,%rdi
  0x00007f81dc26c041:   movabs $0x7f81f12eb9b0,%r10
  0x00007f81dc26c04b:   callq  *%r10
  0x00007f81dc26c04e:   jmp    0x00007f81dc26c07c           ;*iload_1 {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - T::main@2 (line 6)
  0x00007f81dc26c050:   data16 xchg %ax,%ax
  0x00007f81dc26c053:   callq  0x00007f81d47ee400           ; ImmutableOopMap {}
                                                            ;*invokestatic m {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - T::main@8 (line 7)
                                                            ;   {static_call}
  0x00007f81dc26c058:   movabs $0xe1692578,%r10             ;   {oop(a 'java/lang/Class'{0x00000000e1692578} = 'T')}
  0x00007f81dc26c062:   movl   $0x1,0x70(%r10)
  0x00007f81dc26c06a:   lock addl $0x0,-0x40(%rsp)          ;*goto {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - T::main@17 (line 6)
  0x00007f81dc26c070:   mov    0x108(%r15),%r10
  0x00007f81dc26c077:   inc    %ebp                         ; ImmutableOopMap {}
                                                            ;*goto {reexecute=1 rethrow=0 return_oop=0}
                                                            ; - (reexecute) T::main@17 (line 6)
  0x00007f81dc26c079:   test   %eax,(%r10)                  ;*iload_1 {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - T::main@2 (line 6)
                                                            ;   {poll}
  0x00007f81dc26c07c:   cmp    $0xf4240,%ebp
  0x00007f81dc26c082:   jl     0x00007f81dc26c050
  0x00007f81dc26c084:   add    $0x10,%rsp
  0x00007f81dc26c088:   pop    %rbp
  0x00007f81dc26c089:   mov    0x108(%r15),%r10
  0x00007f81dc26c090:   test   %eax,(%r10)                  ;   {poll_return}
  0x00007f81dc26c093:   retq                                ;*invokestatic m {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - T::main@8 (line 7)
  0x00007f81dc26c094:   mov    %rax,%rsi
  0x00007f81dc26c097:   add    $0x10,%rsp
  0x00007f81dc26c09b:   pop    %rbp
  0x00007f81dc26c09c:   jmpq   0x00007f81d48a3e80           ;   {runtime_call _rethrow_Java}
  0x00007f81dc26c0a1:   hlt    
  0x00007f81dc26c0a2:   hlt    
  0x00007f81dc26c0a3:   hlt    
  0x00007f81dc26c0a4:   hlt    
  0x00007f81dc26c0a5:   hlt    
  0x00007f81dc26c0a6:   hlt    
  0x00007f81dc26c0a7:   hlt    
  0x00007f81dc26c0a8:   hlt    
  0x00007f81dc26c0a9:   hlt    
  0x00007f81dc26c0aa:   hlt    
  0x00007f81dc26c0ab:   hlt    
  0x00007f81dc26c0ac:   hlt    
  0x00007f81dc26c0ad:   hlt    
  0x00007f81dc26c0ae:   hlt    
  0x00007f81dc26c0af:   hlt    
  0x00007f81dc26c0b0:   hlt    
  0x00007f81dc26c0b1:   hlt    
  0x00007f81dc26c0b2:   hlt    
  0x00007f81dc26c0b3:   hlt    
  0x00007f81dc26c0b4:   hlt    
  0x00007f81dc26c0b5:   hlt    
  0x00007f81dc26c0b6:   hlt    
  0x00007f81dc26c0b7:   hlt    
  0x00007f81dc26c0b8:   hlt    
  0x00007f81dc26c0b9:   hlt    
  0x00007f81dc26c0ba:   hlt    
  0x00007f81dc26c0bb:   hlt    
  0x00007f81dc26c0bc:   hlt    
  0x00007f81dc26c0bd:   hlt    
  0x00007f81dc26c0be:   hlt    
  0x00007f81dc26c0bf:   hlt    
[Stub Code]
  0x00007f81dc26c0c0:   movabs $0x0,%rbx                    ;   {no_reloc}
  0x00007f81dc26c0ca:   jmpq   0x00007f81dc26c0ca           ;   {runtime_call}
[Exception Handler]
  0x00007f81dc26c0cf:   jmpq   0x00007f81d4809300           ;   {runtime_call ExceptionBlob}
[Deopt Handler Code]
  0x00007f81dc26c0d4:   callq  0x00007f81dc26c0d9
  0x00007f81dc26c0d9:   subq   $0x5,(%rsp)
  0x00007f81dc26c0de:   jmpq   0x00007f81d47ed0a0           ;   {runtime_call DeoptimizationBlob}
  0x00007f81dc26c0e3:   hlt    
  0x00007f81dc26c0e4:   hlt    
  0x00007f81dc26c0e5:   hlt    
  0x00007f81dc26c0e6:   hlt    
  0x00007f81dc26c0e7:   hlt    
--------------------------------------------------------------------------------

============================= C1-compiled nmethod ==============================
----------------------------------- Assembly -----------------------------------

Compiled method (c1)     233   12       3       java.util.Objects::requireNonNull (14 bytes)
 total in heap  [0x00007f81d4d39a10,0x00007f81d4d39ee8] = 1240
 relocation     [0x00007f81d4d39b70,0x00007f81d4d39bb0] = 64
 main code      [0x00007f81d4d39bc0,0x00007f81d4d39d40] = 384
 stub code      [0x00007f81d4d39d40,0x00007f81d4d39de8] = 168
 metadata       [0x00007f81d4d39de8,0x00007f81d4d39df0] = 8
 scopes data    [0x00007f81d4d39df0,0x00007f81d4d39e30] = 64
 scopes pcs     [0x00007f81d4d39e30,0x00007f81d4d39ee0] = 176
 dependencies   [0x00007f81d4d39ee0,0x00007f81d4d39ee8] = 8

--------------------------------------------------------------------------------
[Constant Pool (empty)]

--------------------------------------------------------------------------------

[Verified Entry Point]
  # {method} {0x00007f81d3e273f8} 'requireNonNull' '(Ljava/lang/Object;)Ljava/lang/Object;' in 'java/util/Objects'
  # parm0:    rsi:rsi   = 'java/lang/Object'
  #           [sp+0x40]  (sp of caller)
  0x00007f81d4d39bc0:   mov    %eax,-0x14000(%rsp)
  0x00007f81d4d39bc7:   push   %rbp
  0x00007f81d4d39bc8:   sub    $0x30,%rsp
  0x00007f81d4d39bcc:   movabs $0x7f81d3f4ec28,%rax         ;   {metadata(method data for {method} {0x00007f81d3e273f8} 'requireNonNull' '(Ljava/lang/Object;)Ljava/lang/Object;' in 'java/util/Objects')}
  0x00007f81d4d39bd6:   mov    0x13c(%rax),%edx
  0x00007f81d4d39bdc:   add    $0x8,%edx
  0x00007f81d4d39bdf:   mov    %edx,0x13c(%rax)
  0x00007f81d4d39be5:   and    $0x1ff8,%edx
  0x00007f81d4d39beb:   cmp    $0x0,%edx
  0x00007f81d4d39bee:   je     0x00007f81d4d39cc6           ;*aload_0 {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.Objects::requireNonNull@0 (line 221)
  0x00007f81d4d39bf4:   cmp    $0x0,%rsi
  0x00007f81d4d39bf8:   movabs $0x7f81d3f4ec28,%rax         ;   {metadata(method data for {method} {0x00007f81d3e273f8} 'requireNonNull' '(Ljava/lang/Object;)Ljava/lang/Object;' in 'java/util/Objects')}
  0x00007f81d4d39c02:   movabs $0x190,%rdx
  0x00007f81d4d39c0c:   je     0x00007f81d4d39c1c
  0x00007f81d4d39c12:   movabs $0x180,%rdx
  0x00007f81d4d39c1c:   mov    (%rax,%rdx,1),%rdi
  0x00007f81d4d39c20:   lea    0x1(%rdi),%rdi
  0x00007f81d4d39c24:   mov    %rdi,(%rax,%rdx,1)
  0x00007f81d4d39c28:   je     0x00007f81d4d39c41           ;*ifnonnull {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.Objects::requireNonNull@1 (line 221)
  0x00007f81d4d39c2e:   mov    %rsi,%rax
  0x00007f81d4d39c31:   add    $0x30,%rsp
  0x00007f81d4d39c35:   pop    %rbp
  0x00007f81d4d39c36:   mov    0x108(%r15),%r10
  0x00007f81d4d39c3d:   test   %eax,(%r10)                  ;   {poll_return}
  0x00007f81d4d39c40:   retq                                ;*areturn {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.Objects::requireNonNull@13 (line 223)
  0x00007f81d4d39c41:   nopl   0x0(%rax)
  0x00007f81d4d39c48:   jmpq   0x00007f81d4d39cf6           ;   {no_reloc}
  0x00007f81d4d39c4d:   add    %al,(%rax)
  0x00007f81d4d39c4f:   add    %al,(%rax)
  0x00007f81d4d39c51:   add    %cl,-0x75(%rcx)
  0x00007f81d4d39c54:   xchg   %ebx,(%rax)
  0x00007f81d4d39c56:   add    %eax,(%rax)
  0x00007f81d4d39c58:   add    %cl,-0x73(%rax)
  0x00007f81d4d39c5b:   js     0x00007f81d4d39c85
  0x00007f81d4d39c5d:   cmp    0x128(%r15),%rdi
  0x00007f81d4d39c64:   ja     0x00007f81d4d39d00
  0x00007f81d4d39c6a:   mov    %rdi,0x118(%r15)
  0x00007f81d4d39c71:   mov    0xb8(%rdx),%rcx
  0x00007f81d4d39c78:   mov    %rcx,(%rax)
  0x00007f81d4d39c7b:   mov    %rdx,%rcx
  0x00007f81d4d39c7e:   shr    $0x3,%rcx
  0x00007f81d4d39c82:   mov    %ecx,0x8(%rax)
  0x00007f81d4d39c85:   xor    %rcx,%rcx
  0x00007f81d4d39c88:   mov    %ecx,0xc(%rax)
  0x00007f81d4d39c8b:   xor    %rcx,%rcx
  0x00007f81d4d39c8e:   mov    %rcx,0x10(%rax)
  0x00007f81d4d39c92:   mov    %rcx,0x18(%rax)
  0x00007f81d4d39c96:   mov    %rcx,0x20(%rax)              ;*new {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.Objects::requireNonNull@4 (line 222)
  0x00007f81d4d39c9a:   mov    %rax,%rsi
  0x00007f81d4d39c9d:   movabs $0x7f81d3f4ec28,%rdi         ;   {metadata(method data for {method} {0x00007f81d3e273f8} 'requireNonNull' '(Ljava/lang/Object;)Ljava/lang/Object;' in 'java/util/Objects')}
  0x00007f81d4d39ca7:   addq   $0x1,0x1a0(%rdi)
  0x00007f81d4d39caf:   mov    %rax,%rsi                    ;*invokespecial <init> {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.Objects::requireNonNull@8 (line 222)
  0x00007f81d4d39cb2:   mov    %rax,0x20(%rsp)
  0x00007f81d4d39cb7:   callq  0x00007f81d47eea00           ; ImmutableOopMap {[32]=Oop }
                                                            ;*invokespecial <init> {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.Objects::requireNonNull@8 (line 222)
                                                            ;   {optimized virtual_call}
  0x00007f81d4d39cbc:   mov    0x20(%rsp),%rax
  0x00007f81d4d39cc1:   jmpq   0x00007f81d4d39d35
  0x00007f81d4d39cc6:   movabs $0x7f81d3e273f8,%r10         ;   {metadata({method} {0x00007f81d3e273f8} 'requireNonNull' '(Ljava/lang/Object;)Ljava/lang/Object;' in 'java/util/Objects')}
  0x00007f81d4d39cd0:   mov    %r10,0x8(%rsp)
  0x00007f81d4d39cd5:   movq   $0xffffffffffffffff,(%rsp)
  0x00007f81d4d39cdd:   callq  0x00007f81d489e000           ; ImmutableOopMap {rsi=Oop }
                                                            ;*synchronization entry
                                                            ; - java.util.Objects::requireNonNull@-1 (line 221)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d39ce2:   jmpq   0x00007f81d4d39bf4
  0x00007f81d4d39ce7:   movabs $0x0,%rdx                    ;   {metadata(NULL)}
  0x00007f81d4d39cf1:   mov    $0xa050f00,%eax
  0x00007f81d4d39cf6:   callq  0x00007f81d489cf80           ; ImmutableOopMap {}
                                                            ;*new {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.Objects::requireNonNull@4 (line 222)
                                                            ;   {runtime_call load_klass_patching Runtime1 stub}
  0x00007f81d4d39cfb:   jmpq   0x00007f81d4d39c48
  0x00007f81d4d39d00:   mov    %rdx,%rdx
  0x00007f81d4d39d03:   callq  0x00007f81d480a680           ; ImmutableOopMap {}
                                                            ;*new {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.util.Objects::requireNonNull@4 (line 222)
                                                            ;   {runtime_call fast_new_instance Runtime1 stub}
  0x00007f81d4d39d08:   jmp    0x00007f81d4d39c9a
  0x00007f81d4d39d0a:   nop
  0x00007f81d4d39d0b:   nop
  0x00007f81d4d39d0c:   mov    0x3f0(%r15),%rax
  0x00007f81d4d39d13:   movabs $0x0,%r10
  0x00007f81d4d39d1d:   mov    %r10,0x3f0(%r15)
  0x00007f81d4d39d24:   movabs $0x0,%r10
  0x00007f81d4d39d2e:   mov    %r10,0x3f8(%r15)
  0x00007f81d4d39d35:   add    $0x30,%rsp
  0x00007f81d4d39d39:   pop    %rbp
  0x00007f81d4d39d3a:   jmpq   0x00007f81d480be80           ;   {runtime_call unwind_exception Runtime1 stub}
  0x00007f81d4d39d3f:   hlt    
[Stub Code]
  0x00007f81d4d39d40:   nopl   0x0(%rax,%rax,1)             ;   {no_reloc}
  0x00007f81d4d39d45:   movabs $0x0,%rbx                    ;   {static_stub}
  0x00007f81d4d39d4f:   jmpq   0x00007f81d4d39d4f           ;   {runtime_call}
[Exception Handler]
  0x00007f81d4d39d54:   callq  0x00007f81d489ad00           ;   {runtime_call handle_exception_from_callee Runtime1 stub}
  0x00007f81d4d39d59:   mov    %rsp,-0x28(%rsp)
  0x00007f81d4d39d5e:   sub    $0x80,%rsp
  0x00007f81d4d39d65:   mov    %rax,0x78(%rsp)
  0x00007f81d4d39d6a:   mov    %rcx,0x70(%rsp)
  0x00007f81d4d39d6f:   mov    %rdx,0x68(%rsp)
  0x00007f81d4d39d74:   mov    %rbx,0x60(%rsp)
  0x00007f81d4d39d79:   mov    %rbp,0x50(%rsp)
  0x00007f81d4d39d7e:   mov    %rsi,0x48(%rsp)
  0x00007f81d4d39d83:   mov    %rdi,0x40(%rsp)
  0x00007f81d4d39d88:   mov    %r8,0x38(%rsp)
  0x00007f81d4d39d8d:   mov    %r9,0x30(%rsp)
  0x00007f81d4d39d92:   mov    %r10,0x28(%rsp)
  0x00007f81d4d39d97:   mov    %r11,0x20(%rsp)
  0x00007f81d4d39d9c:   mov    %r12,0x18(%rsp)
  0x00007f81d4d39da1:   mov    %r13,0x10(%rsp)
  0x00007f81d4d39da6:   mov    %r14,0x8(%rsp)
  0x00007f81d4d39dab:   mov    %r15,(%rsp)
  0x00007f81d4d39daf:   movabs $0x7f81f15ff3e2,%rdi         ;   {external_word}
  0x00007f81d4d39db9:   movabs $0x7f81d4d39d59,%rsi         ;   {internal_word}
  0x00007f81d4d39dc3:   mov    %rsp,%rdx
  0x00007f81d4d39dc6:   and    $0xfffffffffffffff0,%rsp
  0x00007f81d4d39dca:   callq  0x00007f81f1108240           ;   {runtime_call}
  0x00007f81d4d39dcf:   hlt    
[Deopt Handler Code]
  0x00007f81d4d39dd0:   movabs $0x7f81d4d39dd0,%r10         ;   {section_word}
  0x00007f81d4d39dda:   push   %r10
  0x00007f81d4d39ddc:   jmpq   0x00007f81d47ed0a0           ;   {runtime_call DeoptimizationBlob}
  0x00007f81d4d39de1:   hlt    
  0x00007f81d4d39de2:   hlt    
  0x00007f81d4d39de3:   hlt    
  0x00007f81d4d39de4:   hlt    
  0x00007f81d4d39de5:   hlt    
  0x00007f81d4d39de6:   hlt    
  0x00007f81d4d39de7:   hlt    
--------------------------------------------------------------------------------

============================= C2-compiled nmethod ==============================
----------------------------------- Assembly -----------------------------------

Compiled method (c2)     237   43       4       T::main (21 bytes)
 total in heap  [0x00007f81dc26c210,0x00007f81dc26c518] = 776
 relocation     [0x00007f81dc26c370,0x00007f81dc26c388] = 24
 main code      [0x00007f81dc26c3a0,0x00007f81dc26c400] = 96
 stub code      [0x00007f81dc26c400,0x00007f81dc26c428] = 40
 oops           [0x00007f81dc26c428,0x00007f81dc26c430] = 8
 metadata       [0x00007f81dc26c430,0x00007f81dc26c440] = 16
 scopes data    [0x00007f81dc26c440,0x00007f81dc26c478] = 56
 scopes pcs     [0x00007f81dc26c478,0x00007f81dc26c4f8] = 128
 dependencies   [0x00007f81dc26c4f8,0x00007f81dc26c500] = 8
 handler table  [0x00007f81dc26c500,0x00007f81dc26c518] = 24

--------------------------------------------------------------------------------
[Constant Pool (empty)]

--------------------------------------------------------------------------------

[Verified Entry Point]
  # {method} {0x00007f81d40fe2d8} 'main' '([Ljava/lang/String;)V' in 'T'
  # parm0:    rsi:rsi   = '[Ljava/lang/String;'
  #           [sp+0x20]  (sp of caller)
  0x00007f81dc26c3a0:   mov    %eax,-0x14000(%rsp)
  0x00007f81dc26c3a7:   push   %rbp
  0x00007f81dc26c3a8:   sub    $0x10,%rsp                   ;*synchronization entry
                                                            ; - T::main@-1 (line 6)
  0x00007f81dc26c3ac:   xor    %ebp,%ebp
  0x00007f81dc26c3ae:   xchg   %ax,%ax
  0x00007f81dc26c3b0:   data16 xchg %ax,%ax
  0x00007f81dc26c3b3:   callq  0x00007f81d47ee400           ; ImmutableOopMap {}
                                                            ;*invokestatic m {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - T::main@8 (line 7)
                                                            ;   {static_call}
  0x00007f81dc26c3b8:   movabs $0xe1692578,%r10             ;   {oop(a 'java/lang/Class'{0x00000000e1692578} = 'T')}
  0x00007f81dc26c3c2:   movl   $0x1,0x70(%r10)
  0x00007f81dc26c3ca:   lock addl $0x0,-0x40(%rsp)          ;*putstatic i {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - T::n@1 (line 17)
                                                            ; - T::main@11 (line 8)
  0x00007f81dc26c3d0:   inc    %ebp                         ;*iinc {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - T::main@14 (line 6)
  0x00007f81dc26c3d2:   cmp    $0xf4240,%ebp
  0x00007f81dc26c3d8:   jl     0x00007f81dc26c3b0           ;*if_icmpge {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - T::main@5 (line 6)
  0x00007f81dc26c3da:   add    $0x10,%rsp
  0x00007f81dc26c3de:   pop    %rbp
  0x00007f81dc26c3df:   mov    0x108(%r15),%r10
  0x00007f81dc26c3e6:   test   %eax,(%r10)                  ;   {poll_return}
  0x00007f81dc26c3e9:   retq                                ;*invokestatic m {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - T::main@8 (line 7)
  0x00007f81dc26c3ea:   mov    %rax,%rsi
  0x00007f81dc26c3ed:   add    $0x10,%rsp
  0x00007f81dc26c3f1:   pop    %rbp
  0x00007f81dc26c3f2:   jmpq   0x00007f81d48a3e80           ;   {runtime_call _rethrow_Java}
  0x00007f81dc26c3f7:   hlt    
  0x00007f81dc26c3f8:   hlt    
  0x00007f81dc26c3f9:   hlt    
  0x00007f81dc26c3fa:   hlt    
  0x00007f81dc26c3fb:   hlt    
  0x00007f81dc26c3fc:   hlt    
  0x00007f81dc26c3fd:   hlt    
  0x00007f81dc26c3fe:   hlt    
  0x00007f81dc26c3ff:   hlt    
[Stub Code]
  0x00007f81dc26c400:   movabs $0x0,%rbx                    ;   {no_reloc}
  0x00007f81dc26c40a:   jmpq   0x00007f81dc26c40a           ;   {runtime_call}
[Exception Handler]
  0x00007f81dc26c40f:   jmpq   0x00007f81d4809300           ;   {runtime_call ExceptionBlob}
[Deopt Handler Code]
  0x00007f81dc26c414:   callq  0x00007f81dc26c419
  0x00007f81dc26c419:   subq   $0x5,(%rsp)
  0x00007f81dc26c41e:   jmpq   0x00007f81d47ed0a0           ;   {runtime_call DeoptimizationBlob}
  0x00007f81dc26c423:   hlt    
  0x00007f81dc26c424:   hlt    
  0x00007f81dc26c425:   hlt    
  0x00007f81dc26c426:   hlt    
  0x00007f81dc26c427:   hlt    
--------------------------------------------------------------------------------

============================= C1-compiled nmethod ==============================
----------------------------------- Assembly -----------------------------------

Compiled method (c1)     243    7       3       java.lang.String::equals (50 bytes)
 total in heap  [0x00007f81d4d39f10,0x00007f81d4d3a6f0] = 2016
 relocation     [0x00007f81d4d3a070,0x00007f81d4d3a0c8] = 88
 main code      [0x00007f81d4d3a0e0,0x00007f81d4d3a480] = 928
 stub code      [0x00007f81d4d3a480,0x00007f81d4d3a528] = 168
 metadata       [0x00007f81d4d3a528,0x00007f81d4d3a530] = 8
 scopes data    [0x00007f81d4d3a530,0x00007f81d4d3a5a8] = 120
 scopes pcs     [0x00007f81d4d3a5a8,0x00007f81d4d3a6d8] = 304
 dependencies   [0x00007f81d4d3a6d8,0x00007f81d4d3a6e0] = 8
 nul chk table  [0x00007f81d4d3a6e0,0x00007f81d4d3a6f0] = 16

--------------------------------------------------------------------------------
[Constant Pool (empty)]

--------------------------------------------------------------------------------

[Entry Point]
  # {method} {0x00007f81d3d048e0} 'equals' '(Ljava/lang/Object;)Z' in 'java/lang/String'
  # this:     rsi:rsi   = 'java/lang/String'
  # parm0:    rdx:rdx   = 'java/lang/Object'
  #           [sp+0x40]  (sp of caller)
  0x00007f81d4d3a0e0:   mov    0x8(%rsi),%r10d
  0x00007f81d4d3a0e4:   shl    $0x3,%r10
  0x00007f81d4d3a0e8:   cmp    %rax,%r10
  0x00007f81d4d3a0eb:   jne    0x00007f81d47eed00           ;   {runtime_call ic_miss_stub}
  0x00007f81d4d3a0f1:   data16 data16 nopw 0x0(%rax,%rax,1)
  0x00007f81d4d3a0fc:   data16 data16 xchg %ax,%ax
[Verified Entry Point]
  0x00007f81d4d3a100:   mov    %eax,-0x14000(%rsp)
  0x00007f81d4d3a107:   push   %rbp
  0x00007f81d4d3a108:   sub    $0x30,%rsp
  0x00007f81d4d3a10c:   movabs $0x7f81d3f45778,%rax         ;   {metadata(method data for {method} {0x00007f81d3d048e0} 'equals' '(Ljava/lang/Object;)Z' in 'java/lang/String')}
  0x00007f81d4d3a116:   mov    0x13c(%rax),%edi
  0x00007f81d4d3a11c:   add    $0x8,%edi
  0x00007f81d4d3a11f:   mov    %edi,0x13c(%rax)
  0x00007f81d4d3a125:   and    $0x1ff8,%edi
  0x00007f81d4d3a12b:   cmp    $0x0,%edi
  0x00007f81d4d3a12e:   je     0x00007f81d4d3a41c           ;*aload_0 {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::equals@0 (line 1019)
  0x00007f81d4d3a134:   cmp    %rdx,%rsi
  0x00007f81d4d3a137:   movabs $0x7f81d3f45778,%rax         ;   {metadata(method data for {method} {0x00007f81d3d048e0} 'equals' '(Ljava/lang/Object;)Z' in 'java/lang/String')}
  0x00007f81d4d3a141:   movabs $0x190,%rdi
  0x00007f81d4d3a14b:   je     0x00007f81d4d3a15b
  0x00007f81d4d3a151:   movabs $0x180,%rdi
  0x00007f81d4d3a15b:   mov    (%rax,%rdi,1),%rbx
  0x00007f81d4d3a15f:   lea    0x1(%rbx),%rbx
  0x00007f81d4d3a163:   mov    %rbx,(%rax,%rdi,1)
  0x00007f81d4d3a167:   je     0x00007f81d4d3a407           ;*if_acmpne {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::equals@2 (line 1019)
  0x00007f81d4d3a16d:   cmp    $0x0,%rdx
  0x00007f81d4d3a171:   jne    0x00007f81d4d3a189
  0x00007f81d4d3a173:   movabs $0x7f81d3f45778,%rbx         ;   {metadata(method data for {method} {0x00007f81d3d048e0} 'equals' '(Ljava/lang/Object;)Z' in 'java/lang/String')}
  0x00007f81d4d3a17d:   orb    $0x1,0x199(%rbx)
  0x00007f81d4d3a184:   jmpq   0x00007f81d4d3a249
  0x00007f81d4d3a189:   movabs $0x100001c78,%rcx            ;   {metadata('java/lang/String')}
  0x00007f81d4d3a193:   mov    0x8(%rdx),%edi
  0x00007f81d4d3a196:   shl    $0x3,%rdi
  0x00007f81d4d3a19a:   cmp    %rdi,%rcx
  0x00007f81d4d3a19d:   jne    0x00007f81d4d3a22d
  0x00007f81d4d3a1a3:   movabs $0x7f81d3f45778,%rbx         ;   {metadata(method data for {method} {0x00007f81d3d048e0} 'equals' '(Ljava/lang/Object;)Z' in 'java/lang/String')}
  0x00007f81d4d3a1ad:   mov    0x8(%rdx),%ecx
  0x00007f81d4d3a1b0:   shl    $0x3,%rcx
  0x00007f81d4d3a1b4:   cmp    0x1b0(%rbx),%rcx
  0x00007f81d4d3a1bb:   jne    0x00007f81d4d3a1ca
  0x00007f81d4d3a1bd:   addq   $0x1,0x1b8(%rbx)
  0x00007f81d4d3a1c5:   jmpq   0x00007f81d4d3a24e
  0x00007f81d4d3a1ca:   cmp    0x1c0(%rbx),%rcx
  0x00007f81d4d3a1d1:   jne    0x00007f81d4d3a1e0
  0x00007f81d4d3a1d3:   addq   $0x1,0x1c8(%rbx)
  0x00007f81d4d3a1db:   jmpq   0x00007f81d4d3a24e
  0x00007f81d4d3a1e0:   cmpq   $0x0,0x1b0(%rbx)
  0x00007f81d4d3a1eb:   jne    0x00007f81d4d3a204
  0x00007f81d4d3a1ed:   mov    %rcx,0x1b0(%rbx)
  0x00007f81d4d3a1f4:   movq   $0x1,0x1b8(%rbx)
  0x00007f81d4d3a1ff:   jmpq   0x00007f81d4d3a24e
  0x00007f81d4d3a204:   cmpq   $0x0,0x1c0(%rbx)
  0x00007f81d4d3a20f:   jne    0x00007f81d4d3a228
  0x00007f81d4d3a211:   mov    %rcx,0x1c0(%rbx)
  0x00007f81d4d3a218:   movq   $0x1,0x1c8(%rbx)
  0x00007f81d4d3a223:   jmpq   0x00007f81d4d3a24e
  0x00007f81d4d3a228:   jmpq   0x00007f81d4d3a24e
  0x00007f81d4d3a22d:   movabs $0x7f81d3f45778,%rbx         ;   {metadata(method data for {method} {0x00007f81d3d048e0} 'equals' '(Ljava/lang/Object;)Z' in 'java/lang/String')}
  0x00007f81d4d3a237:   subq   $0x1,0x1a0(%rbx)
  0x00007f81d4d3a23f:   jmpq   0x00007f81d4d3a249
  0x00007f81d4d3a244:   jmpq   0x00007f81d4d3a24e
  0x00007f81d4d3a249:   xor    %rax,%rax
  0x00007f81d4d3a24c:   jmp    0x00007f81d4d3a258
  0x00007f81d4d3a24e:   movabs $0x1,%rax                    ;*instanceof {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::equals@8 (line 1022)
  0x00007f81d4d3a258:   cmp    $0x0,%eax
  0x00007f81d4d3a25b:   movabs $0x7f81d3f45778,%rax         ;   {metadata(method data for {method} {0x00007f81d3d048e0} 'equals' '(Ljava/lang/Object;)Z' in 'java/lang/String')}
  0x00007f81d4d3a265:   movabs $0x1d8,%rdi
  0x00007f81d4d3a26f:   je     0x00007f81d4d3a27f
  0x00007f81d4d3a275:   movabs $0x1e8,%rdi
  0x00007f81d4d3a27f:   mov    (%rax,%rdi,1),%rbx
  0x00007f81d4d3a283:   lea    0x1(%rbx),%rbx
  0x00007f81d4d3a287:   mov    %rbx,(%rax,%rdi,1)
  0x00007f81d4d3a28b:   je     0x00007f81d4d3a3bd           ;*ifeq {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::equals@11 (line 1022)
  0x00007f81d4d3a291:   cmp    $0x0,%rdx
  0x00007f81d4d3a295:   jne    0x00007f81d4d3a2ad
  0x00007f81d4d3a297:   movabs $0x7f81d3f45778,%rdi         ;   {metadata(method data for {method} {0x00007f81d3d048e0} 'equals' '(Ljava/lang/Object;)Z' in 'java/lang/String')}
  0x00007f81d4d3a2a1:   orb    $0x1,0x1f1(%rdi)
  0x00007f81d4d3a2a8:   jmpq   0x00007f81d4d3a36d
  0x00007f81d4d3a2ad:   movabs $0x100001c78,%rbx            ;   {metadata('java/lang/String')}
  0x00007f81d4d3a2b7:   mov    0x8(%rdx),%eax
  0x00007f81d4d3a2ba:   shl    $0x3,%rax
  0x00007f81d4d3a2be:   cmp    %rax,%rbx
  0x00007f81d4d3a2c1:   jne    0x00007f81d4d3a351
  0x00007f81d4d3a2c7:   movabs $0x7f81d3f45778,%rdi         ;   {metadata(method data for {method} {0x00007f81d3d048e0} 'equals' '(Ljava/lang/Object;)Z' in 'java/lang/String')}
  0x00007f81d4d3a2d1:   mov    0x8(%rdx),%ebx
  0x00007f81d4d3a2d4:   shl    $0x3,%rbx
  0x00007f81d4d3a2d8:   cmp    0x208(%rdi),%rbx
  0x00007f81d4d3a2df:   jne    0x00007f81d4d3a2ee
  0x00007f81d4d3a2e1:   addq   $0x1,0x210(%rdi)
  0x00007f81d4d3a2e9:   jmpq   0x00007f81d4d3a36d
  0x00007f81d4d3a2ee:   cmp    0x218(%rdi),%rbx
  0x00007f81d4d3a2f5:   jne    0x00007f81d4d3a304
  0x00007f81d4d3a2f7:   addq   $0x1,0x220(%rdi)
  0x00007f81d4d3a2ff:   jmpq   0x00007f81d4d3a36d
  0x00007f81d4d3a304:   cmpq   $0x0,0x208(%rdi)
  0x00007f81d4d3a30f:   jne    0x00007f81d4d3a328
  0x00007f81d4d3a311:   mov    %rbx,0x208(%rdi)
  0x00007f81d4d3a318:   movq   $0x1,0x210(%rdi)
  0x00007f81d4d3a323:   jmpq   0x00007f81d4d3a36d
  0x00007f81d4d3a328:   cmpq   $0x0,0x218(%rdi)
  0x00007f81d4d3a333:   jne    0x00007f81d4d3a34c
  0x00007f81d4d3a335:   mov    %rbx,0x218(%rdi)
  0x00007f81d4d3a33c:   movq   $0x1,0x220(%rdi)
  0x00007f81d4d3a347:   jmpq   0x00007f81d4d3a36d
  0x00007f81d4d3a34c:   jmpq   0x00007f81d4d3a36d
  0x00007f81d4d3a351:   movabs $0x7f81d3f45778,%rdi         ;   {metadata(method data for {method} {0x00007f81d3d048e0} 'equals' '(Ljava/lang/Object;)Z' in 'java/lang/String')}
  0x00007f81d4d3a35b:   subq   $0x1,0x1f8(%rdi)
  0x00007f81d4d3a363:   jmpq   0x00007f81d4d3a43d
  0x00007f81d4d3a368:   jmpq   0x00007f81d4d3a36d           ;*checkcast {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::equals@15 (line 1023)
  0x00007f81d4d3a36d:   movabs $0x7f81d3f45778,%rax         ;   {metadata(method data for {method} {0x00007f81d3d048e0} 'equals' '(Ljava/lang/Object;)Z' in 'java/lang/String')}
  0x00007f81d4d3a377:   incl   0x240(%rax)                  ;*ifeq {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::equals@22 (line 1024)
  0x00007f81d4d3a37d:   movsbl 0x14(%rsi),%eax              ;*getfield coder {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::equals@26 (line 1024)
  0x00007f81d4d3a381:   movsbl 0x14(%rdx),%edi              ; implicit exception: dispatches to 0x00007f81d4d3a446
                                                            ;*getfield coder {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::equals@30 (line 1024)
  0x00007f81d4d3a385:   cmp    %edi,%eax
  0x00007f81d4d3a387:   movabs $0x7f81d3f45778,%rax         ;   {metadata(method data for {method} {0x00007f81d3d048e0} 'equals' '(Ljava/lang/Object;)Z' in 'java/lang/String')}
  0x00007f81d4d3a391:   movabs $0x260,%rdi
  0x00007f81d4d3a39b:   je     0x00007f81d4d3a3ab
  0x00007f81d4d3a3a1:   movabs $0x250,%rdi
  0x00007f81d4d3a3ab:   mov    (%rax,%rdi,1),%rbx
  0x00007f81d4d3a3af:   lea    0x1(%rbx),%rbx
  0x00007f81d4d3a3b3:   mov    %rbx,(%rax,%rdi,1)
  0x00007f81d4d3a3b7:   je     0x00007f81d4d3a3d2           ;*if_icmpne {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::equals@33 (line 1024)
  0x00007f81d4d3a3bd:   mov    $0x0,%eax
  0x00007f81d4d3a3c2:   add    $0x30,%rsp
  0x00007f81d4d3a3c6:   pop    %rbp
  0x00007f81d4d3a3c7:   mov    0x108(%r15),%r10
  0x00007f81d4d3a3ce:   test   %eax,(%r10)                  ;   {poll_return}
  0x00007f81d4d3a3d1:   retq                                ;*ireturn {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::equals@49 (line 1028)
  0x00007f81d4d3a3d2:   mov    0xc(%rsi),%esi               ;*getfield value {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::equals@37 (line 1025)
  0x00007f81d4d3a3d5:   mov    0xc(%rdx),%edx               ;*getfield value {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::equals@41 (line 1025)
  0x00007f81d4d3a3d8:   movabs $0x7f81d3f45778,%rdi         ;   {metadata(method data for {method} {0x00007f81d3d048e0} 'equals' '(Ljava/lang/Object;)Z' in 'java/lang/String')}
  0x00007f81d4d3a3e2:   addq   $0x1,0x270(%rdi)
  0x00007f81d4d3a3ea:   nopl   0x0(%rax,%rax,1)
  0x00007f81d4d3a3ef:   callq  0x00007f81d47ee400           ; ImmutableOopMap {}
                                                            ;*invokestatic equals {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::equals@44 (line 1025)
                                                            ;   {static_call}
  0x00007f81d4d3a3f4:   and    $0x1,%eax
  0x00007f81d4d3a3f7:   add    $0x30,%rsp
  0x00007f81d4d3a3fb:   pop    %rbp
  0x00007f81d4d3a3fc:   mov    0x108(%r15),%r10
  0x00007f81d4d3a403:   test   %eax,(%r10)                  ;   {poll_return}
  0x00007f81d4d3a406:   retq                                ;*ireturn {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::equals@47 (line 1025)
  0x00007f81d4d3a407:   mov    $0x1,%eax
  0x00007f81d4d3a40c:   add    $0x30,%rsp
  0x00007f81d4d3a410:   pop    %rbp
  0x00007f81d4d3a411:   mov    0x108(%r15),%r10
  0x00007f81d4d3a418:   test   %eax,(%r10)                  ;   {poll_return}
  0x00007f81d4d3a41b:   retq   
  0x00007f81d4d3a41c:   movabs $0x7f81d3d048e0,%r10         ;   {metadata({method} {0x00007f81d3d048e0} 'equals' '(Ljava/lang/Object;)Z' in 'java/lang/String')}
  0x00007f81d4d3a426:   mov    %r10,0x8(%rsp)
  0x00007f81d4d3a42b:   movq   $0xffffffffffffffff,(%rsp)
  0x00007f81d4d3a433:   callq  0x00007f81d489e000           ; ImmutableOopMap {rsi=Oop rdx=Oop }
                                                            ;*synchronization entry
                                                            ; - java.lang.String::equals@-1 (line 1019)
                                                            ;   {runtime_call counter_overflow Runtime1 stub}
  0x00007f81d4d3a438:   jmpq   0x00007f81d4d3a134
  0x00007f81d4d3a43d:   mov    %rdx,(%rsp)
  0x00007f81d4d3a441:   callq  0x00007f81d489b320           ; ImmutableOopMap {rsi=Oop }
                                                            ;*checkcast {reexecute=0 rethrow=0 return_oop=0}
                                                            ; - java.lang.String::equals@15 (line 1023)
                                                            ;   {runtime_call throw_class_cast_exception Runtime1 stub}
  0x00007f81d4d3a446:   callq  0x00007f81d480afa0           ; ImmutableOopMap {rsi=Oop rdx=Oop }
                                                            ;*getfield coder {reexecute=1 rethrow=0 return_oop=0}
                                                            ; - (reexecute) java.lang.String::equals@30 (line 1024)
                                                            ;   {runtime_call throw_null_pointer_exception Runtime1 stub}
  0x00007f81d4d3a44b:   nop
  0x00007f81d4d3a44c:   nop
  0x00007f81d4d3a44d:   mov    0x3f0(%r15),%rax
  0x00007f81d4d3a454:   movabs $0x0,%r10
  0x00007f81d4d3a45e:   mov    %r10,0x3f0(%r15)
  0x00007f81d4d3a465:   movabs $0x0,%r10
  0x00007f81d4d3a46f:   mov    %r10,0x3f8(%r15)
  0x00007f81d4d3a476:   add    $0x30,%rsp
  0x00007f81d4d3a47a:   pop    %rbp
  0x00007f81d4d3a47b:   jmpq   0x00007f81d480be80           ;   {runtime_call unwind_exception Runtime1 stub}
[Stub Code]
  0x00007f81d4d3a480:   nopl   0x0(%rax,%rax,1)             ;   {no_reloc}
  0x00007f81d4d3a485:   movabs $0x0,%rbx                    ;   {static_stub}
  0x00007f81d4d3a48f:   jmpq   0x00007f81d4d3a48f           ;   {runtime_call}
[Exception Handler]
  0x00007f81d4d3a494:   callq  0x00007f81d489ad00           ;   {runtime_call handle_exception_from_callee Runtime1 stub}
  0x00007f81d4d3a499:   mov    %rsp,-0x28(%rsp)
  0x00007f81d4d3a49e:   sub    $0x80,%rsp
  0x00007f81d4d3a4a5:   mov    %rax,0x78(%rsp)
  0x00007f81d4d3a4aa:   mov    %rcx,0x70(%rsp)
  0x00007f81d4d3a4af:   mov    %rdx,0x68(%rsp)
  0x00007f81d4d3a4b4:   mov    %rbx,0x60(%rsp)
  0x00007f81d4d3a4b9:   mov    %rbp,0x50(%rsp)
  0x00007f81d4d3a4be:   mov    %rsi,0x48(%rsp)
  0x00007f81d4d3a4c3:   mov    %rdi,0x40(%rsp)
  0x00007f81d4d3a4c8:   mov    %r8,0x38(%rsp)
  0x00007f81d4d3a4cd:   mov    %r9,0x30(%rsp)
  0x00007f81d4d3a4d2:   mov    %r10,0x28(%rsp)
  0x00007f81d4d3a4d7:   mov    %r11,0x20(%rsp)
  0x00007f81d4d3a4dc:   mov    %r12,0x18(%rsp)
  0x00007f81d4d3a4e1:   mov    %r13,0x10(%rsp)
  0x00007f81d4d3a4e6:   mov    %r14,0x8(%rsp)
  0x00007f81d4d3a4eb:   mov    %r15,(%rsp)
  0x00007f81d4d3a4ef:   movabs $0x7f81f15ff3e2,%rdi         ;   {external_word}
  0x00007f81d4d3a4f9:   movabs $0x7f81d4d3a499,%rsi         ;   {internal_word}
  0x00007f81d4d3a503:   mov    %rsp,%rdx
  0x00007f81d4d3a506:   and    $0xfffffffffffffff0,%rsp
  0x00007f81d4d3a50a:   callq  0x00007f81f1108240           ;   {runtime_call}
  0x00007f81d4d3a50f:   hlt    
[Deopt Handler Code]
  0x00007f81d4d3a510:   movabs $0x7f81d4d3a510,%r10         ;   {section_word}
  0x00007f81d4d3a51a:   push   %r10
  0x00007f81d4d3a51c:   jmpq   0x00007f81d47ed0a0           ;   {runtime_call DeoptimizationBlob}
  0x00007f81d4d3a521:   hlt    
  0x00007f81d4d3a522:   hlt    
  0x00007f81d4d3a523:   hlt    
  0x00007f81d4d3a524:   hlt    
  0x00007f81d4d3a525:   hlt    
  0x00007f81d4d3a526:   hlt    
  0x00007f81d4d3a527:   hlt    
--------------------------------------------------------------------------------
```

