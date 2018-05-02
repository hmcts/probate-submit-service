package uk.gov.hmcts.probate.services.submit;

public class Registry {

    private String name;
    private int id;
    private String email;
    private String address;

    public void setName(String name) {
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String capitalizeRegistryName() {
        return name.substring(0,1).toUpperCase()
                + name.substring(1).toLowerCase();
    }
}
