package org.sfm.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.sfm.map.FieldMapper;
import org.sfm.map.FieldMapperErrorHandler;
import org.sfm.map.FieldMapperImpl;
import org.sfm.map.LogFieldMapperErrorHandler;
import org.sfm.map.MapperBuilderErrorHandler;
import org.sfm.map.RethrowMapperBuilderErrorHandler;
import org.sfm.reflect.Getter;
import org.sfm.reflect.Instantiator;
import org.sfm.reflect.InstantiatorFactory;
import org.sfm.reflect.Setter;
import org.sfm.reflect.SetterFactory;
import org.sfm.reflect.asm.AsmFactory;

public abstract class AbstractResultSetMapperBuilder<T> implements ResultSetMapperBuilder<T> {

	private FieldMapperErrorHandler fieldMapperErrorHandler = new LogFieldMapperErrorHandler();
	private MapperBuilderErrorHandler mapperBuilderErrorHandler = new RethrowMapperBuilderErrorHandler();

	private final Class<T> target;
	private final PrimitiveFieldMapperFactory<T> primitiveFieldMapperFactory;
	private final AsmFactory asmFactory;
	
	private final Instantiator<T> instantiator;

	private final List<FieldMapper<ResultSet, T>> fields = new ArrayList<FieldMapper<ResultSet, T>>();
	
	public AbstractResultSetMapperBuilder(Class<T> target, SetterFactory setterFactory) throws NoSuchMethodException, SecurityException {
		this.target = target;
		this.primitiveFieldMapperFactory = new PrimitiveFieldMapperFactory<>(setterFactory);
		this.asmFactory = setterFactory.getAsmFactory();
		this.instantiator = new InstantiatorFactory(asmFactory).getInstantiator(target);
	}

	@Override
	public final ResultSetMapperBuilder<T> fieldMapperErrorHandler(final FieldMapperErrorHandler errorHandler) {
		if (!fields.isEmpty()) {
			throw new IllegalStateException(
					"Error Handler need to be set before adding fields");
		}
		fieldMapperErrorHandler = errorHandler;
		return this;
	}

	@Override
	public final ResultSetMapperBuilder<T> mapperBuilderErrorHandler(final MapperBuilderErrorHandler errorHandler) {
		mapperBuilderErrorHandler = errorHandler;
		return this;
	}

	@Override
	public final ResultSetMapperBuilder<T> addNamedColumn(final String column, final int sqlType) {
		final Setter<T, Object> setter = findSetter(column);
		if (setter == null) {
			mapperBuilderErrorHandler.setterNotFound(target, column);
		} else {
			addMapping(setter, column, sqlType);
		}
		return this;
	}
	
	@Override
	public final ResultSetMapperBuilder<T> addNamedColumn(final String column) {
		return addNamedColumn(column, ResultSetGetterFactory.UNDEFINED);
	}

	@Override
	public final ResultSetMapperBuilder<T> addIndexedColumn(final String column) {
		return addIndexedColumn(column, fields.size() + 1);
	}

	@Override
	public final ResultSetMapperBuilder<T> addIndexedColumn(final String column, final int columnIndex) {
		return addIndexedColumn(column, columnIndex, ResultSetGetterFactory.UNDEFINED);
	}
	
	@Override
	public final ResultSetMapperBuilder<T> addIndexedColumn(final String column, final int columnIndex, final int sqlType) {
		final Setter<T, Object> setter = findSetter(column);
		if (setter == null) {
			mapperBuilderErrorHandler.setterNotFound(target, column);
		} else {
			addMapping(setter, columnIndex, sqlType);
		}
		return this;
	}

	@Override
	public final ResultSetMapperBuilder<T> addMapping(final String property, final String column, final int sqlType) {
		final Setter<T, Object> setter = getSetter(property);
		if (setter == null) {
			mapperBuilderErrorHandler.setterNotFound(target, property);
		} else {
			addMapping(setter, column, sqlType);
		}
		return this;
	}
	
