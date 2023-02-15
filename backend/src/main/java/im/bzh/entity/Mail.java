package im.bzh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Mail {
    private Long id;
    private String subject;
    private String content;
    private String fromName;
    private String fromAddress;
    private String date;
    private String[] attachments;
}
