package com.venky.swf.plugins.mobilesignup.db.model;

import com.venky.geo.GeoLocation;
import com.venky.swf.db.model.Model;

import java.sql.Timestamp;

public interface UserLogin extends Model , GeoLocation {
    public long getUserId();
    public void setUserId(long id);

    public String getUserAgent();
    public void setUserAgent(String agent);

    public Timestamp getLoginTime();
    public void setLoginTime(Timestamp loginTime);

    public String getFromIp();
    public void setFromIp(String fromIp);



}
