package com.olmatix.ui.activity;

/**
 * Created by Lesjaw on 07/01/2017.
 */

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.androidadvance.topsnackbar.TSnackbar;
import com.olmatix.lesjaw.olmatix.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    Button _signupButton;
    EditText _nameText,_emailText,_passwordText;
    TextView _loginLink;

    /*@InjectView(R.id.input_name) EditText _nameText;
    @InjectView(R.id.input_email) EditText _emailText;
    @InjectView(R.id.input_password) EditText _passwordText;*/
    //@InjectView(R.id.btn_signup) Button _signupButton;
    //@InjectView(R.id.link_login) TextView _loginLink;
    String name, email, password;
    ProgressDialog progressDialog;
    CoordinatorLayout coordinatorLayout;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.inject(this);

        _nameText = (EditText)findViewById(R.id.input_name);
        _emailText = (EditText)findViewById(R.id.input_email);
        _passwordText = (EditText)findViewById(R.id.input_password);
        _signupButton = (Button)findViewById(R.id.btn_signup);
        _loginLink = (TextView) findViewById(R.id.link_login);

        coordinatorLayout=(CoordinatorLayout)findViewById(R.id.main_content);


        _signupButton.setOnClickListener(v -> signup());

        _loginLink.setOnClickListener(v -> {
            // Finish the registration screen and return to the Login activity
            finish();
        });
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        name = _nameText.getText().toString();
        email = _emailText.getText().toString();
        password = _passwordText.getText().toString();

        progressDialogShow(0);
        onSignupSuccess();

    }


    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        sendJsonSignUp();
    }

    public void onSignupFailed() {
        //Toast.makeText(getBaseContext(), "SIgn failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    private void sendJsonSignUp(){

        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);


            String url = "http://cloud.olmatix.com/rest/insert_sent.php";
            StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
                Log.d(TAG, "onResponse: "+response);
                    parsingJson(response);
            }, error -> Log.d(TAG, "onErrorResponse: "+error)) {
                protected Map<String, String> getParams() {
                    Map<String, String> MyData = new HashMap<String, String>();
                    MyData.put("name", name); //Add the data you'd like to send to the server.
                    MyData.put("email", email);
                    MyData.put("password", password);
                    return MyData;
                }
            };

        int socketTimeout = 60000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        MyStringRequest.setRetryPolicy(policy);
        MyRequestQueue.add(MyStringRequest);


    }

    public void parsingJson(String json) {
        try {
            JSONObject jObject = new JSONObject(json);
            String msg = jObject.optString("error_msg");
            Log.d(TAG, "parsingJson: "+msg);
            TSnackbar snackbar = TSnackbar.make(coordinatorLayout,msg,TSnackbar.LENGTH_INDEFINITE);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
            snackbar.setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            snackbar.show();
            progressDialogShow(1);

            if (msg.equals("Sign up success, please wait for email confirmation")) {
                _signupButton.setEnabled(false);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void progressDialogShow (int what){
        if (what==0) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(true);
            progressDialog.setMessage("Registering your ID, please wait..");
            progressDialog.show();

        } else {
            if (progressDialog!=null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                Log.d("DEBUG", "progressDialogStop: ");
            }
        }
    }

    private void sendEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO)
                .setData(new Uri.Builder().scheme("mailto").build())
                .putExtra(Intent.EXTRA_EMAIL, new String[]{ "Olmatix <lesjaw@olmatix.com>" })
                .putExtra(Intent.EXTRA_SUBJECT, "Request Auth for Login")
                .putExtra(Intent.EXTRA_TEXT, "Your name : " + name +"\n" +" Your email : "+ email +"\n"+" Your password : "+password
                +"\n\n"+" This is your Auth Login request, we will send you a confirmation email as soon as possible.."
                +"\n\n"+ " Send this email now.."+"\n\n"+" Thank you..");

        ComponentName emailApp = intent.resolveActivity(getPackageManager());
        ComponentName unsupportedAction = ComponentName.unflattenFromString("com.android.fallback/.Fallback");
        if (emailApp != null && !emailApp.equals(unsupportedAction))
            try {
                // Needed to customise the chooser dialog title since it might default to "Share with"
                // Note that the chooser will still be skipped if only one app is matched
                Intent chooser = Intent.createChooser(intent, "Send email with");
                startActivity(chooser);
                return;
            }
            catch (ActivityNotFoundException ignored) {
            }

        Toast
                .makeText(this, "Couldn't find an email app and account", Toast.LENGTH_LONG)
                .show();
    }
}