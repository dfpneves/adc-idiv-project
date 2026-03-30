package rest.util;

public class InputData {
    public String username; // email
    public String password;
    public String confirmation;
    public String phone;
    public String address;
    public String role;
    public String tokenId;
    public String newRole;
    public String oldPassword;
    public String newPassword;



    public InputData() {  }

    // Input data modify account attribute

    // Input data Login
    public InputData(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Input data Login
    public InputData(String username, String password, String tokenId) {
        this.username = username;
        this.password = password;
        this.tokenId = tokenId;
    }



    private boolean nonEmptyOrBlankField(String field) {
        return field != null && !field.isBlank();
    }

    public boolean validLoginData() {
        return  nonEmptyOrBlankField(username) &&
                nonEmptyOrBlankField(password);
    }

    public boolean validShowUsersData() {
        return  nonEmptyOrBlankField(username) &&
                nonEmptyOrBlankField(password) &&
                nonEmptyOrBlankField(tokenId);
    }


    public boolean validCreateAccount() {


        return  nonEmptyOrBlankField(username) &&
                nonEmptyOrBlankField(password) &&
                nonEmptyOrBlankField(phone) &&
                nonEmptyOrBlankField(address) &&
                nonEmptyOrBlankField(role) &&
                username.contains("@") &&
                password.equals(confirmation) &&
                (role.equals("USER") || role.equals("BOFFICER") || role.equals("ADMIN"));
    }
}
