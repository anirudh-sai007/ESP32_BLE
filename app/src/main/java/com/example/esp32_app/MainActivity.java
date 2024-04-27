package com.example.esp32_app;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothGattDescriptor;
import android.location.LocationManager;
import android.provider.Settings;
import android.app.AlertDialog;
//import android.content.DialogInterface;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
//import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
//import android.view.View;
//import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private final ArrayList<String> deviceList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private BluetoothGatt mBluetoothGatt;
    //private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mCharacteristic;
//    private TextView dataTextView;
     Button btcycle,btstop,bta,btb;
    private TextView halfDataTextView;
//    private BluetoothDevice mDevice;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_CODE = 2;
    private final ArrayList<String> messageList = new ArrayList<>();
    private ArrayAdapter<String> messageAdapter;
    // Add this at the top of your MainActivity class
    private static final String DEVICE_MAC_ADDRESS = "F4:12:FA:67:38:39";


    private int rom_degrees = Integer.MIN_VALUE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize Bluetooth adapter
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bluetoothManager.getAdapter();
//        dataTextView = findViewById(R.id.dataTextView);
        halfDataTextView = findViewById(R.id.halfDataTextView);
        btcycle=findViewById(R.id.buttonnnn);
        btstop=findViewById(R.id.buttonnnns);
        bta=findViewById(R.id.buttonnnna);
        btb=findViewById(R.id.buttonnnnb);
        btstop.setOnClickListener(v -> {
            String c = "d";
            Toast.makeText(MainActivity.this, c, Toast.LENGTH_SHORT).show();

            sendcmdtoesp32(c);
        });
        bta.setOnClickListener(v -> {
            String c = "l";
            Toast.makeText(MainActivity.this, c, Toast.LENGTH_SHORT).show();

            sendcmdtoesp32(c);
        });
        btb.setOnClickListener(v -> {
            String c = "t";
            Toast.makeText(MainActivity.this, c, Toast.LENGTH_SHORT).show();

            sendcmdtoesp32(c);
        });
        btcycle.setOnClickListener(v -> {
            String c = "c20";
            Toast.makeText(MainActivity.this, c, Toast.LENGTH_SHORT).show();

            sendcmdtoesp32(c);
        });


        // Check if BLE is supported
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Check and request permissions
        if (ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION"}, PERMISSION_REQUEST_CODE);
        }

        // Set up ListView
        ListView messageListView = findViewById(R.id.messageListView);
        messageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messageList);
        messageListView.setAdapter(messageAdapter);
        ListView listView = findViewById(R.id.listView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
        listView.setAdapter(adapter);

        // Set up item click listener
        listView.setOnItemClickListener((parent, view, position, id) -> {
            // Implement code to connect to the selected BLE device here
            String deviceInfo = deviceList.get(position);
            String deviceAddress = deviceInfo.substring(deviceInfo.indexOf("MAC: ") + 5).trim();

            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
            mBluetoothGatt = device.connectGatt(MainActivity.this, false, gattCallback);

            Toast.makeText(MainActivity.this, "Connecting to " + deviceAddress, Toast.LENGTH_SHORT).show();

        });
    }

        private void checkLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;
        boolean networkEnabled = false;

        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {

            Toast.makeText(MainActivity.this, ex.toString(),Toast.LENGTH_SHORT).show();
        }

        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            Toast.makeText(MainActivity.this, ex.toString(),Toast.LENGTH_SHORT).show();
        }

        if (!gpsEnabled && !networkEnabled) {
            // Notify user
            new AlertDialog.Builder(this)
                    .setMessage("Location services must be enabled to use this app")
                    .setPositiveButton("Enable Location", (paramDialogInterface, paramInt) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    // BluetoothGatt callback
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show());
                gatt.discoverServices();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show());
            }
        }
//        @SuppressLint("MissingPermission")
//        @Override
//        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                // Replace with your actual SERVICE_UUID
//                String SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";
//                BluetoothGattService service = gatt.getService(UUID.fromString(SERVICE_UUID));
//                if (service != null) {
//                    // Replace with your actual CHARACTERISTIC_UUID
//                    String CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8";
//                    mCharacteristic = service.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID));
//                    gatt.setCharacteristicNotification(mCharacteristic, true);
//                    readCharacteristic();
//                }
//            }
//        }

        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Replace with your actual SERVICE_UUID
                String SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";
                BluetoothGattService service = gatt.getService(UUID.fromString(SERVICE_UUID));
                if (service != null) {
                    // Replace with your actual CHARACTERISTIC_UUID
                    String CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8";
                    mCharacteristic = service.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID));

                    // Enable notifications for this characteristic
                    gatt.setCharacteristicNotification(mCharacteristic, true);

                    // Write on the config descriptor to enable notification
                    BluetoothGattDescriptor descriptor = mCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);

                    readCharacteristic();
                }
            }
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                displayData(characteristic.getValue());
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            displayData(characteristic.getValue());
        }
    };

    private void writeToFile(String data) {
        FileOutputStream fos = null;
        try {

            fos = openFileOutput("esp32_data.txt", MODE_PRIVATE | MODE_APPEND);

            fos.write(data.getBytes());

            fos.write("\n".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void displayData(byte[] data) {
        if (data != null && data.length > 0) {
            runOnUiThread(() -> {
                String dataStr = new String(data);
                messageList.add(dataStr); // Add the message to the list
                messageAdapter.notifyDataSetChanged();
                try {
                    int intData = Integer.parseInt(dataStr);

                    // Add valid data to the list
                    messageList.add(dataStr);
                    messageAdapter.notifyDataSetChanged();

                    writeToFile(dataStr);

                    if (intData > rom_degrees) {
                        rom_degrees = intData;
                        halfDataTextView.setText(String.valueOf(rom_degrees));
                    }
                } catch (NumberFormatException e) {
                    halfDataTextView.setText(e.getMessage());
                }
            });
        }
    }

    @SuppressLint("MissingPermission")
    private void readCharacteristic() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null || mCharacteristic == null) {
            return;
        }
        mBluetoothGatt.readCharacteristic(mCharacteristic);
    }




    private void sendcmdtoesp32(String command) {
        if (mBluetoothGatt == null || mCharacteristic == null) {

            Log.e("BLE", "BluetoothGatt or characteristic is null");
            return;
        }
        mCharacteristic.setValue(command.getBytes());
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        boolean status = mBluetoothGatt.writeCharacteristic(mCharacteristic);
        Log.d("BLE", "Write status: " + status);
    }
    /** @noinspection deprecation*/
    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();
        checkLocationEnabled();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            startScanning();

        }
    }


    @SuppressLint("MissingPermission")
    private void startScanning() {
        mBluetoothLeScanner.startScan(mScanCallback);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(DEVICE_MAC_ADDRESS);
        mBluetoothGatt = device.connectGatt(MainActivity.this, false, gattCallback);
    }

    @SuppressLint("MissingPermission")
    private void stopScanning() {
        mBluetoothLeScanner.stopScan(mScanCallback);
    }

    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            @SuppressLint("MissingPermission") String deviceName = device.getName();
            @SuppressLint("MissingPermission") String deviceInfo = "Name: " + device.getName() + " - MAC: " + device.getAddress();
            if (!deviceList.contains(deviceInfo) && deviceName != null ) {
                deviceList.add(deviceInfo);
                adapter.notifyDataSetChanged();
            }
        }
    };



    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            stopScanning();
            if (mBluetoothGatt != null) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mBluetoothGatt.close();
                mBluetoothGatt = null;
            }
        }
    }


@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, "Bluetooth must be enabled", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}