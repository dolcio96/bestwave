package pt.ua.cm.bestwave.ui.authentication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

import pt.ua.cm.bestwave.R;

import static android.app.Activity.RESULT_OK;


public class RegisterFragment extends Fragment {
    private static final int RESULT_LOAD_IMAGE = 99 ;
    //AUTHENTICATION
    private FirebaseAuth mAuth;

    //DATABASE
    FirebaseDatabase database;
    DatabaseReference reference;
    //FIREBASE STORAGE
    FirebaseStorage storage;
    StorageReference storageReference;

    NavigationView navigationView;
    EditText regEmail, regUsername, regName, regSurname;
    EditText regPassword, regConfirmPassword;
    Button btnRegister, btnback;
    String email, password, name, surname, username,confirmPassword;
    ImageView btnImage;
    Bitmap imageBitmap =null;
    Uri filePath;
    View view;
    FirebaseUser user;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //GET FIREBASE INSTANCE
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();

        //FirebaseAuth.getInstance().signOut();

    }

    @SuppressLint("WrongViewCast")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        navigationView = getActivity().findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(3).setTitle("Login");

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_register, container, false);
        regUsername = view.findViewById(R.id.et_username);
        regEmail = view.findViewById(R.id.et_email);
        regName = view.findViewById(R.id.et_name);
        regSurname = view.findViewById(R.id.et_surname);
        regPassword = view.findViewById(R.id.et_password_register);
        regConfirmPassword = view.findViewById(R.id.et_confirm_password_register);
        btnRegister = view.findViewById(R.id.button_signup);
        btnback = view.findViewById(R.id.button_goto_login);
        btnImage = view.findViewById(R.id.profile_image);
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
            }
        });
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.navigateFromRegisterToLogin);
            }
        });


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = regUsername.getText().toString();
                email = regEmail.getText().toString();
                name = regName.getText().toString();
                surname = regSurname.getText().toString();
                password = regPassword.getText().toString();
                confirmPassword = regConfirmPassword.getText().toString();


                //CREATE USER ON FIREBASE AUTHENTICATION
                Log.d("URI",String.valueOf(filePath));

                if (validateFields()) {
                    createUser();

                } else {
                    drawSnackbar(getString(R.string.doesnt_respect_format), R.color.md_red_500).show();
                }
            }
        });

        return view;
    }

    public void createUser() {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //REGISTER SUCCESS
                            drawSnackbar(getString(R.string.user_added) + name, R.color.md_green_500).show();
                            //ADD USER TO DATABASE
                            addUserToDB();

                        } else {
                            drawSnackbar(getString(R.string.signup_fail), R.color.md_red_500).show();
                        }
                    }

                });

    }

    public void addUserToDB() {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");
        user = mAuth.getCurrentUser();
        UserHelperClass userHelperClass = new UserHelperClass(username, email, name, surname, password);
        reference.child(user.getUid()).setValue(userHelperClass);
        //NAVIGATE TO HOME (MAP)
        Navigation.findNavController(view).navigate(R.id.navigateFromRegisterToMap);
        if(filePath!=null){
            uploadImageOnStorage();
        }
    }

    private Snackbar drawSnackbar(String text, int color) {
        Snackbar snackbar = Snackbar.make(getView(), text, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getActivity().getResources().getColor(color));
        return snackbar;
    }

    private boolean validateFields() {

        if(!isValidUsername()){
            return false;
        }
        if (!isValidEmail()) {
            return false;
        }
        if (!isValidName()) {
            return false;
        }
        if (!isValidSurname()) {
            return false;
        }
        if (!isValidPassword()) {
            return false;
        }
        if(!isValidSecondPassword()){
            return false;
        }
        else {
            return true;
        }
    }

    private boolean isValidEmail() {
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

    private boolean isValidPassword() {
        String noWhiteSpace = "\\A\\w{4,20}\\z";
        if (password.isEmpty()) {
            regPassword.setError(getString(R.string.field_cannot_be_empty));
            return false;
        }
        if (password.length() < 8) {
            regPassword.setError(getString(R.string.password_must_be_longer));
            return false;
        } else {
            regPassword.setError(null);
            return true;
        }
    }

    private boolean isValidName() {
        if (TextUtils.isEmpty(name)) {
            regName.setError(getString(R.string.field_cannot_be_empty));
            return false;
        }
        if (name.length() < 2) {
            regName.setError(getString(R.string.name_longer));
            return false;
        }
        else {
            regName.setError(null);
            return true;
        }
    }

    private boolean isValidSurname() {
        if (TextUtils.isEmpty(surname)) {
            regSurname.setError(getString(R.string.field_cannot_be_empty));
            return false;
        }
        if (surname.length() < 2) {
            regSurname.setError(getString(R.string.surname_longer));
            return false;
        }
        else {
            regSurname.setError(null);
            return true;
        }
    }

    private boolean isValidUsername() {
        if (TextUtils.isEmpty(username)) {
            regUsername.setError(getString(R.string.field_cannot_be_empty));
            return false;
        }
        if (username.length() < 2) {
            regUsername.setError(getString(R.string.username_longer));
            return false;
        }
        else {
            regUsername.setError(null);
            return true;
        }
    }

    private boolean isValidSecondPassword(){
        if (TextUtils.isEmpty(confirmPassword)) {
            regConfirmPassword.setError(getString(R.string.field_cannot_be_empty));
            return false;
        }
        if (confirmPassword.equals(password)) {
            regConfirmPassword.setError(null);
            return true;
        }
        else {
            regConfirmPassword.setError(getString(R.string.second_password_correspond));
            return false;
        }

    }

    private void uploadImageOnStorage(){
        if (filePath != null) {
            storageReference = storage.getReference().child("profileImages/" + user.getUid());

            storageReference.putFile(filePath)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                }
                            })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //TODO
                        }
                    })
                    .addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                // Progress Listener for loading
                                // percentage on the dialog box
                                @Override
                                public void onProgress(
                                        UploadTask.TaskSnapshot taskSnapshot) {
                                    //TODO
                                }
                            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            // String picturePath contains the path of selected Image
            filePath = data.getData();

            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);

            } catch (IOException e) {
                e.printStackTrace();
            }
            btnImage.setImageURI(filePath);

        }
    }
}