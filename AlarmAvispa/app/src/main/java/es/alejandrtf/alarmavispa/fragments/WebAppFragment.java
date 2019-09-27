package es.alejandrtf.alarmavispa.fragments;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import es.alejandrtf.alarmavispa.R;
import es.alejandrtf.alarmavispa.utilities.Constants;
import es.alejandrtf.alarmavispa.utilities.Internet;

/**
 * A simple {@link Fragment} subclass.
 */
public class WebAppFragment extends Fragment {
    private ProgressDialog mProgressDialog;


    public WebAppFragment() {
        // Required empty public constructor
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showWebViewAlertDialogApplicationWeb();
    }

    /**
     * Method shows an alert dialog that load the application web using a webview
     */
    private void showWebViewAlertDialogApplicationWeb() {


        AlertDialog.Builder ad = new AlertDialog.Builder(getContext());
        ad.setTitle(getString(R.string.web_app_with_privacy));

        View alertDialogLayout = getLayoutInflater().inflate(R.layout.alertdialog_with_webview_layout, null);
        WebView wv = alertDialogLayout.findViewById(R.id.wvPrivacy);
        wv.getSettings().setBuiltInZoomControls(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            wv.setWebContentsDebuggingEnabled(true);
        wv.loadUrl(Constants.URL_APPLICATION_WEB);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                    view.loadUrl(url);

                return true;
            }

            //show alertDialog is loading...
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (Internet.isInternetConectionActive(getContext())) {
                    mProgressDialog = new ProgressDialog(getContext());
                    mProgressDialog.setMessage(getString(R.string.message_loading));
                    mProgressDialog.setCancelable(true);
                    mProgressDialog.show();
                }
            }


            //hide alertdialog shown loading...
            @Override
            public void onPageFinished(WebView view, String url) {
                if (mProgressDialog != null)
                    mProgressDialog.dismiss();
            }


            // catching errors
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext())
                        .setTitle(getString(R.string.error_loading__web_title))
                        .setMessage(getString(R.string.error_loading__web_message) + ": " + description)
                        .setPositiveButton(getString(R.string.accept), null);
                builder.show();
            }
        });

        if(Internet.isInternetConectionActive(getContext()))
            ad.setView(wv);
        else {
            TextView textError=new TextView(getContext());
            textError.setGravity(Gravity.CENTER);
            textError.setText(R.string.error_conection_internet_loading_privacy);
            ad.setView(textError);
        }
        ad.setCancelable(false);
        ad.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                //navigate to mainFragment from app
                Navigation.findNavController(getActivity(), R.id.my_nav_host_fragment).navigate(R.id.action_webAppFragment_to_notifySightingFragment);

            }
        });
        ad.create().show();
    }
}



