/*
 * Copyright (c) 1998-2015 Citrix Online LLC
 * All Rights Reserved Worldwide.
 *
 * THIS PROGRAM IS CONFIDENTIAL AND PROPRIETARY TO CITRIX ONLINE
 * AND CONSTITUTES A VALUABLE TRADE SECRET.  Any unauthorized use,
 * reproduction, modification, or disclosure of this program is
 * strictly prohibited.  Any use of this program by an authorized
 * licensee is strictly subject to the terms and conditions,
 * including confidentiality obligations, set forth in the applicable
 * License and Co-Branding Agreement between Citrix Online LLC and
 * the licensee.
 */

package com.citrix.g2w.reporting.write;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.citrix.g2w.reporting.events.webinars.WebinarReassignmentEvent;
import com.citrix.g2w.reporting.write.webinars.WebinarReassignmentEventListener;
import com.citrix.queue.support.PollingMessageListenerContainer;
import com.citrix.queue.support.QueueServiceImpl;
import com.google.common.collect.Lists;

/**
 * Configuration class needed to initialize MongoDB.
 * @author ankit
 *
 */
@Configuration
@ComponentScan
@Getter
@Setter
@ConfigurationProperties(prefix = "reporting.queues")
public class QueueConfiguration {
	
	private String prefix;
	private String listener;
	private String servers;
	private int readTimeout;
	private int connectTimeout;
	
	public PollingMessageListenerContainer<WebinarReassignmentEvent> webinarReassignmentEventListener() {
		PollingMessageListenerContainer<WebinarReassignmentEvent> container = new PollingMessageListenerContainer<WebinarReassignmentEvent>();
		container.setQueueService(queueService());
		container.setQueueName(prefix + "WebinarReassignmentEvent" + listener);
		container.setServiceAddresses(servers);
		container.setResponseType(WebinarReassignmentEvent.class);
		container.setTransactional(true);
		container.setMessageListener(new WebinarReassignmentEventListener());
		return container;
	}
	
	public QueueServiceImpl queueService() {
		QueueServiceImpl queueService = new QueueServiceImpl();
		queueService.setRestTemplate(jsonRestTemplate());
		return queueService;
	}
	
	public RestTemplate jsonRestTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(httpRequestFactory());
		List<HttpMessageConverter<?>> messageConverters = Lists.newArrayList();
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		messageConverters.add(converter);
		restTemplate.setMessageConverters(messageConverters);
		return restTemplate;
	}
	
	public SimpleClientHttpRequestFactory httpRequestFactory() {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setReadTimeout(readTimeout);
		requestFactory.setConnectTimeout(connectTimeout);
		return requestFactory;
	}
}
