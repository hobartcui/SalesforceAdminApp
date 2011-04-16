package com.pocketsoap.admin;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.pocketsoap.salesforce.SalesforceApi;
import com.pocketsoap.salesforce.SalesforceApi.User;

/** the user list, this defaults to showing the recent users, and allows for a search */
public class UserListActivity extends ListActivity implements OnEditorActionListener, ApiAsyncTask.ActivityCallbacks {

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.user_admin);
		search = (EditText)findViewById(R.id.search_text);
		search.setOnEditorActionListener(this);
	}

	private SalesforceApi salesforce;
	private EditText search;

	@Override
	public void onResume() {
		super.onResume();
		search.setText("");
		try {
			salesforce = new SalesforceApi(getIntent());
			RecentUserListTask t = new RecentUserListTask(this);
			t.execute();
		} catch (URISyntaxException e) {
			showError(e);
		}
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	switch (item.getItemId()) {
    		case R.id.menu_logout:
    			new RefreshTokenStore(this).clearSavedData();
    			Intent i = new Intent(this, Login.class);
    			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			startActivity(i);
    			finish();
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
    
	public void showError(Exception ex) {
        Toast.makeText(
                this, 
                getString(R.string.api_failed, ex.getMessage()),
                Toast.LENGTH_LONG ).show();
	}
	
	public void setBusy(boolean b) {
		setProgressBarIndeterminateVisibility(b);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// the user tapped a row in the list, serialize up the data for that row, and star the detail page activity
		Intent d = new Intent(this, UserDetailActivity.class);
		d.putExtras(getIntent());
		try {
			d.putExtra(UserDetailActivity.EXTRA_USER_JSON, new ObjectMapper().writeValueAsString(v.getTag()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		startActivity(d);
	}

	// build an adapter for this list of users, so they can be rendered in the list view.
	private <T> void bindUserList(List<User> users) {
		UserListAdapter a = new UserListAdapter(this, R.layout.user_row, users);
		this.setListAdapter(a);
	}
	
	// Adapter/Binder that renders the list view rows.
	private class UserListAdapter extends ArrayAdapter<User> {

		public UserListAdapter(Context context, int textViewResourceId, List<User> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null)
				convertView = ((LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.user_row, null);
			User u = getItem(position);
			setText(convertView, R.id.user_name, u.Name);
			setText(convertView, R.id.user_username, u.Username);
			setText(convertView, R.id.user_title, u.Title);
			convertView.setTag(u);
			return convertView;
		}
		
		private void setText(View v, int textViewId, String text) {
			TextView tv = (TextView)v.findViewById(textViewId);
			tv.setText(text);
		}
	}
	
	/** background task to fetch the recent users list, and then bind it to the UI */
	private class RecentUserListTask extends ApiAsyncTask<Void, List<User>> {

		RecentUserListTask(ActivityCallbacks activity) {
			super(activity);
		}
		
		@Override
		protected List<User> doApiCall(Void ... params) throws IOException {
			return salesforce.getRecentUsers();
		}
		
		@Override
		protected void handleResult(List<User> result) {
			bindUserList(result);
		}
	}

	/** background task to run the search query, and bind the results to the UI */
	private class UserSearchTask extends ApiAsyncTask<String, List<User>> {

		UserSearchTask(ActivityCallbacks activity) {
			super(activity);
		}
		
		@Override
		protected List<User> doApiCall(String... params) throws Exception {
			return salesforce.usernameSearch(params[0], 25);
		}

		@Override
		protected void handleResult(List<User> result) {
			bindUserList(result);
		}
	}
	
	/** called when the user clicks the search button or enter on the keyboard */
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		UserSearchTask t = new UserSearchTask(this);
		t.execute(v.getText().toString());
		return true;
	}
}