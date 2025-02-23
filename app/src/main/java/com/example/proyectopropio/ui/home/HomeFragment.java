package com.example.proyectopropio.ui.home;


import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.proyectopropio.databinding.FragmentHomeBinding;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        HomeViewModel homeViewModel1 = new ViewModelProvider(getActivity()).get(HomeViewModel.class);

        HomeViewModel.getCurrentAddress().observe(getViewLifecycleOwner(), address -> {
            binding.localitzacio.setText(String.format(
                    "Direcció: %1$s \n Hora: %2$tr",
                    address, System.currentTimeMillis()));
        });

        homeViewModel1.getButtonText().observe(getViewLifecycleOwner(), s -> binding.buttonLocation.setText(s));
        homeViewModel1.getProgressBar().observe(getViewLifecycleOwner(), visible ->{
            if (visible)
                binding.localitzacio.setVisibility(ProgressBar.VISIBLE);
            else
                binding.localitzacio.setVisibility(ProgressBar.INVISIBLE);
            });

        binding.buttonLocation.setOnClickListener(view -> {
            Log.d("DEBUG", "Has clicado el boton");
            homeViewModel1.switchTrackingLocation();
        });


        return root;


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}