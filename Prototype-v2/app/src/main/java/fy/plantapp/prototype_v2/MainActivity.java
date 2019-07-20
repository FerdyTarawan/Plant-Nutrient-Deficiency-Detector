package fy.plantapp.prototype_v2;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


import java.io.File;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private ImageButton cameraBtn;
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    public static final String ALLOW_KEY = "ALLOWED";
    public static final String CAMERA_PREF = "camera_pref";
    private int REQ_CODE = 1;
    private Uri mCropImageUri;
    private String predict;
    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraBtn = findViewById(R.id.cameraBtn);
        //browseBtn = findViewById(R.id.button);
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
            }
        });

    }

    public void openCamera(){

        CropImage.startPickImageActivity(this);
    }

    public void browse(){
        CropImage.startPickImageActivity(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                mCropImageUri = imageUri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},   CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                // no permissions required or already granted, can start crop image activity
                startCropImageActivity(imageUri);
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            //Uri Of Cropped Image:
            Uri imageUri = result.getUri();
            uploadFile(imageUri);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                startCropImageActivity(mCropImageUri);

            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .start(this);
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    private void uploadFile(final Uri uri){
        predict = "none";

        File file = new File(uri.getPath());
        RequestBody filepart = RequestBody.create(
                MediaType.parse(getMimeType(uri.toString())),
                file
        );

        MultipartBody.Part files = MultipartBody.Part.createFormData("image",file.getName(),filepart);

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("http://35.247.145.169/")
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.build();

        FileUpload client = retrofit.create(FileUpload.class);

        Call call = client.predict(files);

        final ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMax(100);
        progressDialog.setMessage("loading....");
        progressDialog.setTitle("Detection Progress");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // show dialog
        progressDialog.show();


        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call call, Response response) {
                progressDialog.dismiss();
                Gson gson =  new Gson();
                predict = gson.toJson(response.body());
                JsonObject jobj = gson.fromJson(predict, JsonObject.class);
                JsonArray jarr = jobj.getAsJsonArray("predictions");
                predict = jarr.get(0).toString();

                if(predict.equals("\"healthy\""))
                {
                    predict = "healthy";
                }else if(predict.equals("\"calcium_deficiency\"")){
                    predict = "Calcium Deficient";
                }else if(predict.equals("\"kalium_deficiency\"")){
                    predict = "Kalium Deficient";
                }else if(predict.equals("\"nitrogen_deficiency\"")){
                    predict = "Nitrogen Deficient";
                }else if(predict.equals("\"phosporus_deficiency\"")){
                    predict = "Phosporus Deficient";
                }

                System.out.println(predict);
                Intent toResult = new Intent(getApplicationContext(),ResultActivity.class);
                toResult.putExtra("img",uri.toString());
                toResult.putExtra("predict_result",predict);
                context.startActivity(toResult);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                System.out.println("Upload failed");
            }
        });
    }
}

