package pt.ua.cm.bestwave.ui.review;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import pt.ua.cm.bestwave.MainActivity;
import pt.ua.cm.bestwave.R;
import pt.ua.cm.bestwave.ui.authentication.UserHelperClass;
import pt.ua.cm.bestwave.ui.maps.HelperMap;

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
    TextView locationTextView;

    //Location variables
    private FusedLocationProviderClient mFusedLocationClient;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    LatLng latLng;
    boolean imageSet = false;
    String uuid;

    //Database Firebase variables
    FirebaseDatabase database;
    DatabaseReference reference;

    // Uri indicates, where the image will be picked from
    private Uri filePath;

    // request code
    private final int PICK_IMAGE_REQUEST = 22;

    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storageReference;


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

        locationTextView=(TextView) root.findViewById(R.id.locationTextView);

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
                    imageSet = true;
                } catch (ActivityNotFoundException e) {
                    // display error state to the user
                }
            }
        });
        editText = (EditText) root.findViewById(R.id.editTextWriteDescription);

        // initialize FusedLocationProviderClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //when permission granted
            getLocation();
        } else {
            //when permission is denied
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }

        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        button = (Button) root.findViewById(R.id.buttonSendReview);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO move this check into onCreate (aggiungere TextView  che conterrà la posizione scritta in città)
                //check permission


                if(checkFields()){
                    uuid = UUID.randomUUID().toString();
                    uploadImage();
                    if(sendDataToDatabase()){
                        FragmentActivity a = getActivity();
                        Bitmap icon = BitmapFactory.decodeResource(a.getResources(), R.mipmap.camera_image);
                        ratingBar.setRating(Float.parseFloat("0.0"));
                        //TODO Implement set image for camera
                        //imageButton.setImageBitmap(iconCamera);
                        //editText.setText("");
                        Snackbar.make(view, "Review added!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).setBackgroundTint(getActivity().getResources().getColor(R.color.holoBlueDark)).show();

                    }else{
                        Snackbar.make(view, "Data upload failure, try later!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).setBackgroundTint(getActivity().getResources().getColor(R.color.md_red_500)).show();
                    }

                }else{
                    Snackbar.make(view, "Some fields are empty!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).setBackgroundTint(getActivity().getResources().getColor(R.color.md_red_500)).show();

                }
            }


        });

    return root;
    }

    // UploadImage method
    private void uploadImage()
    {
        if (filePath != null) {

            // Code for showing progressDialog while uploading
            final ProgressDialog progressDialog
                    = new ProgressDialog(getActivity());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Defining the child of storageReference
            StorageReference ref
                    = storageReference.child("images/"+ uuid);

            // adding listeners on upload
            // or failure of image
            ref.putFile(filePath)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                                {
                                    Snackbar.make(getView(), "Image uploaded!", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).setBackgroundTint(getActivity().getResources().getColor(R.color.md_green_500)).show();
                                    progressDialog.dismiss();
                                }
                            })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            //TODO
                        }
                    })
                    .addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                // Progress Listener for loading
                                // percentage on the dialog box
                                @Override
                                public void onProgress(
                                        UploadTask.TaskSnapshot taskSnapshot)
                                {
                                    //TODO
                                }
                            });
        }
    }

    private boolean checkFields() {
        if(latLng!=null && imageSet){
            return true;
        }
        else{
            return false;
        }
    }

    private boolean sendDataToDatabase() {
        //Initialize firebase database
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("reviews");

        String username = "Bruno";

        ReviewHelperClass reviewHelperClass = new ReviewHelperClass(username,latLng.latitude,latLng.longitude,ratingBar.getRating(),editText.getText().toString(),uuid);

        reference.child(uuid).setValue(reviewHelperClass);
        sendMarkerTDB();

        return true;
    }

    private void sendMarkerTDB() {
        //Initialize firebase database
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("markers");

        HelperMap helperMap = new HelperMap(latLng.latitude,latLng.longitude);
        reference.child(uuid).setValue(helperMap);
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
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
                        latLng = new LatLng(addresses.get(0).getLatitude(),addresses.get(0).getLongitude());
                        String address = addresses.get(0).getAddressLine(0);
                        locationTextView.setText((String) String.valueOf(address));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
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
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes);
                filePath = Uri.parse(MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), imageBitmap,"Title",null));
                imageButton.setImageBitmap(imageBitmap);

            }else{
                Snackbar.make(getView(), "problemi!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).setBackgroundTint(getActivity().getResources().getColor(R.color.md_red_500)).show();
            }
        }
}