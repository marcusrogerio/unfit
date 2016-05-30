package com.cbcdn.dev.unfit.helpers;

import android.bluetooth.BluetoothGatt;
import android.content.SharedPreferences;
import android.util.Log;
import com.cbcdn.dev.unfit.helpers.ConstMapper.Command;
import com.cbcdn.dev.unfit.helpers.ConstMapper.Characteristic;
import com.cbcdn.dev.unfit.BLEDevice;

public class SyncCallback extends BLECallback {
    private enum SyncState {
        STARTED,
        USER_DATA,
        WEAR_LOCATION;
    }

    private SyncState syncState = SyncState.STARTED;
    private SharedPreferences preferences = null;

    public SyncCallback(SharedPreferences preferences) {
        super();
        this.preferences = preferences;
    }

    @Override
    public void start(BLEDevice device) {
        Log.d("SyncCallback", "Sync callback started");
        device.requestPriorityRead(Characteristic.DEVICE_INFO, this);
    }

    @Override
    public void writeCompleted(BLEDevice self, Characteristic characteristic, int status) {
        switch(syncState){
            case USER_DATA:
                if(status == BluetoothGatt.GATT_SUCCESS){
                    Log.d("SyncCallback", "User info written, writing wear location");
                    this.syncState = SyncState.WEAR_LOCATION;
                    return;
                }
                break;
        }

        Log.e("SyncCallback", "Write operation on " + characteristic + " failed: " + status);
    }

    @Override
    public void readCompleted(BLEDevice self, Characteristic characteristic, int status, byte[] data) {
        switch(syncState){
            case STARTED:
                if(status == BluetoothGatt.GATT_SUCCESS){
                    Log.d("SyncCallback", "Device info read, writing user info");
                    this.syncState = SyncState.USER_DATA;
                    self.requestPriorityWrite(Command.SET_USER_DATA, this, this.preferences);
                    return;
                }
                break;
        }

        Log.e("SyncCallback", "Read operation on " + characteristic + " failed: " + status);
    }
}
