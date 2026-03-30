package rest.util;

import com.google.gson.JsonObject;
public class Errors {

    public static  ErrorRecord INVALID_CREDENTIALS = new ErrorRecord(9900, "The username-password pair is not valid");
    public static  ErrorRecord USER_ALREADY_EXISTS = new ErrorRecord(9901, "Error in creating an account because the username already exists");
    public static  ErrorRecord USER_NOT_FOUND = new ErrorRecord(9902, "The username referred in the operation doesn’t exist in registered accounts");
    public static  ErrorRecord INVALID_TOKEN = new ErrorRecord(9903, "The operation is called with an invalid token (wrong format for example)");
    public static  ErrorRecord TOKEN_EXPIRED = new ErrorRecord(9904, "The operation is called with a token that is expired");
    public static  ErrorRecord UNAUTHORIZED = new ErrorRecord(9905, "The operation is not allowed for the user role");
    public static  ErrorRecord INVALID_INPUT = new ErrorRecord(9906, "The call is using input data not following the correct specification");
    public static  ErrorRecord FORBIDDEN = new ErrorRecord(9907, "The operation generated a forbidden error by other reason");

    public Errors(){
    }
}




/*
Error, Error code, Error Message
INVALID_CREDENTIALS 9900, "The username-password pair is not valid"

USER_ALREADY_EXISTS 9901, "Error in creating an account because the username already exists"

USER_NOT_FOUND 9902, "The username referred in the operation doesn’t exist in registered accounts"

INVALID_TOKEN 9903, "The operation is called with an invalid token (wrong format for example)"

TOKEN_EXPIRED 9904, "The operation is called with a token that is expired"

UNAUTHORIZED 9905, "The operation is not allowed for the user role"

INVALID_INPUT 9906, "The call is using input data not following the correct specification"

FORBIDDEN 9907, "The operation generated a forbidden error by other reason"
 */