package com.dunk.eats.Common;

import com.dunk.eats.models.User;

public class Common {
    public static User currentUser;


    public static String convertCodeToStatus(String status) {
        if (status != null && status.equals("0"))
            return "Placed";
        else if (status != null && status.equals("1"))
            return "On my way";
        else
            return "Shipped";
    }
}
