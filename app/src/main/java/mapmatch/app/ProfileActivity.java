package mapmatch.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import butterknife.BindView;

public class ProfileActivity extends AppCompatActivity {

    User currUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView _firstName = (TextView) findViewById(R.id.firstName);
        TextView _lastName = (TextView) findViewById(R.id.lastName);
        TextView _email = (TextView) findViewById(R.id.email);
        TextView _gender = (TextView) findViewById(R.id.gender);
        TextView _movies = (TextView) findViewById(R.id.movies);
        TextView _music = (TextView) findViewById(R.id.music);
        TextView _interests = (TextView) findViewById(R.id.interests);

        currUser = LoginActivity.getCurrUser();

        System.out.println("currUser is " + currUser);

        _firstName.setText(_firstName.getText() + currUser.firstname);
        _lastName.setText(_lastName.getText() + currUser.lastname);
        _email.setText(_email.getText() +currUser.email);
        _gender.setText(_gender.getText() +currUser.gender);

        _firstName.setFocusable(false);
        _lastName.setFocusable(false);
        _email.setFocusable(false);
        _gender.setFocusable(false);
        _movies.setFocusable(false);
        _music.setFocusable(false);
        _interests.setFocusable(false);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        });
//        _firstName.setText(currUser.firstname);
//        _lastName.setText(currUser.lastname);
//        _email.setText(currUser.email);
//        _gender.setText(currUser.gender);
    }
}
