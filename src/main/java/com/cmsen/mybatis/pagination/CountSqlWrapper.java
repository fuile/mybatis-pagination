/*
 * +---------------------------------------------------------
 * | Author Jared.Yan<yanhuaiwen@163.com>
 * +---------------------------------------------------------
 * | Copyright (c) http://cmsen.com All rights reserved.
 * +---------------------------------------------------------
 */
package com.cmsen.mybatis.pagination;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CountSqlWrapper {
    private Number value;

    public CountSqlWrapper(Executor executor, MappedStatement ms, BoundSql boundSql) throws NoSuchFieldException, IllegalAccessException, SQLException {
        BoundSql countBoundSql = new BoundSql(ms.getConfiguration(), getBoundSql(boundSql.getSql()), boundSql.getParameterMappings(), boundSql.getParameterObject());
        Map<String, Object> additionalParameters = ObjectUtil.getAdditionalParameters(countBoundSql);
        for (String additionalParametersKey : additionalParameters.keySet()) {
            countBoundSql.setAdditionalParameter(additionalParametersKey, additionalParameters.get(additionalParametersKey));
        }
        List<ResultMap> resultMaps = new ArrayList<>();
        resultMaps.add(new ResultMap.Builder(ms.getConfiguration(), ms.getId(), Long.class, new ArrayList<>(0)).build());
        MappedStatement countMs = MappedStatementBuilder.build(ms, ms.getId() + "Count", ms.getSqlSource(), ms.getParameterMap(), resultMaps);
        CacheKey countCacheKey = executor.createCacheKey(countMs, countBoundSql.getParameterObject(), RowBounds.DEFAULT, countBoundSql);
        List<Object> countResultList = executor.query(countMs, countBoundSql.getParameterObject(), RowBounds.DEFAULT, null, countCacheKey, countBoundSql);
        this.value = countResultList != null && !countResultList.isEmpty() ? ((Number) countResultList.get(0)).longValue() : 0;
    }

    public Number getValue() {
        return value;
    }

    private String getBoundSql(String sql) {
        sql = sql.substring(0, sql.toUpperCase().indexOf(" ORDER BY"));
        sql = "SELECT COUNT(0) FROM (" + sql.trim() + ") AS total";
        return sql;
    }
}
