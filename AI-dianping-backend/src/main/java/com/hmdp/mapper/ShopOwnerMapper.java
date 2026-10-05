package com.hmdp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ShopOwnerMapper {
    @Select("SELECT user_id FROM tb_shop_owner WHERE shop_id = #{shopId}")
    Long findOwnerId(Long shopId);
}
