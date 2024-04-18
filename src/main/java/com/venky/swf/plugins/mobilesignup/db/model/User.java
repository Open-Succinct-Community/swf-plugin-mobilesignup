package com.venky.swf.plugins.mobilesignup.db.model;

import com.venky.swf.db.annotations.column.IS_VIRTUAL;
import com.venky.swf.db.annotations.column.UNIQUE_KEY;
import com.venky.swf.db.annotations.column.relationship.CONNECTED_VIA;
import com.venky.swf.plugins.audit.db.model.AUDITED;
import com.venky.swf.plugins.audit.db.model.ModelAudit;

import java.sql.Timestamp;
import java.util.List;

@AUDITED
public interface User extends com.venky.swf.plugins.collab.db.model.user.User {

    @UNIQUE_KEY("PHONE")
    String getPhoneNumber();
    
    @UNIQUE_KEY("EMAIL")
    String getEmail();



    @IS_VIRTUAL
    Timestamp getLastLoginTime();

    @IS_VIRTUAL
    boolean isActive();


    @IS_VIRTUAL
    void deactivate();

    @CONNECTED_VIA("USER_ID")
    public List<SignUp> getSignUps();

    @CONNECTED_VIA(value = "MODEL_ID", additional_join = "( NAME = 'User' )")
    public List<ModelAudit> getAudits();
}
