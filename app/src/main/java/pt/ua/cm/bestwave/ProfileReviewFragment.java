package pt.ua.cm.bestwave;

import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ViewTarget;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
    TextView nameSurnameTextView,emailTextView,locationText,dateText,descriptionText;
    RatingBar ratingBar = null;
    ImageView profileImage,reviewImage;

    Geocoder geocoder;
    List<Address> addresses;

    //FIREBASE DATABASE
    FirebaseDatabase database;
    DatabaseReference reference;

    FirebaseStorage storage;
    StorageReference storageReference;
    View view;

    public ProfileReviewFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile_review, container, false);


        // FIND VIEWS
        reviewImage =view.findViewById(R.id.imageReview);
        profileImage=view.findViewById(R.id.profile_image_image_view);
        nameSurnameTextView=view.findViewById(R.id.name_surname_profile_review_text_view);
        emailTextView=view.findViewById(R.id.email_profile_review_text_view);
        locationText=view.findViewById(R.id.profile_review_location);
        dateText=view.findViewById(R.id.profile_review_date);
        descriptionText=view.findViewById(R.id.profile_review_description);
        ratingBar=view.findViewById(R.id.profile_review_ratingBar);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(getArguments()!=null){
            ProfileReviewFragmentArgs arg = ProfileReviewFragmentArgs.fromBundle(getArguments());
            tag = arg.getCurrentTag();
            getReviewsFromDB();
        }


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
                getReviewImage();

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
                nameSurnameTextView.setText(uhc.getName().toUpperCase()+" "+uhc.getSurname().toUpperCase());
                emailTextView.setText((uhc.getEmail()));

                getProfileImage();
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

    public void getProfileImage(){
        storageReference.child("profileImages/"+rhc.getUuidUser())
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                    Glide.with(view.getContext()).load(uri).centerCrop().into(profileImage);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });


    }

    public void getReviewImage(){
        storageReference.child("images/"+tag)
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                    Glide.with(view.getContext()).load(uri).centerCrop().into(reviewImage);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

