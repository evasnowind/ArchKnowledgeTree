[TOC]

## 查看相关

### k8s集群各种对象的运行情况
使用`kubectl get`，不加任何参数，可以获得`kubectl get`支持的所有类型。`kubectl describe`类似。
- 查看节点：`kubectl get nodes`
- 查看pods：`kubectl get pods`
- 查看服务：`kubectl get services`
- 查看ReplicationController：`kubectl get replicationcontrollers`


### 查看对象更多信息
`kubectl describe 对象类型 对象id`
例如：`kubectl describe node node-id`


### 查看应用程序日志
docker中查看日志命令
`docker logs container-id`

k8s中获取pod日志：
`kubectl logs pod-id`
每天/每次日志文件达到10MB大小时，容器日志将会自动轮替，kubectl logs仅展示轮替后的日志。

若pod中包含多个容器，运行kubectl logs时必须通过`-c 容器名称`指定容器名称，即：
`kubectl logs pod-id -c container-id`

当pod被删除，日志也将被删除。想保留需持久化到日志系统中。

### 向pod发送请求
本地网路端口转发到pod中的端口
`kubectl port-forward pod-id 本地端口:pod端口`

## 标签相关
可以组织pod，以及其他类型的k8s资源

### 查看pod信息（带上标签）
`kubectl get pod --show-labels`

### 查看pod信息，仅列出某些标签信息
`kubectl get pod -L 标签名称1,标签名称2`

### 修改现有pod的标签
添加label
`kubectl label pod pod-id 标签名=标签值`
更改现有标签的值
`kubectl label pod pod-id 标签名=新的标签值 --override`

### 利用标签筛选器筛选pod
筛选带有某个标签值的pod
`kubectl get pod -l 标签名=标签值`
包含某个标签的pod
`kubectl get pod -l 标签值`
没有某个标签的pod
`kubectl get pod -l '!标签值'`

pod的其他选择器：
- `tagXXX!=YYY` 有标签tagXXX，但不是YYY
- `tagXXX in (a, b)` tagXXX的值是a或是b
- `tagXXX notin (a, b)` 有标签tagXXX, 但不能是a或是b

可以使用多个条件，用逗号隔开即可。

### 使用标签和选择器约束pod调度
用标签分类工作节点，比如加速gpu计算的节点，加上标签
`kubectl label node XX-node gpu=true`
创建pod的yaml文件中添加筛选条件：
```
apiVersion: v1
kind: Pod
metadata:
    name:xxx
spec:
    nodeSelector:
        gpu:"true"
……
```

这样做也可以将pod调度到某个特定节点。

## 命名空间相关
列出kube-system空间下的对象
`kubectl get pod --namespace kube-system`

创建命名空间
方法1：从YAML文件创建
假设文件custom-ns.yaml内容
```
apiVersion: v1
kind: Namespace #表明在定义命名空间
metadata:
    name: custom-ns # 命名空间名称
```
然后提交给k8s
`kubectl create -f custom-ns.yaml`

方法2：用命令创建
`kubectl create namespace custom-ns`

创建资源时可以指定命名空间：
`kubectl create -f xxx.yaml -n custom-ns`

## pod相关
删除pod
`kubectl delete pod xxx`
需要删除多个时，用空格分隔即可，即：
`kubectl delete pod xxx yyy`
通过标签选择器删除
`kubectl delete pod -l tagXXX=`
通过删除整个命名空间来删除pod
`kubectl delete ns ns-name`
删除命名空间所有pod，但保留命名空间
`kubectl delete po --all`
——注意pod是创建ReplicationController对象、利用ReplicationController创建的pod，则执行这个删除操作后，可能会有额外的pod，若要彻底清空，需要删除ReplicationController。

删除命名空间内的所有资源，包括ReplicationController、pod、service等
`kubectl delete all --all`

