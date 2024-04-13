/*
 * +---------------------------------------------------------
 * | Author Jared.Yan<yanhuaiwen@163.com>
 * +---------------------------------------------------------
 * | Copyright (c) http://cmsen.com All rights reserved.
 * +---------------------------------------------------------
 */
package com.cmsen.mybatis.pagination;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMap;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlSource;

import java.util.List;

public class MappedStatementBuilder {
    public static MappedStatement build(MappedStatement ms, String id, SqlSource sqlSource, ParameterMap parameterMap, List<ResultMap> resultMaps) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), id, sqlSource, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.parameterMap(parameterMap);
        builder.resultMaps(resultMaps);
        builder.fetchSize(ms.getFetchSize());
        builder.timeout(ms.getTimeout());
        builder.statementType(ms.getStatementType());
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());
        builder.resultOrdered(ms.isResultOrdered());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
            builder.keyProperty(arrayToDelimitedString(ms.getKeyProperties()));
        }
        if (ms.getKeyColumns() != null && ms.getKeyColumns().length != 0) {
            builder.keyColumn(arrayToDelimitedString(ms.getKeyColumns()));
        }
        builder.databaseId(ms.getDatabaseId());
        builder.lang(ms.getLang());
        if (ms.getResultSets() != null && ms.getResultSets().length != 0) {
            builder.resultSets(arrayToDelimitedString(ms.getResultSets()));
        }
        return builder.build();
    }

    private static String arrayToDelimitedString(String[] strings) {
        StringBuilder keyProperty = new StringBuilder();
        if (strings != null && strings.length != 0) {
            for (String property : strings) {
                keyProperty.append(property).append(",");
            }
            keyProperty.delete(keyProperty.length() - 1, keyProperty.length());
        }
        return keyProperty.toString();
    }
}
