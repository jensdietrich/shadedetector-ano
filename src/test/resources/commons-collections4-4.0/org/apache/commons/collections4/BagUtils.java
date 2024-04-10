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
package org.apache.commons.collections4;

import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.SortedBag;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.bag.*;

/**
 * Provides utility methods and decorators for {@link org.apache.commons.collections4.Bag} and {@link org.apache.commons.collections4.SortedBag} instances.
 *
 * @since 2.1
 * @version $Id: BagUtils.java 1543964 2013-11-20 21:53:39Z tn $
 */
public class BagUtils {

    /**
     * An empty unmodifiable bag.
     */
    @SuppressWarnings("rawtypes") // OK, empty bag is compatible with any type
    public static final org.apache.commons.collections4.Bag EMPTY_BAG = UnmodifiableBag.unmodifiableBag(new HashBag<Object>());

    /**
     * An empty unmodifiable sorted bag.
     */
    @SuppressWarnings("rawtypes") // OK, empty bag is compatible with any type
    public static final org.apache.commons.collections4.Bag EMPTY_SORTED_BAG =
            UnmodifiableSortedBag.unmodifiableSortedBag(new TreeBag<Object>());

    /**
     * Instantiation of BagUtils is not intended or required.
     */
    private BagUtils() {}

    //-----------------------------------------------------------------------
    /**
     * Returns a synchronized (thread-safe) bag backed by the given bag. In
     * order to guarantee serial access, it is critical that all access to the
     * backing bag is accomplished through the returned bag.
     * <p>
     * It is imperative that the user manually synchronize on the returned bag
     * when iterating over it:
     *
     * <pre>
     * Bag bag = BagUtils.synchronizedBag(new HashBag());
     * ...
     * synchronized(bag) {
     *     Iterator i = bag.iterator(); // Must be in synchronized block
     *     while (i.hasNext())
     *         foo(i.next());
     *     }
     * }
     * </pre>
     *
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * @param <E> the element type
     * @param bag the bag to synchronize, must not be null
     * @return a synchronized bag backed by that bag
     * @throws IllegalArgumentException if the Bag is null
     */
    public static <E> org.apache.commons.collections4.Bag<E> synchronizedBag(final org.apache.commons.collections4.Bag<E> bag) {
        return SynchronizedBag.synchronizedBag(bag);
    }

    /**
     * Returns an unmodifiable view of the given bag. Any modification attempts
     * to the returned bag will raise an {@link UnsupportedOperationException}.
     *
     * @param <E> the element type
     * @param bag the bag whose unmodifiable view is to be returned, must not be null
     * @return an unmodifiable view of that bag
     * @throws IllegalArgumentException if the Bag is null
     */
    public static <E> org.apache.commons.collections4.Bag<E> unmodifiableBag(final org.apache.commons.collections4.Bag<? extends E> bag) {
        return UnmodifiableBag.unmodifiableBag(bag);
    }

    /**
     * Returns a predicated (validating) bag backed by the given bag.
     * <p>
     * Only objects that pass the test in the given predicate can be added to
     * the bag. Trying to add an invalid object results in an
     * IllegalArgumentException. It is important not to use the original bag
     * after invoking this method, as it is a backdoor for adding invalid
     * objects.
     *
     * @param <E> the element type
     * @param bag the bag to predicate, must not be null
     * @param predicate the predicate for the bag, must not be null
     * @return a predicated bag backed by the given bag
     * @throws IllegalArgumentException if the Bag or Predicate is null
     */
    public static <E> org.apache.commons.collections4.Bag<E> predicatedBag(final org.apache.commons.collections4.Bag<E> bag, final org.apache.commons.collections4.Predicate<? super E> predicate) {
        return PredicatedBag.predicatedBag(bag, predicate);
    }

    /**
     * Returns a transformed bag backed by the given bag.
     * <p>
     * Each object is passed through the transformer as it is added to the Bag.
     * It is important not to use the original bag after invoking this method,
     * as it is a backdoor for adding untransformed objects.
     * <p>
     * Existing entries in the specified bag will not be transformed.
     * If you want that behaviour, see {@link TransformedBag#transformedBag(org.apache.commons.collections4.Bag, org.apache.commons.collections4.Transformer)}.
     *
     * @param <E> the element type
     * @param bag the bag to predicate, must not be null
     * @param transformer the transformer for the bag, must not be null
     * @return a transformed bag backed by the given bag
     * @throws IllegalArgumentException if the Bag or Transformer is null
     */
    public static <E> org.apache.commons.collections4.Bag<E> transformingBag(final org.apache.commons.collections4.Bag<E> bag, final org.apache.commons.collections4.Transformer<? super E, ? extends E> transformer) {
        return TransformedBag.transformingBag(bag, transformer);
    }

