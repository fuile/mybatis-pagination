/*
 * +---------------------------------------------------------
 * | Author Jared.Yan<yanhuaiwen@163.com>
 * +---------------------------------------------------------
 * | Copyright (c) http://cmsen.com All rights reserved.
 * +---------------------------------------------------------
 */
package com.cmsen.mybatis.pagination.dialect;

import com.cmsen.mybatis.pagination.Dialect;
import org.apache.ibatis.cache.CacheKey;

import java.util.Map;

public class SqlServerDialect implements Dialect {
    public SqlServerDialect() {
    }

    public String getPageSql(String sql, Map<String, Object> parameter, CacheKey cacheKey) {
        return sql.trim() + " OFFSET #{offset} ROWS FETCH NEXT #{limit} ROWS ONLY";
    }
}
