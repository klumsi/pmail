package im.bzh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailDTO {
    private String[] address;
    private String subject;
    private String originalAttachments;
    private String attachments;
    private String content;
    private String username;
    private String nickname;
}
