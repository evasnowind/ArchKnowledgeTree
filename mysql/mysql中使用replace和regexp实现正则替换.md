mysql的正则匹配用regexp，而替换字符串用REPLACE(str,from_str,to_str)

例如
UPDATE myTable SET HTML=REPLACE(HTML,'<br>','') WHERE HTML REGEXP '(<br */*>\s*){2,}'

更多例子如下：

为了找出以“d”开头的名字，使用“^”匹配名字的开始：

SELECT * FROM master_data.md_employee WHERE name REGEXP '^d';

为了找出以“love”结尾的名字，使用“$”匹配名字的结尾：

SELECT id,name FROM master_data.md_employee WHERE name REGEXP 'love$';

为了找出包含一个“w”的名字，使用以下查询：

SELECT id,name FROM master_data.md_employee WHERE name REGEXP 'w';

为了找出包含正好5个字符的名字，使用“^”和“$”匹配名字的开始和结尾，和5个“.”实例在两者之间：


SELECT id,name FROM master_data.md_employee WHERE name REGEXP '^.....$';
或者：
SELECT id,name FROM master_data.md_employee WHERE name REGEXP '^.{5}$';


## 参考资料：
MySQL中使用replace、regexp进行正则表达式替换的用法分析
MySQL如何实现正则查找替换？