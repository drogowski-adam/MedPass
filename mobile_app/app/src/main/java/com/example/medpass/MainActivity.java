package com.example.medpass;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private MedPassApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        api = ApiClient.get().create(MedPassApi.class);

        EditText etUsername = findViewById(R.id.etUsername);
        EditText etPassword = findViewById(R.id.etPassword);
        TextView tvStatus = findViewById(R.id.tvStatus);
        Button btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            LoginRequest req = new LoginRequest(username, password);

            api.login(req).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                    if (!response.isSuccessful() || response.body() == null) {
                        tvStatus.setText("Błąd serwera");
                        return;
                    }

                    LoginResponse res = response.body();

                    if (!res.ok) {
                        tvStatus.setText("Błędny login");
                        return;
                    }

                    tvStatus.setText("Zalogowano jako " + res.role);

                    Intent i;
                    if ("ADMIN".equals(res.role)) {
                        i = new Intent(MainActivity.this, AdminActivity.class);
                    } else {
                        i = new Intent(MainActivity.this, VerifyActivity.class);
                    }

                    i.putExtra("guard_code", username);
                    startActivity(i);
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    tvStatus.setText("Brak połączenia z backendem");
                }
            });
        });
    }
}