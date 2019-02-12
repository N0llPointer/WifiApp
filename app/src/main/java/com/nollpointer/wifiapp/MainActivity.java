package com.nollpointer.wifiapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "WifiApp";

    private final IntentFilter intentFilter = new IntentFilter();
    WifiP2pManager.Channel mChannel;
    WifiP2pManager mManager;

    WifiReceiver receiver;

    private List<WifiP2pDevice> devices = new ArrayList<>();
    private DevicesAdapter devicesAdapter;

    private RecyclerView devicesRecyclerView;
    private Button enableWifiButton;

    final HashMap<String, String> buddies = new HashMap<>();


    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            List<WifiP2pDevice> refreshedPeers = new ArrayList<>(peerList.getDeviceList());
            if (!refreshedPeers.equals(devices)) {
                devices.clear();
                devices.addAll(refreshedPeers);

                String[] peersArray = new String[devices.size()];
                for (int i = 0; i < peersArray.length; i++) {
                    peersArray[i] = devices.get(i).deviceName;
                }
                //peersArray = peers.toArray(peersArray);
//                peersAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.peer_layout, peersArray);
//                peersListView.setAdapter(peersAdapter);

                //peersAdapter.clear();
                //peersAdapter.addAll(peersArray);

                // If an AdapterView is backed by this data, notify it
                // of the change. For instance, if you have a ListView of
                // available peers, trigger an update.
                //((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();

                // Perform any other updates needed based on the new list of
                // peers connected to the Wi-Fi P2P network.
            }

            if (devices.size() == 0) {
                Log.d(TAG, "No devices found");
                return;
            } else {
                Log.d(TAG, "Some devices found");
            }
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(final WifiP2pInfo info) {
            //if(info.isGroupOwner)

//            if (mainBuddie == null)
//                mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
//                    @Override
//                    public void onSuccess() {
//                        Log.e("main", "removeGroup onSuccess");
//                    }
//
//                    @Override
//                    public void onFailure(int reason) {
//                        Log.e("main", "removeGroup onFailure -" + reason);
//                    }
//                });
            Log.e(TAG, "onConnectionInfoAvailable: " + info.isGroupOwner + " " + info.groupOwnerAddress.toString() + " " + info.groupFormed);

            InetAddress address = info.groupOwnerAddress;

            if (info.isGroupOwner) {
                //new Server().execute();
                createDialog();
            } else {
                new Client(address).execute();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        startRegistration();

//        android.R.layout.select_dialog_item

        Button peerSearch = findViewById(R.id.peerSearch);
        peerSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                int permissionRecord = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
//                if (permissionRecord != PackageManager.PERMISSION_GRANTED) {
//                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
//                } else {
//
////                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
////                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
////                            PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
////                    //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
////
////                }else{
////                    mManager.requestPeers(mChannel, mainActivity.peerListListener);
////                    //do something, permission was previously granted; or legacy device
////                }
//
//                    mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
//
//                        @Override
//                        public void onSuccess() {
//                            Toast.makeText(MainActivity.this, "Started P2P search ...", Toast.LENGTH_SHORT).show();
//                        }
//
//                        @Override
//                        public void onFailure(int reason) {
//                            Toast.makeText(MainActivity.this, "P2P search failed with code " + String.valueOf(reason), Toast.LENGTH_SHORT).show();
//                        }
//                    });

                discoverService();

            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //connect();
            }
        });

        enableWifiButton = findViewById(R.id.wifiButton);
        enableWifiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wifiManager.setWifiEnabled(true);
            }
        });

        //String[] peersArray = {"OnePlus 5", "Pixel 3", "Xiaomi Mi 8"};
        final List<String> list = Arrays.asList("OnePlus 5", "Pixel 3", "Xiaomi Mi 8");
        devicesAdapter = new DevicesAdapter(list, new DevicesAdapter.Listener() {
            @Override
            public void onClick(int position) {
                connect(devices.get(position));
            }
        });

        devicesRecyclerView = findViewById(R.id.devicesRecyclerView);
        devicesRecyclerView.setAdapter(devicesAdapter);
        devicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new WifiReceiver(mManager, mChannel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void connect(final WifiP2pDevice device) {
        // Picking the first device found on the network.

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = 0;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver notifies us. Ignore for now.
                Toast.makeText(MainActivity.this, "Successful connection to " + device.deviceName,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    class WifiReceiver extends BroadcastReceiver {

        MainActivity activity;
        WifiP2pManager manager;
        WifiP2pManager.Channel channel;

        public WifiReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, MainActivity activity) {
            this.activity = activity;
            this.manager = manager;
            this.channel = channel;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // Determine if Wifi P2P mode is enabled or not, alert
                // the Activity.
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    //activity.setIsWifiP2pEnabled(true);
                    Log.e(TAG, "Wifi is enabled");
                    enableWifiButton.setVisibility(View.GONE);
                } else {
                    Log.e(TAG, "Wifi is disabled");
                    enableWifiButton.setVisibility(View.VISIBLE);
                }
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

//                if (mManager != null) {
//                    mManager.requestPeers(mChannel, peerListListener);
//                }

                Log.e(TAG, "The peer list has changed");
                // The peer list has changed! We should probably do something about
                // that.

            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

                // Connection state changed! We should probably do something about
                // that.

                if (mManager == null) {
                    return;
                }
                NetworkInfo networkInfo = (NetworkInfo) intent
                        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if (networkInfo.isConnected()) {
                    // We are connected with the other device, request connection
                    // info to find group owner IP

                    mManager.requestConnectionInfo(mChannel, connectionListener);
                }

                Log.e(TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION: emm smth happened");

            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
//                DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
//                        .findFragmentById(R.id.frag_list);
                WifiP2pDevice device = (WifiP2pDevice) intent.getParcelableExtra(
                        WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);

                Log.e(TAG, "Device info: " + device.deviceName);

            }
        }
    }

    private void startRegistration() {
        //  Create a string map containing information about your service.
        Map record = new HashMap();
        record.put("listenport", String.valueOf(8080));
        //.put("buddyname", "John Doe" + (int) (228));
        record.put("available", "visible");

        // Service information.  Pass it an instance name, service type
        // _protocol._transportlayer , and the map containing
        // information other devices will want once they connect to this one.
        WifiP2pDnsSdServiceInfo serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance("_test123", "_presence._tcp", record);

        // Add the local service, sending the service info, network channel,
        // and listener that will be used to indicate success or failure of
        // the request.
        mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // Command successful! Code isn't necessarily needed here,
                // Unless you want to update the UI or add logging statements.
            }

            @Override
            public void onFailure(int arg0) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
            }
        });

        WifiP2pManager.DnsSdTxtRecordListener txtListener = new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            /* Callback includes:
             * fullDomain: full domain name: e.g "printer._ipp._tcp.local."
             * record: TXT record dta as a map of key/value pairs.
             * device: The device running the advertised service.
             */

            public void onDnsSdTxtRecordAvailable(
                    String fullDomain, Map record, WifiP2pDevice device) {
                Log.e(TAG, "DnsSdTxtRecord available -" + record.toString());
                buddies.put(device.deviceAddress, (String) record.get("buddyname"));

                if (devices.contains(device))
                    return;

                devices.add(device);

//                resourceType.deviceName = buddies
//                        .containsKey(resourceType.deviceAddress) ? buddies
//                        .get(resourceType.deviceAddress) : resourceType.deviceName;

                // Add to the custom adapter defined specifically for showing
                // wifi devices.
                Log.e(TAG, "onBonjourServiceAvailable " + device.deviceName);

                ArrayList<String> devicesTitles = new ArrayList<>();

                for (WifiP2pDevice d : devices) {
                    devicesTitles.add(d.deviceName);
                }

                devicesAdapter.updateDevices(devicesTitles);

            }
        };

        WifiP2pManager.DnsSdServiceResponseListener servListener = new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType,
                                                WifiP2pDevice resourceType) {

                // Update the device name with the human-friendly version from
                // the DnsTxtRecord, assuming one arrived.
                //mainBuddie = resourceType;

            }
        };

        mManager.setDnsSdResponseListeners(mChannel, servListener, txtListener);

        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mManager.addServiceRequest(mChannel,
                serviceRequest,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        // Success!
                    }

                    @Override
                    public void onFailure(int code) {
                        // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                    }
                });

    }


    private void discoverService() {

        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // Success!
                Toast.makeText(MainActivity.this, "Search Started",
                        Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(int code) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                switch (code) {
                    case WifiP2pManager.P2P_UNSUPPORTED:
                        Log.e(TAG, "P2P isn't supported on this device.");
                        break;
                    case WifiP2pManager.BUSY:
                        Log.e(TAG, "Busy");
                        break;
                    case WifiP2pManager.ERROR:
                        Log.e(TAG, "Error");
                        break;
                }
            }
        });
    }


    public void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите сообщение");
        final String[] messages = {"Good", "Normal", "Bad"};
        builder.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, messages), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new Server(messages[which]).execute();
            }
        });

        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }


    public class Client extends AsyncTask<Void, Void, String> {

        boolean shouldStop = false;

        InetAddress address;

        public Client(InetAddress address) {
            this.address = address;
        }

        @Override
        protected String doInBackground(Void... voids) {

            String message  = "ZERO ";

            Socket socket = new Socket();
            while (!shouldStop) {
                try {
                    if (socket != null)
                        socket.close();

                    socket = new Socket();
                    socket.connect(new InetSocketAddress(address, 8080), 1000);
                    Log.e(TAG, "Connected to socket");
                    Scanner scanner = new Scanner(socket.getInputStream()).useDelimiter("\\A");

                    while(scanner.hasNext())
                    //if (scanner.hasNext())
                        message += scanner.next() + " ";
                    //else
                      //  message = "ZERO";
                    Log.e(TAG, "run: " + message);

                    scanner.close();

                    if (!message.equals("ZERO"))
                        break;
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: ", e);
                }
            }


            /*try {
                Socket mSocket = new Socket();

                while (true) {
                    try {
                        if (mSocket != null)
                            mSocket.close();
                        Log.e(TAG, "Another Try");
                        mSocket = new Socket();
                        mSocket.connect(new InetSocketAddress(address, 8080), 1000);
                        Log.e(TAG, "Success??");
                        break;
                    } catch (IOException e) {

                    }
                }

                Log.e(TAG, "WAAAT??");
                Scanner scanner = new Scanner(mSocket.getInputStream()).useDelimiter("\\A");
                message = scanner.hasNext() ? scanner.next() : "ZERO";
                Log.e(TAG, "run: " + message);
            } catch (Exception e) {
                Log.e(TAG, "run: ", e);
                message = "ERROR";
            }*/

            return message;
        }

        public void stop(){
            shouldStop = true;
        }

        @Override
        protected void onPostExecute(String message) {
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
        }
    }


    public class Server extends AsyncTask<Void, Void, Void> {

        String message;

        public Server(String message) {
            this.message = message;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                ServerSocket mServerSocket = new ServerSocket(8080);
                Log.e(getClass().getSimpleName(), "Running on port: " + mServerSocket.getLocalPort());
                Socket mSocket = mServerSocket.accept();

                DataOutputStream mDataOutputStream = new DataOutputStream(mSocket.getOutputStream());
                mDataOutputStream.writeUTF(message);
                mDataOutputStream.flush();
                mDataOutputStream.close();
                mSocket.close();
                mServerSocket.close();
            } catch (Exception e) {
                Log.e(TAG, "run: ", e);
            }

            return null;
        }
    }

}
