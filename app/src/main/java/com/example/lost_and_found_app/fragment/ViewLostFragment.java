package com.example.lost_and_found_app.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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

public class ViewLostFragment extends Fragment {

    private final List<PostCard> fullPostList = new ArrayList<>();
    private final List<PostCard> filteredList = new ArrayList<>();
    private PostCardAdapter adapter;
    private PostCardRepository postCardRepository;

    public ViewLostFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        showProgressBar();
        return inflater.inflate(R.layout.fragment_view_lost, container, false);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        postCardRepository = new PostCardRepository();

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
        postCardRepository.getAllPosts("lostitems", new IApiCallback<>() {
            @Override
            public void onSuccess(List<PostCard> postCards) {
                fullPostList.clear();
                fullPostList.addAll(postCards);
                filteredList.clear();
                filteredList.addAll(fullPostList);
                filteredList.sort((p1, p2) -> {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
                        Date d1 = sdf.parse(p1.getDate());
                        Date d2 = sdf.parse(p2.getDate());
                        return d2.compareTo(d1);
                    } catch (ParseException e) {
                        return 0;
                    }
                });
                adapter.setPostCards(filteredList);
                hideProgressBar();
            }

            @Override
            public void onError(String errorMessage) {
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
    }

    private void filterByDate(String type) {
        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();

        switch (type) {
            case "today":
                startCal.set(Calendar.HOUR_OF_DAY, 0);
                startCal.set(Calendar.MINUTE, 0);
                startCal.set(Calendar.SECOND, 0);
                startCal.set(Calendar.MILLISECOND, 0);
                endCal.set(Calendar.HOUR_OF_DAY, 23);
                endCal.set(Calendar.MINUTE, 59);
                endCal.set(Calendar.SECOND, 59);
                endCal.set(Calendar.MILLISECOND, 999);
                break;
            case "week":
                startCal.set(Calendar.DAY_OF_WEEK, startCal.getFirstDayOfWeek());
                startCal.set(Calendar.HOUR_OF_DAY, 0);
                startCal.set(Calendar.MINUTE, 0);
                startCal.set(Calendar.SECOND, 0);
                startCal.set(Calendar.MILLISECOND, 0);
                endCal.set(Calendar.DAY_OF_WEEK, startCal.getFirstDayOfWeek() + 6);
                endCal.set(Calendar.HOUR_OF_DAY, 23);
                endCal.set(Calendar.MINUTE, 59);
                endCal.set(Calendar.SECOND, 59);
                endCal.set(Calendar.MILLISECOND, 999);
                break;
            case "month":
                startCal.set(Calendar.DAY_OF_MONTH, 1);
                startCal.set(Calendar.HOUR_OF_DAY, 0);
                startCal.set(Calendar.MINUTE, 0);
                startCal.set(Calendar.SECOND, 0);
                startCal.set(Calendar.MILLISECOND, 0);
                endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH));
                endCal.set(Calendar.HOUR_OF_DAY, 23);
                endCal.set(Calendar.MINUTE, 59);
                endCal.set(Calendar.SECOND, 59);
                endCal.set(Calendar.MILLISECOND, 999);
                break;
            case "year":
                startCal.set(Calendar.MONTH, Calendar.JANUARY);
                startCal.set(Calendar.DAY_OF_MONTH, 1);
                startCal.set(Calendar.HOUR_OF_DAY, 0);
                startCal.set(Calendar.MINUTE, 0);
                startCal.set(Calendar.SECOND, 0);
                startCal.set(Calendar.MILLISECOND, 0);
                endCal.set(Calendar.MONTH, Calendar.DECEMBER);
                endCal.set(Calendar.DAY_OF_MONTH, 31);
                endCal.set(Calendar.HOUR_OF_DAY, 23);
                endCal.set(Calendar.MINUTE, 59);
                endCal.set(Calendar.SECOND, 59);
                endCal.set(Calendar.MILLISECOND, 999);
                break;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("d MMM, yyyy", Locale.getDefault());
        String startDate = sdf.format(startCal.getTime());
        String endDate = sdf.format(endCal.getTime());

        postCardRepository.getCardsByDates("lostitems", "date", startDate, endDate, new IApiCallback<>() {
            @Override
            public void onSuccess(List<PostCard> result) {
                filteredList.clear();
                filteredList.addAll(result);
                filteredList.sort((p1, p2) -> {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
                        Date d1 = sdf.parse(p1.getDate());
                        Date d2 = sdf.parse(p2.getDate());
                        return d2.compareTo(d1);
                    } catch (ParseException e) {
                        return 0;
                    }
                });
                adapter.setPostCards(filteredList);
            }

            @Override
            public void onError(String errorMessage) {
            }
        });
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
