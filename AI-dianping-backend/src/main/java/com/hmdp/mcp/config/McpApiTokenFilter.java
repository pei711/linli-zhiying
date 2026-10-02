package com.hmdp.mcp.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Protects the separately registered MCP servlet, which does not pass through MVC interceptors. */
@Component
public class McpApiTokenFilter extends OncePerRequestFilter {

    private static final String MCP_PATH = "/mcp";
    private final String expectedToken;

    public McpApiTokenFilter(org.springframework.core.env.Environment environment) {
        this.expectedToken = environment.getProperty("app.mcp.api-token", "");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !(MCP_PATH.equals(path) || path.startsWith(MCP_PATH + "/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!StringUtils.hasText(expectedToken)) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "MCP is disabled; configure MCP_API_TOKEN to enable it");
            return;
        }

        String authorization = request.getHeader("Authorization");
        String prefix = "Bearer ";
        if (authorization == null || !authorization.startsWith(prefix)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "A bearer token is required");
            return;
        }

        byte[] supplied = authorization.substring(prefix.length()).getBytes(StandardCharsets.UTF_8);
        byte[] expected = expectedToken.getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expected, supplied)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid bearer token");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
