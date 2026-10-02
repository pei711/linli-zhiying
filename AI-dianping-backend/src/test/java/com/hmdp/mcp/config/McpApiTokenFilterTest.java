package com.hmdp.mcp.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McpApiTokenFilterTest {

    @Test
    void disablesMcpWhenNoTokenIsConfigured() throws Exception {
        McpApiTokenFilter filter = new McpApiTokenFilter(new MockEnvironment());
        MockHttpServletResponse response = invoke(filter, null, new MockFilterChain());
        assertEquals(503, response.getStatus());
    }

    @Test
    void rejectsMissingOrIncorrectBearerToken() throws Exception {
        McpApiTokenFilter filter = new McpApiTokenFilter(
                new MockEnvironment().withProperty("app.mcp.api-token", "test-secret"));
        assertEquals(401, invoke(filter, null, new MockFilterChain()).getStatus());
        assertEquals(401, invoke(filter, "Bearer wrong", new MockFilterChain()).getStatus());
    }

    @Test
    void allowsCorrectBearerToken() throws Exception {
        McpApiTokenFilter filter = new McpApiTokenFilter(
                new MockEnvironment().withProperty("app.mcp.api-token", "test-secret"));
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletResponse response = invoke(filter, "Bearer test-secret", chain);
        assertEquals(200, response.getStatus());
        assertTrue(chain.getRequest() != null);
    }

    private MockHttpServletResponse invoke(McpApiTokenFilter filter, String authorization,
                                          MockFilterChain chain) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/mcp");
        if (authorization != null) {
            request.addHeader("Authorization", authorization);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);
        return response;
    }
}
