package com.example.imageapp

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.google.accompanist.permissions.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App(modifier = Modifier.padding(15.dp))
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun App(modifier: Modifier) {

        val cameraPermissionState = rememberMultiplePermissionsState(
            permissions = listOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_CONTACTS
            )
        )
        val context = LocalContext.current

        val isImageAvailable = remember { mutableStateOf(false) }
        val imageUri = FileProvider.getUriForFile(
            context,
            BuildConfig.APPLICATION_ID + ".provider",
            createImageFile()
        )
        var capturedImageUri by remember {
            mutableStateOf<Uri?>(Uri.EMPTY)
        }

        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture(),
        ) {
            isImageAvailable.value = it
            capturedImageUri = imageUri
            Log.d("xml2208", "$capturedImageUri")
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier,
        ) {
            Button(onClick = {
                cameraPermissionState.launchMultiplePermissionRequest()
            }) {
                Text(stringResource(id = R.string.request_permission_btn))
            }

            cameraPermissionState.permissions.forEach { perm ->
                CheckingPermissionStatus(permissionState = perm)
            }

            if (cameraPermissionState.allPermissionsGranted) {
                Button(
                    onClick = {
                        cameraLauncher.launch(imageUri)
                    },
                ) {
                    Text(text = stringResource(R.string.open_camera_button))
                }
            }
            if (isImageAvailable.value && capturedImageUri != null) {
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = capturedImageUri,
                    contentDescription = stringResource(R.string.captured_image)
                )
            }
        }
    }

    private fun Context.createImageFile(): File {
        val timeStamp = SimpleDateFormat("ddMMyyyy", Locale.US).format(Date())
        val imageFileName = timeStamp + "_"
        return File.createTempFile(
            imageFileName,
            ".jpg",
            externalCacheDir
        )
    }


    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    private fun CheckingPermissionStatus(
        permissionState: PermissionState
    ) {
        when (permissionState.permission) {
            android.Manifest.permission.CAMERA -> {
                when {
                    permissionState.status.isGranted -> {
                        Text(text = stringResource(R.string.camera_perm_granted))
                    }
                    permissionState.status.shouldShowRationale -> {
                        Text(text = stringResource(R.string.alerting_camera_is_needed))
                    }
                }
            }
            android.Manifest.permission.READ_CONTACTS -> {
                when {
                    permissionState.status.isGranted -> {
                        Text(text = stringResource(R.string.contacts_perm_granted))
                    }
                    permissionState.status.shouldShowRationale -> {
                        Text(text = stringResource(R.string.alerting_contacts_is_needed))
                    }
                }
            }
        }

    }
}