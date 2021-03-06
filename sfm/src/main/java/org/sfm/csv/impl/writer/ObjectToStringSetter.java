package org.sfm.csv.impl.writer;

import org.sfm.reflect.Getter;
import org.sfm.reflect.Setter;

public class ObjectToStringSetter<P> implements Setter<Appendable, P> {
    private final Getter<P, ?> getter;

    public ObjectToStringSetter(Getter<P, ?> getter) {
        this.getter = getter;
    }

    @Override
    public void set(Appendable target, P value) throws Exception {
        final Object o = getter.get(value);
        if (o != null) {
            target.append(String.valueOf(o));
        }
    }
}
