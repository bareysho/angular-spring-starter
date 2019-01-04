package com.bfwg.model;


import com.bfwg.instagram4j.requests.payload.InstagramLoginResult;

import java.io.Serializable;

public class SerializibleInsta implements Serializable {

    private InstagramLoginResult instagramLoginResult;

    public SerializibleInsta(InstagramLoginResult instagramLoginResult) {
        this.instagramLoginResult = instagramLoginResult;
    }

    public InstagramLoginResult getInstagramLoginResult() {
        return instagramLoginResult;
    }

    public void setInstagramLoginResult(InstagramLoginResult instagramLoginResult) {
        this.instagramLoginResult = instagramLoginResult;
    }
}
