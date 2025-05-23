package com.example.lost_and_found_app.fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.lost_and_found_app.HomeActivity;
import com.example.lost_and_found_app.R;
import com.example.lost_and_found_app.databinding.FragmentPostFoundBinding;
import com.example.lost_and_found_app.model.PostCard;
import com.example.lost_and_found_app.repository.IApiCallback;
import com.example.lost_and_found_app.repository.PostCardRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class PostFoundFragment extends Fragment {

    private static final int IMAGE_PICK_CODE = 1000;
    private final PostCardRepository postCardRepository = new PostCardRepository();
    private Uri imageUri;
    private HomeActivity homeActivity;
    private FragmentPostFoundBinding binding;
    private final boolean isEditing = false;

    public PostFoundFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPostFoundBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        homeActivity = (HomeActivity) getActivity();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String email = currentUser.getEmail();
            binding.editText.setText(email);
        }

        homeActivity = (HomeActivity) getActivity();

        binding.editPhone.setText("+855 ");
        binding.editPhone.setSelection(binding.editPhone.getText().length());

        binding.editPhone.addTextChangedListener(new TextWatcher() {
            boolean isFormatting;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String input = s.toString();

                if (!input.startsWith("+855 ")) {
                    binding.editPhone.setText("+855 ");
                    binding.editPhone.setSelection(binding.editPhone.getText().length());
                } else if (input.length() > 6) {
                    char firstDigit = input.charAt(6);
                    if (firstDigit == '0') {
                        Toast.makeText(requireContext(), "First number after +855 cannot be 0", Toast.LENGTH_SHORT).show();
                        binding.editPhone.setText("+855 ");
                        binding.editPhone.setSelection(binding.editPhone.getText().length());
                    }
                }

                isFormatting = false;
            }
        });


        binding.checkBoxPolicy.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && binding.editPhone.getError() == null) {
                binding.btnCreate.setEnabled(true);
                binding.btnCreate.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.blue));
            } else {
                binding.btnCreate.setEnabled(false);
                binding.btnCreate.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), android.R.color.darker_gray));
            }
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
                    String formattedDay = String.format(Locale.getDefault(), "%02d", dayOfMonth);
                    String date = formattedDay + " " + months[month] + ", " + year;
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

        if (title.isEmpty() || info.isEmpty() || email.isEmpty() || phone.isEmpty() ||
                date.equals("Select Date") || time.equals("Select Time") || imageUri == null) {
            Toast.makeText(getContext(), "Please fill in all fields and select an image.", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgressBar();

        String filename = UUID.randomUUID().toString() + ".jpg";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("foundimages/" + filename);

        storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    uploadPostCardToDatabase(title, info, email, phone, date, time, uri.toString());
                }).addOnFailureListener(e -> {
                    hideProgressBar();
                    Toast.makeText(getContext(), "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
        ).addOnFailureListener(e -> {
            hideProgressBar();
            Toast.makeText(getContext(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void uploadPostCardToDatabase(String title, String info, String email, String phone,
                                          String date, String time, String imageUrl) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (user != null) ? user.getUid() : "anonymous";
        String postId = FirebaseDatabase.getInstance().getReference().push().getKey();
        String createdDate = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
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
        postCard.setStatus("Not Returned");
        postCard.setReward(null);
        postCard.setCreatedDate(createdDate);

        postCardRepository.createPost("founditems", postId, postCard, new IApiCallback<PostCard>() {
            @Override
            public void onSuccess(PostCard result) {
                hideProgressBar();
                Toast.makeText(getContext(), "Post uploaded successfully", Toast.LENGTH_SHORT).show();

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new SuccessfulFragment())
                        .addToBackStack(null)
                        .commit();

            }

            @Override
            public void onError(String errorMessage) {
                hideProgressBar();
                Toast.makeText(getContext(), "Error posting data: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearFields() {
        binding.inputTitle.setText("");
        binding.inputInformation.setText("");
        binding.editText.setText("");
        binding.editPhone.setText("+855 ");
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

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_PICK_CODE && data != null) {
                Uri sourceUri = data.getData();
                if (sourceUri != null) {
                    Uri destinationUri = Uri.fromFile(new File(requireContext().getCacheDir(), "cropped_" + UUID.randomUUID() + ".jpg"));

                    UCrop.Options options = new UCrop.Options();
                    options.setCircleDimmedLayer(false);
                    options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
                    options.setCompressionQuality(90);

                    UCrop.of(sourceUri, destinationUri)
                            .withAspectRatio(375, 240)
                            .withMaxResultSize(375, 240)
                            .withOptions(options)
                            .start(requireContext(), this);
                }
            } else if (requestCode == UCrop.REQUEST_CROP) {
                final Uri resultUri = UCrop.getOutput(data);
                if (resultUri != null) {
                    imageUri = resultUri;
                    binding.inputImage.setImageURI(imageUri);
                    binding.inputImage.setBackground(null);
                    binding.selectImageOverlay.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Image cropped and selected", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            if (cropError != null) {
                Toast.makeText(getContext(), "Crop error: " + cropError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
}
