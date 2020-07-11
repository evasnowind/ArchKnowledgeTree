# limit
用于限制查询结果返回的数量。
示例：
```
select * from t limit 10;
```

# offset
跳过记录
```
# 跳过前20条记录，然后读取10条记录
select * from t limit 10 offset 20;
# 跳过前30条记录，然后读取40条记录
select * from t limit 30,40;
```