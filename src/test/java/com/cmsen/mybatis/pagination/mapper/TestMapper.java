/*
 * +---------------------------------------------------------
 * | Author Jared.Yan<yanhuaiwen@163.com>
 * +---------------------------------------------------------
 * | Copyright (c) http://cmsen.com All rights reserved.
 * +---------------------------------------------------------
 */
package com.cmsen.mybatis.pagination.mapper;

import com.cmsen.mybatis.pagination.Limit;
import com.cmsen.mybatis.pagination.Offset;
import com.cmsen.mybatis.pagination.PaginationInterceptorTest;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TestMapper {
    List<Object> select();

    List<Object> select(PaginationInterceptorTest.Page page);

    List<Object> select(Object o, PaginationInterceptorTest.Page page);

    List<Object> select(@Offset int offset, @Limit int limit);
}
