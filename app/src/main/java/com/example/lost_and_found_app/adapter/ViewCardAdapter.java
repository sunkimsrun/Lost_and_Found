package com.example.lost_and_found_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.lost_and_found_app.R;
import com.example.lost_and_found_app.model.ViewCardData;

import java.util.List;

public class ViewCardAdapter extends RecyclerView.Adapter<ViewCardAdapter.CardViewHolder> {

    private final List<ViewCardData> cardList;
    private OnCardClickListener listener;

    public ViewCardAdapter(List<ViewCardData> cardList) {
        this.cardList = cardList;
    }

    public void setOnCardClickListener(OnCardClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        ViewCardData card = cardList.get(position);
        holder.titleText.setText(card.getTitle());
        holder.subText.setText(card.getSubText());
        holder.lottieAnimation.setAnimation(card.getLottieRes());

        if (card.getTitle().equalsIgnoreCase("Found Items")) {
            holder.cardContainer.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.bg_btn_home_view));
        }

        holder.goButton.setOnClickListener(v -> {
            if (listener != null) listener.onCardClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    public interface OnCardClickListener {
        void onCardClick(int position);
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, subText;
        LottieAnimationView lottieAnimation;
        Button goButton;

        LinearLayout cardContainer;

        public CardViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            subText = itemView.findViewById(R.id.subText);
            lottieAnimation = itemView.findViewById(R.id.lottieAnimation);
            goButton = itemView.findViewById(R.id.goButton);
            cardContainer = itemView.findViewById(R.id.cardContainer);
        }
    }
}
