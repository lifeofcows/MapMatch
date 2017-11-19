package mapmatch.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.UserProfileChangeRequest;

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

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";

    @BindView(R.id.input_first_name) EditText _firstNameText;
    @BindView(R.id.input_last_name) EditText _lastNameText;
    @BindView(R.id.gender) Spinner _gender;
    @BindView(R.id.input_email) EditText _emailText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.input_reEnterPassword) EditText _reEnterPasswordText;
    @BindView(R.id.btn_signup) Button _signupButton;
    @BindView(R.id.link_login) TextView _loginLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_choices, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        _gender.setAdapter(adapter);

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
                overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
            }
        });
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        String firstname = _firstNameText.getText().toString();
        String lastname = _lastNameText.getText().toString();
        int genderPos =_gender.getSelectedItemPosition();

        String gender = "";
        if (genderPos == 0) {
            gender = "M";
        }
        else if (genderPos == 1) {
            gender = "F";
        }
        else {
            gender = "Other";
        }

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        new SignupPOSTRequest().execute(firstname, lastname, gender, email, password);
//        // TODO: Implement your own signup logic here.
//
//        new android.os.Handler().postDelayed(
//                new Runnable() {
//                    public void run() {
//                        // On complete call either onSignupSuccess or onSignupFailed
//                        // depending on success
//                        onSignupSuccess();
//                        // onSignupFailed();
//                        progressDialog.dismiss();
//                    }
//                }, 3000);
    }


    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String firstname = _firstNameText.getText().toString();
        String lastname = _lastNameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String reEnterPassword = _reEnterPasswordText.getText().toString();

        if (firstname.isEmpty() || firstname.length() < 1) {
            _firstNameText.setError("at least 1 character");
            valid = false;
        } else {
            _firstNameText.setError(null);
        }

        if (lastname.isEmpty() || lastname.length() < 1) {
            _lastNameText.setError("at least 1 character");
            valid = false;
        } else {
            _lastNameText.setError(null);
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

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || !(reEnterPassword.equals(password))) {
            _reEnterPasswordText.setError("Password Do not match");
            valid = false;
        } else {
            _reEnterPasswordText.setError(null);
        }

        return valid;
    }

    // Create GetText Method
    private class SignupPOSTRequest extends AsyncTask<String,Void,Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            // Get user defined values
            String Firstname = params[0];
            String Lastname = params[1];
            String Gender = params[2];
            String Email = params[3];
            String Password = params[4];

            // Create data variable for sent values to server
            String data = "";

            try {
                data += URLEncoder.encode("email", "UTF-8") + "="
                        + URLEncoder.encode(Email, "UTF-8");

                data += "&" + URLEncoder.encode("firstname", "UTF-8")
                        + "=" + URLEncoder.encode(Firstname, "UTF-8");

                data += "&" + URLEncoder.encode("lastname", "UTF-8")
                        + "=" + URLEncoder.encode(Lastname, "UTF-8");

                data += "&" + URLEncoder.encode("gender", "UTF-8")
                        + "=" + URLEncoder.encode(Gender, "UTF-8");

                data += "&" + URLEncoder.encode("password", "UTF-8")
                        + "=" + URLEncoder.encode(Password, "UTF-8");

                data += "&" + URLEncoder.encode("lat", "UTF-8")
                        + "=" + URLEncoder.encode("0.0", "UTF-8");

                data += "&" + URLEncoder.encode("long", "UTF-8")
                        + "=" + URLEncoder.encode("0.0", "UTF-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return false;
            }

            String jsonString = "";
            BufferedReader reader = null;

            // Send data
            try {
                System.out.println("Sending data which is " + data);
                // Defined URL  where to send data
                URL url = new URL("http://34.239.117.6:9000/users/signup");

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

                System.out.println("jsonReader for signupactivity is" + jsonReader.toString());

                return true;

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
                                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                                startActivity(intent);
                                //onLoginSuccess();
                            } else {
                                Toast.makeText(SignupActivity.this, "Signup process failed", Toast.LENGTH_SHORT).show();
                                //onLoginFailed();
                            }
                        }
                    }, 1);
        }

    }
}