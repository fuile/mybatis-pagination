/*
 * +---------------------------------------------------------
 * | Author Jared.Yan<yanhuaiwen@163.com>
 * +---------------------------------------------------------
 * | Copyright (c) http://cmsen.com All rights reserved.
 * +---------------------------------------------------------
 */
package com.cmsen.mybatis.pagination;

import org.apache.ibatis.cache.CacheKey;

import java.util.Map;

public interface Dialect {
    default String getCountSql(String sql) {
        sql = sql.substring(0, sql.toUpperCase().indexOf(" ORDER BY"));
        sql = "SELECT COUNT(0) FROM (" + sql.trim() + ") AS total";
        return sql;
    }

    String getPageSql(String sql, Map<String, Object> parameter, CacheKey cacheKey);
}
