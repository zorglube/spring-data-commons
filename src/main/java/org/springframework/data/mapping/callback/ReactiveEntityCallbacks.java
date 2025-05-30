/*
 * Copyright 2019-2025 the original author or authors.
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
package org.springframework.data.mapping.callback;

import reactor.core.publisher.Mono;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.Assert;

/**
 * Interface to be implemented by objects that can manage a number of {@link EntityCallback} objects and invoke these
 * with a specific entity.
 *
 * @author Christoph Strobl
 * @since 2.2
 * @see EntityCallback
 */
public interface ReactiveEntityCallbacks {

	/**
	 * Add the given {@link EntityCallback callback} using generic type argument detection for identification of supported
	 * types.
	 *
	 * @param callback must not be {@literal null}.
	 * @throws IllegalArgumentException if the required argument is {@literal null}.
	 */
	void addEntityCallback(EntityCallback<?> callback);

	/**
	 * On {@link Mono#subscribe() subscribe} invoke the matching {@link EntityCallback entity callbacks} with given
	 * arguments.
	 *
	 * @param callbackType must not be {@literal null}.
	 * @param entity must not be {@literal null}.
	 * @param args optional arguments.
	 * @param <T> Entity type.
	 * @return a {@link Mono} emitting the result after invoking the callbacks.
	 * @throws IllegalArgumentException if a required argument is {@literal null}.
	 */
	@SuppressWarnings("rawtypes")
	<T> Mono<T> callback(Class<? extends EntityCallback> callbackType, T entity, Object... args);

	/**
	 * Create a new {@link ReactiveEntityCallbacks} instance with given {@link EntityCallback callbacks}. <br />
	 * The provided {@link EntityCallback callbacks} are immediately {@link #addEntityCallback(EntityCallback) added}.
	 */
	static ReactiveEntityCallbacks create(EntityCallback<?>... callbacks) {

		ReactiveEntityCallbacks entityCallbacks = create();
		for (EntityCallback<?> callback : callbacks) {
			entityCallbacks.addEntityCallback(callback);
		}
		return entityCallbacks;
	}

	/**
	 * Obtain a new {@link ReactiveEntityCallbacks} instance. <br />
	 * Use {@link #addEntityCallback(EntityCallback)} to register callbacks manually.
	 */
	static ReactiveEntityCallbacks create() {
		return new DefaultReactiveEntityCallbacks();
	}

	/**
	 * Obtain a new {@link ReactiveEntityCallbacks} instance.
	 * <p>
	 * {@link EntityCallback callbacks} are pre-loaded from the given {@link BeanFactory}. <br />
	 * Use {@link #addEntityCallback(EntityCallback)} to register additional callbacks manually.
	 *
	 * @param beanFactory must not be {@literal null}.
	 * @throws IllegalArgumentException if a required argument is {@literal null}.
	 */
	static ReactiveEntityCallbacks create(BeanFactory beanFactory) {

		Assert.notNull(beanFactory, "Context must not be null");
		return new DefaultReactiveEntityCallbacks(beanFactory);
	}
}
