/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
 */

package com.kuflow.engine.samples.worker;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuflow.engine.samples.worker.ApplicationProperties.TemporalProperties.MutualTlsProperties;
import com.kuflow.rest.KuFlowRestClient;
import com.kuflow.temporal.common.authorization.KuFlowAuthorizationTokenSupplier;
import com.kuflow.temporal.common.ssl.SslContextBuilder;
import com.kuflow.temporal.common.tracing.MDCContextPropagator;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.temporal.authorization.AuthorizationGrpcMetadataProvider;
import io.temporal.client.ActivityCompletionClient;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.common.converter.DataConverter;
import io.temporal.common.converter.DefaultDataConverter;
import io.temporal.common.converter.JacksonJsonPayloadConverter;
import io.temporal.common.converter.PayloadConverter;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.serviceclient.WorkflowServiceStubsOptions.Builder;
import io.temporal.worker.WorkerFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class TemporalConfiguration {

    private final ApplicationProperties applicationProperties;

    private final KuFlowRestClient kuFlowRestClient;

    public TemporalConfiguration(ApplicationProperties applicationProperties, KuFlowRestClient kuFlowRestClient) {
        this.applicationProperties = applicationProperties;
        this.kuFlowRestClient = kuFlowRestClient;
    }

    @Bean(destroyMethod = "shutdown")
    public WorkflowServiceStubs workflowServiceStubs() {
        Builder builder = WorkflowServiceStubsOptions.newBuilder();
        builder.setTarget(this.applicationProperties.getTemporal().getTarget());
        builder.setSslContext(this.createSslContext());
        builder.addGrpcMetadataProvider(new AuthorizationGrpcMetadataProvider(new KuFlowAuthorizationTokenSupplier(this.kuFlowRestClient)));

        WorkflowServiceStubsOptions options = builder.validateAndBuildWithDefaults();

        return WorkflowServiceStubs.newServiceStubs(options);
    }

    @Bean
    public DataConverter dataConverter() {
        // Customize Temporal's default Jackson object mapper to support unknown properties
        ObjectMapper objectMapper = JacksonJsonPayloadConverter.newDefaultObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        List<PayloadConverter> converters = new LinkedList<>();
        converters.addAll(
            Arrays
                .stream(DefaultDataConverter.STANDARD_PAYLOAD_CONVERTERS)
                .filter(it -> !(it instanceof JacksonJsonPayloadConverter))
                .toList()
        );
        converters.add(new JacksonJsonPayloadConverter(objectMapper));

        return new DefaultDataConverter(converters.toArray(new PayloadConverter[0]));
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs service, DataConverter dataConverter) {
        WorkflowClientOptions options = WorkflowClientOptions
            .newBuilder()
            .setNamespace(this.applicationProperties.getTemporal().getNamespace())
            .setContextPropagators(Collections.singletonList(new MDCContextPropagator()))
            .setDataConverter(dataConverter)
            .build();

        return WorkflowClient.newInstance(service, options);
    }

    @Bean
    public WorkerFactory workerFactory(WorkflowClient workflowClient) {
        return WorkerFactory.newInstance(workflowClient);
    }

    @Bean
    public ActivityCompletionClient activityCompletionClient(WorkflowClient workflowClient) {
        return workflowClient.newActivityCompletionClient();
    }

    private SslContext createSslContext() {
        MutualTlsProperties mutualTls = this.applicationProperties.getTemporal().getMutualTls();

        return SslContextBuilder
            .builder()
            .withCa(mutualTls.getCa())
            .withCaData(mutualTls.getCaData())
            .withCert(mutualTls.getCert())
            .withCertData(mutualTls.getCertData())
            .withKey(mutualTls.getKey())
            .withKeyData(mutualTls.getKeyData())
            .build();
    }
}
