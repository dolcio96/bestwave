package pt.ua.cm.bestwave.ui.profile;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import pt.ua.cm.bestwave.MainActivity;
import pt.ua.cm.bestwave.R;
import pt.ua.cm.bestwave.ui.review.ReviewHelperClass;


public class ReviewDetail extends Fragment {
    ReviewHelperClass rhc;
    TextView locationText;
    TextView dateText;
    TextView descriptionText;

    RatingBar ratingBar = null;

    Geocoder geocoder;
    List<Address> addresses;

    public ReviewDetail(ReviewHelperClass rhc) {
        this.rhc = rhc;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_review_detail, container, false);

        // FIND VIEWS
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

    @Override
    public void onDestroy() {

        ProfileFragment fragment = new ProfileFragment();
        FragmentManager fm = ((MainActivity) getView().getContext()).getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.add(R.id.detailContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

        super.onDestroy();
    }
}