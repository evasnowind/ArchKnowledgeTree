# HTTP中Content-Disposition与Content-Type的作用

## 基本作用

- Content-Disposition 控制文件在浏览器中直接打开还是下载  
- Content-Type 定义网络文件的类型和网页的编码。

## 取值

### Content-Disposition

常用的有两种取值：
- `inline`:  将文件内容直接显示在页面  
- `attachment`: 弹出对话框让用户下载具体例子

`inline`的java程序例子：
```
File file = new File("rfc1806.txt");  
String filename = file.getName();  
response.setHeader("Content-Type","text/plain");  
response.addHeader("Content-Disposition","inline;filename=" + new String(filename.getBytes(),"utf-8"));  
response.addHeader("Content-Length","" + file.length());  
```
当然，也可以通过使用HttpHeaders中的常量（如CONTENT_DISPOSITION等常量）来替代手写"Content-Disposition"，修改如下：
```
File file = new File("rfc1806.txt");  
String filename = file.getName();  
response.setHeader(HttpHeaders.CONTENT_TYPE,"text/plain");  
response.addHeader(HttpHeaders.CONTENT_DISPOSITION,"inline;filename=" + new String(filename.getBytes(),"utf-8"));  
response.addHeader(HttpHeaders.CONTENT_LENGTH,"" + file.length());  
```

类似的，`attachment`例子：
```
File file = new File("rfc1806.txt");  
String filename = file.getName();  
response.setHeader("Content-Type","text/plain");  
response.addHeader("Content-Disposition","attachment;filename=" + new String(filename.getBytes(),"utf-8"));  
response.addHeader("Content-Length","" + file.length());  
```

### Content-Type

Content-Type的取值范围即MIME类型。可以参考此处[HTTP content-type](https://www.runoob.com/http/http-content-type.html)

在java中设置Content-Type的方法参见上面的例子。

## 参考资料
- [HTTP content-type](https://www.runoob.com/http/http-content-type.html)
- [Content-Disposition 响应头，设置文件在浏览器打开还是下载](https://blog.csdn.net/ssssny/article/details/77717287)