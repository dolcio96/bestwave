package pt.ua.cm.bestwave.ui.authentication;

import android.annotation.SuppressLint;
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

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import pt.ua.cm.bestwave.MainActivity;
import pt.ua.cm.bestwave.R;



public class RegisterFragment extends Fragment {
    FirebaseDatabase database;
    DatabaseReference reference;

    EditText regEmail,regUsername,regName,regSurname;
    EditText regPassword,regConfirmPassword;
    Button btnRegister, btnback;
    @SuppressLint("WrongViewCast")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");

                String username = regUsername.getText().toString();
                String email = regEmail.getText().toString();
                String name = regName.getText().toString();
                String surname = regSurname.getText().toString();
                String password = regPassword.getText().toString();

                UserHelperClass userHelperClass = new UserHelperClass(username,email,name,surname,password);

                reference.child(username).setValue(userHelperClass);
            }
        });

        return view;
    }
}