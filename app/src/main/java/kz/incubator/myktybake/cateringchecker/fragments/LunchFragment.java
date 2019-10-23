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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

public class LunchFragment extends Fragment {
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
    Button btnCancel;
    int cLunchCount = 0;
    int collegeLunchEatersInt = 0;
    PMenu collegeMenu;
    String date;
    DateFormat dateF;
    TextView desc;
    Dialog dialog;
    CountDownTimer dialogShowingTimer;
    boolean entered = false;
    int errorSound;
    String firebaseDate;
    DateFormat firebaseDateFormat;
    private LayoutManager gridLayoutManager;
    ImageView imageAccess;
    int lLunchCount = 0;
    private LayoutManager linearLayoutManager;
    ArrayList<Personnel> lunchList;
    PMenu lyceumMenu;
    int lyceumStudentsC = 0;
    private FirebaseAuth mAuth;
    DatabaseReference mDatabaseRef;
    SoundPool mSoundPool;
    int pLunchCount = 0;
    PMenu personnelMenu;
    EditText selectStudent;
    SQLiteDatabase sqdb;
    StoreDatabase storeDb;
    int totalC = 0;
    int totalCount = 0;
    TextView tvDate;
    boolean typeCollege = false;
    boolean typeLyceum = false;
    boolean typePersonnel = false;
    Dialog userDescDialog;
    TextView userName;
    ImageView userPhoto;
    View view;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_lunch, container, false);
        setupViews();
        createDialogs();
        manageDate();
        collegeLunchEatersCount();
        lyceumStudentsListCount();
        setDefault();
        getDayliCountEaters();
        return this.view;
    }

    public void setupViews() {
        this.tvDate = (TextView) this.view.findViewById(R.id.textView2);
        this.storeDb = new StoreDatabase(getActivity());
        this.sqdb = this.storeDb.getWritableDatabase();
        FirebaseApp.initializeApp(getActivity());
        this.mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        this.mAuth = FirebaseAuth.getInstance();
        this.gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        this.linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView = (RecyclerView) this.view.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(this.gridLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerViewEatersList = (RecyclerView) this.view.findViewById(R.id.my_recycler_eaters_list);
        recyclerViewEatersList.setHasFixedSize(true);
        recyclerViewEatersList.setLayoutManager(this.linearLayoutManager);
        recyclerViewEatersList.setItemAnimator(new DefaultItemAnimator());
        recyclerViewEatersList.addItemDecoration(new DividerItemDecoration(getContext()));
        this.lunchList = new ArrayList<>();
        eatersAdapter = new EatersListAdapter(getActivity(), this.lunchList);
        recyclerViewEatersList.setAdapter(eatersAdapter);
        this.mSoundPool = new SoundPool(3, 3, 0);
        this.assets = getActivity().getAssets();
        this.bonAppetitSound = loadSound("bon_appetit.mp3");
        this.errorSound = loadSound("error_sound.wav");
        this.autoCompleteTextView = (AutoCompleteTextView) this.view.findViewById(R.id.autoComplete2);
        this.autoCompleteTextView.requestFocus();
        createEaterDescDilaog();
        dialogShowingTimer = new CountDownTimer(2000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                userDescDialog.dismiss();
            }
        };
        menu = new ArrayList<>();
        this.personnelMenu = new PMenu(getString(R.string.personnel_title), R.drawable.menu1, "0");
        this.collegeMenu = new PMenu(getString(R.string.college_title), R.drawable.menu1, "0");
        this.lyceumMenu = new PMenu(getString(R.string.lyceum_title), R.drawable.menu1, "0");
        menu.add(this.personnelMenu);
        menu.add(this.collegeMenu);
        menu.add(this.lyceumMenu);
        adapter = new PMenuListAdapter(menu);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), recyclerView, new OnItemClickListener() {
            public void onItemClick(View view, int position) {
            }

            public void onLongItemClick(View view, int position) {
            }
        }));
        this.autoCompleteTextView.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (checkInternetConnection() && event.getAction() == 0 && keyCode == 66) {
                    String card_number = autoCompleteTextView.getText().toString().toLowerCase();
                    setHideSoftKeyboard(autoCompleteTextView);
                    if (card_number.length() > 0) {
                        String id_number = getIdNumber(card_number).toLowerCase();
                        if (!checkInList(id_number)) {
                            if (typePersonnel) {
                                checkInVolunteerList(id_number);
                            } else if (typeLyceum) {
                                checkInLyceumList(id_number);
                            } else if (typeCollege) {
                                checkCollegeLunchEater(id_number);
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

    public void createDialogs() {
        this.dialog = new Dialog(getActivity());
        this.dialog.setContentView(R.layout.dialog_buy_one_day_food);
        this.selectStudent = (EditText) this.dialog.findViewById(R.id.selectStudent);
        this.btnCancel = (Button) this.dialog.findViewById(R.id.btnCancel);
        Spinner guestSpinner = (Spinner) this.dialog.findViewById(R.id.guest_spinner);
        guestSpinner.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, new String[]{"Таңғы ас", "Түскі ас", "Кешкі ас"}));
        guestSpinner.setSelection(1);
        this.selectStudent.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (checkInternetConnection() && event.getAction() == 0 && keyCode == 66) {
                    Log.i("info", selectStudent.getText().toString().toLowerCase());
                    setHideSoftKeyboard(selectStudent);
                    dialog.dismiss();
                }
                return false;
            }
        });
        this.btnCancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    public void onCreateOptionsMenu(Menu menu2, MenuInflater inflater) {
        menu2.clear();
        inflater.inflate(R.menu.buy_one_day, menu2);
        super.onCreateOptionsMenu(menu2, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != R.id.add_new_eater) {
            return super.onOptionsItemSelected(item);
        }
        this.dialog.show();
        return true;
    }

    public boolean checkInList(String id_number) {
        Iterator it = this.lunchList.iterator();
        while (it.hasNext()) {
            Personnel personnel = (Personnel) it.next();
            if (personnel.getId_number().equals(id_number)) {
                Glide.with(getActivity()).load(personnel.getPhoto()).placeholder((int) R.drawable.eater_icon3).into(this.userPhoto);
                this.imageAccess.setImageResource(R.drawable.error_icon);
                this.userName.setText(personnel.getInfo());
                this.desc.setTextColor(getResources().getColor(R.color.red));
                TextView textView = this.desc;
                StringBuilder sb = new StringBuilder();
                sb.append("Тамақ ішілген: ");
                sb.append(personnel.getTime());
                textView.setText(sb.toString());
                this.userDescDialog.show();
                this.dialogShowingTimer.start();
                playSound(this.errorSound);
                return true;
            }
        }
        return false;
    }

    public void checkCollegeLunchEater(final String id_number) {
        this.mDatabaseRef.child("f_time").child("lunch").child("college").child(this.firebaseDate).child(id_number).addListenerForSingleValueEvent(new ValueEventListener() {
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
                        mDatabaseRef.child("days").child("college").child(firebaseDate).child("lunch").child(id_number).setValue(getNowTime());
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

    public void checkInLyceumList(String id_number) {
        String[] params = {id_number};
        SQLiteDatabase sQLiteDatabase = this.sqdb;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ");
        sb.append(this.TABLE_LYCEUM_STUDENTS);
        sb.append(" WHERE id_number=?");
        Cursor cursor = sQLiteDatabase.rawQuery(sb.toString(), params);
        if (cursor == null || cursor.getCount() <= 0) {
            this.userPhoto.setImageResource(R.drawable.t_icon);
            this.userName.setText("Қолданушы\nЛицей тізімінен табылған жоқ.");
            this.imageAccess.setImageResource(R.drawable.error_icon);
            this.desc.setTextColor(getResources().getColor(R.color.red));
            this.desc.setText("Рұқсат жоқ.");
            this.userDescDialog.show();
            this.dialogShowingTimer.start();
            playSound(this.errorSound);
            return;
        }
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
        Glide.with(getActivity()).load(personnel.getPhoto()).placeholder((int) R.drawable.eater_icon3).into(this.userPhoto);
        this.imageAccess.setImageResource(R.drawable.success_icon);
        this.userName.setText(personnel.getInfo());
        this.desc.setTextColor(getResources().getColor(R.color.green));
        this.desc.setText("Рұқсат бар");
        this.mDatabaseRef.child("days").child("lyceum").child(this.firebaseDate).child("lunch").child(id_number).setValue(getNowTime());
        playSound(this.bonAppetitSound);
        this.userDescDialog.show();
        this.dialogShowingTimer.start();
    }

    public void checkInVolunteerList(String id_number) {
        String[] params = {id_number};
        SQLiteDatabase sQLiteDatabase = this.sqdb;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ");
        sb.append(this.TABLE_PERSONNEL);
        sb.append(" WHERE id_number=?");
        Cursor cursor = sQLiteDatabase.rawQuery(sb.toString(), params);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToNext();
            String info = cursor.getString(0);
            String photo = cursor.getString(3);
            String string = cursor.getString(4);
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
            Glide.with(getActivity()).load(personnel.getPhoto()).placeholder((int) R.drawable.eater_icon3).into(this.userPhoto);
            this.imageAccess.setImageResource(R.drawable.success_icon);
            this.userName.setText(personnel.getInfo());
            if (photo.contains("not")) {
                this.imageAccess.setImageResource(R.drawable.error_icon);
                this.desc.setTextColor(getResources().getColor(R.color.red));
                this.desc.setText("Рұқсат жоқ.");
                playSound(this.errorSound);
            } else {
                this.desc.setTextColor(getResources().getColor(R.color.green));
                this.desc.setText("Рұқсат бар");
                this.mDatabaseRef.child("days").child("personnel").child(this.firebaseDate).child("lunch").child(id_number).setValue(getNowTime());
                playSound(this.bonAppetitSound);
            }
            this.userDescDialog.show();
            this.dialogShowingTimer.start();
        }
    }

    public String getIdNumber(String card_number) {
        String id_number = "";
        this.typePersonnel = false;
        this.typeCollege = false;
        this.typeLyceum = false;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ");
        sb.append(this.TABLE_PERSONNEL);
        sb.append(" WHERE card_number LIKE '%");
        sb.append(card_number);
        sb.append("%'");
        Cursor cursor = this.sqdb.rawQuery(sb.toString(), null);
        if (cursor == null || cursor.getCount() <= 0) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("SELECT * FROM ");
            sb2.append(this.TABLE_COLLEGE_STUDENTS);
            sb2.append(" WHERE card_number LIKE '%");
            sb2.append(card_number);
            sb2.append("%'");
            Cursor cursor2 = this.sqdb.rawQuery(sb2.toString(), null);
            if (cursor2 == null || cursor2.getCount() <= 0) {
                StringBuilder sb3 = new StringBuilder();
                sb3.append("SELECT * FROM ");
                sb3.append(this.TABLE_LYCEUM_STUDENTS);
                sb3.append(" WHERE card_number LIKE '%");
                sb3.append(card_number);
                sb3.append("%'");
                Cursor cursor3 = this.sqdb.rawQuery(sb3.toString(), null);
                if (cursor3 == null || cursor3.getCount() <= 0) {
                    Toast.makeText(getActivity(), "Can not find Id number", Toast.LENGTH_SHORT).show();
                    return id_number;
                }
                cursor3.moveToNext();
                String id_number2 = cursor3.getString(2);
                this.typeLyceum = true;
                return id_number2;
            }
            cursor2.moveToNext();
            String id_number3 = cursor2.getString(2);
            this.typeCollege = true;
            return id_number3;
        }
        cursor.moveToNext();
        String id_number4 = cursor.getString(1);
        this.typePersonnel = true;
        return id_number4;
    }

    public void collegeLunchEatersCount() {
        this.mDatabaseRef.child("f_time").child("lunch").child("college").child(this.firebaseDate).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    collegeLunchEatersInt = (int) dataSnapshot.getChildrenCount();
                    PMenu pMenu = collegeMenu;
                    StringBuilder sb = new StringBuilder();
                    sb.append(cLunchCount);
                    sb.append(" / ");
                    sb.append(collegeLunchEatersInt);
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
                String str = TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("onChildChanged:");
                sb.append(dataSnapshot.getKey());
                Log.d(str, sb.toString());
                pLunchCount++;
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
                String str = TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("onChildRemoved:");
                sb.append(dataSnapshot.getKey());
                Log.d(str, sb.toString());
                pLunchCount--;
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
                String str = TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("onChildChanged:");
                sb.append(dataSnapshot.getKey());
                Log.d(str, sb.toString());
                cLunchCount++;
                addEaterToList(TABLE_COLLEGE_STUDENTS, dataSnapshot.getKey(), dataSnapshot.getValue().toString().toLowerCase());
                PMenu pMenu = collegeMenu;
                StringBuilder sb2 = new StringBuilder();
                sb2.append(cLunchCount);
                sb2.append(" / ");
                sb2.append(collegeLunchEatersInt);
                pMenu.setCount(sb2.toString());
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
                cLunchCount--;
                delEaterFromList(TABLE_COLLEGE_STUDENTS, dataSnapshot.getKey(), dataSnapshot.getValue().toString().toLowerCase());
                PMenu pMenu = collegeMenu;
                StringBuilder sb = new StringBuilder();
                sb.append(cLunchCount);
                sb.append(" / ");
                sb.append(collegeLunchEatersInt);
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
                lLunchCount++;
                addEaterToList(TABLE_LYCEUM_STUDENTS, dataSnapshot.getKey(), dataSnapshot.getValue().toString().toLowerCase());
                PMenu pMenu = lyceumMenu;
                StringBuilder sb = new StringBuilder();
                sb.append(lLunchCount);
                sb.append(" / ");
                sb.append(lyceumStudentsC);
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
                lLunchCount--;
                delEaterFromList(TABLE_LYCEUM_STUDENTS, dataSnapshot.getKey(), dataSnapshot.getValue().toString().toLowerCase());
                PMenu pMenu = lyceumMenu;
                StringBuilder sb = new StringBuilder();
                sb.append(lLunchCount);
                sb.append(" / ");
                sb.append(lyceumStudentsC);
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
        this.mDatabaseRef.child("days").child("personnel").child(this.firebaseDate).child("lunch").addChildEventListener(childEventListenerPersonnel);
        this.mDatabaseRef.child("days").child("college").child(this.firebaseDate).child("lunch").addChildEventListener(childEventListenerCollege);
        this.mDatabaseRef.child("days").child("lyceum").child(this.firebaseDate).child("lunch").addChildEventListener(childEventListenerLyceum);
    }

    public void setDefault() {
        PMenu pMenu = this.personnelMenu;
        StringBuilder sb = new StringBuilder();
        sb.append(this.pLunchCount);
        sb.append(" / ");
        sb.append(totalCount());
        pMenu.setCount(sb.toString());
        PMenu pMenu2 = this.collegeMenu;
        StringBuilder sb2 = new StringBuilder();
        sb2.append(this.cLunchCount);
        sb2.append(" / ");
        sb2.append(this.collegeLunchEatersInt);
        pMenu2.setCount(sb2.toString());
        PMenu pMenu3 = this.lyceumMenu;
        StringBuilder sb3 = new StringBuilder();
        sb3.append(this.lLunchCount);
        sb3.append(" / ");
        sb3.append(this.lyceumStudentsC);
        pMenu3.setCount(sb3.toString());
        adapter.notifyDataSetChanged();
    }

    public void updateViews() {
        PMenu pMenu = this.personnelMenu;
        StringBuilder sb = new StringBuilder();
        sb.append(this.pLunchCount);
        sb.append(" / ");
        sb.append(this.totalCount);
        pMenu.setCount(sb.toString());
        adapter.notifyDataSetChanged();
    }

    public void addEaterToList(String table_name, String id_number, String time) {
        String[] params = {id_number};
        SQLiteDatabase sQLiteDatabase = this.sqdb;
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
            this.lunchList.add(personnel);
            eatersAdapter.notifyDataSetChanged();
        }
    }

    public void delEaterFromList(String table_name, String id_number, String time) {
        String[] params = {id_number};
        SQLiteDatabase sQLiteDatabase = this.sqdb;
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
            Iterator it = this.lunchList.iterator();
            while (it.hasNext()) {
                Personnel personnels = (Personnel) it.next();
                if (personnels.getId_number().equals(personnel.getId_number())) {
                    this.lunchList.remove(personnels);
                    eatersAdapter.notifyDataSetChanged();
                    return;
                }
            }
        }
    }

    public int totalCount() {
        Cursor res = this.sqdb.rawQuery("SELECT totalC FROM personnel_store_count", null);
        this.totalC = 0;
        if (res != null && res.getCount() > 0) {
            res.moveToNext();
            this.totalC = Integer.parseInt(res.getString(0));
        }
        this.totalCount = this.totalC;
        return this.totalC;
    }

    public void lyceumStudentsListCount() {
        SQLiteDatabase sQLiteDatabase = this.sqdb;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ");
        sb.append(this.TABLE_LYCEUM_STUDENTS);
        Cursor res = sQLiteDatabase.rawQuery(sb.toString(), null);
        this.lyceumStudentsC = 0;
        if (res != null && res.getCount() > 0) {
            while (res.moveToNext()) {
                this.lyceumStudentsC++;
            }
        }
        PMenu pMenu = this.lyceumMenu;
        StringBuilder sb2 = new StringBuilder();
        sb2.append(this.lLunchCount);
        sb2.append(" / ");
        sb2.append(this.lyceumStudentsC);
        pMenu.setCount(sb2.toString());
    }

    public String getNowTime() {
        return new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
    }

    public void manageDate() {
        this.dateF = new SimpleDateFormat("EEEE, dd MMMM yyyy");
        this.date = this.dateF.format(Calendar.getInstance().getTime());
        this.firebaseDateFormat = new SimpleDateFormat("dd_MM_yyyy");
        this.firebaseDate = this.firebaseDateFormat.format(Calendar.getInstance().getTime());
        this.tvDate.setText(this.date);
    }

    private void setHideSoftKeyboard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public void createEaterDescDilaog() {
        this.userDescDialog = new Dialog(getActivity());
        this.userDescDialog.requestWindowFeature(1);
        this.userDescDialog.setContentView(R.layout.dialog_desc_eater);
        this.userPhoto = (ImageView) this.userDescDialog.findViewById(R.id.userPhoto);
        this.imageAccess = (ImageView) this.userDescDialog.findViewById(R.id.imageAccess);
        this.userName = (TextView) this.userDescDialog.findViewById(R.id.userName);
        this.desc = (TextView) this.userDescDialog.findViewById(R.id.desc);
    }

    public void connectListenerToUpdateViews() {
        this.mDatabaseRef.child(StoreDatabase.COLUMN_PERSONNEL_VER).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                updateViews();
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /* access modifiers changed from: protected */
    public void playSound(int sound) {
        if (sound > 0) {
            this.mSoundPool.play(sound, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    private int loadSound(String fileName) {
        try {
            return this.mSoundPool.load(this.assets.openFd(fileName), 1);
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

    /* access modifiers changed from: private */
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
