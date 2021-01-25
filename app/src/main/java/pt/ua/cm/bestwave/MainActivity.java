package pt.ua.cm.bestwave;


import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import pt.ua.cm.bestwave.ui.authentication.UserHelperClass;

public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration mAppBarConfiguration;
    NavigationView navigationView;
    DrawerLayout drawer;

    TextView nameSurname, email;
    ImageView picProfile;
    UserHelperClass uhc;
    FloatingActionButton fab;
    //FIREBASE
    private FirebaseAuth mAuth;

    FirebaseStorage storage;
    StorageReference storageReference;
    //FIREBASE DATABASE
    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //FIREBESE INSTANCE
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        changeColorToBar();
        //SETTING TOOLBAR
        Toolbar toolbar = findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);
        //SETTING FAB
        fab= findViewById(R.id.fab);

        //SETTING DRAWER LAYOUT
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_maps, R.id.nav_review, R.id.nav_profile, R.id.nav_login)
                .setOpenableLayout(drawer)
                .build();
        //SETTING NAV CONTROLLER
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //SET LOGOUT ON CLICK OF LOCOUT AND UPDATE THE UI
        setNavViewOnClickItem();

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        View header = navigationView.getHeaderView(0);
        nameSurname = header.findViewById(R.id.textViewNameNavHeader);
        email = header.findViewById(R.id.textViewMailNavHeader);
        picProfile = header.findViewById(R.id.imageViewNavHeader);


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        updateUI();
    }

    public void updateUI() {
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {//LOGGED
            navigationView.getMenu().getItem(3).setTitle("Logout");
            getUserFromDB();

        } else {//NOT LOGGED
            navigationView.getMenu().getItem(3).setTitle("Login");
            nameSurname.setText("");
            email.setText("");
            picProfile.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                    R.drawable.user_register_pic));
        }

    }

    private void changeColorToBar() {
        Window window = getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.bluMedio));
    }

    ;


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void setNavViewOnClickItem() {

        navigationView.getMenu().getItem(3).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getTitle().toString().equals("Logout")) {
                    mAuth.signOut();
                    updateUI();
                } else {
                    updateUI();
                }
                if(item.getTitle().toString().equals(R.string.menu_maps)){
                    fab.setVisibility(View.VISIBLE);
                }


                return false;
            }
        });

    }

    public void getUserFromDB() {
        reference = database.getReference("users").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                uhc = snapshot.getValue(UserHelperClass.class);
                nameSurname.setText(uhc.getName().toUpperCase() + " " + uhc.getSurname().toUpperCase());
                email.setText((uhc.getEmail()));
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
        storageReference.child("profileImages/" + currentUser.getUid())
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if (uri != null) {
                    Glide.with(getApplicationContext()).load(uri).centerCrop().into(picProfile);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}