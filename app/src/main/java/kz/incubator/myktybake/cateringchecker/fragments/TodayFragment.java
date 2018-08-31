package kz.incubator.myktybake.cateringchecker.fragments;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import kz.incubator.myktybake.cateringchecker.R;
import kz.incubator.myktybake.cateringchecker.StoreDatabase;
import kz.incubator.myktybake.cateringchecker.adapters.ViewPagerAdapter;
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

public class TodayFragment extends Fragment {
    View view;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    String date;
    DateFormat dateF;
    int tabPos = 0;
    BreakfastFragment breakfastFragment;
    LunchFragment lunchFragment;
    DinnerFragment dinnerFragment;
    DatabaseReference mDatabaseRef;
    SQLiteDatabase sqdb;
    StoreDatabase storeDb;
    int teacherC = 0, workerC = 0, volunteerC = 0, others = 0, totalC = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_today, container, false);
        setupViews();
        manageDate();

        checkVersion();
        return view;
    }

    public void updatePersonsCount(){
        breakfastFragment.updateViews();
        lunchFragment.updateViews();
        dinnerFragment.updateViews();
    }

    public void setupViews(){
        breakfastFragment = new BreakfastFragment();
        lunchFragment = new LunchFragment();
        dinnerFragment = new DinnerFragment();

        viewPager = view.findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = view.findViewById(R.id.tabs);

        tabLayout.setupWithViewPager(viewPager);
        getActivity().setTitle("Бастысы");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();

    }

    public void checkVersion() {

        Query myTopPostsQuery = mDatabaseRef.child("personnel_ver");
        myTopPostsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.i("info","checkVersion ");
                String newVersion = dataSnapshot.getValue().toString();
                if (!getCurrentVersion().equals(newVersion)) {
                    updateCurrentVersion(newVersion);
                    refreshPersonnels();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void refreshPersonnels() {
        Log.i("info","refreshPersonnels");
        mDatabaseRef.child("personnel_store").child("store").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                storeDb.cleanPersonnel(sqdb);

                totalC = 0;
                teacherC = 0;
                workerC = 0;
                volunteerC = 0;
                others = 0;

                for (DataSnapshot teachersSnapshot : dataSnapshot.getChildren()) {
                    Personnel personnel = teachersSnapshot.getValue(Personnel.class);

                    String info = personnel.getInfo();
                    String idNumber = personnel.getId_number().toLowerCase();
                    String cardNumber = personnel.getCard_number().toLowerCase();
                    String photo = personnel.getPhoto();
                    String type = personnel.getType();

                    totalC++;

                    if (type.equals("teacher")) teacherC++;
                    else if (type.equals("worker")) workerC++;
                    else if (type.equals("volunteer")) volunteerC++;
                    else if (type.equals("others")) others++;
                    else if (type.contains("guest")) others++;

                    ContentValues personnelValue = new ContentValues();
                    personnelValue.put(COLUMN_INFO, info);
                    personnelValue.put(COLUMN_ID_NUMBER, idNumber);
                    personnelValue.put(COLUMN_CARD_NUMBER, cardNumber);
                    personnelValue.put(COLUMN_PHOTO, photo);
                    personnelValue.put(COLUMN_TYPE, type);

                    sqdb.insert(TABLE_PERSONNEL, null, personnelValue);
                }

                updateDb();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void updateDb() {
        storeDb.cleanPersonnelCount(sqdb);

        ContentValues personnelValueCount = new ContentValues();
        personnelValueCount.put(COLUMN_TOTAL_COUNT, totalC);
        personnelValueCount.put(COLUMN_TEACHER_COUNT, teacherC);
        personnelValueCount.put(COLUMN_WORKER_COUNT, workerC);
        personnelValueCount.put(COLUMN_VOLUNTEER_COUNT, volunteerC);
        personnelValueCount.put(COLUMN_OTHER_COUNT, others);

        sqdb.insert(TABLE_PERSONNEL_COUNT, null, personnelValueCount);

        updatePersonsCount();
    }

    public String getCurrentVersion() {
        Cursor res = sqdb.rawQuery("SELECT personnel_ver FROM versions", null);
        res.moveToNext();

        return res.getString(0);
    }

    public void updateCurrentVersion(String newVersion) {
        ContentValues versionValues = new ContentValues();
        versionValues.put("personnel_ver", newVersion);

        sqdb.update("versions", versionValues, "personnel_ver=" + getCurrentVersion(), null);
    }
    public void manageDate() {
        dateF = new SimpleDateFormat("HH:mm");
        date = dateF.format(Calendar.getInstance().getTime());
        String time[] = date.split(":");

        //breakfast: 07:00 - 10:00
        //lunch: 12:00 - 14:30
        //dinner: 18:00 - 19:30

        int hour = Integer.parseInt(time[0]);
        int minute = Integer.parseInt(time[1]);

        if(hour>=7 && hour<=10){
            tabPos = 0;

        }else if(hour>=12 && hour<=14){
            tabPos = 1;

        }else if(hour>=16 && hour<=17){
            tabPos = 2;

        }else if(hour>=18 && hour<=19){
            tabPos = 3;

        }else if(hour>=20 && hour<=21){
            tabPos = 4;
        }

        viewPager.setCurrentItem(tabPos);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(breakfastFragment, "Завтрак");
        adapter.addFragment(lunchFragment, "Обед");
        adapter.addFragment(dinnerFragment, "Ужин");

        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(adapter);

    }
}
