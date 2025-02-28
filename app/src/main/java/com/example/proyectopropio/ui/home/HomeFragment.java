package com.example.proyectopropio.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import com.example.proyectopropio.R;
import com.example.proyectopropio.databinding.FragmentHomeBinding;
import com.example.proyectopropio.ui.Club;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private FirebaseUser authUser;
    String mCurrentPhotoPath;
    private Uri photoURI;
    private ImageView foto;
    static final int REQUEST_TAKE_PHOTO = 1;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        HomeViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        HomeViewModel.getCurrentAddress().observe(getViewLifecycleOwner(), address -> {
            binding.txtDireccio.setText(String.format(
                    "Direcció: %1$s \n Hora: %2$tr",
                    address, System.currentTimeMillis())
            );
        });


        sharedViewModel.getProgressBar().observe(getViewLifecycleOwner(), visible -> {
            if (visible)
                binding.loading.setVisibility(ProgressBar.VISIBLE);
            else
                binding.loading.setVisibility(ProgressBar.INVISIBLE);
        });

        sharedViewModel.switchTrackingLocation();

        sharedViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            authUser = user;
        });


        binding.buttonNotificar.setOnClickListener(button -> {
            Club club = new Club();
            club.setDireccio(binding.txtDireccio.getText().toString());
            double latitud = Double.parseDouble(binding.txtLatitud.getText().toString().trim());
            double longitud = Double.parseDouble(binding.txtLongitud.getText().toString().trim());
            club.setProblema(binding.txtDescripcio.getText().toString());
            club.setCarles("hola carles");


            club.setLatitud(latitud);
            club.setLongitud(longitud);

            DatabaseReference base = FirebaseDatabase.getInstance("https://proyectopropio-fd31a-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

            DatabaseReference users = base.child("users");
            DatabaseReference uid = users.child(authUser.getUid());
            DatabaseReference incidencies = uid.child("incidencies");

            DatabaseReference reference = incidencies.push();
            reference.setValue(club);
        });

        foto = root.findViewById(R.id.foto);
        Button buttonFoto = root.findViewById(R.id.button_foto);

        buttonFoto.setOnClickListener(button -> {
            dispatchTakePictureIntent();
        });


        return root;
    }

    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(
                getContext().getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }

            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(getContext(),
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                Glide.with(this).load(photoURI).into(foto);
            } else {
                Toast.makeText(getContext(),
                        "foto!", Toast.LENGTH_SHORT).show();
            }
        }
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding=null;
}
}