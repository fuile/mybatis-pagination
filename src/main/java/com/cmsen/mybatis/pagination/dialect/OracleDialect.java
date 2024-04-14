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

public class OracleDialect implements Dialect {
    public OracleDialect() {
    }

    public String getPageSql(String sql, Map<String, Object> parameter, CacheKey cacheKey) {
        return "SELECT * FROM (SELECT PAGE.*, ROWNUM ROW_ID FROM (" + sql.trim() + ") PAGE) WHERE ROW_ID <= #{offset} AND ROW_ID > #{limit}";
    }
}
