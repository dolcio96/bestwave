package pt.ua.cm.bestwave.ui.maps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.Navigation;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import pt.ua.cm.bestwave.MainActivity;
import pt.ua.cm.bestwave.R;
import pt.ua.cm.bestwave.SearchBarFragment;
import pt.ua.cm.bestwave.Work;

public class MapsFragment extends Fragment {
    //MAP
    GoogleMap map;
    FusedLocationProviderClient client;
    LatLng currentLatLng;

    SupportMapFragment supportMapFragment;
    FloatingActionButton fab;
    String tag;
    View view;
    HashMap<String,LatLng> markersMap = new HashMap<String, LatLng>();


    //FIREBASE
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_maps, container, false);

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fab = getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 8));
            }
        });
        fab.setVisibility(View.VISIBLE);
        supportMapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        client = LocationServices.getFusedLocationProviderClient(getActivity());
        //check permission
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            //Request permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }


    }

    @Override
    public void onStart() {
        super.onStart();
        MainActivity ma = (MainActivity) getActivity();
        ma.updateUI();

        fab.setVisibility(View.VISIBLE);
    }

    private void getCurrentLocation() {
        @SuppressLint("MissingPermission") Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                if (location != null) {
                    //SET MAP STUFF
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            map = googleMap;
                            currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            Marker marker = map.addMarker(new MarkerOptions().position(currentLatLng)
                                    .icon(BitmapDescriptorFactory.fromBitmap((getBitmapFromVectorDrawable(getContext(), R.drawable.ic_van_de_surf))))
                                    .title("Current Location")
                                    .zIndex(999));
                            marker.setTag("CURRENT");
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 7));
                            addSearchView(googleMap);
                            setMarkerToMap();
                        }

                    });


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

        reference = database.getReference().child("markers");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tag = "";
                String ltd = "";
                String lng = "";
                //FOREACH MARKER TAKE THE LATITUDE, LONGITUDE AND TAG
                for (final DataSnapshot id : snapshot.getChildren()) {
                    for (DataSnapshot ltdlng : id.getChildren()) {
                        if (ltdlng.getKey().equals("latitude")) {
                            ltd = String.valueOf(ltdlng.getValue());
                        } else {
                            lng = String.valueOf(ltdlng.getValue());
                        }
                        tag = String.valueOf(id.getKey());
                    }
                    LatLng reviewPosition = new LatLng(Double.parseDouble(ltd), Double.parseDouble(lng));
                    markersMap.put(tag,reviewPosition);
                    //CREATE MARKER AND SET TAG
                    Marker marker = map.addMarker(new MarkerOptions()
                            .position(reviewPosition));
                    marker.setTag(tag);
                    map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            if (!marker.getTag().toString().equals("CURRENT")) {
                                String currentTag = String.valueOf(marker.getTag());
                                MapsFragmentDirections.NavigateFromMapToProfileReview action =
                                        MapsFragmentDirections.navigateFromMapToProfileReview();
                                action.setCurrentTag(currentTag);
                                Navigation.findNavController(view).navigate(action);
                            }
                            return false;
                        }
                    });
                }
                checkIfMarkersNearCurrentLocation();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void addSearchView(GoogleMap map) {
        FragmentManager fm = getParentFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        SearchBarFragment bar = new SearchBarFragment(map);
        ft.add(R.id.map, bar).addToBackStack(null).commit();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        fab.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        fab.setVisibility(View.VISIBLE);
    }

    public void checkIfMarkersNearCurrentLocation(){
        for (Map.Entry entry : markersMap.entrySet()) {
            LatLng entryLatLng = (LatLng) entry.getValue();
            if(entryLatLng.latitude<currentLatLng.latitude+5 && entryLatLng.latitude>currentLatLng.latitude-5
                    && entryLatLng.longitude<currentLatLng.longitude+5 && entryLatLng.longitude>currentLatLng.longitude-5){
                OneTimeWorkRequest simpleRequest = new OneTimeWorkRequest.Builder(Work.class)
                        .build();
                WorkManager.getInstance().enqueue(simpleRequest);
                return;
            }

        }
    }

}