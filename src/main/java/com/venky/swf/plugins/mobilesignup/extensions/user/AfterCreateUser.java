package com.venky.swf.plugins.mobilesignup.extensions.user;

import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.Database;
import com.venky.swf.db.extensions.AfterModelCreateExtension;
import com.venky.swf.plugins.collab.db.model.user.UserEmail;
import com.venky.swf.plugins.collab.db.model.user.UserPhone;
import com.venky.swf.plugins.mobilesignup.db.model.User;
import com.venky.swf.plugins.security.db.model.Role;
import com.venky.swf.plugins.security.db.model.UserRole;

public class AfterCreateUser extends AfterModelCreateExtension<User> {
    static{
        registerExtension(new AfterCreateUser());
    }
    @Override
    public void afterCreate(User model) {
        Role  userRole = Role.getRole("USER");
        if (userRole != null){
            UserRole ur = Database.getTable(UserRole.class).newRecord();
            ur.setUserId(model.getId());
            ur.setRoleId(userRole.getId());
            ur = Database.getTable(UserRole.class).getRefreshed(ur);
            ur.save();
        }
        if (!ObjectUtil.isVoid(model.getPhoneNumber())){
            UserPhone userPhone = Database.getTable(UserPhone.class).newRecord();
            userPhone.setPhoneNumber(model.getPhoneNumber());
            userPhone.setUserId(model.getId());
            userPhone = Database.getTable(UserPhone.class).getRefreshed(userPhone);
            userPhone.save();
        }else if (!ObjectUtil.isVoid(model.getEmail())){
            UserEmail userEmail = Database.getTable(UserEmail.class).newRecord();
            userEmail.setEmail(model.getEmail());
            userEmail.setUserId(model.getId());
            userEmail = Database.getTable(UserEmail.class).getRefreshed(userEmail);
            userEmail.save();
        }


    }

}
