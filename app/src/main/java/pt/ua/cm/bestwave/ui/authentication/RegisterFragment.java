package pt.ua.cm.bestwave.ui.authentication;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.Executor;

import pt.ua.cm.bestwave.MainActivity;
import pt.ua.cm.bestwave.R;



public class RegisterFragment extends Fragment {
    //AUTHENTICATION
    private FirebaseAuth mAuth;

    //DATABASE
    FirebaseDatabase database;
    DatabaseReference reference;

    NavigationView navigationView;
    EditText regEmail,regUsername,regName,regSurname;
    EditText regPassword,regConfirmPassword;
    Button btnRegister, btnback;
    String email,password,name,surname,username;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("WrongViewCast")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //
        //GET FIREBASE INSTANCE
        mAuth = FirebaseAuth.getInstance();
        FirebaseAuth.getInstance().signOut();
        navigationView = getActivity().findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(3).setTitle("Login");


        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_register, container, false);
        regUsername = view.findViewById(R.id.et_username);
        regEmail = view.findViewById(R.id.et_email);
        regName = view.findViewById(R.id.et_name);
        regSurname = view.findViewById(R.id.et_surname);
        regPassword = view.findViewById(R.id.et_password_register);
        regConfirmPassword = view.findViewById(R.id.et_confirm_password_register);
        btnRegister = view.findViewById(R.id.button_signup);
        btnback= view.findViewById(R.id.button_goto_login);
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginFragment fragment = new LoginFragment();
                FragmentManager fm = ((MainActivity) v.getContext()).getSupportFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.container_signup, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = regEmail.getText().toString();
                password = regPassword.getText().toString();
                username = regUsername.getText().toString();
                name = regName.getText().toString();
                surname = regSurname.getText().toString();

                //CREATE USER ON FIREBASE AUTHENTICATION
                createUser();
            }
        });

        return view;
    }

    public void createUser(){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener( getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG1", "createUserWithEmail:success");
                            Snackbar.make(getView(), "Welcome!", Snackbar.LENGTH_LONG)
                                    .setAction("User added. Welcome "+ name , null).setBackgroundTint(getActivity().getResources().getColor(R.color.md_green_500)).show();
                            //ADD USER TO DATABASE
                            addUserToDB();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG2", "createUserWithEmail:failure", task.getException());
                        }
                    }

                });

    }

    public void addUserToDB(){
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");
        FirebaseUser user = mAuth.getCurrentUser();
        UserHelperClass userHelperClass = new UserHelperClass(username,email,name,surname,password);
        reference.child(user.getUid()).setValue(userHelperClass);
    }


}