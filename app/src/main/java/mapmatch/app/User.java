package mapmatch.app;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by maximkuzmenko on 2017-11-18.
 */

public class User {
    /*
    "firstname" : "mIKE",
       "lastname" : "MIKE",
       "email" : "MIKE@MIKE.COM",
       "password" : "MIKE",
       "gender" : "M",
       "lat" : "0",
       "long" : "0",
       "_id" : ObjectId("5a10c04b32eead2b6e9283c4"),
       "__v" : 0
     */
    String firstname, lastname, email, gender, password;
    String[] music, interests, movies;
    double latitude, longitude;

    public User(String firstname, String lastname, String email, String gender, double latitude, double longitude, String[] music, String[] movies, String[] interests, String password) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.gender = gender;
        this.latitude = latitude;
        this.longitude = longitude;
        this.movies = movies;
        this.music = music;
        this.interests = interests;
        this.password = password;
    }

    public User(JSONObject object) {
        try {
            this.firstname = object.getString("firstname");
            this.lastname = object.getString("lastname");
            this.email = object.getString("email");
            this.gender = object.getString("gender");
            this.password = object.getString("password");
            this.latitude = Double.parseDouble(object.getString("lat"));
            this.longitude = Double.parseDouble(object.getString("long"));
            System.out.println("movies are: " + object.get("movies"));
            System.out.println("music are: " + object.get("music"));
            System.out.println("interests are: " + object.get("interests"));
            this.interests = new String[]{"rockclimbing", "bowing", "hacking"};
            this.movies = new String[]{"shrek"};

            //this.movies = //(String[]) object.get("movies");
            //this.music = (String[]) object.get("music");
            //this.interests = (String[]) object.get("interests");
        }
        catch (JSONException e) {
            System.out.println("JSON Exception: ");
            e.printStackTrace();
        }
    } //email, gender, movies, music, interests
}
