package rest.util;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;

public class TokenValidation {

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public TokenValidation() {}

    // all error messages from this function will, be expired token though invalid token could be true in some cases
    public static Entity getToken(String tokenID) {

        if(tokenID == null)
            return null; // invalid token (never gets called)

        Key tokenKey = datastore.newKeyFactory()
                .setKind("Token")
                .newKey(tokenID);

        Entity token = datastore.get(tokenKey);

        if(token == null)
            return null; // invalid token or token no longer exists

        long expiration = token.getLong("expirationData");

        if(System.currentTimeMillis() > expiration)
            return null; // token expired

        return token;
    }

    public static boolean expiredToken(Entity token) {

        long expiration = token.getLong("expirationData");

        return (System.currentTimeMillis() > expiration);
    }
}
