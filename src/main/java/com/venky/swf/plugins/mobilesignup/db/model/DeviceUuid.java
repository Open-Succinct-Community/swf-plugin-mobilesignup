package com.venky.swf.plugins.mobilesignup.db.model;

import com.venky.swf.db.annotations.column.UNIQUE_KEY;
import com.venky.swf.db.model.Model;

/*
These are the devices user has validated otp from.
 */
public interface DeviceUuid extends Model {
    @UNIQUE_KEY
    public long getUserId();
    public void setUserId(long id);
    public User getUser();

    @UNIQUE_KEY
    public String getDeviceUuid();
    public void setDeviceUuid(String uuid);

}
