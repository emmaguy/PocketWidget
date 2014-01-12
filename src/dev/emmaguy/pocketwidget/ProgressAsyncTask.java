package dev.emmaguy.pocketwidget;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public abstract class ProgressAsyncTask<A, B, C> extends AsyncTask<A, B, C> {

    private ProgressDialog dialog;

    public ProgressAsyncTask(Context c, String dialogMessage) {
        this.dialog = new ProgressDialog(c);
        this.dialog.setMessage(dialogMessage);
        this.dialog.setCancelable(false);
    }

    @Override
    protected void onPreExecute() {
        this.dialog.show();
    }

    @Override
    protected void onPostExecute(final C success) {
        try
        {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch(Exception e) {

        }
    }
}