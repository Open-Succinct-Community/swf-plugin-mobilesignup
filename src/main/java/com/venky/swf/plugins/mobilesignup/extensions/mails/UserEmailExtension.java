package com.venky.swf.plugins.mobilesignup.extensions.mails;

import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.Database;
import com.venky.swf.db.extensions.BeforeModelSaveExtension;
import com.venky.swf.db.extensions.ModelOperationExtension;
import com.venky.swf.plugins.collab.db.model.user.UserEmail;
import com.venky.swf.plugins.mobilesignup.db.model.SignUp;
import com.venky.swf.plugins.mobilesignup.db.model.User;
import com.venky.swf.sql.Conjunction;
import com.venky.swf.sql.Expression;
import com.venky.swf.sql.Operator;
import com.venky.swf.sql.Select;

import java.util.List;

public class UserEmailExtension extends ModelOperationExtension<UserEmail> {

    static {
        registerExtension(new UserEmailExtension());
    }




    @Override
    public void beforeSave(UserEmail model) {
        SignUp signUp = null;
        if (!SignUp.isSignUpKeyPhoneNumber()) {
            signUp = SignUp.getRequest(model.getUserId(), model.getEmail(), false);

            if (!model.isValidated()) {
                model.setValidated(signUp != null && !signUp.getRawRecord().isNewRecord()
                        && signUp.isValidated());
            }
        }
        if (model.isValidated() && model.getRawRecord().isFieldDirty("VALIDATED")){
            Expression expression = new Expression(getPool(), Conjunction.AND);
            expression.add(new Expression(getPool(), "email", Operator.EQ, model.getEmail()));
            expression.add(new Expression(getPool(), "user_id", Operator.NE, model.getUserId()));

            Select select = new Select().from(UserEmail.class).where(expression);
            List<UserEmail> r = select.execute(UserEmail.class);
            if (!r.isEmpty()) {
                for (UserEmail userEmail : r) {
                    userEmail.destroy();
                    User another = userEmail.getUser().getRawRecord().getAsProxy(User.class);
                    if (ObjectUtil.equals(another.getEmail(), userEmail.getEmail())){
                        another.setEmail(null);
                        another.save();
                    }
                }
            }
            User user = model.getUser().getRawRecord().getAsProxy(User.class);
            if (ObjectUtil.isVoid(user.getEmail())){
                user.setEmail(model.getEmail());
                user.save();
            }
            if (signUp != null && signUp.getRawRecord().isNewRecord()){
                signUp.setValidated(true);
                signUp.setUserId(model.getUserId());
                signUp = Database.getTable(SignUp.class).getRefreshed(signUp);
                signUp.save(); //Pretend to signp with this email also
            }

        }
    }
    @Override
    protected void afterCreate(UserEmail instance) {
        super.afterCreate(instance);
        if (!instance.isValidated()) {
            instance.sendOtp();
        }
    }
}
