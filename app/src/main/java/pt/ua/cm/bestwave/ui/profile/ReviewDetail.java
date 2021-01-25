package pt.ua.cm.bestwave.ui.profile;

import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import pt.ua.cm.bestwave.R;
import pt.ua.cm.bestwave.ui.review.ReviewHelperClass;


public class ReviewDetail extends Fragment {
    ReviewHelperClass rhc;

    ImageView reviewImage;
    TextView locationText,dateText,descriptionText;
    RatingBar ratingBar = null;
    String tag;

    Geocoder geocoder;
    List<Address> addresses;
    FirebaseStorage storage;
    StorageReference storageReference;
    View view;

    public ReviewDetail() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ReviewDetailArgs arg = ReviewDetailArgs.fromBundle(getArguments());
        rhc = arg.getCurrentRhc();
        tag = arg.getTag();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_review_detail, container, false);

        // FIND VIEWS
        reviewImage = view.findViewById(R.id.imageReview);
        locationText = view.findViewById(R.id.locationReview);
        dateText = view.findViewById(R.id.dateReview);
        descriptionText = view.findViewById(R.id.descriptionText);
        ratingBar = view.findViewById(R.id.profile_review_ratingBar);

        // SET locationText
        getCompleteAddress();

        // SET dateText
        DateFormat df = new SimpleDateFormat("dd/MM/yy\nHH:mm:ss");
        dateText.setText(df.format(rhc.getDate()));

        ratingBar.setRating(rhc.getStars());
        ratingBar.setIsIndicator(true);

        //SET descriptionText
        descriptionText.setText(rhc.getDescription());

        return view;
    }

    // GET COMPLETE ADDRESS FROM LATITUDE AND LONGITUDE
    public void getCompleteAddress() {

        geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(rhc.getLatitude(), rhc.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }

        String city = addresses.get(0).getLocality();

        locationText.setText((String) String.valueOf(city));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getReviewImage();
    }


    public void getReviewImage() {
        storageReference = storage.getReference();
        storageReference.child("images/" + tag)
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                Glide.with(view.getContext()).load(uri).centerCrop().into(reviewImage);
                reviewImage.setAlpha((float) 1.0);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });

    }
}