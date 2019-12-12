package com.example.spartify1.ui.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.spartify1.R;
import com.example.spartify1.Song;
import com.example.spartify1.SongService;
import com.example.spartify1.User;
import com.example.spartify1.UserService;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.io.InputStream;
import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;

    public static final String CLIENT_ID = "5cf2b16f08d44fb8a6acb81bd1925738";
    public static final String REDIRECT_URI = "http://com.example.spartify1/callback";

    private static final int REQUEST_CODE = 1337;
    private static final String SCOPES = "user-read-recently-played,user-library-modify,user-read-email,user-read-private";

    private SharedPreferences.Editor editor;
    private SharedPreferences msharedPreferences;

    private RequestQueue queue;

    private Song song;

    private UserService userService = null;
    private SongService songService;
    private ArrayList<Song> recentlyPlayedTracks;
    private View root;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        root = inflater.inflate(R.layout.fragment_profile, container, false);
        final TextView textView = root.findViewById(R.id.text_name);
        profileViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        Button connectButton = root.findViewById(R.id.connect_button);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticateSpotify();
                displayUserData(root);
                v.setVisibility(View.GONE);
                ProfileFragment fragment = new ProfileFragment();
                getFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).commit();
            }
        });
        if (this.userService != null) displayUserData(root);
        songService = new SongService(getActivity().getApplicationContext());
        msharedPreferences = this.getActivity().getSharedPreferences("SPOTIFY", 0);
        queue = Volley.newRequestQueue(this.getActivity());
        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences("SPOTIFY", 0);
        Log.d("song", sharedPreferences.getString("userid", "No User"));
        if (msharedPreferences.getBoolean("ConnectionFlag", false) == true) {
            displayUserData((root));
            connectButton.setVisibility(View.GONE);

        }
        return root;
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

                    editor.commit();
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
            displayUserData(root);
        }
    }


    private void waitForUserInfo() {
        this.userService = new UserService(queue, msharedPreferences);
        this.userService.get(() -> {
            User user = this.userService.getUser();
            editor = getActivity().getSharedPreferences("SPOTIFY", 0).edit();
            editor.putString("userid", user.id);
            editor.putString("display_name", user.display_name);
            if(user.images.length > 0) {
                editor.putString("profile_pic_url", user.images[0].url);
            }
            Log.d("GOT USER INFORMATION", user.id);
            editor.putBoolean("ConnectionFlag", true);
            // We use commit instead of apply because we need the information stored immediately
            editor.commit();
//            startMainActivity();
        });
    }

    private void displayUserData(View view) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("SPOTIFY", 0);

        TextView username = view.findViewById(R.id.text_username);
        TextView name = view.findViewById(R.id.text_name);

        name.setText(sharedPreferences.getString("display_name", "Display Name"));
        username.setText(sharedPreferences.getString("userid", "User ID"));
        ImageView profilePic = view.findViewById(R.id.picture_profile);
        if(sharedPreferences.getString("profile_pic_url", "def").equals("def")) {
            profilePic.setImageResource(R.drawable.default_profile);
        }
        else {
            new DownloadImageTask(profilePic).execute(sharedPreferences.getString("profile_pic_url", "def"));
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

}