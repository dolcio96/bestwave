package pt.ua.cm.bestwave.ui.profile;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

import pt.ua.cm.bestwave.R;
import pt.ua.cm.bestwave.ui.authentication.UserHelperClass;
import pt.ua.cm.bestwave.ui.maps.MapsFragmentDirections;
import pt.ua.cm.bestwave.ui.review.ReviewHelperClass;

public class ProfileFragment extends Fragment {

    private ProfileViewModel homeViewModel;
    RecyclerView reviewRecyclerView;
    UserHelperClass uhc;
    HelperAdapterProfile helperAdapterProfile;
    ReviewHelperClass rhc=null;
    String uuidUser;

    ImageView imageProfileView;
    TextView nameSurnameTextView;
    TextView emailTextView;

    //FIREBASE STORAGE
    FirebaseStorage storage;
    StorageReference storageReference;
    //FIREBASE DATABASE
    FirebaseDatabase database;
    DatabaseReference reference;
    //FIREBASE AUTHENTICATION
    FirebaseAuth mAuth;
    FirebaseUser user;
    HashMap<String,ReviewHelperClass> reviewMap = new HashMap<String, ReviewHelperClass>();



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        // get the Firebase  storage reference
        storageReference = storage.getReference();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(user!=null){
            getUserFromDB();

        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //GET VIEW PROFILE COMPONENTS
        imageProfileView=view.findViewById(R.id.profile_image_image_view);
        nameSurnameTextView =view.findViewById(R.id.name_surname_profile_text_view);
        emailTextView=view.findViewById(R.id.email_profile_text_view);
        reviewRecyclerView=view.findViewById(R.id.recyclerviewItem);
        reviewRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(mAuth.getCurrentUser()==null){
            drawSnackbar("You have to login before",R.color.md_red_500).show();
            Navigation.findNavController(view).navigate(R.id.navigateFromProfileToLogin);
        }else {
            getReviewsFromDB();
        }

    }

    //GET USER FROM DB
    public void getUserFromDB(){

        uuidUser = user.getUid();
        reference = database.getReference("users").child(uuidUser);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                uhc = snapshot.getValue(UserHelperClass.class);
                nameSurnameTextView.setText(uhc.getName().toUpperCase()+" "+uhc.getSurname().toUpperCase());
                emailTextView.setText((uhc.getEmail()));
                //GET USER IMAGE
                getUserImage();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getUserImage(){
        storageReference.child("profileImages/"+user.getUid())
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getContext()).load(uri).centerCrop().into(imageProfileView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });




    }

    public void getReviewsFromDB(){
        reference = FirebaseDatabase.getInstance().getReference("reviews");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    ReviewHelperClass rhc = ds.getValue(ReviewHelperClass.class);
                    if(uuidUser.equals(rhc.getUuidUser())){
                        reviewMap.put(ds.getKey(),rhc);
                    }
                }

                helperAdapterProfile =new HelperAdapterProfile(reviewMap);
                reviewRecyclerView.setAdapter(helperAdapterProfile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private Snackbar drawSnackbar(String text, int color){
        Snackbar snackbar =  Snackbar.make(getView(), text, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getActivity().getResources().getColor(color));
        return snackbar;
    }
}