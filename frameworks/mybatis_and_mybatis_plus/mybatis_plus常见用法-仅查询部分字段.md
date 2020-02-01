mybatis-plus select查询语句默认是查全部字段，有两种方法可以指定要查询的字段

假定表结构如下：
```
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `name` varchar(30) DEFAULT NULL COMMENT '姓名',
  `age` int(11) DEFAULT NULL COMMENT '年龄',
  `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
  `manager_id` bigint(20) DEFAULT NULL COMMENT '直属上级id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

假设目前仅需要查询name,age两个字段。

### 方法1：只需要查询出name和age两个字段:使用queryWrapper的select()方法指定要查询的字段
```
    @Test
    public void selectByWrapper1() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("name", "age").like("name", "雨");
        List<User> users = userMapper.selectList(queryWrapper);
        users.forEach(System.out::println);
    }
```

### 方法2：查询出除manager_id和create_time外其它所有字段的数据：同样使用queryWrapper的select()方法
```
    @Test
    public void selectByWrapper2() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select(User.class, info -> !info.getColumn().equals("manager_id")
                && !info.getColumn().equals("create_time"));
        List<User> users = userMapper.selectList(queryWrapper);
        users.forEach(System.out::println);
    }
```

### 参考资料
- [Mybatis-Plus select不列出全部字段](https://www.jianshu.com/p/e97b8236db67)