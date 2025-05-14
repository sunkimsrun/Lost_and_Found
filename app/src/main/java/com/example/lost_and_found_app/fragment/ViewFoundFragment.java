package com.example.lost_and_found_app.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lost_and_found_app.HomeActivity;
import com.example.lost_and_found_app.R;
import com.example.lost_and_found_app.adapter.PostCardAdapter;
import com.example.lost_and_found_app.model.PostCard;
import com.example.lost_and_found_app.repository.IApiCallback;
import com.example.lost_and_found_app.repository.PostCardRepository;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ViewFoundFragment extends Fragment {

    private final List<PostCard> fullPostList = new ArrayList<>();

    private PostCardAdapter adapter;
    private final List<PostCard> filteredList = new ArrayList<>();
    HomeActivity homeActivity;

    public ViewFoundFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        showProgressBar();
        return inflater.inflate(R.layout.fragment_view_found, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.rcv_list_post);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser != null ? currentUser.getUid() : null;

        adapter = new PostCardAdapter(currentUserId);
        recyclerView.setAdapter(adapter);

        EditText editText = view.findViewById(R.id.editText);
        ChipGroup chipGroup = view.findViewById(R.id.filterChipGroup);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBySearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) return;

            if (checkedId == R.id.chip_clear_filter) {
                filteredList.clear();
                filteredList.addAll(fullPostList);
                adapter.setPostCards(filteredList);
                Toast.makeText(getContext(), "Filter cleared. Showing all items.", Toast.LENGTH_SHORT).show();
            } else if (checkedId == R.id.chip_today) {
                filterByDate("today");
            } else if (checkedId == R.id.chip_this_week) {
                filterByDate("week");
            } else if (checkedId == R.id.chip_this_month) {
                filterByDate("month");
            } else if (checkedId == R.id.chip_this_year) {
                filterByDate("year");
            }
        });

        loadDataFromRepository();
    }

    private void loadDataFromRepository() {
        showProgressBar();
        PostCardRepository repository = new PostCardRepository();

        repository.getAllPosts("founditems", new IApiCallback<>() {
            @Override
            public void onSuccess(List<PostCard> postCards) {
                fullPostList.clear();
                fullPostList.addAll(postCards);
                filteredList.clear();
                filteredList.addAll(postCards);
                adapter.setPostCards(filteredList);
                hideProgressBar();
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("ViewFoundFragment", "Error loading found items: " + errorMessage);
                hideProgressBar();
            }
        });
    }

    private void filterBySearch(String query) {
        List<PostCard> tempList = new ArrayList<>();
        for (PostCard post : fullPostList) {
            if (post.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    post.getInformation().toLowerCase().contains(query.toLowerCase())) {
                tempList.add(post);
            }
        }
        filteredList.clear();
        filteredList.addAll(tempList);
        adapter.setPostCards(filteredList);
        Toast.makeText(getContext(), "Search: Found " + tempList.size() + " result(s)", Toast.LENGTH_SHORT).show();
    }

    private void filterByDate(String type) {
        List<PostCard> tempList = new ArrayList<>();
        Calendar now = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("d MMM, yyyy", Locale.getDefault());

        for (PostCard post : fullPostList) {
            try {
                Date postDate = sdf.parse(post.getDate());
                if (postDate == null) continue;
                Calendar postCal = Calendar.getInstance();
                postCal.setTime(postDate);
                boolean include = false;

                switch (type) {
                    case "today":
                        include = postCal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                                postCal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR);
                        break;
                    case "week":
                        include = postCal.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR) &&
                                postCal.get(Calendar.YEAR) == now.get(Calendar.YEAR);
                        break;
                    case "month":
                        include = postCal.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
                                postCal.get(Calendar.YEAR) == now.get(Calendar.YEAR);
                        break;
                    case "year":
                        include = postCal.get(Calendar.YEAR) == now.get(Calendar.YEAR);
                        break;
                }

                if (include) {
                    tempList.add(post);
                }
            } catch (ParseException e) {
                Log.e("DateParse", "Failed to parse date: " + post.getDate());
            }
        }

        filteredList.clear();
        filteredList.addAll(tempList);
        adapter.setPostCards(filteredList);
        Toast.makeText(getContext(), "Filtered by " + type + ": " + tempList.size() + " item(s)", Toast.LENGTH_SHORT).show();
    }

    private void showProgressBar() {
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).showProgressBar();
        }
    }

    private void hideProgressBar() {
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).hideProgressBar();
        }
    }
}
