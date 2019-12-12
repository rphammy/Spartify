package com.example.spartify1.ui.queue;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import org.apache.commons.lang3.RandomStringUtils;

import com.android.volley.RequestQueue;
import com.example.spartify1.R;

public class QueueFragment extends Fragment {

    private String partyCode;
    private ListView listView;

    private SharedPreferences.Editor editor;
    private SharedPreferences msharedPreferences;

    private RequestQueue queue;
    private boolean activeQueue;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root;
        TextView textView;
        msharedPreferences = getContext().getSharedPreferences("SPOTIFY", 0);
        activeQueue = msharedPreferences.getBoolean("activeQueue", false);

        if(activeQueue) { //load saved state
            root = inflater.inflate(R.layout.fragment_queue_display, container, false);
            textView = root.findViewById(R.id.textView);
            partyCode= msharedPreferences.getString("partyCode", "");
            textView.setText("Party code: " + partyCode);
        }

        else {
            root = inflater.inflate(R.layout.fragment_queue_welcome, container, false);
            textView = root.findViewById(R.id.text_queue);
            Button joinButton = root.findViewById(R.id.button);
            Button hostButton = root.findViewById(R.id.button2);
            //host a party
            partyCode = RandomStringUtils.randomAlphanumeric(7);
            hostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editor = getActivity().getSharedPreferences("SPOTIFY", 0).edit();
                    editor.putString("partyCode", partyCode);
                    editor.putBoolean("host", true);
                    editor.putBoolean("activeQueue", true);

                    Log.d("GOT PARTY CODE", partyCode);
                    // We use commit instead of apply because we need the information stored immediately
                    editor.commit();
                    hostButton.setVisibility(View.GONE);
                    joinButton.setVisibility(View.GONE);
                    textView.setText("Party code: " + partyCode);
                }
            });

            //join a party
            joinButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //when play is clicked show stop button and hide play button
                    textView.setText("Enter party code: ");
                    hostButton.setVisibility(View.GONE);
                    joinButton.setVisibility(View.GONE);


                    editor = getActivity().getSharedPreferences("SPOTIFY", 0).edit();
                    editor.putBoolean("host", false);
                    editor.putBoolean("activeQueue", true);

                    editor.commit();
                }
            });

        }

        return root;
    }
}