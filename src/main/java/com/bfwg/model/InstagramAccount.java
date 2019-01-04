package com.bfwg.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@Table(name = "instagram_accounts")
public class InstagramAccount implements Serializable {
    @Id
    @JsonIgnore
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "username")
    private String username;

    @Column(name = "profile_pic")
    private String profilePic;

    @Column(name = "profile_url")
    private String profileUrl;

    @JsonIgnore
    @Column(name = "cookie_store")
    private byte[] cookieStore;

    @JsonIgnore
    @Column(name = "instagram4j")
    private byte[] instagram4j;

    @JsonIgnore
    @ManyToOne(optional = false, cascade = CascadeType.MERGE)
    @JoinColumn(name = "owner_id")
    private User owner;
}
