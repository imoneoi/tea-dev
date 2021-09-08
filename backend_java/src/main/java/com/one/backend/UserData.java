package com.one.backend;

import java.io.Serializable;

public class UserData implements Serializable {
    // basic
    public String username;
    public String passwd_hash;

    // auth
    public String session;

    // data
    public String data;
}
