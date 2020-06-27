package com.venky.swf.plugins.mobilesignup.extensions.signup;

import com.venky.core.date.DateUtils;
import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.Database;
import com.venky.swf.db.extensions.BeforeModelSaveExtension;
import com.venky.swf.plugins.background.core.TaskManager;
import com.venky.swf.plugins.collab.agents.SendOtp;
import com.venky.swf.plugins.mobilesignup.db.model.DeviceUuid;
import com.venky.swf.plugins.mobilesignup.db.model.SignUp;
import com.venky.swf.plugins.mobilesignup.db.model.User;

import java.sql.Timestamp;
import java.util.Optional;

public class BeforeSaveSignUp extends BeforeModelSaveExtension<SignUp> {
    static {
        registerExtension(new BeforeSaveSignUp());
    }
    @Override
    public void beforeSave(SignUp signUp) {
        if (signUp.getRawRecord().isNewRecord() && signUp.isValidated()) {
            return;
        }else if (signUp.getRawRecord().isNewRecord()) {
            TaskManager.instance().executeAsync(new SendOtp(signUp),false);
        }else if (signUp.getRawRecord().isFieldDirty("DEVICE_ID")) {
            User user = null;
            if (!signUp.getReflector().isVoid(signUp.getUserId())) {
                user = signUp.getUser();
                Optional<DeviceUuid> optional = user.getDeviceUuids().stream().filter(d -> ObjectUtil.equals(d.getDeviceUuid(), signUp.getDeviceId())).findAny();
                if (!optional.isPresent()) {
                    //If being signed up from new device.
                    TaskManager.instance().executeAsync(new SendOtp(signUp),false);
                }
            }else {
                //Otp already sent for new necord no need to send again . User cna request from UI if needed.
            }
        }
        if (signUp.isValidated()) {
            if (signUp.getRawRecord().isFieldDirty("VALIDATED")){
                User user = createUser(signUp);
                signUp.setUserId(user.getId());
            }else {
                User user = signUp.getUser();
                if (user != null) {
                    if (user.getNumMinutesToKeyExpiration() < 0){
                        user.generateApiKey();
                    }
                }
            }
        }

    }

    User createUser(SignUp signUp){
        User user = Database.getTable(User.class).newRecord();
        user.setPhoneNumber(signUp.getPhoneNumber());
        user = Database.getTable(User.class).getRefreshed(user);
        if (!user.getRawRecord().isNewRecord() && user.getLastLoginTime() != null && DateUtils.compareToMinutes(System.currentTimeMillis(), user.getLastLoginTime().getTime())> 90 * 24*60 ){
            Optional<DeviceUuid> optional = user.getDeviceUuids().stream().filter(d -> ObjectUtil.equals(d.getDeviceUuid(), signUp.getDeviceId())).findAny();
            if (!optional.isPresent()) {
                //This device has been never used by the old user. !!
                user = Database.getTable(User.class).newRecord();
                user.setPhoneNumber(signUp.getPhoneNumber());
            }
        }
        user.setDeviceId(signUp.getDeviceId());
        user.generateApiKey(false);//Cannot be logged into same account from 2 devices at the same time. Auto logout from other device.
        user.save();
        return user;
    }
}
