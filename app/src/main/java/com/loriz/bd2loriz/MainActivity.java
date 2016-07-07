package com.loriz.bd2loriz;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.Toolbar;

import com.esri.android.map.MapOptions;
import com.esri.android.map.MapView;
import com.loriz.bd2loriz.adapter.EMPagerAdapter;
import com.loriz.bd2loriz.utils.CustomViewPager;
import com.wangjie.androidbucket.utils.ABTextUtil;
import com.wangjie.androidbucket.utils.imageprocess.ABShape;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionButton;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionHelper;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionLayout;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RFACLabelItem;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RapidFloatingActionContentLabelList;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity
        implements NavigationView.OnNavigationItemSelectedListener, RapidFloatingActionContentLabelList.OnRapidFloatingActionContentLabelListListener {

    private MapView mMapView;
    private String mMapState;
    private CustomViewPager mViewPager;
    private EMPagerAdapter mPagerAdapter;
    private RapidFloatingActionHelper rfabHelper;
    private ArrayList<RFACLabelItem> items;
    private int QUERY_PAGE = 1;
    private int MAP_PAGE = 0;
    private boolean isRFABOpen = false;
    private RapidFloatingActionButton rfaBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setActionBar(toolbar);

        rfaBtn = (RapidFloatingActionButton) findViewById(R.id.activity_main_rfab);
        RapidFloatingActionLayout rfaLayout = (RapidFloatingActionLayout) findViewById(R.id.activity_main_rfal);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setDrawerIndicatorEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
            }
        });
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //inizializzazione

        mViewPager = (CustomViewPager) findViewById(R.id.viewpager);
        mViewPager.setPagingEnabled(false);
        mPagerAdapter = new EMPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);

        RapidFloatingActionContentLabelList rfaContent = new RapidFloatingActionContentLabelList(this);
        rfaContent.setOnRapidFloatingActionContentLabelListListener(this);

        items = new ArrayList<>();
        items.add(new RFACLabelItem<Integer>()
                .setLabel("Mostra/Nascondi Province")
                .setResId(R.drawable.ic_sard_white)
                .setIconNormalColor(0xff4e342e)
                .setIconPressedColor(0xff3e2723)
                .setWrapper(0)
        );
        items.add(new RFACLabelItem<Integer>()
                .setLabel("Mostra/Nascondi Elettrodotti")
                .setResId(R.drawable.ic_power_white_24dp)
                .setIconNormalColor(0xffcccc00)
                .setIconPressedColor(0xff808000)
                .setWrapper(0)
        );
        items.add(new RFACLabelItem<Integer>()
                .setLabel("Mostra/Nascondi Metanodotti")
                .setResId(R.drawable.ic_local_gas_station_white_24dp)
                .setIconNormalColor(0xff804000)
                .setIconPressedColor(0xff4d2600)
                .setWrapper(0)
        );
        items.add(new RFACLabelItem<Integer>()
                .setLabel("Vai alle Query")
                .setResId(R.drawable.ic_search_white_24dp)
                .setIconNormalColor(0xffd84315)
                .setIconPressedColor(0xffbf360c)
                .setWrapper(0)
        );
        rfaContent
                .setItems(items)
                .setIconShadowRadius(ABTextUtil.dip2px(this, 5))
                .setIconShadowColor(0xff888888)
                .setIconShadowDy(ABTextUtil.dip2px(this, 5))
        ;
        rfabHelper = new RapidFloatingActionHelper(
                this,
                rfaLayout,
                rfaBtn,
                rfaContent
        ).build();

        rfaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rfabHelper.toggleContent();
                isRFABOpen = !isRFABOpen;
            }
        });

    }

    @Override
    protected void onStart() {

        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMapView != null) {
            // Save map state
            mMapState = mMapView.retainState();


            // Call MapView.pause to suspend map rendering while the activity is
            // paused, which can save battery usage.
            mMapView.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Call MapView.unpause to resume map rendering when the activity returns
        // to the foreground.
        if (mMapView != null) {
            mMapView.unpause();
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START) || isRFABOpen) {
            if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START);
            if (isRFABOpen) {
                isRFABOpen = !isRFABOpen;
                rfabHelper.collapseContent();
            }
        } else if (mViewPager.getCurrentItem() == QUERY_PAGE) {
            mViewPager.setCurrentItem(MAP_PAGE);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        if (mMapView == null) {
            mMapView = (MapView) mPagerAdapter.getFragment(0).getView().findViewById(R.id.map_layout);
        }
        int id = item.getItemId();

        if (id == R.id.map_satellite) {

            if (mMapView != null) {
                mMapView.setMapOptions(new MapOptions(MapOptions.MapType.SATELLITE));
            }

        } else if (id == R.id.map_topo) {

            if (mMapView != null) {
                mMapView.setMapOptions(new MapOptions(MapOptions.MapType.TOPO));
            }

        } else if (id == R.id.map_gray) {

            if (mMapView != null) {
                mMapView.setMapOptions(new MapOptions(MapOptions.MapType.GRAY));
            }

        } else if (id == R.id.map_hybrid) {

            if (mMapView != null) {
                mMapView.setMapOptions(new MapOptions(MapOptions.MapType.HYBRID));
            }

        } else if (id == R.id.map_geographic) {

            if (mMapView != null) {
                mMapView.setMapOptions(new MapOptions(MapOptions.MapType.NATIONAL_GEOGRAPHIC));
            }

        } else if (id == R.id.map_osm) {

            if (mMapView != null) {
                mMapView.setMapOptions(new MapOptions(MapOptions.MapType.OSM));
            }

        } else if (id == R.id.map_streets) {

            if (mMapView != null) {
                mMapView.setMapOptions(new MapOptions(MapOptions.MapType.STREETS));
            }

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRFACItemLabelClick(int i, RFACLabelItem rfacLabelItem) {
    }

    @Override
    public void onRFACItemIconClick(int position, RFACLabelItem item) {
        if (position == (items.size() - 1)) {
            mViewPager.setCurrentItem(QUERY_PAGE, true);
        }
        rfabHelper.toggleContent();
        isRFABOpen = false;
    }


}
