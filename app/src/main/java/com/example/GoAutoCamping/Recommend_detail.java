package com.example.GoAutoCamping;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class Recommend_detail extends Fragment implements Recommend_detail_reviewdialog.SendData {

    Context context;
    FloatingActionButton likebtn;
    ExtendedFloatingActionButton reviewbtn;
    String placeName, placeId;

    CoordinatorLayout coordinatorLayout;

    ImageView imageV, noneImage;
    TextView name_detail, address, ratingText, reviewNumText, noneText;
    RecyclerView recyclerView_filter;
    List<Boolean> RecommendFilter = new ArrayList<>();
    List<Recommend_filterdesDTO> filterdesDTOS;
    ArrayList<String> checkName;
    Recommend_detail_filter_RecyclerAdapter adapter;

    int[] filterImage = {R.drawable.toilet, R.drawable.shower, R.drawable.storeimg, R.drawable.cooking};
    String[] checkingName = { "???", "???", "??????", "??????", "?????????", "??????", "?????????", "?????????", "?????????", "??????", "??????"};
    ChipGroup chipGroup;

    RecyclerView recyclerView_review;
    Recommend_detail_reviewdialog dlg;
    private Recommend_detail_review_RecyclerAdapter review_adapter;
    private DatabaseReference ReviewsReference;
    Map<String, Recommend_reviewDTO> reviews = new HashMap<>();
    public boolean checkReview = true;

    Float rating = 0f;
    int reviewNum = 0;

    //??????????????????
    private FirebaseStorage storage;
    private String imageUrl="";
    private FirebaseFirestore Firestore;
    private String email;
    private FirebaseAuth user;

    //?????? ??????
    String userNickName, userProfile;

    @Override
    public void sendData(float rating, String review) {
        // ????????? ?????????
        postComment(review, rating);
    }

    @Override
    public void clearAll() {

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.recommend_detail, container, false);

        likebtn = view.findViewById(R.id.like);
        imageV = view.findViewById(R.id.image_detail);
        name_detail = view.findViewById(R.id.name_detail);
        address = view.findViewById(R.id.address_detail);
        reviewbtn = view.findViewById(R.id.reviewBtn);
        ratingText = view.findViewById(R.id.ratingText);
        reviewNumText = view.findViewById(R.id.reviewNumText);
        coordinatorLayout = view.findViewById(R.id.snackbar_line);
        noneImage = view.findViewById(R.id.image_none);
        noneText = view.findViewById(R.id.text_none);

        //chipLayout = view.findViewById(R.id.chip_Layout);
        chipGroup = view.findViewById(R.id.chip_group);

        recyclerView_filter = view.findViewById(R.id.filterDes_detail);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView_filter.setLayoutManager(layoutManager);

        recyclerView_review = view.findViewById(R.id.review_detail);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getActivity());
        layoutManager2.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView_review.setLayoutManager(layoutManager2);

        FragmentActivity activity = getActivity();
        ((MainActivity)activity).hideBottomNavi(true);

        Bundle bundle = getArguments();

        if(bundle != null){
            placeName = bundle.getString("placeName");
            placeId = bundle.getString("placeId");

            Firestore = FirebaseFirestore.getInstance();

            load();
            reviewAdapter();
            checkReviewUser();
            calRate();
        }

        //???????????? ?????? ??????
        likebtn.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                boolean ch = checkLogin();
                if(ch){
                    addlike();
                }
            }
        });

        //????????????
        dlg = new Recommend_detail_reviewdialog();

        reviewbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean ch = checkLogin();
                if(ch){
                    if(checkReview){
                        dlg.setTargetFragment(Recommend_detail.this, 1);
                        dlg.show(getActivity().getSupportFragmentManager(), "tag");
                    }
                    else{
                        Snackbar.make(coordinatorLayout, "?????? ????????? ???????????????", Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });

        return view;
    }

    public void filterload(){

        checkName = new ArrayList<>();
        filterdesDTOS = new ArrayList<>();

        Log.d("??????", "??????????" +  RecommendFilter.size());
        //?????? ??????
        for(int i = 0; i < 7; i++){

            if(RecommendFilter.get(i))
                filterdesDTOS.add(new Recommend_filterdesDTO(i));
        }

        //??? ??????
        for(int i = 7; i < RecommendFilter.size(); i++){
            if(RecommendFilter.get(i))
                checkName.add(checkingName[i]);
        }

        adapter = new Recommend_detail_filter_RecyclerAdapter(context, filterdesDTOS);
        recyclerView_filter.setAdapter(adapter);

        addChipView(checkName);
    }

    //??? ??? ??????
    public void addChipView(ArrayList<String> name) {

        for (int i = 0; i < name.size(); i++) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.filter_chip_layout, chipGroup, false);
            chip.setText(name.get(i));
            chip.setCloseIconVisible(false);
            chip.setIconStartPadding(10.0f);

            switch (name.get(i)){
                case "?????????" : chip.setChipIcon(getResources().getDrawable(filterImage[0])); break;
                case "?????????" : chip.setChipIcon(getResources().getDrawable(filterImage[1])); break;
                case "??????" : chip.setChipIcon(getResources().getDrawable(filterImage[2])); break;
                case "??????" : chip.setChipIcon(getResources().getDrawable(filterImage[3])); break;
            }
            chip.setClickable(false);

            chipGroup.addView(chip);
        }
    }

    //???????????? ????????? ????????????
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void load(){

        Firestore = FirebaseFirestore.getInstance();

        DocumentReference docRef = Firestore.collection("places").document(placeName).collection("innerPlaces").document(placeId);

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                RecommendDTO recommendDTO = documentSnapshot.toObject(RecommendDTO.class);

                Glide.with(getContext())
                        .load(recommendDTO.getRecommendImage())
                        .into(imageV);

                name_detail.setText(recommendDTO.getRecommendTitle());

                address.setText(recommendDTO.getRecommendAddress());

                for(int i = 0; i < recommendDTO.getRecommendFilter().size(); i++) {
                    RecommendFilter.add(recommendDTO.getRecommendFilter().get(i));
                }

                filterload();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    public boolean checkLogin(){
        boolean ch = false;

        if(email != null){
            ch = true;
            return ch;
        }
        else{
            //??????????????? ????????????
            MaterialDialog dialog = new MaterialDialog(getContext(), MaterialDialog.getDEFAULT_BEHAVIOR());
            dialog.title(null, "????????? ??????");
            dialog.message(null, "???????????? ????????? ???????????????. \n????????? ????????????.", null);
            //dialog.icon(null, getResources().getDrawable(R.drawable.ic_baseline_report_24));
            dialog.positiveButton(null, "??????", materialDialog -> {
                dialog.dismiss();
                return null;
            });
            dialog.show();
        }
        return ch;

    }

    //????????? ?????? ????????? ??????
    public void checklike(){
        Firestore = FirebaseFirestore.getInstance();

        final DocumentReference documentReference = Firestore.collection("places").document(placeName).collection("innerPlaces").document(placeId);

        //???????????? ?????? ??????????????? ???????????????
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                ArrayList<String> group = (ArrayList<String>) documentSnapshot.get("RecommendLikeUser");

                if(group.contains(email)){
                    likebtn.setImageResource(R.drawable.like_full);
                }
                else{
                    likebtn.setImageResource(R.drawable.like);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    //????????? ??????
    public void addlike(){
        Firestore = FirebaseFirestore.getInstance();

        final DocumentReference documentReference = Firestore.collection("places").document(placeName).collection("innerPlaces").document(placeId);
        final DocumentReference documentReferenceUser = Firestore.collection("users").document(email);

        //???????????? ????????? ?????? - ??????
        documentReferenceUser.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                ArrayList<String> group = (ArrayList<String>) documentSnapshot.get("userFavorite");

                if(group.contains(placeId)){
                    documentReferenceUser.update("userFavorite", FieldValue.arrayRemove(placeId));
                }
                else{
                    documentReferenceUser.update("userFavorite", FieldValue.arrayUnion(placeId));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


        //????????? ????????? ????????? ?????? - ??????
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                ArrayList<String> group = (ArrayList<String>) documentSnapshot.get("RecommendLikeUser");

                if(group.contains(email)){
                    documentReference.update("RecommendLikeUser", FieldValue.arrayRemove(email));
                    likebtn.setImageResource(R.drawable.like);

                }
                else{
                    documentReference.update("RecommendLikeUser", FieldValue.arrayUnion(email));
                    likebtn.setImageResource(R.drawable.like_full);

                }
                ((MainActivity)getActivity()).setMarkbtn();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

        //????????? ????????? ?????? ????????? ??? ???????????????
        Firestore.runTransaction(new Transaction.Function<Double>() {
            @Nullable
            @Override
            public Double apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(documentReference);
                ArrayList<String> group = (ArrayList<String>) snapshot.get("RecommendLikeUser");

                Double likenum = Double.valueOf(group.size());

                if(group.contains(email) && likenum > 0){
                    likenum = likenum - 1;
                    transaction.update(documentReference, "RecommendLike", likenum);
                }
                else{
                    likenum = likenum + 1;
                    transaction.update(documentReference, "RecommendLike", likenum);
                }

                return likenum;
            }
        }).addOnSuccessListener(new OnSuccessListener<Double>() {
            @Override
            public void onSuccess(Double integer) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


    }

    public void calRate(){

        Firestore = FirebaseFirestore.getInstance();

        final DocumentReference documentReference = Firestore.collection("places").document(placeName).collection("innerPlaces").document(placeId);

        ReviewsReference =  FirebaseDatabase.getInstance().getReference().child("Places").child(placeId);
        Query reviewQuery = ReviewsReference.orderByChild("recommendReviewStar");

        reviewQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Float rate = 0f;
                int rateNum = 0;

                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    Recommend_reviewDTO reviewDTO = postSnapshot.getValue(Recommend_reviewDTO.class);
                    rate += reviewDTO.getRecommendReviewStar();
                    rateNum++;
                }

                rating = rate;
                reviewNum = rateNum;

                String r = String.format("%.1f", (rating / reviewNum));
                if(r.equals("NaN")){
                    ratingText.setText("0.0");
                    documentReference.update("RecommendStar", 0f);
                    noneImage.setVisibility(View.VISIBLE);
                    noneText.setVisibility(View.VISIBLE);
                }else{
                    ratingText.setText(r);
                    documentReference.update("RecommendStar", rating / reviewNum);
                    noneImage.setVisibility(View.INVISIBLE);
                    noneText.setVisibility(View.INVISIBLE);
                }
                reviewNumText.setText("??? " + reviewNum + "?????? ??????????????????");

                Log.d("????????????", "????????? : " + rating);
                Log.d("????????????", "?????? : " + reviewNum);
                Log.d("????????????", "???????????? : " + rating / reviewNum);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    //????????? ????????? ??????????????? ??????
    public void checkReviewUser(){

        ReviewsReference =  FirebaseDatabase.getInstance().getReference().child("Places").child(placeId);
        Query reviewQuery = ReviewsReference.orderByChild("recommendReviewStar");

        reviewQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    Recommend_reviewDTO reviewDTO = postSnapshot.getValue(Recommend_reviewDTO.class);
                    if(reviewDTO.getRecommendReviewId().equals(email))
                        checkReview = false;
                    else{
                        checkReview = true;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    public void reviewAdapter(){
        ReviewsReference = FirebaseDatabase.getInstance().getReference().child("Places").child(placeId);
        review_adapter = new Recommend_detail_review_RecyclerAdapter(context, ReviewsReference);

        review_adapter.setOnItemLongClickListener(new Recommend_detail_review_RecyclerAdapter.OnItemLongClickEventListener() {
            @Override
            public void onItemLongClick(View a_view, int a_position,List<String> mReviewIds, List<Recommend_reviewDTO> reviewDTOS) {
                Recommend_reviewDTO recommend_reviewDTO = reviewDTOS.get(a_position);
                String reviewID = mReviewIds.get(a_position);

                //?????????????????? ????????? ????????? ????????????
                if(recommend_reviewDTO.getRecommendReviewId().equals(email)){
                    MaterialDialog dialog = new MaterialDialog(getContext(), MaterialDialog.getDEFAULT_BEHAVIOR());
                    dialog.title(null, "?????? ??????");
                    dialog.message(null, "????????? ?????????????????????????", null);
                    dialog.positiveButton(null, "??????", materialDialog -> {
                        Snackbar.make(coordinatorLayout, "????????? ?????????????????????!", Snackbar.LENGTH_SHORT).show();
                        onDeleteContent(reviewID);

                        return null;
                    });
                    dialog.negativeButton(null, "?????????", materialDialog -> {
                        dialog.dismiss();
                        return null;
                    });
                    dialog.show();
                }
                else{
                    Snackbar.make(coordinatorLayout, "?????? ???????????? ????????????", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        recyclerView_review.setAdapter(review_adapter);
    }

    //?????? ??????
    private void postComment(String reviewText, float reviewRating) {
        // ?????? ?????? ?????????
        Recommend_reviewDTO review = new Recommend_reviewDTO(email, userProfile, userNickName, reviewText, reviewRating);

        // ?????? ????????????
        ReviewsReference.push().setValue(review);


    }

    //?????? ??????
    private void onDeleteContent(String item)
    {
        ReviewsReference.child(item).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        checkReview = true;
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) { }
        });
    }

    //?????? ?????? ??????
    public abstract class OnSingleClickListener implements View.OnClickListener{
        //?????? ?????? ?????? ?????? ?????? ( ?????? ?????? ????????? ?????? ?????? ?????? )
        private static final long MIN_CLICK_INTERVAL = 1000; //1sec
        private long mLastClickTime = 0;
        public abstract void onSingleClick(View v);
        @Override public final void onClick(View v) {
            long currentClickTime = SystemClock.uptimeMillis();
            long elapsedTime = currentClickTime - mLastClickTime;
            mLastClickTime = currentClickTime;
            // ???????????? ?????? ??????
            if (elapsedTime > MIN_CLICK_INTERVAL) {
                onSingleClick(v);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        FragmentActivity activity = getActivity();
        if(activity!=null){
            ((MainActivity)activity).setBackBtn(2,true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onStart() {
        super.onStart();

        checklike();

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;

        Firestore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        FirebaseUser currentUser = user.getCurrentUser();
        if (currentUser != null){
            email = currentUser.getEmail();

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

}