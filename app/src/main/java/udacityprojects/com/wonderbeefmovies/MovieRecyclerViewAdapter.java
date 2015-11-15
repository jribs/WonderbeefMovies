package udacityprojects.com.wonderbeefmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Joshua on 10/31/2015.
 */
public class MovieRecyclerViewAdapter extends RecyclerView.Adapter<MovieRecyclerViewAdapter.MovieViewHolder>{


    private JSONArray data;
    private Context mContext;



    //Our little custom viewHolder. All we need here is the imageview we are going to Picasso like a champ
    public static class MovieViewHolder extends RecyclerView.ViewHolder{
        ImageView poster;

        public MovieViewHolder(View v) {
            super(v);
            poster=(ImageView) v.findViewById(R.id.item_movie_image);
        }

    }




    //Constructor, need context for onclick intent and Picassorita
    public MovieRecyclerViewAdapter(JSONArray movies,  Context context){
        data = movies;
        mContext=context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie, parent, false);
        // set the view's size, margins, paddings and layout parameters
        MovieViewHolder movieViewHolder = new MovieViewHolder(v);
        return movieViewHolder;
    }


    @Override
    public void onBindViewHolder(MovieViewHolder holder, final int position){
        //First parse out the image URL for our position
        try {
            final JSONObject movie = data.getJSONObject(position);
            String imageURL = mContext.getString(R.string.url_posterpath) +
                    mContext.getString(R.string.url_poster_size_185)+
                    movie.getString(mContext.getString(R.string.api_posterPath));
            //Glorious Picasso is binding our image to the ImageView
            Picasso.with(mContext)
                    .load(imageURL)
                    .into(holder.poster);

            //here we are setting the onClickListener to broadcast our movie o
            holder.poster.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Post to Eventbus. This is the only place where we could post data relevant to what
                    //was clicked (need a position)
                    App.getInstance().getEventBus().post(movie);
                }
            });
        } catch (JSONException e){
            Log.e("RecyclerView", e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return data.length();
    }




}


