package com.klinker.android.sai_chat.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;

import com.klinker.android.sai_chat.R;
import com.klinker.android.sai_chat.data.DatabaseHelper;
import com.klinker.android.sai_chat.data.User;
import com.klinker.android.sai_chat.utils.RegistrationUtils;
import com.klinker.android.sai_chat.utils.api.Sender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This activity is used to start a new thread with a user.
 *
 * What to check out:
 *      - AutoCompleteTextView is a really cool widget. You can define some kind of list adapter
 *           for it and it will automatically create an autocomplete from that list as the user types.
 *           It works great for simple string lists, but can get pretty complex when defining your own
 *           custom auto complete classes.
 */
public class NewMessageActivity extends AbstractToolbarActivity {

    /**
     * TODO:
     *
     *      1.) Fill in the activity_new_message.xml layout with the 3 views:
     *          - AutoCompleteTextView, EditText, and ImageButton
     *      2.) Find the userAutoComplete, messageText, and sendButton views
     *      3.) Set the onClickListener for the sendButton
     *      4.) Set up the auto complete for the userAutoComplete view
     */

    private AutoCompleteTextView userAutoComplete;
    private EditText messageText;
    private ImageButton sendButton;

    private User sendTo = null;

    private Map<String, User> userNameMap = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO #1

        setContentView(R.layout.activity_new_message);


        // TODO #2

        // find our views
        userAutoComplete = (AutoCompleteTextView) findViewById(R.id.user_auto_complete);
        messageText = (EditText) findViewById(R.id.message_text);
        sendButton = (ImageButton) findViewById(R.id.send_button);

        setUpAutoComplete();


        // TODO #3

        // set a click listener on the send button to send the message to the user.
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // since I said things got more complex with custom classes on the auto complete,
                // we can simply map the string list we use to the user objects in our database
                sendTo = userNameMap.get(userAutoComplete.getText().toString());

                if (sendTo != null) {
                    // used to get the current users id
                    RegistrationUtils registrationUtils = new RegistrationUtils();

                    // send the message in the background thread.
                    // after the message is successfully sent, the Sender will send a broadcast for the fragments to update
                    // with the action Sender.SEMT_BROADCAST.
                    // we should listen for this broadcast on our fragments and update when necessary.
                    Sender sender = new Sender(NewMessageActivity.this);
                    sender.sendNewMessage(sendTo.getUserId(), registrationUtils.getMyUserId(NewMessageActivity.this), messageText.getText().toString());

                    // we don't want to keep the user waiting on the new message screen, so finish this
                    // activity and kick them back to the conversation list.
                    finish();
                }
            }
        });
    }

    private void setUpAutoComplete() {

        // TODO #4
        // Hints: getUserList() and getAutoCompleteList() are provided. Get the lists, then
        // create and set the adapter to the userAutoComplete view
        // Creating generic array adapter: https://github.com/codepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView

        final List<User> users = getUserList();
        final List<String> autoCompleteList = getAutoCompleteList(users);

        // create a generic string adapter. We dont even have to write this,
        // android has the most basic ones build in. Phew.
        ArrayAdapter<String> adapter = new ArrayAdapter<> (
                this,
                android.R.layout.select_dialog_item,
                autoCompleteList
        );

        userAutoComplete.setThreshold(1);
        userAutoComplete.setAdapter(adapter);
    }

    private List<User> getUserList() {
        return new DatabaseHelper(this).findAllUsers();
    }

    // returns a string list for the auto complete and maps those strings in a HashMap
    // so that we can use them to find the User objects to send the message to.
    private List<String> getAutoCompleteList(List<User> users) {
        List<String> strings = new ArrayList<>();

        for (User u : users) {
            String text = u.getRealName() + " (" + u.getUsername() + ")";
            strings.add(text);

            userNameMap.put(text, u);
        }

        return strings;
    }
}
