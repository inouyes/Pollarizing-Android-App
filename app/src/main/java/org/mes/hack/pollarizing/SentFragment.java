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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class SentFragment extends Fragment {
    private static final String TAG = "SentFragment";
    ListView list;

    public SentFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_sent, container, false);
        list = (ListView) v.findViewById(R.id.list);
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
                HttpGet get = new HttpGet(Constants.DOMAIN + "/my_polls/" +
                        PreferenceManager.getDefaultSharedPreferences(SentFragment.this.getActivity()).getString(Constants.GOOGLE_PLUS_ID, "null"));
                try {
                    HttpResponse response = client.execute(get);
                    String responseJson = convertStreamToString(response.getEntity().getContent());
                    Log.d(TAG, "Response: " + responseJson);
                    Gson gson = new Gson();
                    PollObject[] po = gson.fromJson(responseJson, PollObject[].class);
                    Log.d(TAG, "poll object cast");

                    List<PollObject> pos = new ArrayList<PollObject>();
                    pos.addAll(Arrays.asList(po));
                    return new ReceivedAdapter(SentFragment.this.getActivity(), R.layout.sent_item, pos);
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(ListAdapter la){
                if(la != null) list.setAdapter(la);
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

    public class ReceivedAdapter extends ArrayAdapter<PollObject> {
        public ReceivedAdapter(Context context, int resource, List<PollObject> objects) {
            super(context, resource, objects);
        }

        public ReceivedAdapter(Context context, int resource, int textViewResourceId) {
            super(context, resource, textViewResourceId);
        }

        public ReceivedAdapter(Context context, int resource) {
            super(context, resource);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if(convertView == null){
                v = LayoutInflater.from(getContext()).inflate(R.layout.sent_item, null);
            }

            final ImageView iv = (ImageView) v.findViewById(R.id.recvImage);
            TextView percentUp = (TextView) v.findViewById(R.id.upPercent);
            TextView percentDown = (TextView) v.findViewById(R.id.downPercent);
            LinearLayout layout = (LinearLayout) v.findViewById(R.id.commentsBox);
            layout.removeAllViews();

            PollObject po = getItem(position);
            percentUp.setText(String.valueOf(po.getPerecentYes()));
            percentDown.setText(String.valueOf(po.getPercentDown()));

            for(PollObject.Response response : po.getResponses()){
                if(response.getResult() == PollObject.COMMENT){
                    TextView tv = new TextView(getContext());
                    tv.setText(response.getComment());
                    layout.addView(tv);
                }
            }

            Picasso.with(getContext()).load(Constants.DOMAIN + "/img/" +
                    po.getPoll_id()).into(iv);
            return v;
        }
    }
}
