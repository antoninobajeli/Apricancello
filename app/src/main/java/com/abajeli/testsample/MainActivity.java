package com.abajeli.testsample;

import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class MainActivity
        extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    //private  static ArrayList<String> mWlArrListNames = new ArrayList<String>();

    private  static ArrayList<String> mWlArrListNumbers = new ArrayList<String>();
    private  static List<Map<String, String>> mCursorList = new ArrayList<Map<String,String>>();
    private static SimpleAdapter mCrsAdapter;
    private static ListView mLv;
    //private static ArrayAdapter<String> mArrAdp;

    private static int totInternetCommand=0;
    private static int totCommand=0;

    private ScheduledExecutorService mScheduleTaskExecutorForRefreshWhiteList;
    private ScheduledExecutorService mScheduleTaskExecutorForOpen;
    /**
     *
     * @param savedInstanceState
     */


    public static String TAG = "MainAct";
    static int REQUEST_ENABLE_BT=2;
    //static String WHITELIST_URL     ="http://www.bajeli.com/app/condominial/getJsonWhiteList.php";
    static String WHITELIST_URL     ="http://www.bajeli.com/app/condominial/json-Services.php?serviceName=getWhiteList";

    static String CHECKFOROPEN_URL  ="http://www.bajeli.com/app/condominial/getForOpen.php?requester=master";




    static BluetoothAdapter mBluetoothAdapter;
    //public static UUID  APRIPORTA_UUID =new UUID("00001101-0000-1000-8000-00805f9b34fb");
    public static UUID APRIPORTA_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    public static String deviceName="BluetoothBee";
    static ConnectThread connectedDevice;


    private static final int CONNECTION_TIMEOUT=5000;
    private static final int SO_TIMEOUT=6000;

    private static final int REFRESH_PERIOD_IN_SECONNDS=30;
    private static final int CHECK_PERIOD_FOR_OPEN_IN_SECONDS=6;

    static int MODE=0; // 0 master centralino, 1 slaves remote

    static TextView mLastUpdate,mTotCommand,mTotInternetCommand,mCallDirectCommand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));




        mScheduleTaskExecutorForRefreshWhiteList= Executors.newScheduledThreadPool(5);
        // This schedule a task to run every REFRESH_PERIOD_IN_SECONNDS sec:
        mScheduleTaskExecutorForRefreshWhiteList.scheduleAtFixedRate(new Runnable() {
            public void run() {
                new GetWhiteList().execute("");
/*
                // If you need update UI, simply do this:
                runOnUiThread(new Runnable() {
                    public void run() {
                        // update your UI component here.
                        myTextView.setText("refreshed");
                    }
                });*/
            }
        }, 0, REFRESH_PERIOD_IN_SECONNDS, TimeUnit.SECONDS);


        mScheduleTaskExecutorForOpen= Executors.newScheduledThreadPool(5);
        mScheduleTaskExecutorForOpen.scheduleAtFixedRate(new Runnable() {
            public void run() {
                new CheckForOpen().execute("");
            }
        }, 0, CHECK_PERIOD_FOR_OPEN_IN_SECONDS, TimeUnit.SECONDS);



        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

        // controllo se il buelthooth e' attivo
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);


        }

        connectToBee();





        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
		/*
		mBluetoothAdapter.startDiscovery();
		Log.d(TAG,"mBluetoothAdapter laubched");
		*/
        //ConnectThread




        if (MODE==1) {
            // moved in aa_apricancello automatico
        }
    }


    public static boolean isNumberInList(String phoneNr){
        //for(String str: mWlArrListNumbers) {
        for(String str: mWlArrListNumbers) {
            if(str.trim().equals(phoneNr)){
                return true;
            }
        }
        return false;
    }






    private static void connectToBee(){
        // elenco dei device accoppiati
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                //mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                Log.d(TAG,"device.getAddress() "+device.getAddress());
                Log.d(TAG,"device.getUuids() "+device.getUuids());
                Log.d(TAG,"device.getName() "+device.getName());
                ParcelUuid[] uuid= device.getUuids();
                if (uuid!=null){
                    for (int i=0;i<uuid.length;i++){
                        Log.d(TAG,"uuid >>> "+uuid[i].getUuid());
                    }
                }

                if (device.getName().equals(deviceName)){
                    connectedDevice= new ConnectThread(device);
                    connectedDevice.run();
                }

            }
        }
    }






    /**Link to the actuator giving the command to close and open/circuit
     *
     * */
    public static void commuta(){

        totCommand++;
        byte apri=101; // open the gate
        byte chiudi=111; // close the gate
        byte tmpbuf[] = new byte[1];

        tmpbuf[0]=apri;
        if (connectedDevice==null){
            Log.d(TAG, "connectedDevice is null");
            return;
        }
        if (connectedDevice.connectedToApriportaThread==null){
            connectToBee();
            Log.d(TAG, "connectedToApriportaThread is null");
            return;
        }

        connectedDevice.connectedToApriportaThread.write(tmpbuf);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        tmpbuf[0]=chiudi;
        connectedDevice.connectedToApriportaThread.write(tmpbuf);

    }




    public void exit(){
        this.finish();
    }






    //**
    //* init the communication with BlueTooth device
    //**



    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"BroadcastReceiver onReceive");
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                Log.d(TAG,"device.getAddress() "+device.getAddress());
                ParcelUuid[] uuid= device.getUuids();
                if (uuid!=null)
                    Log.d(TAG, "UUID: " + uuid.length);
                else
                    Log.d(TAG, " null ");


                //mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    };




    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        unregisterReceiver(mReceiver);

        super.onDestroy();
    }


    private class CheckForOpen extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            Long startTime,communicationStartTime = null;
            startTime = System.currentTimeMillis();
            String operationResult="Failed";

            DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());

            httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT); // in ms
            httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, SO_TIMEOUT);// in ms

            HttpGet httppost = new HttpGet(CHECKFOROPEN_URL);
            // Depends on your web service
            httppost.setHeader("Content-type", "application/json");

            InputStream inputStream = null;
            String result = null;
            String msg_res="";
            try {
                communicationStartTime = System.currentTimeMillis();
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();

                inputStream = entity.getContent();
                // json is UTF-8 by default
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line + "\n");
                }
                result = sb.toString();
                msg_res="CommandReceived ";
                if (result.equals("1\n")){
                    totInternetCommand++;
                    commuta();
                }else{
                    System.out.println("Nothing to do");
                }

            }
            catch(SocketTimeoutException se) {
                Long endTime = System.currentTimeMillis();
                System.out.println("SocketTimeoutException :: time elapsed :: " + (endTime-communicationStartTime));
                se.printStackTrace();
                msg_res="SocketTimeout ";
            }
            catch(ConnectTimeoutException cte) {
                Long endTime = System.currentTimeMillis();
                System.out.println("ConnectTimeoutException :: time elapsed :: " + (endTime-communicationStartTime));
                cte.printStackTrace();
                msg_res="ConnectTimeout ";
            } catch (Exception e) {
                // Oops
                e.printStackTrace();
                msg_res="GenericException ";
            }
            finally {
                try{
                    if(inputStream != null)inputStream.close();
                }catch(Exception squish){

                }
            }

            Long endTime = System.currentTimeMillis();
            System.out.println("CheckForOpen :: time elapsed :: " + (endTime-startTime));

            aggiornna_dati(msg_res + "  " + (endTime - startTime));
            return operationResult;
        }

        @Override
        protected void onPostExecute(String result) {
            if (mCrsAdapter!=null) {
                mCrsAdapter.notifyDataSetChanged();
            }
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }







    public void aggiornna_dati(final String s){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mLastUpdate!=null)
                    mLastUpdate.setText(s);


                if (mTotInternetCommand!=null)
                    mTotInternetCommand.setText(""+totInternetCommand);

                if (mTotCommand!=null)
                    mTotCommand.setText(""+totCommand);

                if (mCallDirectCommand!=null)
                    mCallDirectCommand.setText(""+(totCommand-totInternetCommand));


            }
        });
    }







    /*
    * Comunicazione HTTP con il server per il reperimento dei dati
    *
    *
    * */
    private class GetWhiteList extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            Long startTime,communicationStartTime = null;
            startTime = System.currentTimeMillis();
            String operationResult="Failed";

            DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());

            httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT); // in ms
            httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, SO_TIMEOUT);// in ms

            HttpPost httppost = new HttpPost(WHITELIST_URL);
            // Depends on your web service
            httppost.setHeader("Content-type", "application/json");

            InputStream inputStream = null;
            String jsonResult = null;
            try {
                communicationStartTime = System.currentTimeMillis();
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();

                inputStream = entity.getContent();
                // json is UTF-8 by default
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line + "\n");
                }
                jsonResult = sb.toString();



                // We have the result ... so we can extract data
                // from jsonResult
                try {
                    JSONObject jObject = new JSONObject(jsonResult);
                    // if it is an array itself JSONArray jArray = new JSONArray(result)

                    //String aJsonString = jObject.getString("STRINGNAME");
                    //boolean aJsonBoolean = jObject.getBoolean("BOOLEANNAME");
                    JSONArray jArray = jObject.getJSONArray("whiteList");
                    mWlArrListNumbers.clear();
                    mCursorList.clear();
                    for (int i=0; i < jArray.length(); i++)
                    {
                        try {
                            JSONObject oneObject = jArray.getJSONObject(i);
                            // Pulling items from the array
                            String tel = oneObject.getString("tel");
                            String name = oneObject.getString("name");
                            /*mWlArrListNames.add( name );*/
                            mWlArrListNumbers.add( tel );


                            Map<String, String> entry = new HashMap<String, String>();
                            entry.put("tel", tel);
                            entry.put("name", name);
                            mCursorList.add(entry);


                            Log.d("rest",name);
                        } catch (JSONException e) {
                            // Oops
                        }
                    }
                    operationResult="Executed";
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
            catch(SocketTimeoutException se) {
                Long endTime = System.currentTimeMillis();
                System.out.println("SocketTimeoutException :: time elapsed :: " + (endTime-communicationStartTime));
                se.printStackTrace();
            }
            catch(ConnectTimeoutException cte) {
                Long endTime = System.currentTimeMillis();
                System.out.println("ConnectTimeoutException :: time elapsed :: " + (endTime-communicationStartTime));
                cte.printStackTrace();
            } catch (Exception e) {
                // Oops
                e.printStackTrace();
            }
            finally {
                try{
                    if(inputStream != null)inputStream.close();
                }catch(Exception squish){

                }
            }

            Long endTime = System.currentTimeMillis();
            System.out.println("RefreshOperation :: time elapsed :: " + (endTime-startTime));
            return operationResult;
        }

        @Override
        protected void onPostExecute(String result) {
            if (mCrsAdapter!=null) {
                mCrsAdapter.notifyDataSetChanged();
            }
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }





    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();

            fragmentManager.beginTransaction()
                    .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                    .commit();

    }


    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                new GetWhiteList().execute("");
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        MainActivity callerActivity;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            int fragmentnumb=getArguments().getInt(ARG_SECTION_NUMBER);
            View rootView;

            switch(fragmentnumb){
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_main, container, false);
                    mLv = (ListView)rootView.findViewById(R.id.listViewWhiteList);
                    if(mCursorList!=null){

                        String[] keys = {"name","tel"};
                        int[] widgetIds = {android.R.id.text1, android.R.id.text2};

                        SimpleAdapter mCrsAdapter = new SimpleAdapter(rootView.getContext(),mCursorList,android.R.layout.simple_list_item_2, keys,widgetIds);
                        //mArrAdp=new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_list_item_2 ,android.R.id.text1 , mWlArrListNames );
                        mLv.setAdapter(mCrsAdapter);
                    }
                    break;
                case 2:
                    rootView = inflater.inflate(R.layout.fragment_device_status, container, false);

                    mLastUpdate= (TextView) rootView.findViewById(R.id.lastOperation);
                    mTotCommand= (TextView) rootView.findViewById(R.id.totComm);
                    mTotInternetCommand= (TextView) rootView.findViewById(R.id.totInternetComm);
                    mCallDirectCommand= (TextView) rootView.findViewById(R.id.callDirectComm);

                    Button button= (Button) rootView.findViewById(R.id.btexit);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (callerActivity!=null)  callerActivity.exit();
                        }
                    });

                    Button buttonCommuta= (Button) rootView.findViewById(R.id.btcommuta);
                    buttonCommuta.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            commuta();
                        }
                    });
                    break;

                default:
                    rootView = inflater.inflate(R.layout.fragment_main, container, false);
            }

            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {

            super.onAttach(activity);
            callerActivity=(MainActivity) activity;
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }




}