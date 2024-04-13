/*
 * +---------------------------------------------------------
 * | Author Jared.Yan<yanhuaiwen@163.com>
 * +---------------------------------------------------------
 * | Copyright (c) http://cmsen.com All rights reserved.
 * +---------------------------------------------------------
 */
package com.cmsen.mybatis.pagination;

public class DefaultPagination extends AbstractPagination {
    public DefaultPagination(int offset, int limit) {
        this.offset = offset;
        this.limit = limit;
    }

    public static DefaultPagination of(int offset, int limit) {
        return new DefaultPagination(offset, limit);
    }
}
