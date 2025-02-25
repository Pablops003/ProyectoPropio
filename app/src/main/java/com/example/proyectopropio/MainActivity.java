package com.example.proyectopropio;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.proyectopropio.ui.home.HomeViewModel;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.proyectopropio.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ActivityResultLauncher<String[]> locationPermissionRequest;
    private HomeViewModel homeViewModel;
    private ActivityResultLauncher<Intent> signInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        homeViewModel.setFusedLocationClient(mFusedLocationClient);
        homeViewModel.getCheckPermission().observe(this, s -> checkPermission());

        // Register the location permission request launcher
        locationPermissionRequest = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
            Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
            if (fineLocationGranted != null && fineLocationGranted) {
                homeViewModel.startTrackingLocation(false);
            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                homeViewModel.startTrackingLocation(false);
            } else {
                Toast.makeText(this, "No permisos concedidos", Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize the sign-in launcher for Firebase Auth
        signInLauncher = registerForActivityResult(
                new FirebaseAuthUIActivityResultContract(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            homeViewModel.setUser(user);
                            Log.d("AUTH", "Usuario autenticado: " + user.getEmail());
                        } else {
                            Log.e("AUTH", "Usuario es nulo después del inicio de sesión.");
                        }
                    } else {
                        Log.e("AUTH", "Error en el inicio de sesión: código de resultado " + result.getResultCode());
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        Log.d("AUTH", "Estado de autenticación: " + String.valueOf(auth.getCurrentUser()));

        if (auth.getCurrentUser() == null) {
            // Si no hay usuario autenticado, iniciar el flujo de inicio de sesión
            Log.d("AUTH", "El usuario no está autenticado, iniciando sesión...");
            Intent signInIntent =
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setAvailableProviders(
                                    Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build()
                                    )
                            )
                            .build();
            signInLauncher.launch(signInIntent);
        } else {
            // Si ya hay un usuario autenticado, establecer el usuario en el ViewModel
            Log.d("AUTH", "Usuario ya autenticado: " + auth.getCurrentUser().getEmail());
            homeViewModel.setUser(auth.getCurrentUser());
        }
    }

    void checkPermission() {
        Log.d("PERMISSIONS", "Comprobando permisos...");
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Log.d("PERMISSIONS", "Solicitando permisos...");
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        } else {
            // Si los permisos ya están concedidos, comienza a rastrear la ubicación
            homeViewModel.startTrackingLocation(false);
        }
    }
}
