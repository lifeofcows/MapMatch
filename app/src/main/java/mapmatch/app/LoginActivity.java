package mapmatch.app;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import butterknife.ButterKnife;
import butterknife.BindView;

import static android.R.attr.content;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    ProgressDialog progressDialog;
    private static User currUser;
    @BindView(R.id.input_email)
    EditText _emailText;
    @BindView(R.id.input_password)
    EditText _passwordText;
    @BindView(R.id.btn_login)
    Button _loginButton;
    @BindView(R.id.link_signup)
    TextView _signupLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                //finish();
                overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
            }
        });
    }

    public static User getCurrUser() {
        return currUser;
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        //check if exists here
        String myUrl = "http://34.239.117.6:9000/users/";
        //String to place our result in
        String result = null;
        //Instantiate new instance of our class
        HTTPGetRequest getRequest = new HTTPGetRequest();
        //Perform the doInBackground method, passing in our url
        try {
            result = getRequest.execute(myUrl).get();
        } catch (Exception e) {
            System.out.println("Error doing get request.");
            e.printStackTrace();
        }

        System.out.println("result is: " + result);

        if (result != null && !result.isEmpty()) {
            //add stuff to internal DB
        } else {

        }

        boolean authResult = false;
        System.out.println("begin");
        try {
           new LoginPOSTRequest().execute(_emailText.getText().toString(), _passwordText.getText().toString()).get();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("end");
        progressDialog.dismiss();
//        final boolean finalAuthResult = authResult;
//        System.out.println("finalAuthResult is " + finalAuthResult);
//        new android.os.Handler().postDelayed(
//                new Runnable() {
//                    public void run() {
//                        // On complete call either onLoginSuccess or onLoginFailed
//                        if (finalAuthResult) {
//                            onLoginSuccess();
//                        } else {
//                            onLoginFailed();
//                        }
//                        //
//                        progressDialog.dismiss();
//                    }
//                }, 3000);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                //this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
        intent.putExtra("USER_EMAIL", _emailText.getText().toString());
        startActivity(intent);
        //finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

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
    // Create GetText Method
    private class LoginPOSTRequest extends AsyncTask<String,Void,Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            // Get user defined values
            String Email = params[0];
            String Password = params[1];

            // Create data variable for sent values to server
            String data = "";

            try {
                data += URLEncoder.encode("email", "UTF-8") + "="
                        + URLEncoder.encode(Email, "UTF-8");

                data += "&" + URLEncoder.encode("password", "UTF-8")
                        + "=" + URLEncoder.encode(Password, "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return false;
            }

            String jsonString = "";
            BufferedReader reader = null;

            // Send data
            try {
                System.out.println("Sending data which is " + data);
                // Defined URL  where to send data
                URL url = new URL("http://34.239.117.6:9000/users/login");

                // Send POST data request

                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(data);
                wr.flush();

                // Get the server response

                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while ((line = reader.readLine()) != null) {
                    // Append server response in string
                    sb.append(line + "\n");
                }

                jsonString = sb.toString();
                JSONObject jsonReader = new JSONObject(jsonString);

                System.out.println("jsonReader is " + jsonReader);
                boolean result = jsonReader.getBoolean("auth");
                JSONObject userJson = jsonReader.getJSONObject("user");
                currUser = new User(userJson);

                System.out.println("Got result which is " + result);
                return result;

            } catch (Exception ex) {
                System.out.println("Got exception: ");
                ex.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // The results of the above method
            // Processing the results here

            final boolean finalResult = result;
            System.out.println("Result is " + finalResult);
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            // On complete call either onLoginSuccess or onLoginFailed
                            //
                            if (finalResult) {
                                onLoginSuccess();
                            } else {
                                onLoginFailed();
                            }
                        }
                    }, 1);
        }

    }

    Handler myHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    // calling to this function from other places
                    // The notice call method of doing things
                    break;
                default:
                    break;
            }
        }
    };

}

