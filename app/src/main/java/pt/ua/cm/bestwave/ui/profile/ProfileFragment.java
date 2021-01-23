package pt.ua.cm.bestwave.ui.profile;

import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

import pt.ua.cm.bestwave.R;
import pt.ua.cm.bestwave.ui.authentication.UserHelperClass;
import pt.ua.cm.bestwave.ui.review.ReviewHelperClass;

public class ProfileFragment extends Fragment {

    private ProfileViewModel homeViewModel;
    RecyclerView reviewRecyclerView;
    FirebaseRecyclerAdapter adapter;
    UserHelperClass uhc;
    HelperAdapterProfile helperAdapterProfile;
    ReviewHelperClass rhc=null;
    String uuidUser;

    ImageView imageProfileView;
    TextView nameTextView;
    TextView surnameTextView;
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
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        // get the Firebase  storage reference
        storageReference = storage.getReference();
        getUserFromDB();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        //GET VIEW PROFILE COMPONENTS
        imageProfileView=view.findViewById(R.id.profile_image_image_view);
        nameTextView=view.findViewById(R.id.name_profile_text_view);
        surnameTextView=view.findViewById(R.id.surname_rpofile_text_view);
        emailTextView=view.findViewById(R.id.email_profile_text_view);
        reviewRecyclerView=view.findViewById(R.id.recyclerviewItem);
        reviewRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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

        return view;
    }



    //GET USER FROM DB
    public void getUserFromDB(){

        uuidUser = user.getUid();
        reference = database.getReference("users").child(uuidUser);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                uhc = snapshot.getValue(UserHelperClass.class);
                nameTextView.setText(uhc.getName().toUpperCase());
                surnameTextView.setText(uhc.getSurname().toUpperCase());
                emailTextView.setText((uhc.getEmail()));
                //getReviewsFromDB();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getReviewsFromDB(){
        reference = database.getReference("reviews");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot uuidImage : snapshot.getChildren()){

                    ReviewHelperClass rhc = uuidImage.getValue(ReviewHelperClass.class);

                    if (rhc.getUuidUser().equals(uuidUser)){
                        reviewMap.put(uuidImage.getKey(),rhc);
                    }

                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}