package im.bzh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Envelope {
    private Long id;
    private String fromName;
    private String fromAddress;
    private String subject;
    private Integer status;
    private String date;
    private Long timestamp;
    private List<Recipient> recipients;
    public Envelope(Long id, String fromName, String fromAddress, String subject, Integer status, String date, Long timestamp) {
        this.id = id;
        this.fromName = fromName;
        this.fromAddress = fromAddress;
        this.subject = subject;
        this.status = status;
        this.date = date;
        this.timestamp = timestamp;
    }
}
