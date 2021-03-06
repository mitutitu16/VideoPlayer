package com.alirezaahmadi.videoplayer.activity;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.alirezaahmadi.videoplayer.R;
import com.alirezaahmadi.videoplayer.adapter.VideoAdapter;
import com.alirezaahmadi.videoplayer.util.NavigationController;
import com.alirezaahmadi.videoplayer.viewmodel.DaggerViewModelFactory;
import com.alirezaahmadi.videoplayer.viewmodel.PlaylistDetailViewModel;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class PlayListDetailActivity extends BaseActivity implements VideoAdapter.VideoClickListener, VideoAdapter.SelectionModeListener , ActionMode.Callback {
    private static final String ARG_PLAYLIST_ID = "playlist_id";

    @Inject DaggerViewModelFactory viewModelFactory;
    @Inject VideoAdapter adapter;
    @Inject NavigationController navigationController;

    RecyclerView recyclerView;
    Toolbar toolbar;
    View emptyError;

    PlaylistDetailViewModel viewModel;
    LinearLayoutManager layoutManager;
    ActionMode actionMode;

    public static Intent createIntent(Context context, int playerId){
        Intent intent = new Intent(context, PlayListDetailActivity.class);
        intent.putExtra(ARG_PLAYLIST_ID, playerId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_detail);
        startActivityInitProcess();
    }

    @Override
    protected void onCreateViewInstances() {
        recyclerView = findViewById(R.id.play_list_detail_recycler_view);
        toolbar = findViewById(R.id.playlist_detail_toolbar);
        emptyError = findViewById(R.id.play_list_detail_empty_error);
    }

    @Override
    protected void onBindViewModel() {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(PlaylistDetailViewModel.class);

        if(getIntent().getExtras() == null || !getIntent().getExtras().containsKey(ARG_PLAYLIST_ID))
            throw new IllegalStateException("Activity need playlist id to work");

        int playlistId = getIntent().getIntExtra(ARG_PLAYLIST_ID, -1);
        viewModel.setPlaylistId(playlistId);
        viewModel.getVideoList().observe(this, videos -> adapter.setVideoList(videos));
        viewModel.getTitle().observe(this, title -> toolbar.setTitle(title));
        viewModel.getEmptyErrorVisibility().observe(this, visibility -> emptyError.setVisibility(visibility));
    }

    @Override
    protected void onViewInit() {
        layoutManager = new LinearLayoutManager(this);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);

        adapter.setVideoClickListener(this);
        adapter.setSelectionModeListener(this);
        adapter.setSelectionModeListener(this);

        initToolbar();
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setTitle(R.string.app_name);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.playlist_context_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if(item.getItemId() == R.id.delete_from_playlist) {
            viewModel.deleteVideos(adapter.getSelectedList());
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        adapter.cancelCelectionMode();
    }

    @Override
    public void onVideoClicked(int videoId) {
        navigationController.navigateToPlayer(videoId, viewModel.getPlaylistId());
    }

    @Override
    public void onSelectionModeChanged(boolean selectionState) {
        if (selectionState)
            actionMode = startActionMode(this);
    }
}
