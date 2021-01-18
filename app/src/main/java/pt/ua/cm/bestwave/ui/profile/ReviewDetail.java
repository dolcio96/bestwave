package pt.ua.cm.bestwave.ui.profile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pt.ua.cm.bestwave.MainActivity;
import pt.ua.cm.bestwave.R;


public class ReviewDetail extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_review_detail, container, false);
    }

    @Override
    public void onDestroy() {

        ProfileFragment fragment = new ProfileFragment();
        FragmentManager fm = ((MainActivity) getView().getContext()).getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.add(R.id.detailContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

        super.onDestroy();
    }
}