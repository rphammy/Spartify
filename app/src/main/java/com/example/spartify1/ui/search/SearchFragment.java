package com.example.spartify1.ui.search;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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

public class SearchFragment extends Fragment {

    private SearchViewModel searchViewModel;

    private SongService songService;
    private ArrayList<Song> searchedTracks;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        searchViewModel =
                ViewModelProviders.of(this).get(SearchViewModel.class);
        View root = inflater.inflate(R.layout.fragment_search, container, false);

        songService = new SongService(getActivity().getApplicationContext());
        search("abba");

        return root;
    }

    private void search( String query) {
        songService.getSearch(() -> {
            searchedTracks = songService.getSongs();

            for (int i =0; i < searchedTracks.size(); i++){
                Log.d("searched tracks" , searchedTracks.get(i).getName());
            }
        }, query);
    }
}