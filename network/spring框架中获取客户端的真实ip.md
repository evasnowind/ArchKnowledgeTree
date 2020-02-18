题外话，前端也可以调用已有的接口获取ip，例如调用搜狐接口 [https://pv.sohu.com/cityjson?ie=utf-8](https://pv.sohu.com/cityjson?ie=utf-8)

在spring框架中，获取IP接口，则需要获取 HttpServletRequest 对象，该对象中包含了客户端请求的相关信息。

java代码如下：
```
/** 
 * @Description: 获取客户端IP地址   
 */  
private String getIpAddr(HttpServletRequest request) {   
    String ip = request.getHeader("x-forwarded-for");   
    if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {   
        ip = request.getHeader("Proxy-Client-IP");   
    }   
    if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {   
        ip = request.getHeader("WL-Proxy-Client-IP");   
    }   
    if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {   
        ip = request.getRemoteAddr();   
        if(ip.equals("127.0.0.1")){     
            //根据网卡取本机配置的IP     
            InetAddress inet=null;     
            try {     
                inet = InetAddress.getLocalHost();     
            } catch (Exception e) {     
                e.printStackTrace();     
            }     
            ip= inet.getHostAddress();     
        }  
    }   
    // 多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割  
    if(ip != null && ip.length() > 15){    
        if(ip.indexOf(",")>0){     
            ip = ip.substring(0,ip.indexOf(","));     
        }     
    }     
    return ip;   
}
```

注意，此处获取的第一个HTTP header字段`x-forwarded-for`，可能被伪造，具体参见这篇文章：[HTTP 请求头中的 X-Forwarded-For，X-Real-IP](https://www.cnblogs.com/diaosir/p/6890825.html)

对上述代码做改造，即可得到如下代码：
```
/** 
 * @Description: 获取客户端IP地址   
 */  
private String getIpAddr(HttpServletRequest request) {   
    String ip = request.getHeader("x-Real-IP");   
    if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {   
        ip = request.getHeader("Proxy-Client-IP");   
    }   
    if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {   
        ip = request.getHeader("WL-Proxy-Client-IP");   
    }   
    if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {   
        ip = request.getRemoteAddr();   
        if(ip.equals("127.0.0.1")){     
            //根据网卡取本机配置的IP     
            InetAddress inet=null;     
            try {     
                inet = InetAddress.getLocalHost();     
            } catch (Exception e) {     
                e.printStackTrace();     
            }     
            ip= inet.getHostAddress();     
        }  
    }   
    // 多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割  
    if(ip != null && ip.length() > 15){    
        if(ip.indexOf(",")>0){     
            ip = ip.substring(0,ip.indexOf(","));     
        }     
    }     
    return ip;   
}
```


# 参考资料
- [HTTP 请求头中的 X-Forwarded-For，X-Real-IP](https://www.cnblogs.com/diaosir/p/6890825.html)
- [spring框架中获取访问端的ip地址](https://blog.csdn.net/kioo_i_see/article/details/71630014)