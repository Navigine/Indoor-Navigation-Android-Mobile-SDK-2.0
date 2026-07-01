package com.navigine.locationviewcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.navigine.idl.java.NavigineSdk
import com.navigine.locationviewcompose.Utils.REQUIRED_PERMISSIONS
import com.navigine.locationviewcompose.screens.SamplesNav
import com.navigine.locationviewcompose.ui.theme.LocationViewComposeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        runCatching {
            val sdk = NavigineSdk.getInstance().apply {
                setServer("https://ips.yourhost.com")
                setUserHash("0000-0000-0000-0000")
            }
            sdk.locationManager.locationId = Utils.LOC_ID
        }.onFailure { it.printStackTrace() }

        enableEdgeToEdge()
        setContent {
            LocationViewComposeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                    PermissionHandler(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ){
                        SamplesNav()
                    }
                }
            }
        }
    }
}


@Composable
private fun PermissionHandler(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    var permissionsGranted by remember {
        mutableStateOf(
            Utils.arePermissionsGranted(
                context
            )
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            permissionsGranted = Utils.arePermissionsGranted(context)
        }
    )

    LaunchedEffect(permissionsGranted) {
        if (!permissionsGranted) permissionLauncher.launch(REQUIRED_PERMISSIONS)
    }

    if (permissionsGranted)
        content()
    else Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("grant perms")
        Button(onClick = {
            permissionLauncher.launch(REQUIRED_PERMISSIONS)
        }) {
            Text("grant")
        }
    }

}



