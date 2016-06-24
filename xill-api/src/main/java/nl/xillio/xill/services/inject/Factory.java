/**
 * Copyright (C) 2014 Xillio (support@xillio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.xillio.xill.services.inject;

import com.google.inject.Provider;

import java.util.function.Supplier;

/**
 * This class represents a factory that will create a new instance of the parameter class.
 *
 * @param <T> the class that will be made
 */
public class Factory<T> implements Provider<T> {
    private final Supplier<T> supplier;

    /**
     * Creates a new factory that will run a {@link Supplier} to get an instance.
     *
     * @param supplier the supplier
     */
    public Factory(final Supplier<T> supplier) {
        this.supplier = supplier;
    }

    /**
     * Creates a new factory that will instantiate a class using an empty constructor.
     *
     * @param clazz the type of class
     */
    public Factory(final Class<T> clazz) {
        supplier = () -> {
            try {
                clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new FactoryBuilderException("Failed to instantiate a " + clazz.getName(), e);
            }
            return null;
        };
    }

    @Override
    public T get() {
        return supplier.get();
    }

}
