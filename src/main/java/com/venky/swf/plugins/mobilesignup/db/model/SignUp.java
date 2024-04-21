package com.venky.swf.plugins.mobilesignup.db.model;

import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.Database;
import com.venky.swf.db.JdbcTypeHelper.TypeConverter;
import com.venky.swf.db.annotations.column.IS_NULLABLE;
import com.venky.swf.db.annotations.column.IS_VIRTUAL;
import com.venky.swf.db.annotations.column.UNIQUE_KEY;
import com.venky.swf.db.annotations.column.indexing.Index;
import com.venky.swf.db.annotations.column.relationship.CONNECTED_VIA;
import com.venky.swf.db.annotations.model.MENU;
import com.venky.swf.db.model.Model;
import com.venky.swf.db.model.reflection.ModelReflector;
import com.venky.swf.plugins.collab.db.model.user.Email;
import com.venky.swf.plugins.collab.db.model.user.Phone;
import com.venky.swf.plugins.collab.db.model.user.PhoneImpl;
import com.venky.swf.plugins.collab.db.model.user.UserPhone;
import com.venky.swf.routing.Config;
import com.venky.swf.sql.Conjunction;
import com.venky.swf.sql.Expression;
import com.venky.swf.sql.Operator;
import com.venky.swf.sql.Select;

import java.util.List;

public interface SignUp extends Phone, Email,Model {

    @UNIQUE_KEY(value = "SIGNUP_KEY",allowMultipleRecordsWithNull = false)
    @Index
    @IS_NULLABLE
    String getPhoneNumber();

    @UNIQUE_KEY(value = "SIGNUP_KEY",allowMultipleRecordsWithNull = false)
    @Index
    @IS_NULLABLE
    String getEmail();

    @UNIQUE_KEY(value = "SIGNUP_KEY",allowMultipleRecordsWithNull = false)
    @Index
    Long getUserId();
    void setUserId(Long userId);
    User getUser();

    @IS_VIRTUAL
    User getLastSignedUpUser();

    static SignUp getRequest(Long userId, String signUpKey){
        return getRequest(userId,signUpKey,false);
    }
    static SignUp getRequest(Long userId, String signUpKey, boolean exactMatch){

        String phoneNumber = null;
        String email = null;
        if (SignUp.isSignUpKeyPhoneNumber()){
            phoneNumber = signUpKey;
        }else {
            email = signUpKey;
        }

        ModelReflector<SignUp> ref = ModelReflector.instance(SignUp.class);
        Expression where = new Expression(ref.getPool(), Conjunction.AND);
        if (!ObjectUtil.isVoid(phoneNumber)) {
            where.add(new Expression(ref.getPool(), "PHONE_NUMBER", Operator.EQ, phoneNumber));
        }else {
            where.add(new Expression(ref.getPool(), "PHONE_NUMBER", Operator.EQ));
        }
        if (!ObjectUtil.isVoid(email)) {
            where.add(new Expression(ref.getPool(), "EMAIL", Operator.EQ, email));
        }else {
            where.add(new Expression(ref.getPool(), "EMAIL", Operator.EQ));
        }

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
            signUp.setEmail(email);
            signUp.setUserId(userId);
            signUp.setValidated(false);
            return signUp;
        }else {
            TypeConverter<Long> longTypeConverter = ref.getJdbcTypeHelper().getTypeRef(Long.class).getTypeConverter();
            signUps.sort((o1, o2) -> (int)(longTypeConverter.valueOf(o2.getUserId()) - longTypeConverter.valueOf(o1.getUserId()))); // Best Match logic
            return signUps.get(0);
        }
    }

    static String getSignUpKey(){
        return Config.instance().getProperty("swf.signup.key","PHONE_NUMBER"); //Default behaviour
    }

    static boolean isSignUpKeyPhoneNumber(){
        return ObjectUtil.equals(getSignUpKey(),"PHONE_NUMBER");
    }
}
