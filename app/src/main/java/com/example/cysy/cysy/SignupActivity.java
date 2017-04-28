package com.example.cysy.cysy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignupActivity extends AppCompatActivity {

    enum SignupErrorMessages {
        ERROR_REGISTERING ("Error Registering"),
        ERROR_PSEUDO_ALREADY_EXISTS ("Your pseudo is already exists"),
        ERROR_EMAIL_ALREADY_EXISTS ("Your email is already exists");

        private String message;

        SignupErrorMessages(String message){
            this.message = message;
        }

        String getMessage(){
            return this.message;
        }
    }

    enum SignupMessageKeys {
        CONNECT ("_connect"),
        SUCCESS ("_success"),
        ERROR ("_error"),
        USER ("_user");

        private String key;

        SignupMessageKeys(String key){
            this.key = key;
        }

        String getKey(){
            return this.key;
        }

    }

    private static final String TAG = "SignupActivity";

    private static final String REGISTER_URL = "http://cysy.000webhostapp.com/insert.php";
    private static final int WAIT_TIME_AFTER_SIGNUP = 3000;

    @BindView(R.id.input_pseudo) EditText _pseudoText;
    @BindView(R.id.input_name) EditText _nameText;
    @BindView(R.id.input_address) EditText _addressText;
    @BindView(R.id.input_email) EditText _emailText;
    @BindView(R.id.input_mobile) EditText _mobileText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.input_reEnterPassword) EditText _reEnterPasswordText;
    @BindView(R.id.btn_signup) Button _signupButton;
    @BindView(R.id.link_login) TextView _loginLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed(SignupErrorMessages.ERROR_REGISTERING);
            return;
        }

        _signupButton.setEnabled(false);

        String pseudo = _pseudoText.getText().toString();
        String name = _nameText.getText().toString();
        String address = _addressText.getText().toString();
        String email = _emailText.getText().toString();
        String mobile = _mobileText.getText().toString();
        String password = _passwordText.getText().toString();

        User user = new User(pseudo, password, name, address, email, mobile);

        registerUser(user);
    }

    static class User {

        static final String[] UserArgs = new String[]{"pseudo", "password", "name", "address", "email", "mobile"};

        String pseudo, password, name, address, email, mobile;

        User(String pseudo, String password, String name, String address, String email, String mobile) {
            this.pseudo = pseudo;
            this.password = password;
            this.name = name;
            this.address = address;
            this.email = email;
            this.mobile = mobile;
        }

        static boolean isUserArg(String fieldName) {
            for (String userArg : UserArgs){
                if(userArg.equals(fieldName)) {
                    return true;
                }
            }
            return false;
        }
    }

    private void registerUser(User user) {

        class RegisterUser extends AsyncTask<User, Void, JSONObject> {
            private final String TAG = "RegisterUser";

            private ProgressDialog loading;
            private RegisterUserClass ruc = new RegisterUserClass();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.d(TAG, "onPreExecute ...");
                loading = ProgressDialog.show(SignupActivity.this, "Please Wait ...",null, true, true);
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                super.onPostExecute(result);
                Log.d(TAG, "onPostExecute ...");
                loading.dismiss();

                onPostSignup(result);
            }

            @Override
            protected JSONObject doInBackground(User... params) {
                Log.d(TAG, "doInBackground ...");
                HashMap<String, String> data = new HashMap<String,String>();
                Field[] userFields = User.class.getFields();

                for(Field field : userFields){
                    if(User.isUserArg(field.getName())) {
                        try {
                            data.put(field.toString(), field.get(params[0]).toString());
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }

                for (String s : data.values()){
                    Log.d(TAG, s);
                }

                return ruc.sendPostRequest(REGISTER_URL,data);
            }
        }

        RegisterUser ru = new RegisterUser();
        ru.execute(user);
    }

    private void onPostSignup(JSONObject result) {

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Signing up ... ");
        progressDialog.show();

        try {
            int success = result.getInt(SignupMessageKeys.SUCCESS.getKey());

            if (success == 1) {
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                progressDialog.dismiss();

                                onSignupSuccess();

                            }
                        }, WAIT_TIME_AFTER_SIGNUP);
            } else {
                String error = result.getString(SignupMessageKeys.ERROR.getKey());

                if (error.equals(SignupErrorMessages.ERROR_REGISTERING.getMessage())) {
                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    progressDialog.dismiss();

                                    onSignupFailed(SignupErrorMessages.ERROR_REGISTERING);
                                }
                            }, WAIT_TIME_AFTER_SIGNUP);
                } else {
                    if (error.equals(SignupErrorMessages.ERROR_PSEUDO_ALREADY_EXISTS.getMessage())) {
                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {
                                        progressDialog.dismiss();

                                        onSignupFailed(SignupErrorMessages.ERROR_PSEUDO_ALREADY_EXISTS);
                                    }
                                }, WAIT_TIME_AFTER_SIGNUP);
                    } else {
                        if (error.equals(SignupErrorMessages.ERROR_EMAIL_ALREADY_EXISTS.getMessage())) {
                            new android.os.Handler().postDelayed(
                                    new Runnable() {
                                        public void run() {
                                            progressDialog.dismiss();

                                            onSignupFailed(SignupErrorMessages.ERROR_EMAIL_ALREADY_EXISTS);
                                        }
                                    }, WAIT_TIME_AFTER_SIGNUP);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed(SignupErrorMessages signupErrorMessages) {

        switch (signupErrorMessages) {
            case ERROR_REGISTERING:
                Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
                break;
            default:
                Toast.makeText(getBaseContext(), signupErrorMessages.getMessage(), Toast.LENGTH_LONG).show();
                break;
        }

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String pseudo = _pseudoText.getText().toString();
        String name = _nameText.getText().toString();
        String address = _addressText.getText().toString();
        String email = _emailText.getText().toString();
        String mobile = _mobileText.getText().toString();
        String password = _passwordText.getText().toString();
        String reEnterPassword = _reEnterPasswordText.getText().toString();

        if(pseudo.isEmpty() || pseudo.length() < 5){
            _pseudoText.setError("at least 5 characters");
            valid = false;
        } else {
            _pseudoText.setError(null);
        }

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (address.isEmpty()) {
            _addressText.setError("Enter Valid Address");
            valid = false;
        } else {
            _addressText.setError(null);
        }


        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (mobile.isEmpty() || mobile.length()!=10) {
            _mobileText.setError("Enter Valid Mobile Number");
            valid = false;
        } else {
            _mobileText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || !(reEnterPassword.equals(password))) {
            _reEnterPasswordText.setError("Password Do not match");
            valid = false;
        } else {
            _reEnterPasswordText.setError(null);
        }

        return valid;
    }
}