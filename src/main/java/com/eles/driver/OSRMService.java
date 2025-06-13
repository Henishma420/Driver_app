package com.eles.driver;

import com.eles.driver.RouteResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OSRMService {
    @GET("route/v1/driving/{coordinates}")
    Call<RouteResponse> getRoute(
            @Path("coordinates") String coordinates,
            @Query("overview") String overview,
            @Query("geometries") String geometries
    );
}
