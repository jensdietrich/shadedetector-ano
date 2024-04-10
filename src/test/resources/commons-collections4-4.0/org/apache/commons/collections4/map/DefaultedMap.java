/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.collections4.map;

import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.functors.ConstantTransformer;
import org.apache.commons.collections4.functors.FactoryTransformer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Decorates another <code>Map</code> returning a default value if the map
 * does not contain the requested key.
 * <p>
 * When the {@link #get(Object)} method is called with a key that does not
 * exist in the map, this map will return the default value specified in
 * the constructor/factory. Only the get method is altered, so the
 * {@link Map#containsKey(Object)} can be used to determine if a key really
 * is in the map or not.
 * <p>
 * The defaulted value is not added to the map.
 * Compare this behaviour with {@link LazyMap}, which does add the value
 * to the map (via a Transformer).
 * <p>
 * For instance:
 * <pre>
 * Map map = new DefaultedMap("NULL");
 * Object obj = map.get("Surname");
 * // obj == "NULL"
 * </pre>
 * After the above code is executed the map is still empty.
 * <p>
 * <strong>Note that DefaultedMap is not synchronized and is not thread-safe.</strong>
 * If you wish to use this map from multiple threads concurrently, you must use
 * appropriate synchronization. The simplest approach is to wrap this map
 * using {@link java.util.Collections#synchronizedMap(Map)}. This class may throw
 * exceptions when accessed by concurrent threads without synchronization.
 *
 * @since 3.2
 * @version $Id: DefaultedMap.java 1479407 2013-05-05 22:07:53Z tn $
 *
 * @see LazyMap
 */
public class DefaultedMap<K, V> extends AbstractMapDecorator<K, V> implements Serializable {

    /** Serialization version */
    private static final long serialVersionUID = 19698628745827L;

    /** The transformer to use if the map does not contain a key */
    private final Transformer<? super K, ? extends V> value;

    //-----------------------------------------------------------------------
    /**
     * Factory method to create a defaulting map.
     * <p>
     * The value specified is returned when a missing key is found.
     *
     * @param <K>  the key type
     * @param <V>  the value type
     * @param map  the map to decorate, must not be null
     * @param defaultValue  the default value to return when the key is not found
     * @return a new defaulting map
     * @throws IllegalArgumentException if map is null
     * @since 4.0
     */
    public static <K, V> DefaultedMap<K, V> defaultedMap(final Map<K, V> map, final V defaultValue) {
        return new DefaultedMap<K, V>(map, ConstantTransformer.constantTransformer(defaultValue));
    }

    /**
     * Factory method to create a defaulting map.
     * <p>
     * The factory specified is called when a missing key is found.
     * The result will be returned as the result of the map get(key) method.
     *
     * @param <K>  the key type
     * @param <V>  the value type
     * @param map  the map to decorate, must not be null
     * @param factory  the factory to use to create entries, must not be null
     * @return a new defaulting map
     * @throws IllegalArgumentException if map or factory is null
     * @since 4.0
     */
    public static <K, V> DefaultedMap<K, V> defaultedMap(final Map<K, V> map, final Factory<? extends V> factory) {
        if (factory == null) {
            throw new IllegalArgumentException("Factory must not be null");
        }
        return new DefaultedMap<K, V>(map, FactoryTransformer.factoryTransformer(factory));
    }

    /**
     * Factory method to create a defaulting map.
     * <p>
     * The transformer specified is called when a missing key is found.
     * The key is passed to the transformer as the input, and the result
     * will be returned as the result of the map get(key) method.
     *
     * @param <K>  the key type
     * @param <V>  the value type
     * @param map  the map to decorate, must not be null
     * @param transformer  the transformer to use as a factory to create entries, must not be null
     * @return a new defaulting map
     * @throws IllegalArgumentException if map or factory is null
     * @since 4.0
     */
    public static <K, V> Map<K, V> defaultedMap(final Map<K, V> map,
                                                final Transformer<? super K, ? extends V> transformer) {
        if (transformer == null) {
           throw new IllegalArgumentException("Transformer must not be null");
       }
       return new DefaultedMap<K, V>(map, transformer);
    }

    //-----------------------------------------------------------------------
    /**
     * Constructs a new empty <code>DefaultedMap</code> that decorates
     * a <code>HashMap</code>.
     * <p>
     * The object passed in will be returned by the map whenever an
     * unknown key is requested.
     *
     * @param defaultValue  the default value to return when the key is not found
     */
    public DefaultedMap(final V defaultValue) {
        this(ConstantTransformer.constantTransformer(defaultValue));
    }

    /**
     * Constructs a new empty <code>DefaultedMap</code> that decorates a <code>HashMap</code>.
     *
     * @param defaultValueTransformer transformer to use to generate missing values.
     */
    public DefaultedMap(final Transformer<? super K, ? extends V> defaultValueTransformer) {
        this(new HashMap<K, V>(), defaultValueTransformer);
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param map  the map to decorate, must not be null
     * @param defaultValueTransformer  the value transformer to use
     * @throws IllegalArgumentException if map or transformer is null
     */
    protected DefaultedMap(final Map<K, V> map, final Transformer<? super K, ? extends V> defaultValueTransformer) {
        super(map);
        if (defaultValueTransformer == null) {
            throw new IllegalArgumentException("transformer must not be null");
        }
        this.value = defaultValueTransformer;
    }

    //-----------------------------------------------------------------------
    /**
     * Write the map out using a custom routine.
     *
     * @param out  the output stream
     * @throws IOException
     */
    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(map);
    }

    /**
     * Read the map in using a custom routine.
     *
     * @param in  the input stream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        map = (Map<K, V>) in.readObject();
    }

    //-----------------------------------------------------------------------
    @Override
    @SuppressWarnings("unchecked")
    public V get(final Object key) {
        // create value for key if key is not currently in the map
        if (map.containsKey(key) == false) {
            return value.transform((K) key);
        }
        return map.get(key);
    }

    // no need to wrap keySet, entrySet or values as they are views of
    // existing map entries - you can't do a map-style get on them.
}
