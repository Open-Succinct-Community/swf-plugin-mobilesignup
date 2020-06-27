package com.venky.swf.plugins.mobilesignup.extensions.phone;

import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.Database;
import com.venky.swf.db.extensions.BeforeModelSaveExtension;
import com.venky.swf.plugins.background.core.TaskManager;
import com.venky.swf.plugins.collab.agents.SendOtp;
import com.venky.swf.plugins.collab.db.model.user.Phone;
import com.venky.swf.plugins.collab.db.model.user.UserPhone;
import com.venky.swf.plugins.mobilesignup.db.model.SignUp;
import com.venky.swf.plugins.mobilesignup.db.model.User;
import com.venky.swf.sql.Conjunction;
import com.venky.swf.sql.Expression;
import com.venky.swf.sql.Operator;
import com.venky.swf.sql.Select;

import java.util.List;

public class BeforeSaveUserPhone extends BeforeModelSaveExtension<UserPhone> {

    static {
        registerExtension(new BeforeSaveUserPhone());
    }

    private SignUp getRequest(User user){
        SignUp request = Database.getTable(SignUp.class).newRecord();
        request.setPhoneNumber(user.getPhoneNumber());
        request = Database.getTable(SignUp.class).getRefreshed(request);
        return request;
    }

    @Override
    public void beforeSave(UserPhone model) {
        model.setPhoneNumber(Phone.sanitizePhoneNumber(model.getPhoneNumber()));
        User user = model.getUser().getRawRecord().getAsProxy(User.class);
        SignUp signUp = getRequest(user);

        if (!model.isValidated()){
            model.setValidated(signUp != null && !signUp.getRawRecord().isNewRecord()
                && ObjectUtil.equals(signUp.getPhoneNumber(), model.getPhoneNumber())
                && signUp.isValidated());
        }

        if (!model.isValidated() && ObjectUtil.isVoid(model.getLastOtp())){
            TaskManager.instance().executeAsync(new SendOtp(model),false);
        }
        
        if (model.isValidated() && model.getRawRecord().isFieldDirty("VALIDATED")){
            Expression expression = new Expression(getPool(), Conjunction.AND);
            expression.add(new Expression(getPool(), "phone_number", Operator.IN, model.getPhoneNumber()));
            expression.add(new Expression(getPool(), "user_id", Operator.NE, model.getUserId()));
            Select select = new Select().from(UserPhone.class).where(expression);
            List<UserPhone> r = select.execute(UserPhone.class);
            if (!r.isEmpty()) {
                for (UserPhone userPhone : r) {
                    userPhone.destroy();
                    User another = userPhone.getUser().getRawRecord().getAsProxy(User.class);
                    if (ObjectUtil.equals(another.getPhoneNumber(), userPhone.getPhoneNumber())){
                        another.setPhoneNumber(null);
                        another.save();
                    }
                }
            }
            if (signUp.getRawRecord().isNewRecord()){
                signUp.setDeviceId(user.getDeviceId());
                signUp.setValidated(true);
                signUp.setUserId(user.getId());
                signUp.save();
            }
        }
    }
}
