package com.abajeli.testsample;

/**
 * Created by abajeli on 31/12/14.
 */

    import android.app.ListActivity;
    import android.content.Intent;
    import android.os.Bundle;
    import android.view.View;
    import android.widget.ArrayAdapter;
    import android.widget.ListView;
    import android.app.ListFragment;
    import android.widget.Toast;

    import java.util.List;



    public class WhitelistList extends ListActivity {
        String[] values;
        public void Whitelist(String[] values){


        }

        public void onCreate(Bundle icicle) {
            super.onCreate(icicle);
            String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
                    "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
                    "Linux", "OS/2" };
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, values);
            setListAdapter(adapter);
        }

        @Override
        protected void onListItemClick(ListView l, View v, int position, long id) {
            String item = (String) getListAdapter().getItem(position);
            Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
        }
    }