package org.spechide.btappnder.whatsapptransmitter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.util.Log;
import android.app.*;
import java.net.*;
import java.io.*;

public class MainActivity extends ActionBarActivity {

    TextView messageText;
    Button uploadButton;
    EditText txt;

    int serverResponseCode = 0;

    ProgressDialog dialog = null;
    String upLoadServerUri = null ;

    private static final int READ_REQUEST_CODE = 42;
    private static final String TAG="spechide";

    /**********  File Path *************/
    String uploadFilePath = "";
    String uploadFileName = "";
    /**********  File Path *************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uploadButton = (Button) findViewById(R.id.button);
        messageText = (TextView) findViewById(R.id.textView);
        txt = (EditText) findViewById(R.id.editText);
        //messageText.setText("Uploading file path :- "+uploadFilePath+"");

        /************* Php script path ****************/
        upLoadServerUri = "http://btappnder.freeiz.com/server.php";
        /************* Php script path ****************/

        uploadButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog = ProgressDialog.show(MainActivity. this , "", "Uploading file... \n . . . Please wait", true );

                new Thread( new Runnable() {
                    public void run() {
                        runOnUiThread( new Runnable() {
                            public void run() {
                                messageText.setText("uploading started.....");
                            }
                        });

                        uploadFile(uploadFilePath);

                    }
                }).start();
            }
        });

    }

    public int uploadFile(String sourceFileUri) {

        String fileName = sourceFileUri;

        HttpURLConnection conn = null ;
        DataOutputStream dos = null ;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte [] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {

            dialog.dismiss();

            //Log.e("uploadFile", "Source File not exist :"+ uploadFilePath);

            runOnUiThread( new Runnable() {
                public void run() {
                    messageText.setText("Source File does not exist :"
                            +uploadFilePath );
                }
            });

            return 0;

        }
        else if(fileName.endsWith(".jpg") || fileName.endsWith(".avi") || fileName.endsWith(".mp3") || fileName.endsWith(".png") || fileName.endsWith(".mp4")){
            dialog.dismiss();
            runOnUiThread( new Runnable() {
                public void run() {
            messageText.setText("the service is provided in the hope that it will be useful.\n please do not misuse the service.");
        }
    });
            return 0;
        }
        else
        {
            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput( true ); // Allow Inputs
                conn.setDoOutput( true ); // Allow Outputs
                conn.setUseCaches( false ); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""+ fileName + "\"" + lineEnd);

                        dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte [bufferSize];

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
                String serverResponseMessage = conn.getResponseMessage();

                //Log.i("uploadFile", "HTTP Response is : "+ serverResponseMessage + ": " + serverResponseCode);

                if (serverResponseCode == 200){

                    runOnUiThread( new Runnable() {
                        public void run() {

                            String msg = txt.getText().toString()+"\n"
                                    +" \"http://btappnder.freeiz.com/uploads/"
                                    +uploadFileName.replace(" ","_")+"\""
                                    +"\n\n============AD============\n\n" +
                                    "To send any type of file over WhatsApp : " +
                                    "\"http://btappnder.freeiz.com/uploads/application.apk\"";

                            //messageText.setText(msg);
                            Toast.makeText(MainActivity. this , "File Upload Complete.",
                                    Toast.LENGTH_SHORT).show();
                            whatsappintent(msg);
                        }
                    });
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            }catch (MalformedURLException ex) {

                dialog.dismiss();
                ex.printStackTrace();

                runOnUiThread( new Runnable() {
                    public void run() {
                        messageText.setText("MalformedURLException Exception : check script url.");
                        Toast.makeText(MainActivity. this , "MalformedURLException",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                //Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                dialog.dismiss();
                e.printStackTrace();

                runOnUiThread( new Runnable() {
                    public void run() {
                        messageText.setText("upload to server exception");
                        Toast.makeText(MainActivity. this , "Exception",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                //Log.e("Upload file to server Exception", "Exception : " + e.getMessage(), e);
            }
            dialog.dismiss();
            return serverResponseCode;

        } // End else block
    }


    public void whatsappintent(String msg){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
        sendIntent.setType("text/plain");
        sendIntent.setPackage("com.whatsapp");
        startActivity(sendIntent);
    }

    public void filechooserPfm(View v){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivityForResult(intent,READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,Intent resultData) {

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
                Log.i(TAG, "name : "+uri.getLastPathSegment());
                uploadFilePath = getRealPathFromURI(MainActivity.this, uri);
                uploadFileName = uri.getLastPathSegment();
                messageText.setText("Uploading file path :- "+uploadFilePath+"");
            }
        }
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(MainActivity.this,"SpEcHiDe",Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
