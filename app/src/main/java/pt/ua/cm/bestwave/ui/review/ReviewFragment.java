package pt.ua.cm.bestwave.ui.review;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import pt.ua.cm.bestwave.R;
import pt.ua.cm.bestwave.ui.authentication.UserHelperClass;
import pt.ua.cm.bestwave.ui.maps.HelperMap;

import static android.app.Activity.RESULT_OK;

public class ReviewFragment extends Fragment {
    // request code
    static final int REQUEST_IMAGE_CAPTURE = 1;
    //VIEW VARIABLE
    ImageButton imageButtonCamera;
    RatingBar ratingBar = null;
    Float ratingValue;
    Bitmap imageBitmap = null;
    Button sendReviewButton;
    EditText descriptionEditText;
    TextView locationTextView, insertReviewTextView, ratingBarTextView, takeAPictureTextView, writeDescriptionTextView;

    //MAP VARIABLE
    private FusedLocationProviderClient mFusedLocationClient;
    LatLng latLng;

    boolean imageSet = false;
    String uuidImage;
    String uuidUser;

    // Uri indicates, where the image will be picked from
    private Uri filePath;

    //FIREBASE STORAGE
    FirebaseStorage storage;
    StorageReference storageReference;
    //FIREBASE DATABASE
    FirebaseDatabase database;
    DatabaseReference reference;
    //FIREBASE AUTHENTICATION
    FirebaseAuth mAuth;
    FirebaseUser user;
    String username;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //GET FIREBASE INSTANCE
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        //GET LOCATION
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        getLocation();
        // get the Firebase  storage reference
        storageReference = storage.getReference();
        if (user != null) {
            getUserFromDB();
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        View root = inflater.inflate(R.layout.fragment_review, container, false);

        // SET VIEW VARIABLE
        insertReviewTextView = root.findViewById(R.id.insertReviewTextView);
        ratingBarTextView = root.findViewById(R.id.ratingBarTextView);
        takeAPictureTextView = root.findViewById(R.id.takeAPictureTextView);
        writeDescriptionTextView = root.findViewById(R.id.writeDescriptionTextView);
        locationTextView = root.findViewById(R.id.locationTextView);
        ratingBar = root.findViewById(R.id.ratingBar);
        imageButtonCamera = root.findViewById(R.id.imageButtonCamera);
        descriptionEditText = root.findViewById(R.id.editTextWriteDescription);
        sendReviewButton = (Button) root.findViewById(R.id.buttonSendReview);

        // SET TEXT
        insertReviewTextView.setText(R.string.insert_review);
        ratingBarTextView.setText(R.string.location);
        takeAPictureTextView.setText(R.string.rating_bar);
        takeAPictureTextView.setText(R.string.take_picture);
        writeDescriptionTextView.setText(R.string.write_description);

        //LISTENERS
        ratingBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RatingBar bar = (RatingBar) v;
                ratingValue = bar.getRating();
            }
        });
        imageButtonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    imageSet = true;
                } catch (ActivityNotFoundException e) {
                    // display error state to the user
                }
            }
        });
        sendReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check permission
                if (checkFields()) {
                    uuidImage = UUID.randomUUID().toString();
                    uploadImage();
                    sendDataToDatabase();
                    drawSnackbar(getString(R.string.review_added), R.color.holoBlueDark).show();
                    Navigation.findNavController(view).navigate(R.id.navigateFromReviewToMap);
                    drawSnackbar(getString(R.string.review_uploaded), R.color.md_green_500).show();


                } else {
                    drawSnackbar(getString(R.string.field_cannot_be_empty), R.color.md_red_500).show();
                }
            }


        });
        return root;
    }
    //GET USER FROM DB


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mAuth.getCurrentUser() == null) {
            drawSnackbar(getString(R.string.you_have_to_login), R.color.md_red_500).show();
            Navigation.findNavController(view).navigate(R.id.navigateFromReviewToLogin);
        } else {

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == REQUEST_IMAGE_CAPTURE
                && resultCode == RESULT_OK) {

            Bundle extras = data.getExtras();
            // Get the Uri of data
            imageBitmap = (Bitmap) extras.get("data");
            //ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            //imageBitmap.compress(Bitmap.CompressFormat.JPEG,,bytes);
            filePath = Uri.parse(MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), imageBitmap, "Title", null));
            imageButtonCamera.setImageBitmap(imageBitmap);

        }
    }

    public void getUserFromDB() {

        uuidUser = user.getUid();
        DatabaseReference referenceUser = database.getReference("users").child(uuidUser);

        referenceUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserHelperClass uhc = snapshot.getValue(UserHelperClass.class);
                username = uhc.getUsername();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //GET CURRENT LOCATION ADDRESS AND LONGITUDE/LATITUDE
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
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
                            //Set longitude and latitude inside a variable
                            latLng = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
                            String address = addresses.get(0).getAddressLine(0);
                            locationTextView.setText((String) String.valueOf(address));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    //CHECK FIELDS FORMAT
    private boolean checkFields() {
        if (latLng != null && imageSet) {
            return true;
        } else {
            return false;
        }
    }

    //UPLOAD IMAGE TO STORAGE
    private void uploadImage() {
        if (filePath != null) {

            StorageReference ref = storageReference.child("images/" + uuidImage);

            // adding listeners on upload
            // or failure of image
            ref.putFile(filePath)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                }
                            })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        }
                    });
        }
    }

    //SEND REVIEW TO DATABASE
    private void sendDataToDatabase() {
        //Initialize firebase database
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("reviews");

        ReviewHelperClass reviewHelperClass = new ReviewHelperClass(uuidUser, latLng.latitude, latLng.longitude,
                ratingBar.getRating(), descriptionEditText.getText().toString(), new Date());

        reference.child(uuidImage).setValue(reviewHelperClass);
        sendMarkerTDB();

    }

    //SEND MARKER TO DATABASE
    private void sendMarkerTDB() {
        //Initialize firebase database
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("markers");

        HelperMap helperMap = new HelperMap(latLng.latitude, latLng.longitude);
        reference.child(uuidImage).setValue(helperMap);
    }

    private Snackbar drawSnackbar(String text, int color) {
        Snackbar snackbar = Snackbar.make(getView(), text, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getActivity().getResources().getColor(color));
        return snackbar;
    }
    //ON ACTIVITY RESULT FROM CAMERA


}