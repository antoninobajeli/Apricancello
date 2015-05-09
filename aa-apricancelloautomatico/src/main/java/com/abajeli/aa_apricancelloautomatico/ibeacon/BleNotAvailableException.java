package com.abajeli.aa_apricancelloautomatico.ibeacon;

/**
 * Created by abajeli on 14/04/15.
 */
/**
 * Indicates that low energy bluetooth is not available on this device
 * @see  ibeacon.IBeaconManager#CheckAvailability
 * @author David G. Young
 *
 */
public class BleNotAvailableException extends RuntimeException {

    private static final long serialVersionUID = 2242747823097637729L;

    public BleNotAvailableException(String message) {
        super(message);
    }

}