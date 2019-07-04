package nikhil.bhople.kioskmode;

import androidx.appcompat.app.AppCompatActivity;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import nikhil.bhople.kioskmode.kiosk.KioskModeManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        KioskModeManager.startKioskMode(this);

        findViewById(R.id.btnExit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    KioskModeManager.exitKioskMode(MainActivity.this);
                }catch (Exception e){
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });

        findViewById(R.id.btnEditWithKiosk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    KioskModeManager.exitKioskMode(MainActivity.this);

                    // remove device admin
                    DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                    if (mDPM != null) {
                        mDPM.clearDeviceOwnerApp(BuildConfig.APPLICATION_ID);
                    } else {
                        Toast.makeText(MainActivity.this, "DPM is null", Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}
