package com.example.spartify1.ui.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.spartify1.MainActivity;
import com.example.spartify1.R;
import com.example.spartify1.Song;
import com.example.spartify1.SongService;
import com.example.spartify1.User;
import com.example.spartify1.UserService;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.io.IOException;
import java.util.ArrayList;


public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;

    private static final String CLIENT_ID = "5cf2b16f08d44fb8a6acb81bd1925738";
    private static final String REDIRECT_URI = "http://com.example.spartify1/callback";
    private SpotifyAppRemote mSpotifyAppRemote;


    private static final int REQUEST_CODE = 1337;
    private static final String SCOPES = "user-read-recently-played,user-library-modify,user-read-email,user-read-private";

    private SharedPreferences.Editor editor;
    private SharedPreferences msharedPreferences;

    private RequestQueue queue;

    private Song song;

    private UserService userService;
    private SongService songService;
    private ArrayList<Song> recentlyPlayedTracks;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        final TextView textView = root.findViewById(R.id.text_profile);
        profileViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });



        songService = new SongService(getActivity().getApplicationContext());


        authenticateSpotify();
        msharedPreferences = this.getActivity().getSharedPreferences("SPOTIFY", 0);
        queue = Volley.newRequestQueue(this.getActivity());


        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences("SPOTIFY", 0);
        Log.d("song", sharedPreferences.getString("userid", "No User"));

        //'.;.getTracks();

//        OkHttpClient client = new OkHttpClient();
//
//        String url = "https://regres.in/api/users?page=2";
//
//
//        Request request = new Request.Builder()
//                .url(url)
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if(response.isSuccessful()) {
//                    String myResponse = response.body().string();
//
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Log.d("json response",myResponse );
//                        }
//                    });
//                }
//            }
//        });
        return root;
    }

//    private void getTracks() {
//        songService.getRecentlyPlayedTracks(() -> {
//            recentlyPlayedTracks = songService.getSongs();
//            updateSong();
//        });
//    }

    private void updateSong() {
        if (recentlyPlayedTracks.size() > 0) {
            Log.d("recently played track", recentlyPlayedTracks.get(0).getName());
            song = recentlyPlayedTracks.get(0);
        }
    }

    private void connected() {
        mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL");

        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        Log.d("MainActivity", track.name + " by " + track.artist.name);
                    }
                });

    }

    private void authenticateSpotify() {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{SCOPES});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(getActivity(), REQUEST_CODE, request);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    editor = getActivity().getSharedPreferences("SPOTIFY", 0).edit();
                    editor.putString("token", response.getAccessToken());
                    Log.d( "GOT AUTH TOKEN", response.getAccessToken());

                    editor.apply();
                    waitForUserInfo();
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }


    private void waitForUserInfo() {
        UserService userService = new UserService(queue, msharedPreferences);
        userService.get(() -> {
            User user = userService.getUser();
            editor = getActivity().getSharedPreferences("SPOTIFY", 0).edit();
            editor.putString("userid", user.id);

            Log.d("GOT USER INFORMATION", user.id);
            // We use commit instead of apply because we need the information stored immediately
            editor.commit();
//            startMainActivity();
        });
    }

//    private void startMainActivity() {
//        Intent newintent = new Intent(MainActivity.this, MainActivity.class);
//        startActivity(newintent);
//    }

}