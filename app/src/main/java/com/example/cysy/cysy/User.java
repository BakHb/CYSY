package com.example.cysy.cysy;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by youssouf on 28/04/17.
 */

class User {
    public static final String[] UserArgs = new String[]{"pseudo", "password", "name", "address", "email", "mobile"};

    public String pseudo, password, name, address, email, mobile;

    public User(String pseudo, String password, String name, String address, String email, String mobile) {
        this.pseudo = pseudo;
        this.password = password;
        this.name = name;
        this.address = address;
        this.email = email;
        this.mobile = mobile;
    }

    public static boolean isUserArg(String fieldName) {
        for (String userArg : UserArgs){
            if(userArg.equals(fieldName)) {
                return true;
            }
        }
        return false;
    }
}
