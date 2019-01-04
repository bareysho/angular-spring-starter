package com.bfwg.model;

import lombok.Data;
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
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "cookie_store")
    private byte[] cookieStore;

    @Column(name = "instagram4j")
    private byte[] instagram4j;

    @ManyToOne(optional = false, cascade = CascadeType.MERGE)
    @JoinColumn(name = "owner_id")
    private User owner;
}
