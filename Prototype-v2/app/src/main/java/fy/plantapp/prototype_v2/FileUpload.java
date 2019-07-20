package fy.plantapp.prototype_v2;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface FileUpload {
    @Multipart
    @POST("predict")
    Call<Object> predict(
            @Part MultipartBody.Part file
    );
}
