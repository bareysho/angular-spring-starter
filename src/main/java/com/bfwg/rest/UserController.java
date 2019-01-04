package com.bfwg.rest;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import org.brunocvcunha.instagram4j.Instagram4j;
//import org.brunocvcunha.instagram4j.Instagram4j;
import com.bfwg.instagram4j.Instagram4j;
import com.bfwg.instagram4j.requests.InstagramSearchUsernameRequest;
import com.bfwg.model.InstagramAccount;
import com.bfwg.service.InstagramService;
import org.apache.http.client.CookieStore;
import org.brunocvcunha.instagram4j.requests.InstagramGetUserReelMediaFeedRequest;
import org.brunocvcunha.instagram4j.requests.InstagramLikeRequest;
import org.brunocvcunha.instagram4j.requests.InstagramUserFeedRequest;
import org.brunocvcunha.instagram4j.requests.payload.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import com.bfwg.exception.ResourceConflictException;
import com.bfwg.model.User;
import com.bfwg.model.UserRequest;
import com.bfwg.service.UserService;

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

    @RequestMapping(method = POST, value = "/instagram-login")
    @PreAuthorize("hasRole('USER')")
    public InstagramUser instagramLogin(@RequestParam String username,
                                              @RequestParam String password) throws IOException, ClassNotFoundException {

////        Instagram4j instagram = new Instagram4j(username, password);
////        instagram.setLoggedIn(true);
////
////        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(username + ".dat"));
////        oos.writeObject(instagram);
////        oos.close();
////
////        instagram.setup();
////        instagram.login();
////
////        oos = new ObjectOutputStream(new FileOutputStream(username + "Cookie.dat"));
////        oos.writeObject(instagram.getCookieStore());
////        oos.close();
////
////
////        oos = new ObjectOutputStream(new FileOutputStream( username + "Uuid.dat"));
////        oos.writeChars(instagram.getUuid());
////        oos.close();
////
////        System.out.println(instagram.getUsername());
////        System.out.println(instagram.isLoggedIn());
//
//        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(username + ".dat"));
//        Instagram4j instagram2 = (Instagram4j) ois.readObject();
//        ois.close();
//
//        ois = new ObjectInputStream(new FileInputStream(username + "Uuid.dat"));
//        String uuid = ois.readUTF();
//        ois.close();
//
//        ois = new ObjectInputStream(new FileInputStream(username + "Cookie.dat"));
//        CookieStore cookieStore = (CookieStore) ois.readObject();
//        ois.close();
//        System.out.println("-------------------------------------");
//
//        instagram2.setUuid(uuid);
//        instagram2.setCookieStore(cookieStore);
//        instagram2.setup();
//
//        System.out.println(instagram2.getUsername());
//        System.out.println(instagram2.isLoggedIn());
//
//        InstagramSearchUsernameResult userResult = instagram2.sendRequest(new InstagramSearchUsernameRequest(username));
//        return userResult.getUser();




        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<InstagramAccount> accounts = instagramService.findByOwner_id(user.getId());
        System.out.println(accounts.size());
        System.out.println(username);
        System.out.println(password);
        System.out.println(username.equals("unauth"));
        System.out.println(password.equals("unauth"));
        if (accounts.size() == 0) {
            if (username.equals("unauth") && password.equals("unauth")) {
                return null;
            }
            InstagramAccount instagramAccount = new InstagramAccount();

            Instagram4j instagram = new Instagram4j(username, password);
            instagram.setLoggedIn(true);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(instagram);
            instagramAccount.setInstagram4j(out.toByteArray());
            oos.close();

            instagram.setup();
            instagram.login();

            out = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(out);
            oos.writeObject(instagram.getCookieStore());
            instagramAccount.setCookieStore(out.toByteArray());
            oos.close();

            instagramAccount.setUuid(instagram.getUuid());
            instagramAccount.setOwner(user);
            instagramService.save(instagramAccount);
            System.out.println(instagram.getUsername());
            System.out.println(instagram.isLoggedIn());
            InstagramSearchUsernameResult userResult = instagram.sendRequest(new InstagramSearchUsernameRequest(username));
            return userResult.getUser();
        }
        InstagramAccount instagramAccount = accounts.get(0);
        ByteArrayInputStream in = new ByteArrayInputStream(instagramAccount.getInstagram4j());
        ObjectInputStream ois = new ObjectInputStream(in);
        Instagram4j instagram2 = (Instagram4j) ois.readObject();
        ois.close();

        in = new ByteArrayInputStream(instagramAccount.getCookieStore());
        ois = new ObjectInputStream(in);
        CookieStore cookieStore = (CookieStore) ois.readObject();
        ois.close();

        System.out.println("-------------------------------------");

        instagram2.setUuid(instagramAccount.getUuid());
        instagram2.setCookieStore(cookieStore);
        instagram2.setup();

        System.out.println(instagram2.getUsername());
        System.out.println(instagram2.isLoggedIn());

        InstagramSearchUsernameResult userResult = instagram2.sendRequest(new InstagramSearchUsernameRequest("bareysho"));
        return userResult.getUser();
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

}
