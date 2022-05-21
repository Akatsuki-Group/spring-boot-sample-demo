package com.github.lybgeek.dynamic.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.lybgeek.dynamic.model.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author lyb-geek
 * @since 2019-08-15
 */
@Mapper
public interface BookMapper extends BaseMapper<Book> {

  int updateStockById(@Param("id") Long id, @Param("count") Integer count);

}
