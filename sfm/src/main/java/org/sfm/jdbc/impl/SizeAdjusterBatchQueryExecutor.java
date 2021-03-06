package org.sfm.jdbc.impl;

import com.mysql.jdbc.PacketTooBigException;
import org.sfm.utils.RowHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SizeAdjusterBatchQueryExecutor<T> implements BatchQueryExecutor<T> {
    private final BatchQueryExecutor<T> delegate;
    private final AtomicInteger batchSize = new AtomicInteger(Integer.MAX_VALUE);

    public SizeAdjusterBatchQueryExecutor(BatchQueryExecutor<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void insert(Connection connection, Collection<T> values, RowHandler<PreparedStatement> postExecute) throws SQLException {
        int lBatchSize = Math.min(batchSize.get(), values.size());
        try {
            if (values.size() <= lBatchSize) {
                delegate.insert(connection, values, postExecute);
            } else {
                splitBatches(connection, values, lBatchSize, postExecute);
            }
        } catch (PacketTooBigException e) {
            if (lBatchSize <= 2) {
                throw e;
            }
            resize(lBatchSize / 2);
            insert(connection, values, postExecute);
        }
    }

    private void resize(int lBatchSize) {
        int currentSize;
        do {
            currentSize = batchSize.get();
            if (lBatchSize >= currentSize) {
                break;
            }
        } while(batchSize.compareAndSet(currentSize, lBatchSize));
    }

    private void splitBatches(Connection connection, Collection<T> values, int lBatchSize, RowHandler<PreparedStatement> postExecute) throws SQLException {
        int batchNumber = 0;
        Iterator<T> it = values.iterator();
        List<T> list = new ArrayList<T>(lBatchSize);
        do {
            fillList(lBatchSize, it, list);
            delegate.insert(connection, list, postExecute);
            batchNumber ++;
        } while(batchNumber * lBatchSize < values.size());
    }

    private void fillList(int lBatchSize, Iterator<T> it, List<T> list) {
        list.clear();
        int i = 0;
        while (i < lBatchSize && it.hasNext()) {
            list.add(it.next());
            i++;
        }
    }
}
