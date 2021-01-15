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
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

import pt.ua.cm.bestwave.MainActivity;
import pt.ua.cm.bestwave.R;

public class HelperAdapterProfile extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    View view;
    ArrayList<String> arrayListName = new ArrayList<String>();
    ProfileViewHolderClass viewHolderClass;
    Boolean isPhone = false;
    ImageButton buttonReviewDetail;

    public HelperAdapterProfile(){
        this.context=context;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rw_profile,parent,false);
        viewHolderClass= new ProfileViewHolderClass(view);
        // onClick handler for review detail
        buttonReviewDetail = view.findViewById(R.id.openInfoReviewProfileButtonImage);
        buttonReviewDetail.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ReviewDetail fragment = new ReviewDetail();
                FragmentManager fm = ((MainActivity) v.getContext()).getSupportFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.add(R.id.profile_container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        return viewHolderClass;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        viewHolderClass=(ProfileViewHolderClass)holder;
        viewHolderClass.textViewDate.setText("30/04/1996");
        viewHolderClass.textViewScore.setText("3/5");
        viewHolderClass.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public class ProfileViewHolderClass extends RecyclerView.ViewHolder {
        TextView textViewDate;
        TextView textViewScore;
        public ProfileViewHolderClass(@NonNull View itemView) {
            super(itemView);
            textViewDate=(TextView)itemView.findViewById(R.id.dataTextViewProfile);
            textViewScore=(TextView)itemView.findViewById(R.id.starTextViewProfile);
        }
    }
}


