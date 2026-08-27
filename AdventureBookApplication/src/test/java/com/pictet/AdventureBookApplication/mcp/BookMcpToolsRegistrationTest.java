package com.pictet.AdventureBookApplication.mcp;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class BookMcpToolsRegistrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldExposeBookToolsThroughMcp() {
        Map<String, ToolCallbackProvider> providers = applicationContext.getBeansOfType(ToolCallbackProvider.class);

        assertFalse(providers.isEmpty(), "Expected at least one MCP tool provider to be registered");

        List<String> toolNames = providers.values().stream()
            .flatMap(provider -> Arrays.stream(provider.getToolCallbacks()))
            .map(callback -> callback.getToolDefinition())
            .map(definition -> definition.name())
            .toList();

        assertTrue(toolNames.contains("listBooks"), "Expected listBooks tool to be registered");
        assertTrue(toolNames.contains("getBookDetails"), "Expected getBookDetails tool to be registered");
        assertTrue(toolNames.contains("validateBookJson"), "Expected validateBookJson tool to be registered");
    }

    @Test
    void getBookDetails_unknownId_throwsException() {
        BookMcpTools mcpTools = applicationContext.getBean(BookMcpTools.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> mcpTools.getBookDetails("unknown-book-id"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("No book found with id 'unknown-book-id'");
    }
}
