package com.example.evcharger;
/*

This application shows electric vehicle charging stations
across some countries.

Author: Andy Duverneau

 */
import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewPropertyAnimator;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {

    /*

    define our variables
     */

    MapView mapView;

    String url;

    Context context = this;

    SearchView charger_search;

    String charging;

    Activity activity = this;

    final int REQUEST_CODE =0;

    JSONObject jsonObjectAddressInfo;

    double latitude;

    Marker mark;

    double longitudeP, latitudeP;

    double longitude;

    GoogleMap googleForMap;

    List<JSONObject> jsonObjectList;

    TextView textViewTitle, textView_Operational,

    textView_Fast_Charge,textView_Quantity,textViewPublic,textView_PowerKW,

    textView_current_type, textView_usage_cost, textView_country, textViewAddress, textViewTown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // call to our reference
        referenceView();

        charger_search.setSubmitButtonEnabled(true);

        mapView.onCreate(savedInstanceState);

        mapView.onSaveInstanceState(savedInstanceState);

        charger_search.setQueryHint("search EvCharger hint \"us\"");

        default_text();

         /*

        make a call to search query
        In this we validate the url

         */

        Dictionary<String, String> dictionary_next = dixtionary(new Hashtable<>());

        charger_search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                //  Dictionary<String, String> dictionary_next = dixtionary(new Hashtable<>());
                if (dictionary_next.keys().hasMoreElements()) {
                dictionary_next.keys().asIterator().forEachRemaining(new Consumer<String>() {
                    @Override
                    public void accept(String string) {
                        if (Objects.equals(query.strip().toLowerCase(),string)) {


                            url = dictionary_next.get(string).toString();
                            jsonRequest();
                        }
                        else {

                            // call to animation
                            animation();
                        }

                    }
                });
               }


                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return true;
            }
        });

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CODE:
                if(ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    jsonRequest();

                }

                break;
            default:
                ActivityCompat.requestPermissions(activity,permi(),REQUEST_CODE);


        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
/*
Create a json array request
param(s): zero
return: void
 */

    public  void jsonRequest(){
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                /*


                get a call back to google map

                 */
                mapView.getMapAsync(new OnMapReadyCallback() {

                    String[]permi = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

                    @Override
                    public void onMapReady(@NonNull GoogleMap googleMap) {
                        googleForMap = googleMap;
                        if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {

                            googleMap.setMyLocationEnabled(true);


                            for(int i =0; i < response.length(); i++){

                                try {
                                    JSONObject jsonObject = response.getJSONObject(i);
                                    jsonObjectAddressInfo=    jsonObject.getJSONObject("AddressInfo");
                                    latitude =jsonObjectAddressInfo.getDouble("Latitude");
                                    longitude =jsonObjectAddressInfo.getDouble("Longitude");
                                    mark = googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)));


                                    mark.setTitle(String.valueOf(i));

                                    /*

                                    do something when one of the marker is clicked

                                     */
                                    googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                        @Override
                                        public boolean onMarkerClick(@NonNull Marker marker) {

                                            //  json_response(response,marker,googleMap);


                                            if(!Objects.equals(marker.getTitle(), marker.getTitle())){

                                                json_response(response,marker,googleMap);

                                            }
                                            else {

                                                empty_text();
                                                default_text();
                                                json_response(response,marker,googleMap);

                                            }

                                            return false;
                                        }
                                    });


                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }


                            }


                        }
                        else{

                            ActivityCompat.requestPermissions(activity,permi(),REQUEST_CODE);


                        }

                    }
                });



            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d(error.getMessage(), error.getMessage());

            }
        });
        Volley.newRequestQueue(context).add(jsonArrayRequest);

    }
    @Override
    protected void onStart() {

        mapView.onStart();

        super.onStart();
    }

    @Override
    protected void onResume() {

        mapView.onResume();

        super.onResume();
    }

