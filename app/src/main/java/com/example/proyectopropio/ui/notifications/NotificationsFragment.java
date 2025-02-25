package com.example.proyectopropio.ui.notifications;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


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

        // Configuración de OpenStreetMap
        Context ctx = requireActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        // Configurar el mapa
        binding.map.setTileSource(TileSourceFactory.MAPNIK);
        binding.map.setMultiTouchControls(true);
        IMapController mapController = binding.map.getController();
        mapController.setZoom(14.5);


        GeoPoint vall = new GeoPoint(39.8233, -0.232562);
        mapController.setCenter(vall);

        Marker startMarker = new Marker(binding.map);
        startMarker.setPosition(vall);
        startMarker.setTitle("vall");
        binding.map.getOverlays().add(startMarker);

        // Mostrar ubicación del usuario
        MyLocationNewOverlay myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), binding.map);
        myLocationOverlay.enableMyLocation();
        binding.map.getOverlays().add(myLocationOverlay);

        // Agregar brújula
        CompassOverlay compassOverlay = new CompassOverlay(requireContext(), binding.map);
        compassOverlay.enableCompass();
        binding.map.getOverlays().add(compassOverlay);

        // Conectar a Firebase
        auth = FirebaseAuth.getInstance();
        DatabaseReference base = FirebaseDatabase.getInstance().getReference();
        DatabaseReference users = base.child("users");
        DatabaseReference uid = users.child(auth.getUid());
        incidencias = uid.child("incidencias");

        // Cargar marcadores de Firebase
        incidencias.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Incidencia incidencia = snapshot.getValue(Incidencia.class);
                if (incidencia != null) {
                    GeoPoint location = new GeoPoint(
                            Double.parseDouble(incidencia.getLatitud()),
                            Double.parseDouble(incidencia.getLongitud())
                    );

                    Marker marker = new Marker(binding.map);
                    marker.setPosition(location);
                    marker.setTitle(incidencia.getProblema());
                    marker.setSnippet(incidencia.getDireccio());

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