package com.example.medpass;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminActivity extends AppCompatActivity {

    private MedPassApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        api = ApiClient.get().create(MedPassApi.class);

        EditText etGuard = findViewById(R.id.etGuardLogin);
        TextView tvLogs = findViewById(R.id.tvLogs);
        Button btnLoad = findViewById(R.id.btnLoadLogs);

        btnLoad.setOnClickListener(v -> {
            String guard = etGuard.getText().toString().trim();

            api.getLogs(guard).enqueue(new Callback<List<AccessLogDto>>() {
                @Override
                public void onResponse(Call<List<AccessLogDto>> call, Response<List<AccessLogDto>> response) {

                    if (!response.isSuccessful() || response.body() == null) {
                        tvLogs.setText("Błąd pobierania logów");
                        return;
                    }

                    StringBuilder sb = new StringBuilder();

                    for (AccessLogDto log : response.body()) {
                        sb.append(log.date)
                                .append(" | ")
                                .append(log.action);

                        if (log.participantHash != null) {
                            sb.append(" | participant: ").append(log.participantHash);
                        }

                        sb.append("\n\n");
                    }

                    tvLogs.setText(sb.toString());
                }

                @Override
                public void onFailure(Call<List<AccessLogDto>> call, Throwable t) {
                    tvLogs.setText("Brak połączenia z backendem");
                }
            });
        });
    }
}