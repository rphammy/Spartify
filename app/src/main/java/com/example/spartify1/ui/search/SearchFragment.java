package com.example.spartify1.ui.search;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.spartify1.R;
import com.example.spartify1.Song;
import com.example.spartify1.SongService;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchFragment extends Fragment {

    private SearchViewModel searchViewModel;
    private DatabaseReference ref;
    private String partyID;

    private ListView listView;
    ArrayList<Song> searchedTracks;
    private SharedPreferences msharedPreferences;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        msharedPreferences = getContext().getSharedPreferences("SPOTIFY", 0);
        partyID = msharedPreferences.getString("partyCode", "");

        ref = FirebaseDatabase.getInstance().getReference();

        searchViewModel = ViewModelProviders.of(this).get(SearchViewModel.class);
        View root = inflater.inflate(R.layout.fragment_search, container, false);

        listView = root.findViewById(R.id.results);

        //click search
        Button searchButton = root.findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText searchBar = root.findViewById(R.id.search_bar);
                search(searchBar.getText().toString());
            }
        });

        //click song
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Song song = searchedTracks.get(position);
                addSongToQueue(song);
                Toast.makeText(getActivity().getBaseContext(), "Added " + song.toString(), Toast.LENGTH_LONG).show();
            }
        });
        return root;
    }

    private void search( String query) {
        SongService songService = new SongService(getActivity().getApplicationContext());
        songService.getSearch(() -> {
            searchedTracks = songService.getSongs();
            ArrayAdapter<Song> arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.simplerow, searchedTracks);
            listView.setAdapter(arrayAdapter);
        }, query);
    }

    private void addSongToQueue(Song song){
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("name", song.getName());
        dataMap.put("artist", song.getArtist());
        dataMap.put("uri", song.getUri());
        ref.child("parties").child("-PARTY_ID_" + partyID).push().updateChildren(dataMap);
    }
}