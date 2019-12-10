# 需求：xml中传入参数中包含一个list，需要在where中拼接in语句
假设查询person表，参数类型为XXXVo，XXXVo中包含一个List对象，保存了状态列表，此时可以参考如下查询

```
<select id="queryXXX" parameterType="XXXVo"
			resultMap="XXXResult">
		select *
		from person
		 WHERE 1=1
		<if test="statusFilter != null and statusFilter.size() > 0">
            and status in
            <foreach collection="statusFilter" item="statusId" index="i" open="(" close=")" separator=",">
                #{statusId}
            </foreach>
        </if>
		 ORDER BY DEPTID
	</select>
```

## 通过Map对象传递参数给xml
参数同样可以通过Map对象传递到xml这个层面，此时这样写即可：
java代码：
```
map.put(statusFilter, 列表实例对象);
```
xml代码：
```
<select id="queryXXX" parameterType="java.util.HashMap"
			resultMap="XXXResult">
		select *
		from person
		 WHERE 1=1
		<if test="statusFilter != null and statusFilter.size() > 0">
            and status in
            <foreach collection="statusFilter" item="statusId" index="i" open="(" close=")" separator=",">
                #{statusId}
            </foreach>
        </if>
		 ORDER BY DEPTID
	</select>
```

# 参考文章
- [myBatis的xml映射文件中传入list集合与数组做条件](https://blog.csdn.net/qq_15204179/article/details/1000425509)