package com.mmey.reklamyonetim;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, PopupMenu.OnMenuItemClickListener {

    private static final int REQUEST_CODE = 101;
    private EditText editTextLat;
    private EditText editTextLong;
    private EditText editTextSearchCompany;
    private Button getCurrentLocationButton;
    private Button searchByCompany;
    private RecyclerView mRecyclerView;
    private LocationAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Company> companiesNearBy;

    private GoogleMap mMap;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference adsRef = database.getReference("Advertisement");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        companiesNearBy = new ArrayList<>();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        buildRecyclerView();
        fetchLastLocation();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        editTextLat = (EditText) findViewById(R.id.editTextLat);
        editTextLong = (EditText) findViewById(R.id.editTextLong);
        editTextSearchCompany = (EditText) findViewById(R.id.editTextSearchCompany);
        searchByCompany = (Button) findViewById(R.id.searchByCompany);

        getCurrentLocationButton = (Button) findViewById(R.id.buttonCurrent);
        getCurrentLocationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                fetchLastLocation();
            }
        });

        // sirket ismiyle arama
        searchByCompany.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String companyName = editTextSearchCompany.getText().toString();

                if (TextUtils.isEmpty(editTextSearchCompany.getText().toString())) {
                    Toast.makeText(MapsActivity.this, "Şirket adı boş bırakılamaz.", Toast.LENGTH_SHORT).show();
                    return;
                }
                // isSearchingByCategory: True for categorySearch, false for companyName based searches.
                buildAlertForThreshold(companyName, false);
            }
        });

        boolean fromNotification = getIntent().getBooleanExtra("fromNotification", false);
        if (fromNotification) {
            fetchLastLocation();
        }
    }

    public void showCategories(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.category_menu);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        // isSearchingByCategory: True for categorySearch, false for companyName based searches.
        buildAlertForThreshold(item.toString(), true);
        return true;
    }

    // Firebase'den secilen kategori ve esik degeri dikkate alarak verileri cekme islemi.
    // Ardindan Carview icerisinde, haritanın altinda gosterilerecek. Ayrica bildirim yollanacak.
    private void populateCompanies(String searchFor, final String threshold, final Boolean isSearchingByCategory) {

        String searchParameter = "";

        // Kategori ile mi, sirket adina gore mi araniyor.. Firebase sorgulari icin.
        if (isSearchingByCategory == true)
            searchParameter = "companyCategory";
        else
            searchParameter = "companyName";

        adsRef.orderByChild(searchParameter).equalTo(searchFor).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mMap.clear();companiesNearBy.clear();
                    mMap.addMarker(new MarkerOptions().position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())).title("Konumum"));
                    for (DataSnapshot advSnap : dataSnapshot.getChildren()) {
                        Company company = advSnap.getValue(Company.class);

                        // uygun kategorideki sirketler, threshold icerisinde mi kontrolu
                        if (calculateDistance(currentLocation.getLatitude(), currentLocation.getLongitude(),
                                Double.parseDouble(company.getCompanyLocationLat()), Double.parseDouble(company.getCompanyLocationLong())) < Double.parseDouble(threshold)) {
                            companiesNearBy.add(company);
                            // eger yakinlardaysa haritada goster.
                            Log.d("ActivitySirket", company.toString());
                            LatLng latLng = new LatLng(Double.parseDouble(company.getCompanyLocationLat()), Double.parseDouble(company.getCompanyLocationLong()));

                            MarkerOptions options = new MarkerOptions()
                                    .position(latLng).title(company.getCompanyName());
                            Marker marker = mMap.addMarker(options);

                            // aramadan geliyorsa markeri bulunan mekana gotur.
                            if (isSearchingByCategory == false) {
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(company.getCompanyLocationLat()), Double.parseDouble(company.getCompanyLocationLong())), 16.2f));
                                marker.showInfoWindow();
                            }


                            generateNotification(company);
                        }
                    }
                    mRecyclerView.setLayoutManager(mLayoutManager);
                    mRecyclerView.setAdapter(mAdapter);
                    Toast.makeText(MapsActivity.this, "Yakınlarda " + companiesNearBy.size() + " nokta belirlendi.", Toast.LENGTH_SHORT).show();
                }
                // ilgili sorgu ile alakali herhangi bir kayit donmemis, sonuc yok.
                else {
                    buildAlert("Sorgu Sonucu", "\n\nAranan kategoride herhangi bir sonuç bulunamadı.");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    if (currentLocation == null)
                        currentLocation = location;

                    Toast.makeText(MapsActivity.this, currentLocation.getLatitude() + " ve " + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    SupportMapFragment supportMapFragment =
                            (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    supportMapFragment.getMapAsync(MapsActivity.this);

                    LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions().position(latLng)
                            .title("Konumum");

                    // bildirimden geliyorsak marker halihazirda var. yalnizca mekani göster.
                    boolean fromNotification = getIntent().getBooleanExtra("fromNotification", false);
                    String[] latLongNotification = getIntent().getStringArrayExtra("latLong");
                    Intent intent = getIntent();
                    // bildirim ekranindaki mekani goster ve isaretle.
                    if (fromNotification) {
                        Company company = intent.getParcelableExtra("fromNotificationObject");

                        // bildirimdeki magazayi ekleyip gosteriyoruz.
                        Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(company.getCompanyLocationLat()), Double.parseDouble(company.getCompanyLocationLong()))).title(company.getCompanyName()));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(company.getCompanyLocationLat()), Double.parseDouble(company.getCompanyLocationLong())), 11.2f));
                        marker.showInfoWindow();

                        //kendi konumumuzun markerini ekliyoruz.
                        Marker myMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(latLongNotification[0]), Double.parseDouble(latLongNotification[1]))).title("Konumum"));

                        // eski konumu, tekrar acilan activity'e restore ediyoruz.
                        editTextLat.setText(String.valueOf(latLongNotification[0]));
                        editTextLong.setText(String.valueOf(latLongNotification[1]));

                        currentLocation.setLatitude(Double.parseDouble(latLongNotification[0]));
                        currentLocation.setLongitude(Double.parseDouble(latLongNotification[1]));

                        companiesNearBy.clear();
                        companiesNearBy.add(company);
                        mRecyclerView.setLayoutManager(mLayoutManager);
                        mRecyclerView.setAdapter(mAdapter);
                    }
                    else {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13.2f));
                        Marker marker = mMap.addMarker(markerOptions);
                        marker.showInfoWindow();

                        editTextLat.setText(String.valueOf(currentLocation.getLatitude()));
                        editTextLong.setText(String.valueOf(currentLocation.getLongitude()));

                    }
                }
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLastLocation();
                }
                break;
        }
    }

    public void getToLocation(View v) {

        editTextLat = (EditText) findViewById(R.id.editTextLat);
        editTextLong = (EditText) findViewById(R.id.editTextLong);

        if (TextUtils.isEmpty(editTextLat.getText().toString()) || TextUtils.isEmpty(editTextLong.getText().toString())) {
            Toast.makeText(this, "Lat ve long bilgileri boş bırakılamaz.", Toast.LENGTH_SHORT).show();
            return;
        }
        mMap.clear();
        LatLng latLng = new LatLng(Double.parseDouble(editTextLat.getText().toString()), Double.parseDouble(editTextLong.getText().toString()));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10.2f));
        MarkerOptions options = new MarkerOptions()
                .position(latLng).title("Konumum");
        mMap.addMarker(options);

        currentLocation.setLatitude(Double.parseDouble(editTextLat.getText().toString()));
        currentLocation.setLongitude(Double.parseDouble(editTextLong.getText().toString()));

        Toast.makeText(this, "Yeni konum ayarlandı.", Toast.LENGTH_SHORT).show();
    }

    private static Double toRad(Double value) {
        return value * Math.PI / 180;
    }

    public double calculateDistance(double currentLat, double currentLong, double destLat, double destLong) {

        final int R = 6371; //dunyanin yaricapi.

        // HAVERSINE
        Double latDistance = toRad(destLat-currentLat);
        Double lonDistance = toRad(destLong-currentLong);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(toRad(currentLat)) * Math.cos(toRad(destLat)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        Double distance = R * c; //in km

        //Toast.makeText(this, "bak: " + distance * 1000, Toast.LENGTH_SHORT).show();
        return distance * 1000;
        // HAVERSINE
    }

    private void buildAlert(String alertHeader, String alertText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle("Sorgu Sonucu").setMessage("\n\nAranan kategoride herhangi bir sonuç bulunamadı.");
        final TextView text = new TextView(MapsActivity.this);
        builder.setView(text);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        })
                .setIcon(android.R.drawable.ic_dialog_alert).show();
    }

    // isSearchingByCategory: True for categorySearch, false for companyName based searches.
    private void buildAlertForThreshold(final String searchFor, final Boolean isSearchingByCategory) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Aranacak mağazalar uzaklık; eşik değer (metre):");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(TextUtils.isEmpty(input.getText().toString())) {
                    Toast.makeText(MapsActivity.this, "Mesafe boş bırakılamaz.", Toast.LENGTH_SHORT).show();
                    return;
                }
                String threshold = input.getText().toString();
                populateCompanies(searchFor, threshold, isSearchingByCategory);
            }
        });
        builder.show();
    }

    private void generateNotification(Company company) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "mmey_ads_notification";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_MAX);
            // Configure the notification channel.
            notificationChannel.setDescription(company.getCampaignContent());
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);

            Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_explore)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle(company.getCompanyName())
                    .setContentText(company.getCampaignContent())
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentInfo("Information")
                    .setTicker("Got new notification!")
                    .build();
            notificationManager.notify(generateUniqueID(), notification);
        }


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_explore)
                .setTicker("Got new notification!")
                .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
                .setContentTitle(company.getCompanyName())
                .setContentText(company.getCampaignContent())
                .setContentInfo("Information")
                .setGroup(String.valueOf(generateUniqueID()));

        Intent notifyIntent = new Intent(getApplicationContext(), MapsActivity.class);
        notifyIntent.setAction(company.getCompanyName());
        notifyIntent.putExtra("fromNotificationObject", company);
        String[] latLong = {String.valueOf(currentLocation.getLatitude()), String.valueOf(currentLocation.getLongitude())};
        notifyIntent.putExtra("fromNotification", true);
        notifyIntent.putExtra("latLong", latLong);

        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(pendingIntent);

        int uniqueId = generateUniqueID();
        notificationManager.notify(uniqueId, notificationBuilder.build());
    }

    private int generateUniqueID() {
        int uniqueId = ThreadLocalRandom.current().nextInt(0, 1000 + 1);
        return uniqueId;
    }

    private void buildRecyclerView() {
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new LocationAdapter(companiesNearBy);
        mAdapter.setOnItemClickListener(new LocationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //Toast.makeText(MapsActivity.this, companiesNearBy.get(position).getCompanyName() + " ve " + companiesNearBy.get(position).getCampaignContent(), Toast.LENGTH_SHORT).show();
                Company company = companiesNearBy.get(position);
                Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(company.getCompanyLocationLat()), Double.parseDouble(company.getCompanyLocationLong()))).title(company.getCompanyName()));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(company.getCompanyLocationLat()), Double.parseDouble(company.getCompanyLocationLong())), 16.2f));
                marker.showInfoWindow();
            }
        });
    }
}