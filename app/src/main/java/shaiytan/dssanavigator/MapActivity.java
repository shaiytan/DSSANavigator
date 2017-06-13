package shaiytan.dssanavigator;

import android.content.Intent;
import android.location.*;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

public class MapActivity extends FragmentActivity
        implements OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener
{
    private GoogleMap mMap;
    private Marker marker;
    EditText carname;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        carname=(EditText) findViewById(R.id.carname);
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
        mMap.setOnMapLongClickListener(this);
        LocationManager loc= (LocationManager) getSystemService(LOCATION_SERVICE);
        try
        {
            loc.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,createPendingResult(1,new Intent(),0));
            Location l=loc.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            LatLng here = new LatLng(l.getLatitude(),l.getLongitude());
            marker=mMap.addMarker(new MarkerOptions().position(here).title("You Are Here"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(here,15));
        }
        catch (SecurityException e)
        {
            Toast.makeText(this, "Как это низко с вашей стороны ставить заглушку на обработку исключения", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed()
    {
        Intent intent=new Intent();
        setResult(RESULT_CANCELED,intent);
        finish();
    }

    @Override
    public void onMapLongClick(LatLng latLng)
    {
        marker.setPosition(latLng);
    }
    public void addCarClick(View view)
    {
        Intent intent=new Intent();
        intent.putExtra("name",carname.getText().toString());
        intent.putExtra("lat",marker.getPosition().latitude);
        intent.putExtra("lon",marker.getPosition().longitude);
        setResult(RESULT_OK,intent);
        finish();
    }
}
