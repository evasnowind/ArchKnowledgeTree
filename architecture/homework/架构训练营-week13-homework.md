# 架构训练营week13课后作业

## 作业一：

（至少完成一个）

- 你所在的行业，常用的数据分析指标有哪些？请简述。
- Google 搜索引擎是如何对搜索结果进行排序的？（请用自己的语言描述 PageRank 算法。）



## 题目2：Google 搜索引擎是如何对搜索结果进行排序的？（请用自己的语言描述 PageRank 算法。）

 PageRank的计算充分利用了两个假设：数量假设和质量假设。步骤如下：
   1）在初始阶段：网页通过链接关系构建起Web图，每个页面设置相同的PageRank值，通过若干轮的计算，会得到每个页面所获得的最终PageRank值。随着每一轮的计算进行，网页当前的PageRank值会不断得到更新。

   2）在一轮中更新页面PageRank得分的计算方法：在一轮更新页面PageRank得分的计算中，每个页面将其当前的PageRank值平均分配到本页面包含的出链上，这样每个链接即获得了相应的权值。而每个页面将所有指向本页面的入链所传入的权值求和，即可得到新的PageRank得分。当每个页面都获得了更新后的PageRank值，就完成了一轮PageRank计算。计算公式可以参考如下

![page-rank-basic](D:\GitRepository\ArchKnowledgeTree\architecture\images\page-rank-basic.png)

其中L(B)表明B网页有多少指向其他网页的链接，PR(B)表示B的PageRank值。

但上面这样简单的计算有问题，因为存在一些出链为0，也就是那些不链接任何其他网页的页面。因此需要对 PageRank公式进行修正，即在简单公式的基础上增加了**阻尼系数（damping factor）**q， q一般取值q=0.85。 其意义是，在任意时刻，用户到达某页面后并继续向后浏览的概率。 1- q= 0.15就是用户停止点击，随机跳到新URL的概率。

因此，最终的计算公式如下。由于下面的算法，没有页面的PageRank会是0。所以，Google通过数学系统给了每个页面一个最小值。

![image-20201219152852455](..\images\page-rank-ver2.png)

所以一个页面的PageRank是由其他页面的PageRank计算得到。Google不断的重复计算每个页面的PageRank。如果给每个页面一个随机PageRank值（非0），那么经过不断的重复计算，这些页面的PR值会趋向于正常和稳定。这就是搜索引擎使用它的原因。