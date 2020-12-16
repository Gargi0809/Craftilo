package com.example.techno;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;


import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
{
                private LoginButton Loginbutton;
                private ImageView circleImageView;
                private TextView textname,textemail;
                private CallbackManager callbackManager;

                GoogleSignInClient mGoogleSignInClient;
                private static int RC_SIGN_IN=100;


                @Override
                protected void onCreate(Bundle savedInstanceState)
                {
                                super.onCreate(savedInstanceState);
                                setContentView(R.layout.activity_main);

                                //Facebook
                                Loginbutton=findViewById(R.id.login_button);
                                circleImageView=findViewById(R.id.profile_photo);
                                textname=findViewById(R.id.profile_name);
                                textemail=findViewById(R.id.profile_email);


                                callbackManager=CallbackManager.Factory.create();
                                Loginbutton.setPermissions(Arrays.asList("email","public_profile"));
                                checkLoginStatus();

                                Loginbutton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                                    @Override
                                    public void onSuccess(LoginResult loginResult) {

                                    }

                                    @Override
                                    public void onCancel() {

                                    }

                                    @Override
                                    public void onError(FacebookException error) {

                                    }
                                });


                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .build();

                    // Build a GoogleSignInClient with the options specified by gso.
                    mGoogleSignInClient =GoogleSignIn.getClient(this,gso);

                    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                    //updateUI(account);

                    SignInButton signInButton = findViewById(R.id.sign_in_button);
                    signInButton.setSize(SignInButton.SIZE_STANDARD);

                    signInButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            signIn();
                        }
                    });





                }



                private void signIn() {
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                }
                @Override
                protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
                {
                    callbackManager.onActivityResult(requestCode,resultCode,data);
                    super.onActivityResult(requestCode, resultCode, data);

                    // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
                    if (requestCode == RC_SIGN_IN) {
                        // The Task returned from this call is always completed, no need to attach
                        // a listener.
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleSignInResult(task);
                    }
                }

                private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
                    try {
                        GoogleSignInAccount account = completedTask.getResult(ApiException.class);

                        // Signed in successfully, show authenticated UI.
                       // updateUI(account);
                        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
                        if (acct != null) {
                            String personName = acct.getDisplayName();
                            String personGivenName = acct.getGivenName();
                            String personFamilyName = acct.getFamilyName();
                            String personEmail = acct.getEmail();
                            String personId = acct.getId();
                            Uri personPhoto = acct.getPhotoUrl();


                            TextView name=findViewById(R.id.profile_name);
                            name.setText(personName);

                            TextView email=findViewById(R.id.profile_email);
                            email.setText(personEmail);

                            Glide.with(MainActivity.this).load(personPhoto).into(circleImageView);


                        }

                    }
                    catch (ApiException e) {
                        // The ApiException status code indicates the detailed failure reason.
                        // Please refer to the GoogleSignInStatusCodes class reference for more information.


                        Log.d("Message",e.toString());
                        //updateUI(null);
                    }
                }

                AccessTokenTracker tokenTracker=new AccessTokenTracker()
                {
                    @Override
                    protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken)
                    {
                        if(currentAccessToken==null)
                        {
                            textname.setText(" ");
                            textemail.setText(" ");
                            circleImageView.setImageResource(0);
                            Toast.makeText(MainActivity.this,"User logged out!!",Toast.LENGTH_LONG).show();
                        }
                        else
                            loaduserProfile(currentAccessToken);

                    }
                };

                private  void loaduserProfile(AccessToken newAccessToken)
                {
                    GraphRequest request= GraphRequest.newMeRequest(newAccessToken, new GraphRequest.GraphJSONObjectCallback()
                    {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            try {
                                String fname=(String)object.get("first_name");
                                String lname=object.getString("last_name");
                                String email=object.getString("email");
                                String id=object.getString("id");

                                String image_url="https://graph.facebook.com/"+id+"/picture?type=square";



                                textemail.setText(email);
                                textname.setText(fname+" "+lname);
                                RequestOptions requestOptions=new RequestOptions();
                                requestOptions.dontAnimate();

                                Glide.with(MainActivity.this).load(image_url).into(circleImageView);


                            } catch (JSONException e) {

                                e.printStackTrace();

                            }



                        }
                    });
                    Bundle para=new Bundle();
                    para.putString("fields","first_name,last_name,email,id");
                    request.setParameters(para);
                    request.executeAsync();

                }

                private void checkLoginStatus()
                {
                    if(AccessToken.getCurrentAccessToken()!=null)
                    {
                        loaduserProfile(AccessToken.getCurrentAccessToken());
                    }
                }

}