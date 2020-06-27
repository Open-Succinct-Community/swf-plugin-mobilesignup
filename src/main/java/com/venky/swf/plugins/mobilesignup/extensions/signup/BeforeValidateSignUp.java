package com.venky.swf.plugins.mobilesignup.extensions.signup;

import com.venky.swf.plugins.collab.extensions.beforesave.BeforeValidatePhone;
import com.venky.swf.plugins.mobilesignup.db.model.SignUp;

public class BeforeValidateSignUp extends BeforeValidatePhone<SignUp> {
    static {
        registerExtension(new BeforeValidateSignUp());
    }

}
