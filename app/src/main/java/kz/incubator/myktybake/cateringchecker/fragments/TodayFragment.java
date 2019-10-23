package kz.incubator.myktybake.cateringchecker.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import kz.incubator.myktybake.cateringchecker.R;
import kz.incubator.myktybake.cateringchecker.StoreDatabase;
import kz.incubator.myktybake.cateringchecker.adapters.ViewPagerAdapter;
import kz.incubator.myktybake.cateringchecker.module.Personnel;
import kz.incubator.myktybake.cateringchecker.module.Student;

public class TodayFragment extends Fragment {
    public static final String INFO = "info";
    BreakfastFragment breakfastFragment;
    String date;
    DateFormat dateF;
    DinnerFragment dinnerFragment;
    Set<String> groupList;
    LunchFragment lunchFragment;
    Set<String> lyceumGroupList;
    DatabaseReference mDatabaseRef;
    int others = 0;
    //    PoldnikFragment1 poldnikFragment1;
//    PoldnikFragment2 poldnikFragment2;
    SQLiteDatabase sqdb;
    StoreDatabase storeDb;
    Query studentsQuery;
    private TabLayout tabLayout;
    int tabPos = 0;
    int teacherC = 0;
    int totalC = 0;
    View view;
    private ViewPager viewPager;
    int volunteerC = 0;
    int workerC = 0;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_today, container, false);
        setupViews();
        manageDate();
        if (checkInetConnection()) {
            checkVersion();
            checkCollegeVersion();
            checkLyceumVersion();
        }
        return view;
    }

    public void updatePersonsCount() {
        breakfastFragment.updateViews();
        lunchFragment.updateViews();
        dinnerFragment.updateViews();
        Log.i("info", "Yahoo");
    }

    public void setupViews() {
        breakfastFragment = new BreakfastFragment();
        lunchFragment = new LunchFragment();
        dinnerFragment = new DinnerFragment();
//        poldnikFragment1 = new PoldnikFragment1();
//        poldnikFragment2 = new PoldnikFragment2();
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        getActivity().setTitle(getString(R.string.main_title));
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();
        groupList = new HashSet();
        lyceumGroupList = new HashSet();
        addListenerPersonnel();
    }

    public void addListenerPersonnel() {
        mDatabaseRef.child(StoreDatabase.TABLE_PERSONNEL).child("store").addChildEventListener(new ChildEventListener() {
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                storeDb.updatePersonnel(sqdb, (Personnel) dataSnapshot.getValue(Personnel.class));
            }

            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
            }

            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void checkCollegeVersion() {
        mDatabaseRef.child(StoreDatabase.COLUMN_STUDENTS_VER).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                String newVersion = dataSnapshot.getValue().toString();
                if (!getCollegeStudentCurVer().equals(newVersion)) {
                    updateCollegeStudentCurrentVersion(newVersion);
                    getCollegeStudents();
                    return;
                }
                getGroups();
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void getCollegeStudents() {
        storeDb.cleanCollegeStudentsTable(sqdb);
        studentsQuery = mDatabaseRef.child("groups").orderByKey();
        studentsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot groups : dataSnapshot.getChildren()) {
                        String group = groups.getKey();
                        for (DataSnapshot student : groups.getChildren()) {
                            Student student1 = (Student) student.getValue(Student.class);
                            ContentValues sValues = new ContentValues();
                            sValues.put(StoreDatabase.COLUMN_Q_ID, student1.getQr_code());
                            sValues.put("info", student1.getName());
                            sValues.put(StoreDatabase.COLUMN_ID_NUMBER, student1.getId_number());
                            sValues.put(StoreDatabase.COLUMN_CARD_NUMBER, student1.getCard_number());
                            sValues.put(StoreDatabase.COLUMN_GROUP, group);
                            sValues.put(StoreDatabase.COLUMN_PHOTO, student1.getPhoto());
                            sqdb.insert(StoreDatabase.TABLE_COLLEGE_STUDENTS, null, sValues);
                        }
                    }
                    getGroups();
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void getGroups() {
        Cursor res = sqdb.rawQuery("SELECT s_group FROM college_students_list", null);
        groupList.clear();
        while (res.moveToNext()) {
            groupList.add(res.getString(0));
        }
        StringBuilder sb = new StringBuilder();
        sb.append("groupList: ");
        sb.append(groupList);
        Log.i("student", sb.toString());
        addListenerCollege();
    }

    public void addListenerCollege() {
        for (final String group : groupList) {
            mDatabaseRef.child("groups").child(group).orderByKey().addChildEventListener(new ChildEventListener() {
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Student student1 = (Student) dataSnapshot.getValue(Student.class);
                    storeDb.updateCollegeStudent(sqdb, student1, group);
                    StringBuilder sb = new StringBuilder();
                    sb.append("student: ");
                    sb.append(student1.getName());
                    Log.i("student", sb.toString());
                }

                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                }

                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                }

                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    public void getLyceumGroup() {
        Cursor res = sqdb.rawQuery("SELECT s_group FROM lyceum_students_list", null);
        lyceumGroupList.clear();
        while (res.moveToNext()) {
            lyceumGroupList.add(res.getString(0));
        }
        StringBuilder sb = new StringBuilder();
        sb.append("lyceumGroupList: ");
        sb.append(lyceumGroupList);
        Log.i("student", sb.toString());
        addListenerLyceum();
    }

    public void addListenerLyceum() {
        for (final String group : lyceumGroupList) {
            mDatabaseRef.child("classes").child(group).orderByKey().addChildEventListener(new ChildEventListener() {
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    storeDb.updateLyceumStudent(sqdb, (Student) dataSnapshot.getValue(Student.class), group);
                }

                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                }

                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                }

                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    public String getCollegeStudentCurVer() {
        Cursor res = sqdb.rawQuery("SELECT college_student_list_ver FROM versions", null);
        res.moveToNext();
        return res.getString(0);
    }

    public void updateCollegeStudentCurrentVersion(String newVersion) {
        ContentValues versionValues = new ContentValues();
        versionValues.put(StoreDatabase.COLUMN_STUDENTS_VER, newVersion);
        SQLiteDatabase sQLiteDatabase = sqdb;
        String str = StoreDatabase.TABLE_VER;
        StringBuilder sb = new StringBuilder();
        sb.append("college_student_list_ver=");
        sb.append(getCollegeStudentCurVer());
        sQLiteDatabase.update(str, versionValues, sb.toString(), null);
    }

    public void checkLyceumVersion() {
        mDatabaseRef.child(StoreDatabase.COLUMN_LYCEUM_VER).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                String newVersion = dataSnapshot.getValue().toString();
                if (!getLyceumStudentCurVer().equals(newVersion)) {
                    updateLyceumStudentCurrentVersion(newVersion);
                    getLyceumStudents();
                    return;
                }
                getLyceumGroup();
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void getLyceumStudents() {
        storeDb.cleanLyceumStudentsTable(sqdb);
        studentsQuery = mDatabaseRef.child("classes").orderByKey();
        studentsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot classes : dataSnapshot.getChildren()) {
                    Log.i("info", "getLyceumStudents");
                    String lclass = classes.getKey();
                    for (DataSnapshot student : classes.getChildren()) {
                        Student student1 = (Student) student.getValue(Student.class);
                        ContentValues sValues = new ContentValues();
                        sValues.put("info", student1.getName());
                        sValues.put(StoreDatabase.COLUMN_ID_NUMBER, student1.getId_number());
                        sValues.put(StoreDatabase.COLUMN_CARD_NUMBER, student1.getCard_number());
                        sValues.put(StoreDatabase.COLUMN_PHOTO, student1.getPhoto());
                        sValues.put(StoreDatabase.COLUMN_Q_ID, student1.getQr_code());
                        sValues.put(StoreDatabase.COLUMN_GROUP, lclass);
                        sqdb.insert(StoreDatabase.TABLE_LYCEUM_STUDENTS, null, sValues);
                    }
                }
                getLyceumGroup();
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public String getLyceumStudentCurVer() {
        Cursor res = sqdb.rawQuery("SELECT lyceum_student_list_ver FROM versions", null);
        res.moveToNext();
        return res.getString(0);
    }

    public void updateLyceumStudentCurrentVersion(String newVersion) {
        ContentValues versionValues = new ContentValues();
        versionValues.put(StoreDatabase.COLUMN_LYCEUM_VER, newVersion);
        SQLiteDatabase sQLiteDatabase = sqdb;
        String str = StoreDatabase.TABLE_VER;
        StringBuilder sb = new StringBuilder();
        sb.append("lyceum_student_list_ver=");
        sb.append(getLyceumStudentCurVer());
        sQLiteDatabase.update(str, versionValues, sb.toString(), null);
    }

    public void checkVersion() {
        mDatabaseRef.child(StoreDatabase.COLUMN_PERSONNEL_VER).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                String newVersion = dataSnapshot.getValue().toString();
                if (!getCurrentVersion().equals(newVersion)) {
                    Log.i("version", "changed");
                    updateCurrentVersion(newVersion);
                    refreshPersonnels();
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void refreshPersonnels() {
        mDatabaseRef.child(StoreDatabase.TABLE_PERSONNEL).child("store").addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                storeDb.cleanPersonnel(sqdb);
                totalC = 0;
                teacherC = 0;
                workerC = 0;
                volunteerC = 0;
                others = 0;
                for (DataSnapshot teachersSnapshot : dataSnapshot.getChildren()) {
                    Personnel personnel = (Personnel) teachersSnapshot.getValue(Personnel.class);
                    String info = personnel.getInfo();
                    String idNumber = personnel.getId_number().toLowerCase();
                    String cardNumber = personnel.getCard_number().toLowerCase();
                    String photo = personnel.getPhoto();
                    String type = personnel.getType();
                    totalC++;
                    if (type.equals("teacher")) {
                        teacherC++;
                    } else if (type.equals("worker")) {
                        workerC++;
                    } else if (type.equals("volunteer")) {
                        volunteerC++;
                    } else if (type.equals(StoreDatabase.COLUMN_OTHER_COUNT)) {
                        others++;
                    } else if (type.contains("guest")) {
                        others++;
                    }
                    ContentValues personnelValue = new ContentValues();
                    personnelValue.put("info", info);
                    personnelValue.put(StoreDatabase.COLUMN_ID_NUMBER, idNumber);
                    personnelValue.put(StoreDatabase.COLUMN_CARD_NUMBER, cardNumber);
                    personnelValue.put(StoreDatabase.COLUMN_PHOTO, photo);
                    personnelValue.put(StoreDatabase.COLUMN_TYPE, type);
                    sqdb.insert(StoreDatabase.TABLE_PERSONNEL, null, personnelValue);
                }
                updateDb();
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void updateDb() {
        storeDb.cleanPersonnelCount(sqdb);
        ContentValues personnelValueCount = new ContentValues();
        personnelValueCount.put(StoreDatabase.COLUMN_TOTAL_COUNT, Integer.valueOf(totalC));
        personnelValueCount.put(StoreDatabase.COLUMN_TEACHER_COUNT, Integer.valueOf(teacherC));
        personnelValueCount.put(StoreDatabase.COLUMN_WORKER_COUNT, Integer.valueOf(workerC));
        personnelValueCount.put(StoreDatabase.COLUMN_VOLUNTEER_COUNT, Integer.valueOf(volunteerC));
        personnelValueCount.put(StoreDatabase.COLUMN_OTHER_COUNT, Integer.valueOf(others));
        sqdb.insert(StoreDatabase.TABLE_PERSONNEL_COUNT, null, personnelValueCount);
        updatePersonsCount();
    }

    public String getCurrentVersion() {
        Cursor res = sqdb.rawQuery("SELECT personnel_ver FROM versions", null);
        res.moveToNext();
        return res.getString(0);
    }

    public void updateCurrentVersion(String newVersion) {
        ContentValues versionValues = new ContentValues();
        versionValues.put(StoreDatabase.COLUMN_PERSONNEL_VER, newVersion);
        SQLiteDatabase sQLiteDatabase = sqdb;
        String str = StoreDatabase.TABLE_VER;
        StringBuilder sb = new StringBuilder();
        sb.append("personnel_ver=");
        sb.append(getCurrentVersion());
        sQLiteDatabase.update(str, versionValues, sb.toString(), null);
    }

    public void manageDate() {
        dateF = new SimpleDateFormat("HH:mm");
        date = dateF.format(Calendar.getInstance().getTime());
        String[] time = date.split(":");
        int hour = Integer.parseInt(time[0]);
        int parseInt = Integer.parseInt(time[1]);
        if (hour >= 7 && hour <= 10) {
            tabPos = 0;
        } else if (hour >= 12 && hour <= 14) {
            tabPos = 1;
        } else if (hour >= 18 && hour <= 20) {
            tabPos = 2;
        }
        viewPager.setCurrentItem(tabPos);
    }

    private void setupViewPager(ViewPager viewPager2) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(breakfastFragment, getString(R.string.breakfast_title));
        adapter.addFragment(lunchFragment, getString(R.string.lunch_title));
        adapter.addFragment(dinnerFragment, getString(R.string.dinner_title));
        viewPager2.setOffscreenPageLimit(5);
        viewPager2.setAdapter(adapter);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }

    public boolean checkInetConnection() {
        if (isNetworkAvailable()) {
            return true;
        }
        Toast.makeText(getActivity(), getString(R.string.inetConnection), Toast.LENGTH_SHORT).show();
        return false;
    }
}
