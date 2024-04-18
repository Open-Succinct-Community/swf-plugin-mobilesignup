package com.venky.swf.plugins.mobilesignup.extensions.signup;

import com.venky.core.date.DateUtils;
import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.Database;
import com.venky.swf.db.extensions.BeforeModelValidateExtension;
import com.venky.swf.db.model.reflection.ModelReflector;
import com.venky.swf.plugins.background.core.TaskManager;
import com.venky.swf.plugins.collab.agents.SendOtp;
import com.venky.swf.plugins.collab.db.model.user.OtpEnabled;
import com.venky.swf.plugins.collab.extensions.beforesave.BeforeValidateEmail;
import com.venky.swf.plugins.collab.extensions.beforesave.BeforeValidatePhone;
import com.venky.swf.plugins.mobilesignup.db.model.SignUp;
import com.venky.swf.plugins.mobilesignup.db.model.User;
import com.venky.swf.routing.Config;
import com.venky.swf.sql.Expression;
import com.venky.swf.sql.Operator;
import com.venky.swf.sql.Select;

public class BeforeValidateSignUp extends BeforeModelValidateExtension<SignUp> {
    static {
        registerExtension(new BeforeValidateSignUp());
    }



    static BeforeModelValidateExtension<SignUp> ext = SignUp.isSignUpKeyPhoneNumber()? new BeforeValidatePhone<>() : new BeforeValidateEmail<>();
    @Override
    public void beforeValidate(SignUp signUp) {
        ext.beforeValidate(signUp);
        if (signUp.getRawRecord().isNewRecord() && signUp.isValidated()) {
            //From user phone save.!! pre validated.
            return;
        }else if (!signUp.isValidated() && ObjectUtil.isVoid(signUp.getLastOtp())) {
            TaskManager.instance().executeAsync(new SendOtp(signUp),false);
        }else if (signUp.isValidated()) {
            if (signUp.getRawRecord().isFieldDirty("VALIDATED")){
                User user = createUser(signUp);
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
        User user = signUp.getLastSignedUpUser();
        if (user != null && !user.isActive()) {
            user.deactivate();
            user = null;
        }
        boolean newUser = user == null ;
        String signUpKey = SignUp.getSignUpKey();
        String signUpKeyValue = signUp.getReflector().get(signUp,SignUp.getSignUpKey());
        if (newUser){
            user = Database.getTable(User.class).newRecord();
            user.getReflector().set(user,signUpKey,signUpKeyValue);
        }
        user.generateApiKey(false);//Cannot be logged into same
        // account from 2 devices at the same time. Auto logout from other device.
        if (ObjectUtil.isVoid(user.getName())) {
            user.setName(signUpKeyValue);
        }
        user.save();
        if (newUser){
            signUp.setUserId(user.getId());
        }else if (signUp.getUserId() == null ){
            SignUp last = SignUp.getRequest(user.getId(),signUpKeyValue,true);
            if (!last.getRawRecord().isNewRecord()){
                //No Signup exists for the user.
                last.destroy();
            }
            signUp.setUserId(user.getId());
        }
        return user;
    }
}
