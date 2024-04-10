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
package shaded.org.apache.commons.collections4.keyvalue;

import shaded.org.apache.commons.collections4.KeyValue;
import shaded.org.apache.commons.collections4.Unmodifiable;

import java.util.Map;

/**
 * A {@link Map.Entry Map.Entry} that throws
 * UnsupportedOperationException when <code>setValue</code> is called.
 *
 * @since 3.0
 * @version $Id: UnmodifiableMapEntry.java 1533984 2013-10-20 21:12:51Z tn $
 */
public final class UnmodifiableMapEntry<K, V> extends AbstractMapEntry<K, V> implements Unmodifiable {

    /**
     * Constructs a new entry with the specified key and given value.
     *
     * @param key  the key for the entry, may be null
     * @param value  the value for the entry, may be null
     */
    public UnmodifiableMapEntry(final K key, final V value) {
        super(key, value);
    }

    /**
     * Constructs a new entry from the specified <code>KeyValue</code>.
     *
     * @param pair  the pair to copy, must not be null
     * @throws NullPointerException if the entry is null
     */
    public UnmodifiableMapEntry(final KeyValue<? extends K, ? extends V> pair) {
        super(pair.getKey(), pair.getValue());
    }

    /**
     * Constructs a new entry from the specified <code>Map.Entry</code>.
     *
     * @param entry  the entry to copy, must not be null
     * @throws NullPointerException if the entry is null
     */
    public UnmodifiableMapEntry(final Map.Entry<? extends K, ? extends V> entry) {
        super(entry.getKey(), entry.getValue());
    }

    /**
     * Throws UnsupportedOperationException.
     *
     * @param value  the new value
     * @return the previous value
     * @throws UnsupportedOperationException always
     */
    @Override
    public V setValue(final V value) {
        throw new UnsupportedOperationException("setValue() is not supported");
    }

}
