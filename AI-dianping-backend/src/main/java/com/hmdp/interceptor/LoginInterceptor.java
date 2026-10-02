package com.hmdp.interceptor;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpStatus;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {
    //ctrl+o可以选择重写的方法

    //在目标方法执行之前执行
    //返回值表示是否放行 true放行 false不放行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.判断ThreadLocal中有没有用户
        UserDTO user = UserHolder.getUser();
        // 2.没有，拦截，返回401状态码
        if(user == null){
            // 2.没有，拦截，返回401状态码
            response.setStatus(HttpStatus.HTTP_UNAUTHORIZED);
            return false;
        }
        UserHolder.saveUser(user);
        // 3.有，放行
        return true;
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 为了防止内存泄漏和线程复用时的数据污染
        UserHolder.removeUser();
    }
}

