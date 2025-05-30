/*
 * Copyright 2011-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.mapping;

import org.jspecify.annotations.Nullable;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.util.Streamable;

/**
 * Abstraction of a path of {@link PersistentProperty}s.
 *
 * @author Oliver Gierke
 * @author Johannes Englmeier
 */
public interface PersistentPropertyPath<P extends PersistentProperty<P>> extends Streamable<P> {

	/**
	 * Returns the dot based path notation using {@link PersistentProperty#getName()}.
	 *
	 * @return will never be {@literal null}.
	 */
	String toDotPath();

	/**
	 * Returns the dot based path notation using the given {@link Converter} to translate individual
	 * {@link PersistentProperty}s to path segments.
	 *
	 * @param converter must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	String toDotPath(Converter<? super P, String> converter);

	/**
	 * Returns a {@link String} path with the given delimiter based on the {@link PersistentProperty#getName()}.
	 *
	 * @param delimiter must not be {@literal null} or empty.
	 * @return will never be {@literal null}.
	 */
	String toPath(String delimiter);

	/**
	 * Returns a {@link String} path with the given delimiter using the given {@link Converter} for
	 * {@link PersistentProperty} to String conversion.
	 *
	 * @param delimiter must not be {@literal null}.
	 * @param converter must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	String toPath(String delimiter, Converter<? super P, String> converter);

	/**
	 * Returns the last property in the {@link PersistentPropertyPath}. So for {@code foo.bar} it will return the
	 * {@link PersistentProperty} for {@code bar}. For a simple {@code foo} it returns {@link PersistentProperty} for
	 * {@code foo}.
	 *
	 * @return will never be {@literal null}.
	 */
	P getLeafProperty();

	/**
	 * Returns the first property in the {@link PersistentPropertyPath}. So for {@code foo.bar} it will return the
	 * {@link PersistentProperty} for {@code foo}. For a simple {@code foo} it returns {@link PersistentProperty} for
	 * {@code foo}.
	 *
	 * @return will never be {@literal null}.
	 */
	P getBaseProperty();

	/**
	 * Returns whether the current path is located at the root of the traversal. In other words, if the path only contains
	 * a single property.
	 *
	 * @return whether the current path is located at the root of the traversal
	 */
	default boolean isRootPath() {
		return getLength() == 1;
	}

	/**
	 * Returns whether the given {@link PersistentPropertyPath} is a base path of the current one. This means that the
	 * current {@link PersistentPropertyPath} is basically an extension of the given one.
	 *
	 * @param path must not be {@literal null}.
	 * @return whether the given {@link PersistentPropertyPath} is a base path of the current one.
	 */
	boolean isBasePathOf(PersistentPropertyPath<P> path);

	/**
	 * Returns the sub-path of the current one as if it was based on the given base path. So for a current path
	 * {@code foo.bar} and a given base {@code foo} it would return {@code bar}. If the given path is not a base of the
	 * the current one the current {@link PersistentPropertyPath} will be returned as is.
	 *
	 * @param base must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	PersistentPropertyPath<P> getExtensionForBaseOf(PersistentPropertyPath<P> base);

	/**
	 * Returns the parent path of the current {@link PersistentPropertyPath}, i.e. the path without the leaf property.
	 * This happens up to the base property. So for a direct property reference calling this method will result in
	 * returning the property.
	 *
	 * @return
	 * @throws IllegalStateException if the current path only consists of one segment.
	 */
	@Nullable
	PersistentPropertyPath<P> getParentPath() throws IllegalStateException;

	/**
	 * Returns the length of the {@link PersistentPropertyPath}.
	 *
	 * @return a value greater than 0.
	 */
	int getLength();
}
