package com.example.lostoria.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

import static io.lettuce.core.pubsub.PubSubOutput.Type.message;

@Component
public class RateLimitFilter extends OncePerRequestFilter  {

    public static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        if("POST".equalsIgnoreCase(method)) {
            if (path.startsWith("/api/auth/login") && isRateLimited(request, "login", 5, Duration.ofMinutes(1))){
                sendRateLimitResponse(response, "Too many login requests. Please try again after 1 minute.", path);
                return;
            }else if (path.startsWith("/api/auth/register") && isRateLimited(request, "register", 3, Duration.ofMinutes(5))) {
                sendRateLimitResponse(response, "Too many registration attempts. Please try again later.", path);
                return;
            } else if ((path.equals("/api/lost-items") || path.equals("/api/found-items"))
                    && isRateLimited(request, "item-create", 10, Duration.ofMinutes(1))) {
                sendRateLimitResponse(response, "Rate limit exceeded for creating items. Slow down.", path);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isRateLimited(HttpServletRequest request, String action, long maxRequests, Duration window) {
        if(stringRedisTemplate == null) return false;
        try {
            String clientIp = getClientIp(request);
            String key = "ratelimit:" + action + ":" + clientIp;
            Long currentCount = stringRedisTemplate.opsForValue().increment(key);
            if (currentCount != null && currentCount == 1) {
                stringRedisTemplate.expire(key, window);
            }
            return currentCount != null && currentCount > maxRequests;
        } catch (Exception e) {
            log.warn("Redis error during rate limiting check. Degrading to permit request: {}", e.getMessage());
            return false;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if(xf != null && !xf.isBlank()){
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void sendRateLimitResponse(HttpServletResponse response, String meassage, String path) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", HttpStatus.TOO_MANY_REQUESTS.value(),
                "error", "Too Many Requests",
                "message", meassage,
                "path", path
        );
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

}
