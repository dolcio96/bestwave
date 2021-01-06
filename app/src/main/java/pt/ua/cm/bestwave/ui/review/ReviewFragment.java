package pt.ua.cm.bestwave.ui.review;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.snackbar.Snackbar;

import pt.ua.cm.bestwave.MainActivity;
import pt.ua.cm.bestwave.R;

import static android.app.Activity.RESULT_OK;

public class ReviewFragment extends Fragment {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private ReviewViewModel galleryViewModel;
    ImageButton imageButton;
    RatingBar ratingBar =null;
    Float ratingValue =0.0f;
    Bitmap imageBitmap=null;
    Button button;
    EditText editText= null;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                ViewModelProviders.of(this).get(ReviewViewModel.class);
        View root = inflater.inflate(R.layout.fragment_review, container, false);
        final TextView textViewInsertReview = root.findViewById(R.id.inserReviewTextView);

        galleryViewModel.getTextInsertReview().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textViewInsertReview.setText(s);
            }
        });
        final TextView textViewRatingBar = root.findViewById(R.id.ratingBarTextView);
        galleryViewModel.getTextRatingBar().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                textViewRatingBar.setText(s);
            }
        });
        final TextView textViewTakeAPicture = root.findViewById(R.id.takeAPictureTextView);
        galleryViewModel.getTextTakeAPicture().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                textViewTakeAPicture.setText(s);
            }
        });

        final TextView textViewWriteDescription = root.findViewById(R.id.writeDescriptionTextView);
        galleryViewModel.getTextWriteDescription().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                textViewWriteDescription.setText(s);
            }
        });
        ratingBar = (RatingBar) root.findViewById(R.id.ratingBar);
        ratingBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RatingBar bar = (RatingBar) v;
                ratingValue =bar.getRating();
            }
        });
        imageButton = (ImageButton) root.findViewById(R.id.imageButtonCamera);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                } catch (ActivityNotFoundException e) {
                    // display error state to the user
                }
            }
        });
        editText =(EditText) root.findViewById(R.id.editTextWriteDescription);

        button =(Button) root.findViewById(R.id.buttonSendReview);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Review added!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).setBackgroundTint(getActivity().getResources().getColor(R.color.holoBlueDark)).show();

                FragmentActivity a=getActivity();
                Bitmap icon = BitmapFactory.decodeResource(a.getResources(),  R.mipmap.camera_image);
                //TODO Implement set image for camera
                //imageButton.setImageBitmap(iconCamera);
                editText.setText("");
                ratingBar.setRating(Float.parseFloat("0.0"));

            }
        });
    return root;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageButton.setImageBitmap(imageBitmap);
        }
    }
}