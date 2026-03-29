package rest.util;

import java.util.UUID;

public class TokenData {

    public static final long EXPIRATION_TIME = 1000*60*60*2; // 2h

    public String tokenId;
    public String username;
    public String role;
    public long issuedAt; // same as creationData
    public long expiresAt; // same as expirationData

    public TokenData(){ }

    public TokenData(String username){
        this.username = username;
        this.tokenId = UUID.randomUUID().toString();
        this.issuedAt = System.currentTimeMillis();
        this.expiresAt = this.issuedAt + EXPIRATION_TIME;
    }
}
