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
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/** The piece of UI responsible for transmitting frog controls. */
public class ControllerFragment extends Fragment {
    private static final long VIBRATION_DURATION_MS = 50;

    private Vibrator vibrator;
    private TextView scoreText;

    /* Called when the UI is ready to be created. You return the root View for this Fragment. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        scoreText = (TextView) rootView.findViewById(R.id.scoreText);

        // Grab a reference to the system vibrator service. We need a Context to do this,
        // we use the Activity this Fragment is attached to.
        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        /*
         * On click, each button transmits its corresponding command asynchronously.
         * It also causes the device to vibrate!
         *
         * Oh, and let's grab the latest score for good measure.
         */
        rootView.findViewById(R.id.buttonUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transmitCommandAndGetScore("up");
            }
        });
        rootView.findViewById(R.id.buttonLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transmitCommandAndGetScore("left");
            }
        });
        rootView.findViewById(R.id.buttonRight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transmitCommandAndGetScore("right");
            }
        });
        rootView.findViewById(R.id.buttonDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transmitCommandAndGetScore("down");
            }
        });

        return rootView;
    }

    private void transmitCommandAndGetScore(String command){
        TransmitCommandTask commandTask = new TransmitCommandTask();
        commandTask.execute(command);

        vibrator.vibrate(VIBRATION_DURATION_MS);

        new GetScoreTask().execute();
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

    public class GetScoreTask extends AsyncTask<Void, Void, String> {
        private final String LOG_TAG = GetScoreTask.class.getSimpleName();

        // Runs on a background thread.
        @Override
        protected String doInBackground(Void... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the score as a string.
            String scoreResponse = null;

            try {
                // Construct the URL to retrieve the score
                URL url = new URL("http://frog.blazingbear.net/score");

                // Create the request to Frogger, and open the connection.
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String.
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }

                // We got this far - this means our buffer holds our response!
                scoreResponse = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code did not manage to get in touch with Yoda, there's no point in
                // displaying the output.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return scoreResponse;
        }

        // Called after and with the result of doInBackground(). Runs on the UI thread.
        @Override
        protected void onPostExecute(String result){
            if(null != result){
                scoreText.setText("Score: " + result.trim());
            } else {
                Log.d(LOG_TAG, "Failed to retrieve score from server.");
            }
        }

    }
}
