package udacityprojects.com.wonderbeefmovies;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GridFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    //Views
    private RecyclerView vGrid;
    private Spinner vSortBy;

    //Constants
    private static final String TAG = "MainActivity";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_main_grid, container, false);

        //This first segment is for setting up the toolbar.
        ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        toolbar.setDisplayHomeAsUpEnabled(false);
        toolbar.setDisplayShowCustomEnabled(true);

        //Inflate the Spinner
        View vi = inflater.inflate(R.layout.spinner_sortby, null);
        vSortBy = (Spinner) vi.findViewById(R.id.grid_spinner);

        //Set the adapter
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.labels_sortby, R.layout.item_sortby);
        adapter.setDropDownViewResource(R.layout.item_dropdown_sortby);
        vSortBy.setAdapter(adapter);
        vSortBy.setOnItemSelectedListener(this);
        toolbar.setCustomView(vi);

        //Set the spinner position to that of our SharedPref value.
            //Using if statement due to differing values of display and saved value in sahred pref
        if(getSortByPreference().matches(getString(R.string.default_prefs_sortby))){
            vSortBy.setSelection(0);
        } else {
            vSortBy.setSelection(1);
        }

        //Disable title
        toolbar.setDisplayShowTitleEnabled(false);

        //Instantiate the RecyclerView, set it to Gridlayout
        vGrid=(RecyclerView) v.findViewById(R.id.main_list);
        vGrid.setHasFixedSize(true);

        //Depending on orientation, assign 2 or 4 columns for portrait and landscape respectively
        //onCreateView is called on ever orientation change, so we can leave an if statement here
        if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            vGrid.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        }
        else{
            vGrid.setLayoutManager(new GridLayoutManager(getActivity(), 4));
        }

        //Setting a null adapter here just to get rid of pesky errors, it is reset in AsyncTask
        vGrid.setAdapter(null);

        //Initiate the Async Task
        RetrieveMovieList retrieveMovieList = new RetrieveMovieList();
        retrieveMovieList.execute(getSortByPreference());
        return v;
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //First Change the Preferences to selected item
        SharedPreferences sP = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sP.edit().putString(getString(R.string.key_prefs_sortby),
                getResources().getStringArray(R.array.values_sortby)[position]).apply();

        //Re populate the RecyclerView
        RetrieveMovieList list = new RetrieveMovieList();
        list.execute(getSortByPreference());
        
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
            
    }


    /*  Gameplan here:  Retrieve the JSON string.
                        Convert it to a JSONArray
                        Convert JSON objects into MovieItems
                        Return an ArrayList of MovieItems
    */
    private class RetrieveMovieList extends AsyncTask<String, Void, JSONArray> {


        @Override
        protected JSONArray doInBackground(String... params) {



            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String mdbJSONStr = null;

            //Parameters for API
            String apiKey = getString(R.string.url_APIKEY);

            try {
                // Construct the URL for the MovieDB Query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://api.themoviedb.org/3/discover/movie?


                final String MDB_BASE_URL =
                        "http://api.themoviedb.org/3/discover/movie?";
                final String PARAM_SORTBY = "sort_by";
                final String PARAM_APIKEY = "api_key";

                Uri builtUri = Uri.parse(MDB_BASE_URL).buildUpon()
                        .appendQueryParameter(PARAM_SORTBY, params[0])
                        .appendQueryParameter(PARAM_APIKEY, apiKey)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(TAG, "Built URI " + builtUri.toString());

                // Create the request to MovieDB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    //Add new line for readability, derp
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                mdbJSONStr = buffer.toString();

            } catch (IOException e) {
                Log.e(TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
            try{
                //Return the ArrayList
                return parseJSONString(mdbJSONStr);

            } catch (JSONException e){
                //If not, something went wonky
                Log.e(TAG, e.getMessage());
                return null;
            }


        }

        @Override
        protected void onPostExecute(JSONArray result){
            super.onPostExecute(result);
            //Set our adapter on the UI thread.
            vGrid.setAdapter(new MovieRecyclerViewAdapter(result, getActivity()));
        }

        //Method to parse our JSON String and return an Array of MovieItem objects to use in RecyclerView
        //Adapter
        private JSONArray parseJSONString(String JSONstring) throws JSONException{

            try {
                final String MDB_RESULTS = "results";

                JSONObject mdbResponse = new JSONObject(JSONstring);
                JSONArray results = mdbResponse.getJSONArray(MDB_RESULTS);


                //For our JSONObjectArray, make a MovieObject and add it to our ArrayList
                /**for (int i = 0; i < results.length(); i++) {
                    MovieItem item = new MovieItem(results.optJSONObject(i));
                    items.add(item);
                }**/

                return results;
            } catch (JSONException e){
                Log.e(TAG, "failed to populate arraylist due to JSON error: " + e.getMessage());
                return null;
            }
        }

    }
    

    //This is just to save some space and add readability.
    private String getSortByPreference(){
        return PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(
                getString(R.string.key_prefs_sortby),
                getString(R.string.default_prefs_sortby));
    }


}
