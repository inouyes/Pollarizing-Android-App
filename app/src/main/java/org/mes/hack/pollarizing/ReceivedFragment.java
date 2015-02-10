package org.mes.hack.pollarizing;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReceivedFragment extends Fragment {
    private static final String TAG = "ReceivedFragment";
    ListView sfav;
    List<PollObject> friends;

    public ReceivedFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_received, container, false);
        sfav = (ListView) v.findViewById(R.id.sfav);
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        queryUpdates();
    }

    public void queryUpdates(){
        new AsyncTask<Void, Void, ListAdapter>(){
            @Override
            protected ListAdapter doInBackground(Void... params) {
                HttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(Constants.DOMAIN + "/updates/" +
                        PreferenceManager.getDefaultSharedPreferences(ReceivedFragment.this.getActivity()).getString(Constants.GOOGLE_PLUS_ID, "null"));
                try {
                    HttpResponse response = client.execute(get);
                    String responseJson = convertStreamToString(response.getEntity().getContent());
                    Log.d(TAG, "Response: " + responseJson);
                    Gson gson = new Gson();
                    PollObject[] po = gson.fromJson(responseJson, PollObject[].class);
                    Log.d(TAG, "poll object cast");

                    List<PollObject> pos = new ArrayList<PollObject>();
                    //pos.addAll(Arrays.asList(po));
                    if(pos.size() == 0){
                        List<PollObject.Response> responses = new ArrayList<PollObject.Response>();
                        pos.add(new PollObject("null", "null", "Test", responses));
                    }
                    return new JudgementAdapter(ReceivedFragment.this.getActivity(), R.layout.big_view, pos);
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(ListAdapter la){
                if(la != null){
                    sfav.setAdapter(la);
                }
            }
        }.execute();
    }

    //The internet! Hooray!
    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public class JudgementAdapter extends ArrayAdapter<PollObject> {
        public JudgementAdapter(Context context, int resource) {
            super(context, resource);
        }

        public JudgementAdapter(Context context, int resource, PollObject[] objects) {
            super(context, resource, objects);
        }

        public JudgementAdapter(Context context, int resource, List<PollObject> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d(TAG, "Get view");

            View v = convertView;
            if(v == null){
                v = LayoutInflater.from(getContext()).inflate(R.layout.big_view, null);
            }

            ImageView bigView = (ImageView) v.findViewById(R.id.bigImage);
            TextView caption = (TextView) v.findViewById(R.id.caption);

            PollObject po = getItem(position);

            if(!po.getCaption().equals("null")){
                caption.setText(po.getCaption());
            }
            //Picasso.with(getContext()).load(Constants.DOMAIN + "/img/" +
              //      po.getPoll_id()).into(bigView);
            return v;
        }
    }
}
