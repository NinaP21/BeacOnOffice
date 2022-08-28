package com.example.beaconoffice;

import android.content.Context;
import android.os.AsyncTask;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * SendMailAPI class manages to send an e-mail message on a background thread,
 * so that the BeacOnOffice application will continue to run without waiting for the mail to be sent.
 * The sender of the e-mail will always be "beaconoffice.diasemi@gmail.com" whereas the recipient,
 * the subject and the body are given from the sendEmail() function in LogsFragment class.
 *
 * @author Aikaterini - Maria Panteleaki
 * @version 1.0
 * @see LogsFragment#sendEmail()
 * @since 31/8/2022
 */
public class SendMailAPI extends AsyncTask<Void, Void, Void> {
    private static final String fromEmail = "beaconoffice.diasemi@gmail.com";
    private static final String password = "iebqdliupmwnjcpy"; // WP Mail SMTP password

    private Context context;
    private String toEmail, subject, message;
    Session session;

    public SendMailAPI(Context context, String toEmail, String subject, String message) {
        this.context = context;
        this.toEmail = toEmail;
        this.subject = subject;
        this.message = message;
    }

    /**
     * Assigns the e-mail sending to a worker thread, because this procedure may take several seconds.
     * First,it uses the SMTP protocol to log in to beaconoffice.diasemi@gmail.com and then
     * it constructs the message that will be sent to the given e-mail address.
     * @see LogsFragment#sendEmail()
     */
    @Override
    protected Void doInBackground(Void... voids) {

        Properties properties = new Properties();

        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.starttls.enable","true");
        properties.put("mail.smtp.debug", "true");
        properties.put("mail.smtp.socketFactory.port", "587");
        properties.put("mail.smtp.socketFactory.fallback", "false");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.user", fromEmail);

        session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        MimeMessage mimeMessage = new MimeMessage(session);
        try {
            mimeMessage.setFrom(new InternetAddress(fromEmail));
            mimeMessage.setRecipients(Message.RecipientType.TO, String.valueOf(new InternetAddress(toEmail)));
            mimeMessage.setSubject(subject);
            mimeMessage.setText(message);

            Transport transport = session.getTransport("smtp");
            transport.connect("smtp.gmail.com", 587, fromEmail, password);
            transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
            transport.close();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
