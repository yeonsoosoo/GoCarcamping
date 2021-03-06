package com.example.GoAutoCamping;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class Login_findpw extends AppCompatActivity{

    CoordinatorLayout snackbar;
    ConstraintLayout phoneLayout, emailLayout;
    TextInputLayout nameL_findpw, idL_findpw, phoneNumL_findpw, codeL_findpw;
    TextInputEditText name_findpw, id_findpw, phoneNum_findpw, code_findpw;
    MaterialButton btnfindpw, btnSendCode;
    SwitchMaterial switchMaterial;
    int mode = 2;   //????????? - 1
    //???????????? -2

    boolean phoneVerified = false;
    String phone = "";
    String name = "";
    String code = "";

    String memail = "";
    String mpass = "";

    //??????????????????
    private FirebaseFirestore Firestore;
    private FirebaseAuth FireAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_findpw);

        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        phoneLayout = findViewById(R.id.phoneL);
        emailLayout = findViewById(R.id.emailL);

        snackbar = findViewById(R.id.snackbar_line);
        switchMaterial = findViewById(R.id.switchMaterial);

        nameL_findpw = findViewById(R.id.nameLayout_findpw);
        idL_findpw = findViewById(R.id.idLayout_findpw);
        phoneNumL_findpw = findViewById(R.id.phonenumLayout_findpw);
        codeL_findpw = findViewById(R.id.verificationCodeLayout_findpw);
        name_findpw = findViewById(R.id.nameText_findpw);
        id_findpw = findViewById(R.id.idText_findpw);
        phoneNum_findpw = findViewById(R.id.phonenumText_findpw);
        code_findpw = findViewById(R.id.verificationCode_findpw);

        btnfindpw = findViewById(R.id.btnFindPW);
        btnSendCode = findViewById(R.id.btnSendCode_findpw);

        Firestore = FirebaseFirestore.getInstance();
        FireAuth = FirebaseAuth.getInstance();

        switchMaterial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(switchMaterial.isChecked()){
                    mode = 1;
                    phoneNumL_findpw.setErrorEnabled(false);
                    emailLayout.setVisibility(View.VISIBLE);
                    phoneLayout.setVisibility(View.INVISIBLE);
                    switchMaterial.setText("?????????");
                    btnSendCode.setClickable(false);
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(phoneNumL_findpw.getWindowToken(), 0);

                }
                else{
                    mode = 2;
                    idL_findpw.setErrorEnabled(false);
                    phoneLayout.setVisibility(View.VISIBLE);
                    emailLayout.setVisibility(View.INVISIBLE);
                    switchMaterial.setText("????????????");
                    btnSendCode.setClickable(true);
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(idL_findpw.getWindowToken(), 0);

                }
            }
        });


        //????????? ????????? ??????
        id_findpw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {   //???????????? ??????????????? ??????
                if(!android.util.Patterns.EMAIL_ADDRESS.matcher(id_findpw.getText().toString()).matches() && mode == 1){
                    idL_findpw.setError("????????? ????????? ??????????????????.");
                }
                else{
                    idL_findpw.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                //????????? ?????? ??????
                if(!android.util.Patterns.EMAIL_ADDRESS.matcher(id_findpw.getText().toString()).matches() && mode == 1){
                    idL_findpw.setError("????????? ????????? ??????????????????.");
                }
                else{
                    idL_findpw.setErrorEnabled(false);
                }
            }
        });

        //???????????? ????????? ??????
        phoneNum_findpw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = phoneNum_findpw.getText().toString();
                if(text.equals("")){
                    phoneNumL_findpw.setError("?????? ???????????????");
                }else{
                    phoneNumL_findpw.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        code_findpw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = code_findpw.getText().toString();
                if(text.equals("")){
                    codeL_findpw.setError("?????? ???????????????");
                }else{
                    codeL_findpw.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //??????
        name_findpw.addTextChangedListener(new TextWatcher() {
            @Override //????????? ????????? ??????
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override //????????? ??????????????? ??????
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = name_findpw.getText().toString();
                if(text.equals("")){
                    nameL_findpw.setError("?????? ???????????????");
                }else{
                    nameL_findpw.setErrorEnabled(false);
                }
            }

            @Override //????????? ?????? ?????? ??????
            public void afterTextChanged(Editable s) { }
        });

        //???????????? ?????????
        btnSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateForm2()){
                    name = name_findpw.getText().toString();
                    phone = phoneNum_findpw.getText().toString();
                    checkUserPhone();
                }
            }
        });

        btnfindpw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mode == 1){
                    if(validateForm1()){
                        String name = name_findpw.getText().toString();
                        String email = id_findpw.getText().toString();
                        checkUser(name, email);
                    }
                }
                else if(mode == 2){
                    Log.d("?????? ???>", "??????");
                    if(validateForm2()){
                        Log.d("?????? ???>", "??????");
                        code = code_findpw.getText().toString();
                        if(!TextUtils.isEmpty(code)){
                            Log.d("?????? ???>", "??????");


                            verifyPhoneNumberWithCode(mVerificationId, code);
                        }
                    }
                }

            }
        });


        //????????? ?????? ?????? ?????? ??????
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d("TAG", "onVerificationCompleted:" + credential);
                Log.d("?????? ??????", "??????????????????.");

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w("TAG", "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {

                } else if (e instanceof FirebaseTooManyRequestsException) {

                }
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {

                Log.d("TAG", "onCodeSent:" + verificationId);

                mVerificationId = verificationId;
                mResendToken = token;
            }

        };

    }



    //??? ??????
    private boolean validateForm1() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        boolean valid = true;

        //??????
        String name = name_findpw.getText().toString();
        //?????????
        String email = id_findpw.getText().toString();

        if(TextUtils.isEmpty(name)){
            valid = false;
            name_findpw.requestFocus();
            imm.showSoftInput(name_findpw, InputMethodManager.SHOW_IMPLICIT);
        }
        else if (TextUtils.isEmpty(email)) {
            valid = false;
            id_findpw.requestFocus();
            imm.showSoftInput(id_findpw, InputMethodManager.SHOW_IMPLICIT);
        }
        else if(id_findpw.getError() != null){
            valid = false;
            id_findpw.requestFocus();
            imm.showSoftInput(id_findpw, InputMethodManager.SHOW_IMPLICIT);
        }
        else{
            //mBinding.fieldPassword.setError(null);
        }

        return valid;
    }

    //??? ??????
    private boolean validateForm2() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        boolean valid = true;

        //??????
        String name = name_findpw.getText().toString();
        //????????????
        String phone = phoneNum_findpw.getText().toString();

        if(TextUtils.isEmpty(name)){
            valid = false;
            name_findpw.requestFocus();
            imm.showSoftInput(name_findpw, InputMethodManager.SHOW_IMPLICIT);
        }
        else if (TextUtils.isEmpty(phone)) {
            valid = false;
            phoneNum_findpw.requestFocus();
            imm.showSoftInput(phoneNum_findpw, InputMethodManager.SHOW_IMPLICIT);
        }
        else{
            //mBinding.fieldPassword.setError(null);
        }

        return valid;
    }



    //?????? ?????? ?????? - ?????????
    public void checkUser(String name, String email){
        Firestore.collection("users").document(email).get().addOnCompleteListener(this, new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();

                    if(documentSnapshot.get("userName") != null){
                        if(!documentSnapshot.get("userName").toString().equals(name)){
                            Snackbar.make(snackbar, "?????? ????????? ???????????? ????????????.", Snackbar.LENGTH_SHORT).show();
                        }
                        else{
                            sendPWmail(email);
                            Snackbar.make(snackbar, "????????? ?????????????????????!", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        Snackbar.make(snackbar, "?????? ????????? ???????????? ????????????.", Snackbar.LENGTH_SHORT).show();
                    }

                }
                else{
                    Snackbar.make(snackbar, "?????? ????????? ???????????? ????????????.", Snackbar.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(snackbar, "?????? ????????? ???????????? ????????????.", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    public void checkUserPhone(){



        Firestore.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    boolean check = false;
                    List<DocumentSnapshot> dd = task.getResult().getDocuments();
                    for(int i = 0; i < dd.size(); i++){
                        if(dd.get(i).get("userName").toString().equals(name) && dd.get(i).get("userPhone").toString().equals(phone)){

                            memail = dd.get(i).getId();
                            mpass = dd.get(i).get("userPasswd").toString();

                            Snackbar.make(snackbar, "??????????????? ?????????????????????!", Snackbar.LENGTH_SHORT).show();
                            startPhoneNumberVerification(phone);
                            codeL_findpw.setVisibility(View.VISIBLE);
                            check = true;
                            return;
                        }
                    }
                    if(!check)
                        Snackbar.make(snackbar, "?????? ????????? ???????????? ????????????.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });


    }

    //????????? ?????????
    public void sendPWmail(String emailAddress)
    {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("????????? ?????????", "Email sent.");
                        }
                    }
                });

    }


    //???????????? ????????????
    private void startPhoneNumberVerification(String phoneNumber) {
        String phonenum = "+82"+phoneNumber;
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(FireAuth)
                        .setPhoneNumber(phonenum)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);   //???????????????

        Snackbar.make(snackbar, "??????????????? ?????????????????????", Snackbar.LENGTH_SHORT).show();

    }

    //???????????? ???????????????
    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(FireAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .setForceResendingToken(token)     // ForceResendingToken from callbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

        Snackbar.make(snackbar, "??????????????? ????????????????????????", Snackbar.LENGTH_SHORT).show();
    }

    //??????????????? ?????????????????????
    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        signInWithPhoneAuthCredential(credential);
    }

    //??????????????? ????????? - ?????? ?????? ?????? ??????
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FireAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Log.d("TAG", "signInWithCredential:success");
                            phoneVerified = true;
                            checkPhoneVerification(phoneVerified);

                        } else {

                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            Snackbar.make(snackbar, "????????? ?????????????????????", Snackbar.LENGTH_SHORT).show();
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {

                            }
                        }
                    }
                });
    }

    private void checkPhoneVerification(boolean phoneVerified){
        if(phoneVerified) {
            //????????????
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            user.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("?????? ???????????????", "?????? ?????? ??????");
                            }
                        }
                    });

            //?????? ?????????????????? - ???????????? ?????? ??????
            Intent intent = new Intent(this, Login_findpw_resetpw.class);
            intent.putExtra("email", memail);
            intent.putExtra("passwd", mpass);
            startActivity(intent);
            finish();

        }
        else{
            //???????????? ????????? ??????
        }
    }

}
