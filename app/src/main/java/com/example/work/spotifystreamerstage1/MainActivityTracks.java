package com.example.work.spotifystreamerstage1;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


public class MainActivityTracks extends ActionBarActivity {
    public static final String TAG = "ActivityTracks";
    private static TrackInfoAdapter adapter;

    public static ArrayList<trackInfo> trackInfos = new ArrayList<trackInfo>();
    public static trackInfo noEntries = new trackInfo("none", "none", "none","none");
    public static int totalTracksFound = 0;
    public static int totalTracksShown = 0;

    public static String artistNameString = null;
    public static String artistIDString = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity_tracks);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        Intent intent = getIntent();
        artistNameString = intent.getStringExtra(artistFragment.EXTRA_MESSAGE);
        artistIDString = intent.getStringExtra(artistFragment.EXTRA_MESSAGE_ID);

        ActionBar myBar = getSupportActionBar();
        myBar.setTitle("Top 10 Tracks");
        myBar.setSubtitle(artistNameString);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_activity_tracks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_activity_tracks, container, false);

            adapter = new TrackInfoAdapter(getActivity(), trackInfos);

            ListView trackView = (ListView) rootView.findViewById(R.id.listViewTracks);
            trackView.setAdapter(adapter);

            trackView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    trackInfo value = adapter.getItem(position);
                    Toast.makeText(getActivity(), value.trackName, Toast.LENGTH_SHORT).show();
                }
            });

            if (artistNameString != null) {
                new fetchTrackInfoTask().execute(artistNameString,artistIDString);
            }

            return rootView;
        }
    }

    static private class fetchTrackInfoTask extends AsyncTask<String, String, ArrayList<trackInfo>> {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.

        SpotifyApi api = new SpotifyApi();

        private final String asyncTAG = fetchTrackInfoTask.class.getSimpleName();

        /**
         * The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute()
         */
        protected ArrayList<trackInfo> doInBackground(String... id) {

            totalTracksFound = 0;
            totalTracksShown = 0;

            if (id[0].equals("none") == false) {
                Log.d(asyncTAG, "artist name " + id[0] + "artist ID "+id[1]);

                SpotifyService spotify = api.getService();
                final Map<String, Object> options = new HashMap<String, Object>();
                options.put(SpotifyService.COUNTRY, "US");
                Tracks results = spotify.getArtistTopTrack(id[1], options);

                publishProgress("50%");

                int len = results.tracks.size();
                totalTracksFound = len;
                if (len > 10) len = 10;  // list just the top 10
                if (len != 0) {
                    totalTracksShown = len;
                    ArrayList<trackInfo> data = new ArrayList<trackInfo>();
                    Log.d(asyncTAG, "len = " + len);
                    Track item;
                    for (int i = 0; i < len; i++) {
                        item = results.tracks.get(i);
                        trackInfo a =
                            new trackInfo(item.name, item.album.name, item.album.images.get(0).url, item.preview_url);
                        data.add(a);
                    }
                    return data;
                } else {
                    ArrayList<trackInfo> data = new ArrayList<trackInfo>();
                    data.add(noEntries);
                    return data;
                }
            } else {
                totalTracksShown = 0;
                totalTracksFound = 0;
                ArrayList<trackInfo> data = new ArrayList<trackInfo>();
                data.add(noEntries);
                return data;
            }
        }

        @Override
        protected void onPreExecute() {
            Log.d(asyncTAG, "onPreExecute ... ");
        }

        /**
         * The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground()
         */
        protected void onPostExecute(ArrayList<trackInfo> result) {
            adapter.clear();
            if (result != null) {
                if (result.get(0).toString().equals("none"))
                    Toast.makeText(adapter.getContext(), "No Artists Found", Toast.LENGTH_SHORT).show();
                else
                    adapter.addAll(result);

            } else
                Toast.makeText(adapter.getContext(), "Internet Connection problem?", Toast.LENGTH_LONG).show();

            Log.d(TAG,Integer.toString(totalTracksShown) + " of " + Integer.toString(totalTracksFound));
            publishProgress("100%");

        }

        @Override
        protected void onProgressUpdate(String... progress) {
            Log.d(asyncTAG, progress[0]);
        }

    }

}
