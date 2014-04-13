package com.srbs.drawpath;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivity extends Activity {
	SettingsDialog dialog;
	DrawView drawView;
	SharedPreferences prefs;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog = new SettingsDialog();
        setContentView(R.layout.activity_main);
        drawView = (DrawView) findViewById(R.id.drawview);
        prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String timerWaitPrefKey = getResources().getString(R.string.timer_wait_preference_key);
    	String distThresholdPrefKey = getResources().getString(R.string.dist_threshold_preference_key);
        prefs.edit().putLong(timerWaitPrefKey, getResources().getInteger(R.integer.timer_wait_default)).commit();
        prefs.edit().putLong(distThresholdPrefKey, getResources().getInteger(R.integer.dist_threshold_default)).commit();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_edit_params:
                showSettingsPopup();
                return true;
            case R.id.action_settings:
                showSettingsPopup();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	void showSettingsPopup() {
    	dialog.show(getFragmentManager(), "tag");
    }
}

class SettingsDialog extends DialogFragment implements OnSeekBarChangeListener {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
    	MainActivity currentActivity = (MainActivity) getActivity();
    	String timerWaitPrefKey = currentActivity.getResources().getString(R.string.timer_wait_preference_key);
    	String distThresholdPrefKey = currentActivity.getResources().getString(R.string.dist_threshold_preference_key);
    	
        AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
        LayoutInflater layoutInflater = currentActivity.getLayoutInflater();
        View contentView = layoutInflater.inflate(R.layout.settings_dialog, null);
        
        builder.setMessage("Settings").setView(contentView);

        SeekBar timerWait = (SeekBar) contentView.findViewById(R.id.timer_wait_seek_bar);
        timerWait.setOnSeekBarChangeListener(this);
        timerWait.setProgress((int) currentActivity.prefs.getLong(timerWaitPrefKey,
    			currentActivity.getResources().getInteger(R.integer.timer_wait_default)));
        
        SeekBar distThreshold = (SeekBar) contentView.findViewById(R.id.dist_threshold_seek_bar);
        distThreshold.setOnSeekBarChangeListener(this);
        distThreshold.setProgress((int) currentActivity.prefs.getLong(distThresholdPrefKey,
    			currentActivity.getResources().getInteger(R.integer.dist_threshold_default)));
        
        // Create the AlertDialog object and return it
        return builder.create();
    }
    
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    	MainActivity currentActivity = (MainActivity) getActivity();
    	String timerWaitPrefKey = currentActivity.getResources().getString(R.string.timer_wait_preference_key);
    	String distThresholdPrefKey = currentActivity.getResources().getString(R.string.dist_threshold_preference_key);
    	Log.i("key", String.valueOf(currentActivity.prefs.contains(timerWaitPrefKey)));
    	switch(seekBar.getId()) {
    	case R.id.timer_wait_seek_bar:
    		currentActivity.prefs.edit().putLong(timerWaitPrefKey, progress).commit();
    		break;
    	case R.id.dist_threshold_seek_bar:
    		currentActivity.prefs.edit().putLong(distThresholdPrefKey, progress).commit();
    		break;
    	}
    }

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
}
