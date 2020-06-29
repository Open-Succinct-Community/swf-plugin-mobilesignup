package com.venky.swf.plugins.mobilesignup.db.model;

import com.venky.swf.db.annotations.column.IS_VIRTUAL;
import com.venky.swf.db.annotations.column.UNIQUE_KEY;
import com.venky.swf.db.annotations.column.ui.HIDDEN;

import java.sql.Timestamp;
import java.util.List;

public interface User extends in.succinct.plugins.ecommerce.db.model.participation.User {

    @UNIQUE_KEY("PHONE")
    String getPhoneNumber();

    @HIDDEN
    String getLastPrimaryPhoneNumber();
    void setLastPrimaryPhoneNumber(String lastPrimaryPhoneNumber);


    @IS_VIRTUAL
    Timestamp getLastLoginTime();

    @IS_VIRTUAL
    boolean isActive();


    @IS_VIRTUAL
    void deactivate();
}
