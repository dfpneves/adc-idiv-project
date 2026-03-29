package rest.util;

public class OutputData {

    public String username; // email
    public String password;
    public String confirmation;
    public String phone;
    public String address;
    public String role;

    public OutputData() { }

    public OutputData(String username, String role){
        this.username = username;
        this.role = role;
    }
}
