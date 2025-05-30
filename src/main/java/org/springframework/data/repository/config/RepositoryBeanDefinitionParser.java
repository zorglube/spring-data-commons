/*
 * Copyright 2008-2025 the original author or authors.
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
package org.springframework.data.repository.config;

import static org.springframework.beans.factory.support.BeanDefinitionReaderUtils.*;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.ReaderContext;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.config.ConfigurationUtils;
import org.springframework.util.Assert;

import org.w3c.dom.Element;

/**
 * Base class to implement repository namespaces. These will typically consist of a main XML element potentially having
 * child elements. The parser will wrap the XML element into a {@link XmlRepositoryConfigurationSource} object and allow
 * either manual configuration or automatic detection of repository interfaces.
 *
 * @author Oliver Gierke
 */
public class RepositoryBeanDefinitionParser implements BeanDefinitionParser {

	private final RepositoryConfigurationExtension extension;

	/**
	 * Creates a new {@link RepositoryBeanDefinitionParser} using the given {@link RepositoryConfigurationExtension}.
	 *
	 * @param extension must not be {@literal null}.
	 */
	public RepositoryBeanDefinitionParser(RepositoryConfigurationExtension extension) {

		Assert.notNull(extension, "Extension must not be null");
		this.extension = extension;
	}

	@Override
	@Nullable
	public BeanDefinition parse(Element element, ParserContext parser) {

		XmlReaderContext readerContext = parser.getReaderContext();

		try {

			ResourceLoader resourceLoader = ConfigurationUtils.getRequiredResourceLoader(readerContext);
			Environment environment = readerContext.getEnvironment();
			BeanDefinitionRegistry registry = parser.getRegistry();

			XmlRepositoryConfigurationSource configSource = new XmlRepositoryConfigurationSource(element, parser,
					environment);
			RepositoryConfigurationDelegate delegate = new RepositoryConfigurationDelegate(configSource, resourceLoader,
					environment);

			RepositoryConfigurationUtils.exposeRegistration(extension, registry, configSource);

			for (BeanComponentDefinition definition : delegate.registerRepositoriesIn(registry, extension)) {
				readerContext.fireComponentRegistered(definition);
			}

		} catch (RuntimeException e) {
			handleError(e, element, readerContext);
		}

		return null;
	}

	@SuppressWarnings("NullAway")
	private void handleError(Exception e, Element source, ReaderContext reader) {
		reader.error(e.getMessage(), reader.extractSource(source), e);
	}

	/**
	 * Returns whether the given {@link BeanDefinitionRegistry} already contains a bean of the given type assuming the
	 * bean name has been autogenerated.
	 *
	 * @param type
	 * @param registry
	 * @return
	 */
	protected static boolean hasBean(Class<?> type, BeanDefinitionRegistry registry) {

		String name = type.getName().concat(GENERATED_BEAN_NAME_SEPARATOR).concat("0");
		return registry.containsBeanDefinition(name);
	}
}
