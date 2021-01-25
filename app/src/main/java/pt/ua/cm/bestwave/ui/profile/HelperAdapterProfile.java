package pt.ua.cm.bestwave.ui.profile;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pt.ua.cm.bestwave.R;
import pt.ua.cm.bestwave.ui.review.ReviewHelperClass;

public class HelperAdapterProfile
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    ReviewHelperClass rhc;
    ArrayList<ReviewHelperClass> arrayListReview = new ArrayList<ReviewHelperClass>();
    ArrayList<String> keys = new ArrayList<String>();
    ArrayList<ImageView> listOfViewOlderImages = new ArrayList<ImageView>();
    ProfileViewHolderClass viewHolderClass;
    HashMap<String, ReviewHelperClass> reviewMap;
    View view;

    FirebaseStorage storage;
    StorageReference storageReference;

    public class ProfileViewHolderClass extends RecyclerView.ViewHolder {
        TextView textViewDate, textViewScore;
        ImageView imageView;
        final HelperAdapterProfile mAdapter;

        public ProfileViewHolderClass(@NonNull View itemView, HelperAdapterProfile adapter) {
            super(itemView);
            this.mAdapter = adapter;
            textViewDate = (TextView) itemView.findViewById(R.id.dataTextViewProfile);
            textViewScore = (TextView) itemView.findViewById(R.id.starTextViewProfile);
            imageView = (ImageView) itemView.findViewById(R.id.imageReviewImageView);
        }
    }


    public HelperAdapterProfile(HashMap<String, ReviewHelperClass> reviewMap) {
        this.reviewMap = reviewMap;
        for (Map.Entry entry : reviewMap.entrySet()) {
            arrayListReview.add((ReviewHelperClass) entry.getValue());
            keys.add((String) entry.getKey());
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rw_profile, parent, false);
        //viewHolderClass= new ProfileViewHolderClass(view);
        return new ProfileViewHolderClass(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        viewHolderClass = (ProfileViewHolderClass) holder;
        //GET REVIEW CLASS
        rhc = arrayListReview.get(position);
        //SET TEXTVIEWS AND COMPONENTS
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM");
        viewHolderClass.textViewDate.setText(String.valueOf(formatter.format(rhc.getDate())));
        viewHolderClass.textViewScore.setText(String.valueOf(rhc.getStars()) + "/5");


        listOfViewOlderImages.add((ImageView) viewHolderClass.imageView);
        //GET IMAGE FROM DATABASE
        storageReference.child("images/" + keys.get(position))
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(viewHolderClass.imageView.getContext()).load(uri).centerCrop().into(listOfViewOlderImages.get(position));
                listOfViewOlderImages.get(position).setAlpha((float)1.0);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
        //SET TAG TO ITEMVIEW FOR MAKE A MAP
        viewHolderClass.itemView.setTag(keys.get(position));
        viewHolderClass.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            ProfileFragmentDirections.NavigateFromProfileToReviewDetail action =
                    ProfileFragmentDirections.navigateFromProfileToReviewDetail(reviewMap.get(v.getTag()));
            action.setCurrentRhc(reviewMap.get(v.getTag()));
            action.setTag(String.valueOf(v.getTag()));
            Navigation.findNavController(view).navigate(action);
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrayListReview.size();
    }


}

