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
package com.bfwg.instagram4j.requests;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import com.bfwg.instagram4j.requests.internal.InstagramConfigureAlbumRequest;
import com.bfwg.instagram4j.requests.internal.InstagramExposeRequest;
import com.bfwg.instagram4j.requests.payload.InstagramConfigureAlbumResult;
import com.bfwg.instagram4j.requests.payload.StatusResult;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

/**
 * InstagramUploadAlbumRequest
 * 
 * @author Justin Vo
 *
 */

@Log4j
@RequiredArgsConstructor
public class InstagramUploadAlbumRequest extends InstagramPostRequest<InstagramConfigureAlbumResult> {
    @NonNull
    private Collection<File> imageFile;
    
    @NonNull
    private String caption;
    
    @Override
    public String getUrl() {
        return "upload/photo/";
    }

    @Override
    public String getMethod() {
        return "POST";
    }
    
    @Override
    public InstagramConfigureAlbumResult execute() throws ClientProtocolException, IOException {
        int count = 0;
        long stamp = System.currentTimeMillis();
        List<String> uploadIds = new LinkedList<>();
        for(File f : imageFile) {
            HttpPost post = createHttpRequest();
            String uploadId = Long.toString(stamp + count++);
            post.setEntity(createMultipartEntity(f, uploadId));
            
            try (CloseableHttpResponse response = api.getClient().execute(post)) {
                api.setLastResponse(response);
                
                int resultCode = response.getStatusLine().getStatusCode();
                String content = EntityUtils.toString(response.getEntity());
                
                log.info("Photo Upload result: " + resultCode + ", " + content);
                
                post.releaseConnection();
        
                StatusResult result = parseResult(resultCode, content);
                
                if (!result.getStatus().equalsIgnoreCase("ok")) {
                    throw new RuntimeException("Error happened in photo upload: " + result.getMessage());
                }
                uploadIds.add(uploadId);
            }
        }
        
        InstagramConfigureAlbumResult configurePhotoResult = api.sendRequest(new InstagramConfigureAlbumRequest(uploadIds, caption));
        
        System.out.println("Configure photo result: " + configurePhotoResult);
        if (!configurePhotoResult.getStatus().equalsIgnoreCase("ok")) {
            throw new IllegalArgumentException("Failed to configure image: " + configurePhotoResult.getMessage());
        }
        
        StatusResult exposeResult = api.sendRequest(new InstagramExposeRequest());
        System.out.println("Expose result: " + exposeResult);
        if (!exposeResult.getStatus().equalsIgnoreCase("ok")) {
            throw new IllegalArgumentException("Failed to expose image: " + exposeResult.getMessage());
        }

        return configurePhotoResult;
        
    }

    /**
     * Creates required multipart entity with the image binary
     * @return HttpEntity to send on the post
     * @throws ClientProtocolException
     * @throws IOException
     */
    protected HttpEntity createMultipartEntity(File imageFile, String uploadId) throws ClientProtocolException, IOException {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("upload_id", uploadId);
        builder.addTextBody("_uuid", api.getUuid());
        builder.addTextBody("_csrftoken", api.getOrFetchCsrf());
        builder.addTextBody("image_compression", "{\"lib_name\":\"jt\",\"lib_version\":\"1.3.0\",\"quality\":\"87\"}");
        builder.addBinaryBody("photo", imageFile, ContentType.APPLICATION_OCTET_STREAM, "pending_media_" + uploadId + ".jpg");
        builder.setBoundary(api.getUuid());

        HttpEntity entity = builder.build();
        return entity;
    }

    /**
     * Creates the Post Request
     * @return Request
     */
    protected HttpPost createHttpRequest() {
        String url = com.bfwg.instagram4j.InstagramConstants.API_URL + getUrl();
        System.out.println("URL Upload: " + url);

        HttpPost post = new HttpPost(url);
        post.addHeader("X-IG-Capabilities", "3Q4=");
        post.addHeader("X-IG-Connection-Type", "WIFI");
        post.addHeader("Cookie2", "$Version=1");
        post.addHeader("Accept-Language", "en-US");
        post.addHeader("Accept-Encoding", "gzip, deflate");
        post.addHeader("Connection", "close");
        post.addHeader("Content-Type", "multipart/form-data; boundary=" + api.getUuid());
        post.addHeader("User-Agent", com.bfwg.instagram4j.InstagramConstants.USER_AGENT);
        return post;
    }

    @Override
    public InstagramConfigureAlbumResult parseResult(int statusCode, String content) {
        return parseJson(statusCode, content, InstagramConfigureAlbumResult.class);
    }
}
