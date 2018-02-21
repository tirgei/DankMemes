package com.gelostech.dankmemes.activities;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.gelostech.dankmemes.R;
import com.gelostech.dankmemes.commoners.FirebaseConstants;
import com.gelostech.dankmemes.models.UploadModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class UploadMemeActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{
    private Toolbar toolbar;
    private EditText editText;
    private Button selectImage, selectDate;
    private ImageView iv;
    private Uri imageUri;
    private static final int GALLERY_REQUEST = 1;
    //private String uploadYear, uploadMonth, uploadDay;
    private long time;
    private String uploadDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_meme);

        toolbar = (Toolbar) findViewById(R.id.upload_meme_activity_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editText = (EditText) findViewById(R.id.upload_date);
        editText.setEnabled(false);

        selectImage = (Button) findViewById(R.id.button_select_image);
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        selectDate = (Button) findViewById(R.id.select_upload_date);
        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDate();
            }
        });

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        uploadDate = sdf.format(System.currentTimeMillis());
        editText.setText(uploadDate);

        iv = (ImageView) findViewById(R.id.display_image);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.upload_meme_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()){

            case android.R.id.home:
                //Toast.makeText(this, "Take me to my faves!", Toast.LENGTH_SHORT).show();
                finish();

                return true;

            case R.id.upload_meme:
                //Toast.makeText(this, imageUri.toString(), Toast.LENGTH_SHORT).show();
                if(!TextUtils.isEmpty(editText.getText().toString()) && imageUri != null){
                    uploadFile();
                } else {
                    Toast.makeText(UploadMemeActivity.this, "Date or image is empty!", Toast.LENGTH_SHORT).show();
                }
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        }
        else {
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
         if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
        //if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){

            imageUri = data.getData();
            //iv.setImageURI(imageUri);

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);

          //Toast.makeText(this, "Image saved to:\n" + data.getExtras().get("data"), Toast.LENGTH_LONG).show();
             
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                iv.setImageURI(resultUri);
                imageUri = resultUri;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Log.d("Cropping Error: ", result.getError().getMessage());
            }
        }

    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void uploadFile(){

        InputStream stream = null;
        try {
            stream = getContentResolver().openInputStream(imageUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Bitmap mImageUri1 = BitmapFactory.decodeStream(stream);
        //mSelectImage.setImageBitmap(mImageUri1);
        imageUri = getUri(this, mImageUri1);

        if(imageUri != null){
            final ProgressDialog pd = new ProgressDialog(this);
            pd.show();
            time = System.currentTimeMillis();
            final String name = "IMG-" + time;
            long timeMillis=0;
            //final String uploadDate = uploadDay + "/" + uploadMonth + "/" + uploadYear;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            try{
                Date d = sdf.parse(uploadDate);
                timeMillis = d.getTime();

            } catch (ParseException e){
                e.printStackTrace();
            }

            final String dbRef = FirebaseConstants.MEMES_PATH_UPLOADS + "/" + 2017 + "_" + timeMillis;
            final String uploadDay = 2017 + "_" + timeMillis;
            final SimpleDateFormat day = new SimpleDateFormat("dd");
            final SimpleDateFormat month = new SimpleDateFormat("MMM");

            FirebaseStorage fs = FirebaseStorage.getInstance();
            final DatabaseReference fd = FirebaseDatabase.getInstance().getReference(dbRef);

            //StorageReference fbRef = fs.getReference().child("2017/July/03/image1.jpg");
            StorageReference fbRef = fs.getReference().child(2017 + "/" + month.format(System.currentTimeMillis()) + "/" + uploadDay + "/" + name);
            fbRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            pd.dismiss();
                            Toast.makeText(getApplicationContext(), "Image uploaded!", Toast.LENGTH_SHORT).show();
                            iv.setImageResource(R.drawable.ic_image);

                            String uploadId = fd.push().getKey();
                            String picKey = uploadId;
                            UploadModel upload = new UploadModel(picKey, name, taskSnapshot.getDownloadUrl().toString(), uploadDay);
                            fd.child(uploadId).setValue(upload);


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();

                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                            //for(int i= 0; i<100; i++) {
                                pd.setMessage("Uploading Image...");
                            pd.setCancelable(false);
                            //}
                            //pd.setProgress((int) progress);
                        }
                    });

        } else {
            Toast.makeText(getApplicationContext(), "No image to upload!", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri getUri(Context context, Bitmap bitmap){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null );

        return Uri.parse(path);
    }

    private void pickDate(){
        Calendar cal = Calendar.getInstance();
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                UploadMemeActivity.this, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show(getFragmentManager(), "Datepickerdialog");
        datePickerDialog.setThemeDark(true);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String date =dayOfMonth + "/" + (monthOfYear+1) + "/" + year;
        String [] months = {"Jan", "Feb", "March", "April", "May", "June", "July", "Aug", "Sept", "Oct", "Nov", "Dec"};
        /*this.uploadYear = "" + year;
        this.uploadMonth = "" + (monthOfYear + 1);
        this.uploadDay = "" + dayOfMonth;*/
        //Toast.makeText(this, date, Toast.LENGTH_SHORT).show();
        editText.setText(date);
    }



}
