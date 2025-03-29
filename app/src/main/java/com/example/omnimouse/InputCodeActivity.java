package com.example.omnimouse;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class InputCodeActivity extends MainActivity {

    private EditText codeInput;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_code);

        codeInput = findViewById(R.id.codeInput);
        Button submitButton = findViewById(R.id.submitButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = codeInput.getText().toString();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("user_code", code);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
