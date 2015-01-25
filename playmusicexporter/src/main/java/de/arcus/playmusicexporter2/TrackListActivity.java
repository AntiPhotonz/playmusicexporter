/*
 * Copyright (c) 2015 David Schulte
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.arcus.playmusicexporter2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import java.util.List;

import de.arcus.framework.logger.Logger;
import de.arcus.framework.crashhandler.CrashHandler;
import de.arcus.playmusiclib.PlayMusicManager;
import de.arcus.playmusiclib.datasources.PlaylistDataSource;
import de.arcus.playmusiclib.enums.ID3v2Version;
import de.arcus.playmusiclib.items.MusicTrack;
import de.arcus.playmusiclib.items.Playlist;

/**
 * An activity representing a list of Tracks. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link TrackDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link TrackListFragment} and the item details
 * (if present) is a {@link TrackDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link TrackListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class TrackListActivity extends ActionBarActivity
        implements TrackListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_list);

        // Adds the crash handler to this class
        CrashHandler.addCrashHandler(this);

        Logger.getInstance().logVerbose("Activity", "onCreate(" + this.getLocalClassName() + ")");

        // Setup ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.app_name);
        }

        if (findViewById(R.id.track_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((TrackListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.track_list))
                    .setActivateOnItemClick(true);
        }


        PlayMusicManager playMusicManager = new PlayMusicManager(this);

        try {
            // Simple play ground
            playMusicManager.startUp();
            playMusicManager.setOfflineOnly(true);


            playMusicManager.setID3Enable(true);
            playMusicManager.setID3EnableArtwork(true);
            playMusicManager.setID3EnableFallback(true);
            playMusicManager.setID3v2Version(ID3v2Version.ID3v23);

            PlaylistDataSource playlistDataSource = new PlaylistDataSource(playMusicManager);

            playlistDataSource.setSerchKey("Angesagte Songs");

            // Load all albums
            List<Playlist> playlists = playlistDataSource.getAll();

            for (Playlist playlist : playlists) {
                // Load tracks from album
                List<MusicTrack> tracks = playlist.getMusicTrackList();

                for (MusicTrack track : tracks) {
                    // Test: exports the track to the sd card
                    String filename = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC) + "/"+track.getTitle()+".mp3";
                    boolean success = playMusicManager.exportMusicTrack(track, filename);

                    Log.d("Debug", track.getTitle() + ": " + success);
                }
            }

        } catch (Exception e) {
            Logger.getInstance().logError("Test", e.toString());
        }


        // TODO: If exposing deep links into your app, handle intents here.
    }

    /**
     * Callback method from {@link TrackListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(TrackDetailFragment.ARG_ITEM_ID, id);
            TrackDetailFragment fragment = new TrackDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.track_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, TrackDetailActivity.class);
            detailIntent.putExtra(TrackDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }
}