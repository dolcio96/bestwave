package pt.ua.cm.bestwave.ui.review;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class ReviewHelperClass implements Serializable {
    String uuidUser,description;
    float stars;
    double latitude,longitude;
    Date date;

    public ReviewHelperClass() {

    }
    public ReviewHelperClass(String uuidUser, double latitude, double longitude,  float stars, String description,Date date) {
        this.uuidUser = uuidUser;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.stars = stars;
        this.date = date;
    }

    public String getUuidUser() {
        return uuidUser;
    }

    public void setUuidUser(String uuidUser) {
        this.uuidUser = uuidUser;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public float getStars() {
        return stars;
    }

    public void setStars(float stars) {
        this.stars = stars;
    }
}
