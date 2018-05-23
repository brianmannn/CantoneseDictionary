package com.crazyhands.dictionary.Api.Service;

import com.crazyhands.dictionary.Api.Model.POJ;
import com.crazyhands.dictionary.Api.Model.Word;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface DictionaryClient {
    @POST("dictionary")
    Call<Word> CreateWord(@Body Word word);

    @PUT("dictionary")
    Call<POJ> UpdateWord(@Body Word word);

    @GET("dictionary")
    Call <List<Word>> GetWords();

    @DELETE("dictionary/{id}")
    Call<Word> DeleteWord(@Path("id") int id);

    @GET("dictionary/{id}")
    Call <POJ>  GetWord(@Path("id") int id);

    @Multipart
    @POST("dictionary")
    Call<ResponseBody> upload(
     //       @Part("description") RequestBody description,
            @Part MultipartBody.Part file
    );
}