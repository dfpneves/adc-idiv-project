package rest.util;

public class LoginData {
	
	public String username;
	public String password;
    public String tokenID;
	
	public LoginData() { }
	
	public LoginData(String username, String password, String tokenID) {
		this.username = username;
		this.password = password;
        this.tokenID = tokenID;
	}
	
}
