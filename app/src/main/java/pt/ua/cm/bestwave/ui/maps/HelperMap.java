package pt.ua.cm.bestwave.ui.maps;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import pt.ua.cm.bestwave.ui.review.ReviewHelperClass;

public class HelperMap {

    //Database Firebase variables
    FirebaseDatabase database;
    DatabaseReference reference;


    double latitude,longitude;

    public HelperMap(){

    }

    public HelperMap(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
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

}
