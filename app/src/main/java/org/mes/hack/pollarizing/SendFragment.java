package org.mes.hack.pollarizing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SendFragment extends Fragment {
    private static final String TAG = "SendFragment";
    GoogleApiClient client;
    ImageButton image;
    EditText caption;
    ListView sendView;
    Button send;

    public static int REQUEST_CAPTURE = 200;
    String file = "";

    public SendFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.fragment_send, container, false);
        image = (ImageButton) vg.findViewById(R.id.imageButton);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(SendFragment.this.getActivity().getPackageManager()) != null) {
                    File dir = SendFragment.this.getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    if(dir != null) {
                        try {
                            File image = File.createTempFile(String.valueOf(System.currentTimeMillis()), ".jpg", dir);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
                            startActivityForResult(takePictureIntent, REQUEST_CAPTURE);

                            file = image.getPath();
                        } catch (Exception e) {
                        }
                    }
                }
            }
        });
        caption = (EditText) vg.findViewById(R.id.captionInput);
        sendView = (ListView) vg.findViewById(R.id.friends);
        send = (Button) vg.findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //(1) Grab the parts
                if(sendView.getAdapter() != null){
                    new AsyncTask<Void, Void, String>(){
                        @Override
                        protected String doInBackground(Void... params) {
                            List<String> ids = ((PersonAdapter)sendView.getAdapter()).getSelectedIds();
                            if(ids.size() == 0){
                                return "Please selected a contact.";
                            } else {
                                Log.d(TAG, ((PersonAdapter)sendView.getAdapter()).getCheckedIds().toString());
                                for(Integer i : ((PersonAdapter)sendView.getAdapter()).getCheckedIds()){
                                    Log.d(TAG, ((PersonAdapter)sendView.getAdapter()).getItem(i).getDisplayName());
                                }
                            }

                            if(file.isEmpty()){
                                return "Please selected an image.";
                            }

                            Log.d(TAG, "File: " + file);

                            //We have an image and ID; all the required components are filled
                            String text = caption.getText().toString();
                            if(text == null || text.isEmpty()){
                                text = "null";
                            }
                            Log.d(TAG, "Text: " + text);

                            String myId = PreferenceManager
                                    .getDefaultSharedPreferences(SendFragment.this.getActivity()).getString(Constants.GOOGLE_PLUS_ID, "null");
                            if(myId.equals("null")){
                                return "Google+ credentials invalid.";
                            }

                            String rIds = "";
                            for(String id : ids){
                                rIds += id + ",";
                            }
                            rIds = rIds.substring(0, rIds.length() - 1);
                            Log.d(TAG, "rIds: " + rIds);

                            Bitmap bitmap = BitmapFactory.decodeFile(file);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] b = baos.toByteArray();
                            String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);

                            Log.d(TAG, "Image is encoded");

                            //We have all we need!
                            try {
                                HttpClient http = new DefaultHttpClient();
                                HttpPost httpPost = new HttpPost();

                                HttpClient httpClient = new DefaultHttpClient();
                                HttpPost post = new HttpPost(Constants.DOMAIN + "/send");
                                post.setEntity(new StringEntity(encodedImage));

                                HttpEntity fe = new FileEntity(new File(file), "image/jpeg");
                                post.setEntity(fe);

                                post.addHeader(new BasicHeader("user_id", myId));
                                post.addHeader(new BasicHeader("recipient_id", rIds));
                                post.addHeader(new BasicHeader("caption", text));
                                HttpResponse response = httpClient.execute(post);
                                Log.d(TAG, "Completed request");
                                Log.d(TAG, "Result: " + response.getStatusLine().getStatusCode());
                                return "POLL'd those users.";
                            } catch (Exception e) {
                                return e.toString();
                            }
                        }

                        @Override
                        protected void onPostExecute(String error){
                            Toast.makeText(SendFragment.this.getActivity(), error, Toast.LENGTH_SHORT).show();
                        }
                    }.execute();
                }
            }
        });
        return vg;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "On Activity Result");
        if (requestCode == SendFragment.REQUEST_CAPTURE && resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "Result found");
            setPic();
        } else {
            file = "";
        }
    }

    //From the Google tutorial
    private void setPic() {
        try {
            // Get the dimensions of the View
            int targetW = image.getWidth();
            int targetH = image.getHeight();

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeFile(file, bmOptions);
            image.setImageBitmap(bitmap);
        } catch (Exception e) {

        }
    }

    @Override
    public void onAttach(Activity activity) {
        client = new GoogleApiClient.Builder(activity)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
        client.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                //Query for friends
                friendsQuery();
            }

            @Override
            public void onConnectionSuspended(int i) {
                //Check YO INTERNET
                Toast.makeText(SendFragment.this.getActivity(), "Check your interent connectivity!", Toast.LENGTH_SHORT).show();
            }
        });
        client.connect();
        Log.d(TAG, "Registered callbacks");
        super.onAttach(activity);
    }

    public void friendsQuery(){
        new AsyncTask<Void, Void, ListAdapter>(){
            @Override
            protected ListAdapter doInBackground(Void... params) {
                try {
                    Log.d(TAG, "Executing friends query...");
                    People.LoadPeopleResult result = Plus.PeopleApi.loadVisible(client, "").await();
                    List<Person> people = new ArrayList<>();
                    Log.d(TAG, people.size() + " found");
                    for (Person p : result.getPersonBuffer()) {
                        if(p.getObjectType() == Person.ObjectType.PERSON) people.add(p);
                    }
                    return new PersonAdapter(SendFragment.this.getActivity(),
                            R.layout.person_item, people);
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(ListAdapter result){
                if(result != null) sendView.setAdapter(result);
            }
        }.execute();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public class PersonAdapter extends ArrayAdapter<Person> {
        List<Integer> checkedIds = new ArrayList<>();

        public PersonAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if(v == null){
                v = LayoutInflater.from(this.getContext()).inflate(R.layout.person_item, null);
            }

            final CheckBox checked = (CheckBox) v.findViewById(R.id.personCheckbox);
            if(checkedIds.contains(Integer.valueOf(position))){
                checked.setChecked(true);
            } else {
                checked.setChecked(false);
            }
            ImageView iv = (ImageView) v.findViewById(R.id.personPicture);
            TextView name = (TextView) v.findViewById(R.id.personName);

            checked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        checkedIds.add(Integer.valueOf(position));
                    } else {
                        checkedIds.remove(Integer.valueOf(position));
                    }
                }
            });
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checked.setChecked(!checked.isChecked());
                }
            });

            Picasso.with(getContext()).load(getItem(position).getImage().getUrl()).into(iv);
            name.setText(getItem(position).getDisplayName());
            return v;
        }

        public List<String> getSelectedIds(){
            List<String> ids = new ArrayList<>();
            for(Integer i : checkedIds){
                ids.add(getItem(i).getDisplayName().replace(" ", ""));
            }
            return ids;
        }

        public List<Integer> getCheckedIds(){
            return checkedIds;
        }

        public PersonAdapter(Context context, int resource, int textViewResourceId) {
            super(context, resource, textViewResourceId);
        }

        public PersonAdapter(Context context, int resource, Person[] objects) {
            super(context, resource, objects);
        }

        public PersonAdapter(Context context, int resource, int textViewResourceId, Person[] objects) {
            super(context, resource, textViewResourceId, objects);
        }

        public PersonAdapter(Context context, int resource, List<Person> objects) {
            super(context, resource, objects);
        }

        public PersonAdapter(Context context, int resource, int textViewResourceId, List<Person> objects) {
            super(context, resource, textViewResourceId, objects);
        }
    }
}
