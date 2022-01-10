package com.venky.swf.plugins.mobilesignup.extensions.user;

import com.venky.core.string.StringUtil;
import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.Database;
import com.venky.swf.db.extensions.BeforeModelValidateExtension;
import com.venky.swf.db.model.UserEmail;
import com.venky.swf.plugins.collab.db.model.user.Phone;
import com.venky.swf.plugins.collab.db.model.user.UserPhone;
import com.venky.swf.plugins.mobilesignup.db.model.User;
import com.venky.swf.plugins.sequence.db.model.SequentialNumber;

import java.util.Optional;

public class BeforeValidateUser extends BeforeModelValidateExtension<User> {
    static {
        registerExtension(new BeforeValidateUser());
    }
    @Override
    public void beforeValidate(User model) {
        if (!ObjectUtil.isVoid(model.getPhoneNumber())){
            model.setPhoneNumber(Phone.sanitizePhoneNumber(model.getPhoneNumber()));
        }else {
            Optional<UserPhone> optionalUserPhone = model.getUserPhones().stream().filter(up->up.isValidated()).findAny();
            if (optionalUserPhone.isPresent()){
                UserPhone up = optionalUserPhone.get();
                model.setPhoneNumber(Phone.sanitizePhoneNumber(up.getPhoneNumber()));
            }
        }

        if (ObjectUtil.isVoid(model.getName())){
            model.setName(SequentialNumber.get("USER_ID").next());
        }

        if (!model.getRawRecord().isNewRecord() &&  model.getRawRecord().isFieldDirty("PHONE_NUMBER") && model.getRawRecord().getOldValue("PHONE_NUMBER") != null && !ObjectUtil.isVoid(model.getPhoneNumber())){
            Optional<UserPhone> optionalUserPhone = model.getUserPhones().stream().filter(up->ObjectUtil.equals(up.getPhoneNumber(),model.getPhoneNumber())).findFirst();
            if (!optionalUserPhone.isPresent()){
                throw new RuntimeException ( "Phone needs to verified before you can make it primary.");
            }else {
                UserPhone userPhone = optionalUserPhone.get();
                if (!userPhone.isValidated()){
                    throw new RuntimeException ( "Phone needs to verified before you can make it primary.");
                }
            }
        }

        if (!model.getRawRecord().isNewRecord() &&  model.getRawRecord().isFieldDirty("EMAIL") && model.getRawRecord().getOldValue("EMAIL") != null && !ObjectUtil.isVoid(model.getEmail())){
            Optional<UserEmail> optionalUserEmail = model.getUserEmails().stream().filter(up->ObjectUtil.equals(up.getEmail(),model.getEmail())).findFirst();
            if (!optionalUserEmail.isPresent()){
                throw new RuntimeException ( "Email needs to verified before you can make it primary.");
            }else {
                com.venky.swf.plugins.collab.db.model.user.UserEmail userEmail = optionalUserEmail.get().getRawRecord().getAsProxy(com.venky.swf.plugins.collab.db.model.user.UserEmail.class);
                if (!userEmail.isValidated()){
                    throw new RuntimeException ( "Email needs to verified before you can make it primary.");
                }
            }
        }
        if (ObjectUtil.isVoid(model.getLastPrimaryPhoneNumber())){
            model.setLastPrimaryPhoneNumber(model.getPhoneNumber());
        }else if (model.getRawRecord().isFieldDirty("PHONE_NUMBER")){
            String oldPhoneNumber = StringUtil.valueOf(model.getRawRecord().getOldValue("PHONE_NUMBER"));
            if (!ObjectUtil.isVoid(oldPhoneNumber)){
                model.setLastPrimaryPhoneNumber(model.getPhoneNumber());
            }
        }
    }
}
