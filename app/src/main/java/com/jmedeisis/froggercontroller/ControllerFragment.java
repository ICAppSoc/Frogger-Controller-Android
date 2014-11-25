package com.jmedeisis.froggercontroller;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/** The piece of UI responsible for transmitting frog controls. */
public class ControllerFragment extends Fragment {

    /* Called when the UI is ready to be created. You return the root View for this Fragment. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        /*
         * On click, each button transmits its corresponding command asynchronously.
         */
        rootView.findViewById(R.id.buttonUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransmitCommandTask commandTask = new TransmitCommandTask();
                commandTask.execute("up");
            }
        });
        rootView.findViewById(R.id.buttonLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransmitCommandTask commandTask = new TransmitCommandTask();
                commandTask.execute("left");
            }
        });
        rootView.findViewById(R.id.buttonRight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransmitCommandTask commandTask = new TransmitCommandTask();
                commandTask.execute("right");
            }
        });
        rootView.findViewById(R.id.buttonDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransmitCommandTask commandTask = new TransmitCommandTask();
                commandTask.execute("down");
            }
        });

        return rootView;
    }

    /**
     * Asynchronously (on a background thread that does not lock up the UI thread)
     * transmits a frog command.
     */
    public class TransmitCommandTask extends AsyncTask<String, Void, Void> {
        private final String LOG_TAG = TransmitCommandTask.class.getSimpleName();

        // Runs on a background thread.
        @Override
        protected Void doInBackground(String... params) {

            if(params.length < 1){
                // We do not have the necessary input!
                return null;
            }
            // Assumes first string is the command.
            String command = params[0];

            HttpURLConnection urlConnection = null;
            try {
                // Construct the URL for the Frogger command.
                String address = "http://frog.blazingbear.net/keyboard/" + command;
                URL url = new URL(address);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.connect();

                Log.d(LOG_TAG, "POSTing to " + address);

                // look for a response
                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }

                // read and drop any input - we don't care about the response!
                while(-1 != inputStream.read());

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return null;
        }

    }
}
