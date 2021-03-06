/**
 * Copyright (C) 2016 Bruno Candido Volpato da Cunha (brunocvcunha@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bfwg.instagram4j.requests.internal;

import java.util.LinkedHashMap;
import java.util.Map;

import com.bfwg.instagram4j.requests.payload.InstagramSyncFeaturesResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;

/**
 * Sync Features Request
 * 
 * @author Bruno Candido Volpato da Cunha
 *
 */
@AllArgsConstructor
@Log4j
public class InstagramSyncFeaturesRequest extends com.bfwg.instagram4j.requests.InstagramPostRequest<InstagramSyncFeaturesResult> {

    private boolean preLogin = false;
    
    @Override
    public String getUrl() {
        return "qe/sync/";
    }

    @Override
    @SneakyThrows
    public String getPayload() {
        
        Map<String, Object> likeMap = new LinkedHashMap<>();
        likeMap.put("id", api.getUuid());
        likeMap.put("experiments", com.bfwg.instagram4j.InstagramConstants.DEVICE_EXPERIMENTS);
        
        if (!preLogin) {
            likeMap.put("_uuid", api.getUuid());
            likeMap.put("_uid", api.getUserId());
            likeMap.put("_csrftoken", api.getOrFetchCsrf());
            
        }
        
        ObjectMapper mapper = new ObjectMapper();
        String payloadJson = mapper.writeValueAsString(likeMap);

        return payloadJson;
    }

    @Override
    @SneakyThrows
    public com.bfwg.instagram4j.requests.payload.InstagramSyncFeaturesResult parseResult(int statusCode, String content) {
        return parseJson(statusCode, content, com.bfwg.instagram4j.requests.payload.InstagramSyncFeaturesResult.class);
    }

    /**
     * @return if request must be logged in
     */
    @Override
    public boolean requiresLogin() {
        return false;
    }

}
