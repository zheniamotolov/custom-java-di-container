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


    //    private final Map<Class<?>, Provider<?>>  providers = new HashMap<>();
    private Set<Class> requestedClasses = new HashSet<>();
    private Set<Class> instantiableClasses = new HashSet<>();

    private final Map<Class, Provider> providerMap = new HashMap<>();
    private final Map<Class<?>, Class<?>> classMap = new HashMap<>();

    private Map<Class, Class> classMappings = new HashMap<>();
    private Map<Class, Object> singletonInstances = new HashMap<>();
    private Map<Class, Class> singletonMappings = new HashMap<>();


    @Override
    public <T> Provider<T> getProvider(Class<T> requestedType) {
        if (classMappings.containsKey(requestedType)) {
//            T instance = getInstanceCustom(requestedType);

            return new Provider<T>() {
                @Override
                public T getInstance() {
                    return getInstanceCustom(requestedType);
//                    return getInstanceCustom(requestedType);
                }
            };
        } else if (singletonMappings.containsKey(requestedType)) {
            return new Provider<T>() {
                @Override
                public T getInstance() {
                    return getInstanceCustom(requestedType);
//                    return getInstanceCustom(requestedType);
                }
            };

        } else if (!requestedType.isInterface()) { //todo: Not the best way for dependency binding check
            throw new BindingNotFoundException();
        } else {
            return null;
        }
    }

    public <T> T getInstanceCustomgit (Class<T> requestedType) {
        return getInstanceCustom(requestedType, null);
    }

//    private <T> T getInstanceFromProvider(Class<T> type) {
//        try {
//            final Provider<T> provider = providerMap.get(type);
//            return provider.getInstance();
//        } catch (Exception e) {
//            throw new BindingNotFoundException();
//        }
//
//    }

    private <T> T getInstanceCustom(Class<T> requestedType, Class<?> parent) throws
            ConstructorAmbiguityException, NoSuitableConstructorException, BindingNotFoundException {

        Class<T> type = requestedType;


        if (classMappings.containsKey(requestedType)) {//
            type = classMappings.get(requestedType);
        } else if (singletonMappings.containsKey(requestedType)) {
            type = singletonMappings.get(requestedType);
        }
//        else {
//            throw new BindingNotFoundException();
//        }
//        } else if (providerMap.containsKey(requestedType)) {
//            return getInstanceFromProvider(requestedType);
//        }
        if (requestedClasses.contains(type)) {
            if (!instantiableClasses.contains(type)) {
                throw new ConstructorAmbiguityException();
            }
        } else {
            requestedClasses.add(type);
        }
        if (singletonInstances.containsKey(type)) {
            return (T) singletonInstances.get(type);
        }

//        if (providerMap.containsKey(type)) {
//            final T instanceFromProvider = getInstanceFromProvider(type);
//            markAsInstantiable(type);
//            if (isSingleton(type)) {
//                singletonInstances.put(type, instanceFromProvider);
//            }
//            return instanceFromProvider;
//        }
        return createNewInstance(type, parent);

    }


    private void markAsInstantiable(Class type) {
        if (!instantiableClasses.contains(type)) {
            instantiableClasses.add(type);
        }
    }

    private <T> T createNewInstance(Class<T> type, Class<?> parent) throws ConstructorAmbiguityException, NoSuitableConstructorException {
        final Constructor<T> constructor = findConstructor(type);
        final Parameter[] parameters = constructor.getParameters();

        // recursively get all constructor arguments
        final List<Object> arguments = Arrays.stream(parameters)
                .map(param -> {
//                    if (param.getType().equals(Provider.class)) {
//                        return getProviderArgument(param, type);
//                    } else {
                    return (Object) getInstanceCustom(param.getType(), type);
//                    }
                })
                .collect(Collectors.toList());

        try {
            final T newInstance = constructor.newInstance(arguments.toArray());

            markAsInstantiable(type);

//            if (isSingleton(type)) {
//                singletonInstances.put(type, newInstance);
//            }
            if (isSingleton(type)) {
                singletonInstances.put(type, newInstance);
            }
            return newInstance;
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private <T> Constructor<T> findConstructor(Class<T> type) {
        final Constructor<?>[] constructors = type.getConstructors();

//        if (constructors.length == 0) { //for checking non public constructors
//            throw new BindingNotFoundException();
//        }

        if (constructors.length > 1) {
            final List<Constructor<?>> constructorsWithInject = Arrays
                    .stream(constructors)
                    .filter(c -> c.isAnnotationPresent(Inject.class))
                    .collect(Collectors.toList());
            if (constructorsWithInject.size() == 0) { //todo: It's not really check default constructor in class, it's just check any constructor with 0 arguments
                if (constructors.length > 1 || constructors[0].getParameterCount() != 0) {
                    throw new NoSuitableConstructorException();
                }
            } else if (constructorsWithInject.size() >= 2) {
                throw new ConstructorAmbiguityException();
            }


            return (Constructor<T>) constructorsWithInject.get(0);
        } else {
            return (Constructor<T>) constructors[0];
        }
    }

    private boolean isSingleton(Class type) {
        return singletonMappings.containsValue(type);
    }

    //    private Provider getProviderArgument(Parameter param, Class requestedType) {
//        if (param.getParameterizedType() instanceof ParameterizedType) {
//            ParameterizedType typeParam = (ParameterizedType) param.getParameterizedType();
//
//            final Type providerType = typeParam.getActualTypeArguments()[0];
//
//            return () -> InjectorImpl.this.createNewInstance((Class) providerType);
//        } else {
//            throw new BindingNotFoundException();
//        }
//    }
    @Override
    public <T> void bind(Class<T> base, Class<? extends T> impl) {
        classMappings.put(base, impl);
    }

    @Override
    public <T> void bindSingleton(Class<T> base, Class<? extends T> impl) {

//            if (type.isInterface()) {
//                throw new IllegalArgumentException("The given type is an interface. Expecting the param to be an actual class");
//            }
        singletonMappings.put(base, impl);
//        singletonClasses.add(type);
    }

}

