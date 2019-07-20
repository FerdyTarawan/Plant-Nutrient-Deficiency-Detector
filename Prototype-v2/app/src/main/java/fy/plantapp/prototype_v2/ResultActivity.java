package fy.plantapp.prototype_v2;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

    private ImageView crop_image;
    private TextView result;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        crop_image = findViewById(R.id.imageView);
        result = findViewById(R.id.result);

        Intent intent = getIntent();
        String img_path = intent.getStringExtra("img");
        Uri crop = Uri.parse(img_path);
        crop_image.setImageURI(crop);

        String result_string = intent.getStringExtra("predict_result");
        result.setText(result_string);

    }
}
