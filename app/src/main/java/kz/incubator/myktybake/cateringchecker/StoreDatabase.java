package kz.incubator.myktybake.cateringchecker;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import kz.incubator.myktybake.cateringchecker.module.Personnel;
import kz.incubator.myktybake.cateringchecker.module.Student;

public class StoreDatabase extends SQLiteOpenHelper {
    public static final String COLUMN_CARD_NUMBER = "card_number";
    public static final String COLUMN_COLLEGE_DAY_VER = "college_day_ver";
    public static final String COLUMN_GROUP = "s_group";
    public static final String COLUMN_ID_NUMBER = "id_number";
    public static final String COLUMN_INFO = "info";
    public static final String COLUMN_LYCEUM_DAY_VER = "lyceum_day_ver";
    public static final String COLUMN_LYCEUM_VER = "lyceum_student_list_ver";
    public static final String COLUMN_OTHER_COUNT = "others";
    public static final String COLUMN_PERSONNEL_VER = "personnel_ver";
    public static final String COLUMN_PHOTO = "photo";
    public static final String COLUMN_Q_ID = "qr_code";
    public static final String COLUMN_STUDENTS_VER = "college_student_list_ver";
    public static final String COLUMN_TEACHER_COUNT = "teacherC";
    public static final String COLUMN_TOTAL_COUNT = "totalC";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_VOLUNTEER_COUNT = "volunteerC";
    public static final String COLUMN_WORKER_COUNT = "workerC";
    public static final String DATABASE_NAME = "askhana_catering_checker.db";
    private static final int DATABASE_VERSION = 5;
    public static final String TABLE_COLLEGE_STUDENTS = "college_students_list";
    public static final String TABLE_LYCEUM_STUDENTS = "lyceum_students_list";
    public static final String TABLE_PERSONNEL = "personnel_store";
    public static final String TABLE_PERSONNEL_COUNT = "personnel_store_count";
    public static final String TABLE_VER = "versions";
    Context context;

    public StoreDatabase(Context context2) {
        super(context2, DATABASE_NAME, null, 5);
        this.context = context2;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE personnel_store(info TEXT, id_number TEXT, card_number TEXT, photo TEXT, type TEXT )");
        db.execSQL("CREATE TABLE college_students_list(info TEXT, qr_code TEXT, id_number TEXT, photo TEXT, s_group TEXT, card_number TEXT )");
        db.execSQL("CREATE TABLE lyceum_students_list(info TEXT, qr_code TEXT, id_number TEXT, photo TEXT, s_group TEXT, card_number TEXT )");
        db.execSQL("CREATE TABLE personnel_store_count(totalC TEXT, teacherC TEXT, workerC TEXT, volunteerC TEXT, others TEXT )");
        db.execSQL("CREATE TABLE versions(personnel_ver TEXT, college_student_list_ver TEXT, lyceum_student_list_ver TEXT, college_day_ver TEXT, lyceum_day_ver TEXT )");
        addVersions(db);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS personnel_store");
        db.execSQL("DROP TABLE IF EXISTS college_students_list");
        db.execSQL("DROP TABLE IF EXISTS lyceum_students_list");
        db.execSQL("DROP TABLE IF EXISTS personnel_store_count");
        db.execSQL("DROP TABLE IF EXISTS versions");
        onCreate(db);
    }

    public void cleanPersonnel(SQLiteDatabase db) {
        db.execSQL("delete from personnel_store");
    }

    public void cleanCollegeStudentsTable(SQLiteDatabase db) {
        db.execSQL("delete from college_students_list");
    }

    public void cleanLyceumStudentsTable(SQLiteDatabase db) {
        db.execSQL("delete from lyceum_students_list");
    }

    public void cleanPersonnelCount(SQLiteDatabase db) {
        db.execSQL("delete from personnel_store_count");
    }

    public void cleanVersions(SQLiteDatabase db) {
        db.execSQL("delete from versions");
    }

    public void addVersions(SQLiteDatabase db) {
        ContentValues versionValues = new ContentValues();
        versionValues.put(COLUMN_PERSONNEL_VER, "0");
        versionValues.put(COLUMN_STUDENTS_VER, "0");
        versionValues.put(COLUMN_LYCEUM_VER, "0");
        versionValues.put(COLUMN_COLLEGE_DAY_VER, "0");
        versionValues.put(COLUMN_LYCEUM_DAY_VER, "0");
        db.insert(TABLE_VER, null, versionValues);
    }

    public void updatePersonnel(SQLiteDatabase db, Personnel personnel) {
        ContentValues updateValues = new ContentValues();
        updateValues.put("info", personnel.getInfo());
        updateValues.put(COLUMN_ID_NUMBER, personnel.getId_number());
        updateValues.put(COLUMN_CARD_NUMBER, personnel.getCard_number());
        updateValues.put(COLUMN_PHOTO, personnel.getPhoto());
        updateValues.put(COLUMN_TYPE, personnel.getType());
        String str = TABLE_PERSONNEL;
        StringBuilder sb = new StringBuilder();
        sb.append("id_number='");
        sb.append(personnel.getId_number());
        sb.append("'");
        db.update(str, updateValues, sb.toString(), null);
        StringBuilder sb2 = new StringBuilder();
        sb2.append("db: ");
        sb2.append(personnel.getInfo());
        Log.i("personnel", sb2.toString());
    }

    public void updateCollegeStudent(SQLiteDatabase db, Student student, String group) {
        ContentValues updateValues = new ContentValues();
        updateValues.put("info", student.getName());
        updateValues.put(COLUMN_Q_ID, student.getQr_code());
        updateValues.put(COLUMN_ID_NUMBER, student.getId_number());
        updateValues.put(COLUMN_PHOTO, student.getPhoto());
        updateValues.put(COLUMN_GROUP, group);
        updateValues.put(COLUMN_CARD_NUMBER, student.getCard_number());
        String str = TABLE_COLLEGE_STUDENTS;
        StringBuilder sb = new StringBuilder();
        sb.append("id_number='");
        sb.append(student.getId_number());
        sb.append("'");
        db.update(str, updateValues, sb.toString(), null);
        StringBuilder sb2 = new StringBuilder();
        sb2.append("db: ");
        sb2.append(group);
        Log.i("student", sb2.toString());
        StringBuilder sb3 = new StringBuilder();
        sb3.append("db: ");
        sb3.append(student.getName());
        Log.i("student", sb3.toString());
    }

    public void updateLyceumStudent(SQLiteDatabase db, Student student, String group) {
        ContentValues updateValues = new ContentValues();
        updateValues.put("info", student.getName());
        updateValues.put(COLUMN_Q_ID, student.getQr_code());
        updateValues.put(COLUMN_ID_NUMBER, student.getId_number());
        updateValues.put(COLUMN_PHOTO, student.getPhoto());
        updateValues.put(COLUMN_GROUP, group);
        updateValues.put(COLUMN_CARD_NUMBER, student.getCard_number());
        String str = TABLE_LYCEUM_STUDENTS;
        StringBuilder sb = new StringBuilder();
        sb.append("id_number='");
        sb.append(student.getId_number());
        sb.append("'");
        db.update(str, updateValues, sb.toString(), null);
        StringBuilder sb2 = new StringBuilder();
        sb2.append("db: ");
        sb2.append(group);
        Log.i("student", sb2.toString());
        StringBuilder sb3 = new StringBuilder();
        sb3.append("db: ");
        sb3.append(student.getName());
        Log.i("student", sb3.toString());
    }
}
