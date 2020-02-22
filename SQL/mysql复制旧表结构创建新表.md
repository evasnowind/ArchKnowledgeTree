## mysql复制旧表结构创建新表

1、复制表结构及数据到新表
`CREATE TABLE 新表SELECT * FROM 旧表`
这种方法会将oldtable中所有的内容都拷贝过来，当然我们可以用delete from newtable;来删除。
不过这种方法的一个最不好的地方就是新表中没有了旧表的primary key、Extra（auto_increment）等属性。需要自己用"alter"添加，而且容易搞错。

2、只复制表结构到新表
`CREATE TABLE 新表SELECT * FROM 旧表WHERE 1=2`
或CREATE TABLE 新表LIKE 旧表

3、复制旧表的数据到新表(假设两个表结构一样)
`INSERT INTO 新表SELECT * FROM 旧表`

4、复制旧表的数据到新表(假设两个表结构不一样)
`INSERT INTO 新表(字段1,字段2,…….) SELECT 字段1,字段2,…… FROM 旧表`

5、可以将表1结构复制到表2
`SELECT * INTO 表2 FROM 表1 WHERE 1=2`

6、可以将表1内容全部复制到表2
`SELECT * INTO 表2 FROM 表1`

7、 show create table 旧表;
这样会将旧表的创建命令列出。我们只需要将该命令拷贝出来，更改table的名字，就可以建立一个完全一样的表

8、mysqldump
用mysqldump将表dump出来，改名字后再导回去或者直接在命令行中运行


## 参考资料
- [MySQL复制旧表结构创建新表](https://blog.csdn.net/u012643122/article/details/52948460)