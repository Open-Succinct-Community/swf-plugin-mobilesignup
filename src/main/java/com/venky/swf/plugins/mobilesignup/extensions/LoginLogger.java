package com.venky.swf.plugins.mobilesignup.extensions;

import com.venky.extension.Extension;
import com.venky.extension.Registry;
import com.venky.swf.db.Database;
import com.venky.swf.db.model.Model;
import com.venky.swf.path.Path;
import com.venky.swf.path._IPath;
import com.venky.swf.plugins.background.core.Task;
import com.venky.swf.plugins.background.core.TaskManager;
import com.venky.swf.plugins.mobilesignup.db.model.User;
import com.venky.swf.plugins.mobilesignup.db.model.UserLogin;

import java.sql.Timestamp;

public class LoginLogger implements Extension {
    static {
        Registry.instance().registerExtension(_IPath.USER_LOGIN_SUCCESS_EXTENSION,new LoginLogger());
    }
    @Override
    public void invoke(Object... objects) {
        Path path = (Path)objects[0];
        User user = ((Model)objects[1]).getRawRecord().getAsProxy(User.class);
        TaskManager.instance().executeAsync(new UserLoginLogger(path,user),false);
    }

    public static class UserLoginLogger implements Task {
        User user;
        String remoteHost;
        String userAgent;
        public UserLoginLogger(Path path, User user){
            this.user = user;
            remoteHost = path.getRequest().getHeader("X-Real-IP") ;
            if (remoteHost == null){
                remoteHost = path.getRequest().getRemoteHost();
            }
            userAgent = path.getHeader("User-Agent");
        }

        @Override
        public void execute() {
            UserLogin login = Database.getTable(UserLogin.class).newRecord();
            if (user != null){
                login.setLoginTime(new Timestamp(System.currentTimeMillis()));
                login.setUserId(user.getId());
                login.setLat(user.getCurrentLat());
                login.setLng(user.getCurrentLng());
                login.setUserAgent(userAgent);
                login.setFromIp(remoteHost);
                login.save();
            }

        }
    }
}
