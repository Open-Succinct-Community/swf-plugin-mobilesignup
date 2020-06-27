package com.venky.swf.plugins.mobilesignup.db.model;

import com.venky.swf.db.annotations.column.UNIQUE_KEY;
import com.venky.swf.db.model.Model;
import com.venky.swf.plugins.collab.db.model.user.Phone;

public interface SignUp extends Phone, Model {

    @UNIQUE_KEY("PHONE")
    String getPhoneNumber();

    String getDeviceId();
    void setDeviceId(String deviceId);

    Long getUserId();
    void setUserId(Long userId);
    User getUser();
}
