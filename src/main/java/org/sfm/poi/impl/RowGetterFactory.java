package org.sfm.poi.impl;


import org.apache.poi.ss.usermodel.Row;
import org.sfm.csv.CsvColumnKey;
import org.sfm.map.ColumnDefinition;
import org.sfm.map.GetterFactory;
import org.sfm.reflect.Getter;
import org.sfm.reflect.TypeHelper;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class RowGetterFactory implements GetterFactory<Row, CsvColumnKey> {


    private static final Map<Class<?>, GetterFactory<Row, CsvColumnKey>> getterFactories = new HashMap<Class<?>, GetterFactory<Row, CsvColumnKey>>();
    static {
        getterFactories.put(String.class, new GetterFactory<Row, CsvColumnKey>() {
            @Override
            public <P> Getter<Row, P> newGetter(Type target, CsvColumnKey key, ColumnDefinition<?, ?> columnDefinition) {
                return (Getter<Row, P>) new PoiStringGetter(key.getIndex());
            }
        });
        getterFactories.put(Date.class, new GetterFactory<Row, CsvColumnKey>() {
            @Override
            public <P> Getter<Row, P> newGetter(Type target, CsvColumnKey key, ColumnDefinition<?, ?> columnDefinition) {
                return (Getter<Row, P>) new PoiDateGetter(key.getIndex());
            }
        });

        getterFactories.put(Byte.class, new GetterFactory<Row, CsvColumnKey>() {
            @Override
            public <P> Getter<Row, P> newGetter(Type target, CsvColumnKey key, ColumnDefinition<?, ?> columnDefinition) {
                return (Getter<Row, P>) new PoiByteGetter(key.getIndex());
            }
        });

        getterFactories.put(Character.class, new GetterFactory<Row, CsvColumnKey>() {
            @Override
            public <P> Getter<Row, P> newGetter(Type target, CsvColumnKey key, ColumnDefinition<?, ?> columnDefinition) {
                return (Getter<Row, P>) new PoiCharacterGetter(key.getIndex());
            }
        });
        getterFactories.put(Short.class, new GetterFactory<Row, CsvColumnKey>() {
            @Override
            public <P> Getter<Row, P> newGetter(Type target, CsvColumnKey key, ColumnDefinition<?, ?> columnDefinition) {
                return (Getter<Row, P>) new PoiShortGetter(key.getIndex());
            }
        });
        getterFactories.put(Integer.class, new GetterFactory<Row, CsvColumnKey>() {
            @Override
            public <P> Getter<Row, P> newGetter(Type target, CsvColumnKey key, ColumnDefinition<?, ?> columnDefinition) {
                return (Getter<Row, P>) new PoiIntegerGetter(key.getIndex());
            }
        });
        getterFactories.put(Long.class, new GetterFactory<Row, CsvColumnKey>() {
            @Override
            public <P> Getter<Row, P> newGetter(Type target, CsvColumnKey key, ColumnDefinition<?, ?> columnDefinition) {
                return (Getter<Row, P>) new PoiLongGetter(key.getIndex());
            }
        });

        getterFactories.put(Float.class, new GetterFactory<Row, CsvColumnKey>() {
            @Override
            public <P> Getter<Row, P> newGetter(Type target, CsvColumnKey key, ColumnDefinition<?, ?> columnDefinition) {
                return (Getter<Row, P>) new PoiFloatGetter(key.getIndex());
            }
        });

        getterFactories.put(Double.class, new GetterFactory<Row, CsvColumnKey>() {
            @Override
            public <P> Getter<Row, P> newGetter(Type target, CsvColumnKey key, ColumnDefinition<?, ?> columnDefinition) {
                return (Getter<Row, P>) new PoiDoubleGetter(key.getIndex());
            }
        });

        getterFactories.put(byte.class, getterFactories.get(Byte.class));
        getterFactories.put(char.class, getterFactories.get(Character.class));
        getterFactories.put(short.class, getterFactories.get(Short.class));
        getterFactories.put(int.class, getterFactories.get(Integer.class));
        getterFactories.put(long.class, getterFactories.get(Long.class));
        getterFactories.put(float.class, getterFactories.get(Float.class));
        getterFactories.put(double.class, getterFactories.get(Double.class));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <P> Getter<Row, P> newGetter(Type target, CsvColumnKey key, ColumnDefinition<?, ?> columnDefinition) {

        Class<?> targetClass = TypeHelper.toClass(target);

        final GetterFactory<Row, CsvColumnKey> rowGetterFactory = getterFactories.get(targetClass);

        if (rowGetterFactory != null) {
            return rowGetterFactory.newGetter(target, key, columnDefinition);
        }

        return null;
    }
}
