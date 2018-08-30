package com.vizorgames.interview.ioc;

import com.vizorgames.interview.exception.BindingNotFoundException;
import com.vizorgames.interview.exception.ConstructorAmbiguityException;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
//import javax.inject.Provider;

//Injector==Linker
//FAktory== Provider                                       ==Mappingg=(injector)
public class InjectorImpl implements Injector {


    //    private final Map<Class<?>, Provider<?>>  providers = new HashMap<>();
    private Set<Class> requestedClasses = new HashSet<>();
    private Set<Class> instantiableClasses = new HashSet<>();

    private final Map<Class, Provider> providerMap = new HashMap<>();
    //    private Map<Class, Class> interfaceMappings = new HashMap<>();
    private final Map<Class<?>, Class<?>> classMap = new HashMap<>();
    private Map<Class, Class> interfaceMappings = new HashMap<>();
    private Map<Class, Class> singletonMappings = new HashMap<>();


    private <T> Constructor<T> findConstructor(Class<T> type) {
        final Constructor<?>[] constructors = type.getConstructors();

        if (constructors.length == 0) {
            throw new BindingNotFoundException();
        }

        if (constructors.length > 1) {

            final List<Constructor<?>> constructorsWithInject = Arrays
                    .stream(constructors)
                    .filter(c -> c.isAnnotationPresent(Inject.class))
                    .collect(Collectors.toList());

            if (constructorsWithInject.size() != 1) {
                throw new BindingNotFoundException();
            }

            // we are not modifying the constructor array so we can safely cast here.
            return (Constructor<T>) constructorsWithInject.get(0);
        } else {
            return (Constructor<T>) constructors[0];
        }
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

    private <T> T createNewInstance(Class<T> type, Class<?> parent) {
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

//            // when the class is marked as singleton it's instance is now added to the singleton map
//            if (isSingleton(type)) {
//                singletonInstances.put(type, newInstance);
//            }

            return newInstance;
        } catch (Exception e) {
            throw new BindingNotFoundException();
        }
    }


    @Override
    public <T> Provider<T> getProvider(Class<T> requestedType) {
        if (interfaceMappings.containsKey(requestedType)) {
            return new Provider<T>() {
                @Override
                public T getInstance() {
                    return getInstanceCustom(requestedType);
                }
            };
        } else {
            return null;
        }
//        return getInstanceCustom(requestedType);
    }

    public <T> T getInstanceCustom(Class<T> requestedType) {
        return getInstanceCustom(requestedType, null);
    }

    private <T> T getInstanceFromProvider(Class<T> type) {
        try {
            final Provider<T> provider = providerMap.get(type);
            return provider.getInstance();
        } catch (Exception e) {
            throw new BindingNotFoundException();
        }

    }

    //    @SuppressWarnings("unchecked")
    private <T> T getInstanceCustom(Class<T> requestedType, Class<?> parent) {
        try {
            Class<T> type = requestedType;

            if (requestedType.isInterface()) {
                if (interfaceMappings.containsKey(requestedType)) {
                    type = interfaceMappings.get(requestedType);
                }
            } else if (providerMap.containsKey(requestedType)) {
                return getInstanceFromProvider(requestedType);
            }
            if (requestedClasses.contains(type)) {
                // ... we should have been able to instantiate it in the past ...
                if (!instantiableClasses.contains(type)) {

                    // if not, this means a cyclic dependency and is an error
                    throw new ConstructorAmbiguityException();
                }
            } else {
                // if this class wasn't requested before we now add it to the checklist.
                requestedClasses.add(type);
            }
            if (providerMap.containsKey(type)) {
                final T instanceFromProvider = getInstanceFromProvider(type);
                markAsInstantiable(type);
                return instanceFromProvider;
            }
            return createNewInstance(type, parent);
        } catch (BindingNotFoundException e) {
            throw new BindingNotFoundException();
        }

    }

    private void markAsInstantiable(Class type) {
        if (!instantiableClasses.contains(type)) {
            instantiableClasses.add(type);
        }
    }

    @Override
    public <T> void bind(Class<T> base, Class<? extends T> impl) {
//        classMap.put(base, impl.asSubclass(base));
        interfaceMappings.put(base, impl); // <---
//        providerMap.put(base, () -> {
//            final Provider provider = new Provider() {
//                @Override
//                public Object getInstance() {
//                    return impl;
//                }
//            };
//            return provider;
//        });


//                new Provider<Class<?>>() {
//            @Override
//            public Class getInstance() {
//                return  impl; //here should be new instance of impl class
//            }
//        });
//        if (interfaceType.isInterface()) {
//            if (implementationType.isInterface()) {
//                throw new IllegalArgumentException("The given type is an interface. Expecting the second argument to not be an interface but an actual class");
//            } else if (isAbstractClass(implementationType)) {
//                throw new IllegalArgumentException("The given type is an abstract class. Expecting the second argument to be an actual implementing class");
//            } else {
//                interfaceMappings.put(interfaceType, implementationType);
//            }
//        } else {
//            throw new IllegalArgumentException("The given type is not an interface. Expecting the first argument to be an interface.");
//        }


//        throw new UnsupportedOperationException("Not implemented");
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

