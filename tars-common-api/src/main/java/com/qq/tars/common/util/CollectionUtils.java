package com.qq.tars.common.util;

import java.util.Collection;

public class CollectionUtils {
    private CollectionUtils() {

    }

    public static boolean isEmpty(Collection<?> collction) {
        if (collction == null || collction.isEmpty()) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public static boolean isNotEmpty(Collection<?> collction) {
        return !isEmpty(collction);
    }




}
