# Java中日期格式化yyyyMMdd和YYYYMMdd的区别

示例代码：


```
 public static void main(String[] args) {
        //YYYY 是表示：当天所在的周属于的年份，一周从周日开始，周六结束，只要本周跨年，那么这周就算入下一年。
        //2019-12-29至2020-1-4跨年周
        Calendar calendar = Calendar.getInstance();
        //2019-12-28
        calendar.set(2019, Calendar.DECEMBER, 28);  
        Date strDate1 = calendar.getTime(); 
        //2019-12-29
        calendar.set(2019, Calendar.DECEMBER, 29);  
        Date strDate2 = calendar.getTime(); 
        // 2019-12-31  
        calendar.set(2019, Calendar.DECEMBER, 31);  
        Date strDate3 = calendar.getTime();  
        // 2020-01-01  
        calendar.set(2020, Calendar.JANUARY, 1);  
        Date strDate4 = calendar.getTime();  
        
        DateFormat df1 = new SimpleDateFormat("yyyyMMdd");
        DateFormat df2 = new SimpleDateFormat("YYYYMMdd");
        //yyyyMMdd
        System.out.println("yyyyMMdd");
        System.out.println("2019-12-28: " + df1.format(strDate1)); 
        System.out.println("2019-12-29: " + df1.format(strDate2)); 
        System.out.println("2019-12-31: " + df1.format(strDate3));  
        System.out.println("2020-01-01: " + df1.format(strDate4));  
        //YYYYMMdd
        System.out.println("YYYYMMdd");
        System.out.println("2019-12-28: " + df2.format(strDate1));
        System.out.println("2019-12-29: " + df2.format(strDate2));
        System.out.println("2019-12-31: " + df2.format(strDate3));  
        System.out.println("2020-01-01: " + df2.format(strDate4));
    }
```

输出结果：
```
yyyyMMdd
2019-12-28: 20191228
2019-12-29: 20191229
2019-12-31: 20191231
2020-01-01: 20200101
YYYYMMdd
2019-12-28: 20191228
2019-12-29: 20201229
2019-12-31: 20201231
2020-01-01: 20200101
```

原因：
YYYY是week-based-year，表示：当天所在的周属于的年份，一周从周日开始，周六结束，只要本周跨年，那么这周就算入下一年。所以2019年12月31日那天在这种表述方式下就已经 2020 年了。而当使用yyyy的时候，就还是 2019 年。

相关说明：
https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#patterns
https://nakedsecurity.sophos.com/2019/12/23/serious-security-the-decade-ending-y2k-bug-that-wasnt/