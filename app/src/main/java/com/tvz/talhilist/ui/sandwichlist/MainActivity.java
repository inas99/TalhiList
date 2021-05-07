package com.tvz.talhilist.ui.sandwichlist;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.tvz.talhilist.ui.details.DetailActivity;
import com.tvz.talhilist.R;
import com.tvz.talhilist.model.Sandwich;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    BroadcastReceiver broadcastReceiver;
    private SandwichAdapter mSandwichAdapter;
    private SandwichListViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button changeLang = findViewById(R.id.Lang);
        changeLang.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                //show AlertDialog to display list of languages, one can be selected
                showChangeLanguageDialog ();

            }

        });

        mViewModel = obtainViewModel(this);

        setupToolbar();

        setupListAdapter();

        // subscribe to sandwich observable livedata
        mViewModel.getSandwichList().observe(this, new Observer<List<Sandwich>>() {
            @Override
            public void onChanged(@Nullable List<Sandwich> sandwiches) {
                if (sandwiches != null) {
                    mSandwichAdapter.replaceData(sandwiches);
                }
            }
        });

        // Subscribe to "open sandwich" event
        mViewModel.getOpenSandwichEvent().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer position) {
                if (position != null) {
                    launchDetailActivity(position);
                }
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }

    private SandwichListViewModel obtainViewModel(FragmentActivity activity) {
        return ViewModelProviders.of(activity).get(SandwichListViewModel.class);
    }

    private void setupListAdapter() {
        RecyclerView recyclerView = findViewById(R.id.recycler_sandwich_list);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        mSandwichAdapter = new SandwichAdapter(this,
                new ArrayList<Sandwich>(0),
                mViewModel
        );
        recyclerView.setAdapter(mSandwichAdapter);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void launchDetailActivity(int position) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_POSITION, position);
        startActivity(intent);
    }
    private void showChangeLanguageDialog()
    {
        //array of languages to display in alert dialog
        final String[] listItems = {"English", "Français", "العربية"  , "Türkçe" , "Española"};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        mBuilder.setTitle("choose Language...");
        mBuilder.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener(){

            public void onClick(DialogInterface dialogInterface, int i){
                if (i ==0){
                    //English
                    setLocale("en");
                    recreate();
                }
                if (i ==1){
                    //French
                    setLocale("fr");
                    recreate();
                }
                if (i ==2){
                    //Arabic
                    setLocale("ar");
                    recreate();
                }
                if (i ==3){
                    //Turkish
                    setLocale("tr");
                    recreate();
                }
                if (i ==4){
                    //Spanish
                    setLocale("sp");
                    recreate();
                }

                //dismiss dialog when language selected
                dialogInterface.dismiss();
            }
        });

        AlertDialog mDialog =  mBuilder.create();
        //show alert dialog
        mDialog.show();
    }
    private void setLocale(String lang){
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,getBaseContext().getResources().getDisplayMetrics());

        //save data to shared preferances
        SharedPreferences.Editor editor = getSharedPreferences("settings", MODE_PRIVATE ).edit();
        editor.putString("My lang", lang);
        editor.apply();
    }
    //load language saved in shared preferences
    public void loadLocale() {
        SharedPreferences prefs = getSharedPreferences("settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_lang", "");
        setLocale(language);

    }

    public boolean OnOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.share:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Do you really want to share this great receipee?")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which){
                                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                                sharingIntent.setType("text/plain");
                                String shareBody="The receipee will be shared";
                                String shareSubject="Sharing...";

                                sharingIntent.putExtra(Intent.EXTRA_TEXT,shareBody);
                                sharingIntent.putExtra(Intent.EXTRA_SUBJECT,shareSubject);

                                startActivity(Intent.createChooser(sharingIntent, "Share Using"));

                            }})
                        .setNegativeButton("Cancel",null);
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            case R.id.help:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }


    }
    protected void registerNetworkBroadcastReceiver(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
            registerReceiver(broadcastReceiver,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        }
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            registerReceiver(broadcastReceiver,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        }
    }
    protected void unregisterNetwork(){
        try {
            unregisterReceiver(broadcastReceiver);
        }
        catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterNetwork();
    }
}