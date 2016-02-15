package org.springframework.boot.autoconfigure.reactiveweb;

import java.util.Arrays;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.codec.support.ByteBufferEncoder;
import org.springframework.core.codec.support.JacksonJsonEncoder;
import org.springframework.core.codec.support.JsonObjectEncoder;
import org.springframework.core.codec.support.StringEncoder;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.core.convert.support.ReactiveStreamsToCompletableFutureConverter;
import org.springframework.core.io.buffer.DataBufferAllocator;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.reactive.ResponseStatusExceptionHandler;
import org.springframework.web.reactive.handler.SimpleHandlerResultHandler;
import org.springframework.web.reactive.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.reactive.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.reactive.method.annotation.ResponseBodyResultHandler;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

/**
 * @author Brian Clozel
 */
@Configuration
@ConditionalOnClass({DispatcherHandler.class, HttpHandler.class})
@AutoConfigureAfter({ReactiveHttpServerAutoConfiguration.class})
public class ReactiveWebAutoConfiguration implements ApplicationContextAware {

	public static final String DEFAULT_DISPATCHER_HANDLER_BEAN_NAME = "dispatcherHandler";

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Bean(name = DEFAULT_DISPATCHER_HANDLER_BEAN_NAME)
	public DispatcherHandler dispatcherHandler() {
		DispatcherHandler dispatcherHandler = new DispatcherHandler();
		dispatcherHandler.setApplicationContext(this.applicationContext);
		return dispatcherHandler;
	}

	@Bean
	public HttpHandler httpHandler() {
		return WebHttpHandlerBuilder.webHandler(dispatcherHandler())
				.exceptionHandlers(new ResponseStatusExceptionHandler())
				.build();
	}

	@Bean
	public RequestMappingHandlerMapping handlerMapping() {
		return new RequestMappingHandlerMapping();
	}

	@Bean
	public RequestMappingHandlerAdapter handlerAdapter() {
		RequestMappingHandlerAdapter handlerAdapter = new RequestMappingHandlerAdapter();
		// TODO: do those conversion services belong to spring proper?
		// should we change the way those are configured?
		handlerAdapter.setConversionService(conversionService());
		return handlerAdapter;
	}

	// TODO: this belongs to spring-reactive javaconfig
	public ConversionService conversionService() {
		GenericConversionService service = new GenericConversionService();
		service.addConverter(new ReactiveStreamsToCompletableFutureConverter());
		return service;
	}

	@Bean
	public ResponseBodyResultHandler responseBodyResultHandler(
			@Qualifier(ReactiveHttpServerAutoConfiguration.RUNTIME_DATABUFFER_ALLOCATOR_BEAN_NAME)
					DataBufferAllocator bufferAllocator) {
		return new ResponseBodyResultHandler(Arrays.asList(
				new ByteBufferEncoder(bufferAllocator), new StringEncoder(bufferAllocator),
				new JacksonJsonEncoder(bufferAllocator, new JsonObjectEncoder(bufferAllocator))), conversionService());
	}

	@Bean
	public SimpleHandlerResultHandler simpleHandlerResultHandler() {
		return new SimpleHandlerResultHandler(conversionService());
	}

}
