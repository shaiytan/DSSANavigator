package shaiytan.dssanavigator;

import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.*;
import android.support.annotation.NonNull;


/**
 * Created by Shaiytan on 23.04.2017.
 * File class provides sqlite connection
 */

class DBHelper extends SQLiteOpenHelper
{
    private static final String DBNAME ="carstore.db";
    private final Resources res;
    public DBHelper(@NonNull Context context)
    {
        super(context, DBNAME,null,1);
        res=context.getResources();
    }
    @Override
    public void onCreate(@NonNull SQLiteDatabase db)
    {
        db.execSQL(res.getString(R.string.createQuery));
        db.execSQL("INSERT INTO cars (name, latitude, longitude) VALUES ('TEST', 14, 88);");
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL(res.getString(R.string.dropQouery));
        onCreate(db);
    }
}
