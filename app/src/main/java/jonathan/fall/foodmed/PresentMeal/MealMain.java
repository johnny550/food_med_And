package jonathan.fall.foodmed.PresentMeal;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import databaseSettings.DatabaseHelper;
import jonathan.fall.foodmed.Dashboard.Dashboard;
import jonathan.fall.foodmed.R;


import static android.app.Activity.RESULT_OK;

public class MealMain extends Fragment {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    public ImageView myThumbnail, photoAdding, photoTaking;
    public TextView previous, submit;
    private static final String TAG = "MyActivity";
    static final int REQUEST_TAKE_PHOTO = 1;
    private File photoFile = null;
    public Bitmap myBitmap;

    private ProgressDialog progress;
    public EditText mealComments;
    public Map<String, String> questions;

    public String appetite, condition, volumeMeal;
    public String freeComments;

    public static final String SHARED_PREF = "shPrefs";

    public DatabaseHelper myDBHelper;

    public String patientIDRetrievied;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_present_meal, container, false);
        try {
            getQuestions();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //initializing the database helper
        myDBHelper = new DatabaseHelper(getActivity());

        //Linking myThumbnail to the imageView thumbnail on our layout
        myThumbnail = v.findViewById(R.id.thumbnail);
        //SAME
        photoAdding = v.findViewById(R.id.add_photo);
        photoTaking = v.findViewById(R.id.take_photo);

        //a listener for taking pictures
        photoTaking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        patientIDRetrievied = getPatientID();
        mealComments = v.findViewById(R.id.meal_comments);
        previous = v.findViewById(R.id.PreviousBFinal);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deletePreviouslySavedData();
                Objects.requireNonNull(getFragmentManager())
                        .beginTransaction()
                        .replace(R.id.fragment_container, new ConditionStatement())
                        .addToBackStack(null)
                        .commit();
            }
        });

        submit = v.findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageSender start_task = new imageSender();
                readMyData();
                start_task.execute();
            }
        });

        return v;
    }

    /**
     * Method to verify if the device is connected to the Internet
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void readMyData() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        String mealsList[] = getResources().getStringArray(R.array.meal_propositions);
        String fullQuestionsList[] = getResources().getStringArray(R.array.first_questions_block);
        String questionsList[] = new String[8];
        for (int i = 0; i < fullQuestionsList.length - 1; i++) {
            questionsList[i] = fullQuestionsList[i];
        }
        questions = new HashMap<>();
        for (String item : mealsList) {
            Boolean meal = sharedPref.getBoolean(item, false);
            //Log.d("TAG", "--"+item+": "+meal);
            if (meal) {
                //TODO
                //question8 = meal;
            }
        }

        for (int i = 0; i < questionsList.length; i++) {
            Boolean question = sharedPref.getBoolean(questionsList[i], false);
            int newIndex = i + 1;
            questions.put("question" + newIndex, Boolean.toString(question));
        }

        appetite = String.valueOf(sharedPref.getFloat("Appetite", 0));
        condition = String.valueOf(sharedPref.getFloat("Condition", 0));
        volumeMeal = sharedPref.getString("volume_meal", "0");
        freeComments = mealComments.getText().toString();
    }

    public void deletePreviouslySavedData() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        String mealsList[] = getResources().getStringArray(R.array.meal_propositions);
        SharedPreferences.Editor editor = sharedPref.edit();
        for (String item : mealsList) {
            // if(sharedPref.getString(item,"")){
            editor.putBoolean(item, false);
            //}
        }
        editor.commit();
    }

    public void dispatchTakePictureIntent() {
        //No need to save the image
  /*     Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
 */
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(Objects.requireNonNull(getActivity()).getPackageManager()) != null) {
            // Create the File where the photo should go

            try {
                photoFile = createImageFile();
            } catch (Exception ex) {
                // Error occurred while creating the File
                Log.i(TAG, "We are in-----------------------------------------------");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity().getApplicationContext(),
                        "jonathan.fall.foodmed.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                //startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    //for saving the photo as a full size image
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Objects.requireNonNull(getActivity()).getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        // String currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
/*            Bundle extras = data.getExtras();
            assert extras != null;
            Bitmap imageBitmap = (Bitmap) Objects.requireNonNull(extras).get("data");
            myThumbnail.setImageBitmap(imageBitmap);*/
            myBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            myThumbnail.setImageBitmap(myBitmap);
            submit.setVisibility(View.VISIBLE);

        }
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    public String getPatientID() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        String patientID = sharedPref.getString("patientID", "0000");
        return patientID;
    }

    public JSONObject getQuestions() throws JSONException {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);

        String questionsList[] = new String[8];
        questionsList = getResources().getStringArray(R.array.first_questions_block);
        JSONObject mu = new JSONObject();
        for (int i = 0; i < questionsList.length; i++) {
            Boolean question = sharedPref.getBoolean(questionsList[i], false);
            int newIndex = i + 1;
            mu.put("question" + newIndex, question);
            // Toast.makeText(getActivity().getApplicationContext(), "question "+newIndex+" : "+question, Toast.LENGTH_SHORT).show();
        }
        return mu;
    }


    public class imageSender extends AsyncTask<Void, Void, Void> {
        private String reqResponse;

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(getActivity());
            progress.setTitle("Uploading....");
            progress.setMessage("Please wait until the process is finished");
            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
            progress.show();

        }

        @Override
        protected Void doInBackground(Void... params) {
            String realPath = photoFile.getAbsolutePath();
            try {
                if (isNetworkAvailable()) {
/*                    try {
                        JSONObject toSend = getDatabaseContent();
                    }catch (SQLiteException ex){
                        ex.getMessage();
                    }*/
                  /*  try {
                        sendDBContent(getDatabaseContent());
                    } catch (Exception ex) {
                        //set reqReponse as an error
                        reqResponse = "error";
                        ex.getMessage();
                    }*/
                    //get the patient ID from the shared pref
                    if (checkIfSomethingInDB() > 0) {
                        reqResponse = postImage(realPath, true, getDatabaseContent());
                        //empty the database
                        deleteDBContent();
                        checkIfSomethingInDB();
                        reqResponse = postImage(realPath, false, new JSONObject());
                        //deleteDBContent();
                    } else {
                        reqResponse = postImage(realPath, false, new JSONObject());
                    }
                } else {
                    //set reqReponse as an error
                    reqResponse = "error";
                    //turn the image form bitmap to an array of bytes
                    //byte[] dataFromPic = getBitmapAsByteArray(myBitmap);
                    //get the patientID

                    //Insert in the database
                    try {
                        AddData(patientIDRetrievied, realPath, Double.parseDouble(condition), Double.parseDouble(appetite),
                                Double.parseDouble(volumeMeal),
                                freeComments,
                                questions);
                        //Reinitialize the shared preferences
                        spReinitialization();
                        reqResponse = "DB ok";
                    } catch (Exception e) {
                        e.getMessage();
                    }
                }
            } catch (Exception e) {
                e.getMessage();
                progress.dismiss();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progress.dismiss();
            if (reqResponse.contains("DB ok")) {
                new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("Data saving")
                        .setMessage("Your data has been saved!")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Objects.requireNonNull(getFragmentManager())
                                        .beginTransaction()
                                        .replace(R.id.fragment_container, new Dashboard())
                                        .commit();
                            }
                        }).show();
            } else if (reqResponse.contains("success")) {
                new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("Data uploading")
                        .setMessage("Your data has been uploaded!")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Objects.requireNonNull(getFragmentManager())
                                        .beginTransaction()
                                        .replace(R.id.fragment_container, new Dashboard())
                                        .commit();
                            }
                        }).show();
            } else {
                new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("Data uploading")
                        .setMessage("Oups! An error happened!")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
            }
        }
    }

    public void deleteDBContent() {
        try {
            myDBHelper.deleteFromDB(patientIDRetrievied);
        } catch (Exception e) {
            e.getMessage();
        }
    }

    public void spReinitialization() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        //the questions
        String fullQuestionsList[] = getResources().getStringArray(R.array.first_questions_block);
        for (String question : fullQuestionsList) {
            editor.putBoolean(question, false);
        }

        //the meal quantity, condition and appetite...--not needed because they take a single value
        editor.commit();
    }

    public int checkIfSomethingInDB() {
        return myDBHelper.getCount(patientIDRetrievied);
    }

    public String postImage(String filepath, boolean fromDB, JSONObject parameters) throws Exception {
        String UPLOAD_SERVER = getURL(); //"http://210.146.64.139:8080/MealRecorder/save_file";
        HttpURLConnection connection;
        DataOutputStream outputStream;
        InputStream inputStream;
        //int patientID=00001;
        String dashes = "-----------------------------";
        String boundary = Long.toString(System.currentTimeMillis());
        StringBuffer response = new StringBuffer();

        //Initializing
        URL url = new URL(UPLOAD_SERVER);
        connection = (HttpURLConnection) url.openConnection();


        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setConnectTimeout(1000 * 15);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + dashes + boundary);
        //Initializing x
        outputStream = new DataOutputStream(connection.getOutputStream());

        if (!fromDB) {
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            String[] q = filepath.split("/");
            int idx = q.length - 1;
            File imageFile = new File(filepath);
            FileInputStream fileInputStream = new FileInputStream(imageFile);

            //hereYYYYYYYYYYYYYYYYYYYYYYYY

            //hereXXXXXXXXXXXXXXXXXXXXXXXXXX
            outputStream.writeBytes("--" + dashes + boundary + "\r\n");
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + "imageFile" + "\"; filename=\"" + q[idx] + "\"" + "\r\n");
            outputStream.writeBytes("Content-Type: image/jpeg" + "\r\n");
            //outputStream.writeBytes("Content-Transfer-Encoding: binary" + "\r\n");
            outputStream.writeBytes("\r\n");
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, 1048576);
            buffer = new byte[bufferSize];
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, 1048576);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }
            outputStream.writeBytes("\r\n");
            outputStream.writeBytes("--" + dashes + boundary + "\r\n");
            // outputStream.writeBytes("patientID="+String.valueOf(patientID));


            //Map<String, String> params = new HashMap<>(2);
            //params.put("patientID", "00002");
            // params.put("bar", caption);
            //JSONObject parameters = new JSONObject();
            parameters.put("Patient_ID", patientIDRetrievied);
            //retrieve the data from the shared preferences
            parameters.put("appetite", appetite);
            parameters.put("condition", condition);
            parameters.put("volume_meal", volumeMeal);
            parameters.put("free_comments", freeComments);
            parameters.put("datetime", new Timestamp(new Date().getTime()));
            for (Map.Entry<String, String> entry : questions.entrySet()) {
                parameters.put(entry.getKey(), entry.getValue());
            }

        } else {
            //get the image file path from the non empty json object
            String imageFilePath = parameters.getString("photo");


            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            String[] q = filepath.split("/");
            int idx = q.length - 1;
            File imageFile = new File(imageFilePath);
            FileInputStream fileInputStream = new FileInputStream(imageFile);
            //
            outputStream.writeBytes("--" + dashes + boundary + "\r\n");
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + "imageFile" + "\"; filename=\"" + q[idx] + "\"" + "\r\n");
            outputStream.writeBytes("Content-Type: image/jpeg" + "\r\n");
            outputStream.writeBytes("\r\n");
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, 1048576);
            buffer = new byte[bufferSize];
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, 1048576);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }
            outputStream.writeBytes("\r\n");
            outputStream.writeBytes("--" + dashes + boundary + "\r\n");
            ///

            // parameters = getDatabaseContent();
        }


        outputStream.writeBytes("Content-Disposition: form-data; name=\"parameters\"" + "\r\n");
        outputStream.writeBytes("\r\n" + parameters.toString());
        outputStream.writeBytes("\r\n" + "--" + dashes + boundary + "--" + "\r\n");
        inputStream = connection.getInputStream();
        int status = connection.getResponseCode();

        if (status == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            response.append("success");
            inputStream.close();
            connection.disconnect();
            //fileInputStream.close();
            outputStream.flush();
            outputStream.close();

        } else {
            response = new StringBuffer();
            response.append("error");
        }

        return response.toString();
    }

    //for saving in the database
    public void AddData(String xPatientId, String xPhoto, double xCondition, double xAppetite, double xVolume_meal,
                        String xFreeComments, Map<String, String> xQuestions) {
        myDBHelper.addData(
                xPatientId,
                xPhoto,
                xCondition,
                xAppetite,
                xVolume_meal,
                xFreeComments,
                xQuestions
        );
    }

    //Called when there is a valid internet connection, right before sending anything in it
    public JSONObject getDatabaseContent() {
        Cursor data = myDBHelper.getData(patientIDRetrievied);
        JSONObject myData = new JSONObject();

        while (data.moveToNext()) {
            try {
                myData.put(data.getColumnName(0), data.getString(0));
                myData.put(data.getColumnName(1), data.getString(1));
                myData.put(data.getColumnName(2), data.getString(2));
                myData.put(data.getColumnName(3), data.getString(3));
                myData.put(data.getColumnName(4), data.getString(4));
                myData.put(data.getColumnName(5), data.getString(5));
                myData.put(data.getColumnName(6), data.getString(6));
                myData.put(data.getColumnName(7), data.getString(7));
                myData.put(data.getColumnName(8), data.getString(8));
                myData.put(data.getColumnName(9), data.getString(9));
                myData.put(data.getColumnName(10), data.getString(10));
                myData.put(data.getColumnName(11), data.getString(11));
                myData.put(data.getColumnName(12), data.getString(12));
                myData.put(data.getColumnName(13), data.getString(13));
                myData.put(data.getColumnName(14), data.getString(14));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return myData;
    }

    public String getURL(){
        SharedPreferences sharedPref = getActivity().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        String desURL = sharedPref.getString("destinationURL", "no url");
        return  desURL;
    }

    public String sendDBContent(JSONObject whatToSend) throws IOException {
        StringBuffer response;
        if (whatToSend.length() > 0) {
            //call postImage with a true bool parameter
            try {
                postImage(new String(), true, whatToSend);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String UPLOAD_SERVER = "http://210.146.64.139:8080/MealRecorder/save_file";
            HttpURLConnection connection;
            DataOutputStream outputStream;


            InputStream inputStream;
            String dashes = "-----------------------------";
            String boundary = Long.toString(System.currentTimeMillis());
            URL url = new URL(UPLOAD_SERVER);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setConnectTimeout(1000 * 15);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            // connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
            //connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + dashes + boundary);
            //connection.setRequestProperty("Content-Type", "application/json; utf-8");
            //connection.setRequestProperty("Accept", "application/json");

            outputStream = new DataOutputStream(connection.getOutputStream());


            outputStream.writeBytes("Content-Disposition: form-data; name=\"parameters\"" + "\r\n");
            outputStream.writeBytes("\r\n" + whatToSend.toString());
            //byte[] input = whatToSend.toString().getBytes("utf-8");
            //outputStream.write(input, 0, input.length);
            outputStream.writeBytes("\r\n" + "--" + dashes + boundary + "--" + "\r\n");

            inputStream = connection.getInputStream();
            int status = connection.getResponseCode();

            if (status == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                response.append("success");
                inputStream.close();
                connection.disconnect();
                outputStream.flush();
                outputStream.close();

            } else {
                response = new StringBuffer();
                response.append("error");
            }
        } else {
            response = new StringBuffer();
            response.append("empty");
        }

        return response.toString();
    }
}