package com.example.note.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.note.R;

public class PinActivity extends AppCompatActivity {

    TextView tvTitle;
    LinearLayout tvPinDots; // dots ka layout
    View[] dotViews = new View[4]; // dots array

    String enteredPin = "";
    String firstPin = "";
    SharedPreferences prefs;

    // 0 = Set PIN, 1 = Confirm PIN, 2 = Enter PIN
    int currentState = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        tvTitle = findViewById(R.id.tvTitle);
        tvPinDots = findViewById(R.id.tvPinDots);

        // dots ko layout se fetch karna
        for (int i = 0; i < 4; i++) {
            dotViews[i] = tvPinDots.getChildAt(i);
        }

        prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

        if (prefs.contains("user_pin")) {
            currentState = 2;
            tvTitle.setText("Enter PIN");
        } else {
            currentState = 0;
            tvTitle.setText("Set PIN");
        }

        setupNumberButtons();
        updatePinDots(); // initially sab unfilled
    }

    private void setupNumberButtons() {
        int[] btnIds = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3,
                R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7,
                R.id.btn8, R.id.btn9};

        View.OnClickListener numberClickListener = v -> {
            if (enteredPin.length() < 4) {
                enteredPin += ((Button) v).getText().toString();
                updatePinDots();

                if (enteredPin.length() == 4) {
                    handlePinComplete();
                }
            }
        };

        for (int id : btnIds) {
            findViewById(id).setOnClickListener(numberClickListener);
        }

        findViewById(R.id.btnDelete).setOnClickListener(v -> {
            if (!enteredPin.isEmpty()) {
                enteredPin = enteredPin.substring(0, enteredPin.length() - 1);
                updatePinDots();
            }
        });
    }

    private void updatePinDots() {
        for (int i = 0; i < 4; i++) {
            if (i < enteredPin.length()) {
                dotViews[i].setBackgroundResource(R.drawable.pin_dot_filled); // filled dot
            } else {
                dotViews[i].setBackgroundResource(R.drawable.pin_dot_unfilled); // unfilled dot
            }
        }
    }

    private void handlePinComplete() {
        if (currentState == 0) {
            firstPin = enteredPin;
            enteredPin = "";
            currentState = 1;
            tvTitle.setText("Confirm your PIN");
            updatePinDots();
        } else if (currentState == 1) {
            if (enteredPin.equals(firstPin)) {
                prefs.edit().putString("user_pin", enteredPin).apply();
                Toast.makeText(this, "PIN set successfully!", Toast.LENGTH_SHORT).show();
                openHomePage();
            } else {
                Toast.makeText(this, "PINs do not match!", Toast.LENGTH_SHORT).show();
                enteredPin = "";
                updatePinDots();
            }
        } else if (currentState == 2) {
            String savedPin = prefs.getString("user_pin", "");
            if (enteredPin.equals(savedPin)) {
//                Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show();
                openHomePage();
            } else {
                Toast.makeText(this, "Incorrect PIN!", Toast.LENGTH_SHORT).show();
                enteredPin = "";
                updatePinDots();
            }
        }
    }

    private void openHomePage() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}
