package com.pictet.AdventureBookApplication.mcp;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BookMcpServerConfig {

    @Bean
    public ToolCallbackProvider bookToolCallbackProvider(BookMcpTools bookMcpTools) {
        return MethodToolCallbackProvider.builder()
            .toolObjects(bookMcpTools)
            .build();
    }
}
