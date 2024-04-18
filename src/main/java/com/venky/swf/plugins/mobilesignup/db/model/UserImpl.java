package com.venky.swf.plugins.mobilesignup.db.model;

import com.venky.core.date.DateUtils;
import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.model.Model;
import com.venky.swf.db.model.UserEmail;
import com.venky.swf.db.model.UserLogin;
import com.venky.swf.db.model.reflection.ModelReflector;
import com.venky.swf.db.table.ModelImpl;
import com.venky.swf.plugins.collab.db.model.user.OtpEnabled;
import com.venky.swf.plugins.collab.db.model.user.UserPhone;
import com.venky.swf.sql.Expression;
import com.venky.swf.sql.Operator;
import com.venky.swf.sql.Select;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UserImpl extends ModelImpl<User> {
    public UserImpl(){
        super();
    }
    public UserImpl(User user){
        super(user);
    }

    public Timestamp getLastLoginTime() {
        List<UserLogin> logins = new Select().from(UserLogin.class).
                where(new Expression(ModelReflector.instance(UserLogin.class).getPool(),"USER_ID",
                        Operator.EQ,getProxy().getId()))
                .orderBy("USER_ID","LOGIN_TIME DESC").execute(1);
        if (logins.isEmpty()){
            return null;
        }else {
            UserLogin login = logins.get(0);
            return login.getLoginTime();
        }
    }

    public boolean isActive(){
        User user = getProxy();
        Timestamp lastLoginTime =  user.getLastLoginTime();
        return (lastLoginTime == null ||
                DateUtils.compareToMinutes(System.currentTimeMillis(), lastLoginTime.getTime()) < 90 * 24* 60 );
    }

    public <T extends Model & OtpEnabled> void deactivate() {
        User user = getProxy();

        ModelReflector<User> userRef = ModelReflector.instance(User.class);
        String signUpKey = SignUp.getSignUpKey();
        String signUpKeyValue = userRef.get(user,signUpKey);
        if (ObjectUtil.isVoid(signUpKeyValue)){
            return;
        }

        userRef.set(user,SignUp.getSignUpKey(),null);
        List<OtpEnabled> registeredSignUpKeys = new ArrayList<>();

        ModelReflector<? extends OtpEnabled> otpEnableRef = null;
        if (SignUp.isSignUpKeyPhoneNumber()){
            otpEnableRef = ModelReflector.instance(UserPhone.class);
            registeredSignUpKeys.addAll(user.getUserPhones());
        }else {
            otpEnableRef = ModelReflector.instance(com.venky.swf.plugins.collab.db.model.user.UserEmail.class);
            user.getUserEmails().forEach(ue->registeredSignUpKeys.add(ue.getRawRecord().getAsProxy(com.venky.swf.plugins.collab.db.model.user.UserEmail.class)));
        }
        for (OtpEnabled aKey : registeredSignUpKeys){
            String aSignUpKeyValue = otpEnableRef.get(aKey,signUpKey);
            if (ObjectUtil.equals(signUpKeyValue,aSignUpKeyValue)){
                ((Model)aKey).destroy();
            }else if (aKey.isValidated()) {
                userRef.set(user, signUpKey, aSignUpKeyValue);
            }
        }
        user.save();
    }
}
