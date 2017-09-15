package shaiytan.dssanavigator.model

import android.content.Context
import android.database.sqlite.*
import shaiytan.dssanavigator.R
import java.io.Serializable


/**
 * Created by Shaiytan on 12.09.2017.
 * Data classes
 */

//data class CarRecord()
data class CarRecord(var carName: String, var lat: Double, var lon: Double, val id: Long) : Serializable

class DBHelper(context: Context) : SQLiteOpenHelper(context, DBNAME, null, 1) {
    companion object {
        private val DBNAME: String = "carstore.db"
    }

    private val res = context.resources
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(res.getString(R.string.createQuery))
        db.execSQL("INSERT INTO cars (name, latitude, longitude) VALUES ('TEST', 14, 88);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(res.getString(R.string.dropQouery))
        onCreate(db)
    }
}