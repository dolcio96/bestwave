package pt.ua.cm.bestwave.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import pt.ua.cm.bestwave.MainActivity;
import pt.ua.cm.bestwave.R;
import pt.ua.cm.bestwave.ui.maps.MapsFragmentDirections;
import pt.ua.cm.bestwave.ui.review.ReviewHelperClass;

public class HelperAdapterProfile extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    ReviewHelperClass rhc;
    ArrayList<ReviewHelperClass> arrayListReview = new ArrayList<ReviewHelperClass>();
    ProfileViewHolderClass viewHolderClass;
    HashMap<String, ReviewHelperClass> reviewMap;
    View view;

    public HelperAdapterProfile(HashMap<String, ReviewHelperClass> reviewMap){
        this.context=context;
        this.reviewMap=reviewMap;

        for (Map.Entry entry : reviewMap.entrySet()) {

            arrayListReview.add((ReviewHelperClass)entry.getValue());
        }

    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rw_profile,parent,false);
        viewHolderClass= new ProfileViewHolderClass(view);
        return viewHolderClass;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        viewHolderClass=(ProfileViewHolderClass)holder;
        rhc = arrayListReview.get(position);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        viewHolderClass.textViewDate.setText(String.valueOf(formatter.format(rhc.getDate())));
        viewHolderClass.textViewScore.setText(String.valueOf(rhc.getStars())+"/5");
        viewHolderClass.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ProfileFragmentDirections.NavigateFromProfileToReviewDetail action =
                        ProfileFragmentDirections.navigateFromProfileToReviewDetail(rhc);
                action.setCurrentRhc(rhc);
                Navigation.findNavController(view).navigate(action);
                /*ReviewDetail fragment = new ReviewDetail(rhc);
                FragmentManager fm = ((MainActivity) v.getContext()).getSupportFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.add(R.id.profile_container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();*/
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrayListReview.size();
    }

    public class ProfileViewHolderClass extends RecyclerView.ViewHolder {
        TextView textViewDate,textViewScore;
        public ProfileViewHolderClass(@NonNull View itemView) {
            super(itemView);
            textViewDate=(TextView)itemView.findViewById(R.id.dataTextViewProfile);
            textViewScore=(TextView)itemView.findViewById(R.id.starTextViewProfile);
        }
    }
}


