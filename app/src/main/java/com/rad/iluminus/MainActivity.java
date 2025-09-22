package com.rad.iluminus;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavArgument;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.rad.iluminus.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> enableBluetoothLauncher;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitleEnabled(true);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        navController.addOnDestinationChangedListener((navController1, navDestination, bundle) ->
        {
            binding.toolbar.setTitle(navDestination.getLabel());
            collapsingToolbarLayout.setTitle(navDestination.getLabel());

            NavArgument nArg = navDestination.getArguments().get("enable_fab");
            if (nArg != null)
                binding.FABSearchDevices.setVisibility((Boolean)nArg.getDefaultValue() ? View.VISIBLE : View.GONE);
            else
                binding.FABSearchDevices.setVisibility(View.GONE);
        });
        
        enableBluetoothLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result ->
            {
                if (result.getResultCode() == Activity.RESULT_OK)
                    Toast.makeText(this, "Bluetooth enabled successfully", Toast.LENGTH_SHORT).show();
                else
                {
                    showBluetoothTurnOnRationale();
                    Toast.makeText(this, "Bluetooth enabling denied", Toast.LENGTH_SHORT).show();
                }
            }
        );

        BluetoothAdapter bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null)
        {
            // Device doesn't support Bluetooth
            Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_LONG).show();
            finish();
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void requestBluetoothEnable()
    {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        enableBluetoothLauncher.launch(enableBtIntent);
    }

    private void showBluetoothTurnOnRationale()
    {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder
            .setTitle("Bluetooth Enabled Required")
            .setMessage("Bluetooth need to be enabled for this feature.")
            .setPositiveButton("Grant Permissions", (dialog, id) -> this.requestBluetoothEnable())
            .setNegativeButton("Não permitir", (dialog, id) ->
            {
                Toast.makeText(this, "Permissão negada. Aplicação será fechada.", Toast.LENGTH_SHORT).show();
                this.finish();
            });
        
        // Create the AlertDialog.
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}