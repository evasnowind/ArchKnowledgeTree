# spring boot实现文件下载

## 代码
```
 /**
     * 稿源周报excel表格下载
     * @return
     */
 
    @RequestMapping(value = "/downExcel", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String downExcel(HttpServletResponse response) throws UnsupportedEncodingException {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(14);
        String filename = "稿源抓取周报-" + end.format(DateTimeFormatter.ISO_DATE) + ".xlsx";
        String filepath = "files/" + filename;
        writeExcelFile(start, end, filepath);
        // 如果文件名不为空，则进行下载
        if (filename != null) {
            File file = new File(filepath);
            // 如果文件存在，则进行下载
            if (file.exists()) {
                // 配置文件下载
                response.setHeader("content-type", "application/octet-stream");
                response.setContentType("application/octet-stream");
                // 下载文件能正常显示中文
                response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
                // 实现文件下载
                byte[] buffer = new byte[1024];
                FileInputStream fis = null;
                BufferedInputStream bis = null;
                try {
                    fis = new FileInputStream(file);
                    bis = new BufferedInputStream(fis);
                    OutputStream os = response.getOutputStream();
                    int i = bis.read(buffer);
                    while (i != -1) {
                        os.write(buffer, 0, i);
                        i = bis.read(buffer);
                    }
                    System.out.println("Download  successfully!");
                    return "successfully";
 
                } catch (Exception e) {
                    System.out.println("Download  failed!");
                    return "failed";
 
                } finally {
                    if (bis != null) {
                        try {
                            bis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return "";
    }
```

## 注意

前端不要使用AJAX请求去下载这个文件，否则可能出现`后台不抛出异常，也不出现下载的提示`的情况。

## 参考资料
- [SpringBoot实现文件下载](https://blog.csdn.net/zhangvalue/article/details/89387261)