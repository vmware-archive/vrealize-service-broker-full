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

package org.cloudfoundry.community.servicebroker.vrealize.adapter;

import com.jayway.jsonpath.DocumentContext;
import org.cloudfoundry.community.servicebroker.vrealize.persistance.VrServiceInstance;

import java.util.Map;

public interface Adaptor {

    Map<String, Object> getCredentials(VrServiceInstance instance);

    String getServiceType();

    void prepareRequest(DocumentContext ctx, VrServiceInstance instance);

}
