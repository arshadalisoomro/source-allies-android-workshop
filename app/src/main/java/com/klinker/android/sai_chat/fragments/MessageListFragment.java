package com.klinker.android.sai_chat.fragments;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.klinker.android.sai_chat.R;
import com.klinker.android.sai_chat.adapters.MessageArrayAdapter;
import com.klinker.android.sai_chat.data.DatabaseHelper;
import com.klinker.android.sai_chat.data.Message;
import com.klinker.android.sai_chat.data.sql.MessageDataSource;
import com.klinker.android.sai_chat.utils.RegistrationUtils;
import com.klinker.android.sai_chat.utils.api.Sender;

import java.util.ArrayList;
import java.util.List;

/**
 * This fragment holds the message list and the reply bar for a threaded conversation.
 *
 * What to learn:
 *      - Using a bundle to pass information from an activity to a fragment as arguments
 */
public class MessageListFragment extends Fragment {

    /**
     * TODO:
     *
     *      1.) inflate the fragment_message_list layout and return it from the onCreateView(...)
     *          - Similar to how we have done it in the adapters
     *          - Find and set the listView, replyBar, and sendButton views (View.findViewById(int))
     *      2.) Implement the sendButton.onClickListener(...) and call the sendMessage(String) function
     *          - grab the text to send with EditText.getText().toString()
     *          - set the text of the reply bar back to blank
     *      3.) Create a MessageArrayAdapter object and apply it to the list view
     */

    public static final String EXTRA_THREAD_ID = "thread_id";
    public static final String EXTRA_CONVO_NAME = "convo_name";

    /*
        Here, we create the bundle and add the data to it, then
        end that bundle along with the fragment when it is created.

        You NEVER want to use a constructor with arguements for something like
        this on a fragment. This is because when a savedInstanceState of the fragment is restored
        (which can be done after changing orientation, or when the activity is kicked out of memory for
        whatever reason and the fragment isn't), it will call the default constructor and your
        fragment won't contain the data that you needed to send with it.
     */
    public static MessageListFragment getInstance(long threadId) {
        Bundle b = new Bundle();
        b.putLong(EXTRA_THREAD_ID, threadId);

        MessageListFragment fragment = new MessageListFragment();
        fragment.setArguments(b);
        return fragment;
    }

    private BroadcastReceiver sentBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            new GetMessages().execute();
        }
    };

    private Sender sender;
    private RegistrationUtils registrationUtils;

    private long threadId;

    private ListView listView;
    private EditText replyBar;
    private ImageButton sendButton;

    // We actually need to make a layout for this fragment, so we override this method and return
    // the view containing our inflated layout
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        sender = new Sender(getActivity());
        registrationUtils = new RegistrationUtils();


        // TODO #1

        // inflate the view and set the container ViewGroup as the parent
        View v = inflater.inflate(R.layout.fragment_message_list, container, false);

        // find the views from the inflated layout
        listView = (ListView) v.findViewById(R.id.listview);
        replyBar = (EditText) v.findViewById(R.id.reply_text);
        sendButton = (ImageButton) v.findViewById(R.id.send_button);


        // get the arguements and start loading the data and filling the list
        threadId = getArguments().getLong(EXTRA_THREAD_ID);
        new GetMessages().execute();


        // TODO #2

        // set the functionality of the send button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(replyBar.getText().toString());
                replyBar.setText("");
            }
        });

        // return the view that we inflated
        return v;
    }

    // Same idea as the ConversationFragment with the onResume and onPause methods
    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Sender.SENT_BROADCAST);

        getActivity().registerReceiver(sentBroadcastReceiver, filter);

        new GetMessages().execute();
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(sentBroadcastReceiver);
        
        super.onPause();
    }

    // uses the sender object to send a new message in the current thread.
    // when the sending is complete, it will broadcast the Sender.SENT_BROADCAST,
    // which is picked up by our receiver to update the list.
    private void sendMessage(String message) {
        sender.sendThreadedMessage(
                threadId,
                registrationUtils.getMyUserId(getActivity()),
                message
        );
    }

    private void setMessageAdapter(List<Message> messages) {

        // TODO #3

        MessageArrayAdapter adapter = new MessageArrayAdapter(getActivity(), messages);
        listView.setAdapter(adapter);
    }

    /*
        Another subclassed AsyncTask to load our data. Same idea as on the Conversation fragment.
     */
    class GetMessages extends AsyncTask<Void, Void, List<Message>> {
        @Override
        protected List<Message> doInBackground(Void... arg0) {
            return new DatabaseHelper(getActivity()).findThreadMessages(threadId);
        }

        @Override
        protected void onPostExecute(List<Message> result) {
            setMessageAdapter(result);
        }
    }
}
