# 需求：xml中需要在where中拼接like语句
## 方法1：concat
```
<where>
    <trim  suffixOverrides="," >
        <if test="id != null and id != ''" >
            and id =  #{id}
        </if>
        <if test="name != null and name != ''" >
            and name like concat('%',#{name},'%')
        </if>
    </trim>
</where>
```
## 方法2：${}
```
<if test="examTypeName!=null and examTypeName!=''">
    and exam_type_name like '%${examTypeName}%'
</if>
```
## 方法3：#{}
```
<if test="examTypeName!=null and examTypeName!=''">
    and exam_type_name like "%"#{examTypeName}"%"
</if>
```

# 参考文章
- [mybatis中xml开发like的几种写法](https://blog.csdn.net/xzj80927/article/details/90038411)