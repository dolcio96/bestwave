package pt.ua.cm.bestwave.ui.authentication;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
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

import pt.ua.cm.bestwave.R;


public class LoginFragment extends Fragment {
    //AUTHENTICATION
    private FirebaseAuth mAuth;
    EditText regEmail, regPassword;
    Button buttonRegister,buttonLogin;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        mAuth =FirebaseAuth.getInstance();
        regEmail = view.findViewById(R.id.editEmail);
        regPassword = view.findViewById(R.id.editTextPassword);
        buttonRegister = view.findViewById(R.id.button_signup);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.navigateFromLoginToRegister);
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
        if(isValidEmail() && isValidPassword()){
            logInUserValidate();
        }
        else{
            drawSnackbar("Check the fields",R.color.md_red_500).show();
        }


    }

    public boolean isValidEmail() {
        String email = regEmail.getText().toString();
        if (TextUtils.isEmpty(email)){
            regEmail.setError("Field cannot be empty!");
            return false;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            regEmail.setError("Wrong email format!");
            return false;
        }
        regEmail.setError(null);
        return true;
    }

    private Boolean isValidPassword(){
        String pass = regPassword.getText().toString();
        String noWhiteSpace = "\\A\\w{4,20}\\z";
        if (pass.isEmpty()){
            regPassword.setError("Field cannot be empty!");
            return false;
        }
        if(pass.length()<8){
            regPassword.setError("Password must be longer than 8 charachters!");
            return false;
        }
        else{
            regPassword.setError(null);
            return true;
        }
    }

    public void logInUserValidate(){
        final String email = regEmail.getText().toString().trim();
        final String password = regPassword.getText().toString().trim();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's informations
                            drawSnackbar("Welcome " + email + " !",R.color.md_green_500).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            //NAVIGATE TO HOME (MAP)
                            Navigation.findNavController(getView()).navigate(R.id.navigateFromLoginToMap);
                        } else {
                            drawSnackbar("Fail to login, try later",R.color.md_red_500).show();
                        }
                    }
                });
    }

    private Snackbar drawSnackbar(String text, int color){
        Snackbar snackbar =  Snackbar.make(getView(), text, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getActivity().getResources().getColor(color));
        return snackbar;
    }


}