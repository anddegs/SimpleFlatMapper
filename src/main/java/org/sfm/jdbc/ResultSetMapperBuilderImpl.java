package org.sfm.jdbc;

import org.sfm.reflect.Setter;
import org.sfm.reflect.SetterFactory;
import org.sfm.utils.PropertyNameMatcher;

public final class ResultSetMapperBuilderImpl<T> extends AbstractResultSetMapperBuilder<T> {

	private final SetterFactory setterFactory;

	public ResultSetMapperBuilderImpl(final Class<T> target) throws NoSuchMethodException, SecurityException {
		this(target, new SetterFactory());
	}

	public ResultSetMapperBuilderImpl(final Class<T> target, final SetterFactory setterFactory) throws NoSuchMethodException, SecurityException {
		super(target, setterFactory);
		this.setterFactory = setterFactory;
	}

	protected Setter<T, Object> findSetter(final String column) {
		return setterFactory.findSetter(new PropertyNameMatcher(column), getTarget());
	}
	protected  Setter<T, Object> getSetter(final String property) {
		return setterFactory.getSetter(getTarget(), property);
	}

}