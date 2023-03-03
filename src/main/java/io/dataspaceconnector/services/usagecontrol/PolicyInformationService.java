/*
 * Copyright 2020 Fraunhofer Institute for Software and Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dataspaceconnector.services.usagecontrol;

import com.tecnalia.datausage.utils.HttpUtils;

import java.io.UnsupportedEncodingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;

//TECNALIA-ICT-OPTMA: All the methods in this class have been modified.
/**
 * This class provides access permission information for the {@link RuleValidator} depending on
 * the policy content.
 */
@Service
@RequiredArgsConstructor
public class PolicyInformationService {

    @Autowired
    private final HttpUtils httpUtils;

    /**
     * Get access number of artifact.
     * @param pipEndpoint pipEndpoint
     * @param targetId  The target id.
     * @param consumerUri consumer URI
     * @return The artifact's access number.
     */
    public long getAccessNumber(final URI pipEndpoint, final String targetId, final String consumerUri) {
        long numAccessed = -1;
        try {
            String encodedTargetUri =  URLEncoder.encode(targetId, StandardCharsets.UTF_8.toString());
            String encodedConsumerUri = URLEncoder.encode(consumerUri, StandardCharsets.UTF_8.toString());
            String accessed = httpUtils.sendHttpGetRequest(
                    pipEndpoint + "?targetUri="+ encodedTargetUri + "&consumerUri="+ encodedConsumerUri);
            numAccessed = Integer.parseInt(accessed);
        } catch (UnsupportedEncodingException | URISyntaxException | RuntimeException e) {
        }
        return numAccessed;
        
    }

    /**
     * Get remote info.
     * @param pipEndpoint pip Endpoint
     * @return The info.
     */
    public String getRemoteInfo(final String pipEndpoint) {
        String info = "";
        try {
            info = httpUtils.sendHttpGetRequest(pipEndpoint);
        } catch (URISyntaxException | RuntimeException e) {
        }
        return info;        
    }
}
