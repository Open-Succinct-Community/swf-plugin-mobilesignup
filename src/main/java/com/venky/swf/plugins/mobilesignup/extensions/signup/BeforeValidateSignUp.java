package com.venky.swf.plugins.mobilesignup.extensions.signup;

import com.venky.core.date.DateUtils;
import com.venky.swf.db.Database;
import com.venky.swf.plugins.background.core.TaskManager;
import com.venky.swf.plugins.collab.agents.SendOtp;
import com.venky.swf.plugins.collab.extensions.beforesave.BeforeValidatePhone;
import com.venky.swf.plugins.mobilesignup.db.model.SignUp;
import com.venky.swf.plugins.mobilesignup.db.model.User;
import com.venky.swf.sql.Expression;
import com.venky.swf.sql.Operator;
import com.venky.swf.sql.Select;

public class BeforeValidateSignUp extends BeforeValidatePhone<SignUp> {
    static {
        registerExtension(new BeforeValidateSignUp());
    }

    @Override
    public void beforeValidate(SignUp signUp) {
        super.beforeValidate(signUp);
        if (signUp.getRawRecord().isNewRecord() && signUp.isValidated()) {
            //From user phone save.!! pre validated.
            return;
        }else if (!signUp.isValidated()) {
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
        if (newUser){
            user = Database.getTable(User.class).newRecord();
            user.setPhoneNumber(signUp.getPhoneNumber());
        }
        user.generateApiKey(false);//Cannot be logged into same account from 2 devices at the same time. Auto logout from other device.
        user.save();
        if (newUser){
            signUp.setUserId(user.getId());
        }else if (signUp.getUserId() == null ){
            SignUp last = SignUp.getRequest(user.getId(),user.getPhoneNumber(),true);
            if (!last.getRawRecord().isNewRecord()){
                //No Signup exists for the user.
                last.destroy();
            }
            signUp.setUserId(user.getId());
        }
        return user;
    }
}
