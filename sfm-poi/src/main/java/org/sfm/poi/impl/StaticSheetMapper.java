package org.sfm.poi.impl;


import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.sfm.map.*;
import org.sfm.map.context.MappingContextFactory;
import org.sfm.poi.RowMapper;
import org.sfm.utils.RowHandler;

import java.util.Iterator;

//IFJAVA8_START
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
//IFJAVA8_END

public class StaticSheetMapper<T> implements RowMapper<T> {

    private final Mapper<Row, T> mapper;
    private final int startRow = 0;

    private final RowHandlerErrorHandler rowHandlerErrorHandler;
    private final MappingContextFactory<? super Row> mappingContextFactory;

    public StaticSheetMapper(Mapper<Row, T> mapper, RowHandlerErrorHandler rowHandlerErrorHandler, MappingContextFactory<? super Row> mappingContextFactory) {
        this.mapper = mapper;
        this.rowHandlerErrorHandler = rowHandlerErrorHandler;
        this.mappingContextFactory = mappingContextFactory;
    }

    @Override
    public Iterator<T> iterator(Sheet sheet) {
        return iterator(startRow, sheet);
    }

    @Override
    public Iterator<T> iterator(int startRow, Sheet sheet) {
        return new SheetIterator<T>(this, startRow, sheet, newMappingContext());
    }

    @Override
    public <RH extends RowHandler<T>> RH forEach(Sheet sheet, RH rowHandler) {
        return forEach(startRow, sheet, rowHandler);
    }

    @Override
    public <RH extends RowHandler<T>> RH forEach(int startRow, Sheet sheet, RH rowHandler) {
        MappingContext<? super Row> mappingContext = newMappingContext();
        Mapper<Row, T> lMapper = this.mapper;
        for(int rowNum = startRow; rowNum <= sheet.getLastRowNum(); rowNum++) {
            T object = lMapper.map(sheet.getRow(rowNum), mappingContext);
            try {
                rowHandler.handle(object);
            } catch(Exception e) {
                rowHandlerErrorHandler.handlerError(e, object);
            }
        }
        return rowHandler;
    }

    //IFJAVA8_START
    @Override
    public Stream<T> stream(Sheet sheet) {
        return stream(startRow, sheet);
    }

    @Override
    public Stream<T> stream(int startRow, Sheet sheet) {
        return StreamSupport.stream(new SheetSpliterator<T>(this, startRow, sheet, newMappingContext()), false);
    }
    //IFJAVA8_END


    @Override
    public T map(Row source) throws MappingException {
        return mapper.map(source);
    }

    @Override
    public T map(Row source, MappingContext<? super Row> context) throws MappingException {
        return mapper.map(source, context);
    }

    @Override
    public void mapTo(Row source, T target, MappingContext<? super Row> context) throws Exception {
        mapper.mapTo(source, target, context);
    }

    private MappingContext<? super Row> newMappingContext() {
        return mappingContextFactory.newContext();
    }
}
