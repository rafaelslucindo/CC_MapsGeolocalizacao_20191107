package br.com.rafael.meuappmapas;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_PERMISSION_LOCATION = 1;
    private GoogleMap mapa;
    private List<Marker> markerList = new ArrayList<>();
    //
    private FusedLocationProviderClient locationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inicializaComponentes();
        verificaPermissoes();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode
            , @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_PERMISSION_LOCATION)
        {
            for(int grant : grantResults)
            {
                if(grant != PackageManager.PERMISSION_GRANTED)
                {
                    this.finish();
                    return;
                }
            }
            inicializaCapturaLocalizacao();
        }
    }

    private void verificaPermissoes()
    {
       boolean ACCESS_COARSE_LOCATION = ActivityCompat
                .checkSelfPermission(this
                        , Manifest.permission.ACCESS_COARSE_LOCATION)
               == PackageManager.PERMISSION_GRANTED;

        boolean ACCESS_FINE_LOCATION = ActivityCompat
                .checkSelfPermission(this
                        , Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if(ACCESS_COARSE_LOCATION && ACCESS_FINE_LOCATION)
        {
            inicializaCapturaLocalizacao();
        }
        else
        {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSION_LOCATION);
        }


    }

    private void inicializaComponentes() {
        MapFragment mapFragment =
                (MapFragment) getFragmentManager().findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapa = googleMap;
        mapa.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        //
        mapa.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                adicionaMarcador(latLng);
                //
                CameraUpdate cameraUpdate = CameraUpdateFactory
                        .newCameraPosition(CameraPosition.fromLatLngZoom(latLng, 12));
                mapa.animateCamera(cameraUpdate);
            }
        });
    }

    private void adicionaMarcador(LatLng localizacao)
    {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(localizacao);
        markerOptions.title("Minha posição atual");
        markerOptions.draggable(true);
        //
        markerList.add(mapa.addMarker(markerOptions));
    }

    private void inicializaCapturaLocalizacao()
    {
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        //
        LocationRequest configuracaoLocalizacao = new LocationRequest();
        configuracaoLocalizacao.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        configuracaoLocalizacao.setInterval(1000);
        configuracaoLocalizacao.setFastestInterval(1000);
        //
        locationClient.requestLocationUpdates(configuracaoLocalizacao,
                locationCallback, Looper.getMainLooper());
    }

    private void cancelaCapturaLocalizacao()
    {
        //Cancela o listener de atualização
        if(locationClient != null)
            locationClient.removeLocationUpdates(locationCallback);
    }

    private LocationCallback locationCallback = new LocationCallback()
    {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location ultimaLocalizacao = locationResult.getLastLocation();
            LatLng latLng = new LatLng(ultimaLocalizacao.getLatitude()
                    , ultimaLocalizacao.getLongitude());
            adicionaMarcador(latLng);
            //
            CameraUpdate cameraUpdate = CameraUpdateFactory
                    .newCameraPosition(CameraPosition.fromLatLngZoom(latLng, 10));
            mapa.animateCamera(cameraUpdate);
            //
            cancelaCapturaLocalizacao();
        }
    };
}
