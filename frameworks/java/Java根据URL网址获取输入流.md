# java根据URL获取输入流以及下载文件

代码取自：[JAVA根据URL网址获取输入流](https://blog.csdn.net/u012012240/article/details/85078929)
```
/**
 * 根据地址获得数据的输入流
 * @param strUrl 网络连接地址
 * @return url的输入流
 */
    public static InputStream getInputStreamByUrl(String strUrl){
        HttpURLConnection conn = null;
        try {
            URL url = new URL(strUrl);
            conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(20 * 1000);
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            IOUtils.copy(conn.getInputStream(),output);
            return  new ByteArrayInputStream(output.toByteArray());
        } catch (Exception e) {
            logger.error(e+"");
        }finally {
            try{
                if (conn != null) {
                    conn.disconnect();
                }
            }catch (Exception e){
                logger.error(e+"");
            }
        }
        return null;
    }
```

如若想下载文件，则可以在上面方法基础上，进一步封装即可：
```
public ResponseEntity<byte[]> downloadFileByUrl(String downUrl, String fileName, String contentType, String contentDisposition) throws IOException {
        InputStream inputStream = getInputStreamByUrl(downUrl);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, byteArrayOutputStream);
        inputStream.close();
        HttpHeaders headers = new HttpHeaders();
        //设置下载协议头。下面fileName的两次转换主要是为了保证中文文件名
        fileName = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, contentDisposition + ";filename=" + new String(fileName.getBytes(), "utf-8"));
        headers.set(HttpHeaders.CONTENT_TYPE, contentType);
        ResponseEntity<byte[]> responseEntity = new ResponseEntity<byte[]>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
        byteArrayOutputStream.close();
        return responseEntity;
    }
```

其中`Content-Type`可以用MediaType中的常量，自行控制；`Content-Disposition`则看需求，如果需要直接打开文件，则传"inline"，若是想下载则传"attachment"。