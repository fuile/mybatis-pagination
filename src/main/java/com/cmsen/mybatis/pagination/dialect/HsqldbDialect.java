/*
 * +---------------------------------------------------------
 * | Author Jared.Yan<yanhuaiwen@163.com>
 * +---------------------------------------------------------
 * | Copyright (c) http://cmsen.com All rights reserved.
 * +---------------------------------------------------------
 */
package com.cmsen.mybatis.pagination.dialect;

import com.cmsen.mybatis.pagination.Dialect;
import com.cmsen.mybatis.pagination.ObjectUtil;
import org.apache.ibatis.cache.CacheKey;

import java.util.Map;

public class HsqldbDialect implements Dialect {
    public HsqldbDialect() {
    }

    public String getPageSql(String sql, Map<String, Object> parameter, CacheKey cacheKey) {
        sql = sql.trim();
        Number offset = ObjectUtil.get(parameter.getOrDefault("offset", 0));
        Number limit = ObjectUtil.get(parameter.getOrDefault("limit", 0));
        if (limit.longValue() > 0L) {
            sql += " LIMIT #{limit}";
        }
        if (offset.longValue() > 0L) {
            sql += " OFFSET #{offset}";
        }
        return sql;
    }
}
