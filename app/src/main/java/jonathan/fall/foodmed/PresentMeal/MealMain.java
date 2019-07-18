package jonathan.fall.foodmed.PresentMeal;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

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

    private ProgressDialog progress;
    private AlertDialog alert;


    /**/
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_present_meal, container, false);
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

        previous = v.findViewById(R.id.PreviousBFinal);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Objects.requireNonNull(getFragmentManager())
                        .beginTransaction()
                        .replace(R.id.fragment_container, new ConditionStatement())
                        .commit();
            }
        });

        submit = v.findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageSender start_task = new imageSender();
                start_task.execute();
                if (!start_task.isCancelled()) {
                    new AlertDialog.Builder(getActivity())
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setTitle("Image upload")
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
                }
                else{
                    new AlertDialog.Builder(getActivity())
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setTitle("Image upload")
                            .setMessage("Your data has NOT been uploaded!")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                }
            }
        });

        return v;
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
            Bitmap myBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            myThumbnail.setImageBitmap(myBitmap);

        }
    }


    public class imageSender extends AsyncTask<Void, Void, Void> {
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
           // String response;
            try {
                //response = postImage(realPath);
                progress.dismiss();

            } catch (Exception e) {
                //response = e.getMessage();
                progress.dismiss();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
          /*  if (response.contains("success")) {
                view_status.setTextColor(Color.parseColor("#21c627"));
            }
            view_status.setText(response);
            uploadButton.getBackground().setColorFilter(0xffd6d7d7, PorterDuff.Mode.MULTIPLY);*/
        }
    }


    public String postImage(String filepath) throws Exception {
        String UPLOAD_SERVER = "http://210.146.64.139:8080/MealRecorder/save_file";
        HttpURLConnection connection;
        DataOutputStream outputStream;
        InputStream inputStream;
        String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        String[] q = filepath.split("/");
        int idx = q.length - 1;
        File imageFile = new File(filepath);
        FileInputStream fileInputStream = new FileInputStream(imageFile);
        URL url = new URL(UPLOAD_SERVER);
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setConnectTimeout(1000 * 15);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.writeBytes("--" + boundary + "\r\n");
        outputStream.writeBytes("Content-Disposition: form-data; name=\"" + "imageFile" + "\"; filename=\"" + q[idx] + "\"" + "\r\n");
        outputStream.writeBytes("Content-Type: image/jpeg" + "\r\n");
        outputStream.writeBytes("Content-Transfer-Encoding: binary" + "\r\n");
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
        outputStream.writeBytes("--" + boundary + "--" + "\r\n");
        inputStream = connection.getInputStream();
        int status = connection.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            inputStream.close();
            connection.disconnect();
            fileInputStream.close();
            outputStream.flush();
            outputStream.close();

            return response.toString();
        } else {
            throw new Exception("Non ok response returned");
        }
    }


}