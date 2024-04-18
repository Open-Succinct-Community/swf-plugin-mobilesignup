package com.venky.swf.plugins.mobilesignup.db.model;

import com.venky.swf.db.Database;
import com.venky.swf.db.model.reflection.ModelReflector;
import com.venky.swf.db.table.ModelImpl;
import com.venky.swf.plugins.collab.db.model.user.Email;
import com.venky.swf.plugins.collab.db.model.user.EmailImpl;
import com.venky.swf.plugins.collab.db.model.user.OtpEnabledImpl;
import com.venky.swf.plugins.collab.db.model.user.PhoneImpl;

public class SignUpImpl extends OtpEnabledImpl<SignUp> {
    public SignUpImpl(){
        super();
    }

    OtpEnabledImpl<SignUp> otpEnabledImpl ;

    public SignUpImpl(SignUp proxy){
        super(proxy);
        this.otpEnabledImpl = SignUp.isSignUpKeyPhoneNumber() ? new PhoneImpl<>(proxy)  :new EmailImpl<>(proxy);
    }

    @Override
    public void sendOtp(boolean generateFresh) {
        otpEnabledImpl.sendOtp(generateFresh);
    }

    public String getDomain(){
        return null;
    }

    public User getLastSignedUpUser() {
        SignUp proxy  = getProxy();
        User user = Database.getTable(User.class).newRecord();
        if (SignUp.isSignUpKeyPhoneNumber()){
            user.setPhoneNumber(proxy.getPhoneNumber());
        }else {
            user.setEmail(proxy.getEmail());
        }

        User exists = Database.getTable(User.class).getRefreshed(user);
        if (!exists.getRawRecord().isNewRecord()){
            return exists;
        }else {
            return null;
        }
    }
}
