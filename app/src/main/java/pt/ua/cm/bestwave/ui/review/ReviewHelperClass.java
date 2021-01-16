package pt.ua.cm.bestwave.ui.review;

public class ReviewHelperClass {
    String username,description,uuid;
    float stars;
    double latitude,longitude;

    public ReviewHelperClass(String username, double latitude, double longitude,  float stars, String description, String uuid) {
        this.username = username;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.uuid = uuid;
        this.stars = stars;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public float getStars() {
        return stars;
    }

    public void setStars(float stars) {
        this.stars = stars;
    }
}
