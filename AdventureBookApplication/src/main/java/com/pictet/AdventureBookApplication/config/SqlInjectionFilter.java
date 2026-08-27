package com.pictet.AdventureBookApplication.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class SqlInjectionFilter implements Filter {

    private static final Pattern SQL_PATTERN = Pattern.compile(
        "(?i)(exec\\s+(sp_col|sp_executesql|xp_cmdshell)|" +
        "union\\s+all\\s+select|" +
        "select\\s+.*\\s+from|" +
        "insert\\s+into|" +
        "delete\\s+from|" +
        "drop\\s+table|" +
        "update\\s+.*\\s+set|" +
        "or\\s+\\d+=\\d+)",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest) {
            String method = httpRequest.getMethod();
            if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method)) {
                Map<String, String[]> parameterMap = httpRequest.getParameterMap();
                for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                    for (String value : entry.getValue()) {
                        if (value != null && SQL_PATTERN.matcher(value).find()) {
                            HttpServletResponse httpResponse = (HttpServletResponse) response;
                            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            httpResponse.setContentType("application/json");
                            httpResponse.getWriter().write("{\"error\": \"Potential SQL Injection detected in request data.\"}");
                            return;
                        }
                    }
                }
            }
        }
        chain.doFilter(request, response);
    }
}
