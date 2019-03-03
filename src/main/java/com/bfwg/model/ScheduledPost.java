package com.bfwg.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;

@Entity
@Data
@Table(name = "scheduled_posts")
public class ScheduledPost implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "comment")
    private String comment;

    @Column(name = "file")
    private byte[] file;

    @Column(name = "date")
    private String date;

    @Column(name = "uuid")
    private String uuid;
}
