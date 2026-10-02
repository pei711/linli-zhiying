package com.hmdp.mapper;

import com.hmdp.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 超大王
 * @since 2025-09-15
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {


    User selectByPhone(String phone);

    void save(User user);
}
