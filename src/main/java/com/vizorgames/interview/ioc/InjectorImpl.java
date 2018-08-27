package com.vizorgames.interview.ioc;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;


//Injector==Linker
//FAktory== Provider
public class InjectorImpl implements Injector
{


    private final Map<Class<?>, Provider<?>>  providers = new HashMap<>();


    @Inject
    @Override
    public <T> Provider<T> getProvider(Class<T> type)
    {


        throw new UnsupportedOperationException("Not implemented");
    }

    //base==key
    //
    @Override
    public <T> void bind(Class<T> base, Class<? extends T> impl)
    {

        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public <T> void bindSingleton(Class<T> base, Class<? extends T> impl)
    {
        throw new UnsupportedOperationException("Not Implemented");
    }
}
