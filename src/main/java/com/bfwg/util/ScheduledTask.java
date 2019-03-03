package com.bfwg.util;

import com.bfwg.instagram4j.Instagram4j;
import com.bfwg.instagram4j.requests.InstagramUploadPhotoRequest;
import com.bfwg.instagram4j.requests.payload.InstagramConfigurePhotoResult;
import com.bfwg.model.InstagramAccount;
import com.bfwg.model.ScheduledPost;
import com.bfwg.rest.UserController;
import com.bfwg.service.InstagramService;
import com.bfwg.service.ScheduledPostService;
import org.apache.http.client.CookieStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
public class ScheduledTask {

    @Autowired
    private ScheduledPostService scheduledPostService;

    @Autowired
    private InstagramService instagramService;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Scheduled(fixedRate = 5000)
    public void reportCurrentTime() throws IOException, ClassNotFoundException {
        String dateToExecute = dateFormat.format(new Date());
        System.out.println(dateToExecute);
        List<ScheduledPost> scheduledPosts = scheduledPostService.findByDate(dateToExecute);
        System.out.println(scheduledPosts);
        for (ScheduledPost sp : scheduledPosts) {
            String str = new String(sp.getFile(), StandardCharsets.UTF_8);
            BufferedImage bufferedImage = Util.decodeToImage(str);
            String comment = sp.getComment();
            String uuid = sp.getUuid();

            InstagramAccount instagramAccount = instagramService.findByUuid(uuid);

            Instagram4j instagram = (Instagram4j) Util.byteArrayToObject(instagramAccount.getInstagram4j());
            CookieStore cookieStore = (CookieStore) Util.byteArrayToObject(instagramAccount.getCookieStore());

            instagram.setUuid(instagramAccount.getUuid());
            instagram.setCookieStore(cookieStore);
            instagram.setup();

            InstagramConfigurePhotoResult configurePhotoResult = instagram.sendRequest(new InstagramUploadPhotoRequest(bufferedImage, comment, null));
            System.out.println(configurePhotoResult.getStatus());
            scheduledPostService.deleteById(sp.getId());
        }
    }
}
