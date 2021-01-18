package pt.ua.cm.bestwave.ui.maps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.ua.cm.bestwave.MainActivity;
import pt.ua.cm.bestwave.R;
import pt.ua.cm.bestwave.SearchBarFragment;

public class MapsFragment extends Fragment {

    SupportMapFragment supportMapFragment;
    FragmentManager fm;
    SearchView searchView;
    FusedLocationProviderClient client;
    GoogleMap map;
    SearchBarFragment bar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        supportMapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        client = LocationServices.getFusedLocationProviderClient(getActivity());
        //check permission
        if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            getCurrentLocation();
            //setSearchView(view);
        }
        else{
            //Request permission
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
        }


    }


    private void getCurrentLocation(){
        @SuppressLint("MissingPermission") Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                if (location!=null){
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            map = googleMap;
                            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                            MarkerOptions options= new MarkerOptions().position(latLng).title("Current Location");
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));
                            googleMap.addMarker(options);
                            addSearchView(googleMap);
                            setMarkerToMap();
                        }

                    });
                }
                else {
                    Log.d("LOCATION NULL","LOCATION NULL");
                }
            }

        });
    }

    private void setMarkerToMap() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference().child("markers");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String ltd = "";
                String lng = "";

                Map<String, HelperMap> helperMap =(Map<String, HelperMap>) snapshot.getValue();

                for (DataSnapshot id : snapshot.getChildren()){
                    for(DataSnapshot ltdlng : id.getChildren()){


                        if(ltdlng.getKey().equals("latitude")){
                            ltd = String.valueOf(ltdlng.getValue());
                        }else{
                            lng = String.valueOf(ltdlng.getValue());
                        }
                    }
                    LatLng reviewPosition = new LatLng(Double.parseDouble(ltd),Double.parseDouble(lng));
                    map.addMarker(new MarkerOptions().position(reviewPosition));
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void addSearchView(GoogleMap map){
        fm = getParentFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        bar = new SearchBarFragment(map);
        ft.add(R.id.map,bar).addToBackStack(null).commit();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==44){
            if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getCurrentLocation();
            }

        }
    }


}