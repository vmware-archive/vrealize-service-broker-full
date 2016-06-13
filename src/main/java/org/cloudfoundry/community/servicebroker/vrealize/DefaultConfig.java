/**
 * vrealize-service-broker
 * <p>
 * Copyright (c) 2015-Present Pivotal Software, Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * limitations under the License.
 */

package org.cloudfoundry.community.servicebroker.vrealize;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.cloudfoundry.community.servicebroker.vrealize.domain.CatalogTranslator;
import org.cloudfoundry.community.servicebroker.vrealize.domain.PlanTranslator;
import org.cloudfoundry.community.servicebroker.vrealize.domain.ServiceDefinitionTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.feign.support.ResponseEntityDecoder;
import org.springframework.cloud.servicebroker.model.BrokerApiVersion;
import org.springframework.cloud.servicebroker.model.Catalog;
import org.springframework.cloud.servicebroker.model.Plan;
import org.springframework.cloud.servicebroker.model.ServiceDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultConfig {

    @Autowired
    String serviceUri;

    @Bean
    public BrokerApiVersion brokerApiVersion() {
        return new BrokerApiVersion("2.7");
    }

    @Bean
    Gson gson() {
        GsonBuilder builder = new GsonBuilder();
        CatalogTranslator catalogTranslator = new CatalogTranslator();
        ServiceDefinitionTranslator serviceDefinitionTranslator = new ServiceDefinitionTranslator();
        PlanTranslator planTranslator = new PlanTranslator();

        builder.registerTypeAdapter(Catalog.class, catalogTranslator);
        builder.registerTypeAdapter(ServiceDefinition.class,
                serviceDefinitionTranslator);
        builder.registerTypeAdapter(Plan.class, planTranslator);

        builder.setPrettyPrinting();
        Gson gson = builder.create();
        catalogTranslator.setGson(gson);
        serviceDefinitionTranslator.setGson(gson);

        return gson;
    }

    @Bean
    public VraRepository vraRepository() {
        return Feign.builder().encoder(new GsonEncoder())
                .decoder(new ResponseEntityDecoder(new GsonDecoder()))
                .target(VraRepository.class, serviceUri);
    }

}