package pt.ua.cm.bestwave.ui.profile;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
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

import java.util.HashMap;

import pt.ua.cm.bestwave.R;
import pt.ua.cm.bestwave.ui.authentication.UserHelperClass;
import pt.ua.cm.bestwave.ui.review.ReviewHelperClass;

public class ProfileFragment extends Fragment {

    UserHelperClass uhc;
    HelperAdapterProfile helperAdapterProfile;

    RecyclerView reviewRecyclerView;
    ImageView imageProfileView;
    TextView nameSurnameTextView;
    TextView emailTextView;
    HashMap<String, ReviewHelperClass> reviewMap = new HashMap<String, ReviewHelperClass>();

    //FIREBASE STORAGE
    FirebaseStorage storage;
    StorageReference storageReference;
    //FIREBASE DATABASE
    FirebaseDatabase database;
    DatabaseReference reference;
    //FIREBASE AUTHENTICATION
    FirebaseAuth mAuth;




    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            getUserFromDB();
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //GET VIEW PROFILE COMPONENTS
        imageProfileView = view.findViewById(R.id.profile_image_image_view);
        nameSurnameTextView = view.findViewById(R.id.name_surname_profile_text_view);
        emailTextView = view.findViewById(R.id.email_profile_text_view);
        //SET LAYOUT MANAGER FOR RECYCLERVIEW
        reviewRecyclerView = view.findViewById(R.id.recyclerviewItem);
        reviewRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mAuth.getCurrentUser() == null) {
            drawSnackbar(getString(R.string.you_have_to_login), R.color.md_red_500).show();
            Navigation.findNavController(view).navigate(R.id.navigateFromProfileToLogin);
        } else {
            getReviewsFromDB();
        }

    }

    //GET USER FROM DB
    public void getUserFromDB() {


        reference = database.getReference("users").child(mAuth.getCurrentUser().getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                uhc = snapshot.getValue(UserHelperClass.class);
                nameSurnameTextView.setText(uhc.getName().toUpperCase() + " " + uhc.getSurname().toUpperCase());
                emailTextView.setText((uhc.getEmail()));
                //GET USER IMAGE
                getUserImage();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getUserImage() {
        storageReference = storage.getReference();
        storageReference.child("profileImages/" + mAuth.getCurrentUser().getUid())
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

    public void getReviewsFromDB() {
        reference = FirebaseDatabase.getInstance().getReference("reviews");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ReviewHelperClass rhc = ds.getValue(ReviewHelperClass.class);
                    if (mAuth.getCurrentUser().getUid().equals(rhc.getUuidUser())) {
                        reviewMap.put(ds.getKey(), rhc);
                    }
                }

                helperAdapterProfile = new HelperAdapterProfile(reviewMap);
                reviewRecyclerView.setAdapter(helperAdapterProfile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private Snackbar drawSnackbar(String text, int color) {
        Snackbar snackbar = Snackbar.make(getView(), text, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getActivity().getResources().getColor(color));
        return snackbar;
    }
}