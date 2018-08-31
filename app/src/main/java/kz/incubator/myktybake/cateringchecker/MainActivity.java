package kz.incubator.myktybake.cateringchecker;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import kz.incubator.myktybake.cateringchecker.fragments.OrderFragment;
import kz.incubator.myktybake.cateringchecker.fragments.TodayFragment;
import kz.incubator.myktybake.cateringchecker.module.Personnel;

import static kz.incubator.myktybake.cateringchecker.StoreDatabase.COLUMN_CARD_NUMBER;
import static kz.incubator.myktybake.cateringchecker.StoreDatabase.COLUMN_ID_NUMBER;
import static kz.incubator.myktybake.cateringchecker.StoreDatabase.COLUMN_INFO;
import static kz.incubator.myktybake.cateringchecker.StoreDatabase.COLUMN_OTHER_COUNT;
import static kz.incubator.myktybake.cateringchecker.StoreDatabase.COLUMN_PHOTO;
import static kz.incubator.myktybake.cateringchecker.StoreDatabase.COLUMN_TEACHER_COUNT;
import static kz.incubator.myktybake.cateringchecker.StoreDatabase.COLUMN_TOTAL_COUNT;
import static kz.incubator.myktybake.cateringchecker.StoreDatabase.COLUMN_TYPE;
import static kz.incubator.myktybake.cateringchecker.StoreDatabase.COLUMN_VOLUNTEER_COUNT;
import static kz.incubator.myktybake.cateringchecker.StoreDatabase.COLUMN_WORKER_COUNT;
import static kz.incubator.myktybake.cateringchecker.StoreDatabase.TABLE_PERSONNEL;
import static kz.incubator.myktybake.cateringchecker.StoreDatabase.TABLE_PERSONNEL_COUNT;

public class MainActivity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener {

    TodayFragment todayFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().getItem(0).setChecked(true);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        FirebaseApp.initializeApp(this);

        todayFragment = new TodayFragment();
        changeFragment(todayFragment);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_main) {
            changeFragment(todayFragment);
        }else if(id == R.id.nav_order){
            changeFragment(new OrderFragment());
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void changeFragment(Fragment cfragment) {
        Fragment fragment = cfragment;
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();
    }


}
