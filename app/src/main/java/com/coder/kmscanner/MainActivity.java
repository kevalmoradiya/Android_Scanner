package com.coder.kmscanner;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private View mview;
    private Uri outputFuri;
    public final static String EXTRA_MESSAGE = "com.coder.kmscanner.message";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ViewCompat.setElevation(toolbar,10);

        mview=findViewById(R.id.mainView);
        mview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }


        });

        // Permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
        }

    }

    private void uploadImage() {
        final String filename = "image"+ System.currentTimeMillis() + ".jpg";
        final File pictures = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), filename);
        outputFuri = Uri.fromFile(pictures);
        final List<Intent> camIntents = new ArrayList<>();
        final Intent capIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(capIntent, 0);
        for(ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(capIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFuri);
            camIntents.add(intent);
        }
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_PICK);
        final Intent chIntent = Intent.createChooser(galleryIntent, "Select Source");
        chIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, camIntents.toArray(new Parcelable[camIntents.size()]));

        startActivityForResult(chIntent, 1);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                final boolean isCamera;
                if (data == null || data.getData() == null) {
                    isCamera = true;
                } else {
                    final String action = data.getAction();
                    isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
                }
                Uri selectedImageUri;
                if (isCamera) {
                    selectedImageUri = outputFuri;
                    if (selectedImageUri == null) return;
                    nextStep(selectedImageUri.toString());

                } else {
                    selectedImageUri = data.getData();
                    if (selectedImageUri == null) return;
                    nextStep(selectedImageUri.toString());
                }
            }
        }
    }

    private void nextStep(String file) {
        Intent intent = new Intent(this, CropRotate.class);
        intent.putExtra(EXTRA_MESSAGE, file);
        startActivity(intent);
    }
}
