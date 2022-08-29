package com.example.beaconoffice;

import android.content.Context;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


/**
 * LogsFragment class creates the "Logs" page, which shows the "Measurement Results" table
 * and provides the user with the ability to send the table's data to an e-mail address of their choice.
 * @author Aikaterini - Maria Panteleaki
 * @version 1.0
 * @see SendMailAPI
 * @see LogsAdapter
 * @see LogResult
 * @since 31/8/2022
 */
public class LogsFragment extends Fragment {

    private MainActivity mainActivity;
    private HomeFragment home = new HomeFragment();
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private ArrayList<LogResult> logResultsList = new ArrayList<LogResult>();
    private EditText emailAddress;
    private TextView title;
    private Button sendButton;
    private SendMailAPI sender;
    private Context context;

    /**
     * Class constructor
     * @param context the MainActivity context
     */
    public LogsFragment(Context context) {
        this.context = context;
    }

    /**
     * Creates the basic visual layout of the application.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.logs_fragment, container, false);
        return view;
    }

    /**
     * Creates the complete visual layout of the Logs page.
     * When the user clicks the "Send" button, the application first checks if there is an internet connection.
     * If there is one, it checks for the given e-mail address's validity and then it sends the proper e-mail message.
     * @see #isValidEmail(String)
     * @see #sendEmail()
     * @see MainActivity#isInternetConnected()
     * @see MainActivity#showSnackBar(String)
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        title = view.findViewById(R.id.logs_title);
        recyclerView = view.findViewById(R.id.logs_recyclerView);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        sendButton = view.findViewById(R.id.send_button);
        emailAddress = view.findViewById(R.id.textEmailAddress);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mainActivity.isInternetConnected()) {
                    mainActivity.showSnackBar("You are offline. Please connect to the Internet");
                    emailAddress.onEditorAction(EditorInfo.IME_ACTION_DONE);
                } else {
                    if (isValidEmail(emailAddress.getText().toString())) {
                        emailAddress.onEditorAction(EditorInfo.IME_ACTION_DONE);
                        sendEmail();
                        mainActivity.showSnackBar("Sending email...");
                    } else {
                        mainActivity.showSnackBar("Please give a valid e-mail address");
                    }
                    emailAddress.getText().clear();
                }
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
    }

    /**
     * Sets up the e-mail message content and sends it to the given e-mail address.
     * The title will consist of the phrase "BeacOnOffice Log Results at: " and the timestamp
     * of the time that the message was sent. The recipient is given by the user as text input
     * and the body is created in HomeFragment class.
     *
     * @author Aikaterini - Maria Panteleaki
     * @see HomeFragment#getEmailBody()
     * @see SendMailAPI
     * @since 5/8/2022
     */
    public void sendEmail() {
        Date currentTime = Calendar.getInstance().getTime();
        String subject = "BeacOnOffice Log Results at: "+ currentTime;
        String message = home.getEmailBody();
        String to = emailAddress.getText().toString();

        sender = new SendMailAPI(context, to, subject, message);
        sender.execute();
    }

    /**
     * Checks if the given String object represents a valid e-mail address.
     * @param email the text input given by the user
     * @return true if the email string is valid e-mail address
     *              otherwise it returns false
     */
    public boolean isValidEmail(String email) {
        return (email != null) && (Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    /**
     * Fills the content of "Measurement Results" table with the adapter data.
     * @param adapter LogsAdapter object that holds the data which need to be
     *                shown to the user.
     * @see MainActivity#addLogList(LogResult)
     * @see MainActivity#clearLogsAdapter()
     */
    public void setAdapter(LogsAdapter adapter) {
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(layoutManager.findLastVisibleItemPosition());
    }
}
