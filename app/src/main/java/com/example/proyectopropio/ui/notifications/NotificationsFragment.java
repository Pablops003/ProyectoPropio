package com.example.proyectopropio.ui.notifications;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.proyectopropio.R;
import com.example.proyectopropio.databinding.FragmentNotificationsBinding;
import com.example.proyectopropio.ui.Incidencia;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private FirebaseAuth auth;
    private DatabaseReference incidencias;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // configuracionn del mapa
        Context ctx = requireActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        binding.map.setTileSource(TileSourceFactory.MAPNIK);
        binding.map.setMultiTouchControls(true);
        IMapController mapController = binding.map.getController();
        mapController.setZoom(14.5);

        // Coordenadas de la Vall
        GeoPoint vall = new GeoPoint(39.8233, -0.232562);
        mapController.setCenter(vall);

        // Marcador para la Vall
        Marker startMarker = new Marker(binding.map);
        startMarker.setPosition(vall);
        startMarker.setTitle("la vall");
        startMarker.setIcon(requireContext().getDrawable(R.drawable.ic_home_black_24dp));
        binding.map.getOverlays().add(startMarker);

        // Localización del usuario
        MyLocationNewOverlay myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), binding.map);
        myLocationOverlay.enableMyLocation();
        binding.map.getOverlays().add(myLocationOverlay);

        // Brújula
        CompassOverlay compassOverlay = new CompassOverlay(requireContext(), binding.map);
        compassOverlay.enableCompass();
        binding.map.getOverlays().add(compassOverlay);

        // Autenticación de Firebase
        auth = FirebaseAuth.getInstance();
        DatabaseReference base = FirebaseDatabase.getInstance("https://proyectopropio-fd31a-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        DatabaseReference users = base.child("users");
        DatabaseReference uid = users.child(auth.getUid());
        incidencias = uid.child("incidencies");

        // Listener para añadir los marcadores de las incidencias
        incidencias.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                if (binding == null || binding.map == null) {
                    Log.e("Pablo", "no está visible.");
                    return;
                }

                // Obtener la incidencia y las coordenadas
                Incidencia incidencia = snapshot.getValue(Incidencia.class);
                Double latitud = snapshot.child("latitud").getValue(Double.class);
                Double longitud = snapshot.child("longitud").getValue(Double.class);
                Log.d("O", latitud + " " + longitud);

                // Verificar que la incidencia no es nula
                if (incidencia != null) {
                    GeoPoint location = new GeoPoint(latitud, longitud);

                    // Calcular distancia y tiempo
                    double distancia = calcularDistancia(vall, location);
                    double tiempoEstimado = calcularTiempo(distancia); // Tiempo en minutos

                    // Crear y agregar el marcador
                    Marker marker = new Marker(binding.map);
                    marker.setPosition(location);
                    marker.setTitle(incidencia.getProblema() + "\n" +
                            String.format("%.2f", distancia) + " metros\n" +
                            String.format("%.2f", tiempoEstimado) + " minutos");

                    binding.map.getOverlays().add(marker);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        return root;
    }

    // Método para calcular la distancia entre dos puntos
    private double calcularDistancia(GeoPoint punto1, GeoPoint punto2) {
        if (punto1 != null && punto2 != null) {
            return punto1.distanceToAsDouble(punto2); // Retorna la distancia en metros
        }
        return -1;
    }

    // Método para calcular el tiempo de viaje
    private double calcularTiempo(double distancia) {
        double velocidad = 50; // Velocidad en km/h
        double velocidadEnMetrosPorSegundo = velocidad * 1000 / 3600; // Convertimos km/h a m/s

        // Y los 3600 son los segundo que hay en una hora y mil son los metros que hay en un km

        // Calcular el tiempo en segundos
        double tiempoEnSegundos = distancia / velocidadEnMetrosPorSegundo;

        // Convertimos el tiempo a minutos
        double tiempoEnMinutos = tiempoEnSegundos / 60;

        return tiempoEnMinutos;
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.map.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
