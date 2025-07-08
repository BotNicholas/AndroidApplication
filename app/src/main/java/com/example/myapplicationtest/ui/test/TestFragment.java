package com.example.myapplicationtest.ui.test;

import static android.view.View.VISIBLE;
import static androidx.core.content.ContextCompat.getSystemService;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplicationtest.databinding.FragmentTestBinding;
import com.example.myapplicationtest.models.UserResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TestFragment extends Fragment {

    private FragmentTestBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        TestViewModel testViewModel =
                new ViewModelProvider(this).get(TestViewModel.class);

        binding = FragmentTestBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        TextView result = binding.editTextText;
        TextView id = binding.editTextTextId;
        Button button = binding.button;

        TableLayout table = binding.receivedTable;
        TextView receivedUserId = binding.receivedUserId;
        TextView receivedId = binding.receivedId;
        TextView receivedTitle = binding.receivedTitle;
        TextView receivedBody = binding.receivedBody;

        ObjectMapper om = new ObjectMapper();
        OkHttpClient client = new OkHttpClient();
//        Request request = new Request.Builder()
//                .url("https://jsonplaceholder.typicode.com/posts/1")
//                .build();

        button.setOnClickListener((View v) -> {
            if (isNetworkAvailable()) {
                Request request = new Request.Builder()
                        .url("https://jsonplaceholder.typicode.com/posts/" + id.getText())
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        requireActivity().runOnUiThread(() -> {
                            result.setText(e.getLocalizedMessage());
                            e.printStackTrace();
                        });
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            requireActivity().runOnUiThread(() -> {
                                try {
                                    String responseBody = response.body().string();

                                    UserResponse user = om.readValue(responseBody, UserResponse.class);

                                    Log.d("HTTP", responseBody);
                                    Log.d("MAPPED OBJECT", user.toString());
                                    result.setText(String.format("Successfully received %d user info!", user.getId()));

                                    receivedUserId.setText(user.getUserId().toString());
                                    receivedId.setText(user.getId().toString());
                                    receivedTitle.setText(user.getTitle());
                                    receivedBody.setText(user.getBody());

                                    table.setVisibility(VISIBLE);

                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    }
                });
            } else {
                result.setText("No Internet Connection!!!");
            }


//            System.out.println(v.getX());
//            System.out.println(v.getY());
//            text.setText("This is a default TEST text");
        });

        final TextView textView = binding.textTest;
        testViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = requireContext().getSystemService(ConnectivityManager.class);
        Network currentNetwork = connectivityManager.getActiveNetwork();
        NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(currentNetwork);

        return caps != null && (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET));
    }
}