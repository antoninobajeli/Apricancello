package com.abajeli.aa_apricancelloautomatico.BT;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import com.abajeli.aa_apricancelloautomatico.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

/**
 * Created by abajeli on 18/04/15.
 */
public class BTMain {


    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    ConnectThread connectThread;
    final UUID MY_UUID= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    final String LOG="BTMain";

    public BTMain(){
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }
        setBluetooth(true);

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                //mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                Log.d(LOG,"Looking for server : "+device.getName() + "\n" + device.getAddress());
                if (device.getName().compareToIgnoreCase("RaspTooth")==0){
                    Log.d(LOG,"Found ==================================================   >>> :"+device.getName() + "\n" + device.getAddress());
                    connectThread  = new ConnectThread(device);
                    connectThread.start();
                    break;
                };
            }
        }


    }



    public void startCommunication(){

    }



    ConnectedThread socketConnectionThread;
    public void manageConnectedSocket(BluetoothSocket mmSocket){
        //now make the socket connection in separate thread to avoid FC
        socketConnectionThread  = new ConnectedThread(mmSocket);
        socketConnectionThread.start();

        try {
            String text = "OPEN_GATE:antonino.bajeli@gmail.com";
            byte[] bytes = text.getBytes("UTF-8");
            Log.d(LOG, "sending SOCKET message :" + text);
            socketConnectionThread.write(bytes);

        }catch(Exception e){
            e.printStackTrace();
            Log.e(LOG,"String error");
        }
        //connectThread.cancel();
    }




    public static boolean setBluetooth(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (enable && !isEnabled) {
            bluetoothAdapter.enable();
            while (bluetoothAdapter.isEnabled() == false){


            }
            return true;
        }
        else if(!enable && isEnabled) {
            return bluetoothAdapter.disable();
        }
        // No need to change bluetooth state
        return true;
    }



    // connect as a client
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(LOG, "IOException SOCKET not created :");
            }
            mmSocket = tmp;
            Log.d(LOG, "SOCKET created :");
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
                Log.d(LOG, "SOCKET connected :");
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                    connectException.printStackTrace();
                    Log.e(LOG,"connectException SOCKET closed 1");
                } catch (IOException closeException) {

                }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
                Log.d(LOG, "cancel SOCKET closed");
                socketConnectionThread.cancel();
            } catch (IOException e) { }
        }
    }





    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[64];  // buffer store for the stream
            int bytes; // bytes returned from read()

            boolean replyed=false;
            int timeout=2;
            long startTime=System.nanoTime();
            boolean isTimeout=false;
            Scanner scanner = new Scanner(mmInStream, "UTF-8");
            scanner.useDelimiter("\u0000");
            // Keep listening to the InputStream until an exception occurs
            while (!isInterrupted() && !replyed && !isTimeout) {
                    if ((System.nanoTime()-startTime) >3000) {
                        isTimeout=true;
                    }
                    //Scanner scanner = new Scanner(mmInStream, "UTF-8");
                    while (!isInterrupted() && scanner.hasNext() && mmSocket.isConnected()) {
                        String resp= scanner.next();
                        Log.d(LOG, "response=> " + resp);
                        if (resp.equalsIgnoreCase("ok")==true){
                            MainActivity.parla("Il servo ha eseguito");


                            try {
                                Thread.sleep(3000, 00);
                            }catch(Exception e){

                            }
                        }
                        replyed=true;
                        //System.out.println(scanner.next());
                        //System.out.println("--------------------------------");
                    }


                    /*
                   try {
                        // Read from the InputStream
                        bytes = mmInStream.read(buffer);
                        // Send the obtained bytes to the UI activity
                        if (bytes>0) {
                            String text = new String(buffer, "UTF-8");





                            Log.d(LOG, "response=> " + text);
                        }

                        //mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                        //        .sendToTarget();
                    } catch (IOException e) {
                        break;
                    }*/
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                Log.d(LOG, "cancel => closing connection !!! ");
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}



