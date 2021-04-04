package com.venky.swf.plugins.mobilesignup.db.model;

import com.venky.core.date.DateUtils;
import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.model.reflection.ModelReflector;
import com.venky.swf.db.table.ModelImpl;
import com.venky.swf.plugins.collab.db.model.user.UserPhone;
import com.venky.swf.sql.Conjunction;
import com.venky.swf.sql.Expression;
import com.venky.swf.sql.Operator;
import com.venky.swf.sql.Select;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

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
                .orderBy("USER_ID","ID DESC").execute(1);
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
    public void deactivate() {
        User user = getProxy();
        String phoneNumber = user.getPhoneNumber();
        if (ObjectUtil.isVoid(phoneNumber)){
            return;
        }
        user.setPhoneNumber(null);

        List<UserPhone> userPhones = user.getUserPhones();
        for (Iterator<UserPhone> i = userPhones.iterator() ; i.hasNext(); ){
            UserPhone  up = i.next();
            if (ObjectUtil.equals(up.getPhoneNumber(),phoneNumber)){
                up.destroy();
            }else if (up.isValidated()){
                user.setPhoneNumber(up.getPhoneNumber());
                break;
            }
        }
        user.save();
    }
}
