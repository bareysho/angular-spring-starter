package com.bfwg.rest;

import com.bfwg.exception.ResourceConflictException;
import com.bfwg.instagram4j.Instagram4j;
import com.bfwg.instagram4j.requests.InstagramLikeRequest;
import com.bfwg.instagram4j.requests.InstagramSearchUsernameRequest;
import com.bfwg.instagram4j.requests.InstagramUserFeedRequest;
import com.bfwg.instagram4j.requests.payload.*;
import com.bfwg.model.InstagramAccount;
import com.bfwg.model.User;
import com.bfwg.model.UserRequest;
import com.bfwg.service.InstagramService;
import com.bfwg.service.UserService;
import org.apache.http.client.CookieStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

//import org.brunocvcunha.instagram4j.Instagram4j;
//import org.brunocvcunha.instagram4j.Instagram4j;

/**
 * Created by fan.jin on 2016-10-15.
 */

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private InstagramService instagramService;


    @RequestMapping(method = GET, value = "/user/{userId}")
    public User loadById(@PathVariable Long userId) {
        return this.userService.findById(userId);
    }

    @RequestMapping(method = GET, value = "/user/all")
    public List<User> loadAll() {
        return this.userService.findAll();
    }

    @RequestMapping(method = GET, value = "/user/reset-credentials")
    public ResponseEntity<Map> resetCredentials() {
        this.userService.resetCredentials();
        Map<String, String> result = new HashMap<>();
        result.put("result", "success");
        return ResponseEntity.accepted().body(result);
    }

    @RequestMapping(method = GET, value = "/user-posts/{username}/{uuid}")
    public List<InstagramFeedItem> getUserPosts(@PathVariable String username, @PathVariable String uuid) throws IOException, ClassNotFoundException {
        InstagramAccount instagramAccount = instagramService.findByUuid(uuid);

        Instagram4j instagram = (Instagram4j) UserController.byteArrayToObject(instagramAccount.getInstagram4j());
        CookieStore cookieStore = (CookieStore) UserController.byteArrayToObject(instagramAccount.getCookieStore());

        instagram.setUuid(instagramAccount.getUuid());
        instagram.setCookieStore(cookieStore);
        instagram.setup();

        InstagramSearchUsernameResult userResult = instagram.sendRequest(new InstagramSearchUsernameRequest(username));
        long id = userResult.getUser().getPk();
        List<InstagramFeedItem> instagramFeedItems = instagram.sendRequest(new InstagramUserFeedRequest(id)).getItems();
        System.out.println(instagramFeedItems);
        return instagramFeedItems;
    }

    @RequestMapping(method = GET, value = "/user-instagram-accounts")
    public List<InstagramAccount> getUserInstagramAccount() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return instagramService.findByOwner_id(user.getId());
    }

    @RequestMapping(method = POST, value = "/instagram/like-post")
    @PreAuthorize("hasRole('USER')")
    public InstagramLikeResult instagramPostLike (@RequestParam long postId,
                                        @RequestParam String uuid) throws IOException, ClassNotFoundException {
        System.out.println(postId);
        InstagramAccount instagramAccount = instagramService.findByUuid(uuid);

        Instagram4j instagram = (Instagram4j) UserController.byteArrayToObject(instagramAccount.getInstagram4j());
        CookieStore cookieStore = (CookieStore) UserController.byteArrayToObject(instagramAccount.getCookieStore());

        instagram.setUuid(instagramAccount.getUuid());
        instagram.setCookieStore(cookieStore);
        instagram.setup();


        return instagram.sendRequest(new InstagramLikeRequest(postId));
    }

    @RequestMapping(method = POST, value = "/claim-instagram")
    @PreAuthorize("hasRole('USER')")
    public InstagramUser instagramLogin(@RequestParam String username,
                                        @RequestParam String password) throws IOException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        InstagramAccount instagramAccount = new InstagramAccount();

        Instagram4j instagram = new Instagram4j(username, password);
        instagram.setLoggedIn(true);

        instagramAccount.setInstagram4j(UserController.objectToByteArray(instagram));

        instagram.setup();
        InstagramLoggedUser instagramUser = instagram.login().getLogged_in_user();

        instagramAccount.setUuid(instagram.getUuid());
        instagramAccount.setUsername(instagramUser.getUsername());
        if (instagramUser.getProfile_pic_url().length() == 0) {
            instagramAccount.setProfilePic("https://cdn.icon-icons.com/icons2/838/PNG/512/circle-instagram_icon-icons.com_66832.png");
        } else {
            instagramAccount.setProfilePic(instagramUser.getProfile_pic_url());
        }
        instagramAccount.setProfileUrl("https://www.instagram.com/" + username);
        instagramAccount.setCookieStore(UserController.objectToByteArray(instagram.getCookieStore()));
        instagramAccount.setOwner(user);
        instagramService.save(instagramAccount);
        InstagramSearchUsernameResult userResult = instagram.sendRequest(new InstagramSearchUsernameRequest(username));
        return userResult.getUser();
//        List<InstagramAccount> accounts = instagramService.findByOwner_id(user.getId());
//        InstagramAccount instagramAccount = accounts.get(0);
//        ByteArrayInputStream in = new ByteArrayInputStream(instagramAccount.getInstagram4j());
//        ObjectInputStream ois = new ObjectInputStream(in);
//        Instagram4j instagram2 = (Instagram4j) ois.readObject();
//        ois.close();
//
//        in = new ByteArrayInputStream(instagramAccount.getCookieStore());
//        ois = new ObjectInputStream(in);
//        CookieStore cookieStore = (CookieStore) ois.readObject();
//        ois.close();
//
//        System.out.println("-------------------------------------");
//
//        instagram2.setUuid(instagramAccount.getUuid());
//        instagram2.setCookieStore(cookieStore);
//        instagram2.setup();
//
//
//        System.out.println(instagram2.getUsername());
//        System.out.println(instagram2.isLoggedIn());
//
//        InstagramSearchUsernameResult userResult = instagram2.sendRequest(new InstagramSearchUsernameRequest("bareysho"));
//        return userResult.getUser();
    }


    @RequestMapping(method = POST, value = "/signup")
    public ResponseEntity<?> addUser(@RequestBody UserRequest userRequest,
                                     UriComponentsBuilder ucBuilder) {

        User existUser = this.userService.findByUsername(userRequest.getUsername());
        if (existUser != null) {
            throw new ResourceConflictException(userRequest.getId(), "Username already exists");
        }
        User user = this.userService.save(userRequest);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/api/user/{userId}").buildAndExpand(user.getId()).toUri());
        return new ResponseEntity<User>(user, HttpStatus.CREATED);
    }

    /*
     * We are not using userService.findByUsername here(we could), so it is good that we are making
     * sure that the user has role "ROLE_USER" to access this endpoint.
     */
    @RequestMapping("/whoami")
    @PreAuthorize("hasRole('USER')")
    public User user() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static byte[] objectToByteArray (Object object) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(object);
        oos.close();
        return out.toByteArray();
    }

    public static Object byteArrayToObject(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(in);
        Object object = ois.readObject();
        ois.close();
        return object;
    }

}
