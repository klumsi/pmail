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
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.converter.EmailConverter;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
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
            long id = Long.parseLong(rows[i].substring(pos_1, pos_2).trim());
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
            Envelope e = new Envelope(id, fromName, fromAddress, subject, status, date, timestamp);
            result.add(e);
        }
        return result;
    }

    @Override
    public List<Envelope> getFullEnvelopeList(String username, String folder) throws Exception {
        folder = formatFolderName(folder);
        List<Envelope> result = new ArrayList<>();
        String envelopes = Shell.exec("maddy imap-msgs list -f " + username + "@" + domain + " " + folder, null);
        if ("".equals(envelopes)) {
            return null;
        }
        String[] rows = envelopes.split("\n");
        for (int i = 0; i < rows.length; i += 13) {
            long id = Long.parseLong(rows[i + 1].substring(4).trim());
            String subject = MimeUtility.decodeText(rows[i + 11].substring(8).trim());
            String flags = rows[i + 3].substring(rows[i + 3].indexOf('[') + 1, rows[i + 3].indexOf(']'));

            int status = 0;
            for (String s : flags.split(" ")) {
                if ("\\Seen".equals(s)) {
                    status = 1;
                    break;
                }
            }

            String[] dateSplit = rows[i + 10].split(" ");
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-ddHH:mm:ssZ");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            Date d = sdf1.parse(dateSplit[2] + dateSplit[3] + dateSplit[4]);
            String date = sdf2.format(d);
            long timestamp = d.getTime() / 1000;

            String[] toSplit = rows[i + 8].substring(3).trim().split(",");
            List<im.bzh.entity.Recipient> recipients = new ArrayList<>();

            for (String s : toSplit) {
                String[] split = s.trim().split(" ");
                String name = null;
                String address = null;
                if (split.length == 1) {
                    name = split[0].split("@")[0].trim().substring(1);
                    address = split[0].trim();
                } else {
                    name = split[0].trim();
                    address = split[1].trim();
                }
                name = MimeUtility.decodeText(name);
                address = MimeUtility.decodeText(address);
                recipients.add(new im.bzh.entity.Recipient(name, address));
            }

            Envelope e = new Envelope(id, null, null, subject, status, date, timestamp, recipients);

            result.add(e);
        }
        return result;
    }

    @Override
    public boolean markMailAsRead(String username, String folder, Integer[] ids) throws Exception {
        folder = formatFolderName(folder);
        String temp = Arrays.toString(ids);
        String idsStr = temp.substring(1, temp.length() - 1).replace(" ", "");
        String cmd = "maddy imap-msgs set-flags --uid " + username + "@" + domain + " " + folder + " " + idsStr + " \\Seen";
        Shell.exec(cmd, null);
        return true;
    }

    @Override
    public boolean markMailAsUnread(String username, String folder, Integer[] ids) throws Exception {
        folder = formatFolderName(folder);
        String temp = Arrays.toString(ids);
        String idsStr = temp.substring(1, temp.length() - 1).replace(" ", "");
        String cmd = "maddy imap-msgs rem-flags --uid " + username + "@" + domain + " " + folder + " " + idsStr + " \\Seen";
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
        String idsStr = temp.substring(1, temp.length() - 1).replace(" ", "");
        String cmd = "maddy imap-msgs move --uid " + username + "@" + domain + " " + folder + " " + idsStr + " " + destination;
        Shell.exec(cmd, null);
        return true;
    }

    @Override
    public Mail getMail(String username, String folder, Long id) throws Exception {
        folder = formatFolderName(folder);
        String cmd = "maddy imap-msgs dump --uid " + username + "@" + domain + " " + folder + " " + id;
        String mailStr = Shell.exec(cmd, null);

        Email email = EmailConverter.emlToEmail(mailStr);
        String subject = email.getSubject();
        String content = email.getHTMLText();
        if (content == null) {
            content = email.getPlainText();
        }
        String fromName = email.getFromRecipient().getName();
        fromName = fromName == null ? "" : fromName;
        String fromAddress = "<" + email.getFromRecipient().getAddress() + ">";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        String date = sdf.format(email.getSentDate());

        List<String> attachments = new ArrayList<>();

        for (AttachmentResource attachment : email.getAttachments()) {
            attachments.add(MimeUtility.decodeText(Objects.requireNonNull(attachment.getName())));
        }

        for (AttachmentResource attachment : email.getEmbeddedImages()) {
            attachments.add(MimeUtility.decodeText(Objects.requireNonNull(attachment.getName())));
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
                File file = new File(uploadPath + "/" + mailDTO.getAttachments());
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

            Mailer mailer = MailerBuilder.withSMTPServer(host, 465, mailDTO.getUsername() + "@" + domain, password)
                    .withTransportStrategy(TransportStrategy.SMTPS)
                    .buildMailer();
            AsyncResponse asyncResponse = mailer.sendMail(email, true);
            if (asyncResponse == null) {

            } else {
                asyncResponse.onSuccess(() -> {

                });
                asyncResponse.onException(System.out::println);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean saveMailToDrafts(MailDTO mailDTO) throws Exception {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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

    @Override
    public InputStream getAttachmentInputStream(String username, String folder, Long id, String fileName) throws Exception {
        folder = formatFolderName(folder);
        String cmd = "maddy imap-msgs dump --uid " + username + "@" + domain + " " + folder + " " + id;
        String mailStr = Shell.exec(cmd, null);
        Email email = EmailConverter.emlToEmail(mailStr);

        for (AttachmentResource attachment : email.getAttachments()) {
            if (fileName.equals(MimeUtility.decodeText(Objects.requireNonNull(attachment.getName())))) {
                return attachment.getDataSourceInputStream();
            }
        }

        for (AttachmentResource attachment : email.getEmbeddedImages()) {
            if (fileName.equals(MimeUtility.decodeText(Objects.requireNonNull(attachment.getName())))) {
                return attachment.getDataSourceInputStream();
            }
        }
        return null;
    }


}
