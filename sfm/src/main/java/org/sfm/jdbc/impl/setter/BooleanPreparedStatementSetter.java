package org.sfm.jdbc.impl.setter;

import org.sfm.reflect.Setter;
import org.sfm.reflect.primitive.BooleanSetter;

import java.sql.PreparedStatement;

public class BooleanPreparedStatementSetter implements Setter<PreparedStatement, Boolean>, BooleanSetter<PreparedStatement> {

    private final int columnIndex;
    private final BooleanPreparedStatementIndexSetter setter = new BooleanPreparedStatementIndexSetter();

    public BooleanPreparedStatementSetter(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    @Override
    public void setBoolean(PreparedStatement target, boolean value) throws Exception {
        setter.setBoolean(target, value, columnIndex);
    }

    @Override
    public void set(PreparedStatement target, Boolean value) throws Exception {
        setter.set(target, value, columnIndex);
    }
}
