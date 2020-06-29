package com.venky.swf.plugins.mobilesignup.db.model;

import com.venky.swf.db.Database;
import com.venky.swf.db.JdbcTypeHelper.TypeConverter;
import com.venky.swf.db.annotations.column.IS_VIRTUAL;
import com.venky.swf.db.annotations.column.UNIQUE_KEY;
import com.venky.swf.db.annotations.column.relationship.CONNECTED_VIA;
import com.venky.swf.db.model.Model;
import com.venky.swf.db.model.reflection.ModelReflector;
import com.venky.swf.plugins.collab.db.model.user.Phone;
import com.venky.swf.plugins.collab.db.model.user.PhoneImpl;
import com.venky.swf.plugins.collab.db.model.user.UserPhone;
import com.venky.swf.sql.Conjunction;
import com.venky.swf.sql.Expression;
import com.venky.swf.sql.Operator;
import com.venky.swf.sql.Select;

import java.util.List;

public interface SignUp extends Phone, Model {

    @UNIQUE_KEY("PHONE")
    String getPhoneNumber();

    @UNIQUE_KEY(value = "PHONE",allowMultipleRecordsWithNull = false)
    Long getUserId();
    void setUserId(Long userId);
    User getUser();

    @IS_VIRTUAL
    User getLastSignedUpUser();

    static SignUp getRequest(Long userId, String aphoneNumber){
        return getRequest(userId,aphoneNumber,false);
    }
    static SignUp getRequest(Long userId, String aphoneNumber, boolean exactMatch){
        String phoneNumber = Phone.sanitizePhoneNumber(aphoneNumber);

        ModelReflector<SignUp> ref = ModelReflector.instance(SignUp.class);

        Expression where = new Expression(ref.getPool(), Conjunction.AND);
        where.add(new Expression(ref.getPool(),"PHONE_NUMBER", Operator.EQ,phoneNumber));
        Expression userWhere = new Expression(ref.getPool(),Conjunction.OR);
        userWhere.add(new Expression(ref.getPool(),"USER_ID",Operator.EQ,userId));
        if (!exactMatch) {
            userWhere.add(new Expression(ref.getPool(), "USER_ID", Operator.EQ));
        }

        where.add(userWhere);
        List<SignUp> signUps = new Select().from(SignUp.class).where(where).execute();
        if (signUps.isEmpty()){
            SignUp signUp = Database.getTable(SignUp.class).newRecord();
            signUp.setPhoneNumber(phoneNumber);
            signUp.setUserId(userId);
            signUp.setValidated(false);
            return signUp;
        }else {
            TypeConverter<Long> longTypeConverter = ref.getJdbcTypeHelper().getTypeRef(Long.class).getTypeConverter();
            signUps.sort((o1, o2) -> (int)(longTypeConverter.valueOf(o2.getUserId()) - longTypeConverter.valueOf(o1.getUserId())));
            return signUps.get(0);
        }
    }
}
