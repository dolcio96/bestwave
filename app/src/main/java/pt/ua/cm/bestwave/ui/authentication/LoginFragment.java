package pt.ua.cm.bestwave.ui.authentication;

import android.os.Bundle;

import androidx.annotation.NonNull;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import pt.ua.cm.bestwave.MainActivity;
import pt.ua.cm.bestwave.R;


public class LoginFragment extends Fragment {
    //AUTHENTICATION
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference reference;
    EditText regUsername, regPassword;
    Button buttonRegister,buttonLogin;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        mAuth =FirebaseAuth.getInstance();
        regUsername = view.findViewById(R.id.editTextUsername);
        regPassword = view.findViewById(R.id.editTextPassword);
        buttonRegister = view.findViewById(R.id.button_signup);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterFragment fragment = new RegisterFragment();
                FragmentManager fm = ((MainActivity) v.getContext()).getSupportFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.container_login, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        buttonLogin = view.findViewById(R.id.button_login);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser(v);


            }
        });

        return view;
    }
    public void loginUser(View view){
        if(!validatePassword()){//!validateUsername()|

        }
        else{
            logInUserValidate();
        }


    }

    private Boolean validateUsername(){
        String val = regUsername.getText().toString();
        String noWhiteSpace = "\\A\\w{4,20}\\z";
        if (val.isEmpty()){
            regUsername.setError("Field cannot be empty");
            return false;
        }
        else if (!val.matches(noWhiteSpace)){
            regUsername.setError("White Spaces are not allowed");
            return false;
        }
        else{
            regUsername.setError(null);
            return true;
        }

    }

    private Boolean validatePassword(){
        String val = regPassword.getText().toString();
        String noWhiteSpace = "\\A\\w{4,20}\\z";
        if (val.isEmpty()){
            regPassword.setError("Field cannot be empty");
            return false;
        }
        else{
            regPassword.setError(null);
            return true;
        }
    }



    public void logInUserValidate(){
        final String email = regUsername.getText().toString().trim();
        final String password = regPassword.getText().toString().trim();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Snackbar.make(getView(), "Welcome!", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).setBackgroundTint(getActivity().getResources().getColor(R.color.md_green_500)).show();

                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("NOTLOGGED", "signInWithEmail:failure", task.getException());
                        }
                    }
                });



    }

    private void isUser() {
        final String userEnteredUsername = regUsername.getText().toString().trim();
        final String userEnteredPassword = regPassword.getText().toString().trim();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("users");

        Query checkUser = ref.orderByChild("username").equalTo(userEnteredUsername);

        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    regUsername.setError(null);

                    String passwordFromDB = dataSnapshot.child(userEnteredUsername).child("password").getValue(String.class);

                    if(passwordFromDB.equals(userEnteredPassword)){

                        String nameFromDB = dataSnapshot.child(userEnteredUsername).child("name").getValue(String.class);
                        String surnameFromDB = dataSnapshot.child(userEnteredUsername).child("surname").getValue(String.class);

                        Log.d("FUNGE","FUNGE");


                    }
                    else {
                        regPassword.setError("Wrong Password");
                        regPassword.requestFocus();
                    }
                }
                else {
                    regUsername.setError("No such User exist");
                    regUsername.requestFocus();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }



}