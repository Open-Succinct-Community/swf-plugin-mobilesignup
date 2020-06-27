package com.venky.swf.plugins.mobilesignup.extensions.phone;

import com.venky.swf.plugins.collab.db.model.user.UserPhone;
import com.venky.swf.plugins.collab.extensions.beforesave.BeforeValidatePhone;

public class BeforeValidateUserPhone extends BeforeValidatePhone<UserPhone> {
    static {
        registerExtension(new BeforeValidateUserPhone());
    }

}
