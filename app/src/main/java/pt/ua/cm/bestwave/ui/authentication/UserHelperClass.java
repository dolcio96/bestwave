package pt.ua.cm.bestwave.ui.authentication;

public class UserHelperClass {

    String username,email,name,surname,password;

    public UserHelperClass() {
    }

    public UserHelperClass(String username, String email, String name,String surname, String password) {
        this.username = username;
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


}
