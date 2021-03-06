package com.example.GoAutoCamping;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.content.CursorLoader;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Community_add extends AppCompatActivity {

    int REQUEST_IMAGE_CODE = 1001;
    int REQUEST_EXTERNAL_STORAGE_PERMISSION = 1002;
    String STATE_IMAGE = "StoreImage";

    CoordinatorLayout snackbar;
    ImageView image_address;
    TextView address_NameTv, textTitle;
    MaterialButton btnOk;
    EditText editContent;
    RatingBar starRate;

    private FirebaseStorage storage;
    private String imageUrl="";
    private FirebaseFirestore Firestore;
    private String email;
    private FirebaseAuth user;
    private float starNum;

    Context context;

    String userNickName,userProfile;

    byte[] img;
    String place;

    Bitmap bitmap;

    //???????????? ???????????? ?????????????????? ??????
    ArrayList<CommunityDTO> dtos;
    ArrayList<CommunityDTO> dtos2;
    ArrayList<CommunityDTO> communityData;

    ArrayList<String> likeName = new ArrayList<>();
    ArrayList<String> declearName = new ArrayList<>();

    int pos;

    //????????? ??????
    String postId, address, content, id, image, nickName, profile, uploadTime, rec2, home, word;
    Float star;
    int like, pos2;
    ArrayList<String> likeNames;

    String Add, title = "??????";
    Float Lat, Lng;

    //????????? ????????? ??????
    boolean checking = false;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.community_add);

        image_address = findViewById(R.id.image_address);
        address_NameTv = findViewById(R.id.addressNameTv);
        btnOk = findViewById(R.id.btnOk);
        editContent = findViewById(R.id.editContent);
        starRate = findViewById(R.id.starRate);
        textTitle = findViewById(R.id.textTitle);
        snackbar = findViewById(R.id.snackbar_line);

        storage = FirebaseStorage.getInstance();
        Firestore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance();

        //???????????? ????????? ??? ?????? ??????
        starRate.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float star, boolean b) {
                starNum = star;
            }
        });

        //??? ?????? ??????
        Intent intent = getIntent();
        String place = intent.getStringExtra("place");
        String update = intent.getStringExtra("update");

        if(update != null) {
            updateStart();
        }

        if(rec2 != null) {
            loadRecommend();
        }
        else if ( home != null) {
            loadHome();
        }

        else if (word != null) {
            loadSearch();
        }

        else {
            //????????????
            load();
        }

        Toolbar toolbar = findViewById(R.id.consL);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //?????? ?????? ????????? ?????? ??? ?????? ?????? ???????????? ??????
        address_NameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplication(), Community_add_placesChoice.class);
                startActivityForResult(intent,10);
            }
        });

        //?????? ???????????? ?????? ?????? ??????
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE_PERMISSION);
            }
        } else {
        }

        //TODO - ???????????? ?????? ??? ????????? ??????
        image_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE_CODE);
            }
        });

        Log.d("url", imageUrl);

        //TODO - ???????????? ?????? ??? ????????????????????? ?????? ??????.
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //????????? ?????? ????????????
                if(editContent.getText().toString().equals("")) {
                    Snackbar.make(snackbar, "???????????? ????????? ??????????????????", Snackbar.LENGTH_SHORT).show();
                }

                //????????? ?????? ????????????
                else if(address_NameTv.getText().toString().equals("?????? ??????")) {
                    Snackbar.make(snackbar, "???????????? ????????? ??????????????????", Snackbar.LENGTH_SHORT).show();
                }

                //TODO - ????????? ?????? ??? ?????? ?????? ??????
                else {
                    //????????? ??????
                    if (update != null) {
                        update(imageUrl);
                        Toast.makeText(getApplication(), "????????? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                    } else {
                        //????????? ??????
                        //????????? ?????? ????????????
                        if(img == null) {
                            Snackbar.make(snackbar, "???????????? ????????? ??????????????????", Snackbar.LENGTH_SHORT).show();
                        }
                        uploadImg(imageUrl);
                        Log.d("?????????", "?????????");
                        Log.d("###", imageUrl);
                    }
                }
            }
        });

    }

    //??????????????? ???????????? ????????? ??????????????? ??????.
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CODE) {
            try {
                Uri image = data.getData();
                try {
                    imageUrl = getRealPathFromUri(image);

                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), image);
                    img = bitmapToByteArray(bitmap); //???????????? ???????????? ?????????????????? ??????
                    Log.d("###", img.length + "");

                    GradientDrawable drawable = (GradientDrawable) image_address.getContext().getDrawable(R.drawable.community_edge);

                    image_address.setBackground(drawable);
                    image_address.setClipToOutline(true);

                    Glide.with(getApplicationContext())
                            .load(imageUrl)
                            .into(image_address);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        //????????? ???????????????????????? ??????
        else if (requestCode == 10) {
            if (data != null) {
                Add = data.getStringExtra("Add2");
                Lat = data.getFloatExtra("Lat2", 0.0f);
                Lng = data.getFloatExtra("Lng2", 0.0f);
                title = data.getStringExtra("title");
                Log.d("??????", title);

                //???????????? ?????? ????????? ???????????? ?????????
                if (title == null)
                    Log.d("?????? ???????????????", "???????????? ???????????????");
                else {
                    address_NameTv.setText(title);
                }

                //???????????? ?????? ????????? ???????????? ?????????
                if (img == null)
                    Log.d("?????? ???????????????", "???????????? ???????????????");
                else {

                    Glide.with(getApplicationContext())
                            .load(imageUrl)
                            .into(image_address);
                }
            }
        }
    }

    //??????????????? ?????????.
    private String getRealPathFromUri(Uri uri)
    {
        String[] proj=  {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this,uri,proj,null,null,null);
        Cursor cursor = cursorLoader.loadInBackground();

        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String url = cursor.getString(columnIndex);
        cursor.close();
        return  url;
    }

    //????????? ??????
    public void update(String uri) {

        if(imageUrl.equals("")) {
            Firestore = FirebaseFirestore.getInstance();

            CommunityDTO communityDTO = new CommunityDTO();
            communityDTO.setCommunityId(id);
            communityDTO.setCommunityImage(image);
            communityDTO.setCommunityAddress(address_NameTv.getText().toString());
            communityDTO.setCommunityContent(editContent.getText().toString());
            communityDTO.setCommunityUserNickName(nickName);
            communityDTO.setCommunityStar(starNum);
            communityDTO.setCommunityLike(dtos.get(pos2).getCommunityLike());
            communityDTO.setCommunityUserProfile(profile);
            communityDTO.setCommunityLikeUser(dtos.get(pos2).getCommunityLikeUser());
            communityDTO.setCommunityUploadTime(dtos.get(pos2).getCommunityUploadTime());
            communityDTO.setCommunityDeclear(dtos.get(pos2).getCommunityDeclear());
            if(Add == null)
                communityDTO.setCommunityAddress2(dtos.get(pos2).getCommunityAddress2());
            else
                communityDTO.setCommunityAddress2(Add);


            Firestore.collection("communication").document(postId).set(communityDTO).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Intent i = getIntent();
                    i.putExtra("check", 1);
                    if(rec2 != null)
                        i.putExtra("rec2", rec2);

                    setResult(Activity.RESULT_OK, i);
                    checking = true;
                    finish();
                    overridePendingTransition(R.anim.none, R.anim.exit_from_right);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
            //???????????? ???????????? ???????????? ???
        }else {
            try {
                // Create a storage reference from our app
                StorageReference storageRef = storage.getReference();

                Uri file = Uri.fromFile(new File(uri));
                final StorageReference riversRef = storageRef.child("communityImages/" + file.getLastPathSegment());
                UploadTask uploadTask = riversRef.putFile(file);

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return riversRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {

                            //????????????????????? ?????????????????? ?????????
                            @SuppressWarnings("VisibleForTests")
                            Uri downloadUrl = task.getResult();

                            Firestore = FirebaseFirestore.getInstance();

                            CommunityDTO communityDTO = new CommunityDTO();
                            communityDTO.setCommunityId(id);
                            communityDTO.setCommunityImage(downloadUrl.toString());
                            communityDTO.setCommunityAddress(address_NameTv.getText().toString());
                            communityDTO.setCommunityContent(editContent.getText().toString());
                            communityDTO.setCommunityUserNickName(nickName);
                            communityDTO.setCommunityStar(starNum);
                            communityDTO.setCommunityLike(dtos.get(pos2).getCommunityLike());
                            communityDTO.setCommunityUserProfile(profile);
                            communityDTO.setCommunityLikeUser(dtos.get(pos2).getCommunityLikeUser());
                            communityDTO.setCommunityUploadTime(dtos.get(pos2).getCommunityUploadTime());
                            communityDTO.setCommunityDeclear(dtos.get(pos2).getCommunityDeclear());
                            if(Add == null)
                                communityDTO.setCommunityAddress2(dtos.get(pos2).getCommunityAddress2());
                            else
                                communityDTO.setCommunityAddress2(Add);

                            //?????????????????? ???????????? ??? ??????
                            Firestore.collection("communication").document(postId).set(communityDTO).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Intent i = getIntent();
                                    i.putExtra("check", 1);
                                    setResult(Activity.RESULT_OK, i);
                                    checking = true;
                                    finish();
                                    overridePendingTransition(R.anim.none, R.anim.exit_from_right);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });

                        } else {
                            // Handle failures
                            // ...
                        }
                    }
                });

            } catch (Exception e) {


                e.printStackTrace();
            }
        }
    }

    //???????????? ????????? ????????????(??????????????? ??????)
    public void loadSearch(){
        Firestore = FirebaseFirestore.getInstance();

        //?????????????????? ?????? ??????
        dtos = new ArrayList<>();

        communityData = new ArrayList<>();

        //??????????????? ?????? ????????? ???????????? - onComplete??? ???????????? Success??? ????????? ?????? ?????? ?????? ????????? ??????
        Firestore.collection("communication").orderBy("communityUploadTime", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){

                    Log.d("?????????2", "?????????2");
                    //???????????? ????????? ??????
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                    //???????????? ??????????????? ???????????? ????????? ????????????
                    for(DocumentSnapshot d : list){
                        CommunityDTO communityDTO = d.toObject(CommunityDTO.class);
                        communityDTO.setCommunityId(d.getId());
                        Log.d("?????????", "?????????");
                        communityData.add(communityDTO); //??? ????????? ???????????? ???????????? ????????? ???????????? ???????????? ????????? ??????
                    }
                    //TODO ????????? ????????? 5??? ????????? 5???????????????
                    for (int j = 0; j<communityData.size(); j++) {
                        if(communityData.get(j).getCommunityAddress().contains(word) || communityData.get(j).getCommunityContent().contains(word)) {
                            dtos.add(communityData.get(j));
                        }
                    }
                }
                else {
                    Log.d("??? ??????", "");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("?????????", "");
            }
        });
    }

    //???????????? ????????? ???????????? (????????? ??????)
    public void loadHome(){

        Firestore = FirebaseFirestore.getInstance();

        //?????????????????? ?????? ??????
        dtos = new ArrayList<>();

        //??????????????? ?????? ????????? ???????????? - onComplete??? ???????????? Success??? ????????? ?????? ?????? ?????? ????????? ??????
        Firestore.collection("communication").orderBy("communityLike", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){

                    //???????????? ????????? ??????
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                    //???????????? ??????????????? ???????????? ????????? ????????????
                    for(DocumentSnapshot d : list){
                        CommunityDTO communityDTO = d.toObject(CommunityDTO.class);
                        dtos.add(communityDTO); //??? ????????? ???????????? ???????????? ????????? ???????????? ???????????? ????????? ??????
                    }
                }
                else {
                    Log.d("??? ??????", "");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("?????????", "");
            }
        });
    }

    //???????????? ????????? ????????????
    public void loadRecommend(){

        Firestore = FirebaseFirestore.getInstance();

        //?????????????????? ?????? ??????
        dtos = new ArrayList<>();

        dtos2 = new ArrayList<>();

        //??????????????? ?????? ????????? ???????????? - onComplete??? ???????????? Success??? ????????? ?????? ?????? ?????? ????????? ??????
        Firestore.collection("communication").orderBy("communityLike", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){

                    //???????????? ????????? ??????
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                    //???????????? ??????????????? ???????????? ????????? ????????????
                    for(DocumentSnapshot d : list){
                        CommunityDTO communityDTO = d.toObject(CommunityDTO.class);
                        dtos2.add(communityDTO); //??? ????????? ???????????? ???????????? ????????? ???????????? ???????????? ????????? ??????
                    }

                    //???????????? ????????? ????????? ??? ??????
                    for (int j = 0; j<dtos2.size(); j++) {
                        if(dtos2.get(j).getCommunityAddress2().contains(rec2)) {
                            dtos.add(dtos2.get(j));
                        }
                    }

                    pos = dtos.size();
                }
                else {
                    Log.d("??? ??????", "");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("?????????", "");
            }
        });
    }

    //???????????? ????????? ????????????
    public void load(){

        Firestore = FirebaseFirestore.getInstance();

        //?????????????????? ?????? ??????
        dtos = new ArrayList<>();

        //??????????????? ?????? ????????? ???????????? - onComplete??? ???????????? Success??? ????????? ?????? ?????? ?????? ????????? ??????
        Firestore.collection("communication").orderBy("communityUploadTime", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){

                    //???????????? ????????? ??????
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                    //???????????? ??????????????? ???????????? ????????? ????????????
                    for(DocumentSnapshot d : list){
                        CommunityDTO communityDTO = d.toObject(CommunityDTO.class);
                        dtos.add(communityDTO); //??? ????????? ???????????? ???????????? ????????? ???????????? ???????????? ????????? ??????
                    }
                    pos = dtos.size();
                }
                else {
                    Log.d("??? ??????", "");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("?????????", "");
            }
        });
    }

    //????????????????????? ?????????
    private void uploadImg(String uri)
    {
        final DocumentReference documentReferenceUser = Firestore.collection("users").document(email);

        try {
            // Create a storage reference from our app
            StorageReference storageRef = storage.getReference();

            Uri file = Uri.fromFile(new File(uri));
            final StorageReference riversRef = storageRef.child("communityImages/"+file.getLastPathSegment());
            UploadTask uploadTask = riversRef.putFile(file);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return riversRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful())
                    {
                        //????????????????????? ?????????????????? ?????????
                        @SuppressWarnings("VisibleForTests")
                        Uri downloadUrl = task.getResult();

                        CommunityDTO communityDTO = new CommunityDTO();
                        communityDTO.setCommunityId("");
                        communityDTO.setCommunityImage(downloadUrl.toString());
                        communityDTO.setCommunityAddress(address_NameTv.getText().toString());
                        communityDTO.setCommunityContent(editContent.getText().toString());
                        communityDTO.setCommunityUserNickName(userNickName);
                        communityDTO.setCommunityStar(starNum);
                        communityDTO.setCommunityLike(0);
                        communityDTO.setCommunityUserProfile(userProfile);
                        communityDTO.setCommunityUploadTime(com.google.firebase.Timestamp.now());
                        communityDTO.setCommunityLikeUser(likeName);
                        communityDTO.setCommunityDeclear(declearName);
                        communityDTO.setCommunityAddress2(Add);

                        Firestore.collection("communication").add(communityDTO)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {

                                        //?????? ???????????? ??????
                                        documentReferenceUser.update("userPosts", FieldValue.arrayUnion(documentReference.getId()));

                                        Intent i = getIntent();
                                        i.putExtra("check", 1);
                                        setResult(Activity.RESULT_OK, i);
                                        finish();
                                        overridePendingTransition(R.anim.none, R.anim.exit_from_right);

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("?????????", "Error adding document", e);
                                    }
                                });



                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });

        }catch (NullPointerException e)
        {
            e.printStackTrace();
        }
    }

    //?????????????????? ?????????????????? ??????, ?????? : https://crazykim2.tistory.com/434
    public byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: { //?????? ???????????? ??????
                finish();
                overridePendingTransition(R.anim.none, R.anim.exit_from_right);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = user.getCurrentUser();
        if(currentUser != null){
            email = currentUser.getEmail();

            //????????? ??????????????????
            DocumentReference docRef = Firestore.collection("users").document(email);
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    //????????? ????????? ????????????
                    UserDTO userDTO = documentSnapshot.toObject(UserDTO.class);
                    userNickName = userDTO.getUserNickname();
                    userProfile = userDTO.getUserProfile();
                }
            });
        }
    }

    //?????? ??? ????????? ?????? ????????????
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void updateStart() {
        //??? ?????? ??????
        Intent intent = getIntent();
        String update = intent.getStringExtra("update");
        if (update != null) {
            if((intent.getStringExtra("rec2") != null)) {
                rec2 = intent.getStringExtra("rec2");
            }
            else if((intent.getStringExtra("home") != null)) {
                home = intent.getStringExtra("home");
            }
            else if((intent.getStringExtra("word") != null)) {
                word = intent.getStringExtra("word");
            }

            postId = intent.getStringExtra("postId");
            address = intent.getStringExtra("address");
            content = intent.getStringExtra("content");
            id = intent.getStringExtra("id");
            image = intent.getStringExtra("image");
            like = intent.getIntExtra("like", 0);
            star = intent.getFloatExtra("star", 0);
            profile = intent.getStringExtra("profile");
            nickName = intent.getStringExtra("nickName");
            uploadTime = intent.getStringExtra("uploadTime");
            pos2 = intent.getIntExtra("pos", 0);

            Log.d("??????????????????", postId);
        }

        //???????????? ?????? ?????????
        if(update != null) {
            textTitle.setText("????????? ??????");
            address_NameTv.setText(address);
            GradientDrawable drawable = (GradientDrawable) image_address.getContext().getDrawable(R.drawable.community_edge);

            image_address.setBackground(drawable);
            image_address.setClipToOutline(true);
            Glide.with(this)
                    .load(image)
                    .into(image_address);
            starRate.setRating(star);
            editContent.setText(content);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("imageURI", imageUrl);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        imageUrl = savedInstanceState.getString("imageURI");
    }
}