/*
 * +---------------------------------------------------------
 * | Author Jared.Yan<yanhuaiwen@163.com>
 * +---------------------------------------------------------
 * | Copyright (c) http://cmsen.com All rights reserved.
 * +---------------------------------------------------------
 */
package com.cmsen.mybatis.pagination;

import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PageSqlWrapper {
    private Object value;

    public PageSqlWrapper(Executor executor, MappedStatement ms, ResultHandler resultHandler, RowBounds rowBounds, CacheKey cacheKey, BoundSql boundSql, Map<String, Object> parameterObject) throws NoSuchFieldException, IllegalAccessException, SQLException {
        Map<String, Object> additionalParameters = ObjectUtil.getAdditionalParameters(boundSql);
        SqlSourceBuilder sqlSourceBuilder = new SqlSourceBuilder(ms.getConfiguration());
        SqlSource sqlSource = sqlSourceBuilder.parse(getBoundSql(boundSql.getSql(), parameterObject), Object.class, additionalParameters);
        BoundSql newBoundSql = sqlSource.getBoundSql(parameterObject);
        List<ParameterMapping> parameterMappings = new LinkedList<>(boundSql.getParameterMappings());
        parameterMappings.addAll(newBoundSql.getParameterMappings());
        BoundSql pageBoundSql = new BoundSql(ms.getConfiguration(), newBoundSql.getSql(), parameterMappings, parameterObject);
        for (String additionalParametersKey : additionalParameters.keySet()) {
            if ("_parameter".equals(additionalParametersKey)) {
                pageBoundSql.setAdditionalParameter(additionalParametersKey, parameterObject);
                continue;
            }
            pageBoundSql.setAdditionalParameter(additionalParametersKey, additionalParameters.get(additionalParametersKey));
        }
        MappedStatement pageMs = MappedStatementBuilder.build(ms, ms.getId() + "Page", ms.getSqlSource(), ms.getParameterMap(), ms.getResultMaps());
        this.value = executor.query(pageMs, parameterObject, rowBounds, resultHandler, cacheKey, pageBoundSql);
    }

    public Object getValue() {
        return value;
    }

    private String getBoundSql(String sql, Map<String, Object> parameter) {
        sql = sql.trim();
        if (parameter.get("offset") != null && parameter.get("limit") != null) {
            return sql + " LIMIT #{offset}, #{limit}";
        } else if (parameter.get("offset") == null && parameter.get("limit") != null) {
            return sql + " LIMIT #{limit}";
        }
        return sql;
    }
}
