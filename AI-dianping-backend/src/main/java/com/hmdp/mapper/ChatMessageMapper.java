package com.hmdp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmdp.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    @Select("SELECT customer_user_id FROM tb_chat_message WHERE shop_id = #{shopId} " +
            "GROUP BY customer_user_id ORDER BY MAX(id) DESC")
    List<Long> findCustomerIdsByShopId(Long shopId);
}
