package com.example.GoAutoCamping;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Home extends Fragment {

    public static final int THREAD_HANDLER_SUCCESS_INFO = 1;
    Home_foreCastManager mForeCast;
    Home mThis;

    TextView myGps; //νμ¬ μμΉ
    View view;
    private Home_gpsTracker gpsTracker;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};
    double latitude, longitude; //μλ, κ²½λ

    //λ μ¨ μ λ³΄ νμ
    TextView tv_name, tv_date, tv_temp, tv_temp1, tv_temp2, tv_temp3, tv_temp4, tv_date1, tv_date2, tv_date3, tv_date4;
    ImageView imageV1, imageV2, imageV3, imageV4, imageV5;
    String lon = "127.0"; // μ’ν μ€μ 
    String lat = "37.583328";  // μ’ν μ€μ 

    RecyclerView recyclerView, recyclerView2, recyclerView3;
    Home_Adapter adapter;
    Home_Adapter2 adapter2, adapter3;
    List<Home_model> models;
    List<Home_model2> models2, models3;
    Context context;

    String address;

    String gpsAddress;

    private FirebaseFirestore Firestore;
    private String email;
    private FirebaseAuth user;

    boolean checking;
    int point; //κ°κΉμ΄μ¬νμ§

    ArrayList<CommunityDTO> dtos;
    ArrayList<SuppliesDTO> dtos2;
    CommunityDTO communityDTO;
    ArrayList<SuppliesDTO> suppliesDTOS2;
    ArrayList<homeDTO> homeImage;

    String category;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.home, container, false);
        myGps = view.findViewById(R.id.myGps);
        tv_name = view.findViewById(R.id.tv_name);
        tv_date = view.findViewById(R.id.tv_date);
        tv_temp = view.findViewById(R.id.tv_temp);
        tv_temp1 = view.findViewById(R.id.tv_temp1);
        tv_temp2 = view.findViewById(R.id.tv_temp2);
        tv_temp3 = view.findViewById(R.id.tv_temp3);
        tv_temp4 = view.findViewById(R.id.tv_temp4);
        imageV1 = view.findViewById(R.id.imageV1);
        imageV2 = view.findViewById(R.id.imageV2);
        imageV3 = view.findViewById(R.id.imageV3);
        imageV4 = view.findViewById(R.id.imageV4);
        imageV5 = view.findViewById(R.id.imageV5);
        tv_date1 = view.findViewById(R.id.tv_date1);
        tv_date2 = view.findViewById(R.id.tv_date2);
        tv_date3 = view.findViewById(R.id.tv_date3);
        tv_date4 = view.findViewById(R.id.tv_date4);

        Bundle bundle = getArguments();

        if (bundle != null) {
            Log.d("λ²λ€", bundle.getInt("pop")+"");
            if(bundle.getInt("pop") == 0) {
                popup_main pm = new popup_main();
                pm.show(requireActivity().getSupportFragmentManager(), "tag");
            }
        }

        Log.d("μ²΄ν¬", checking+"");
        if(!checking)
            Initialize("μμΈνΉλ³μ");

        //gpsλ²νΌ ν΄λ¦­ μ λ΄ μμΉμ λ³΄ λ°μμ€κΈ°
        myGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gpsTracker = new Home_gpsTracker(getContext());

                //μλμ κ²½λλ₯Ό μ»μ΄μ΄(GPS κΈ°λ₯ μ΄μ©)
                latitude = gpsTracker.getLatitude();
                longitude = gpsTracker.getLongitude();

                if (!checkLocationServicesStatus()) {
                    showDialogForLocationServiceSetting();
                }else {
                    checkRunTimePermission();
                }

                address = getCurrentAddress(latitude, longitude);
                myGps.setText(address);

                //dbμ μ§μ­μ λ³΄ μΆκ°
                addLocation();

                lat = Double.toString(latitude);
                lon = Double.toString(longitude);

                //μ£Όμ λ―Έλ°κ²¬μ΄λ©΄ μμΈλ‘ κ·Έλλ‘ λ μ¨ λ³΄μ¬μ€
                if(address.equals("μ£Όμ λ―Έλ°κ²¬") || address.equals("μ§μ€μ½λ μλΉμ€ μ¬μ©λΆκ°") || address.equals("μλͺ»λ GPS μ’ν")) {
                    ;
                }
                else {
                    Initialize(address);
                    plusModel();
                }
            }
        });

        //λ¦¬μ¬μ΄ν΄λ¬λ·°
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView2 = view.findViewById(R.id.recyclerView2);
        recyclerView3 = view.findViewById(R.id.recyclerView3);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getActivity());
        LinearLayoutManager layoutManager3 = new LinearLayoutManager(getActivity());

        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManager3.setOrientation(LinearLayoutManager.HORIZONTAL);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView2.setLayoutManager(layoutManager2);
        recyclerView3.setLayoutManager(layoutManager3);

        Log.d("μμΉ", myGps.getText().toString());

        //κ°κΉμ΄ μ¬νμ§ - μμΉμ λ³΄μ λ°λΌ μ λμ μΈ μκ³ λ¦¬μ¦
        plusModel();

        //μ©ν μΆμ²
        loadSupplies();

        //μΈκΈ° κ²μλ¬Ό
        loadCommunity();

        return view;
    }

    //κ°κΉμ΄ μ¬νμ§ κ°μ Έμ€κΈ°
    public void plusModel(){

        Firestore = FirebaseFirestore.getInstance();

        //μ΄λ μ΄λ¦¬μ€νΈ μλ‘ μμ±
        homeImage = new ArrayList<>();

        //μ©ν λ°μ΄ν°
        Firestore.collection("home").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
        {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){

                    //λνλ¨ΌνΈ λ¦¬μ€νΈ μμ±
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                    //λνλ¨ΌνΈ λ¦¬μ€νΈμμ λνλ¨ΌνΈ νλμ© κ°μ Έμ€κΈ°
                    for(DocumentSnapshot d : list){
                        homeDTO homeDTO = d.toObject(homeDTO.class);
                        homeImage.add(homeDTO); //μ μμ±ν μ»€λ?€λν° λ°μ΄ν°ν μ΄λ μ΄ λ¦¬μ€νΈμ λνλ¨ΌνΈ λ°μ΄ν° μΆκ°
                    }

                    models = new ArrayList<>();

                    Log.d("gps2", myGps.getText().toString());
                    Log.d("ν¬ν¨",myGps.getText().toString().contains("μΈμ²")+"");
                    if (myGps.getText().toString().contains("μμΈ") || myGps.getText().toString().contains("κ²½κΈ°λ") || myGps.getText().toString().contains("κ°μλ") || myGps.getText().toString().contains("μΈμ²")) {
                        point = 0;
                        models.add(new Home_model(homeImage.get(9).getHomeImage(), "μμΈ"));
                        models.add(new Home_model(homeImage.get(2).getHomeImage(), "κ²½κΈ°λ"));
                        models.add(new Home_model(homeImage.get(5).getHomeImage(), "κ°μλ"));
                        models.add(new Home_model(homeImage.get(0).getHomeImage(), "μΆ©μ²­λΆλ"));
                        models.add(new Home_model(homeImage.get(1).getHomeImage(), "μΆ©μ²­λ¨λ"));
                    } else if (myGps.getText().toString().contains("μΆ©μ²­λΆλ") || myGps.getText().toString().contains("μΆ©μ²­λ¨λ") || myGps.getText().toString().contains("κ²½μλΆλ") || myGps.getText().toString().contains("λμ ") || myGps.getText().toString().contains("μΈμ°") || myGps.getText().toString().contains("λκ΅¬")) {
                        point = 1;
                        models.add(new Home_model(homeImage.get(0).getHomeImage(), "μΆ©μ²­λΆλ"));
                        models.add(new Home_model(homeImage.get(1).getHomeImage(), "μΆ©μ²­λ¨λ"));
                        models.add(new Home_model(homeImage.get(3).getHomeImage(), "κ²½μλΆλ"));
                        models.add(new Home_model(homeImage.get(7).getHomeImage(), "μ λΌλΆλ"));
                        models.add(new Home_model(homeImage.get(8).getHomeImage(), "μ λΌλ¨λ"));
                    } else if (myGps.getText().toString().contains("μ λΌλΆλ") || myGps.getText().toString().contains("μ λΌλ¨λ") || myGps.getText().toString().contains("κ²½μλ¨λ") || myGps.getText().toString().contains("κ΄μ£Ό") || myGps.getText().toString().contains("λΆμ°")) {
                        point = 2;
                        models.add(new Home_model(homeImage.get(7).getHomeImage(), "μ λΌλΆλ"));
                        models.add(new Home_model(homeImage.get(8).getHomeImage(), "μ λΌλ¨λ"));
                        models.add(new Home_model(homeImage.get(4).getHomeImage(), "κ²½μλ¨λ"));
                        models.add(new Home_model(homeImage.get(3).getHomeImage(), "κ²½μλΆλ"));
                        models.add(new Home_model(homeImage.get(6).getHomeImage(), "μ μ£Όλ"));
                    }
                    else {
                        point = 3;
                        models.add(new Home_model(homeImage.get(9).getHomeImage(), "μμΈ"));
                        models.add(new Home_model(homeImage.get(2).getHomeImage(), "κ²½κΈ°λ"));
                        models.add(new Home_model(homeImage.get(5).getHomeImage(), "κ°μλ"));
                        models.add(new Home_model(homeImage.get(4).getHomeImage(), "κ²½μλ¨λ"));
                        models.add(new Home_model(homeImage.get(6).getHomeImage(), "μ μ£Όλ"));
                    }

                    adapter = new Home_Adapter(models, context);

                    recyclerView.setAdapter(adapter);

                    if(point == 0) {
                        adapter.setOnItemClicklistener(new Home_OnItemClickListener() {
                            @Override
                            public void onItemClick(Home_Adapter.ItemViewHolder holder, View view, int pos) {
                                switch (pos) {
                                    case 0: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_seoul()).addToBackStack(null).commit();
                                        break;
                                    }
                                    case 1: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_ggd()).addToBackStack(null).commit();
                                        break;
                                    }
                                    case 2: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_gwd()).addToBackStack(null).commit();
                                        break;
                                    }
                                    //μΆ©μ²­λΆλ
                                    case 3: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_ccbd()).addToBackStack(null).commit();
                                        break;
                                    }
                                    //μΆ©μ²­λ¨λ
                                    case 4: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_ccnd()).addToBackStack(null).commit();
                                        break;
                                    }
                                }
                            }
                        });
                    }

                    else if(point == 1) {
                        adapter.setOnItemClicklistener(new Home_OnItemClickListener() {
                            @Override
                            public void onItemClick(Home_Adapter.ItemViewHolder holder, View view, int pos) {
                                switch (pos) {
                                    //μΆ©λΆ
                                    case 0: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_ccbd()).addToBackStack(null).commit();
                                        break;
                                    }
                                    //μΆ©λ¨
                                    case 1: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_ccnd()).addToBackStack(null).commit();
                                        break;
                                    }
                                    //κ²½λΆ
                                    case 2: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_gsbd()).addToBackStack(null).commit();
                                        break;
                                    }
                                    //μ λΆ
                                    case 3: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_jlbd()).addToBackStack(null).commit();
                                        break;
                                    }
                                    //μ λ¨
                                    case 4: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_jlnd()).addToBackStack(null).commit();
                                        break;
                                    }
                                }
                            }
                        });
                    }
                    else if(point == 2) {
                        adapter.setOnItemClicklistener(new Home_OnItemClickListener() {
                            @Override
                            public void onItemClick(Home_Adapter.ItemViewHolder holder, View view, int pos) {
                                switch (pos) {
                                    //μ λΆ
                                    case 0: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_jlbd()).addToBackStack(null).commit();
                                        break;
                                    }
                                    //μ λ¨
                                    case 1: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_jlnd()).addToBackStack(null).commit();
                                        break;
                                    }
                                    //κ²½λ¨
                                    case 2: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_gsnd()).addToBackStack(null).commit();
                                        break;
                                    }
                                    //κ²½λΆ
                                    case 3: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_gsbd()).addToBackStack(null).commit();
                                        break;
                                    }
                                    //μ μ£Ό
                                    case 4: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_jeju()).addToBackStack(null).commit();
                                        break;
                                    }
                                }
                            }
                        });
                    }
                    else {
                        adapter.setOnItemClicklistener(new Home_OnItemClickListener() {
                            @Override
                            public void onItemClick(Home_Adapter.ItemViewHolder holder, View view, int pos) {
                                switch (pos) {
                                    case 0: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_seoul()).addToBackStack(null).commit();
                                        break;
                                    }
                                    case 1: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_ggd()).addToBackStack(null).commit();
                                        break;
                                    }
                                    case 2: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_gwd()).addToBackStack(null).commit();
                                        break;
                                    }
                                    case 3: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_gsnd()).addToBackStack(null).commit();
                                        break;
                                    }
                                    case 4: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_jeju()).addToBackStack(null).commit();
                                        break;
                                    }
                                }
                            }
                        });
                    }
                }
                else {
                    Log.d("κ° μμ", "");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("μ€ν¨ν¨", "");
            }
        });
    }

    //μ©ν λ°μ΄ν° κ°μ Έμ€κΈ°
    public void loadSupplies(){

        Firestore = FirebaseFirestore.getInstance();

        //μ΄λ μ΄λ¦¬μ€νΈ μλ‘ μμ±
        dtos2 = new ArrayList<>();

        suppliesDTOS2 = new ArrayList<>();

        models2 = new ArrayList<>();

        //light
        Firestore.collection("supplies").document("category_light").collection("posts")
                .orderBy("post_like", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
        {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){

                    //λνλ¨ΌνΈ λ¦¬μ€νΈ μμ±
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                    //λνλ¨ΌνΈ λ¦¬μ€νΈμμ λνλ¨ΌνΈ νλμ© κ°μ Έμ€κΈ°
                    for(DocumentSnapshot d : list){
                        SuppliesDTO suppliesDTO = d.toObject(SuppliesDTO.class);
                        suppliesDTO.setPost_id(d.getId());
                        dtos2.add(suppliesDTO); //μ μμ±ν μ»€λ?€λν° λ°μ΄ν°ν μ΄λ μ΄ λ¦¬μ€νΈμ λνλ¨ΌνΈ λ°μ΄ν° μΆκ°
                    }

                    models2.add(new Home_model2(dtos2.get(0).getPost_Image(), dtos2.get(0).getPost_name()));
                    suppliesDTOS2.add(dtos2.get(0));
                    dtos2.clear();

                    Firestore.collection("supplies").document("category_cooking").collection("posts")
                            .orderBy("post_like", Query.Direction.DESCENDING)
                            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
                    {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if(!queryDocumentSnapshots.isEmpty()){

                                //λνλ¨ΌνΈ λ¦¬μ€νΈ μμ±
                                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                                //λνλ¨ΌνΈ λ¦¬μ€νΈμμ λνλ¨ΌνΈ νλμ© κ°μ Έμ€κΈ°
                                for(DocumentSnapshot d : list){
                                    SuppliesDTO suppliesDTO = d.toObject(SuppliesDTO.class);
                                    suppliesDTO.setPost_id(d.getId());
                                    dtos2.add(suppliesDTO); //μ μμ±ν μ»€λ?€λν° λ°μ΄ν°ν μ΄λ μ΄ λ¦¬μ€νΈμ λνλ¨ΌνΈ λ°μ΄ν° μΆκ°
                                }

                                models2.add(new Home_model2(dtos2.get(0).getPost_Image(), dtos2.get(0).getPost_name()));
                                models2.add(new Home_model2(dtos2.get(1).getPost_Image(), dtos2.get(1).getPost_name()));
                                suppliesDTOS2.add(dtos2.get(0));
                                suppliesDTOS2.add(dtos2.get(1));
                                dtos2.clear();

                                Firestore.collection("supplies").document("category_living").collection("posts")
                                        .orderBy("post_like", Query.Direction.DESCENDING)
                                        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
                                {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        if(!queryDocumentSnapshots.isEmpty()){

                                            //λνλ¨ΌνΈ λ¦¬μ€νΈ μμ±
                                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                                            //λνλ¨ΌνΈ λ¦¬μ€νΈμμ λνλ¨ΌνΈ νλμ© κ°μ Έμ€κΈ°
                                            for(DocumentSnapshot d : list){
                                                SuppliesDTO suppliesDTO = d.toObject(SuppliesDTO.class);
                                                suppliesDTO.setPost_id(d.getId());
                                                dtos2.add(suppliesDTO); //μ μμ±ν μ»€λ?€λν° λ°μ΄ν°ν μ΄λ μ΄ λ¦¬μ€νΈμ λνλ¨ΌνΈ λ°μ΄ν° μΆκ°
                                            }

                                            models2.add(new Home_model2(dtos2.get(0).getPost_Image(), dtos2.get(0).getPost_name()));
                                            suppliesDTOS2.add(dtos2.get(0));
                                            dtos2.clear();

                                            Firestore.collection("supplies").document("category_etc").collection("posts")
                                                    .orderBy("post_like", Query.Direction.DESCENDING)
                                                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
                                            {
                                                @Override
                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                    if(!queryDocumentSnapshots.isEmpty()){

                                                        //λνλ¨ΌνΈ λ¦¬μ€νΈ μμ±
                                                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                                                        //λνλ¨ΌνΈ λ¦¬μ€νΈμμ λνλ¨ΌνΈ νλμ© κ°μ Έμ€κΈ°
                                                        for(DocumentSnapshot d : list){
                                                            SuppliesDTO suppliesDTO = d.toObject(SuppliesDTO.class);
                                                            suppliesDTO.setPost_id(d.getId());
                                                            dtos2.add(suppliesDTO); //μ μμ±ν μ»€λ?€λν° λ°μ΄ν°ν μ΄λ μ΄ λ¦¬μ€νΈμ λνλ¨ΌνΈ λ°μ΄ν° μΆκ°
                                                        }

                                                        models2.add(new Home_model2(dtos2.get(0).getPost_Image(), dtos2.get(0).getPost_name()));
                                                        suppliesDTOS2.add(dtos2.get(0));

                                                        //μ΄ν­ν° μμ± λ° setAdapter
                                                        adapter2 = new Home_Adapter2(models2, context);
                                                        recyclerView2.setAdapter(adapter2);

                                                        //ν΄λ¦­ μ νλ©΄μ νμ΄λ²€νΈ μ²λ¦¬
                                                        adapter2.setOnItemClicklistener(new Home_OnItemClickListener2() {
                                                            @Override
                                                            public void onItemClick(Home_Adapter2.ItemViewHolder holder, View view, int pos) {
                                                                switch (pos) {
                                                                    case 0 : {
                                                                        category = "category_light";
                                                                        break;
                                                                    }
                                                                    case 1 : {
                                                                        category = "category_cooking";
                                                                        break;
                                                                    }
                                                                    case 2 : {
                                                                        category = "category_cooking";
                                                                        break;
                                                                    }
                                                                    case 3 : {
                                                                        category = "category_living";
                                                                        break;
                                                                    }
                                                                    case 4 : {
                                                                        category = "category_etc";
                                                                        break;
                                                                    }

                                                                }

                                                                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                                                Fragment fragment = new Supplies_Detail();

                                                                Bundle bundle = new Bundle();
                                                                bundle.putString("supplyKind", category);
                                                                bundle.putString("postId", suppliesDTOS2.get(pos).getPost_id());

                                                                fragment.setArguments(bundle);
                                                                transaction.replace(R.id.main_frame, fragment).addToBackStack(null).commit();
                                                            }
                                                        });
                                                    }
                                                    else {
                                                        Log.d("κ° μμ", "");
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d("μ€ν¨ν¨", "");
                                                }
                                            });
                                        }
                                        else {
                                            Log.d("κ° μμ", "");
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("μ€ν¨ν¨", "");
                                    }
                                });
                            }
                            else {
                                Log.d("κ° μμ", "");
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("μ€ν¨ν¨", "");
                        }
                    });
                }
                else {
                    Log.d("κ° μμ", "");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("μ€ν¨ν¨", "");
            }
        });
    }

    //μ»€λ?€λν° λ°μ΄ν° κ°μ Έμ€κΈ°
    public void loadCommunity(){
        Firestore = FirebaseFirestore.getInstance();

        //μ΄λ μ΄λ¦¬μ€νΈ μλ‘ μμ±
        dtos = new ArrayList<>();

        //μ»€λ?€λν°μ λͺ¨λ  λ°μ΄ν° κ°μ Έμ€κΈ° - onCompleteκ° μλ¨Ήμ΄μ Successλ‘ λ°κΏ¨μ κ·Έμ λ°λΌ μλ λ°©λ²λ λ°λ
        Firestore.collection("communication").orderBy("communityLike", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){

                    Log.d("μ€νμ€2", "μ€νμ€2");
                    //λνλ¨ΌνΈ λ¦¬μ€νΈ μμ±
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                    //λνλ¨ΌνΈ λ¦¬μ€νΈμμ λνλ¨ΌνΈ νλμ© κ°μ Έμ€κΈ°
                    for(DocumentSnapshot d : list){
                        communityDTO = d.toObject(CommunityDTO.class);
                        communityDTO.setCommunityId(d.getId());
                        Log.d("μ€νμ€", "μ€νμ€");
                        dtos.add(communityDTO); //μ μμ±ν μ»€λ?€λν° λ°μ΄ν°ν μ΄λ μ΄ λ¦¬μ€νΈμ λνλ¨ΌνΈ λ°μ΄ν° μΆκ°
                    }

                    models3 = new ArrayList<>();
                    //TODO λμ€μ μ΄λΆλΆ 5λ‘ λ°κΏμ 5κ°λ³΄μ¬μ£ΌκΈ°
                    for(int i=0; i<5; i++) {
                        models3.add(new Home_model2(dtos.get(i).getCommunityImage(), dtos.get(i).getCommunityAddress()));
                    }

                    //μ΄ν­ν° μμ± λ° setAdapter
                    adapter3 = new Home_Adapter2(models3, context);
                    recyclerView3.setAdapter(adapter3);

                    adapter3.setOnItemClicklistener(new Home_OnItemClickListener2() {
                        @Override
                        public void onItemClick(Home_Adapter2.ItemViewHolder holder, View view, int pos) {
                            openCommuDetail(pos);
                        }
                    });
                }
                else {
                    Log.d("κ° μμ", "");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("μ€ν¨ν¨", "");
            }
        });
    }

    public void openCommuDetail(int pos) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        Fragment fragment = new Community_detail(pos);

        Bundle bundle = new Bundle();
        bundle.putString("postId", dtos.get(pos).getCommunityId());
        bundle.putString("home", "home");

        fragment.setArguments(bundle);
        transaction.replace(R.id.main_frame, fragment).addToBackStack(null).commit();
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            // μμ²­ μ½λκ° PERMISSIONS_REQUEST_CODE μ΄κ³ , μμ²­ν νΌλ―Έμ κ°μλ§νΌ μμ λμλ€λ©΄
            boolean check_result = true;
            // λͺ¨λ  νΌλ―Έμμ νμ©νλμ§ μ²΄ν¬ν©λλ€.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }
            if ( check_result ) {
                //μμΉ κ°μ κ°μ Έμ¬ μ μμ
                ;
            }
            else {
                // κ±°λΆν νΌλ―Έμμ΄ μλ€λ©΄ μ±μ μ¬μ©ν  μ μλ μ΄μ λ₯Ό μ€λͺν΄μ£Όκ³  μ±μ μ’λ£ν©λλ€.2 κ°μ§ κ²½μ°κ° μμ΅λλ€.
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(getActivity(), "νΌλ―Έμμ΄ κ±°λΆλμμ΅λλ€. μ±μ λ€μ μ€ννμ¬ νΌλ―Έμμ νμ©ν΄μ£ΌμΈμ.", Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(getActivity(), "νΌλ―Έμμ΄ κ±°λΆλμμ΅λλ€. μ€μ (μ± μ λ³΄)μμ νΌλ―Έμμ νμ©ν΄μΌ ν©λλ€. ", Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    void checkRunTimePermission(){

        //λ°νμ νΌλ―Έμ μ²λ¦¬
        // 1. μμΉ νΌλ―Έμμ κ°μ§κ³  μλμ§ μ²΄ν¬ν©λλ€.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 2. μ΄λ―Έ νΌλ―Έμμ κ°μ§κ³  μλ€λ©΄
            // ( μλλ‘μ΄λ 6.0 μ΄ν λ²μ μ λ°νμ νΌλ―Έμμ΄ νμμκΈ° λλ¬Έμ μ΄λ―Έ νμ©λ κ±Έλ‘ μΈμν©λλ€.)
            // 3.  μμΉ κ°μ κ°μ Έμ¬ μ μμ
        } else {  //2. νΌλ―Έμ μμ²­μ νμ©ν μ μ΄ μλ€λ©΄ νΌλ―Έμ μμ²­μ΄ νμν©λλ€. 2κ°μ§ κ²½μ°(3-1, 4-1)κ° μμ΅λλ€.
            // 3-1. μ¬μ©μκ° νΌλ―Έμ κ±°λΆλ₯Ό ν μ μ΄ μλ κ²½μ°μλ
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[0])) {
                // 3-2. μμ²­μ μ§ννκΈ° μ μ μ¬μ©μκ°μκ² νΌλ―Έμμ΄ νμν μ΄μ λ₯Ό μ€λͺν΄μ€ νμκ° μμ΅λλ€.
                Toast.makeText(getContext(), "μ΄ μ±μ μ€ννλ €λ©΄ μμΉ μ κ·Ό κΆνμ΄ νμν©λλ€.", Toast.LENGTH_LONG).show();
                // 3-3. μ¬μ©μκ²μ νΌλ―Έμ μμ²­μ ν©λλ€. μμ²­ κ²°κ³Όλ onRequestPermissionResultμμ μμ λ©λλ€.
                ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            } else {
                // 4-1. μ¬μ©μκ° νΌλ―Έμ κ±°λΆλ₯Ό ν μ μ΄ μλ κ²½μ°μλ νΌλ―Έμ μμ²­μ λ°λ‘ ν©λλ€.
                // μμ²­ κ²°κ³Όλ onRequestPermissionResultμμ μμ λ©λλ€.
                ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
    }
    public String getCurrentAddress( double latitude, double longitude) {
        //μ§μ€μ½λ... GPSλ₯Ό μ£Όμλ‘ λ³ν
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude,7);
        } catch (IOException ioException) {
            //λ€νΈμν¬ λ¬Έμ 
            Toast.makeText(getContext(), "μ§μ€μ½λ μλΉμ€ μ¬μ©λΆκ°", Toast.LENGTH_LONG).show();
            return "μ§μ€μ½λ μλΉμ€ μ¬μ©λΆκ°";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(getContext(), "μλͺ»λ GPS μ’ν", Toast.LENGTH_LONG).show();
            return "μλͺ»λ GPS μ’ν";
        }
        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(getContext(), "μ£Όμ λ―Έλ°κ²¬", Toast.LENGTH_LONG).show();
            return "μ£Όμ λ―Έλ°κ²¬";
        }
        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }
    //μ¬κΈ°λΆν°λ GPS νμ±νλ₯Ό μν λ©μλλ€
    private void showDialogForLocationServiceSetting() {
        MaterialDialog dialog = new MaterialDialog(getContext(), MaterialDialog.getDEFAULT_BEHAVIOR());
        dialog.title(null, "GPS νμ±ν");
        dialog.message(null, "GPS κΈ°λ₯μ νμ±ννμκ² μ΅λκΉ?", null);
        dialog.positiveButton(null, "νμ±ν", materialDialog -> {
            Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            return null;
        });
        dialog.negativeButton(null, "μλμ", materialDialog -> {
            dialog.dismiss();
            return null;
        });
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //μ¬μ©μκ° GPS νμ± μμΌ°λμ§ κ²μ¬
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS νμ±ν λμμ");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    //λ μ¨
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void Initialize(String address)
    {
        String ad = address;
        mThis = this;
        mForeCast = new Home_foreCastManager(lon, lat, context, mThis, ad);
        mForeCast.run();
    }

    public void addLocation() {
        if(myGps.getText().equals("μ£Όμ λ―Έλ°κ²¬")) {

        }
        //μ£Όμ λ―Έλ°κ²¬μΌ λλ μλ ₯ μν΄μ€.
        else {
            LocationDTO locationDTO = new LocationDTO();

            locationDTO.setUserId(email);
            locationDTO.setUserAddress(address);
            locationDTO.setLon(longitude);
            locationDTO.setLat(latitude);

            if(email != null) {
                Firestore.collection("Location").document(email).set(locationDTO).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        FragmentActivity activity = getActivity();
        if(activity!=null){
            ((MainActivity)activity).setBackBtn(0,false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        Firestore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance();

        FirebaseUser currentUser = user.getCurrentUser();
        if (currentUser != null) {
            email = currentUser.getEmail();

            Firestore = FirebaseFirestore.getInstance();

            Firestore.collection("Location").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        Log.d("μ€νμ€2", "μ€νμ€2");
                        //λνλ¨ΌνΈ λ¦¬μ€νΈ μμ±
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                        //λνλ¨ΌνΈ λ¦¬μ€νΈμμ λνλ¨ΌνΈ νλμ© κ°μ Έμ€κΈ°
                        for (DocumentSnapshot d : list) {
                            Log.d("μμ΄λ", d.getId());
                            if(d.getId().equals(email)) {
                                checking = true;
                                Log.d("μμ΄λ1",checking+"");
                                Firestore.collection("Location").document(email).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @RequiresApi(api = Build.VERSION_CODES.O)
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        LocationDTO locationDTO = documentSnapshot.toObject(LocationDTO.class);
                                        gpsAddress = locationDTO.getUserAddress();
                                        Log.d("μ£Όμλͺλͺ",gpsAddress);
                                        myGps.setText(gpsAddress);
                                        plusModel();
                                        String ad = locationDTO.getUserAddress();
                                        mForeCast = new Home_foreCastManager(Double.toString(locationDTO.getLon()), Double.toString(locationDTO.getLat()), context, mThis, ad);
                                        mForeCast.run();
                                    }
                                });
                            }
                        }
                    }
                }
            });
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        this.context = context;

        mThis = this;

    }
}