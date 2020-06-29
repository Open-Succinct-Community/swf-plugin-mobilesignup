package com.venky.swf.plugins.mobilesignup.db.model;

import com.venky.swf.db.Database;
import com.venky.swf.plugins.collab.db.model.user.PhoneImpl;

public class SignUpImpl extends PhoneImpl<SignUp> {
    public SignUpImpl(){
        super();
    }
    public SignUpImpl(SignUp proxy){
        super(proxy);
    }

    public User getLastSignedUpUser() {
        SignUp proxy  = getProxy();
        User user = Database.getTable(User.class).newRecord();
        user.setPhoneNumber(proxy.getPhoneNumber());
        User exists = Database.getTable(User.class).getRefreshed(user);
        if (!exists.getRawRecord().isNewRecord()){
            return exists;
        }else {
            return null;
        }
    }
}
