package shaiytan.dssanavigator;

import android.content.Intent;
import android.graphics.Color;
import android.location.*;
import android.os.*;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.maps.android.PolyUtil;

import org.w3c.dom.*;
import org.xml.sax.InputSource;

import java.net.URL;
import java.util.*;

import javax.xml.parsers.*;

import shaiytan.dssanavigator.model.CarRecord;

public class ShowMapActivity extends FragmentActivity implements OnMapReadyCallback
{

    private GoogleMap mMap;
    private ArrayList<CarRecord> cars;
    private long id;
    private Location currloc;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        cars=(ArrayList<CarRecord>) getIntent().getSerializableExtra("carslist");
        if (cars == null)
        {
            cars= new ArrayList<>();
        }
        id=getIntent().getLongExtra("myid",-1);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        LocationManager loc= (LocationManager) getSystemService(LOCATION_SERVICE);
        try
        {
            loc.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,createPendingResult(1,new Intent(),0));
            currloc=loc.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            LatLng here = new LatLng(currloc.getLatitude(),currloc.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(here,15));
        }
        catch (SecurityException e)
        {
            Toast.makeText(this, "Как это низко с вашей стороны ставить заглушку на обработку исключения", Toast.LENGTH_SHORT).show();
        }
        for (int i = 0; i < cars.size(); i++)
        {
            CarRecord carRecord = cars.get(i);
            LatLng coord=new LatLng(carRecord.getLat(),carRecord.getLon());
            mMap.addMarker(new MarkerOptions()
                    .position(coord)
                    .title(carRecord.getCarName())
                    .icon(BitmapDescriptorFactory.fromResource(carRecord.getId()==id?R.mipmap.ic_you:R.mipmap.ic_car)));
        }
        new AsyncTask<CarRecord, Void, ArrayList<String>>()
        {
            @Override
            protected ArrayList<String> doInBackground(CarRecord... params)
            {
                ArrayList<String> routes= new ArrayList<>();
                for (CarRecord x:params)
                {
                    if(x.getId()==id) continue;
                    routes.add(requestRoute(x));
                }
                return routes;
            }

            @Override
            protected void onPostExecute(ArrayList<String> s)
            {
                for(String route:s)
                {
                    List<LatLng> pointslist = PolyUtil.decode(route);
                    PolylineOptions line = new PolylineOptions().color(Color.argb(125,125,255,255)).width(10);
//                    for (int i = 0; i < pointslist.size(); i++) {
//                        if (i == 0) {
//                            mMap.addMarker(new MarkerOptions()
//                                    .position(pointslist.get(i))
//                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_you)));
//                        } else if (i == pointslist.size() - 1) {
//                            mMap.addMarker(new MarkerOptions()
//                                    .position(pointslist.get(i))
//                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car)));
//                        }
//                        line.add(pointslist.get(i));
//                    }
                    line.addAll(pointslist);
                    mMap.addPolyline(line).setVisible(true);
                }
            }
        }.execute((CarRecord[]) cars.toArray(new CarRecord[cars.size()]));
    }
    private String requestRoute(CarRecord car)
    {
        String ret="";
        try
        {
            String address="https://maps.googleapis.com/maps/api/directions/xml?origin="
                    +currloc.getLatitude()+","+currloc.getLongitude()
                    +"&destination="+car.getLat()+","+car.getLon()+
                    "&key="+getResources().getString(R.string.google_maps_key);

            URL url=new URL(address);
            DocumentBuilder domb= DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc=domb.parse(new InputSource(url.openStream()));
            doc.getDocumentElement().normalize();
            NodeList lst=doc.getElementsByTagName("points");
            Node op=lst.item(lst.getLength()-1);

            //Node pnts=op.getFirstChild();
            ret=op.getTextContent();

        }
        catch (Exception e)
        {
            final String msg=e.toString();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ShowMapActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
        return ret;
    }
}
