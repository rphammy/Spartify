package com.example.spartify1.ui.queue;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import org.apache.commons.lang3.RandomStringUtils;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.example.spartify1.R;
import com.example.spartify1.Song;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import com.example.spartify1.Song;
import com.example.spartify1.ui.profile.ProfileFragment;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;

import java.util.ArrayList;

public class QueueFragment extends Fragment {

    private String partyCode;
    private ListView listView;

    private SharedPreferences.Editor editor;
    private SharedPreferences msharedPreferences;

    private RequestQueue queue;
    private boolean activeQueue;

    private SpotifyAppRemote mSpotifyAppRemote;
    ArrayList<Song> songQueue = songQueue = new ArrayList<>();


    DatabaseReference ref;

    private View root;
    Button backButton;
    Context mContext;

    private TextView textView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        TextView textView;
        EditText editText;
        msharedPreferences = getContext().getSharedPreferences("SPOTIFY", 0);
        editor = getActivity().getSharedPreferences("SPOTIFY", 0).edit();
        activeQueue = msharedPreferences.getBoolean("activeQueue", true);


        if(activeQueue) { //load saved state
            root = inflater.inflate(R.layout.fragment_queue_display, container, false);
            backButton = root.findViewById(R.id.button);
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editor = getActivity().getSharedPreferences("SPOTIFY", 0).edit();
                    editor.putBoolean("activeQueue", false);
                    editor.commit();
                    QueueFragment fragment = new QueueFragment();
                    getFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).commit();

                }
            });
            textView = root.findViewById(R.id.textView);
            editText = root.findViewById(R.id.editText);
            editText.setVisibility(View.GONE);
            partyCode= msharedPreferences.getString("partyCode", "");
            textView.setText("Party code: " + partyCode);
        }

        else {
            root = inflater.inflate(R.layout.fragment_queue_welcome, container, false);
            textView = root.findViewById(R.id.text_queue);
            editText = root.findViewById(R.id.editText);
            editText.setVisibility(View.GONE);
            Button joinButton = root.findViewById(R.id.button);
            Button hostButton = root.findViewById(R.id.button2);

            backButton = root.findViewById(R.id.backButton);
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editor = getActivity().getSharedPreferences("SPOTIFY", 0).edit();
                    editor.putBoolean("activeQueue", false);
                    editor.commit();
                    QueueFragment fragment = new QueueFragment();
                    getFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).commit();

                }
            });
            backButton.setVisibility(View.GONE);
            Button goButton = root.findViewById(R.id.go_button);
            goButton.setVisibility(View.GONE);

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
                    QueueFragment fragment = new QueueFragment();
                    getFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).commit();
                    //hostButton.setVisibility(View.GONE);
                    //joinButton.setVisibility(View.GONE);
                    //textView.setText("Party code: " + partyCode);
                    //backButton.setVisibility(View.VISIBLE);
                }
            });


            //join a party
            QueueViewModel queueViewModel= ViewModelProviders.of(this).get(QueueViewModel.class);
            Observer<String> observer = new Observer<String>() {
                @Override
                public void onChanged(@Nullable String s) {
                    partyCode = s;
                    editor.putString("partyCode", partyCode);
                    editText.setVisibility(View.GONE);
                    goButton.setVisibility(View.GONE);
                    textView.setText("Party code: " + partyCode);
                    textView.setVisibility(View.VISIBLE);
                    editor.putBoolean("activeQueue", true);
                    editor.commit();

                }
            };
            queueViewModel.getText().observe(this, observer);

            joinButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    textView.setText("Enter party code: ");
                    hostButton.setVisibility(View.GONE);
                    joinButton.setVisibility(View.GONE);
                    editText.setVisibility(View.VISIBLE);
                    goButton.setVisibility(View.VISIBLE);

                    editor = getActivity().getSharedPreferences("SPOTIFY", 0).edit();

                    backButton.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.GONE);


                    goButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            queueViewModel.setText(editText.getText().toString());

                        }
                    });

                    if(editText.getText().length() == 7) {
                        editor.putBoolean("host", false);
                        editor.putBoolean("activeQueue", true);
                        editor.commit();
                    }

                }
            });

        }

        return root;
    }


    public void playSong(Song song) {
        String songToPlay = song.getUri();
        mSpotifyAppRemote.getPlayerApi().play(songToPlay);
    }


    public void removeSong(Song removeSong) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {

                    Song song = ds.getValue(Song.class);

                    if (song.toString().equals(removeSong.toString())) {
                        String key = ds.getKey();
                        Log.d("key",  key);
                        ds.getRef().removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        ref.child("parties").child("-PARTY_ID_" + partyCode).addListenerForSingleValueEvent(valueEventListener);
    }

    private void getSpotifyAppRemote() {
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(ProfileFragment.CLIENT_ID)
                        .setRedirectUri(ProfileFragment.REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(getActivity(), connectionParams,
                new Connector.ConnectionListener() {

                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");
                    }

                    public void onFailure(Throwable throwable) {
                        Log.e("MyActivity", throwable.getMessage(), throwable);
                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        listView = root.findViewById(R.id.queueList);
        ref = FirebaseDatabase.getInstance().getReference();

        getSpotifyAppRemote();

        //add or remove when data is changed
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //songQueue.clear();
                Song song = dataSnapshot.getValue(Song.class);
                songQueue.add(song);

                Log.d("songqueue", songQueue.toString());
                if(getActivity() != null) {
                    ArrayAdapter<Song> arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.simplerow, songQueue);
                    if (listView != null) listView.setAdapter(arrayAdapter);

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                Song song = dataSnapshot.getValue(Song.class);
                songQueue.remove(song);
                    if(getActivity() != null) {
                        Log.d("song", "removed");
                        ArrayAdapter<Song> arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.simplerow, songQueue);
                        listView.setAdapter(arrayAdapter);
                    }

                    //refresh
                    QueueFragment fragment = new QueueFragment();
                    getFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).commit();

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        ref.child("parties").child("-PARTY_ID_" + partyCode).addChildEventListener(childEventListener);
        //ref.child("parties").addChildEventListener(childEventListener);

        //when you click a song
        if (listView != null) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Song song = songQueue.get(position);

                    boolean host = msharedPreferences.getBoolean("host", false);

                    if(host) {
                        Toast.makeText(getActivity().getBaseContext(), "Playing " + song.toString(), Toast.LENGTH_LONG).show();
                        playSong(song);
                        removeSong(song);
                    }
                }
            });
        }

    }
}