package im.bzh.service.impl;

import im.bzh.dto.MailDTO;
import im.bzh.entity.Envelope;
import im.bzh.entity.Mail;
import im.bzh.service.MailService;
import im.bzh.service.UserService;
import im.bzh.utils.Shell;
import im.bzh.vo.DraftVO;
import org.apache.tomcat.util.http.fileupload.util.mime.MimeUtility;
import org.simplejavamail.api.email.AttachmentResource;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.email.Recipient;
import org.simplejavamail.api.mailer.AsyncResponse;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.converter.EmailConverter;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.activation.FileDataSource;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class MailServiceImpl implements MailService {

    @Value("${mail.domain}")
    private String domain;

    @Value("${mail.host}")
    private String host;

    @Value("${mail.file-upload-path}")
    private String uploadPath;

    @Autowired
    private UserService userService;

    @Override
    public String formatFolderName(String folder) {
        folder = folder.substring(0, 1).toUpperCase() + folder.substring(1).toLowerCase();
        return folder.equals("Inbox") ? "INBOX" : folder;
    }

    @Override
    public List<Envelope> getEnvelopeList(String username, String folder) throws Exception {
        folder = formatFolderName(folder);
        List<Envelope> result = new ArrayList<>();
        String envelopes = Shell.exec("maddy imap-msgs list " + username + "@" + domain + " " + folder, null);
        if ("".equals(envelopes)) {
            return null;
        }
        String[] rows = envelopes.split("\n");
        for (int i = 0; i < rows.length; i += 3) {
            String[] split = null;
            int pos_1 = rows[i].indexOf(" ");
            int pos_2 = rows[i].indexOf(":");
            int pos_3 = rows[i].indexOf("<");
            int pos_4 = rows[i].indexOf(">");
            int pos_5 = rows[i].lastIndexOf(" - ");
            long uid = Long.parseLong(rows[i].substring(pos_1, pos_2).trim());
            String fromName = MimeUtility.decodeText(rows[i].substring(pos_2 + 1, pos_3).trim());
            String fromAddress = MimeUtility.decodeText(rows[i].substring(pos_3, pos_4 + 1));
            String subject = MimeUtility.decodeText(rows[i].substring(pos_5 + 3));
            int status = 0;
            if (rows[i + 1].indexOf("]") - rows[i + 1].indexOf("[") != 1) {
                split = rows[i + 1].substring(rows[i + 1].indexOf("\\"), rows[i + 1].indexOf("]")).split(" ");
                for (String s : split) {
                    if ("\\Seen".equals(s)) {
                        status = 1;
                        break;
                    }
                }
            }
            split = rows[i + 1].substring(rows[i + 1].indexOf(",")).split(" ");
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-ddHH:mm:ssZ");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            Date d = sdf1.parse(split[1] + split[2] + split[3]);
            String date = sdf2.format(d);
            long timestamp = d.getTime() / 1000;
            Envelope e = new Envelope(uid, fromName, fromAddress, subject, status, date, timestamp);
            result.add(e);
        }
        return result;
    }

    @Override
    public boolean markMailAsRead(String username, String folder, Integer[] ids) throws Exception {
        folder = formatFolderName(folder);
        String temp = Arrays.toString(ids);
        String idsStr = temp.substring(1, temp.length() - 1).replace(" ", "");
        String cmd = "maddy imap-msgs set-flags --uid " + username + "@" + domain + " " + folder + " " + idsStr + " \\\\Seen";
        Shell.exec(cmd, null);
        return true;
    }

    @Override
    public boolean markMailAsUnread(String username, String folder, Integer[] ids) throws Exception {
        folder = formatFolderName(folder);
        String temp = Arrays.toString(ids);
        String uidsStr = temp.substring(1, temp.length() - 1).replace(" ", "");
        String cmd = "maddy imap-msgs rem-flags --uid " + username + "@" + domain + " " + folder + " " + uidsStr + " \\\\Seen";
        Shell.exec(cmd, null);
        return true;
    }

    @Override
    public boolean deleteMail(String username, String folder, Integer[] ids, Boolean permanent) throws Exception {
        folder = formatFolderName(folder);
        String temp = Arrays.toString(ids);
        String idsStr = temp.substring(1, temp.length() - 1).replace(" ", "");

        String cmd = null;

        if (permanent) {
            cmd = "maddy imap-msgs remove --uid " + username + "@" + domain + " " + folder + " " + idsStr;
            Shell.exec(cmd, "y");
        } else {
            cmd = "maddy imap-msgs move --uid " + username + "@" + domain + " " + folder + " " + idsStr + " Trash";
            Shell.exec(cmd, null);
        }

        return true;
    }

    @Override
    public boolean moveMail(String username, String folder, Integer[] ids, String destination) throws Exception {
        folder = formatFolderName(folder);
        destination = formatFolderName(destination);
        String temp = Arrays.toString(ids);
        String uidsStr = temp.substring(1, temp.length() - 1).replace(" ", "");
        String cmd = "maddy imap-msgs move --uid " + username + "@" + domain + " " + folder + " " + uidsStr + " " + destination;
        Shell.exec(cmd, null);
        return true;
    }

    @Override
    public Mail getMail(String username, String folder, Long id) throws Exception {
        folder = formatFolderName(folder);
        String cmd = "maddy imap-msgs dump --uid " + username + "@" + domain + " " + folder + " " + id;
        String mailStr = Shell.exec(cmd, null);
        javax.mail.Session session = javax.mail.Session.getDefaultInstance(new Properties());
        MimeMessage message = new MimeMessage(session, new ByteArrayInputStream(mailStr.getBytes()));

        List<String> attachments = new ArrayList<>();

        Email email = EmailConverter.mimeMessageToEmail(message);
        String subject = email.getSubject();
        String content = email.getHTMLText();
        if (content == null) {
            content = email.getPlainText();
        }
        String fromName = email.getFromRecipient().getName();
        fromName = fromName == null ? "" : fromName;
        String fromAddress = "<" + email.getFromRecipient().getAddress() + ">";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        String date = sdf.format(message.getSentDate());

        for (AttachmentResource attachment : email.getAttachments()) {
            attachments.add(attachment.getName());
        }

        for (AttachmentResource attachment : email.getEmbeddedImages()) {
            attachments.add(attachment.getName());
        }

        return new Mail(id, subject, content, fromName, fromAddress, date,  attachments.toArray(new String[0]));
    }

    @Override
    public boolean sendMail(MailDTO mailDTO) {
        try {
            String[] address = mailDTO.getAddress();
            List<Recipient> recipients = new ArrayList<>();
            for (String addr : address) {
                String nickname = addr.substring(0, addr.indexOf("@"));
                recipients.add(new Recipient(nickname, addr, Message.RecipientType.TO));
            }

            Email email = null;

            if (mailDTO.getAttachments() == null) {
                email = EmailBuilder.startingBlank()
                        .from(mailDTO.getNickname(), mailDTO.getUsername() + "@" + domain)
                        .to(recipients)
                        .withSubject(mailDTO.getSubject())
                        .withHTMLText(mailDTO.getContent())
                        .buildEmail();
            } else {
                File file = new File(uploadPath + mailDTO.getAttachments());
                email = EmailBuilder.startingBlank()
                        .from(mailDTO.getNickname(), mailDTO.getUsername() + "@" + domain)
                        .to(recipients)
                        .withSubject(mailDTO.getSubject())
                        .withHTMLText(mailDTO.getContent())
                        .withAttachment(mailDTO.getOriginalAttachments(), new FileDataSource(file))
                        .buildEmail();
            }

            String password = userService.getUserByUsername(mailDTO.getUsername()).getPassword();

            String rawMail = EmailConverter.emailToEML(email);
            String cmd = "maddy imap-msgs add " + mailDTO.getUsername() + "@" + domain +" Sent";
            String id = Shell.exec(cmd, rawMail).trim();
            markMailAsRead(mailDTO.getUsername(), "Sent", new Integer[]{Integer.parseInt(id)});

            AsyncResponse asyncResponse = MailerBuilder
                    .withSMTPServer(host, 465, mailDTO.getUsername() + "@" + domain, password)
                    .withTransportStrategy(TransportStrategy.SMTPS)
                    .buildMailer()
                    .sendMail(email);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean saveMailToDrafts(MailDTO mailDTO) throws Exception {
        String[] address = mailDTO.getAddress();
        List<Recipient> recipients = new ArrayList<>();
        for (String addr : address) {
            String nickname = addr.substring(0, addr.indexOf("@"));
            recipients.add(new Recipient(nickname, addr, Message.RecipientType.TO));
        }

        Email email = EmailBuilder.startingBlank()
                .from(mailDTO.getNickname(), mailDTO.getUsername() + "@" + domain)
                .to(recipients)
                .withSubject(mailDTO.getSubject())
                .withHTMLText(mailDTO.getContent())
                .buildEmail();

        String rawMail = EmailConverter.emailToEML(email);
        String cmd = "maddy imap-msgs add " + mailDTO.getUsername() + "@" + domain + " Drafts";
        Shell.exec(cmd, rawMail);
        return true;
    }

    @Override
    public DraftVO getDraftVO(String username, String folder, Long id) throws Exception {
        folder = formatFolderName(folder);
        String cmd = "maddy imap-msgs dump --uid " + username + "@" + domain + " " + folder + " " + id;
        String mailStr = Shell.exec(cmd, null);
        javax.mail.Session session = javax.mail.Session.getDefaultInstance(new Properties());
        MimeMessage message = new MimeMessage(session, new ByteArrayInputStream(mailStr.getBytes()));
        Email email = EmailConverter.mimeMessageToEmail(message);
        String subject = email.getSubject();
        List<Recipient> recipients = email.getRecipients();
        String content = email.getHTMLText();
        if (content == null) {
            content = email.getPlainText();
        }
        String[] address = new String[recipients.size()];
        int i = 0;
        for (Recipient recipient : recipients) {
            address[i++] = recipient.getAddress();
        }
        return new DraftVO(subject, address, content);
    }


}
