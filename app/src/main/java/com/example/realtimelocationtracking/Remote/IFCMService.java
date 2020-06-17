package com.example.realtimelocationtracking.Remote;

//import android.database.Observable;
import com.example.realtimelocationtracking.Model.MyResponse;
import com.example.realtimelocationtracking.Model.Request;

import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import io.reactivex.Observable;

public interface IFCMService {
    @Headers({
        "Content-Type:application/json",
            "Authorization:key =AAAAOruPyQQ:APA91bEJQXdiF3XO4OzD1NiFX5DcP3JdwbKcbjRv5JDuSWx_9vIymQPfGIT_yiqJJuTK0I1bUzJc42HSQQJuhE7vjOryUWAeuCyvgdL20pmn-DLtm-kCSSj3xD6CNr5UCmvSprSiLs_1"

    })
    @POST("fcm/send")
    Observable<MyResponse> sendFriendRequestToUser(@Body Request body);
}
