package com.halo.khonsu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.halo.khonsu.entity.Type;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author chen
 * @since 2022-05-08
 */
public interface ITypeService extends IService<Type> {

    Page<Type> findPage(Page<Type> objectPage, String typename);
}
