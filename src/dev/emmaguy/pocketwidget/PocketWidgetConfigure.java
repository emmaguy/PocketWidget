package dev.emmaguy.pocketwidget;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class PocketWidgetConfigure extends Activity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	setContentView(R.layout.activity_configure);
	
	findViewById(R.id.login_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
	if (view.getId() == R.id.login_button) {

	    Toast.makeText(getApplicationContext(), "login! ", Toast.LENGTH_SHORT).show();
	    
	    new RetrievePocketApiRequestTokenAsyncTask(getResources().getString(R.string.pocket_consumer_key_mobile)).execute();
	}
    }
}
