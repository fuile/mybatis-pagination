/*
 * +---------------------------------------------------------
 * | Author Jared.Yan<yanhuaiwen@163.com>
 * +---------------------------------------------------------
 * | Copyright (c) http://cmsen.com All rights reserved.
 * +---------------------------------------------------------
 */
package com.cmsen.mybatis.pagination;

import java.io.Closeable;
import java.io.IOException;

public class PaginationHelper implements Closeable {
    protected static final ThreadLocal<DefaultPagination> LOCAL = new ThreadLocal<>();

    public static DefaultPagination get() {
        DefaultPagination defaultPagination = LOCAL.get();
        clear();
        return defaultPagination;
    }

    public static void set(DefaultPagination pagination) {
        LOCAL.set(pagination);
    }

    public static DefaultPagination of(int offset, int limit) {
        DefaultPagination dp = DefaultPagination.of(offset, limit);
        set(dp);
        return dp;
    }

    public static void clear() {
        LOCAL.remove();
    }

    @Override
    public void close() throws IOException {
        PaginationHelper.clear();
    }
}
