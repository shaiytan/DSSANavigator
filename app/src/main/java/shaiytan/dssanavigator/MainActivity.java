package shaiytan.dssanavigator;

import android.app.Activity;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.*;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.io.*;
import java.util.ArrayList;


public class MainActivity extends Activity
{
    ListView carsList;
    TextView header;
    DBHelper dbhelper;
    SQLiteDatabase db;
    Cursor carCursor;
    SimpleCursorAdapter carAdapter;
    String carName;
    long id;
    public void deleteItem(long id)
    {
        if(id==this.id)return;
        db=dbhelper.getWritableDatabase();
        db.execSQL("DELETE FROM cars WHERE _id="+id+";");
        onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        header = (TextView) findViewById(R.id.header);
        carsList= (ListView) findViewById(R.id.list);
        dbhelper =new DBHelper(getApplicationContext());
        carsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                deleteItem(id);
                return true;
            }
        });
        boolean update=readConfigs();
        updateMyLoc(update);
        writeConfigs();
    }

    private void updateMyLoc(boolean updateflag)
    {
        LocationManager loc= (LocationManager) getSystemService(LOCATION_SERVICE);
        Location l=null;
        double lat=14,lon=88;
        try
        {
            loc.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,createPendingResult(1,new Intent(),0));
            l=loc.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        catch (SecurityException e)
        {
            Toast.makeText(this, "Как это низко с вашей стороны ставить заглушку на обработку исключения", Toast.LENGTH_SHORT).show();
        }
        if (l != null)
        {
            lat=l.getLatitude();
            lon=l.getLongitude();
        }
        db=dbhelper.getWritableDatabase();
        ContentValues cv=new ContentValues();
        cv.put("name","MyCar");
        cv.put("latitude",lat);
        cv.put("longitude",lon);
        if(updateflag)
        {
            db.update("cars",cv,"_id=?",new String[]{String.valueOf(id)});
        }
        else
        {
            id=db.insert("cars",null,cv);
        }

    }
    private void writeConfigs()
    {
        try
        {
            OutputStream out=openFileOutput("config",MODE_PRIVATE);
            DataOutputStream fout=new DataOutputStream(out);
            fout.writeUTF(carName);
            fout.writeLong(id);
            fout.close();
        }
        catch (IOException e)
        {
            Toast.makeText(this, "Write config fail", Toast.LENGTH_SHORT).show();
        }
    }
    private boolean readConfigs()
    {
        try
        {
            InputStream in=openFileInput("config");
            DataInputStream fin=new DataInputStream(in);
            carName=fin.readUTF();
            id=fin.readLong();
            fin.close();
        }
        catch (IOException e)
        {
            Toast.makeText(this, "No config files", Toast.LENGTH_SHORT).show();
            carName="MyCar";
            id=-1;
            return false;
        }
        return true;
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        db=dbhelper.getReadableDatabase();
        carCursor=db.rawQuery("select * from cars",null);
        String[] headers=new String[]{"name","latitude","longitude"};
        carAdapter=new SimpleCursorAdapter(this,R.layout.item,
                carCursor,headers,new int[]{R.id.iname,R.id.ilat,R.id.ilon},0);
        header.setText("Найдено элементов: "+carCursor.getCount());
        carsList.setAdapter(carAdapter);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        db.close();
        carCursor.close();
    }
    public void addClick(View v)
    {
        Intent intent=new Intent(this,MapActivity.class);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode!=RESULT_OK) return;
        String name=data.getStringExtra("name");
        double lat=data.getDoubleExtra("lat",0);
        double lon=data.getDoubleExtra("lon",0);
        db=dbhelper.getWritableDatabase();
        db.execSQL("INSERT INTO cars (name, latitude, longitude) VALUES ('"+name+"',"+lat+","+lon+")");
    }

    public void showMapClick(View view)
    {
        carCursor.moveToFirst();
        ArrayList<CarRecord> lst=new ArrayList<CarRecord>();
        do
        {
            lst.add(new CarRecord(
                    carCursor.getString(carCursor.getColumnIndex("name")),
                    carCursor.getDouble(carCursor.getColumnIndex("latitude")),
                    carCursor.getDouble(carCursor.getColumnIndex("longitude")),
                    carCursor.getInt(carCursor.getColumnIndex("_id"))
            ));
        }
        while(carCursor.moveToNext());
        Intent intent=new Intent(this,ShowMapActivity.class);
        intent.putExtra("carslist",lst);
        intent.putExtra("myid",id);
        startActivity(intent);
    }
}
