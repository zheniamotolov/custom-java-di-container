package com.vizorgames.interview.ioc;

import com.vizorgames.interview.exception.BindingNotFoundException;
import com.vizorgames.interview.exception.ConstructorAmbiguityException;
import com.vizorgames.interview.exception.NoSuitableConstructorException;

import javax.inject.Inject;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
//import javax.inject.Provider;

//Injector==Linker
//Faktory==Provider
public class InjectorImpl implements Injector {

    private Map<Class, Class> classMappings = new HashMap<>();
    private Map<Class, Object> singletonInstances = new HashMap<>();
    private Map<Class, Class> singletonMappings = new HashMap<>();


    @Override
    public <T> Provider<T> getProvider(Class<T> requestedType) {
        if (classMappings.containsKey(requestedType) || singletonMappings.containsKey(requestedType)) {
            isConstructorSuitable(retrieveClassType(requestedType).getConstructors());
            return new Provider<T>() {
                @Override
                public T getInstance() {
                    return prepareInstance(requestedType);
//                    return instance;
                }
            };
        } else if (!requestedType.isInterface()) { //todo: Not the best way for dependency binding check
            throw new BindingNotFoundException();
        } else {
            return null;
        }
    }

    public <T> T prepareInstance(Class<T> requestedType) {
        return prepareInstance(requestedType, null);
    }

    private <T> T prepareInstance(Class<T> requestedType, Class<?> parent) throws
            ConstructorAmbiguityException, NoSuitableConstructorException, BindingNotFoundException {

        Class<T> type = retrieveClassType(requestedType);

        if (singletonInstances.containsKey(type)) {
            return (T) singletonInstances.get(type);
        }
        return createNewInstance(type, parent);

    }

    private <T> Class<T> retrieveClassType(Class<T> requestedType) {
        if (classMappings.containsKey(requestedType)) {//
            return classMappings.get(requestedType);
        } else if (singletonMappings.containsKey(requestedType)) {
            return singletonMappings.get(requestedType);
        }//todo: Can be added Exception for "no implementation class mapping for this class/interface"
        return requestedType;
    }


    private <T> T createNewInstance(Class<T> type, Class<?> parent) throws ConstructorAmbiguityException, NoSuitableConstructorException {
        final Constructor<T> constructor = findConstructor(type);
        final Parameter[] parameters = constructor.getParameters();

        final List<Object> arguments = prepareArguments(type, parameters);
        try {
            final T newInstance = constructor.newInstance(arguments.toArray());
            if (isSingleton(type)) {
                singletonInstances.put(type, newInstance);
            }
            return newInstance;
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private <T> List<Object> prepareArguments(Class<T> type, Parameter[] parameters) {
        return Arrays.stream(parameters)
                .map(param -> {
                    return (Object) prepareInstance(param.getType(), type);
                })
                .collect(Collectors.toList());
    }

    private <T> Constructor<T> findConstructor(Class<T> type) throws NoSuitableConstructorException {
        final Constructor<?>[] constructors = type.getConstructors();
        if (constructors.length > 1) {
            final List<Constructor<?>> constructorsWithInject = findConstructorsWithInjectAnnotation(constructors);
            if (constructorsWithInject.size() >= 2) {
                throw new ConstructorAmbiguityException();
            }
            return (Constructor<T>) constructorsWithInject.get(0);
        } else {
            return (Constructor<T>) constructors[0];
        }
    }

    private void isConstructorSuitable(Constructor<?>[] constructors) { //todo: this part must be specified more precisely
        if ((constructors[0].getParameterCount() != 0) && !constructors[0].isAnnotationPresent(Inject.class)) {
            throw new NoSuitableConstructorException();
        }
    }

    private List<Constructor<?>> findConstructorsWithInjectAnnotation(Constructor<?>[] constructors) {
        return Arrays
                .stream(constructors)
                .filter(c -> c.isAnnotationPresent(Inject.class))
                .collect(Collectors.toList());
    }

    private boolean isSingleton(Class type) {
        return singletonMappings.containsValue(type);
    }

    @Override
    public <T> void bind(Class<T> base, Class<? extends T> impl) {
        classMappings.put(base, impl);
    }

    @Override
    public <T> void bindSingleton(Class<T> base, Class<? extends T> impl) {
        singletonMappings.put(base, impl);
    }

}

