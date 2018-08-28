package com.vizorgames.interview.ioc;

public interface Provider<T>
{
//    T getInstance();
      T getInstance(Class<T> type);




//    T get(Linker linker);
}
