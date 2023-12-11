package ie.setu.pupplan.api

import ie.setu.pupplan.models.PetLocationModel
import retrofit2.Call
import retrofit2.http.*

interface PetLocationService {
    @GET("/petLocations")
fun findall(): Call<List <PetLocationModel>>

@GET("/petLocations/{email}")
fun findall(@Path("email") email: String?)
        : Call<List <PetLocationModel>>

@GET("/petLocations/{email}/{id}")
fun get(@Path("email") email: String?,
        @Path("id") id: String): Call <PetLocationModel>

@DELETE("/petLocations/{email}/{id}")
fun delete(@Path("email") email: String?,
           @Path("id") id: String): Call<PetLocationWrapper>

@POST("/petLocations/{email}")
fun post(@Path("email") email: String?,
         @Body petLocation: PetLocationModel)
        : Call<PetLocationWrapper>

@PUT("/petLocations/{email}/{id}")
fun put(@Path("email") email: String?,
        @Path("id") id: String,
        @Body petLocation: PetLocationModel
): Call<PetLocationWrapper>
}