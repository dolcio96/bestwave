package pt.ua.cm.bestwave.ui.review;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
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
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private final int PICK_IMAGE_REQUEST = 22;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    //VIEW VARIABLE
    private ReviewViewModel galleryViewModel;
    ImageButton imageButtonCamera;
    RatingBar ratingBar = null;
    Float ratingValue = 0.0f;
    Bitmap imageBitmap = null;
    Button sendReviewButton;
    EditText descriptionEditText = null;
    TextView locationTextView;

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
        //TODO ADD CHECH FOR NON LOGGED USER, REDIRECT TO LOGGIN
        getUserFromDB();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        galleryViewModel =
                ViewModelProviders.of(this).get(ReviewViewModel.class);
        View root = inflater.inflate(R.layout.fragment_review, container, false);

        final TextView textViewInsertReview = root.findViewById(R.id.locationTextView);
        galleryViewModel.getTextInsertReview().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textViewInsertReview.setText(s);
            }
        });

        final TextView textViewLocation = root.findViewById(R.id.inserReviewTextView);
        galleryViewModel.getTextLocation().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textViewLocation.setText(s);
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

        // SET VIEW VARIABLE
        locationTextView = root.findViewById(R.id.locationTextView);
        ratingBar = root.findViewById(R.id.ratingBar);
        imageButtonCamera = root.findViewById(R.id.imageButtonCamera);
        descriptionEditText = root.findViewById(R.id.editTextWriteDescription);
        sendReviewButton = (Button) root.findViewById(R.id.buttonSendReview);

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
                //TODO move this check into onCreate (aggiungere TextView  che conterrà la posizione scritta in città)
                //check permission
                if (checkFields()) {
                    uuidImage = UUID.randomUUID().toString();
                    uploadImage();
                    sendDataToDatabase();
                    drawSnackbar("Review added!",R.color.holoBlueDark).show();
                    Navigation.findNavController(view).navigate(R.id.navigateFromReviewToMap);
                    drawSnackbar("Data upload failure, try later!",R.color.md_red_500).show();


                } else {
                    drawSnackbar("Some fields are empty!",R.color.md_red_500).show();
                }
            }


        });
        return root;
    }
    //GET USER FROM DB
    public void getUserFromDB(){

        uuidUser = user.getUid();
        DatabaseReference referenceUser = database.getReference("users").child(uuidUser);

        referenceUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("LOG13",snapshot.getValue().toString());
                UserHelperClass uhc = snapshot.getValue(UserHelperClass.class);
                username=uhc.getUsername();
                Log.d("USER",username);
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
                            //TODO editText.setText(Html.fromHtml("Address : " + addresses.get(0).getAddressLine(0)));
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
        }else {
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

            // Code for showing progressDialog while uploading
            final ProgressDialog progressDialog
                    = new ProgressDialog(getActivity());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Defining the child of storageReference
            StorageReference ref
                    = storageReference.child("images/" + uuidImage);

            // adding listeners on upload
            // or failure of image
            ref.putFile(filePath)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Snackbar.make(getView(), "Image uploaded!", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).setBackgroundTint(getActivity().getResources().getColor(R.color.md_green_500)).show();
                                    progressDialog.dismiss();
                                }
                            })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //TODO
                        }
                    })
                    .addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                // Progress Listener for loading
                                // percentage on the dialog box
                                @Override
                                public void onProgress(
                                        UploadTask.TaskSnapshot taskSnapshot) {
                                    //TODO
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
                ratingBar.getRating(), descriptionEditText.getText().toString(),new Date());

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
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes);
                filePath = Uri.parse(MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), imageBitmap,"Title",null));
                imageButtonCamera.setImageBitmap(imageBitmap);

            }else{
                Snackbar.make(getView(), "problemi!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).setBackgroundTint(getActivity().getResources().getColor(R.color.md_red_500)).show();
            }
        }





}