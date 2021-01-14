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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import pt.ua.cm.bestwave.MainActivity;
import pt.ua.cm.bestwave.R;


public class LoginFragment extends Fragment {
    FirebaseDatabase database;
    DatabaseReference reference;
    EditText regUsername, regPassword;
    Button buttonRegister,buttonLogin;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

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

    public void loginUser(View view){
        if(!validateUsername()|!validatePassword()){

        }
        else{
            isUser();
        }


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