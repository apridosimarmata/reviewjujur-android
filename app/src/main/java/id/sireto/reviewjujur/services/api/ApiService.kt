package id.sireto.reviewjujur.services.api

import id.sireto.reviewjujur.models.BaseResponse
import id.sireto.reviewjujur.models.UserEmailAuthenticationPost
import id.sireto.reviewjujur.models.UserPost
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("/users")
    suspend fun registerUser(@Body user : UserPost) : Response<BaseResponse>

    @POST("/auth/authentication/email")
    suspend fun authenticateUserByEmail(@Body userEmailAuthenticationPost: UserEmailAuthenticationPost) : Response<BaseResponse>

    @GET("/auth/authentication/phone/{whatsappNo}")
    suspend fun requestUserVerificationCode(@Path("whatsappNo") whatsappNo : String) : Response<BaseResponse>

    @GET("/auth/authorization")
    suspend fun authorizeUser(@Header("Access-Token") accessToken : String, @Header("Access-Refresh-Token") refreshToken : String) : Response<BaseResponse>

    @GET("/auth/authorization/refresh")
    suspend fun refreshUserToken(@Header("Access-Refresh-Token") refreshToken : String) : Response<BaseResponse>
}