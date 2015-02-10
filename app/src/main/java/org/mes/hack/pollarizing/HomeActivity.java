package org.mes.hack.pollarizing;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class HomeActivity extends ActionBarActivity {
    private static final String TAG = "HomeActivity";
    private String[] navTitles;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle toggle;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        navTitles = getResources().getStringArray(R.array.nav_items);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close){
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                int pos = drawerList.getSelectedItemPosition();
                if(pos != -1) {
                    getSupportActionBar().setTitle(navTitles[pos]);
                }
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("POLLarizing");
            }
        };

        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerList.setAdapter(new ArrayAdapter<>(this,
                R.layout.list_item,
                navTitles));
        drawerList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                replaceFragment(position);
            }
        });

        drawerLayout.setDrawerListener(toggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(!prefs.getBoolean(Constants.HAS_RUN, false) || !prefs.contains(Constants.GOOGLE_PLUS_ID)){
            startActivity(new Intent(this, RegisterActivity.class));
            prefs.edit().putBoolean(Constants.HAS_RUN, true).apply();
        }
        replaceFragment(0);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    private void replaceFragment(int location){
        Fragment fragment;
        drawerList.setItemChecked(location, true);
        switch(location){
            case 0:
                fragment = new SendFragment();
                break;
            case 1:
                fragment = new SentFragment();
                break;
            case 2:
                fragment = new ReceivedFragment();
                break;
            default:
                fragment = new ReceivedFragment();
                break;
        }
        getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
        setTitle(navTitles[location]);
        drawerLayout.closeDrawer(drawerList);
    }

    private void setTitle(String string){
        getSupportActionBar().setTitle(string);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }

        int id = item.getItemId();
        switch(id){
            default:
                return true;
        }
    }
}
