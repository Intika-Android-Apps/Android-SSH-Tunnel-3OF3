/* Copyright (c) 2009, Nathan Freitas, Orbot / The Guardian Project - http://openideals.com/guardian */
/* See LICENSE for licensing information */

package org.sshtunnel;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class AppManager extends Activity implements OnCheckedChangeListener, OnClickListener {

	private static ProxyedApp[] apps = null;

	private ListView listApps;
	
	private AppManager mAppManager;
	
	public final static String PREFS_KEY_PROXYED = "Proxyed";


	private boolean appsLoaded = false;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		this.setContentView(R.layout.layout_apps);
		
		mAppManager = this;


		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		listApps = (ListView)findViewById(R.id.applistview);

		if (!appsLoaded)
			loadApps();
	}



	private void loadApps ()
	{
        final ProxyedApp[] apps = getApps(this);
        
        Arrays.sort(apps, new Comparator<ProxyedApp>() {
			public int compare(ProxyedApp o1, ProxyedApp o2) {
				if (o1.isProxyed() == o2.isProxyed()) return o1.getName().compareTo(o2.getName());
				if (o1.isProxyed()) return -1;
				return 1;
			}
        });
        
        final LayoutInflater inflater = getLayoutInflater();
		
        final ListAdapter adapter = new ArrayAdapter<ProxyedApp>(this,R.layout.layout_apps_item,R.id.itemtext,apps) {
        	public View getView(int position, View convertView, ViewGroup parent) {
       			ListEntry entry;
        		if (convertView == null) {
        			// Inflate a new view
        			convertView = inflater.inflate(R.layout.layout_apps_item, parent, false);
       				entry = new ListEntry();
       				entry.icon = (ImageView) convertView.findViewById(R.id.itemicon);
       				entry.box = (CheckBox) convertView.findViewById(R.id.itemcheck);
       				entry.text = (TextView) convertView.findViewById(R.id.itemtext);
       				
       				entry.text.setOnClickListener(mAppManager);
       				entry.text.setOnClickListener(mAppManager);
       				
       				convertView.setTag(entry);
       			
       				entry.box.setOnCheckedChangeListener(mAppManager);
        		} else {
        			// Convert an existing view
        			entry = (ListEntry) convertView.getTag();
        		}
        		
        		
        		final ProxyedApp app = apps[position];
        		
        	
        		entry.icon.setImageDrawable(app.getIcon());
        		entry.icon.setAdjustViewBounds(true);
        		entry.icon.setMaxWidth(32);
        		entry.icon.setMaxHeight(32);
        		
        		entry.text.setText(app.getName());
        		
        		final CheckBox box = entry.box;
        		box.setTag(app);
        		box.setChecked(app.isProxyed());
        		
        		entry.text.setTag(box);
        		entry.icon.setTag(box);
        		
       			return convertView;
        	}
        };
        
        listApps.setAdapter(adapter);
        
        appsLoaded = true;
		   
	}
	
	private static class ListEntry {
		private CheckBox box;
		private TextView text;
		private ImageView icon;
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();
		
		//Log.d(getClass().getName(),"Exiting Preferences");
	}


	public static ProxyedApp[] getApps (Context context)
	{

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		String tordAppString = prefs.getString(PREFS_KEY_PROXYED, "");
		String[] tordApps;
		
		StringTokenizer st = new StringTokenizer(tordAppString,"|");
		tordApps = new String[st.countTokens()];
		int tordIdx = 0;
		while (st.hasMoreTokens())
		{
			tordApps[tordIdx++] = st.nextToken();
		}
		
		Arrays.sort(tordApps);
		
		//else load the apps up
		PackageManager pMgr = context.getPackageManager();
		
		List<ApplicationInfo> lAppInfo = pMgr.getInstalledApplications(0);
		
		Iterator<ApplicationInfo> itAppInfo = lAppInfo.iterator();
		
		apps = new ProxyedApp[lAppInfo.size()];
		
		ApplicationInfo aInfo = null;
		
		int appIdx = 0;
		
		while (itAppInfo.hasNext())
		{
			aInfo = itAppInfo.next();
			
			apps[appIdx] = new ProxyedApp();
			
			apps[appIdx].setEnabled(aInfo.enabled);
			apps[appIdx].setUid(aInfo.uid);
			apps[appIdx].setUsername(pMgr.getNameForUid(apps[appIdx].getUid()));
			apps[appIdx].setProcname(aInfo.processName);
			apps[appIdx].setName(pMgr.getApplicationLabel(aInfo).toString());
			apps[appIdx].setIcon(pMgr.getApplicationIcon(aInfo));
			
			// check if this application is allowed
			if (Arrays.binarySearch(tordApps, apps[appIdx].getUsername()) >= 0) {
				apps[appIdx].setProxyed(true);
			}
			else
			{
				apps[appIdx].setProxyed(false);
			}
			
			appIdx++;
		}
		
		return apps;
	}
	

	public void saveAppSettings (Context context)
	{
		if (apps == null)
			return;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

	//	final SharedPreferences prefs = context.getSharedPreferences(PREFS_KEY, 0);

		StringBuilder tordApps = new StringBuilder();
		
		for (int i = 0; i < apps.length; i++)
		{
			if (apps[i].isProxyed())
			{
				tordApps.append(apps[i].getUsername());
				tordApps.append("|");
			}
		}
		
		Editor edit = prefs.edit();
		edit.putString(PREFS_KEY_PROXYED, tordApps.toString());
		edit.commit();
		
	}
	

	/**
	 * Called an application is check/unchecked
	 */
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		final ProxyedApp app = (ProxyedApp) buttonView.getTag();
		if (app != null) {
			app.setProxyed(isChecked);
		}
		
		saveAppSettings(this);

	}



	@Override
	public void onClick(View v) {
		
		CheckBox cbox = (CheckBox)v.getTag();
		
		final ProxyedApp app = (ProxyedApp)cbox.getTag();
		if (app != null) {
			app.setProxyed(!app.isProxyed());
			cbox.setChecked(app.isProxyed());
		}
		
		saveAppSettings(this);
		
	}
	
}
