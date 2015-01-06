package com.abajeli.testsample;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class ConnectThread extends Thread {
    public static String TAG = "ConnectThread";
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    public  ConnectedToApriportaThread connectedToApriportaThread;


    public ConnectThread(BluetoothDevice device)
    {

        ParcelUuid[] uuid= device.getUuids();
        UUID muuid=uuid[0].getUuid();
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        mmDevice = device;

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            tmp = device.createRfcommSocketToServiceRecord(muuid);
        }

        catch (Exception e)
        {

            try
            {
                if(tmp != null)
                    tmp.close();
            }
            catch (IOException e1) {
                // TODO Auto-generated catch block
                Log.e(TAG, "Closing un successful socket: " + e);
                e1.printStackTrace();

            }
            Log.e(TAG, "Error occured while creating bluetooth socket for connection: " + e);

        }

        mmSocket = tmp;
    }




    public void run() {
        // Cancel discovery because it will slow down the connection
        MainActivity.mBluetoothAdapter.cancelDiscovery();
        Log.d(TAG, "run");
        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                mmSocket.close();
            } catch (IOException closeException) { }
            return;
        }

        Log.d(TAG, "ending run");
        // Do work to manage the connection (in a separate thread)
        manageConnectedSocket(mmSocket);
    }


    public void manageConnectedSocket(BluetoothSocket sock)
    {
        String adr = mmDevice.getAddress();

        connectedToApriportaThread=new ConnectedToApriportaThread(sock);

        byte apri=101;
        byte chiudi=111;
        byte tmpbuf[] = new byte[1];

        tmpbuf[0]=apri;
        connectedToApriportaThread.write(tmpbuf);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        tmpbuf[0]=chiudi;
        connectedToApriportaThread.write(tmpbuf);

        /*mobitradeProtocol.SetExchangeThreadForSession(adr, new ExchangeWithMobiTradeDevice(mobitradeProtocol, sock));

        // Start the exchanging thread to read the received messages
        // Sending the list of channels

        mobitradeProtocolcom.MobiTrade.network.MobiTradeProtocol.StartExchangeThreadForSession(adr);
        mobitradeProtocol.WriteListOfChannelsToExchangeThreadForSession(adr);*/
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }


        try {
            if(mmSocket != null)
                mmSocket.close();

        } catch (IOException e) {
            Log.e(TAG, "Cancelling ongoing connection: " + e);
            // TODO Auto-generated catch block
            e.printStackTrace();
            //mobitradeProtocol.setExceptionOccuredOnSession(mmSocket.getRemoteDevice());
        }


    }
}