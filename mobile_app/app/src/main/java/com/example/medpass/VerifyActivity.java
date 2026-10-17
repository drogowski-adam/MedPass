package com.example.medpass;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyActivity extends AppCompatActivity {

    private MedPassApi api;
    private String guardCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        api = ApiClient.get().create(MedPassApi.class);

        guardCode = getIntent().getStringExtra("guard_code");

        EditText etDoc = findViewById(R.id.etDoc);
        Button btnVerify = findViewById(R.id.btnVerify);
        TextView tvDisease = findViewById(R.id.tvDisease);
        TextView tvItems = findViewById(R.id.tvItems);

        btnVerify.setOnClickListener(v -> {

            String doc = etDoc.getText().toString().trim();

            api.verify(doc, guardCode).enqueue(new Callback<VerifyResponse>() {
                @Override
                public void onResponse(Call<VerifyResponse> call, Response<VerifyResponse> response) {

                    if (!response.isSuccessful() || response.body() == null) {
                        tvDisease.setText("Błąd API");
                        return;
                    }

                    VerifyResponse res = response.body();

                    if (!res.found) {
                        tvDisease.setText("Brak danych medycznych");
                        tvItems.setText("");
                        return;
                    }

                    tvDisease.setText("Choroba: " + res.diseaseName);
                    tvItems.setText(String.join(", ", res.allowedItems));
                }

                @Override
                public void onFailure(Call<VerifyResponse> call, Throwable t) {
                    tvDisease.setText("Brak połączenia");
                }
            });
        });
    }
}