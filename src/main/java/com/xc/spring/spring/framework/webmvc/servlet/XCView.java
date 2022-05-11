package com.xc.spring.spring.framework.webmvc.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.RandomAccess;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 具体页面
 *
 * @author lichao chao.li07@hand-china.com 4/24/22 6:15 PM
 */
public class XCView {

    private File viewFile;

    public XCView(File templateFile) {
        this.viewFile = templateFile;
    }

    /*
     * 输出页面，渲染方法
     */
    public void render(Map<String,?> model, HttpServletRequest req, HttpServletResponse resp) throws Exception{
        //利用StringBuffer读取页面文件
        StringBuffer sb = new StringBuffer();
        //高性能文件流
        RandomAccessFile ra = new RandomAccessFile(this.viewFile, "r");
        //逐行读取
        String line = null;
        while(null != (line = ra.readLine())){
            line = new String(line.getBytes("ISO-8859-1"),"utf-8");
            //利用正则替换页面文件中的占位符表达式
            Pattern pattern = Pattern.compile("￥\\{[^\\}]+\\}",Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(line);
            while(matcher.find()){
                String paramName = matcher.group();
                //将 "{" 或者 "}" 替换成 空 - 拿到文件中的E表达式占位符
                paramName = paramName.replaceAll("￥\\{|\\}","");
                //获取占位符中参数对应ModelAndView中的值
                Object paramValue = model.get(paramName);
                line = matcher.replaceFirst(makeStringForRegExp(paramValue.toString()));
                matcher = pattern.matcher(line);
            }
            sb.append(line);
        }
        resp.setCharacterEncoding("utf-8");
        resp.getWriter().write(sb.toString());
    }

    //处理页面文件中的特殊字符
    public static String makeStringForRegExp(String str) {
        return str.replace("\\", "\\\\").replace("*", "\\*")
                .replace("+", "\\+").replace("|", "\\|")
                .replace("{", "\\{").replace("}", "\\}")
                .replace("(", "\\(").replace(")", "\\)")
                .replace("^", "\\^").replace("$", "\\$")
                .replace("[", "\\[").replace("]", "\\]")
                .replace("?", "\\?").replace(",", "\\,")
                .replace(".", "\\.").replace("&", "\\&");
    }

}
