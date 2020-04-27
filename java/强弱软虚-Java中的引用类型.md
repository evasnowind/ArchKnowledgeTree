
GC调优经验

面试官：实际调优场景？
频繁Full GC 、频繁OOM？
小米出过的问题：C++开发人员，重写finalize方法，导致了频繁gc
——耗时操作放在了finalize  ，比如网络IO，相当于延长了对象生命周期



## 软应用
SoftReference<byte[]> m = new SoftReference<>(new byte[1024 * 1024]);

m -> Sf -> byte
1、m -> sf 强引用
2、sf -> byte 软

