package com.venky.swf.plugins.mobilesignup.controller;

import com.venky.core.util.ObjectUtil;
import com.venky.swf.controller.annotations.RequireLogin;
import com.venky.swf.db.Database;
import com.venky.swf.db.model.Model;
import com.venky.swf.db.model.reflection.ModelReflector;
import com.venky.swf.integration.IntegrationAdaptor;
import com.venky.swf.integration.api.HttpMethod;
import com.venky.swf.path.Path;
import com.venky.swf.plugins.collab.controller.OtpEnabledController;
import com.venky.swf.plugins.collab.db.model.user.Phone;
import com.venky.swf.plugins.mobilesignup.db.model.SignUp;
import com.venky.swf.plugins.mobilesignup.db.model.User;
import com.venky.swf.views.View;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class SignUpsController extends OtpEnabledController<SignUp> {
    public SignUpsController(Path path) {
        super(path);
    }

    @RequireLogin(false)
    public View sendOtp(long id) {
        return super.sendOtp(id, SignUp.getSignUpKey());
    }

    @RequireLogin(false)
    public View validateOtp(long id) throws Exception {
        return super.validateOtp(id, SignUp.getSignUpKey());
    }
    public View otpValidationComplete(SignUp signUp){
        boolean validated = signUp.isValidated();
        if (validated && signUp.getUserId() != null){
            User user = signUp.getUser();
            if (user != null){
                getPath().createUserSession(user,false);
            }
        }
        Map<Class<? extends Model>, List<String>> map = getIncludedModelFields(signUp.isValidated());

        return getIntegrationAdaptor().createResponse(getPath(),signUp, map.get(SignUp.class),new HashSet<>(),map);
    }
    @RequireLogin(false)
    @Override
    public View register(){
        ensureIntegrationMethod(HttpMethod.POST);
        List<SignUp> signUpList = getIntegrationAdaptor().readRequest(getPath());
        if (signUpList.size() != 1){
            throw new RuntimeException("Parameter not correct. Parameter must be a single SignUp element");
        }
        SignUp signUp = signUpList.get(0);
        String signUpKey = SignUp.getSignUpKey();
        String signUpKeyValue = ModelReflector.instance(SignUp.class).get(signUp,signUpKey);
        if (SignUp.isSignUpKeyPhoneNumber()){
            signUp.setEmail(null);
        }else {
            signUp.setPhoneNumber(null);
        }
        signUp = Database.getTable(SignUp.class).getRefreshed(signUp);
        String persistedSignUpKeyValue = ModelReflector.instance(SignUp.class).get(signUp,signUpKey);
        if (!signUp.getRawRecord().isNewRecord()){
            if ( !ObjectUtil.equals(persistedSignUpKeyValue,signUpKeyValue)) {
                //Security issue.
                //Id was same but different phone.!! is possible if a user was deleted. Though only a test scenario. Important  to fix it.
                signUp = Database.getTable(SignUp.class).newRecord();
                signUp.getReflector().set(signUp, signUpKey, signUpKeyValue);
            }else if (signUp.getUserId() != null){
                throw new RuntimeException(String.format("%s already registered",signUpKey));
            }else {
                signUp.setValidated(false);
                signUp.setLastOtp(null); //Re sign in.
            }
        }
        signUp.save();
        return otpValidationComplete(signUp);
    }

    protected Map<Class<? extends Model>, List<String>> getIncludedModelFields(boolean validated) {
        Map<Class<? extends Model>,List<String>> map = super.getIncludedModelFields();
        map.put(SignUp.class, Arrays.asList("ID","PHONE_NUMBER","EMAIL","USER_ID", "VALIDATED"));
        if (validated) {
            map.put(User.class, Arrays.asList("ID", "NAME", "LONG_NAME" , "API_KEY"));
        }
        return map;
    }

}
