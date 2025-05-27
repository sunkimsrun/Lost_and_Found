package com.example.lost_and_found_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;


public class NoConnectionActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_connection);

        Button btnRetry = findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(v -> {
            if (no_internet.isConnected(this)) {
                Intent intent = new Intent(NoConnectionActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (no_internet.isConnected(this)) {
            finish();
        }
    }

}
