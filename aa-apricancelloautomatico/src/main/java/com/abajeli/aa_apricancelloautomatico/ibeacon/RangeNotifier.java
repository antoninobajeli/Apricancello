package com.abajeli.aa_apricancelloautomatico.ibeacon;

/**
 * Created by abajeli on 14/04/15.
 */
import java.util.Collection;
/**
 * This interface is implemented by classes that receive iBeacon ranging notifications
 *
 * @see IBeaconManager#setRangeNotifier(RangeNotifier notifier)
 * @see IBeaconManager#startRangingBeaconsInRegion(Region region)
 * @see Region
 * @see IBeacon
 *
 * @author David G. Young
 *
 */
public interface RangeNotifier {
    /**
     * Called once per second to give an estimate of the distance to visible iBeacons
     * @param iBeacons a collection of <code>IBeacon<code> objects that have been seen in the past second
     * @param region the <code>Region</code> object that defines the criteria for the ranged iBeacons
     */
    public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region);
}
