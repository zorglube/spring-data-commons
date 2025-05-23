/*
 * Copyright 2016-2025 the original author or authors.
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
package org.springframework.data.repository.core.support;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.ValueExpressionDelegate;
import org.springframework.data.repository.util.ReactiveWrapperConverters;
import org.springframework.data.util.ReactiveWrappers;
import org.springframework.util.ClassUtils;

/**
 * Base class for repository factories to use reactive support. Centralizes the validation of the classpath setup in
 * case a repository uses reactive types.
 *
 * @author Mark Paluch
 * @author Oliver Gierke
 * @author Johannes Englmeier
 * @since 2.0
 */
public abstract class ReactiveRepositoryFactorySupport extends RepositoryFactorySupport {

	@Override
	protected void validate(RepositoryMetadata repositoryMetadata) {

		if (!ReactiveWrappers.isAvailable()) {

			throw new InvalidDataAccessApiUsageException(
					String.format("Cannot implement repository %s without reactive library support",
							repositoryMetadata.getRepositoryInterface().getName()));
		}

		if (RxJavaOneConversionSetup.REACTIVE_STREAMS_PRESENT) {

			Arrays.stream(repositoryMetadata.getRepositoryInterface().getMethods())
					.forEach(RxJavaOneConversionSetup::validate);
		}
	}

	/**
	 * Returns the {@link QueryLookupStrategy} for the given {@link QueryLookupStrategy.Key} and
	 * {@link ValueExpressionDelegate}.
	 *
	 * @param key can be {@literal null}.
	 * @param valueExpressionDelegate will never be {@literal null}.
	 * @return the {@link QueryLookupStrategy} to use or {@literal null} if no queries should be looked up.
	 * @since 3.4
	 */
	@Override
	protected Optional<QueryLookupStrategy> getQueryLookupStrategy(QueryLookupStrategy.@Nullable Key key,
			ValueExpressionDelegate valueExpressionDelegate) {
		return Optional.empty();
	}

	/**
	 * We need to make sure that the necessary conversion libraries are in place if the repository interface uses RxJava 1
	 * types.
	 *
	 * @author Mark Paluch
	 * @author Oliver Gierke
	 */
	private static class RxJavaOneConversionSetup {

		private static final boolean REACTIVE_STREAMS_PRESENT = ClassUtils.isPresent("org.reactivestreams.Publisher",
				RxJavaOneConversionSetup.class.getClassLoader());

		/**
		 * Reactive MongoDB support requires reactive wrapper support. If return type/parameters are reactive wrapper types,
		 * then it's required to be able to convert these.
		 *
		 * @param method the method to validate.
		 */
		private static void validate(Method method) {

			if (ReactiveWrappers.supports(method.getReturnType())
					&& !ClassUtils.isAssignable(Publisher.class, method.getReturnType())
					&& !ReactiveWrapperConverters.supports(method.getReturnType())) {

				throw new InvalidDataAccessApiUsageException(
						String.format("No reactive type converter found for type %s used in %s, method %s",
								method.getReturnType().getName(), method.getDeclaringClass().getName(), method));
			}

			Arrays.stream(method.getParameterTypes()) //
					.filter(ReactiveWrappers::supports) //
					.filter(parameterType -> !ClassUtils.isAssignable(Publisher.class, parameterType)) //
					.filter(parameterType -> !ReactiveWrapperConverters.supports(parameterType)) //
					.forEach(parameterType -> {
						throw new InvalidDataAccessApiUsageException(
								String.format("No reactive type converter found for type %s used in %s, method %s",
										parameterType.getName(), method.getDeclaringClass().getName(), method));
					});
		}
	}
}
