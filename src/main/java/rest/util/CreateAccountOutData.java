package rest.util;

public class CreateAccountOutData {

    public String username; // email
    public String password;
    public String confirmation;
    public String phone;
    public String address;
    public String role;


    public CreateAccountOutData() {

    }

    public CreateAccountOutData(String username, String password, String confirmation, String phone, String address, String role) {
        this.username = username;
        this.password = password;
        this.confirmation = confirmation;
        this.phone = phone;
        this.address = address;
        this.role = role;
    }

}