/*
Create a simple reference method to our views and widgets to keep the code clean
and organize; so that we know where to look for them
param(s): zero
return: void
 */

    public void referenceView(){

        mapView = findViewById(R.id.mapView);

        charger_search = findViewById(R.id.search);

        textViewTitle = findViewById(R.id.textView3);

        textViewTown = findViewById(R.id.textView13);

        textView_current_type = findViewById(R.id.textView9);

        textView_country = findViewById(R.id.textView11);

        textViewAddress = findViewById(R.id.textView12);

        textView_Quantity = findViewById(R.id.textView2);

        textViewPublic = findViewById(R.id.textView5);

        textView_PowerKW = findViewById(R.id.textView8);

        textView_usage_cost = findViewById(R.id.textView10);

        textView_Fast_Charge = findViewById(R.id.textView);

        textView_Operational = findViewById(R.id.textView4);

    }

    /*
    params: zero
    return a string of permissions

     */
    public String[] permi(){
        String[] permi = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

        return  permi;
    }

    /*

    create an animation


     */
    public void animation(){

        Dialog dialog = new Dialog(this);

        Button button = new Button(this);

        button.setBackgroundColor(Color.WHITE);

        button.setTextSize(14f);
        button.setAllCaps(false);
        // button.setBackgroundColor(Color.RED);

        ViewPropertyAnimator propertyAnimator = button.animate();
        //Give a rotational float value
        propertyAnimator.rotation(6.6f);

        propertyAnimator.start();

        button.setText(R.string.valid_name);
        dialog.setContentView(button);

        dialog.show();


    }
    /*

    create a default text to display

     */
    public void default_text(){
        textView_country.append("Country:");

        textViewTown.append("Town:");

        textViewAddress.append("Address:");

        textViewPublic.append("Public:");

        textView_Quantity.append("Quantity:");

        textView_Operational.append("In use:");

        textView_usage_cost.append("Usage Cost:");

        textView_PowerKW.append("PowerKW:");

        textViewTitle.append("Title:");

        textView_current_type.append("Current type: ");

        textView_Fast_Charge.append("FastChargeCapable:");

    }
    /*

    create an empty text to reset

     */
    public void empty_text(){
        textView_country.setText(R.string.empty_string);

        textViewTown.setText(R.string.empty_string);

        textViewAddress.setText(R.string.empty_string);

        textViewPublic.setText(R.string.empty_string);

        textView_Quantity.setText(R.string.empty_string);

        textView_Operational.setText(R.string.empty_string);

        textView_usage_cost.setText(R.string.empty_string);

        textView_PowerKW.setText(R.string.empty_string);

        textViewTitle.setText(R.string.empty_string);

        textView_current_type.setText(R.string.empty_string);

        textView_Fast_Charge.setText(R.string.empty_string);

    }

    /*

    create a json response

     */
    public void json_response(JSONArray response, Marker marker, GoogleMap googleMap){

        try {

            JSONObject jsonObject = response.getJSONObject(Integer.parseInt(marker.getTitle()));

            JSONObject jsonObjectStatusType = jsonObject.getJSONObject("StatusType");
            jsonObjectStatusType = jsonObject.getJSONObject("StatusType");

            textView_Operational.append("\t" + jsonObjectStatusType.getString("IsOperational"));

            textView_usage_cost.append("\t" + jsonObject.getString("UsageCost"));

            JSONObject jsonObjectUsageType = jsonObject.getJSONObject("UsageType");

            textViewPublic.append("\t" + jsonObjectUsageType.getString("Title"));

            jsonObjectAddressInfo = jsonObject.getJSONObject("AddressInfo");


            latitudeP = jsonObjectAddressInfo.getDouble("Latitude");

            longitudeP = jsonObjectAddressInfo.getDouble("Longitude");


            textViewAddress.append("\t" + jsonObjectAddressInfo.getString("AddressLine1"));

            textViewTown.append("\t" + jsonObjectAddressInfo.getString("Town"));

            JSONObject jsonObjectCountry = jsonObjectAddressInfo.getJSONObject("Country");

            textView_country.append("\t" + jsonObjectCountry.getString("Title"));

            JSONArray jsonObjectConnections = jsonObject.getJSONArray("Connections");

            JSONObject jsonObjectLevel = jsonObjectConnections.getJSONObject(0);

            textView_Quantity.append("\t" + jsonObjectLevel.getString("Quantity"));

            textView_PowerKW.append("\t" + jsonObjectLevel.getString("PowerKW"));

            JSONObject object = jsonObjectLevel.getJSONObject("Level");

            textViewTitle.append("\t" + object.getString("Title"));


            google_map_location(googleMap);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


    }

    /*

    set a location

     */
    public void google_map_location(GoogleMap googleMap){

        googleMap.setLocationSource(new LocationSource() {
            @Override
            public void activate(@NonNull OnLocationChangedListener onLocationChangedListener) {

                Location location = new Location(Location.convert(0.0, Location.FORMAT_DEGREES));



                location.setLatitude(latitudeP);
                location.setLongitude(longitudeP);

                onLocationChangedListener.onLocationChanged(location);

            }

            @Override
            public void deactivate() {

            }
        });

    }
    /*

      create a dictionary
      param(s) dictionary
      return dictionary

     */
    public Dictionary<String,String> dixtionary(Dictionary<String,String> dictionary){

        dictionary.put("canada","https://api.openchargemap.io/v3/poi/?output=json&countrycode=ca&maxresults=100?key=5709af2d-598b-4027-95a0-ffb036bc1851");
        dictionary.put("us","https://api.openchargemap.io/v3/poi/?output=json&countrycode=us&maxresults=100?key=5709af2d-598b-4027-95a0-ffb036bc1851");
        dictionary.put("uk","https://api.openchargemap.io/v3/poi/?output=json&countrycode=uk&maxresults=100?key=5709af2d-598b-4027-95a0-ffb036bc1851");
        return dictionary;
    }

}