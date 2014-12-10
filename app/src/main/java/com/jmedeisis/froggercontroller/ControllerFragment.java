package com.jmedeisis.froggercontroller;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
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
    private static final long VIBRATION_DURATION_MS = 50;

    /* Called when the UI is ready to be created. You return the root View for this Fragment. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Grab a reference to the system vibrator service. We need a Context to do this,
        // we use the Activity this Fragment is attached to.
        final Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        /*
         * On click, each button transmits its corresponding command asynchronously.
         * It also causes the device to vibrate!
         */
        rootView.findViewById(R.id.buttonUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransmitCommandTask commandTask = new TransmitCommandTask();
                commandTask.execute("up");

                vibrator.vibrate(VIBRATION_DURATION_MS);
            }
        });
        rootView.findViewById(R.id.buttonLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransmitCommandTask commandTask = new TransmitCommandTask();
                commandTask.execute("left");

                vibrator.vibrate(VIBRATION_DURATION_MS);
            }
        });
        rootView.findViewById(R.id.buttonRight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransmitCommandTask commandTask = new TransmitCommandTask();
                commandTask.execute("right");

                vibrator.vibrate(VIBRATION_DURATION_MS);
            }
        });
        rootView.findViewById(R.id.buttonDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransmitCommandTask commandTask = new TransmitCommandTask();
                commandTask.execute("down");

                vibrator.vibrate(VIBRATION_DURATION_MS);
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
