package org.sfm.datastax.impl.getter;

import com.datastax.driver.core.GettableByIndexData;
import org.sfm.reflect.Getter;
import org.sfm.reflect.primitive.LongGetter;

public class DatastaxLongGetter implements LongGetter<GettableByIndexData>, Getter<GettableByIndexData, Long> {

    private final int index;

    public DatastaxLongGetter(int index) {
        this.index = index;
    }

    @Override
    public Long get(GettableByIndexData target) throws Exception {
        if (target.isNull(index)) {
            return null;
        }
        return getLong(target);
    }

    @Override
    public long getLong(GettableByIndexData target) throws Exception {
        return target.getLong(index);
    }
}
