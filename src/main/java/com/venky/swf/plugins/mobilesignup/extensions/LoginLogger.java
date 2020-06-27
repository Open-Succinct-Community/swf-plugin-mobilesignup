package com.venky.swf.plugins.mobilesignup.extensions;

import com.venky.extension.Extension;
import com.venky.extension.Registry;
import com.venky.swf.db.Database;
import com.venky.swf.db.model.Model;
import com.venky.swf.path._IPath;
import com.venky.swf.plugins.background.core.Task;
import com.venky.swf.plugins.background.core.TaskManager;
import com.venky.swf.plugins.mobilesignup.db.model.User;

import java.sql.Timestamp;

public class LoginLogger implements Extension {
    static {
        Registry.instance().registerExtension(_IPath.USER_LOGIN_SUCCESS_EXTENSION,new LoginLogger());
    }
    @Override
    public void invoke(Object... objects) {
        User user = ((Model)objects[0]).getRawRecord().getAsProxy(User.class);
        TaskManager.instance().executeAsync(new UserLoginLogger(user.getId()));
    }

    public static class UserLoginLogger implements Task {
        long userId ;
        public UserLoginLogger(long id){
            this.userId = id;
        }

        @Override
        public void execute() {
            User user = Database.getTable(User.class).get(userId);
            if (user != null){
                user.setLastLoginTime(new Timestamp(System.currentTimeMillis()));
                user.save();
            }

        }
    }
}
