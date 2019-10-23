package kz.incubator.myktybake.cateringchecker.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import kz.incubator.myktybake.cateringchecker.R;
import kz.incubator.myktybake.cateringchecker.StoreDatabase;
import kz.incubator.myktybake.cateringchecker.adapters.DividerItemDecoration;
import kz.incubator.myktybake.cateringchecker.adapters.EatersListAdapter;
import kz.incubator.myktybake.cateringchecker.adapters.PMenuListAdapter;
import kz.incubator.myktybake.cateringchecker.adapters.RecyclerItemClickListener;
import kz.incubator.myktybake.cateringchecker.adapters.RecyclerItemClickListener.OnItemClickListener;
import kz.incubator.myktybake.cateringchecker.module.PMenu;
import kz.incubator.myktybake.cateringchecker.module.Personnel;

public class DinnerFragment extends Fragment {
    public static Adapter adapter;
    private static Adapter eatersAdapter;
    private static ArrayList<PMenu> menu;
    private static RecyclerView recyclerView;
    private static RecyclerView recyclerViewEatersList;
    String TABLE_COLLEGE_STUDENTS = StoreDatabase.TABLE_COLLEGE_STUDENTS;
    String TABLE_LYCEUM_STUDENTS = StoreDatabase.TABLE_LYCEUM_STUDENTS;
    String TABLE_PERSONNEL = StoreDatabase.TABLE_PERSONNEL;
    String TAG = FirebaseAuthProvider.PROVIDER_ID;
    AssetManager assets;
    AutoCompleteTextView autoCompleteTextView;
    int bonAppetitSound;
    int cBreakfastCount = 0;
    int collegeBreakfastEatersInt = 0;
    PMenu collegeMenu;
    String date;
    DateFormat dateF;
    TextView desc;
    CountDownTimer dialogShowingTimer;
    ArrayList<Personnel> dinnerList;
    int errorSound;
    String firebaseDate;
    DateFormat firebaseDateFormat;
    private LayoutManager gridLayoutManager;
    ImageView imageAccess;
    int lDinnerCount = 0;
    private LayoutManager linearLayoutManager;
    int lyceumBreakfastEatersInt = 0;
    PMenu lyceumMenu;
    DatabaseReference mDatabaseRef;
    SoundPool mSoundPool;
    int pBreakfastCount = 0;
    PMenu personnelMenu;
    SQLiteDatabase sqdb;
    StoreDatabase storeDb;
    TextView tvDate;
    boolean typeCollege = false;
    boolean typeLyceum = false;
    boolean typePersonnel = false;
    Dialog userDescDialog;
    TextView userName;
    ImageView userPhoto;
    View view;
    int volunteerC = 0;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_dinner, container, false);
        setupViews();
        manageDate();
        collegeBreakfastEatersCount();
        lyceumBreakfastEatersCount();
        setDefault();
        getDayliCountEaters();
        return view;
    }

    public void setupViews() {
        tvDate = (TextView) view.findViewById(R.id.textView2);
        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();
        FirebaseApp.initializeApp(getActivity());
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerViewEatersList = (RecyclerView) view.findViewById(R.id.my_recycler_eaters_list);
        recyclerViewEatersList.setHasFixedSize(true);
        recyclerViewEatersList.setLayoutManager(linearLayoutManager);
        recyclerViewEatersList.setItemAnimator(new DefaultItemAnimator());
        recyclerViewEatersList.addItemDecoration(new DividerItemDecoration(getContext()));
        dinnerList = new ArrayList<>();
        eatersAdapter = new EatersListAdapter(getActivity(), dinnerList);
        recyclerViewEatersList.setAdapter(eatersAdapter);
        mSoundPool = new SoundPool(3, 3, 0);
        assets = getActivity().getAssets();
        bonAppetitSound = loadSound("bon_appetit.mp3");
        errorSound = loadSound("error_sound.wav");
        autoCompleteTextView = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextView);
        autoCompleteTextView.requestFocus();
        createEaterDescDilaog();
        dialogShowingTimer = new CountDownTimer(2000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                userDescDialog.dismiss();
            }
        };
        menu = new ArrayList<>();
        personnelMenu = new PMenu(getString(R.string.personnel_title), R.drawable.menu1, "0");
        collegeMenu = new PMenu(getString(R.string.college_title), R.drawable.menu1, "0");
        lyceumMenu = new PMenu(getString(R.string.lyceum_title), R.drawable.menu1, "0");
        menu.add(personnelMenu);
        menu.add(collegeMenu);
        menu.add(lyceumMenu);
        adapter = new PMenuListAdapter(menu);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), recyclerView, new OnItemClickListener() {
            public void onItemClick(View view, int position) {
            }

            public void onLongItemClick(View view, int position) {
            }
        }));
        autoCompleteTextView.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (checkInternetConnection() && event.getAction() == 0 && keyCode == 66) {
                    String card_number = autoCompleteTextView.getText().toString().toLowerCase();
                    setHideSoftKeyboard(autoCompleteTextView);
                    if (card_number.length() > 0) {
                        String id_number = getIdNumber(card_number).toLowerCase();
                        if (!checkInList(id_number)) {
                            if (typePersonnel) {
                                checkInVolunteerList(id_number);
                            } else if (typeCollege) {
                                checkCollegeBreakfastEater(id_number);
                            } else if (typeLyceum) {
                                checkLyceumBreakfastEater(id_number);
                            }
                        }
                        autoCompleteTextView.setText("");
                        autoCompleteTextView.clearFocus();
                        autoCompleteTextView.requestFocus();
                        autoCompleteTextView.setCursorVisible(true);
                        return true;
                    }
                    System.out.println("id_number length is Zero ");
                }
                return false;
            }
        });
    }

    public boolean checkInList(String id_number) {
        Iterator it = dinnerList.iterator();
        while (it.hasNext()) {
            Personnel personnel = (Personnel) it.next();
            if (personnel.getId_number().equals(id_number)) {
                Glide.with(getActivity()).load(personnel.getPhoto()).placeholder((int) R.drawable.eater_icon3).into(userPhoto);
                imageAccess.setImageResource(R.drawable.error_icon);
                userName.setText(personnel.getInfo());
                desc.setTextColor(getResources().getColor(R.color.red));
                TextView textView = desc;
                StringBuilder sb = new StringBuilder();
                sb.append("Тамақ ішілген: ");
                sb.append(personnel.getTime());
                textView.setText(sb.toString());
                userDescDialog.show();
                dialogShowingTimer.start();
                playSound(errorSound);
                return true;
            }
        }
        return false;
    }

    public void checkCollegeBreakfastEater(final String id_number) {
        mDatabaseRef.child("f_time").child("dinner").child("college").child(firebaseDate).child(id_number).addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                String[] params = {id_number};
                SQLiteDatabase sQLiteDatabase = sqdb;
                StringBuilder sb = new StringBuilder();
                sb.append("SELECT * FROM ");
                sb.append(TABLE_COLLEGE_STUDENTS);
                sb.append(" WHERE id_number=?");
                Cursor cursor = sQLiteDatabase.rawQuery(sb.toString(), params);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToNext();
                    String info = cursor.getString(0);
                    String photo = cursor.getString(3);
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("");
                    sb2.append(info);
                    String sb3 = sb2.toString();
                    StringBuilder sb4 = new StringBuilder();
                    sb4.append("");
                    sb4.append(id_number);
                    StringBuilder sb5 = new StringBuilder();
                    sb5.append("");
                    sb5.append(photo);
                    StringBuilder sb6 = new StringBuilder();
                    sb6.append("");
                    sb6.append(getNowTime());
                    Personnel personnel = new Personnel(sb3, sb4.toString(), " ", sb5.toString(), " ", sb6.toString());
                    Glide.with(getActivity()).load(personnel.getPhoto()).placeholder((int) R.drawable.eater_icon3).into(userPhoto);
                    imageAccess.setImageResource(R.drawable.success_icon);
                    userName.setText(personnel.getInfo());
                    if (dataSnapshot.exists()) {
                        desc.setTextColor(getResources().getColor(R.color.green));
                        desc.setText("Рұқсат бар");
                        mDatabaseRef.child("days").child("college").child(firebaseDate).child("dinner").child(id_number).setValue(getNowTime());
                        playSound(bonAppetitSound);
                    } else {
                        imageAccess.setImageResource(R.drawable.error_icon);
                        desc.setTextColor(getResources().getColor(R.color.red));
                        desc.setText("Рұқсат жоқ");
                        playSound(errorSound);
                    }
                    userDescDialog.show();
                    dialogShowingTimer.start();
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void checkLyceumBreakfastEater(final String id_number) {
        mDatabaseRef.child("f_time").child("dinner").child("lyceum").child(firebaseDate).child(id_number).addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                String[] params = {id_number};
                SQLiteDatabase sQLiteDatabase = sqdb;
                StringBuilder sb = new StringBuilder();
                sb.append("SELECT * FROM ");
                sb.append(TABLE_LYCEUM_STUDENTS);
                sb.append(" WHERE id_number=?");
                Cursor cursor = sQLiteDatabase.rawQuery(sb.toString(), params);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToNext();
                    String info = cursor.getString(0);
                    String photo = cursor.getString(3);
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("");
                    sb2.append(info);
                    String sb3 = sb2.toString();
                    StringBuilder sb4 = new StringBuilder();
                    sb4.append("");
                    sb4.append(id_number);
                    StringBuilder sb5 = new StringBuilder();
                    sb5.append("");
                    sb5.append(photo);
                    StringBuilder sb6 = new StringBuilder();
                    sb6.append("");
                    sb6.append(getNowTime());
                    Personnel personnel = new Personnel(sb3, sb4.toString(), " ", sb5.toString(), " ", sb6.toString());
                    Glide.with(getActivity()).load(personnel.getPhoto()).placeholder((int) R.drawable.eater_icon3).into(userPhoto);
                    imageAccess.setImageResource(R.drawable.success_icon);
                    userName.setText(personnel.getInfo());
                    if (dataSnapshot.exists()) {
                        desc.setTextColor(getResources().getColor(R.color.green));
                        desc.setText("Рұқсат бар");
                        mDatabaseRef.child("days").child("lyceum").child(firebaseDate).child("dinner").child(id_number).setValue(getNowTime());
                        playSound(bonAppetitSound);
                    } else {
                        imageAccess.setImageResource(R.drawable.error_icon);
                        desc.setTextColor(getResources().getColor(R.color.red));
                        desc.setText("Рұқсат жоқ");
                        playSound(errorSound);
                    }
                    userDescDialog.show();
                    dialogShowingTimer.start();
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void checkInVolunteerList(String id_number) {
        String[] params = {id_number};
        SQLiteDatabase sQLiteDatabase = sqdb;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ");
        sb.append(TABLE_PERSONNEL);
        sb.append(" WHERE id_number=?");
        Cursor cursor = sQLiteDatabase.rawQuery(sb.toString(), params);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToNext();
            String info = cursor.getString(0);
            String photo = cursor.getString(3);
            String type = cursor.getString(4);
            StringBuilder sb2 = new StringBuilder();
            sb2.append("");
            sb2.append(info);
            String sb3 = sb2.toString();
            StringBuilder sb4 = new StringBuilder();
            sb4.append("");
            sb4.append(id_number);
            StringBuilder sb5 = new StringBuilder();
            sb5.append("");
            sb5.append(photo);
            StringBuilder sb6 = new StringBuilder();
            sb6.append("");
            sb6.append(getNowTime());
            Personnel personnel = new Personnel(sb3, sb4.toString(), " ", sb5.toString(), " ", sb6.toString());
            Glide.with(getActivity()).load(personnel.getPhoto()).placeholder((int) R.drawable.eater_icon3).into(userPhoto);
            imageAccess.setImageResource(R.drawable.success_icon);
            userName.setText(personnel.getInfo());
            if (!type.equals("volunteer") || photo.contains("not")) {
                imageAccess.setImageResource(R.drawable.error_icon);
                desc.setTextColor(getResources().getColor(R.color.red));
                desc.setText("Рұқсат жоқ");
                playSound(errorSound);
            } else {
                desc.setTextColor(getResources().getColor(R.color.green));
                desc.setText("Рұқсат бар");
                mDatabaseRef.child("days").child("personnel").child(firebaseDate).child("dinner").child(id_number).setValue(getNowTime());
                playSound(bonAppetitSound);
            }
            userDescDialog.show();
            dialogShowingTimer.start();
        }
    }

    public String getIdNumber(String card_number) {
        String id_number = "";
        typePersonnel = false;
        typeCollege = false;
        typeLyceum = false;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ");
        sb.append(TABLE_PERSONNEL);
        sb.append(" WHERE card_number LIKE '%");
        sb.append(card_number);
        sb.append("%'");
        Cursor cursor = sqdb.rawQuery(sb.toString(), null);
        if (cursor == null || cursor.getCount() <= 0) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("SELECT * FROM ");
            sb2.append(TABLE_COLLEGE_STUDENTS);
            sb2.append(" WHERE card_number LIKE '%");
            sb2.append(card_number);
            sb2.append("%'");
            Cursor cursor2 = sqdb.rawQuery(sb2.toString(), null);
            if (cursor2 == null || cursor2.getCount() <= 0) {
                StringBuilder sb3 = new StringBuilder();
                sb3.append("SELECT * FROM ");
                sb3.append(TABLE_LYCEUM_STUDENTS);
                sb3.append(" WHERE card_number LIKE '%");
                sb3.append(card_number);
                sb3.append("%'");
                Cursor cursor3 = sqdb.rawQuery(sb3.toString(), null);
                if (cursor3 == null || cursor3.getCount() <= 0) {
                    Toast.makeText(getActivity(), "Can not find Id number", 0).show();
                    return id_number;
                }
                cursor3.moveToNext();
                String id_number2 = cursor3.getString(2);
                typeLyceum = true;
                return id_number2;
            }
            cursor2.moveToNext();
            String id_number3 = cursor2.getString(2);
            typeCollege = true;
            return id_number3;
        }
        cursor.moveToNext();
        String id_number4 = cursor.getString(1);
        typePersonnel = true;
        return id_number4;
    }

    public void collegeBreakfastEatersCount() {
        mDatabaseRef.child("f_time").child("dinner").child("college").child(firebaseDate).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    collegeBreakfastEatersInt = (int) dataSnapshot.getChildrenCount();
                    PMenu pMenu = collegeMenu;
                    StringBuilder sb = new StringBuilder();
                    sb.append(cBreakfastCount);
                    sb.append(" / ");
                    sb.append(collegeBreakfastEatersInt);
                    pMenu.setCount(sb.toString());
                    adapter.notifyDataSetChanged();
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void lyceumBreakfastEatersCount() {
        mDatabaseRef.child("f_time").child("dinner").child("lyceum").child(firebaseDate).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    lyceumBreakfastEatersInt = (int) dataSnapshot.getChildrenCount();
                    PMenu pMenu = lyceumMenu;
                    StringBuilder sb = new StringBuilder();
                    sb.append(lDinnerCount);
                    sb.append(" / ");
                    sb.append(lyceumBreakfastEatersInt);
                    pMenu.setCount(sb.toString());
                    adapter.notifyDataSetChanged();
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void getDayliCountEaters() {
        ChildEventListener childEventListenerPersonnel = new ChildEventListener() {
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                pBreakfastCount++;
                addEaterToList(TABLE_PERSONNEL, dataSnapshot.getKey(), dataSnapshot.getValue().toString().toLowerCase());
                updateViews();
            }

            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                String str = TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("onChildChanged:");
                sb.append(dataSnapshot.getKey());
                Log.d(str, sb.toString());
            }

            public void onChildRemoved(DataSnapshot dataSnapshot) {
                pBreakfastCount--;
                delEaterFromList(TABLE_PERSONNEL, dataSnapshot.getKey(), dataSnapshot.getValue().toString().toLowerCase());
                updateViews();
            }

            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                String str = TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("onChildMoved:");
                sb.append(dataSnapshot.getKey());
                Log.d(str, sb.toString());
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        };
        ChildEventListener childEventListenerCollege = new ChildEventListener() {
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                cBreakfastCount++;
                addEaterToList(TABLE_COLLEGE_STUDENTS, dataSnapshot.getKey(), dataSnapshot.getValue().toString().toLowerCase());
                PMenu pMenu = collegeMenu;
                StringBuilder sb = new StringBuilder();
                sb.append(cBreakfastCount);
                sb.append(" / ");
                sb.append(collegeBreakfastEatersInt);
                pMenu.setCount(sb.toString());
                adapter.notifyDataSetChanged();
            }

            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                String str = TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("onChildChanged:");
                sb.append(dataSnapshot.getKey());
                Log.d(str, sb.toString());
            }

            public void onChildRemoved(DataSnapshot dataSnapshot) {
                cBreakfastCount--;
                delEaterFromList(TABLE_COLLEGE_STUDENTS, dataSnapshot.getKey(), dataSnapshot.getValue().toString().toLowerCase());
                PMenu pMenu = collegeMenu;
                StringBuilder sb = new StringBuilder();
                sb.append(cBreakfastCount);
                sb.append(" / ");
                sb.append(collegeBreakfastEatersInt);
                pMenu.setCount(sb.toString());
                adapter.notifyDataSetChanged();
            }

            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                String str = TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("onChildMoved:");
                sb.append(dataSnapshot.getKey());
                Log.d(str, sb.toString());
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        };
        ChildEventListener childEventListenerLyceum = new ChildEventListener() {
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                lDinnerCount++;
                addEaterToList(TABLE_LYCEUM_STUDENTS, dataSnapshot.getKey(), dataSnapshot.getValue().toString().toLowerCase());
                PMenu pMenu = lyceumMenu;
                StringBuilder sb = new StringBuilder();
                sb.append(lDinnerCount);
                sb.append(" / ");
                sb.append(lyceumBreakfastEatersInt);
                pMenu.setCount(sb.toString());
                adapter.notifyDataSetChanged();
            }

            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                String str = TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("onChildChanged:");
                sb.append(dataSnapshot.getKey());
                Log.d(str, sb.toString());
            }

            public void onChildRemoved(DataSnapshot dataSnapshot) {
                lDinnerCount--;
                delEaterFromList(TABLE_LYCEUM_STUDENTS, dataSnapshot.getKey(), dataSnapshot.getValue().toString().toLowerCase());
                PMenu pMenu = lyceumMenu;
                StringBuilder sb = new StringBuilder();
                sb.append(lDinnerCount);
                sb.append(" / ");
                sb.append(lyceumBreakfastEatersInt);
                pMenu.setCount(sb.toString());
                adapter.notifyDataSetChanged();
            }

            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                String str = TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("onChildMoved:");
                sb.append(dataSnapshot.getKey());
                Log.d(str, sb.toString());
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mDatabaseRef.child("days").child("personnel").child(firebaseDate).child("dinner").addChildEventListener(childEventListenerPersonnel);
        mDatabaseRef.child("days").child("college").child(firebaseDate).child("dinner").addChildEventListener(childEventListenerCollege);
        mDatabaseRef.child("days").child("lyceum").child(firebaseDate).child("dinner").addChildEventListener(childEventListenerLyceum);
    }

    public void setDefault() {
        PMenu pMenu = personnelMenu;
        StringBuilder sb = new StringBuilder();
        sb.append(pBreakfastCount);
        sb.append(" / ");
        sb.append(tarbiewiListCount());
        pMenu.setCount(sb.toString());
        PMenu pMenu2 = collegeMenu;
        StringBuilder sb2 = new StringBuilder();
        sb2.append(cBreakfastCount);
        sb2.append(" / ");
        sb2.append(collegeBreakfastEatersInt);
        pMenu2.setCount(sb2.toString());
        PMenu pMenu3 = lyceumMenu;
        StringBuilder sb3 = new StringBuilder();
        sb3.append(lDinnerCount);
        sb3.append(" / ");
        sb3.append(lyceumBreakfastEatersInt);
        pMenu3.setCount(sb3.toString());
        adapter.notifyDataSetChanged();
    }

    public void updateViews() {
        PMenu pMenu = personnelMenu;
        StringBuilder sb = new StringBuilder();
        sb.append(pBreakfastCount);
        sb.append(" / ");
        sb.append(volunteerC);
        pMenu.setCount(sb.toString());
        adapter.notifyDataSetChanged();
    }

    public void addEaterToList(String table_name, String id_number, String time) {
        String[] params = {id_number};
        SQLiteDatabase sQLiteDatabase = sqdb;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ");
        sb.append(table_name);
        sb.append(" WHERE id_number=?");
        Cursor res = sQLiteDatabase.rawQuery(sb.toString(), params);
        if (res != null && res.getCount() > 0) {
            res.moveToNext();
            String info = res.getString(0);
            String photo = res.getString(3);
            StringBuilder sb2 = new StringBuilder();
            sb2.append("");
            sb2.append(info);
            String sb3 = sb2.toString();
            StringBuilder sb4 = new StringBuilder();
            sb4.append("");
            sb4.append(id_number);
            StringBuilder sb5 = new StringBuilder();
            sb5.append("");
            sb5.append(photo);
            StringBuilder sb6 = new StringBuilder();
            sb6.append("");
            sb6.append(time);
            Personnel personnel = new Personnel(sb3, sb4.toString(), " ", sb5.toString(), " ", sb6.toString());
            dinnerList.add(personnel);
            eatersAdapter.notifyDataSetChanged();
        }
    }

    public void delEaterFromList(String table_name, String id_number, String time) {
        String[] params = {id_number};
        SQLiteDatabase sQLiteDatabase = sqdb;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ");
        sb.append(table_name);
        sb.append(" WHERE id_number=?");
        Cursor res = sQLiteDatabase.rawQuery(sb.toString(), params);
        if (res != null && res.getCount() > 0) {
            res.moveToNext();
            String info = res.getString(0);
            String photo = res.getString(3);
            StringBuilder sb2 = new StringBuilder();
            sb2.append("");
            sb2.append(info);
            String sb3 = sb2.toString();
            StringBuilder sb4 = new StringBuilder();
            sb4.append("");
            sb4.append(id_number);
            StringBuilder sb5 = new StringBuilder();
            sb5.append("");
            sb5.append(photo);
            StringBuilder sb6 = new StringBuilder();
            sb6.append("");
            sb6.append(time);
            Personnel personnel = new Personnel(sb3, sb4.toString(), " ", sb5.toString(), " ", sb6.toString());
            Iterator it = dinnerList.iterator();
            while (it.hasNext()) {
                Personnel personnels = (Personnel) it.next();
                if (personnels.getId_number().equals(personnel.getId_number())) {
                    dinnerList.remove(personnels);
                    eatersAdapter.notifyDataSetChanged();
                    return;
                }
            }
        }
    }

    public int tarbiewiListCount() {
        Cursor res = sqdb.rawQuery("SELECT volunteerC FROM personnel_store_count", null);
        volunteerC = 0;
        if (res != null && res.getCount() > 0) {
            res.moveToNext();
            volunteerC = Integer.parseInt(res.getString(0));
        }
        return volunteerC;
    }

    public String getNowTime() {
        return new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
    }

    public void manageDate() {
        dateF = new SimpleDateFormat("EEEE, dd MMMM yyyy");
        date = dateF.format(Calendar.getInstance().getTime());
        firebaseDateFormat = new SimpleDateFormat("dd_MM_yyyy");
        firebaseDate = firebaseDateFormat.format(Calendar.getInstance().getTime());
        tvDate.setText(date);
    }

    private void setHideSoftKeyboard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public void createEaterDescDilaog() {
        userDescDialog = new Dialog(getActivity());
        userDescDialog.requestWindowFeature(1);
        userDescDialog.setContentView(R.layout.dialog_desc_eater);
        userPhoto = (ImageView) userDescDialog.findViewById(R.id.userPhoto);
        imageAccess = (ImageView) userDescDialog.findViewById(R.id.imageAccess);
        userName = (TextView) userDescDialog.findViewById(R.id.userName);
        desc = (TextView) userDescDialog.findViewById(R.id.desc);
    }

    public void connectListenerToUpdateViews() {
        mDatabaseRef.child(StoreDatabase.COLUMN_PERSONNEL_VER).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                updateViews();
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void playSound(int sound) {
        if (sound > 0) {
            mSoundPool.play(sound, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    private int loadSound(String fileName) {
        try {
            return mSoundPool.load(assets.openFd(fileName), 1);
        } catch (IOException e) {
            e.printStackTrace();
            FragmentActivity activity = getActivity();
            StringBuilder sb = new StringBuilder();
            sb.append("Не могу загрузить файл ");
            sb.append(fileName);
            Toast.makeText(activity, sb.toString(), Toast.LENGTH_SHORT).show();
            return -1;
        }
    }

    public boolean checkInternetConnection() {
        if (isNetworkAvailable()) {
            return true;
        }
        Toast.makeText(getActivity(), getResources().getString(R.string.inetConnection), Toast.LENGTH_SHORT).show();
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }

}
