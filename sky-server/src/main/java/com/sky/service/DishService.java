package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;

import java.util.List;

public interface DishService {
    /**
     * 新增菜品和口味
     * @param dishDTO
     */
    public void saveWithFlavor(DishDTO dishDTO);


    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 批量删除菜品
     * @param dishIds
     * @return
     */
    void deleteBatch(List<Long> dishIds);
}
