package com.venky.swf.plugins.mobilesignup.extensions.signup;

import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.extensions.ModelOperationExtension;
import com.venky.swf.plugins.collab.db.model.participants.admin.Company;
import com.venky.swf.plugins.mobilesignup.db.model.SignUp;
import com.venky.swf.plugins.mobilesignup.db.model.User;
import com.venky.swf.sql.Expression;
import com.venky.swf.sql.Operator;
import com.venky.swf.sql.Select;

import java.util.List;

public class SignupExtension extends ModelOperationExtension<SignUp> {
    static {
        registerExtension(new SignupExtension());
    }

    @Override
    protected void afterDestroy(SignUp instance) {
        super.afterDestroy(instance);
        if (instance.getUserId() != null){
            User user = instance.getUser();
            Select select = new Select().from(Company.class);
            select.where(new Expression(select.getPool(),"CREATOR_ID", Operator.EQ,user.getId()));
            List<Company> companyList = select.execute();
            companyList.forEach(c->c.destroy());
            user.destroy();
        }
    }
}
