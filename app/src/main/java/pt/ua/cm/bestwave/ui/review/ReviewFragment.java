package pt.ua.cm.bestwave.ui.review;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import pt.ua.cm.bestwave.MainActivity;
import pt.ua.cm.bestwave.R;

import static android.app.Activity.RESULT_OK;

public class ReviewFragment extends Fragment {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private ReviewViewModel galleryViewModel;
    ImageButton imageButton;
    RatingBar ratingBar = null;
    Float ratingValue = 0.0f;
    Bitmap imageBitmap = null;
    Button button;
    EditText editText = null;

    private FusedLocationProviderClient mFusedLocationClient;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    LatLng latLng;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                ViewModelProviders.of(this).get(ReviewViewModel.class);
        View root = inflater.inflate(R.layout.fragment_review, container, false);
        final TextView textViewInsertReview = root.findViewById(R.id.inserReviewTextView);

        galleryViewModel.getTextInsertReview().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textViewInsertReview.setText(s);
            }
        });
        final TextView textViewRatingBar = root.findViewById(R.id.ratingBarTextView);
        galleryViewModel.getTextRatingBar().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                textViewRatingBar.setText(s);
            }
        });
        final TextView textViewTakeAPicture = root.findViewById(R.id.takeAPictureTextView);
        galleryViewModel.getTextTakeAPicture().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                textViewTakeAPicture.setText(s);
            }
        });

        final TextView textViewWriteDescription = root.findViewById(R.id.writeDescriptionTextView);
        galleryViewModel.getTextWriteDescription().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                textViewWriteDescription.setText(s);
            }
        });
        ratingBar = (RatingBar) root.findViewById(R.id.ratingBar);
        ratingBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RatingBar bar = (RatingBar) v;
                ratingValue = bar.getRating();
            }
        });
        imageButton = (ImageButton) root.findViewById(R.id.imageButtonCamera);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                } catch (ActivityNotFoundException e) {
                    // display error state to the user
                }
            }
        });
        editText = (EditText) root.findViewById(R.id.editTextWriteDescription);

        // initialize FusedLocationProviderClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        button = (Button) root.findViewById(R.id.buttonSendReview);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Review added!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).setBackgroundTint(getActivity().getResources().getColor(R.color.holoBlueDark)).show();

                //check permission
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //when permission granted
                    getLocation();
                } else {
                    //when permission is denied
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                }

                FragmentActivity a = getActivity();
                Bitmap icon = BitmapFactory.decodeResource(a.getResources(), R.mipmap.camera_image);
                //TODO Implement set image for camera
                //imageButton.setImageBitmap(iconCamera);
                //editText.setText("");
                ratingBar.setRating(Float.parseFloat("0.0"));
            }

            private void getLocation() {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        //initialize Location
                        Location location = task.getResult();
                        if (location != null) {
                            try {
                                //Initialize geoCoder
                                Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                                //Initialize address list
                                List<Address> addresses = geocoder.getFromLocation(
                                        location.getLatitude(), location.getLongitude(), 1
                                );
                                //TODO Set Latitude on TextView or save into variable.
                                editText.setText(Html.fromHtml("Address : " + addresses.get(0).getAddressLine(0)));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });


    return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageButton.setImageBitmap(imageBitmap);
        }
    }
}