package pt.ua.cm.bestwave.ui.maps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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
import pt.ua.cm.bestwave.ProfileReviewFragment;
import pt.ua.cm.bestwave.R;
import pt.ua.cm.bestwave.SearchBarFragment;
import pt.ua.cm.bestwave.ui.profile.HelperAdapterProfile;
import pt.ua.cm.bestwave.ui.profile.ReviewDetail;
import pt.ua.cm.bestwave.ui.review.ReviewHelperClass;

public class MapsFragment extends Fragment {

    SupportMapFragment supportMapFragment;
    FragmentManager fm;
    SearchView searchView;
    FusedLocationProviderClient client;
    GoogleMap map;
    SearchBarFragment bar;
    //FIREBASE DATABASE
    FirebaseDatabase database;
    DatabaseReference reference;

    ReviewHelperClass rhc;
    View viewMarkerInfo;
    TextView title;


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
                            MarkerOptions options= new MarkerOptions().position(latLng).title("Current Location")
                                    .icon(BitmapDescriptorFactory.fromBitmap((getBitmapFromVectorDrawable(getContext(),R.drawable.ic_van_de_surf))))
                                    .zIndex(999);
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,20));
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

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private void setMarkerToMap() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference().child("markers");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String tag = "";
                String ltd = "";
                String lng = "";

                for (DataSnapshot id : snapshot.getChildren()){
                    for(DataSnapshot ltdlng : id.getChildren()){
                        if(ltdlng.getKey().equals("latitude")){
                            ltd = String.valueOf(ltdlng.getValue());
                        }else{
                            lng = String.valueOf(ltdlng.getValue());
                        }
                        tag=String.valueOf(id.getKey());
                    }
                    LatLng reviewPosition = new LatLng(Double.parseDouble(ltd),Double.parseDouble(lng));
                    Marker marker =  map.addMarker(new MarkerOptions()
                            .position(reviewPosition));
                    //marker.setTitle("View Review");
                    marker.setTag(tag);
                    Log.d("TAG",tag);
                    map.addMarker(new MarkerOptions().position(reviewPosition)).setTitle("View Review");
                    map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            Log.d("PROVA","PROVA");
                            ProfileReviewFragment fragment = new ProfileReviewFragment();
                            FragmentManager fm = getActivity().getSupportFragmentManager();
                            FragmentTransaction transaction = fm.beginTransaction();
                            transaction.replace(R.id.map, fragment);
                            transaction.addToBackStack(null);
                            transaction.commit();

                            return false;
                        }
                    });
                    //setMarkerInfoWindow();

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

    private void setMarkerInfoWindow(){

        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker args) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(Marker args) {

                // Getting view from the layout file info_window_layout
                viewMarkerInfo = getLayoutInflater().inflate(R.layout.info_window_layout, null);
                Log.d("MINCHIA",String.valueOf(args.getTag()));
                getReviewDetailFromDB(String.valueOf(args.getTag()));
                // Getting the position from the marker


                map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    public void onInfoWindowClick(Marker marker) {
                        Log.d("MARKEROK2","MARKEROK2");



                    }
                });

                // Returning the view containing InfoWindow contents
                return viewMarkerInfo;

            }
        });




    }

    public void getReviewDetailFromDB(String tag){

        Log.d("TAGUUID",tag);
        reference = FirebaseDatabase.getInstance().getReference("reviews").child(tag);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d("PROVISSIMA",snapshot.getKey());
                    rhc = snapshot.getValue(ReviewHelperClass.class);
                    title = (TextView) viewMarkerInfo.findViewById(R.id.titleTextViewMarkerInfo);
                    Log.d("PROVISSIMA2",String.valueOf(rhc.getStars()));
                    Log.d("PROVISSIMA3",String.valueOf(title.getText()));
                    CharSequence stars = String.valueOf(rhc.getStars());
                    title.setText("PROVA");
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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