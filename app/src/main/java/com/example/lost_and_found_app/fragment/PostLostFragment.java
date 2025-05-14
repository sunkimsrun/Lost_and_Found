package com.example.lost_and_found_app.fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.lost_and_found_app.HomeActivity;
import com.example.lost_and_found_app.R;
import com.example.lost_and_found_app.databinding.FragmentPostLostBinding;
import com.example.lost_and_found_app.model.PostCard;
import com.example.lost_and_found_app.repository.IApiCallback;
import com.example.lost_and_found_app.repository.PostCardRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class PostLostFragment extends Fragment {

    private static final int IMAGE_PICK_CODE = 1000;
    HomeActivity homeActivity;
    private FragmentPostLostBinding binding;
    private Uri imageUri;

    public PostLostFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPostLostBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        homeActivity = (HomeActivity) getActivity();

        binding.checkBoxPolicy.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.btnCreate.setEnabled(isChecked);
            binding.btnCreate.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(),
                    isChecked ? R.color.blue : android.R.color.darker_gray));
        });

        binding.inputImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, IMAGE_PICK_CODE);
        });

        binding.tvSelectedDate.setOnClickListener(v -> showDatePicker());
        binding.tvSelectedTime.setOnClickListener(v -> showTimePicker());
        binding.btnCreate.setOnClickListener(v -> submitPost());

        return view;
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                    String date = dayOfMonth + " " + months[month] + ", " + year;
                    binding.tvSelectedDate.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                (view, hourOfDay, minute) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    binding.tvSelectedTime.setText(time);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        timePickerDialog.show();
    }

    private void submitPost() {
        String title = binding.inputTitle.getText().toString().trim();
        String info = binding.inputInformation.getText().toString().trim();
        String email = binding.editText.getText().toString().trim();
        String phone = binding.editPhone.getText().toString().trim();
        String date = binding.tvSelectedDate.getText().toString().trim();
        String time = binding.tvSelectedTime.getText().toString().trim();
        String rewardInput = binding.inputReward.getText().toString().trim();
        String reward = rewardInput.isEmpty() || rewardInput.equals(" ") ? null : rewardInput;

        if (title.isEmpty() || info.isEmpty() || email.isEmpty() || phone.isEmpty() ||
                date.equals("Select Date") || time.equals("Select Time") || imageUri == null) {
            Toast.makeText(getContext(), "Please fill in all fields and select an image.", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgressBar();

        String filename = UUID.randomUUID().toString() + ".jpg";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("lostimages/" + filename);

        storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                uploadDataToDatabase(title, info, email, phone, date, time, reward, uri.toString());
            });
        }).addOnFailureListener(e -> {
            hideProgressBar();
            Toast.makeText(getContext(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void uploadDataToDatabase(String title, String info, String email, String phone,
                                      String date, String time, String reward, String imageUrl) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (user != null) ? user.getUid() : "anonymous";
        String postId = FirebaseDatabase.getInstance().getReference().push().getKey();
        if (postId == null) postId = UUID.randomUUID().toString();

        PostCard postCard = new PostCard();
        postCard.setTitle(title);
        postCard.setInformation(info);
        postCard.setEmail(email);
        postCard.setPhone(phone);
        postCard.setDate(date);
        postCard.setPostTime(time);
        postCard.setImageUrl(imageUrl);
        postCard.setUserId(userId);
        postCard.setPostId(postId);
        postCard.setStatus("Not Found");

        if (reward != null && !reward.isEmpty()) {
            postCard.setReward(reward);
        } else {
            postCard.setReward(null);
        }

        String createdDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
        postCard.setCreatedDate(createdDate);

        PostCardRepository postCardRepository = new PostCardRepository();
        postCardRepository.createPost("lostitems", postId, postCard, new IApiCallback<PostCard>() {
            @Override
            public void onSuccess(PostCard result) {
                hideProgressBar();
                Toast.makeText(getContext(), "Post uploaded successfully", Toast.LENGTH_SHORT).show();
                clearFields();
            }

            @Override
            public void onError(String errorMessage) {
                hideProgressBar();
                Toast.makeText(getContext(), "Failed to post data: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearFields() {
        binding.inputTitle.setText("");
        binding.inputInformation.setText("");
        binding.editText.setText("");
        binding.editPhone.setText("");
        binding.tvSelectedDate.setText("Select Date");
        binding.tvSelectedTime.setText("Select Time");
        binding.inputImage.setImageResource(R.drawable.bg_input_image);
        binding.inputImage.setBackgroundResource(R.drawable.bg_input_image);
        imageUri = null;
        binding.selectImageOverlay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            binding.inputImage.setImageURI(imageUri);
            binding.inputImage.setBackground(null);
            binding.selectImageOverlay.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void showProgressBar() {
        if (homeActivity != null) {
            homeActivity.showProgressBar();
        }
    }

    private void hideProgressBar() {
        if (homeActivity != null) {
            homeActivity.hideProgressBar();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
