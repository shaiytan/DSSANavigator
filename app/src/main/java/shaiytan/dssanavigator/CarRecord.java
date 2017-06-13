package shaiytan.dssanavigator;

import java.io.Serializable;

/**
 * Created by Shaiytan on 05.05.2017.
 */

public class CarRecord implements Serializable
{

    public String getCarName(){return carName;}
    public void setCarName(String carName){this.carName = carName;}
    public double getLat(){return lat;}
    public void setLat(double lat){this.lat = lat;}
    public double getLon(){return lon;}
    public void setLon(double lon){this.lon = lon;}
    public long getId(){return id;}
    private String carName;
    private double lat;
    private double lon;
    private long id;
    public CarRecord(String carName, double lat, double lon,long id)
    {
        this.carName = carName;
        this.lat = lat;
        this.lon = lon;
        this.id = id;
    }
}
