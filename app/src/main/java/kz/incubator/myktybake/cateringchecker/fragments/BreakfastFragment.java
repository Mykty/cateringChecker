package kz.incubator.myktybake.cateringchecker.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import kz.incubator.myktybake.cateringchecker.R;
import kz.incubator.myktybake.cateringchecker.StoreDatabase;
import kz.incubator.myktybake.cateringchecker.adapters.DividerItemDecoration;
import kz.incubator.myktybake.cateringchecker.adapters.EatersListAdapter;
import kz.incubator.myktybake.cateringchecker.module.PMenu;
import kz.incubator.myktybake.cateringchecker.adapters.PMenuListAdapter;
import kz.incubator.myktybake.cateringchecker.adapters.RecyclerItemClickListener;
import kz.incubator.myktybake.cateringchecker.module.Personnel;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static kz.incubator.myktybake.cateringchecker.StoreDatabase.COLUMN_VOLUNTEER_COUNT;
import static kz.incubator.myktybake.cateringchecker.StoreDatabase.TABLE_PERSONNEL;
import static kz.incubator.myktybake.cateringchecker.StoreDatabase.TABLE_PERSONNEL_COUNT;

public class BreakfastFragment extends Fragment {

    View view;
    private static RecyclerView recyclerView, recyclerViewEatersList;
    SQLiteDatabase sqdb;
    StoreDatabase storeDb;
    private RecyclerView.LayoutManager gridLayoutManager;
    private RecyclerView.LayoutManager linearLayoutManager;
    private static ArrayList<PMenu> menu;
    private static RecyclerView.Adapter adapter, eatersAdapter;
    PMenu personnelMenu, collegeMenu, lyceumMenu;
    TextView tvDate;
    String date, firebaseDate;
    DateFormat dateF, firebaseDateFormat;
    DatabaseReference mDatabaseRef;
    AutoCompleteTextView autoCompleteTextView;
    int volunteerC = 0;
    int breakfastCount = 0;
    ArrayList<Personnel> breakfastList;
    ImageView userPhoto, imageAccess;
    TextView userName, desc;
    Dialog userDescDialog;
    CountDownTimer dialogShowingTimer;
    int bonAppetitSound, errorSound;
    SoundPool mSoundPool;
    AssetManager assets;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_breakfast, container, false);
        setupViews();
        manageDate();
        refreshDayCount();

        return view;
    }

    public void refreshDayCount() {
        mDatabaseRef.child("days").child("personnel").child(firebaseDate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                breakfastCount = 0;
                breakfastList.clear();

                for (DataSnapshot daysSnap : dataSnapshot.getChildren()) {
                    for (DataSnapshot foodTime : daysSnap.getChildren()) {

                        if (daysSnap.getKey().equals("breakfast")) {

                            breakfastCount++;
                            String id_number = foodTime.getKey();
                            String pTime = daysSnap.child(id_number).getValue().toString().toLowerCase();

                            addPersonnelToList(id_number, pTime);
                        }
                    }
                }

                //connectListenerToUpdateViews();
                updateViews();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void connectListenerToUpdateViews() {
        Query myTopPostsQuery = mDatabaseRef.child("personnel_ver");
        myTopPostsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                updateViews();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public int tarbiewiListCount() {
        Cursor res = sqdb.rawQuery("SELECT " + COLUMN_VOLUNTEER_COUNT + " FROM " + TABLE_PERSONNEL_COUNT, null);
        volunteerC = 0;

        if (res != null && (res.getCount() > 0)) {
            res.moveToNext();

            volunteerC = Integer.parseInt(res.getString(0));
        }

        return volunteerC;
    }

    public void addPersonnelToList(String id_number, String time) {

        String[] params = new String[]{id_number};
        Cursor res = sqdb.rawQuery("SELECT * FROM " + TABLE_PERSONNEL + " WHERE id_number=?", params);

        if (res != null && (res.getCount() > 0)) {
            res.moveToNext();

            String info = res.getString(0);
//            String card_number = res.getString(2);
            String photo = res.getString(3);

            Personnel personnel = new Personnel("" + info, "" + id_number, " ", "" + photo, " ", "" + time);
            breakfastList.add(personnel);
            eatersAdapter.notifyDataSetChanged();
        }
    }

    public void updateViews() {
        System.out.println("updateViews Breakfast");
        personnelMenu.setCount(breakfastCount + " / " + tarbiewiListCount());
        adapter.notifyDataSetChanged();
    }

    public void setupViews() {
        tvDate = view.findViewById(R.id.textView2);
        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();

        FirebaseApp.initializeApp(getActivity());
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        linearLayoutManager = new LinearLayoutManager(getActivity());

        recyclerView = view.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerViewEatersList = view.findViewById(R.id.my_recycler_eaters_list);
        recyclerViewEatersList.setHasFixedSize(true);
        recyclerViewEatersList.setLayoutManager(linearLayoutManager);
        recyclerViewEatersList.setItemAnimator(new DefaultItemAnimator());
        recyclerViewEatersList.addItemDecoration(new DividerItemDecoration(getContext()));

        breakfastList = new ArrayList<>();
        eatersAdapter = new EatersListAdapter(getActivity(), breakfastList);
        recyclerViewEatersList.setAdapter(eatersAdapter);

        mSoundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
        assets = getActivity().getAssets();

        bonAppetitSound = loadSound("bon_appetit.mp3");
        errorSound = loadSound("error_sound.wav");

        autoCompleteTextView = view.findViewById(R.id.autoCompleteTextView);
        autoCompleteTextView.requestFocus();
        createEaterDescDilaog();

        dialogShowingTimer = new CountDownTimer(2000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                userDescDialog.dismiss();
            }
        };

        menu = new ArrayList();

        personnelMenu = new PMenu("Персонал", R.drawable.menu1, "0");
        //collegeMenu = new PMenu("Колледж", R.drawable.menu1, "0");
        //lyceumMenu = new PMenu("Лицей", R.drawable.menu1, "0");

        menu.add(personnelMenu);
        //menu.add(collegeMenu);
        //menu.add(lyceumMenu);

        adapter = new PMenuListAdapter(menu);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, final int position) {

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }
                })
        );


        autoCompleteTextView.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {


                if (checkInternetConnection() && (event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String card_number = autoCompleteTextView.getText().toString().toLowerCase();
                    setHideSoftKeyboard(autoCompleteTextView);

                    if (card_number.length() > 0) {
                        String id_number = getIdNumber(card_number);

                        checkInList(id_number);

                        autoCompleteTextView.setText("");

                        autoCompleteTextView.clearFocus();
                        autoCompleteTextView.requestFocus();
                        autoCompleteTextView.setCursorVisible(true);

                        return true;
                    } else {
                        System.out.println("id_number length is Zero ");
                    }
                }
                return false;
            }
        });

    }

    public String getIdNumber(String card_number) {
        String[] params = new String[]{card_number};
        String id_number = "";
        Cursor cursor = sqdb.rawQuery("SELECT * FROM " + TABLE_PERSONNEL + " WHERE card_number=?", params);

        if (cursor != null && (cursor.getCount() > 0)) {
            cursor.moveToNext();
            id_number = cursor.getString(1);
        }
        return id_number;
    }

    public void checkInList(String id_number) {
        boolean contain = false;

        for (Personnel personnel : breakfastList) {
            if (personnel.getId_number().equals(id_number)) {

                Glide.with(getActivity())
                        .load(personnel.getPhoto())
                        .placeholder(R.drawable.eater_icon3)
                        .into(userPhoto);

                imageAccess.setImageResource(R.drawable.error_icon);
                userName.setText(personnel.getInfo());
                desc.setTextColor(getResources().getColor(R.color.red));
                desc.setText("Тамақ ішілген: " + personnel.getTime());

                userDescDialog.show();
                dialogShowingTimer.start();
                contain = true;
                playSound(errorSound);
                break;
            }
        }

        if (!contain) checkInVolunteerList(id_number.toLowerCase());
    }

    public void checkInVolunteerList(String id_number) {

        String[] params = new String[]{id_number};

        Cursor cursor = sqdb.rawQuery("SELECT * FROM " + TABLE_PERSONNEL + " WHERE id_number=?", params);

        if (cursor != null && (cursor.getCount() > 0)) {
            cursor.moveToNext();

            String info = cursor.getString(0);
            String photo = cursor.getString(3);
            String type = cursor.getString(4);

            Personnel personnel = new Personnel("" + info, "" + id_number, " ", "" + photo, " ", "" + getNowTime());

            Glide.with(getActivity())
                    .load(personnel.getPhoto())
                    .placeholder(R.drawable.eater_icon3)
                    .into(userPhoto);

            imageAccess.setImageResource(R.drawable.success_icon);
            userName.setText(personnel.getInfo());

            if (type.equals("volunteer")) {

                desc.setTextColor(getResources().getColor(R.color.green));
                desc.setText("Рұқсат бар");
                mDatabaseRef.child("days").child("personnel").child(firebaseDate).child("breakfast").child(id_number).setValue(getNowTime());
                playSound(bonAppetitSound);

            } else {

                imageAccess.setImageResource(R.drawable.error_icon);
                desc.setTextColor(getResources().getColor(R.color.red));
                desc.setText("Рұқсат жоқ");
                playSound(errorSound);
            }

            userDescDialog.show();
            dialogShowingTimer.start();

        } else {

            userPhoto.setImageResource(R.drawable.t_icon);
            userName.setText("Қолданушы\nПерсоннал тізімінен табылған жоқ.");
            imageAccess.setImageResource(R.drawable.error_icon);
            desc.setTextColor(getResources().getColor(R.color.red));
            desc.setText("Рұқсат жоқ.");

            userDescDialog.show();
            dialogShowingTimer.start();
            playSound(errorSound);
        }

    }

    public String getNowTime() {

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String now = timeFormat.format(Calendar.getInstance().getTime());

        return now;
    }

    public void manageDate() {
        dateF = new SimpleDateFormat("EEEE, dd MMMM yyyy");
        date = dateF.format(Calendar.getInstance().getTime());

        firebaseDateFormat = new SimpleDateFormat("dd_MM");//2001.07.04
        firebaseDate = firebaseDateFormat.format(Calendar.getInstance().getTime());
        //firebaseDate = "23_04";

        tvDate.setText(date);
    }

    private void setHideSoftKeyboard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public void createEaterDescDilaog() {
        userDescDialog = new Dialog(getActivity());
        userDescDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        userDescDialog.setContentView(R.layout.dialog_desc_eater);

        userPhoto = userDescDialog.findViewById(R.id.userPhoto);
        imageAccess = userDescDialog.findViewById(R.id.imageAccess);
        userName = userDescDialog.findViewById(R.id.userName);
        desc = userDescDialog.findViewById(R.id.desc);
    }

    protected void playSound(int sound) {
        if (sound > 0)
            mSoundPool.play(sound, 1, 1, 1, 0, 1);
    }

    private int loadSound(String fileName) {
        AssetFileDescriptor afd = null;
        try {
            afd = assets.openFd(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Не могу загрузить файл " + fileName, Toast.LENGTH_SHORT).show();
            return -1;
        }

        return mSoundPool.load(afd, 1);
    }

    private boolean checkInternetConnection() {
        if (isNetworkAvailable()) {
            return true;

        } else {

            Toast.makeText(getActivity(), getResources().getString(R.string.inetConnection), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }
}