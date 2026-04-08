package com.clms.utils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.hutool.core.bean.BeanUtil;

public class DataContainerConvertor {
    private DataContainerConvertor() {
    }

    /**
     * Converts a Page of one type to a Page of another type.
     *
     * @param <S>         the type of the source elements
     * @param <T>         the type of the target elements
     * @param sourcePage  the source Page containing elements of type E
     * @param sourceClazz the Class object of the source type E
     * @param targetClazz the Class object of the target type T
     * @return a new Page containing elements of type T
     * @throws RuntimeException if an instance of the target type T cannot be
     *                          created
     */
    public static <S, T> Page<T> convertPage(Page<S> sourcePage, Class<S> sourceClazz, Class<T> targetClazz) {
        Page<T> newPage = new Page<>();
        BeanUtil.copyProperties(sourcePage, newPage, "records");
        List<T> newRecords = new ArrayList<>();
        try {
            Constructor<T> constructor = targetClazz.getConstructor(sourceClazz);
            for (S record : sourcePage.getRecords()) {
                T data = constructor.newInstance(record);
                newRecords.add(data);
            }
        } catch (Exception ex) {
            throw new RuntimeException("创建实例失败", ex);
        }
        newPage.setRecords(newRecords);
        return newPage;
    }

    /**
     * Converts a List of one type to a List of another type.
     *
     * @param <S>         the type of the source elements
     * @param <T>         the type of the target elements
     * @param sourceList  the source List containing elements of type E
     * @param sourceClazz the Class object of the source type E
     * @param targetClazz the Class object of the target type T
     * @return a new List containing elements of type T
     * @throws RuntimeException if an instance of the target type T cannot be
     *                          created
     */
    public static <S, T> List<T> convertList(List<S> sourceList, Class<S> sourceClazz, Class<T> targetClazz) {
        List<T> newList = new ArrayList<>();
        try {
            Constructor<T> constructor = targetClazz.getConstructor(sourceClazz);
            for (S record : sourceList) {
                T data = constructor.newInstance(record);
                newList.add(data);
            }
        } catch (Exception ex) {
            throw new RuntimeException("创建实例失败", ex);
        }
        return newList;
    }
}
