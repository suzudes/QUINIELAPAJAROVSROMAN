package com.example.quinielapajarovsroman;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainActivity extends AppCompatActivity {

    private MatchViewModel viewModel;
    private MatchAdapter matchAdapter;
    private UserAdapter userAdapter;
    private RecyclerView recyclerView;
    private TextView titleText, dateText;
    private SwipeRefreshLayout swipeRefresh;
    private SessionManager sessionManager; // Mantener para compatibilidad por ahora
    private TokenManager tokenManager;
    private int currentTab = R.id.nav_today;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        tokenManager = new TokenManager(this);
        String savedToken = tokenManager.getToken();

        if (savedToken == null) {
            startActivity(new android.content.Intent(this, SettingsActivity.class));
            finish();
            return;
        }

        // Intentar login automático en cada inicio para asegurar cookie fresca
        ApiClient.getService(this).login(savedToken).enqueue(new retrofit2.Callback<AuthResponse>() {
            @Override
            public void onResponse(retrofit2.Call<AuthResponse> call, retrofit2.Response<AuthResponse> response) {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "ERROR DE SESIÓN. Revisa tu token.", Toast.LENGTH_LONG).show());
                }
            }
            @Override
            public void onFailure(retrofit2.Call<AuthResponse> call, Throwable t) {}
        });

        setContentView(R.layout.activity_main);
        initViews();
        setupViewModel();
        setupNavigation();
        viewModel.refresh();
    }

    private void initViews() {
        titleText = findViewById(R.id.titleText);
        dateText = findViewById(R.id.dateText);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        recyclerView = findViewById(R.id.matchRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        matchAdapter = new MatchAdapter();
        matchAdapter.setListener((match, home, away) -> {
            viewModel.savePrediction(match.id, home, away, new MatchRepository.OnSaveListener() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "PREDICCIÓN BLOQUEADA EN NUBE", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onError(int code) {
                    runOnUiThread(() -> {
                        String msg = "ERROR " + code + ": ";
                        if (code == 401) msg += "SESIÓN EXPIRADA. Re-identifícate.";
                        else if (code == 403) msg += "VENTANA CERRADA (T-10)";
                        else if (code == 404) msg += "PARTIDO NO ENCONTRADO EN NUBE";
                        else if (code == 409) msg += "YA GUARDADO";
                        else if (code == -1) msg += "SIN CONEXIÓN AL SERVIDOR";
                        else msg += "INTENTA DE NUEVO";

                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                    });
                }
            });
        });

        recyclerView.setAdapter(matchAdapter);
        swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MatchViewModel.class);
        observeData();
    }

    private void observeData() {
        viewModel.getActiveMatches().observe(this, matches -> {
            if (currentTab == R.id.nav_today) {
                recyclerView.setAdapter(matchAdapter);
                matchAdapter.setMatches(matches);
                swipeRefresh.setRefreshing(false);
                updateTimestamp();
            }
        });

        viewModel.getClosedMatches().observe(this, closedList -> {
            if (currentTab == R.id.nav_closed) {
                recyclerView.setAdapter(matchAdapter);
                matchAdapter.setMatches(closedList);
                swipeRefresh.setRefreshing(false);
            }
        });

        viewModel.getStandings().observe(this, standings -> {
            if (currentTab == R.id.nav_standings) {
                userAdapter = new UserAdapter(standings);
                recyclerView.setAdapter(userAdapter);
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            currentTab = item.getItemId();
            if (currentTab == R.id.nav_today) {
                titleText.setText("PARTIDOS ACTIVOS");
                recyclerView.setAdapter(matchAdapter);
                if (viewModel.getActiveMatches().getValue() != null) {
                    matchAdapter.setMatches(viewModel.getActiveMatches().getValue());
                }
            } else if (currentTab == R.id.nav_closed) {
                titleText.setText("RESULTADOS FINALES");
                recyclerView.setAdapter(matchAdapter);
                if (viewModel.getClosedMatches().getValue() != null) {
                    matchAdapter.setMatches(viewModel.getClosedMatches().getValue());
                }
            } else if (currentTab == R.id.nav_standings) {
                titleText.setText("TABLA DE POSICIONES");
                if (viewModel.getStandings().getValue() != null) {
                    userAdapter = new UserAdapter(viewModel.getStandings().getValue());
                    recyclerView.setAdapter(userAdapter);
                }
            }
            return true;
        });
    }

    private void updateTimestamp() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        dateText.setText("SINCRO NUBE: " + now);
    }
}
