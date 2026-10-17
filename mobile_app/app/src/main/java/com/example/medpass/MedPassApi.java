package com.example.medpass;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface MedPassApi {

    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest req);

    @GET("verify")
    Call<VerifyResponse> verify(
            @Query("doc") String doc,
            @Query("guard") String guard
    );

    @GET("admin/logs")
    Call<List<AccessLogDto>> getLogs(@Query("guard") String guard);
}
