package pt.ua.cm.bestwave.ui.review;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ReviewViewModel extends ViewModel {

    private MutableLiveData<String> mTextInsertReview;
    private MutableLiveData<String> mTextLocation;
    private MutableLiveData<String> mTextRatingBar;
    private MutableLiveData<String> mTextTakeAPicture;
    private MutableLiveData<String> mTextWriteDescription;


    public ReviewViewModel() {
        mTextInsertReview = new MutableLiveData<>();
        mTextInsertReview.setValue("Insert your review");
        mTextLocation = new MutableLiveData<>();
        mTextLocation.setValue("Location");
        mTextRatingBar = new MutableLiveData<>();
        mTextRatingBar.setValue("Rating Bar");
        mTextTakeAPicture = new MutableLiveData<>();
        mTextTakeAPicture.setValue("Take a Picture");
        mTextWriteDescription =new MutableLiveData<>();
        mTextWriteDescription.setValue("Write a Description");
    }

    public LiveData<String> getTextInsertReview() {
        return mTextInsertReview;
    }
    public LiveData<String> getTextLocation() {
        return mTextLocation;
    }
    public LiveData<String> getTextRatingBar() {
        return mTextRatingBar;
    }
    public LiveData<String> getTextTakeAPicture() {
        return mTextTakeAPicture;
    }
    public LiveData<String> getTextWriteDescription() {
        return mTextWriteDescription;
    }
}