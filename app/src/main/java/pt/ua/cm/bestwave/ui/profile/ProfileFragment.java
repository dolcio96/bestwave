package pt.ua.cm.bestwave.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import pt.ua.cm.bestwave.R;

public class ProfileFragment extends Fragment {

    private ProfileViewModel homeViewModel;
    RecyclerView reviewRecyclerView;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        reviewRecyclerView=view.findViewById(R.id.recyclerviewItem);
        HelperAdapterProfile helperAdapter=new HelperAdapterProfile();
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext());
        reviewRecyclerView.setLayoutManager(linearLayoutManager);
        reviewRecyclerView.setAdapter(helperAdapter);
        return view;
    }
}