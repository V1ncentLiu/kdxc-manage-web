package com.kuaidao.manageweb.config;

import org.springframework.web.util.HtmlUtils;
import com.kuaidao.manageweb.util.XssUtil;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;


public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }
    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        if(value!=null){
            value = HtmlUtils.htmlEscape(value);
        }
        return value;
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return XssUtil.xssEncode(value);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values != null) {
            int length = values.length;
            String[] escapseValues = new String[length];
            for (int i = 0; i < length; i++) {
                escapseValues[i] = XssUtil.xssEncode(values[i]);
            }
            return escapseValues;
        }
        return super.getParameterValues(name);
    }
}
