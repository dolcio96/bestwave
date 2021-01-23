package pt.ua.cm.bestwave;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import pt.ua.cm.bestwave.ui.authentication.UserHelperClass;
import pt.ua.cm.bestwave.ui.review.ReviewHelperClass;


public class ProfileReviewFragment extends Fragment {

    ReviewHelperClass rhc;
    UserHelperClass uhc;
    String uuidUser;
    String tag;
    TextView nameTextView;
    TextView surnameTextView;
    TextView emailTextView;
    TextView locationText;
    TextView dateText;
    TextView descriptionText;

    RatingBar ratingBar = null;

    Geocoder geocoder;
    List<Address> addresses;

    //FIREBASE DATABASE
    FirebaseDatabase database;
    DatabaseReference reference;


    public ProfileReviewFragment(String tag) {
        this.tag = tag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance();
        getReviewsFromDB();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_review, container, false);

        // FIND VIEWS
        nameTextView=view.findViewById(R.id.name_profile_review_text_view);
        surnameTextView=view.findViewById(R.id.surname_profile_review_text_view);
        emailTextView=view.findViewById(R.id.email_profile_review_text_view);
        locationText=view.findViewById(R.id.profile_review_location);
        dateText=view.findViewById(R.id.profile_review_date);
        descriptionText=view.findViewById(R.id.profile_review_description);
        ratingBar=view.findViewById(R.id.profile_review_ratingBar);

        return view;
    }

    public void getReviewsFromDB(){
        reference = database.getReference("reviews").child(tag);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                rhc = snapshot.getValue(ReviewHelperClass.class);
                // SET locationText
                getCompleteAddress();
                // SET dateText
                DateFormat df = new SimpleDateFormat("dd/MM/yy\nHH:mm:ss");
                dateText.setText(df.format(rhc.getDate()));

                ratingBar.setRating(rhc.getStars());
                ratingBar.setIsIndicator(true);
                //SET descriptionText
                descriptionText.setText(rhc.getDescription());

                getUserFromDB();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //GET USER FROM DB
    public void getUserFromDB(){
        uuidUser = rhc.getUuidUser();
        reference = database.getReference("users").child(uuidUser);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                uhc = snapshot.getValue(UserHelperClass.class);
                nameTextView.setText(uhc.getName().toUpperCase());
                surnameTextView.setText(uhc.getSurname().toUpperCase());
                emailTextView.setText((uhc.getEmail()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // GET COMPLETE ADDRESS FROM LATITUDE AND LONGITUDE
    public void getCompleteAddress(){

        geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(rhc.getLatitude(), rhc.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }

        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

        locationText.setText((String) String.valueOf(city));
    }
}