    /**
     * Returns a bag that complies to the Collection contract, backed by the given bag.
     *
     * @param <E> the element type
     * @param bag the bag to decorate, must not be null
     * @return a Bag that complies to the Collection contract
     * @throws IllegalArgumentException if bag is null
     * @since 4.0
     */
    public static <E> org.apache.commons.collections4.Bag<E> collectionBag(final org.apache.commons.collections4.Bag<E> bag) {
        return CollectionBag.collectionBag(bag);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a synchronized (thread-safe) sorted bag backed by the given
     * sorted bag. In order to guarantee serial access, it is critical that all
     * access to the backing bag is accomplished through the returned bag.
     * <p>
     * It is imperative that the user manually synchronize on the returned bag
     * when iterating over it:
     *
     * <pre>
     * SortedBag bag = BagUtils.synchronizedSortedBag(new TreeBag());
     * ...
     * synchronized(bag) {
     *     Iterator i = bag.iterator(); // Must be in synchronized block
     *     while (i.hasNext())
     *         foo(i.next());
     *     }
     * }
     * </pre>
     *
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * @param <E> the element type
     * @param bag the bag to synchronize, must not be null
     * @return a synchronized bag backed by that bag
     * @throws IllegalArgumentException if the SortedBag is null
     */
    public static <E> org.apache.commons.collections4.SortedBag<E> synchronizedSortedBag(final org.apache.commons.collections4.SortedBag<E> bag) {
        return SynchronizedSortedBag.synchronizedSortedBag(bag);
    }

    /**
     * Returns an unmodifiable view of the given sorted bag. Any modification
     * attempts to the returned bag will raise an
     * {@link UnsupportedOperationException}.
     *
     * @param <E> the element type
     * @param bag the bag whose unmodifiable view is to be returned, must not be null
     * @return an unmodifiable view of that bag
     * @throws IllegalArgumentException if the SortedBag is null
     */
    public static <E> org.apache.commons.collections4.SortedBag<E> unmodifiableSortedBag(final org.apache.commons.collections4.SortedBag<E> bag) {
        return UnmodifiableSortedBag.unmodifiableSortedBag(bag);
    }

    /**
     * Returns a predicated (validating) sorted bag backed by the given sorted
     * bag.
     * <p>
     * Only objects that pass the test in the given predicate can be added to
     * the bag. Trying to add an invalid object results in an
     * IllegalArgumentException. It is important not to use the original bag
     * after invoking this method, as it is a backdoor for adding invalid
     * objects.
     *
     * @param <E> the element type
     * @param bag the sorted bag to predicate, must not be null
     * @param predicate the predicate for the bag, must not be null
     * @return a predicated bag backed by the given bag
     * @throws IllegalArgumentException if the SortedBag or Predicate is null
     */
    public static <E> org.apache.commons.collections4.SortedBag<E> predicatedSortedBag(final org.apache.commons.collections4.SortedBag<E> bag,
                                                                                       final Predicate<? super E> predicate) {
        return PredicatedSortedBag.predicatedSortedBag(bag, predicate);
    }

    /**
     * Returns a transformed sorted bag backed by the given bag.
     * <p>
     * Each object is passed through the transformer as it is added to the Bag.
     * It is important not to use the original bag after invoking this method,
     * as it is a backdoor for adding untransformed objects.
     * <p>
     * Existing entries in the specified bag will not be transformed.
     * If you want that behaviour, see
     * {@link TransformedSortedBag#transformedSortedBag(org.apache.commons.collections4.SortedBag, org.apache.commons.collections4.Transformer)}.
     *
     * @param <E> the element type
     * @param bag the bag to predicate, must not be null
     * @param transformer the transformer for the bag, must not be null
     * @return a transformed bag backed by the given bag
     * @throws IllegalArgumentException if the Bag or Transformer is null
     */
    public static <E> org.apache.commons.collections4.SortedBag<E> transformingSortedBag(final org.apache.commons.collections4.SortedBag<E> bag,
                                                                                         final Transformer<? super E, ? extends E> transformer) {
        return TransformedSortedBag.transformingSortedBag(bag, transformer);
    }

    /**
     * Get an empty <code>Bag</code>.
     *
     * @param <E> the element type
     * @return an empty Bag
     */
    @SuppressWarnings("unchecked") // OK, empty bag is compatible with any type
    public static <E> org.apache.commons.collections4.Bag<E> emptyBag() {
        return (Bag<E>) EMPTY_BAG;
    }

    /**
     * Get an empty <code>SortedBag</code>.
     *
     * @param <E> the element type
     * @return an empty sorted Bag
     */
    @SuppressWarnings("unchecked") // OK, empty bag is compatible with any type
    public static <E> org.apache.commons.collections4.SortedBag<E> emptySortedBag() {
        return (SortedBag<E>) EMPTY_SORTED_BAG;
    }
}
