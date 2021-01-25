package pt.ua.cm.bestwave.ui.authentication;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import pt.ua.cm.bestwave.R;


public class LoginFragment extends Fragment {
    //AUTHENTICATION
    private FirebaseAuth mAuth;
    EditText regEmail, regPassword;
    Button buttonRegister, buttonLogin;
    FirebaseUser user;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        //GET VIEWS
        regEmail = view.findViewById(R.id.editEmail);
        regPassword = view.findViewById(R.id.editTextPassword);
        buttonRegister = view.findViewById(R.id.button_signup);
        buttonLogin = view.findViewById(R.id.button_login);
        //SET LISTENER
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.navigateFromLoginToRegister);
            }
        });
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //LOGIN THE USER AFTER THE VALIDATION
                loginUser();
            }
        });
        return view;
    }

    public void loginUser() {
        if (isValidEmail() && isValidPassword()) {
            //LOGIN USER IN FIREBASE
            logInUserValidate();
        } else {
            drawSnackbar(getString(R.string.check_the_fields), R.color.md_red_500).show();
        }
    }

    public boolean isValidEmail() {
        String email = regEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            regEmail.setError(getString(R.string.field_cannot_be_empty));
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            regEmail.setError(getString(R.string.wrong_email_format));
            return false;
        }
        regEmail.setError(null);
        return true;
    }

    private Boolean isValidPassword() {
        String pass = regPassword.getText().toString();
        if (pass.isEmpty()) {
            regPassword.setError(getString(R.string.field_cannot_be_empty));
            return false;
        }
        if (pass.length() < 8) {
            regPassword.setError(getString(R.string.password_must_be_longer));
            return false;
        } else {
            regPassword.setError(null);
            return true;
        }
    }

    public void logInUserValidate() {
        final String email = regEmail.getText().toString().trim();
        final String password = regPassword.getText().toString().trim();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's informations
                            drawSnackbar(getString(R.string.welcome) + email + " !", R.color.md_green_500).show();
                            //NAVIGATE TO HOME (MAP)
                            Navigation.findNavController(getView()).navigate(R.id.navigateFromLoginToMap);
                        } else {
                            drawSnackbar(getString(R.string.fail_to_login), R.color.md_red_500).show();
                        }
                    }
                });
    }

    private Snackbar drawSnackbar(String text, int color) {
        Snackbar snackbar = Snackbar.make(getView(), text, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getActivity().getResources().getColor(color));
        return snackbar;
    }


}