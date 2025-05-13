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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.lost_and_found_app.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

public class PostFoundFragment extends Fragment {

    private static final int IMAGE_PICK_CODE = 1000;
    private ImageView imageView;
    private EditText inputTitle, inputInformation, inputEmail, inputPhone;
    private TextView tvSelectedDate, tvSelectedTime;
    private MaterialButton btnCreate;
    private CheckBox checkBoxPolicy;
    private Uri imageUri;

    public PostFoundFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_found, container, false);

        imageView = view.findViewById(R.id.inputImage);
        inputTitle = view.findViewById(R.id.inputTitle);
        inputInformation = view.findViewById(R.id.inputInformation);
        inputEmail = view.findViewById(R.id.editText);
        inputPhone = view.findViewById(R.id.editPhone);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        tvSelectedTime = view.findViewById(R.id.tvSelectedTime);
        btnCreate = view.findViewById(R.id.btnCreate);
        checkBoxPolicy = view.findViewById(R.id.checkBoxPolicy);

        checkBoxPolicy.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnCreate.setEnabled(isChecked);
            btnCreate.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(),
                    isChecked ? R.color.blue : android.R.color.darker_gray));
        });

        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, IMAGE_PICK_CODE);
        });

        tvSelectedDate.setOnClickListener(v -> showDatePicker());
        tvSelectedTime.setOnClickListener(v -> showTimePicker());
        btnCreate.setOnClickListener(v -> submitPost());

        return view;
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String date = dayOfMonth + " " + (month + 1) + "," + year;
                    if ((month + 1) == 1) {
                        date = dayOfMonth + " Jan, " + year;
                    } else if ((month + 1) == 2) {
                        date = dayOfMonth + " Feb, " + year;
                    } else if ((month + 1) == 3) {
                        date = dayOfMonth + " Mar, " + year;
                    } else if ((month + 1) == 4) {
                        date = dayOfMonth + " Apr, " + year;
                    } else if ((month + 1) == 5) {
                        date = dayOfMonth + " May, " + year;
                    } else if ((month + 1) == 6) {
                        date = dayOfMonth + " Jun, " + year;
                    } else if ((month + 1) == 7) {
                        date = dayOfMonth + " Jul, " + year;
                    } else if ((month + 1) == 8) {
                        date = dayOfMonth + " Aug, " + year;
                    } else if ((month + 1) == 9) {
                        date = dayOfMonth + " Sep, " + year;
                    } else if ((month + 1) == 10) {
                        date = dayOfMonth + " Oct, " + year;
                    } else if ((month + 1) == 11) {
                        date = dayOfMonth + " Nov, " + year;
                    } else if ((month + 1) == 12) {
                        date = dayOfMonth + " Dec, " + year;
                    }
                    tvSelectedDate.setText(date);
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
                    tvSelectedTime.setText(time);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        timePickerDialog.show();
    }

    private void submitPost() {
        String title = inputTitle.getText().toString().trim();
        String info = inputInformation.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();
        String date = tvSelectedDate.getText().toString().trim();
        String time = tvSelectedTime.getText().toString().trim();

        if (title.isEmpty() || info.isEmpty() || email.isEmpty() || phone.isEmpty() ||
                date.equals("Select Date") || time.equals("Select Time") || imageUri == null) {
            Toast.makeText(getContext(), "Please fill in all fields and select an image.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getContext(), "Uploading...", Toast.LENGTH_SHORT).show();

        String filename = UUID.randomUUID().toString() + ".jpg";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("foundimages/" + filename);

        storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                uploadDataToDatabase(title, info, email, phone, date, time, uri.toString());
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void uploadDataToDatabase(String title, String info, String email, String phone,
                                      String date, String time, String imageUrl) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (user != null) ? user.getUid() : "anonymous";
        String postId = FirebaseDatabase.getInstance().getReference().push().getKey();

        HashMap<String, Object> postData = new HashMap<>();
        postData.put("title", title);
        postData.put("information", info);
        postData.put("email", email);
        postData.put("phone", phone);
        postData.put("date", date);
        postData.put("time", time);
        postData.put("imageUrl", imageUrl);
        postData.put("userId", userId);
        postData.put("postId", postId);
        postData.put("status", "Not Returned");
        postData.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime()));

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("founditems");
        dbRef.child(postId).setValue(postData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Post uploaded successfully", Toast.LENGTH_SHORT).show();
                clearFields();
            } else {
                Toast.makeText(getContext(), "Failed to post data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearFields() {
        inputTitle.setText("");
        inputInformation.setText("");
        inputEmail.setText("");
        inputPhone.setText("");
        tvSelectedDate.setText("Select Date");
        tvSelectedTime.setText("Select Time");
        imageView.setImageResource(R.drawable.bg_input_image);
        imageView.setBackgroundResource(R.drawable.bg_input_image);
        imageUri = null;
        View overlay = getView().findViewById(R.id.selectImageOverlay);
        if (overlay != null) {
            overlay.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
            imageView.setBackground(null);
            View overlay = getView().findViewById(R.id.selectImageOverlay);
            if (overlay != null) {
                overlay.setVisibility(View.GONE);
            }
            Toast.makeText(getContext(), "Image selected", Toast.LENGTH_SHORT).show();
        }
    }
}
