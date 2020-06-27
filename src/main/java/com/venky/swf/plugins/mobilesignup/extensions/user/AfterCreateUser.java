package com.venky.swf.plugins.mobilesignup.extensions.user;

import com.venky.swf.db.Database;
import com.venky.swf.db.extensions.AfterModelCreateExtension;
import com.venky.swf.plugins.mobilesignup.db.model.DeviceUuid;
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
            ur.save();
        }

        DeviceUuid uuid = Database.getTable(DeviceUuid.class).newRecord();
        uuid.setDeviceUuid(model.getDeviceId());
        uuid.setUserId(model.getId());
        uuid.save();

    }

}