	@Override
	public final ResultSetMapperBuilder<T> addMapping(final String property, final String column) {
		return addMapping(property, column, ResultSetGetterFactory.UNDEFINED);
	}

	@Override
	public final ResultSetMapperBuilder<T> addMapping(final String property, final int column, final int sqlType) {
		final Setter<T, Object> setter = getSetter(property);
		if (setter == null) {
			mapperBuilderErrorHandler.setterNotFound(target, property);
		} else {
			addMapping(setter, column, sqlType);
		}
		return this;
	}
	
	@Override
	public final ResultSetMapperBuilder<T> addMapping(final String property, final int column) {
		return addMapping(property, column, ResultSetGetterFactory.UNDEFINED);
	}

	@Override
	public final ResultSetMapperBuilder<T> addMapping(final ResultSetMetaData metaData) throws SQLException {
		for(int i = 1; i <= metaData.getColumnCount(); i++) {
			addIndexedColumn(metaData.getColumnName(i), i, metaData.getColumnType(i));
		}
		
		return this;
	}
	
	@Override
	public final JdbcMapper<T> mapper() {
		if (asmFactory != null) {
			try {
				return asmFactory.createJdbcMapper(fields(), getInstantiator(), target);
			} catch(Exception e) {
				return new JdbcMapperImpl<T>(fields(), getInstantiator());
			}
		} else {
			return new JdbcMapperImpl<T>(fields(), getInstantiator());
		}
	}

	private Instantiator<T> getInstantiator() {
		return instantiator;
	}

	public final Class<T> getTarget() {
		return target;
	}

	@Override
	@SuppressWarnings("unchecked")
	public final FieldMapper<ResultSet, T>[] fields() {
		return fields.toArray(new FieldMapper[fields.size()]);
	}

	private void addMapping(Setter<T, Object> setter, String column, int sqlType) {
		FieldMapper<ResultSet, T> fieldMapper;
	
		if (setter.getPropertyType().isPrimitive()) {
			fieldMapper = primitiveFieldMapperFactory.primitiveFieldMapper(column, setter, column, fieldMapperErrorHandler);
		} else {
			fieldMapper = objectFieldMapper(column, setter, sqlType);
		}
	
		fields.add(fieldMapper);
	}

	private void addMapping(Setter<T, Object> setter, int column, int sqlType) {
		FieldMapper<ResultSet, T> fieldMapper;
	
		if (setter.getPropertyType().isPrimitive()) {
			fieldMapper = primitiveFieldMapperFactory.primitiveFieldMapper(column, setter, String.valueOf(column), fieldMapperErrorHandler);
		} else {
			fieldMapper = objectFieldMapper(column, setter, sqlType);
		}
	
		fields.add(fieldMapper);
	}

	private FieldMapper<ResultSet, T> objectFieldMapper(String column, Setter<T, Object> setter, int sqlType) {
		Class<? extends Object> type = setter.getPropertyType();
		Getter<ResultSet, ? extends Object> getter = ResultSetGetterFactory.newGetter(type, column, sqlType);
		if (getter == null) {
			mapperBuilderErrorHandler.getterNotFound("No getter for column "
					+ column + " type " + type);
			return null;
		} else {
			return new FieldMapperImpl<ResultSet, T, Object>(column, getter,
					setter, fieldMapperErrorHandler);
		}
	}

	private FieldMapper<ResultSet, T> objectFieldMapper(int column, Setter<T, Object> setter, int sqlType) {
		Class<? extends Object> type = setter.getPropertyType();
		Getter<ResultSet, ? extends Object> getter = ResultSetGetterFactory.newGetter(type, column, sqlType);
		if (getter == null) {
			mapperBuilderErrorHandler.getterNotFound("No getter for column "
					+ column + " type " + type);
			return null;
		} else {
			return new FieldMapperImpl<ResultSet, T, Object>(
					String.valueOf(column), getter, setter,
					fieldMapperErrorHandler);
		}
	}
	
	protected abstract Setter<T, Object> findSetter(String column);
	protected abstract  Setter<T, Object> getSetter(String property);


}