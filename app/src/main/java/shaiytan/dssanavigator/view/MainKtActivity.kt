package shaiytan.dssanavigator.view

import android.app.Activity
import android.app.PendingIntent
import android.content.*
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.location.LocationManager
import android.os.Bundle
import android.support.design.widget.*
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.*
import shaiytan.dssanavigator.MapActivity

import shaiytan.dssanavigator.R
import shaiytan.dssanavigator.ShowMapActivity
import shaiytan.dssanavigator.model.CarRecord
import shaiytan.dssanavigator.model.DBHelper
import java.util.ArrayList

class MainKtActivity : AppCompatActivity() {
    companion object {
        private val ID_PREF = "id"
        private val CAR_NAME_PREF = "cn"
        private val LOCATION_REQUEST = 1
        private val ADD_REQUEST = 2
    }
    private lateinit var header: TextView
    private lateinit var carsList: ListView
    private lateinit var carCursor: Cursor
    private lateinit var dbHelper: DBHelper
    private lateinit var db: SQLiteDatabase

    private var id = -1L
    private var carName = "My Car"

    private lateinit var pref: SharedPreferences

    private fun deleteItem(id: Long) {
        if (id == this.id) return
        db = dbHelper.writableDatabase
        db.execSQL("DELETE FROM cars WHERE _id=$id;")
        onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_kt)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        dbHelper = DBHelper(applicationContext)
        header = findViewById(R.id.header) as TextView
        carsList = findViewById(R.id.list) as ListView
        carsList.setOnItemLongClickListener { parent, view, position, id -> deleteItem(id); true }
        pref=getPreferences(Context.MODE_PRIVATE)

        val update = readConfigs()
        updateMyLoc(update)
        writeConfigs()
        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener(this::addClick)
    }
    private fun addClick(view:View){
        val intent = Intent(this, MapActivity::class.java)
        startActivityForResult(intent, ADD_REQUEST)
    }
    private fun updateMyLoc(updateFlag:Boolean){
        val locManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,
                createPendingResult(LOCATION_REQUEST, Intent(), PendingIntent.FLAG_UPDATE_CURRENT))
        val loc = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        val lat = loc?.latitude ?: 14.0
        val lon = loc?.longitude ?: 88.0
        db = dbHelper.writableDatabase
        val cv = ContentValues()
        cv.put("name", "MyCar")
        cv.put("latitude", lat)
        cv.put("longitude", lon)
        if (updateFlag){
            db.update("cars",cv,"_id=?", arrayOf(id.toString()))
        } else {
            id = db.insert("cars",null,cv)
        }
    }
    private fun readConfigs():Boolean{
        id = pref.getLong(ID_PREF,-1L)
        carName = pref.getString(CAR_NAME_PREF,"My Car")
        return id != -1L
    }
    private fun writeConfigs(){
        val edit = pref.edit()
        edit.putLong(ID_PREF,id)
                .putString(CAR_NAME_PREF,carName)
                .apply()
    }

    override fun onResume() {
        super.onResume()
        db = dbHelper.readableDatabase
        carCursor = db.rawQuery("select * from cars", null)
        val headers = arrayOf("name", "latitude", "longitude")
        val carAdapter = SimpleCursorAdapter(this, R.layout.item,
                carCursor, headers, intArrayOf(R.id.iname, R.id.ilat, R.id.ilon), 0)
        header.text = "Найдено элементов: ${carCursor.count}"
        carsList.adapter = carAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
        db.close()
        carCursor.close()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode != Activity.RESULT_OK || requestCode != ADD_REQUEST) return
        val name = data.getStringExtra("name")
        val lat = data.getDoubleExtra("lat", 0.0)
        val lon = data.getDoubleExtra("lon", 0.0)
        db = dbHelper.writableDatabase
        db.execSQL("INSERT INTO cars (name, latitude, longitude) VALUES ('$name',$lat,$lon)")
    }

    fun showMapClick(view: View) {
        if(!carCursor.moveToFirst()) return
        val lst = ArrayList<CarRecord>()
        do {
            lst.add(CarRecord(
                    carCursor.getString(carCursor.getColumnIndex("name")),
                    carCursor.getDouble(carCursor.getColumnIndex("latitude")),
                    carCursor.getDouble(carCursor.getColumnIndex("longitude")),
                    carCursor.getLong(carCursor.getColumnIndex("_id"))
            ))
        } while (carCursor.moveToNext())
        val intent = Intent(this, ShowMapActivity::class.java)
        intent.putExtra("carslist", lst)
        intent.putExtra("myid", id)
        startActivity(intent)
    }
}
