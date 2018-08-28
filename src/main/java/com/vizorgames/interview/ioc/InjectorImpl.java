package com.vizorgames.interview.ioc;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;


//Injector==Linker
//FAktory== Provider                                       ==Mappingg=(injector)
public class InjectorImpl implements Injector {


//    private final Map<Class<?>, Provider<?>>  providers = new HashMap<>();

    private final Map<Class<?>, Provider<?>> map = new HashMap<>();

    private Map<Class, Class> interfaceMappings = new HashMap<>();


    @Override
    public <T> Provider<T> getProvider(Class<T> type) {
        return (Provider<T>) map.get(type);

//        throw new UnsupportedOperationException("Not implemented");
    }

    //base==key
    //
    @Override
    public <T> void bind(Class<T> base, Class<? extends T> impl) {
        interfaceMappings.put(base, impl.asSubclass(base));
        if (interfaceType.isInterface()) {
            if (implementationType.isInterface()) {
                throw new IllegalArgumentException("The given type is an interface. Expecting the second argument to not be an interface but an actual class");
            } else if (isAbstractClass(implementationType)) {
                throw new IllegalArgumentException("The given type is an abstract class. Expecting the second argument to be an actual implementing class");
            } else {
                interfaceMappings.put(interfaceType, implementationType);
            }
        } else {
            throw new IllegalArgumentException("The given type is not an interface. Expecting the first argument to be an interface.");
        }


//        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public <T> void bindSingleton(Class<T> base, Class<? extends T> impl) {
        throw new UnsupportedOperationException("Not Implemented");
    }
}
