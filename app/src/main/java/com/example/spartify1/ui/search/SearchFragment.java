package com.example.spartify1.ui.search;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.spartify1.R;
import com.example.spartify1.Song;
import com.example.spartify1.SongService;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private SearchViewModel searchViewModel;

    private SongService songService;
    private ArrayList<Song> searchedTracks;
    private ListView listView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        searchViewModel =
                ViewModelProviders.of(this).get(SearchViewModel.class);
        View root = inflater.inflate(R.layout.fragment_search, container, false);

        listView = root.findViewById(R.id.results);
        Button searchButton = root.findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText searchBar = root.findViewById(R.id.search_bar);
                search(searchBar.getText().toString());
            }
        });

        return root;
    }

    private void search( String query) {
        songService = new SongService(getActivity().getApplicationContext());
        songService.getSearch(() -> {
            searchedTracks = songService.getSongs();

            ArrayAdapter<Song> arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.simplerow, searchedTracks);
            listView.setAdapter(arrayAdapter);
        }, query);
    }
}