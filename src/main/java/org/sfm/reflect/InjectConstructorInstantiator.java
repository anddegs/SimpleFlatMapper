package org.sfm.reflect;

import java.util.Map;

import org.sfm.reflect.asm.ConstructorDefinition;
import org.sfm.reflect.asm.Parameter;

public final class InjectConstructorInstantiator<S, T> implements Instantiator<S, T> {
	
	

	private final ConstructorDefinition<T> constructorDefinition;
	private final ArgumentBuilder<S, T> argBuilder;

	public InjectConstructorInstantiator(ConstructorDefinition<T> constructorDefinition, Map<Parameter, Getter<S, ?>> injections) {
		this.constructorDefinition = constructorDefinition;
		this.argBuilder = new ArgumentBuilder<S, T>(constructorDefinition, injections);
	}

	@Override
	public T newInstance(S s) throws Exception {
		return constructorDefinition.getConstructor().newInstance(argBuilder.build(s));
	}

}