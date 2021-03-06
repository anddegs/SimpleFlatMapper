package org.sfm.datastax.impl.setter;

import com.datastax.driver.core.SettableByIndexData;
import org.sfm.reflect.Setter;
import org.sfm.reflect.primitive.DoubleSetter;

public class DoubleSettableDataSetter implements Setter<SettableByIndexData, Double>, DoubleSetter<SettableByIndexData> {
    private final int index;

    public DoubleSettableDataSetter(int index) {
        this.index = index;
    }

    @Override
    public void setDouble(SettableByIndexData target, double value) throws Exception {
        target.setDouble(index, value);
    }

    @Override
    public void set(SettableByIndexData target, Double value) throws Exception {
        if (value == null) {
            target.setToNull(index);
        } else {
            target.setDouble(index, value);
        }
    }
}
