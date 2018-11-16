package sadiq.raza.connectiondemo;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.http.conn.ConnectTimeoutException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    ConnectivityManager con;
    static  ImageView iv;
    static  Context context;
    BluetoothAdapter blueAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context=MainActivity.this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        con=(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        iv=findViewById(R.id.imageView);
        blueAdapter=BluetoothAdapter.getDefaultAdapter();
    }
    public void doThis(View view)
    {
        NetworkInfo networkInfo=con.getActiveNetworkInfo();
        if(networkInfo!=null && networkInfo.isConnected())
        {
            if(networkInfo.getType()==ConnectivityManager.TYPE_WIFI)
            {
                //Toast.makeText(this, "WI FI ", Toast.LENGTH_SHORT).show();
                String imagePath="https://assesszone.000webhostapp.com/client/login.php";

                new MyImageTask().execute(imagePath);
            }
            if(networkInfo.getType()==ConnectivityManager.TYPE_MOBILE)
            {
                Toast.makeText(this, "Mobile Data ", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public  void discoverDevices(View view)
    {

        if (blueAdapter.isDiscovering()) {
            blueAdapter.cancelDiscovery();
        }
        blueAdapter.startDiscovery();
    }

    public void startBluetooth(View view)
    {
        if(blueAdapter==null)
        {
            Toast.makeText(context, "Device does not support bluetooth", Toast.LENGTH_SHORT).show();
        }
        else
        {
            if(!blueAdapter.isEnabled())
            {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent,1);
            }
            if(blueAdapter.isEnabled())
            {

            }
        }
    }
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Toast.makeText(context, deviceName, Toast.LENGTH_SHORT).show();
                Log.e("Device name :  Address",deviceName+" " +deviceHardwareAddress);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==1)
        {
            if(resultCode==RESULT_OK)
            {
                Toast.makeText(context, "Bluetooth turned on", Toast.LENGTH_SHORT).show();

            }
             if(requestCode==RESULT_CANCELED)
            {
                Toast.makeText(context, "Bluetooth failed to turn on", Toast.LENGTH_SHORT).show();
            }
            blueAdapter.startDiscovery();
            Toast.makeText(MainActivity.this, "Finding devices...", Toast.LENGTH_SHORT).show();
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver,filter);

        }
    }
}

class MyImageTask extends AsyncTask<String,Void,Bitmap>
{
    ProgressDialog pb;
    @Override
    protected Bitmap doInBackground(String... strings) {
        return downloadImage(strings[0]);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pb=new ProgressDialog(MainActivity.context);
        pb.setMessage("Loading...");
        pb.show();
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        pb.dismiss();
        if(bitmap!=null)
        {
            //MainActivity.iv.setImageBitmap(bitmap);
            //MainActivity.iv.setScaleType(ImageView.ScaleType.FIT_XY);
        }
    }

    Bitmap downloadImage(String path)
    {
        Bitmap bitmap=null;
        try {
            URL url = new URL(path);
            HttpURLConnection connection=(HttpURLConnection)url.openConnection();
            connection.setReadTimeout(3000);
            connection.setConnectTimeout(3000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();

            int code =connection.getResponseCode();
            if(code==HttpURLConnection.HTTP_CLIENT_TIMEOUT)
                Log.e("Dd","Time out");
            if(code==HttpURLConnection.HTTP_GATEWAY_TIMEOUT)
                Log.e("Dd","Time outsss");
            if(code==HttpURLConnection.HTTP_OK)
            {
                InputStream stream = connection.getInputStream();
                /*if(stream!=null)
                {
                    bitmap=BitmapFactory.decodeStream(stream);
                }*/

                BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                String result="";
                String line="";
                while((line=br.readLine())!=null)
                {
                    result+=line;
                }
                Log.e("Result",result);
            }

        }
        catch (ConnectTimeoutException e)
        {

            Toast.makeText(MainActivity.context, "Connection Time out", Toast.LENGTH_SHORT).show();
        }
        catch (SocketTimeoutException e)
        {
            Log.e("Dd","Time out");
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  bitmap;
    }
}
