package udacityprojects.com.wonderbeefmovies;

import android.content.Intent;
import android.graphics.Movie;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.LeadingMarginSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;


public class DetailFragment extends Fragment{

    private static final String TAG = "MovieDetailFragment";

    //JSON object representing our movie. Instantiate onCreate
    private JSONObject mMovie;

    //View variables. Used when parsin data
    private TextView vReleaseDate;
    private TextView vSynopsis;
    private TextView vRating;
    private ImageView vPoster;
    private ActionBar vBar;



    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //For reasons I don't understand, the line of code below is how to access the toolbar
        //belonging to the parent Activity
        vBar=((AppCompatActivity) getActivity()).getSupportActionBar();
        vBar.setDisplayHomeAsUpEnabled(true);
        vBar.setDisplayShowCustomEnabled(false);

        //Setting our JSONObject = our passed string before UI is displayed
        String jsonMovie = getArguments().getString(getString(R.string.key_args_movieItem));

        //Just a double check to make sure we actually received an JSON string
        if(jsonMovie==null){
            Log.e(TAG, "Fragment Instantiated without a JSON String");
        } else {
            //Now we instantiate the JSONObject, with ye olde try catch to handle exceptions
            try{
                mMovie = new JSONObject(jsonMovie);
            } catch (JSONException e){
                Log.e(TAG, e.getMessage());
            }
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_movie_detail, container, false);
            //Instantiate all of our views

            vSynopsis = (TextView) v.findViewById(R.id.detail_description);
            vRating = (TextView) v.findViewById(R.id.detail_rating);
            vReleaseDate = (TextView) v.findViewById(R.id.detail_releaseDate);
            vPoster = (ImageView) v.findViewById(R.id.detail_poster);
            parseJSONData(mMovie);
        return  v;
    }

    //This segment is for actually going back when the back button is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
        }
        return (super.onOptionsItemSelected(menuItem));
    }



    //Here we crank out linking JSON fields to their respective Views
    private void parseJSONData(JSONObject movie){
        //First some strings representing the fieldnames as marked in MDB API
        final String MDB_TITLE="original_title";
        final String MDB_RATING="vote_average";
        final String MDB_SYNOPSIS = "overview";
        final String MDB_RELEASEDATE="release_date";




        try {
            //Image URL acquired by lovely concatenation
            String imageURL = getString(R.string.url_posterpath) +
                    getString(R.string.url_poster_size_185)+
                    movie.get(getString(R.string.api_posterPath));

            vBar.setDisplayShowTitleEnabled(true);
            vBar.setTitle(movie.getString(MDB_TITLE));
            vRating.setText(movie.getString(MDB_RATING));

            vReleaseDate.setText(formatUSDate(movie.getString(MDB_RELEASEDATE)));

            //Some of the synopses were null, so I am putting a check here so it doesn't look silly
            String synopsis = movie.getString(MDB_SYNOPSIS);
            if(synopsis.matches("null")||synopsis==null) {
                vSynopsis.setText("");
            }else {
                vSynopsis.setText("     " + (synopsis));
            }

            //Load the image using Picasso
            Picasso.with(getActivity())
                    .load(imageURL)
                    .into(vPoster);

        } catch (JSONException e){
            Log.e(TAG, e.getMessage());
        }

    }

    //For formatting the date and keeping the parseJSON method readable
    private String formatUSDate(String date){
        String formattedDate="";
        SimpleDateFormat fromMDB = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat toUSstandard = new SimpleDateFormat("MM/dd/yyyy");


        try {
            formattedDate= toUSstandard.format(fromMDB.parse(date));

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return formattedDate;
    }


}
