package rest.util;

import java.util.UUID;

public class AuthToken {

	public static final long EXPIRATION_TIME = 1000*60*15; // 15m
	
	public String tokenID;
    public String username;
    public String role;
    public long creationData;
	public long expirationData;
	
	public AuthToken() { }
	
	public AuthToken(String username, String role) {
        this.tokenID = UUID.randomUUID().toString();
        this.username = username;
        this.role = role;
		this.creationData = System.currentTimeMillis();
		this.expirationData = this.creationData + EXPIRATION_TIME;
	}
	
}
