package udacityprojects.com.wonderbeefmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;


import com.squareup.otto.Subscribe;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //First taking care of the toolbar. We dont want the title here. Further customization
        //is handled at the Fragment level.
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //If we have a savedInstanceState, leave the fragment as it was.
        if(savedInstanceState!=null){
            return;
        } else {

            //Attach fragment to framelayout
            GridFragment gridFragment = new GridFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_fragContainer, gridFragment).addToBackStack("details").commit();
        }

    }


    @Override
    public void onStart(){
        super.onStart();
        //Register event bus on start
        App.getInstance().getEventBus().register(this);
    }
    @Override
    public void onStop(){
        super.onStop();
        //Unregister when destroyed
        App.getInstance().getEventBus().unregister(this);
    }


    //Handle an itemGridClick. It is used, but the IDE doesn't play nice with EventBus
    @Subscribe
    public void handleItemClicked(JSONObject movie){

        String jsonString = movie.toString();
            //This is only called when GridLayout is in View

            //Make the fragment, set the arguments
            DetailFragment detailFragment = new DetailFragment();
            Bundle movieArgs = new Bundle();
            movieArgs.putString(getString(R.string.key_args_movieItem), jsonString);
            detailFragment.setArguments(movieArgs);
            //Perform transactions
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragContainer, detailFragment).addToBackStack("home").commit();
    }



}
