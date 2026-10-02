package com.hmdp.controller;


import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.service.IShopTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 超大王
 * @since 2025-09-18
 */
@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {

    private final IShopTypeService typeService;

    public ShopTypeController(IShopTypeService typeService) {
        this.typeService = typeService;
    }

    @GetMapping("list")
    public Result queryTypeList() {
        return typeService.queryShopType();
    }
}
