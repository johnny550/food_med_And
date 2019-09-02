package databaseSettings;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Map;
import java.util.TreeMap;


public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TABLE_NAME = "Meal_table";
    private static final String COLA = "Patient_ID";
    private static final String COLB = "photo";
    private static final String COLC = "condition";
    private static final String COLD = "appetite";
    private static final String COLE = "volume_meal";
    private static final String COLF = "free_comments";



    public DatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, 6);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createReq = "CREATE TABLE " + TABLE_NAME + " (Patient_ID CHAR (256) NOT NULL," +
                " datetime DATETIME NOT NULL DEFAULT (datetime('now', 'localtime')), photo TEXT (1024) NOT NULL," +
                " condition DECIMAL NOT NULL, appetite DECIMAL NOT NULL, volume_meal" +
                " DECIMAL NOT NULL, free_comments TEXT (1024), question1 DECIMAL NOT NULL DEFAULT (0)," +
                " question2 DECIMAL DEFAULT (0) NOT NULL, question3 DECIMAL DEFAULT (0) NOT NULL," +
                " question4 DECIMAL DEFAULT (0) NOT NULL, question5 DECIMAL DEFAULT (0) NOT NULL," +
                " question6 DECIMAL NOT NULL DEFAULT (0), question7 DECIMAL NOT NULL DEFAULT (0)," +
                " question8 DECIMAL NOT NULL DEFAULT (0),  Serial INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE );";

        sqLiteDatabase.execSQL(createReq);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean addData(String patientId, String photo, double condition, double appetite, double volume_meal,
                           String freeComments, Map<String, String> questions) throws SQLiteException {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLA, patientId);
        contentValues.put(COLB, photo);
        contentValues.put(COLC, condition);
        contentValues.put(COLD, appetite);
        contentValues.put(COLE, volume_meal);
        contentValues.put(COLF, freeComments);
        //order  the hasMap of questions
        TreeMap<String, String> orderedQuestions = new TreeMap<>();
        orderedQuestions.putAll(sortbykey(questions));
        String name = "question";

        for (int i = 0; i < orderedQuestions.size(); i++) {
            int newIndex = i + 1;
            contentValues.put(name + newIndex, orderedQuestions.get("question" + newIndex));
        }


        long result = 0;
        try {
            result = db.insert(TABLE_NAME, null, contentValues);
        } catch (SQLiteException ex) {
            ex.getMessage();
        }
        //if date as inserted incorrectly it will return -1
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Returns all the data from database
     *
     * @return
     */
    public Cursor getData( String id) {
        getCount(id);
        SQLiteDatabase db = this.getWritableDatabase();
        String query = " SELECT patient_ID, datetime, photo, appetite, condition, volume_meal, free_comments, " +
                "question1," +
                "question2, " +
                "question3, " +
                "question4, " +
                "question5, " +
                "question6, " +
                "question7, " +
                "question8 " +
                " FROM Meal_table WHERE patient_ID =\""+id+"\" ";
        Cursor data = db.rawQuery(query, null);
        return data;
    }


    public int getCount(String id) {
        SQLiteDatabase d = this.getWritableDatabase();
        String count = "SELECT count(*) FROM " + TABLE_NAME +" where patient_id =\""+id+"\"";
        Cursor mcursor = d.rawQuery(count, null);
        mcursor.moveToFirst();
        int icount = mcursor.getInt(0);
        if (icount > 0)
            Log.d("TAGTAG", "lines: " + icount);

        d.close();
        return icount;
    }

    public void deleteFromDB(String id){
        SQLiteDatabase db = this.getWritableDatabase();
       // db.rawQuery(request, null);
        db.delete(TABLE_NAME, "patient_id=?", new String[]{id});
    }

    // Function to sort map by Key
    public static TreeMap sortbykey(Map<String, String> mapOfQuestions) {
        // TreeMap to store values of HashMap
        TreeMap<String, String> sorted = new TreeMap<>();

        // Copy all data from hashMap into TreeMap
        sorted.putAll(mapOfQuestions);

        return sorted;
    }


}
